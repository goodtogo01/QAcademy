package com.qacademy.automation.core.backend;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.qacademy.automation.config.ConfigManager;
import com.qacademy.automation.core.api.ApiEndpoints;
import com.qacademy.automation.utils.LoggerUtil;

/**
 * Answers the exact concern this class exists for: "I clicked Run on a test without
 * running {@code mvn spring-boot:run} first - what happens?"
 *
 * Before this class: nothing. Every UI test's first Selenium action (loading the login
 * page) fails with a browser-level "site can't be reached", and every API test's first
 * RestAssured call fails with a raw ConnectException - both look like framework bugs,
 * not "the backend was never running", and you get one confusing failure per test class
 * instead of one clear one.
 *
 * What this does instead, wired into {@link com.qacademy.automation.core.base.BaseTest}'s
 * {@code @BeforeSuite} so it runs exactly once per suite, before the first UI or API test:
 * <ol>
 *   <li>Ask the backend directly, over real HTTP, whether it's already up.</li>
 *   <li>If yes - do nothing else. (You ran it yourself, or a previous suite left it
 *       running.) This is the common case and costs one fast HTTP call.</li>
 *   <li>If no - launch it ourselves as a background process (equivalent to typing
 *       {@code mvn spring-boot:run} yourself), and poll the same health check every
 *       2 seconds until it answers or a timeout is reached.</li>
 *   <li>If it still isn't up when the timeout expires - fail the suite immediately with
 *       one {@link BackendNotAvailableException}, pointing at the backend's own console
 *       log, instead of letting every test discover this separately.</li>
 * </ol>
 *
 * Why a real HTTP health request and not just "is the port open": a bare TCP connect
 * only proves Tomcat has bound the port - it can succeed while Spring is still wiring
 * the ApplicationContext, running the Flyway migration, or seeding demo data. Hitting
 * {@link ApiEndpoints#COURSE} - open to everyone by design - only succeeds once the
 * whole stack (web, JPA, Flyway, security) is actually serving requests, which is the
 * only definition of "ready" that matters to a UI or API test.
 *
 * Why Singleton: exactly one backend process must be started per suite run - two
 * {@code mvn spring-boot:run} instances would fight over port 8085 and the H2 file lock
 * (the app's own README documents this exact "Database may be already in use" case). A
 * Singleton guarantees every {@code @BeforeSuite}/{@code @AfterSuite} in every test class
 * shares one instance and one process handle, the same reasoning as
 * {@code ConfigManager} and {@code ExtentReportManager} elsewhere in this framework.
 *
 * Ownership rule: this only ever stops a backend it started itself.
 * {@link #shutdownIfStartedByUs()} is a no-op if the backend was already running when
 * {@link #ensureRunning()} checked - so it never kills an instance you're using yourself
 * for manual exploration alongside the suite.
 *
 * <h2>config.properties keys (all optional - every one has a hardcoded default)</h2>
 * <pre>
 * backend.healthCheckPath       = /api/course (ApiEndpoints.COURSE)  # checked against base.url
 * backend.autoStart             = true                     # false = fail fast instead of starting it
 * backend.autoStop              = true                     # false = leave it running after the suite
 * backend.startupTimeoutSeconds = 90                        # cold Maven+Spring Boot start budget
 * backend.projectDir            = .                         # folder containing pom.xml
 * backend.startCommand          = mvn spring-boot:run       # override, e.g. to run a pre-built jar instead
 * </pre>
 */
public final class BackendLifecycleManager {

	private static final Logger LOGGER = LoggerUtil.getLogger(BackendLifecycleManager.class);

	private static final class Holder {
		private static final BackendLifecycleManager INSTANCE = new BackendLifecycleManager();
	}

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(3))
			.build();

	private Process backendProcess;
	private volatile boolean startedByThisFramework = false;

	private BackendLifecycleManager() {
		// Singleton - see class Javadoc for why exactly one instance/process matters.
	}

	public static BackendLifecycleManager getInstance() {
		return Holder.INSTANCE;
	}

	/**
	 * Call once, from {@code BaseTest.beforeSuite()}. Blocks the suite from starting any
	 * test until the backend is confirmed reachable, or throws
	 * {@link BackendNotAvailableException} to fail the whole run cleanly.
	 */
	public synchronized void ensureRunning() {
		String baseUrl = ConfigManager.getInstance().getBaseUrl();
		String healthPath = ConfigManager.getInstance().getProperty("backend.healthCheckPath", ApiEndpoints.COURSE);
		String healthUrl = baseUrl + healthPath;

		if (isBackendUp(healthUrl)) {
			LOGGER.info("Backend already running at {} - using the existing instance as-is.", baseUrl);
			return;
		}

		boolean autoStart = Boolean.parseBoolean(ConfigManager.getInstance().getProperty("backend.autoStart", "true"));
		if (!autoStart) {
			throw new BackendNotAvailableException(
					"Backend is not running at " + baseUrl + ", and backend.autoStart=false in "
					+ "config.properties. Start it yourself first with `mvn spring-boot:run`, or set "
					+ "backend.autoStart=true to let the framework do it automatically.");
		}

		LOGGER.info("Backend not detected at {} - starting it automatically now. A cold Maven + Spring "
				+ "Boot start can take up to a minute; the suite will wait.", baseUrl);
		startBackend();
		waitUntilUp(healthUrl,
				Integer.parseInt(ConfigManager.getInstance().getProperty("backend.startupTimeoutSeconds", "90")));
		LOGGER.info("Backend is up at {} - proceeding with the suite.", baseUrl);
	}

	/**
	 * Call once, from {@code BaseTest.afterSuite()}. Does nothing unless this exact
	 * manager was the one that started the process (see the ownership rule in the class
	 * Javadoc), and does nothing if {@code backend.autoStop=false}.
	 */
	public synchronized void shutdownIfStartedByUs() {
		if (!startedByThisFramework || backendProcess == null) {
			return;
		}
		if (!Boolean.parseBoolean(ConfigManager.getInstance().getProperty("backend.autoStop", "true"))) {
			LOGGER.info("backend.autoStop=false - leaving the backend running for manual follow-up.");
			return;
		}
		LOGGER.info("Stopping the backend instance this framework started...");
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

	// ---------------------------------------------------------------------
	// internals
	// ---------------------------------------------------------------------

	/**
	 * Kills the whole process tree, not just the immediate child. This matters because
	 * {@code startCommand} runs through a shell ({@code sh -c "mvn spring-boot:run"}),
	 * and Maven itself forks the actual Spring Boot JVM as a child of that shell -
	 * {@link Process#destroy()} only signals the direct child (the shell), leaving the
	 * real JVM underneath it running and silently holding port 8085 and the H2 file lock.
	 * {@link Process#descendants()} (Java 9+) is what makes killing the whole tree
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
		String projectDir = ConfigManager.getInstance().getProperty("backend.projectDir", ".");
		String mavenExecutable = isWindows() ? "mvn.cmd" : "mvn";
		String startCommand = ConfigManager.getInstance()
				.getProperty("backend.startCommand", mavenExecutable + " spring-boot:run");

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
}
