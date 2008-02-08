package org.archive.accesscontrol;

import org.apache.commons.httpclient.URIException;
import org.archive.accesscontrol.model.RuleSet;

/**
 * A RuleDao provides methods for retrieving rule information from a local database or
 * remote oracle.
 * 
 * @author aosborne
 * 
 */
public interface RuleDao {

    /**
     * Returns the "rule tree" for a given SURT. This is a sorted set of all
     * rules equal or lower in specificity than the given SURT plus all rules on
     * the path from this SURT to the root SURT "(".
     * 
     * The intention is to call this function with a domain or public suffix,
     * then queries within that domain can be made very fast by searching the
     * resulting list.
     * 
     * @param surt
     * @return
     * @throws URIException
     */
    public RuleSet getRuleTree(String surt);

}
