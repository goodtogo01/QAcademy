package com.qacademy.automation.utils;

 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thin wrapper over SLF4J so the rest of the framework never imports LoggerFactory directly 
 * if the logging backend ever changes, this is the only class that needs to.
 */


public final class LoggerUtil {

	private LoggerUtil() {
		 // static holder - never instantiated
	}
	
	public static Logger getLogger(Class<?> clazz) {
		return LoggerFactory.getLogger(clazz);
	}
}
