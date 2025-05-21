package org.archive.url;

public class NonMassagingIAURLCanonicalizer implements URLCanonicalizer {
	private static final BasicURLCanonicalizer basic =
		new BasicURLCanonicalizer();
	private static CanonicalizeRules nonMassagingRules = 
		new AggressiveIACanonicalizerRules();
	static {
		nonMassagingRules.setRule(CanonicalizeRules.HOST_SETTINGS,
				CanonicalizeRules.HOST_LOWERCASE);
	}
	private static final IAURLCanonicalizer ia = 
		new IAURLCanonicalizer(nonMassagingRules);

	public void canonicalize(HandyURL url) {
		// just google's stuff, followed by the IA default stuff:
		basic.canonicalize(url);
		ia.canonicalize(url);
	}
}
