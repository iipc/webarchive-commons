package org.archive.accesscontrol;

import org.archive.accesscontrol.model.RuleSet;

/**
 * CachingRuleDao is a wrapper for another RuleDao that implements in-memory
 * caching of the rule trees.
 * 
 * @author aosborne
 * 
 */
public class CachingRuleDao extends LruCache<String, RuleSet> implements
        RuleDao {
    private static final long serialVersionUID = 1L;
    protected RuleDao ruleDao;

    public CachingRuleDao(RuleDao ruleDao) {
        super();
        this.ruleDao = ruleDao;
    }

    public CachingRuleDao(String oracleUrl) {
        this(new HttpRuleDao(oracleUrl));
    }

    public RuleDao getRuleDao() {
        return ruleDao;
    }

    public void setRuleDao(RuleDao ruleDao) {
        this.ruleDao = ruleDao;
    }

    public RuleSet getRuleTree(String surt) {
        RuleSet rules = super.get(surt);
        if (rules == null) {
            rules = ruleDao.getRuleTree(surt);
            super.put(surt, rules);
        }
        return rules;
    }
}
