package org.archive.hadoop.pig;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.archive.util.ArchiveUtils;

public class DateFilter extends FirstPigJobOnlyFilter implements PathFilter
{
	protected final static Log LOGGER = LogFactory.getLog(DateFilter.class);
	
	protected Date date1;
	protected Date date2;
	
	protected String paramString = null;
	
	protected String extFilter = null;
	
	protected PrintWriter fileWriter = null;
	
	protected FileSystem fs = null;
	
	protected DateFilter.CompareOp op1 = null;	
	protected DateFilter.CompareOp op2 = null;
	
	public final static String DATE_FILTER_PARAM = "org.archive.pig.filter.date";
	public final static String EXT_FILTER_PARAM = "org.archive.pig.filter.ext";
	
	public final static String DATE_OUTPUT_LOG = "org.archive.pig.filter.date.logfile";
	
	public final static String MTIME_VAR = "mtime";
	
	static enum CompareOp
	{
		EQ,
		LT,
		GT,
		GTEQ,
		LTEQ,
	}
	
	private boolean compare(DateFilter.CompareOp op, long A, long B) {
		switch (op) {
		case EQ:
			return (A == B);
		case LT:
			return (A < B);
		case GT:
			return (A > B);
		case GTEQ:
			return (A >= B);
		case LTEQ:
			return (A <= B);
		default:
			return (A > B);
		}
	}
	
	private boolean dirSkipOp(DateFilter.CompareOp theOp, boolean second)
	{
		switch (theOp) {
		case LT:
			return (second ? false : true);
		case LTEQ:
			return (second ? false : true);			
		case GT:
			return (second ? true : false);
		case GTEQ:
			return (second ? true : false);
		}
		
		return false;
	}
	
	protected DateFilter.CompareOp flipOp(CompareOp theOp)
	{
		switch (theOp) {
		case LT:
			return CompareOp.GT;
		case GT:
			return CompareOp.LT;
		case GTEQ:
			return CompareOp.LTEQ;
		case LTEQ:
			return CompareOp.GTEQ;
		}
		return CompareOp.EQ;
	}
	
	protected DateFilter.CompareOp parseOp(String op)
	{
		if (op.equals("=")) {
			return CompareOp.EQ;
		}
		
		if (op.equals("<")) {
			return CompareOp.LT;
		}
		
		if (op.equals(">")) {
			return CompareOp.GT;
		}
		
		if (op.equals(">=")) {
			return CompareOp.GTEQ;
		}
		
		if (op.equals("<=")) {
			return CompareOp.LTEQ;
		}
		
		throw new IllegalArgumentException("Illegal comparison op: " + op);
	}
	
	@Override
	public void setConfWhenEnabled(Configuration conf) {
		
		if (conf == null) {
			return;
		}
		
		init(conf.get(DATE_FILTER_PARAM), conf);	
		extFilter = conf.get(EXT_FILTER_PARAM, "");
	}
	
	public void init(String paramString, Configuration conf)
	{
		this.paramString = paramString;
		
		if (this.paramString == null || this.paramString.isEmpty()) {
			return;
		}
		
		String[] params = paramString.split("\\s+");
		
		String dateStr1 = null;
		String dateStr2 = null;
		
		// < DATE1
		if (params.length == 2) {
			op1 = parseOp(params[0]);
			dateStr1 = params[1];
		} else if (params.length == 3) {			
						
			if (params[0].equals(MTIME_VAR)) {
				// $mtype < DATE1
				dateStr1 = params[2];
				op1 = parseOp(params[1]);
			} else if (params[2].equals(MTIME_VAR)) {
				// DATE2 > $mtype
				dateStr2 = params[0];
				op2 = parseOp(params[1]);
			} else {
				throw new IllegalArgumentException("Must Specify mtime as param: X < mtime or mtime > X");
			}
			
		} else if (params.length == 5) {
			
			// DATE2 > $mtype
			dateStr2 = params[0];
			op2 = parseOp(params[1]);
			
			if (!params[2].equals(MTIME_VAR)) {
				throw new IllegalArgumentException("Must Specify mtime as param: X < mtime < Y");		
			}
			
			// $mtype < DATE1
			op1 = parseOp(params[3]);
			dateStr1 = params[4];
		} else {
			throw new IllegalArgumentException("Must use form: OP X, X OP mtime, mtime OP X, X OP mtime OP2 Y, where OP is one of <, >, <=, >=, =");
		}
					
		try {
			this.fs = FileSystem.get(conf);
			
			if (dateStr1 != null) {
				date1 = parseDateForParam(dateStr1);
				writeLog("Date 1: " + date1.toString());
			}
			
			if (dateStr2 != null) {
				date2 = parseDateForParam(dateStr2);
				writeLog("Date 2: " + date2.toString());
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Date Output Log
		String fileLog = conf.get(DATE_OUTPUT_LOG);
		
		if (fileLog != null) {
			try {
				fileWriter = new PrintWriter(new File(fileLog));
			} catch (IOException io) {
				LOGGER.warn(io.toString());
			}
		}
	}
	
	protected Date parseDateForParam(String dateParam) throws IOException
	{
		// Try default format
		try {
			return new SimpleDateFormat().parse(dateParam);
		} catch (ParseException pe) {

		}
		
		// Then ArchiveUtils		
		try {
			return ArchiveUtils.getDate(dateParam);
		} catch (ParseException pe) {

		}
		
		// Then file path
		Path datePath = new Path(dateParam);
		
		FileStatus status = fs.getFileStatus(datePath);
		return new Date(status.getModificationTime());
	}

	public boolean acceptWhenEnabled(Path path) {
		
		try {
			FileStatus status = fs.getFileStatus(path);
			
			boolean isDir = status.isDir();
			
//			if (isDir) {
//				return true;
//			}
			
			if ((extFilter != null) && !extFilter.isEmpty() && !isDir) {
				if (!path.getName().endsWith(extFilter)) {
					return false;
				}
			}
			
			long mtime = status.getModificationTime();
						
			// DATE2 > $mtype
			if (date2 != null) {
				if (!(isDir && dirSkipOp(op2, true)) && !compare(op2, date2.getTime(), mtime)) {
				//if (!compare(op2, date2.getTime(), mtime)) {		
					return false;
				}
			}
			
			// $mtype < DATE1
			if (date1 != null) {
				if (!(isDir && dirSkipOp(op1, false)) && !compare(op1, mtime, date1.getTime())) {
				//if (!compare(op1, mtime, date1.getTime())) {
					return false;
				}
			}
			
			String msg = path.getName() + " (" + new Date(mtime).toString() + ")";
			
			if (isDir) {
				LOGGER.info(msg);
			} else {
				writeLog(msg);
			}
			
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private void writeLog(String string) {
		LOGGER.info(string);
		
		if (fileWriter != null) {
			fileWriter.println(string);
			fileWriter.flush();
		}
	}

	public String toString()
	{
		return paramString;
	}
	
	public void close()
	{
		if (fileWriter != null) {
			fileWriter.close();
		}
	}
}