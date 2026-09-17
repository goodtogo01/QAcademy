package com.qacademy.automation.core.backend;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Answers the exact concern this class exists for: "I clicked Run on a test without
 * running {@code mvn spring-boot:run} first — what happens?"
 *
 * <p><b>Before this class:</b> nothing. Every UI test's very first Selenium action
 * (loading the login page) fails with a browser-level "This site can't be reached", and
 * every API test's first RestAssured call fails with a raw {@code ConnectException}.
 * Both look like framework bugs, not like "the backend was never running", and — worse —
 * you get one confusing failure per test class instead of one clear one.</p>
 *
 * <p><b>What this class does instead:</b> exactly once per suite run (wired into
 * {@code BaseTest.@BeforeSuite}, so it runs before the very first test method, UI or
 * API, regardless of how many test classes there are):</p>
 * <ol>
 *   <li>Ask the backend directly, over real HTTP, whether it's already up.</li>
 *   <li>If yes — do nothing else. (You ran it yourself, or a previous suite left it
 *       running.) This is the common case and costs one fast HTTP call.</li>
 *   <li>If no — launch it ourselves as a background process (same as typing
 *       {@code mvn spring-boot:run} yourself), and poll the same health check every
 *       2 seconds until it answers or a timeout is reached.</li>
 *   <li>If it still isn't up when the timeout expires — fail the suite immediately with
 *       one clear {@link BackendNotAvailableException}, pointing at the backend's own
 *       console log, instead of letting every test discover this separately.</li>
 * </ol>
 *
 * <p><b>Why a health <em>request</em> and not just "is the port open":</b> a bare TCP
 * connect only proves Tomcat has bound the port — it can succeed while Spring is still
 * wiring the ApplicationContext, running Flyway migrations, or seeding demo data. Hitting
 * a real endpoint ({@code GET /api/course}, open to everyone by design — see the Courses
 * module in the Project Guide) only succeeds once the whole stack — web, JPA, Flyway,
 * security — is actually serving requests, which is the only definition of "ready" that
 * matters to a UI or API test.</p>
 *
 * <p><b>Why Singleton:</b> exactly one backend process must be started per suite run —
 * two {@code mvn spring-boot:run} instances would fight over port 8085 and the H2 file
 * lock (this is the app's own documented "Database may be already in use" case). A
 * Singleton is what guarantees every {@code @BeforeSuite}/{@code @AfterSuite} call in
 * every test class — even across parallel suites in the same JVM — shares one instance
 * and one process handle.</p>
 *
 * <p><b>Ownership rule:</b> this class only ever stops a backend it started itself. If it
 * finds the backend already running, {@link #shutdownIfStartedByUs()} is a no-op — so it
 * never kills an instance you're using yourself for manual exploration alongside the
 * suite.</p>
 *
 * <p>Reads {@code config.properties} directly (see the keys below) rather than going
 * through {@code ConfigManager} — once that Singleton exists (Automation Framework
 * Design §4 step 2), swap {@link #loadConfig()} to delegate to it instead of loading the
 * file a second time. Everything here works standalone in the meantime.</p>
 *
 * <h2>config.properties keys (all optional — every one has a hardcoded default)</h2>
 * <pre>
 * backend.baseUrl               = http://localhost:8085   # where the app listens
 * backend.healthCheckPath       = /api/course              # any open, DB-backed GET endpoint
 * backend.autoStart             = true                     # false = fail fast instead of starting it
 * backend.autoStop              = true                     # false = leave it running after the suite
 * backend.startupTimeoutSeconds = 90                        # cold Maven+Spring Boot start budget
 * backend.projectDir            = .                         # folder containing pom.xml
 * backend.startCommand          = mvn spring-boot:run       # override, e.g. to run a pre-built jar instead
 * </pre>
 */
public final class BackendLifecycleManager {

    private static final BackendLifecycleManager INSTANCE = new BackendLifecycleManager();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private final Properties config = loadConfig();

    private Process backendProcess;
    private volatile boolean startedByThisFramework = false;

    private BackendLifecycleManager() {
        // Singleton - see class Javadoc for why exactly one instance/process matters.
    }

    public static BackendLifecycleManager getInstance() {
        return INSTANCE;
    }

    /**
     * Call once, from {@code BaseTest.@BeforeSuite(alwaysRun = true)}. Blocks the suite
     * from starting any test until the backend is confirmed reachable, or throws
     * {@link BackendNotAvailableException} to fail the whole run cleanly.
     */
    public synchronized void ensureRunning() {
        String baseUrl = prop("backend.baseUrl", "http://localhost:8085");
        String healthPath = prop("backend.healthCheckPath", "/api/course");
        String healthUrl = baseUrl + healthPath;

        if (isBackendUp(healthUrl)) {
            log("Backend already running at " + baseUrl + " - using the existing instance as-is.");
            return;
        }

        boolean autoStart = Boolean.parseBoolean(prop("backend.autoStart", "true"));
        if (!autoStart) {
            throw new BackendNotAvailableException(
                    "Backend is not running at " + baseUrl + ", and backend.autoStart=false in "
                  + "config.properties. Start it yourself first with `mvn spring-boot:run`, or set "
                  + "backend.autoStart=true to let the framework do it automatically.");
        }

        log("Backend not detected at " + baseUrl + " - starting it automatically now. "
          + "A cold Maven + Spring Boot start can take up to a minute; the suite will wait.");
        startBackend();
        waitUntilUp(healthUrl, Integer.parseInt(prop("backend.startupTimeoutSeconds", "90")));
        log("Backend is up at " + baseUrl + " - proceeding with the suite.");
    }

    /**
     * Call once, from {@code BaseTest.@AfterSuite(alwaysRun = true)}. Does nothing unless
     * this exact manager was the one that started the process (see the ownership rule in
     * the class Javadoc), and does nothing if {@code backend.autoStop=false}.
     */
    public synchronized void shutdownIfStartedByUs() {
        if (!startedByThisFramework || backendProcess == null) {
            return;
        }
        if (!Boolean.parseBoolean(prop("backend.autoStop", "true"))) {
            log("backend.autoStop=false - leaving the backend running for manual follow-up.");
            return;
        }
        log("Stopping the backend instance this framework started...");
        killProcessTree(backendProcess, false);
        try {
            if (!backendProcess.waitFor(10, TimeUnit.SECONDS)) {
                killProcessTree(backendProcess, true);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            killProcessTree(backendProcess, true);
        } finally {
            startedByThisFramework = false;
            backendProcess = null;
        }
    }

    /**
     * Kills the whole process tree, not just the immediate child. This matters because
     * {@code startCommand} normally runs through a shell ({@code sh -c "mvn spring-boot:run"}),
     * and Maven itself typically forks the actual Spring Boot JVM as a *child of the shell*.
     * {@link Process#destroy()} only signals that one direct child (the shell) - the real
     * JVM underneath it is left running, silently keeping port 8085 and the H2 file lock
     * held. {@link Process#descendants()} (Java 9+) is what makes killing the whole tree
     * possible without shelling out to {@code pkill}/{@code taskkill}.
     */
    private static void killProcessTree(Process process, boolean force) {
        process.descendants().forEach(handle -> {
            if (force) {
                handle.destroyForcibly();
            } else {
                handle.destroy();
            }
        });
        if (force) {
            process.destroyForcibly();
        } else {
            process.destroy();
        }
    }

    // ---------------------------------------------------------------------
    // internals
    // ---------------------------------------------------------------------

    private boolean isBackendUp(String healthUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(healthUrl))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            // Any real HTTP status (even a 4xx) means Tomcat + Spring answered the request -
            // that is the definition of "up" here. Only a connection failure means "down".
            return response.statusCode() < 500;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private void startBackend() {
        String projectDir = prop("backend.projectDir", ".");
        String mavenExecutable = isWindows() ? "mvn.cmd" : "mvn";
        String startCommand = prop("backend.startCommand", mavenExecutable + " spring-boot:run");

        try {
            File logDir = new File("target/automation-logs");
            if (!logDir.exists() && !logDir.mkdirs() && !logDir.exists()) {
                throw new IOException("Could not create log directory: " + logDir.getAbsolutePath());
            }
            File logFile = new File(logDir, "backend-console.log");

            ProcessBuilder builder = isWindows()
                    ? new ProcessBuilder("cmd.exe", "/c", startCommand)
                    : new ProcessBuilder("sh", "-c", startCommand);
            builder.directory(new File(projectDir));
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.to(logFile));

            backendProcess = builder.start();
            startedByThisFramework = true;

            // Safety net: if the JVM running the suite is killed (Ctrl+C, IDE stop button)
            // before @AfterSuite gets to run, don't leave an orphaned Spring Boot process
            // holding port 8085 and the H2 file lock for the next run to trip over.
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownIfStartedByUs));
        } catch (IOException e) {
            throw new BackendNotAvailableException(
                    "Could not launch the backend with `" + startCommand + "` in directory `"
                  + new File(projectDir).getAbsolutePath() + "`. See the cause below for the OS-level error.",
                    e);
        }
    }

    private void waitUntilUp(String healthUrl, int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        while (System.currentTimeMillis() < deadline) {
            if (isBackendUp(healthUrl)) {
                return;
            }
            if (backendProcess != null && !backendProcess.isAlive()) {
                throw new BackendNotAvailableException(
                        "The backend process exited before it ever came up (exit code "
                      + backendProcess.exitValue() + "). Check target/automation-logs/backend-console.log "
                      + "for the real error - a common one is \"Database may be already in use\", meaning "
                      + "another copy of the app is still running somewhere and needs to be stopped first.");
            }
            sleepQuietly(2000);
        }
        throw new BackendNotAvailableException(
                "Backend did not respond at " + healthUrl + " within " + timeoutSeconds + "s. Check "
              + "target/automation-logs/backend-console.log, or raise backend.startupTimeoutSeconds "
              + "in config.properties if this machine's cold start is just slower than that.");
    }

    private String prop(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void log(String message) {
        System.out.println("[BackendLifecycleManager] " + message);
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream in = BackendLifecycleManager.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // Fall through - every prop() call carries its own hardcoded default,
            // so a missing or unreadable file degrades gracefully rather than failing here.
        }
        return props;
    }
}
