package com.nailora.service;

public interface RateLimitService {
	boolean tryConsume(String key, int maxPerMinute);
}