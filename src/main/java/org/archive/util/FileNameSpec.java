package org.archive.util;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class FileNameSpec {
//	private final static String DEFAULT_PREFIX_PATTERN = "UNK-%h-%p-%t-%s";
	private AtomicInteger aInt;
	private String prefix;
	private String suffix;
	public FileNameSpec(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
		aInt = new AtomicInteger(-1);
	}
	public String getNextName() {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append(String.format(Locale.ROOT, "%06d",aInt.incrementAndGet()));
		sb.append(suffix);
		return sb.toString();
	}
}
