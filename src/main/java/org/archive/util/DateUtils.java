package org.archive.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    
	// lifted from org.archive.util.ArchiveUtils
	
    final public static String VERSION = "0.1";
    
    /**
     * RFC 1123 Style dates with 4 digit year, the best sort for HTTP.
     */
//    Sun, 06 Nov 1994 08:49:37 GMT 
    private static final ThreadLocal<SimpleDateFormat> 
    	TIMESTAMPRFC1123 = threadLocalDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    
    
    /**
     * Arc-style date stamp in the format yyyyMMddHHmm and UTC time zone.
     */
    private static final ThreadLocal<SimpleDateFormat> 
        TIMESTAMP12 = threadLocalDateFormat("yyyyMMddHHmm");;
    
    /**
     * Arc-style date stamp in the format yyyyMMddHHmmss and UTC time zone.
     */
    private static final ThreadLocal<SimpleDateFormat> 
       TIMESTAMP14 = threadLocalDateFormat("yyyyMMddHHmmss");
    /**
     * Arc-style date stamp in the format yyyyMMddHHmmssSSS and UTC time zone.
     */
    private static final ThreadLocal<SimpleDateFormat> 
        TIMESTAMP17 = threadLocalDateFormat("yyyyMMddHHmmssSSS");

    /**
     * Log-style date stamp in the format yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     * UTC time zone is assumed.
     */
    private static final ThreadLocal<SimpleDateFormat> 
        TIMESTAMP17ISO8601Z = threadLocalDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    /**
     * Log-style date stamp in the format yyyy-MM-dd'T'HH:mm:ss'Z'
     * UTC time zone is assumed.
     */
    private static final ThreadLocal<SimpleDateFormat>
        TIMESTAMP14ISO8601Z = threadLocalDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    /**
     * Default character to use padding strings.
     */
    private static final char DEFAULT_PAD_CHAR = ' ';

    /** milliseconds in an hour */ 
    private static final int HOUR_IN_MS = 60 * 60 * 1000;
    /** milliseconds in a day */
    private static final int DAY_IN_MS = 24 * HOUR_IN_MS;
    
    private static ThreadLocal<SimpleDateFormat> threadLocalDateFormat(final String pattern) {
        ThreadLocal<SimpleDateFormat> tl = new ThreadLocal<SimpleDateFormat>() {
            protected SimpleDateFormat initialValue() {
                SimpleDateFormat df = new SimpleDateFormat(pattern, Locale.ENGLISH);
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                return df;
            }
        };
        return tl;
    }
    
    public static int MAX_INT_CHAR_WIDTH =
        Integer.toString(Integer.MAX_VALUE).length();

    /*
     * ===================================
     * ===================================
     *  Date/long/NOW => String
     * ===================================
     * ===================================
     */

    /**
     * Utility function for creating HTTP header style date strings, 
     * specifically, RFC 1123: EEE, dd MMM yyyy HH:mm:ss z.
     * 
     * Date stamps are in the UTC time zone
     * @return the date stamp
     */
    public static String getRFC1123Date(Date date){
        return TIMESTAMPRFC1123.get().format(date);
    }

    /*
     * ================================================
     * 17-digit (yyyyMMddHHmmssSSS) : 20010203040506007
     * ================================================
     */
    /**
     * Utility function for creating arc-style date stamps
     * in the format yyyMMddHHmmssSSS.
     * Date stamps are in the UTC time zone
     * @return the date stamp for NOW
     */
    public static String get17DigitDate(){
        return get17DigitDate(new Date());
    }

    /**
     * Utility function for creating warc-style date stamps
     * in the format yyyyMMddHHmmssSSS.
     * Date stamps are in the UTC time zone
     *
     * @param date milliseconds since epoc
     * @return the date stamp
     */
    public static String get17DigitDate(long date){
        return get17DigitDate(new Date(date));
    }

    /**
     * Utility function for creating warc-style date stamps
     * in the format yyyyMMddHHmmssSSS.
     * Date stamps are in the UTC time zone
     *
     * @param date Date object
     * @return the date stamp
     */
    public static String get17DigitDate(Date date){
        return TIMESTAMP17.get().format(date);
    }

    /*
     * ====================================================
     * 14-digit ARC-style (yyyyMMddHHmmss) : 20010203040506
     * ====================================================
     */

    /**
     * Utility function for creating arc-style date stamps
     * in the format yyyyMMddHHmmss.
     * Date stamps are in the UTC time zone
     * @return the date stamp for NOW
     */
    public static String get14DigitDate(){
        return get14DigitDate(new Date());
    }
    /**
     * Utility function for creating arc-style date stamps
     * in the format yyyyMMddHHmmss.
     * Date stamps are in the UTC time zone
     *
     * @param date milliseconds since epoc
     * @return the date stamp
     */
    public static String get14DigitDate(long date){
        return get14DigitDate(new Date(date));
    }
    /**
     * Utility function for creating arc-style date stamps
     * in the format yyyyMMddHHmmss.
     * Date stamps are in the UTC time zone
     *
     * @param d Date for timestamp
     * @return the date stamp
     */
    public static String get14DigitDate(Date d) {
        return TIMESTAMP14.get().format(d);
    }

    /*
     * ====================================================
     * 12-digit (yyyyMMddHHmm) : 200102030405
     * ====================================================
     */

    /**
     * Utility function for creating 12-digit(no seconds!) date stamps
     * in the format yyyyMMddHHmm.
     * Date stamps are in the UTC time zone
     * @return the date stamp for NOW
     */
    public static String get12DigitDate(){
        return get12DigitDate(new Date());
    }
    /**
     * Utility function for creating 12-digit(no seconds!) date stamps
     * in the format yyyyMMddHHmm.
     * Date stamps are in the UTC time zone
     *
     * @param date milliseconds since epoc
     * @return the date stamp
     */
    public static String get12DigitDate(long date){
        return get12DigitDate(new Date(date));
    }
    /**
     * Utility function for creating 12-digit(no seconds!) date stamps
     * in the format yyyyMMddHHmm.
     * Date stamps are in the UTC time zone
     *
     * @param d Date object to format
     * @return the date stamp
     */    
    public static String get12DigitDate(Date d) {
        return TIMESTAMP12.get().format(d);
    }
    /*
     * ====================================================
     * W3C/ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
     * ====================================================
     */
    /**
     * Utility function for creating log timestamps, in
     * W3C/ISO8601 format, assuming UTC. Use current time. 
     * 
     * Format is yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     * 
     * @return the date stamp for NOW
     */
    public static String getLog17Date(){
        return getLog17Date(new Date());
    }
    
    /**
     * Utility function for creating log timestamps, in
     * W3C/ISO8601 format, assuming UTC. 
     * 
     * Format is yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     * @param date milliseconds since epoc.
     * 
     * @return the date stamp
     */
    public static String getLog17Date(long date){
        return getLog17Date(new Date(date));
    }
    /**
     * Utility function for creating log timestamps, in
     * W3C/ISO8601 format, assuming UTC. 
     * 
     * Format is yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     * @param date Date to format.
     * 
     * @return the date stamp
     */
    public static String getLog17Date(Date date){
        return TIMESTAMP17ISO8601Z.get().format(date);
    }
 
    /*
     * ====================================================
     * W3C/ISO8601 (yyyy-MM-dd'T'HH:mm:ss'Z')
     * ====================================================
     */
    /**
     * Utility function for creating log timestamps, in
     * W3C/ISO8601 format, assuming UTC. Use current time. 
     * 
     * Format is yyyy-MM-dd'T'HH:mm:ss'Z'
     * 
     * @return the date stamp for NOW
     */
    public static String getLog14Date(){
        return getLog14Date(new Date());
    }
    /**
     * Utility function for creating log timestamps, in
     * W3C/ISO8601 format, assuming UTC. 
     * 
     * Format is yyyy-MM-dd'T'HH:mm:ss'Z'
     * @param date long timestamp to format.
     * 
     * @return the date stamp
     */
    public static String getLog14Date(long date){
        return getLog14Date(new Date(date));
    }
    /**
     * Utility function for creating log timestamps, in
     * W3C/ISO8601 format, assuming UTC. 
     * 
     * Format is yyyy-MM-dd'T'HH:mm:ss'Z'
     * @param date Date to format.
     * 
     * @return the date stamp
     */
    public static String getLog14Date(Date date){
        return TIMESTAMP14ISO8601Z.get().format(date);
    }
    
    /*
     * ===================================
     * ===================================
     *  String => Date
     * ===================================
     * ===================================
     */
    
    /**
     * Parses an ARC-style date.  If passed String is &lt; 12 characters in length,
     * we pad.  At a minimum, String should contain a year (&gt;=4 characters).
     * Parse will also fail if day or month are incompletely specified.  Depends
     * on the above getXXDigitDate methods.
     * @param d A 4-17 digit date in ARC style (<code>yyyy</code> to
     * <code>yyyyMMddHHmmssSSS</code>) formatting.  
     * @return A Date object representing the passed String. 
     * @throws ParseException
     */
    public static Date getDate(String d) throws ParseException {
        Date date = null;
        if (d == null) {
            throw new IllegalArgumentException("Passed date is null");
        }
        switch (d.length()) {
        case 14:
            date = parse14DigitDate(d);
            break;

        case 17:
            date = parse17DigitDate(d);
            break;

        case 12:
            date = parse12DigitDate(d);
            break;
           
        case 0:
        case 1:
        case 2:
        case 3:
            throw new ParseException("Date string must at least contain a" +
                "year: " + d, d.length());
            
        default:
            if (!(d.startsWith("19") || d.startsWith("20"))) {
                throw new ParseException("Unrecognized century: " + d, 0);
            }
            if (d.length() < 8 && (d.length() % 2) != 0) {
                throw new ParseException("Incomplete month/date: " + d,
                    d.length());
            }
            StringBuilder sb = new StringBuilder(d);
            if (sb.length() < 8) {
                for (int i = sb.length(); sb.length() < 8; i += 2) {
                    sb.append("01");
                }
            }
            if (sb.length() < 12) {
                for (int i = sb.length(); sb.length() < 12; i++) {
                    sb.append("0");
                }
            }
            date = parse12DigitDate(sb.toString());
        }

        return date;
    }

    /**
     * Utility function for parsing arc-style date stamps
     * in the format yyyMMddHHmmssSSS.
     * Date stamps are in the UTC time zone.  The whole string will not be
     * parsed, only the first 17 digits.
     *
     * @param date an arc-style formatted date stamp
     * @return the Date corresponding to the date stamp string
     * @throws ParseException if the inputstring was malformed
     */
    public static Date parse17DigitDate(String date) throws ParseException {
        return TIMESTAMP17.get().parse(date);
    }

    /**
     * Utility function for parsing arc-style date stamps
     * in the format yyyMMddHHmmss.
     * Date stamps are in the UTC time zone.  The whole string will not be
     * parsed, only the first 14 digits.
     *
     * @param date an arc-style formatted date stamp
     * @return the Date corresponding to the date stamp string
     * @throws ParseException if the inputstring was malformed
     */
    public static Date parse14DigitDate(String date) throws ParseException {
        return TIMESTAMP14.get().parse(date);
    }

    /**
     * Utility function for parsing arc-style date stamps
     * in the format yyyMMddHHmm.
     * Date stamps are in the UTC time zone.  The whole string will not be
     * parsed, only the first 12 digits.
     *
     * @param date an arc-style formatted date stamp
     * @return the Date corresponding to the date stamp string
     * @throws ParseException if the inputstring was malformed
     */
    public static Date parse12DigitDate(String date) throws ParseException {
        return TIMESTAMP12.get().parse(date);
    }
    
    /**
     * @param timestamp A 14-digit timestamp or the suffix for a 14-digit
     * timestamp: E.g. '20010909014640' or '20010101' or '1970'.
     * @return Seconds since the epoch as a string zero-pre-padded so always
     * Integer.MAX_VALUE wide (Makes it so sorting of resultant string works
     * properly).
     * @throws ParseException 
     */
    public static String secondsSinceEpoch(String timestamp)
    throws ParseException {
        return zeroPadInteger((int)
            (getSecondsSinceEpoch(timestamp).getTime()/1000));
    }
    
    /**
     * @param timestamp A 14-digit timestamp or the suffix for a 14-digit
     * timestamp: E.g. '20010909014640' or '20010101' or '1970'.
     * @return A date.
     * @see #secondsSinceEpoch(String)
     * @throws ParseException 
     */
    public static Date getSecondsSinceEpoch(String timestamp)
    throws ParseException {
        if (timestamp.length() < 14) {
            if (timestamp.length() < 10 && (timestamp.length() % 2) == 1) {
                throw new IllegalArgumentException("Must have year, " +
                    "month, date, hour or second granularity: " + timestamp);
            }
            if (timestamp.length() == 4) {
                // Add first month and first date.
                timestamp = timestamp + "01010000";
            }
            if (timestamp.length() == 6) {
                // Add a date of the first.
                timestamp = timestamp + "010000";
            }
            if (timestamp.length() < 14) {
                timestamp = timestamp +
                    padTo("", 14 - timestamp.length(), '0');
            }
        }
        return parse14DigitDate(timestamp);
    }
    
    /**
     * @param i Integer to add prefix of zeros too.  If passed
     * 2005, will return the String <code>0000002005</code>. String
     * width is the width of Integer.MAX_VALUE as a string (10
     * digits).
     * @return Padded String version of <code>i</code>.
     */
    public static String zeroPadInteger(int i) {
        return padTo(Integer.toString(i), MAX_INT_CHAR_WIDTH, '0');
    }

    /** 
     * Convert an <code>int</code> to a <code>String</code>, and pad it to
     * <code>pad</code> spaces.
     * @param i the int
     * @param pad the width to pad to.
     * @return String w/ padding.
     */
    public static String padTo(final int i, final int pad) {
        String n = Integer.toString(i);
        return padTo(n, pad);
    }
    
    /** 
     * Pad the given <code>String</code> to <code>pad</code> characters wide
     * by pre-pending spaces.  <code>s</code> should not be <code>null</code>.
     * If <code>s</code> is already wider than <code>pad</code> no change is
     * done.
     *
     * @param s the String to pad
     * @param pad the width to pad to.
     * @return String w/ padding.
     */
    public static String padTo(final String s, final int pad) {
        return padTo(s, pad, DEFAULT_PAD_CHAR);
    }

    /** 
     * Pad the given <code>String</code> to <code>pad</code> characters wide
     * by pre-pending <code>padChar</code>.
     * 
     * <code>s</code> should not be <code>null</code>. If <code>s</code> is
     * already wider than <code>pad</code> no change is done.
     *
     * @param s the String to pad
     * @param pad the width to pad to.
     * @param padChar The pad character to use.
     * @return String w/ padding.
     */
    public static String padTo(final String s, final int pad,
            final char padChar) {
        String result = s;
        int l = s.length();
        if (l < pad) {
            StringBuffer sb = new StringBuffer(pad);
            while(l < pad) {
                sb.append(padChar);
                l++;
            }
            sb.append(s);
            result = sb.toString();
        }
        return result;
    }

    /** check that two byte arrays are equal.  They may be <code>null</code>.
     *
     * @param lhs a byte array
     * @param rhs another byte array.
     * @return <code>true</code> if they are both equal (or both
     * <code>null</code>)
     */
    public static boolean byteArrayEquals(final byte[] lhs, final byte[] rhs) {
        if (lhs == null && rhs != null || lhs != null && rhs == null) {
            return false;
        }
        if (lhs==rhs) {
            return true;
        }
        if (lhs.length != rhs.length) {
            return false;
        }
        for(int i = 0; i<lhs.length; i++) {
            if (lhs[i]!=rhs[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts a double to a string.
     * @param val The double to convert
     * @param maxFractionDigits How many characters to include after '.'
     * @return the double as a string.
     */
    public static String doubleToString(double val, int maxFractionDigits){
        return doubleToString(val, maxFractionDigits, 0);
    }

    private static String doubleToString(double val, int maxFractionDigits, int minFractionDigits) {
        NumberFormat f = NumberFormat.getNumberInstance(Locale.US); 
        f.setMaximumFractionDigits(maxFractionDigits);
        f.setMinimumFractionDigits(minFractionDigits);
        return f.format(val); 
    }

    /**
     * Takes a byte size and formats it for display with 'friendly' units. 
     * <p>
     * This involves converting it to the largest unit 
     * (of B, KiB, MiB, GiB, TiB) for which the amount will be &gt; 1.
     * <p>
     * Additionally, at least 2 significant digits are always displayed. 
     * <p>
     * Negative numbers will be returned as '0 B'.
     *
     * @param amount the amount of bytes
     * @return A string containing the amount, properly formated.
     */
    public static String formatBytesForDisplay(long amount) {
        double displayAmount = (double) amount;
        int unitPowerOf1024 = 0; 

        if(amount <= 0){
            return "0 B";
        }
        
        while(displayAmount>=1024 && unitPowerOf1024 < 4) {
            displayAmount = displayAmount / 1024;
            unitPowerOf1024++;
        }
        
        final String[] units = { " B", " KiB", " MiB", " GiB", " TiB" };
        
        // ensure at least 2 significant digits (#.#) for small displayValues
        int fractionDigits = (displayAmount < 10) ? 1 : 0; 
        return doubleToString(displayAmount, fractionDigits, fractionDigits) 
                   + units[unitPowerOf1024];
    }

    /**
     * Convert milliseconds value to a human-readable duration
     * @param time
     * @return Human readable string version of passed <code>time</code>
     */
    public static String formatMillisecondsToConventional(long time) {
        return formatMillisecondsToConventional(time,5);
    }
        
    /**
     * Convert milliseconds value to a human-readable duration of 
     * mixed units, using units no larger than days. For example,
     * "5d12h13m12s113ms" or "19h51m". 
     * 
     * @param duration
     * @param unitCount how many significant units to show, at most
     *  for example, a value of 2 would show days+hours or hours+seconds 
     *  but not hours+second+milliseconds
     * @return Human readable string version of passed <code>time</code>
     */
    public static String formatMillisecondsToConventional(long duration, int unitCount) {
        if(unitCount <=0) {
            unitCount = 5;
        }
        if(duration==0) {
            return "0ms";
        }
        StringBuffer sb = new StringBuffer();
        if(duration<0) {
            sb.append("-");
        }
        long absTime = Math.abs(duration);
        long[] thresholds = {DAY_IN_MS, HOUR_IN_MS, 60000, 1000, 1};
        String[] units = {"d","h","m","s","ms"};
        
        for(int i = 0; i < thresholds.length; i++) {
            if(absTime >= thresholds[i]) {
                sb.append(absTime / thresholds[i] + units[i]);
                absTime = absTime % thresholds[i];
                unitCount--;
            }
            if(unitCount==0) {
                break;
            }
        }
        return sb.toString();
    }
    
    static long LAST_UNIQUE_NOW14 = 0;
    static String LAST_TIMESTAMP14 = ""; 
    /**
     * Utility function for creating UNIQUE-from-this-class 
     * arc-style date stamps in the format yyyMMddHHmmss.
     * Rather than giving a duplicate datestamp on a 
     * subsequent call, will increment the seconds until a 
     * unique value is returned. 
     * 
     * Date stamps are in the UTC time zone
     * @return the date stamp
     */
    public synchronized static String getUnique14DigitDate(){
        long effectiveNow = System.currentTimeMillis(); 
        effectiveNow = Math.max(effectiveNow, LAST_UNIQUE_NOW14+1);
        String candidate = get14DigitDate(effectiveNow);
        while(candidate.equals(LAST_TIMESTAMP14)) {
            effectiveNow += 1000;
            candidate = get14DigitDate(effectiveNow);
        }
        LAST_UNIQUE_NOW14 = effectiveNow;
        LAST_TIMESTAMP14 = candidate; 
        return candidate;
    }
}