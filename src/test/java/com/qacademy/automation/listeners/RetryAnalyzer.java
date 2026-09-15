package com.qacademy.automation.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test once before letting it fail for good. TestNG creates
 * a fresh RetryAnalyzer instance per test method invocation, so retryCount
 * naturally resets between different tests without any extra bookkeeping.
 */


public class RetryAnalyzer implements IRetryAnalyzer {
	
	private static final int MAX_RETRY_COUNT = 1;
	
	private int retryCount = 0;
	
	@Override
	public boolean retry(ITestResult result) {
		if(retryCount < MAX_RETRY_COUNT) {
			retryCount++;
			return true;
		}
		return false;
	}

}
