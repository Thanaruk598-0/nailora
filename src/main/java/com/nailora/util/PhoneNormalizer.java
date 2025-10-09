package com.nailora.util;

public final class PhoneNormalizer {
	private PhoneNormalizer() {
	}

	public static String normalize(String raw) {
		if (raw == null)
			return null;
		var p = raw.replaceAll("[^0-9]", "");
// Basic TH rule: if starts with 66 and length 11 -> convert to 0xxxxxxxxx
		if (p.startsWith("66") && p.length() == 11) {
			p = "0" + p.substring(2);
		}
		return p;
	}
}
