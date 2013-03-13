package org.archive.url;

public class AggressiveIAURLCanonicalizer implements URLCanonicalizer {
	private static final BasicURLCanonicalizer basic = 
			new BasicURLCanonicalizer();
	
		private static final IAURLCanonicalizer ia = 
			new IAURLCanonicalizer(new AggressiveIACanonicalizerRules());

		public void canonicalize(HandyURL url) {
			// just google's stuff, followed by the IA default stuff:
			basic.canonicalize(url);
			ia.canonicalize(url);
		}
}
