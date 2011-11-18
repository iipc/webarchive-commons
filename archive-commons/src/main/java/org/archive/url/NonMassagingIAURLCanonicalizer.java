package org.archive.url;

public class NonMassagingIAURLCanonicalizer implements URLCanonicalizer {
	private static final GoogleURLCanonicalizer google = 
		new GoogleURLCanonicalizer();
	private static CanonicalizeRules nonMassagingRules = 
		new DefaultIACanonicalizerRules();
	static {
		nonMassagingRules.setRule(CanonicalizeRules.HOST_SETTINGS,
				CanonicalizeRules.HOST_LOWERCASE);
	}
	private static final IAURLCanonicalizer ia = 
		new IAURLCanonicalizer(nonMassagingRules);

	public void canonicalize(HandyURL url) {
		// just google's stuff, followed by the IA default stuff:
		google.canonicalize(url);
		ia.canonicalize(url);
	}
}
