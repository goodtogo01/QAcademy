package com.qacademy.automation.core.driver;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import com.qacademy.automation.config.ConfigManager;

/**
 * Factory Method: creates a WebDriver per BrowserType.
 *
 * Callers never call `new ChromeDriver()` directly - they ask this factory for "whatever
 * browser config.properties says", so adding a new browser later means adding one case
 * here, not touching every test or page object.
 */

public final class DriverFactory {

	
	private DriverFactory() {
		// static factory - never instantiated - constructor/default
		
	}
    /** Reads which browser to launch from config.properties (the "browser" key). */
	public static WebDriver createDriver() {
		String browserName = ConfigManager.getInstance().getBrowser();
		BrowserType browserType;
		
		try {
			browserType = BrowserType.valueOf(browserName.trim().toUpperCase());
		}catch(IllegalArgumentException e) {
			throw new IllegalStateException(
  "Unknown browser '" + browserName + "' in config.properties. Expected one of: CHROME, FIREFOX, EDGE.");
		}
		return createDriver(browserType);
	}
	
	  /** Creates a driver for an explicitly chosen browser, bypassing config.properties. */
    public static WebDriver createDriver(BrowserType browserType) {
    	boolean headless = ConfigManager.getInstance().isHeadless();
    	WebDriver driver;
    	switch (browserType) {
    	case CHROME:
    		ChromeOptions chromeOptions = new ChromeOptions();
    		if (headless) {
    			// --headless=new: Chrome's modern headless mode (the old --headless is
    			// deprecated and behaves subtly differently from a real window in some
    			// cases). --window-size replaces window().maximize() below, which has no
    			// real screen to maximize against on a build agent. --no-sandbox and
    			// --disable-dev-shm-usage are the two flags every CI guide for Chrome
    			// converges on: CI containers commonly run as root (Chrome's sandbox
    			// refuses to start as root) and with a tiny /dev/shm, both of which
    			// otherwise crash Chrome on launch, not just slow it down.
    			chromeOptions.addArguments(
    					"--headless=new",
    					"--window-size=1920,1080",
    					"--no-sandbox",
    					"--disable-dev-shm-usage",
    					"--disable-gpu");
    		}
    		driver = new ChromeDriver(chromeOptions);
    		break;
    	case FIREFOX:
    		FirefoxOptions firefoxOptions = new FirefoxOptions();
    		if (headless) {
    			firefoxOptions.addArguments("-headless");
    		}
    		driver = new FirefoxDriver(firefoxOptions);
    		break;
    	case EDGE:
    		// EdgeOptions intentionally not wired for headless yet - config.properties'
    		// "browser" key defaults to chrome and nothing in this framework switches it
    		// to edge today. Follow the ChromeOptions pattern above if that changes.
    		driver = new EdgeDriver();
    		break;
    		default:
    			throw new IllegalStateException("No WebDriver wiring for browser type: " + browserType);
        }
    	applyCommonSettings(driver, headless);
    	return driver;
    }
    
    public static void applyCommonSettings(WebDriver driver, boolean headless) {
    	int implicitWaitSeconds = ConfigManager.getInstance().getImplicitWaitSeconds();
    	driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitSeconds));
    	if (!headless) {
    		// maximize() has no meaningful effect (and an inconsistent one, depending on
    		// ChromeDriver version) against a headless browser's virtual display - the
    		// explicit --window-size argument above already fixes the viewport size for
    		// that case, so this only runs for a real, visible local browser window.
    		driver.manage().window().maximize();
    	}
    }
	/**
	 * Browsers DriverFactory knows how to create. Kept in this file rather than its own,
	 * since it exists only to parameterize DriverFactory's factory method.
	 */
	enum BrowserType{
		CHROME, FIREFOX, EDGE
	}
}
