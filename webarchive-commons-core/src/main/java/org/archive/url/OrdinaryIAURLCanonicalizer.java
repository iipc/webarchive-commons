package org.archive.url;

/**
 * Idea of this canonicalizer is to accomplish roughly the equivalent of
 * {@link UsableURIFactory} fixup plus {@link BasicURLCanonicalizer} fixup.
 */
public class OrdinaryIAURLCanonicalizer implements URLCanonicalizer {
	private static final BasicURLCanonicalizer basic = new BasicURLCanonicalizer();

	private static final IAURLCanonicalizer ia = 
			new IAURLCanonicalizer(new OrdinaryIACanonicalizerRules());

	public void canonicalize(HandyURL url) {
		basic.canonicalize(url);
		ia.canonicalize(url);
	}
}
