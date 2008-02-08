package org.archive.accesscontrol;

import java.util.Date;

import org.archive.accesscontrol.model.Rule;
import org.archive.accesscontrol.model.RuleSet;
import org.archive.net.PublicSuffixes;
import org.archive.util.SURT;

/**
 * The Exclusions Client provides a facade for accessing a remote or local
 * exclusions oracle.
 * 
 * In future it will perform heavy caching to prevent queries about related and
 * recently-accessed pages from needing to hit the oracle.
 * 
 * @author aosborne
 */
public class AccessControlClient {
    protected RuleDao ruleDao;

    public AccessControlClient(RuleDao ruleDao) {
        super();
        this.ruleDao = ruleDao;
    }

    /**
     * Create a new (caching) client to query a remote oracle.
     * 
     * @param oracleUrl
     *            Base url of the oracle webapp. eg.
     *            "http://localhost:8080/exclusions-oracle/"
     */
    public AccessControlClient(String oracleUrl) {
        this(new CachingRuleDao(oracleUrl));
    }

    /**
     * Return the best-matching policy for the requested document.
     * 
     * @param url
     *            URL of the requested document.
     * @param captureDate
     *            Date the document was archived.
     * @param retrievalDate
     *            Date of retrieval (usually now).
     * @param who
     *            Group name of the user accessing the document.
     * @return Access-control policy that should be enforced. eg "robots",
     *         "block" or "allow".
     */
    public String getPolicy(String url, Date captureDate, Date retrievalDate,
            String who) {
        Rule matchingRule = getRule(url, captureDate, retrievalDate, who);
        return matchingRule.getPolicy();
    }

    /**
     * Return the most specific matching rule for the requested document.
     * 
     * @param url
     *            URL of the requested document.
     * @param captureDate
     *            Date the document was archived.
     * @param retrievalDate
     *            Date of retrieval (usually now).
     * @param who
     *            Group name of the user accessing the document.
     * @return
     */
    public Rule getRule(String url, Date captureDate, Date retrievalDate,
            String who) {
        String surt = SURT.fromURI(url);
        String publicSuffix = PublicSuffixes
                .reduceSurtToTopmostAssigned(getSurtAuthority(surt));

        surt = stripScheme(surt);

        RuleSet rules = ruleDao.getRuleTree("(" + publicSuffix);

        Rule matchingRule = rules.getMatchingRule(surt, captureDate,
                retrievalDate, who);
        return matchingRule;
    }

    protected String getSurtAuthority(String surt) {
        int indexOfOpen = surt.indexOf("://(");
        int indexOfClose = surt.indexOf(")");
        if (indexOfOpen == -1 || indexOfClose == -1
                || ((indexOfOpen + 4) >= indexOfClose)) {
            return surt;
        }
        return surt.substring(indexOfOpen + 4, indexOfClose);
    }

    protected static String stripScheme(String surt) {
        int i = surt.indexOf("://");
        int j = surt.indexOf(":");
        if (i >= 0 && i == j) {
            return surt.substring(i + 3);
        } else {
            return surt;
        }
    }
}
