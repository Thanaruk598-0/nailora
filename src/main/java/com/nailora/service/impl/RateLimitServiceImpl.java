package com.nailora.service.impl;

import com.nailora.service.RateLimitService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitServiceImpl implements RateLimitService {
	private final Map<String, Window> buckets = new ConcurrentHashMap<>();

	@Override
	public boolean tryConsume(String key, int maxPerMinute) {
		var now = Instant.now().getEpochSecond() / 60; // minute window
		var win = buckets.computeIfAbsent(key, k -> new Window(now, 0));
		synchronized (win) {
			if (win.minute != now) {
				win.minute = now;
				win.count = 0;
			}
			if (win.count >= maxPerMinute)
				return false;
			win.count++;
			return true;
		}
	}

	private static class Window {
		long minute;
		int count;

		Window(long m, int c) {
			minute = m;
			count = c;
		}
	}
}