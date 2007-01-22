/* WaxDateQueryFilter
 * 
 * $Id$
 * 
 * Created 06/02/2005
 * 
 * Copyright (C) 2005 Internet Archive.
 * 
 * This file is part of the archive-access tools project
 * (http://sourceforge.net/projects/archive-access).
 * 
 * The archive-access tools are free software; you can redistribute them and/or
 * modify them under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or any
 * later version.
 * 
 * The archive-access tools are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * the archive-access tools; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.archive.access.nutch.searcher;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.QueryException;
import org.apache.nutch.searcher.QueryFilter;
import org.apache.nutch.searcher.Query.Clause;
import org.archive.util.ArchiveUtils;

/**
 * Based on query-more DateQueryFilter from nutch.
 * Query syntax is defined as date:YYYYMMDDmmssSS, i.e. the IA 14 digit date
 * format or for ranges, date:YYYY...-YYYY... where YYYY... is at least a year
 * optionally followed by month, day, etc. up to the granularity of the 14
 * digit IA date format. Date as date:-YYYYMMDD for a range that is from the
 * start of the epoch up to and inclusive of YYYYMMDD and its inverse doesn't
 * work because the nutch query parser is stripping the '-' (Its probably
 * interpreting it as NOT).
 * 
 * @author St.Ack
 */
public class WaxDateQueryFilter implements QueryFilter {
    public static final Log LOGGER =
        LogFactory.getLog(WaxDateQueryFilter.class.getName());
    
    private Configuration conf;

    private static final String FIELD_NAME = "date";

    /**
     * Query syntax is defined as date:YYYYMMddmmssSS, or for ranges,
     * date:YYYYMMddmmssSS-yyyyMMDDmmssSS (i.e. the data is in the
     * IA 14 digit format).
     */
    private static final Pattern pattern =
        Pattern.compile("^(\\d{14}+)$|" +
            "^(\\d{4,14}+)(?:-(\\d{4,14}+))$");
    
    /**
     * Maximum future.
     * Since date actually holds seconds since epoch and since seconds
     * needs to fit into an integer for lucene sort to work, the
     * maximum future is max int seconds multipled by 1000 to get
     * a long of ms since epoch suitable for giving into date. 
     * Give this into date and we come out w/ 2038.
     */
//    private static String MAXIMUM_FUTURE = ArchiveUtils.
//        get14DigitDate((0xffffffff & (long)Integer.MAX_VALUE) * 1000);
    
    /**
     * Maximum past.
     * The epoch.  1970.
     */
//    private static String MAXIMUM_PAST = ArchiveUtils.get14DigitDate(0);
    

    public BooleanQuery filter(Query input, BooleanQuery output)
            throws QueryException {
        // Examine each clause in the Nutch query
        Clause [] clauses = input.getClauses();
        for (int i = 0; i < clauses.length; i++) {
            Clause c = clauses[i];
            // Skip if not date clauses
            if (!c.getField().equals(FIELD_NAME)) {
                continue;
            }
            
            String dateTerm = c.getTerm().toString();
            Matcher matcher = pattern.matcher(dateTerm);
            if (matcher == null || !matcher.matches()) {
                String message = "Wrong query syntax " + FIELD_NAME
                    + ":" + dateTerm + ". Must be standalone 14 digit " +
                    " IA format date or a range with a hyphen between.";
                LOGGER.error(message);
                throw new QueryException(message);
            }
            
            // So, date is in one of 2 possible formats.  First is standalone
            // 14 character IA date.
            String d = matcher.group(1);
            if (d != null) {
                LOGGER.info("Found single date: " + d);
                // This is not a range query. Take the passed date and convert
                // it to seconds-since-epoch.
                BooleanQuery bq = new BooleanQuery();
                bq.add(new TermQuery(getTerm(getSeconds(pad(d)))),
                    BooleanClause.Occur.SHOULD);
                output.add(bq, 
                    (c.isRequired() == true && c.isProhibited() == false)?
                        BooleanClause.Occur.MUST:
                        (c.isRequired() == false && c.isProhibited() == false)?
                            BooleanClause.Occur.SHOULD:
                            BooleanClause.Occur.MUST_NOT);
                continue;
            }
            
            // OK, must be 2nd possibility: DIGITS-DIGITS.
            String lower = matcher.group(2);
            String upper = matcher.group(3);
            if (lower != null && upper != null) {
                doRangeQuery(output, c, lower, upper);
                continue;
            }
            
            String message = "Unparseable query " + dateTerm + " (Is " +
                "it in 14 digit IA date format?)";
            LOGGER.error(message);
            throw new QueryException(message);
        }

        return output;
    }
    
    private void doRangeQuery(BooleanQuery output, Clause c, String lower,
            String upper)
    throws QueryException {
        LOGGER.info("Found range date: " + lower + ", " + upper);
        int iLower = getSeconds(pad(lower));
        int iUpper = getSeconds(pad(upper));
        if (iUpper < iLower) {
            String message = upper + " must be > than " + lower;
            LOGGER.error(message);
            throw new QueryException(message);
        }
        // Inclusive of upper term.
        RangeQuery rangeQuery = new RangeQuery(getTerm(iLower),
            getTerm(iUpper), true);
        // 0.0f triggers filterization.  We want this.
        rangeQuery.setBoost(0.0f);
        output.add(rangeQuery, 
            (c.isRequired() == true && c.isProhibited() == false)?
                BooleanClause.Occur.MUST:
                    (c.isRequired() == false && c.isProhibited() == false)?
                        BooleanClause.Occur.SHOULD:
                        BooleanClause.Occur.MUST_NOT);
    }
    
    protected int getSeconds(String s) throws QueryException {
        Date d = null;
        try {
            d = ArchiveUtils.parse14DigitDate(s);
        } catch (Exception e) {
            String message = "Failed parse of " + s + e.getMessage();
            throw new QueryException(message);
        }
        long seconds = d.getTime()/1000;
        if (seconds > Integer.MAX_VALUE) {
            throw new RuntimeException("Seconds is larger than " +
                " Integer.MAX_VALUE: " + seconds);
        }
        return (int)seconds;
    }

    private Term getTerm(int seconds) {
        return new Term(FIELD_NAME, ArchiveUtils.zeroPadInteger(seconds));
    }

    private String pad(String s) {
        return ArchiveUtils.padTo(s, 14, '0');
    }

    public Configuration getConf() {
        return this.conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }
}
