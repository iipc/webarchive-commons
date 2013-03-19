package org.archive.url;

/**
 * Idea of these rules is to accomplish roughly the equivalent of
 * {@link UsableURIFactory} fixup plus {@link BasicURLCanonicalizer} fixup.
 */
public class OrdinaryIACanonicalizerRules extends CanonicalizeRules {
	public OrdinaryIACanonicalizerRules() {
		setRule(SCHEME_SETTINGS, SCHEME_LOWERCASE);
		setRule(HOST_SETTINGS, HOST_LOWERCASE);
		setRule(PORT_SETTINGS, PORT_STRIP_DEFAULT);
		setRule(QUERY_SETTINGS, QUERY_STRIP_EMPTY);
		setRule(HASH_SETTINGS, HASH_STRIP);
	}

}
