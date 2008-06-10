package org.archive.accesscontrol.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.archive.surt.NewSurtTokenizer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * A set of acess control rules which can be queried to find the governing rule
 * for a particular request.
 * 
 * @author aosborne
 * 
 */
public class RuleSet implements Iterable<Rule> {
    protected HashMap<String, TreeSet<Rule>> rulemap = new HashMap<String, TreeSet<Rule>>();

    class RuleSetIterator implements Iterator<Rule> {
        private Iterator<TreeSet<Rule>> mapIterator;
        private Iterator<Rule> setIterator;

        public RuleSetIterator() {
            mapIterator = rulemap.values().iterator();
            setIterator = null;
            hasNext();
        }

        public boolean hasNext() {
            while (true) {
                if (setIterator != null && setIterator.hasNext())
                    return true;
                if (!mapIterator.hasNext())
                    return false;
                setIterator = mapIterator.next().iterator();
            }
        }

        public Rule next() {
            if (hasNext()) {
                return setIterator.next();
            }
            return null;
        }

        public void remove() {
            throw new NotImplementedException();
        }
    }

    public RuleSet() {
        super();
    }

    /**
     * Return the most specific matching rule for the given request.
     * 
     * @param surt
     * @param captureDate
     * @param retrievalDate
     * @param who
     *            group
     * @return
     */
    public Rule getMatchingRule(String surt, Date captureDate,
            Date retrievalDate, String who) {

        NewSurtTokenizer tok = new NewSurtTokenizer(surt);

        for (String key: tok.getSearchList()) {
            Iterable<Rule> rules = rulemap.get(key);
            if (rules != null) {
                for (Rule rule : rules) {
                    if (rule.matches(surt, captureDate, retrievalDate, who)) {
                        return rule;
                    }
                }
            }
        }
        return null;
    }

    public void addAll(Iterable<Rule> rules) {
        for (Rule rule : rules) {
            add(rule);
        }
    }

    public void add(Rule rule) {
        String surt = rule.getSurt();
        TreeSet<Rule> set = rulemap.get(surt);
        if (set == null) {
            set = new TreeSet<Rule>();
            rulemap.put(surt, set);
        }
        set.add(rule);
    }

    public Iterator<Rule> iterator() {
        return new RuleSetIterator();
    }

}
