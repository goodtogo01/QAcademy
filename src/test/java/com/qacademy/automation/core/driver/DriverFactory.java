package com.qacademy.automation.core.driver;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
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
    	WebDriver driver;
    	switch (browserType) {
    	case CHROME:
    		driver = new ChromeDriver(new ChromeOptions());
    		break;
    	case FIREFOX:
    		driver = new FirefoxDriver();
    		break;
    	case EDGE:
    		driver = new EdgeDriver();
    		break;
    		default:
    			throw new IllegalStateException("No WebDriver wiring for browser type: " + browserType);
        }
    	applyCommonSettings(driver);
    	return driver;
    }
    
    public static void applyCommonSettings(WebDriver driver) {
    	int implicitWaitSeconds = ConfigManager.getInstance().getImplicitWaitSeconds();
    	driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitSeconds));
    	driver.manage().window().maximize();
    }
	/**
	 * Browsers DriverFactory knows how to create. Kept in this file rather than its own,
	 * since it exists only to parameterize DriverFactory's factory method.
	 */
	enum BrowserType{
		CHROME, FIREFOX, EDGE
	}
}
