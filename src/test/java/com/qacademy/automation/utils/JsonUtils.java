package com.qacademy.automation.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serialize/deserialize helpers for API payloads, backed by a single shared ObjectMapper. 
 * No JavaTimeModule registered on purpose - our request/response
 * records use String for date fields instead of Instant/LocalDateTime, so we
 * don't need jackson-datatype-jsr310 as a dependency at all.
 */
public final class JsonUtils {
	
	private static final  ObjectMapper MAPPER = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
	private JsonUtils() {
		 // static holder - never instantiated
	}

	public static String toJson(Object payload) {
		try {
			return MAPPER.writeValueAsString(payload);
		}catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Failed to serialize "+ payload.getClass()
			.getSimpleName()+" to JSON",e);
		}
	}
	
	public static <T> T fromJson(String json, Class<T> type) {
		try {
			return MAPPER.readValue(json, type);
		}catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Failed to deserialize JSON to "+type.getSimpleName(), e);
		}
	}
}
