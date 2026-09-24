package com.qacademy.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton that loads config.properties for base URL, browser, and environment settings.
 *
 * Uses the initialization-on-demand holder idiom (a private static inner class) rather
 * than a synchronized getInstance() or eager field initialization: the JVM only loads the
 * Holder class - and therefore only reads config.properties - the first time getInstance()
 * is actually called, and that load is thread-safe with zero locking overhead.
 */
public final class ConfigManager {

    private static final String CONFIG_FILE = "config.properties";

    private final Properties properties;

    private ConfigManager() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException(
                        "Could not find " + CONFIG_FILE + " on the test classpath (expected under src/test/resources).");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
    }

    private static final class Holder {
        private static final ConfigManager INSTANCE = new ConfigManager();
    }

    public static ConfigManager getInstance() {
        return Holder.INSTANCE;
    }

    public String getBaseUrl() {
        return getProperty("base.url", "http://localhost:8085");
    }

    public String getBrowser() {
        return getProperty("browser", "chrome");
    }

    public Environment getEnvironment() {
        String value = getProperty("env", Environment.LOCAL.name());
        try {
            return Environment.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Unknown env '" + value + "' in " + CONFIG_FILE + ". Expected one of: LOCAL, CI.");
        }
    }

    /**
     * Whether DriverFactory should launch the browser headless (no visible window).
     *
     * Resolved in this order:
     * 1. A "-Dheadless=..." JVM system property, if present - e.g. `mvn test
     *    -Dheadless=true` from a terminal, an IDE run configuration's VM arguments, or a
     *    Jenkins build parameter passed through the same way (see the Jenkinsfile's
     *    HEADLESS parameter). This always wins when set, with no need to edit
     *    config.properties for a one-off run.
     * 2. Otherwise, config.properties' "headless" key.
     *
     * Either source accepts the same three values:
     * - "true" / "false": explicit override, always wins.
     * - "auto" (the default): headless only when this run looks like a CI build - either
     *   env=CI in config.properties, or one of the environment variables a CI runner sets
     *   for itself (GITHUB_ACTIONS on GitHub Actions runners, JENKINS_URL on a Jenkins
     *   agent, or the generic CI variable most other CI systems set). A plain local
     *   `mvn test` run matches none of these, so a developer's own machine keeps showing
     *   the real browser window exactly as it always has - this only changes behavior
     *   once these tests actually run on a build agent, which has no display to show a
     *   window on in the first place.
     */
    public boolean isHeadless() {
        String systemPropertyOverride = System.getProperty("headless");
        String value = (systemPropertyOverride != null && !systemPropertyOverride.isBlank())
                ? systemPropertyOverride.trim()
                : getProperty("headless", "auto").trim();
        if (!"auto".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        return getEnvironment() == Environment.CI
                || System.getenv("CI") != null
                || System.getenv("GITHUB_ACTIONS") != null
                || System.getenv("JENKINS_URL") != null;
    }

    public int getImplicitWaitSeconds() {
        return Integer.parseInt(getProperty("implicit.wait.seconds", "10"));
    }

    public int getExplicitWaitSeconds() {
        return Integer.parseInt(getProperty("explicit.wait.seconds", "15"));
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
