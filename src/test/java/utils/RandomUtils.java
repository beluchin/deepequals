package utils;

import java.util.UUID;

public final class RandomUtils {
	public static String uniqueString() {
		return UUID.randomUUID().toString();
	}
}
