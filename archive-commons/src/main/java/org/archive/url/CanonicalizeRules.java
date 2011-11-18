package org.archive.url;

public class CanonicalizeRules implements CanonicalizerConstants {
	private int[] settings = new int[NUM_SETTINGS];

	public void setRule(int rule, int value) {
		settings[rule] = value;
	}
	public int getRule(int rule) {
		return settings[rule];
	}
	public boolean isSet(int rule, int value) {
		return (settings[rule] & value) == value;
	}
}
