package org.archive.url;

public class DefaultIAURLCanonicalizer implements URLCanonicalizer {
	private static final GoogleURLCanonicalizer google = 
		new GoogleURLCanonicalizer();
	private static final IAURLCanonicalizer ia = 
		new IAURLCanonicalizer(new DefaultIACanonicalizerRules());

	public void canonicalize(HandyURL url) {
		// just google's stuff, followed by the IA default stuff:
		google.canonicalize(url);
		ia.canonicalize(url);
	}
}
