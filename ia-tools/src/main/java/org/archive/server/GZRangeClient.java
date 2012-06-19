package org.archive.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.archive.format.gzip.GZIPFormatException;
import org.archive.format.gzip.GZIPMemberSeries;
import org.archive.format.gzip.GZIPMemberWriter;
import org.archive.format.gzip.GZIPSeriesMember;
import org.archive.streamcontext.SimpleStream;
import org.archive.util.IAUtils;
import org.archive.util.DateUtils;
import org.archive.util.FileNameSpec;

import com.google.common.io.ByteStreams;
import com.google.common.io.LimitInputStream;

public class GZRangeClient {
	
	private final static Logger LOGGER = 
		Logger.getLogger(GZRangeClient.class.getName());
	
	
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static int CR = 13;
	private static int LF = 10;
	private static final long DEFAULT_MAX_ARC_SIZE = 1024 * 1024 * 100;
	private static final long DEFAULT_MAX_WARC_SIZE = 1024 * 1024 * 1024;
	
	
	
	private File targetDir;
	private long maxArcSize;
	private long maxWarcSize;
	private String timestamp14;
	private String timestampZ;
	private FileNameSpec warcNamer;
	private FileNameSpec arcNamer;
	private File currentArc;
	private File currentArcTmp;
	
	private File currentWarc;
	private File currentWarcTmp;
	
	private FileOutputStream currentArcOS;
	private long currentArcSize = 0;
	private FileOutputStream currentWarcOS;
	private long currentWarcSize = 0;
	private byte[] warcHeaderContents;
	private boolean exitOnError = false;
	
	private final static String ARC_PATTERN = 
		"filedesc://%s 0.0.0.0 %s text/plain 76\n" +
		"1 0 InternetArchive\n" +
		"URL IP-address Archive-date Content-type Archive-length\n\n";

	private final static String WARC_PATTERN =
		"WARC/1.0\r\n" +
		"WARC-Type: warcinfo\r\n" +
		"WARC-Date: %s\r\n" +
		"WARC-Filename: %s\r\n" +
		"WARC-Record-ID: <urn:uuid:%s>\r\n" +
		"Content-Type: application/warc-fields\r\n" +
		"Content-Length: %d\r\n\r\n";


	/*
filedesc://IQ-125-20061126082604-03075-crawling08.us.archive.org.arc 0.0.0.0 20061126082604 text/plain 1447
1 1 InternetArchive
URL IP-address Archive-date Content-type Archive-length



WARC-Date: 2009-10-10T21:33:10Z
WARC-Filename: LOC-WEEKLY-008-20091010213310-06162-crawling110.us.archive.org.warc.gz
WARC-Record-ID: <urn:uuid:776a760b-f456-48f1-97e3-1d29967c75d2>
Content-Type: application/warc-fields
Content-Length: 599

software: Heritrix/1.15.4 http://crawler.archive.org
ip: 207.241.235.29
hostname: crawling110.us.archive.org
format: WARC File Format 0.17
conformsTo: http://crawler.archive.org/warc/0.17/WARC0.17ISO.doc
operator: Vinay Goel
publisher: Internet Archive
audience: Library of Congress
isPartOf: LOC-WEEKLY-008-RECOVER
created: 2009-09-30T12:21:49Z
description: Library of Congress Monthly Harvest
robots: ignore
http-header-user-agent: Mozilla/5.0 (compatible; archive.org_bot/1.5.0 +http://www.loc.gov/minerva/crawl.html)
http-header-from: archive-crawler-agent@lists.sourceforge.net

	 */

	private static String DEFAULT_WARC_PATTERN = "software: %s Extractor\r\n" +
	"format: WARC File Format 1.0\r\n" +
	"conformsTo: http://bibnum.bnf.fr/WARC/WARC_ISO_28500_version1_latestdraft.pdf\r\n" +
	"publisher: Internet Archive\r\n" +
	"created: %s\r\n\r\n";
	
	private static final String defaultWarcHeaderString = String.format(
			DEFAULT_WARC_PATTERN, 
			IAUtils.COMMONS_VERSION, 
			DateUtils.getLog17Date(System.currentTimeMillis()));

	private static final byte[] DEFAULT_WARC_HEADER_BYTES = 
		defaultWarcHeaderString.getBytes(UTF8);
	
	public GZRangeClient(File targetDir, String prefix, String timestamp14)
	throws ParseException {

		this.targetDir = targetDir;

		arcNamer = new FileNameSpec(prefix, ".arc.gz");
		warcNamer = new FileNameSpec(prefix, ".warc.gz");

		this.timestamp14 = timestamp14;
		long msse = DateUtils.parse14DigitDate(timestamp14).getTime();
		this.timestampZ = DateUtils.getLog17Date(msse);
	
		maxArcSize = DEFAULT_MAX_ARC_SIZE;
		maxWarcSize = DEFAULT_MAX_WARC_SIZE;
		warcHeaderContents = DEFAULT_WARC_HEADER_BYTES;
	}

	
	public void finish() throws IOException {
		closeArc();
		closeWarc();
	}

	private long getGZLength(InputStream is) 
	throws IOException, GZIPFormatException {
		
		SimpleStream s = new SimpleStream(is);
		GZIPMemberSeries gzs = new GZIPMemberSeries(s,"range",0,true);
		GZIPSeriesMember m = gzs.getNextMember();
		m.skipMember();
		return m.getCompressedBytesRead();
	}

	public void append(long offset, List<String> urls) throws IOException {
		boolean isArc = false;
		String first = urls.get(0);
		if(first.endsWith(".arc.gz")) {
			isArc = true;
		} else if(first.endsWith(".warc.gz")) {
			
		} else {
			throw new IOException("URL (" + first +
					") must end with '.arc.gz' or '.warc.gz'");
		}
		for(String url : urls) {
			FileBackedInputStream fbis = null;
			InputStream is = null;
			try {
				URL u = new URL(url);
				URLConnection conn = u.openConnection();
				conn.setRequestProperty("Range", String.format("bytes=%d-", offset));
				LOGGER.info(String.format("Attempting(%d) from(%s)",offset,url));
				conn.connect();
				is = conn.getInputStream();
				fbis = new FileBackedInputStream(is);
				long length = getGZLength(fbis);
				InputStream orig = fbis.getInputStream();
				if(isArc) {
					writeARCRecord(orig, length);
				} else {
					writeWARCRecord(orig, length);					
				}
				LOGGER.info(String.format("Wrote record(%d) from(%s)",
						offset,url));
				return;
			} catch (IOException e) {
				LOGGER.warning("FAILED URL-OFFSET("+url+")(" + offset+")");
			} finally {
				if(is != null) {
					is.close();
				}
				if(fbis != null) {
					fbis.resetBacker();
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		for(String u : urls) {
			if(sb.length() != 0) {
				sb.append(",");
			}
			sb.append(u);
		}
		String errMsg = String.format("Unable to get offset(%d) from (%s)",
				offset,sb.toString());
		if(exitOnError) {
			throw new IOException(errMsg);
		} else {
			LOGGER.severe(errMsg);
		}
	}

	
	
	private String getWARCRecordID() {
		return "urn:uuid:" + UUID.randomUUID().toString();
	}
	private byte[] getARCHeader(String name) {
		return String.format(ARC_PATTERN,name,timestamp14).getBytes(UTF8);
	}
	private byte[] getWARCHeader(String name) {
		String t = String.format(WARC_PATTERN,
				timestampZ,name,getWARCRecordID(),warcHeaderContents.length + 4);
		byte[] b = t.getBytes(UTF8);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			baos.write(b);
			baos.write(warcHeaderContents);
		} catch(IOException e) {
			// not gonna happen
		}
		baos.write(CR);
		baos.write(LF);
		baos.write(CR);
		baos.write(LF);
		return baos.toByteArray();
	}

	private void writeWARCRecord(InputStream is, long length) throws IOException {
		if(currentWarcSize == 0) {
			nextWarc();
		}
		LimitInputStream lis = new LimitInputStream(is, length);
		ByteStreams.copy(lis, currentWarcOS);
		currentWarcSize += length;
		if(currentWarcSize > maxWarcSize) {
			closeWarc();
		}
	}
	private void writeARCRecord(InputStream is, long length) throws IOException {
		if(currentArcSize == 0) {
			nextArc();
		}
		LimitInputStream lis = new LimitInputStream(is, length);
		ByteStreams.copy(lis, currentArcOS);
		currentArcSize += length;
		if(currentArcSize > maxArcSize) {
			closeArc();
		}
	}
	
	private void closeArc() throws IOException {
		if(currentArcOS == null) {
			return;
		}
		currentArcOS.close();
		if(!currentArcTmp.renameTo(currentArc)) {
			throw new IOException(String.format("Failed rename(%s) to (%s)",
					currentArcTmp.getAbsolutePath(),
					currentArc.getAbsolutePath()));
		}
		currentArcOS = null;
		currentArcSize = 0;
		LOGGER.info(String.format("Closed(%s)",currentArc.getAbsolutePath()));
	}
	private void closeWarc() throws IOException {
		if(currentWarcOS == null) {
			return;
		}
		currentWarcOS.close();
		if(!currentWarcTmp.renameTo(currentWarc)) {
			throw new IOException(String.format("Failed rename(%s) to (%s)",
					currentWarcTmp.getAbsolutePath(),
					currentWarc.getAbsolutePath()));
		}
		currentWarcOS = null;
		currentWarcSize = 0;
		LOGGER.info(String.format("Closed(%s)",currentWarc.getAbsolutePath()));
	}
	private void nextArc() throws IOException {
		String newArcName = arcNamer.getNextName();
		currentArc = new File(targetDir,newArcName);
		String tmpArcName = newArcName + ".OPEN";
		currentArcTmp = new File(targetDir,tmpArcName);
		currentArcOS = new FileOutputStream(currentArcTmp);
		byte[] header = getARCHeader(newArcName);
		GZIPMemberWriter w = new GZIPMemberWriter(currentArcOS);
                w.write(new ByteArrayInputStream(header));
		currentArcSize = w.getBytesWritten();
		LOGGER.info(String.format("Openned(%s)",currentArc.getAbsolutePath()));
	}

	private void nextWarc() throws IOException {
		String newWarcName = warcNamer.getNextName();
		currentWarc = new File(targetDir,newWarcName);
		String tmpWarcName = newWarcName + ".OPEN";
		currentWarcTmp = new File(targetDir,tmpWarcName);
		currentWarcOS = new FileOutputStream(currentWarcTmp);

		byte[] header = getWARCHeader(newWarcName);
		GZIPMemberWriter w = new GZIPMemberWriter(currentWarcOS);
                w.write(new ByteArrayInputStream(header));
		currentWarcSize = w.getBytesWritten();
		LOGGER.info(String.format("Openned(%s)",currentWarc.getAbsolutePath()));
	}

	/**
	 * @return the warcHeaderContents
	 */
	public byte[] getWarcHeaderContents() {
		return warcHeaderContents;
	}

	/**
	 * @param warcHeaderContents the warcHeaderContents to set
	 */
	public void setWarcHeaderContents(byte[] warcHeaderContents) {
		this.warcHeaderContents = warcHeaderContents;
	}

	/**
	 * @return the maxArcSize
	 */
	public long getMaxArcSize() {
		return maxArcSize;
	}

	/**
	 * @param maxArcSize the maxArcSize to set
	 */
	public void setMaxArcSize(long maxArcSize) {
		this.maxArcSize = maxArcSize;
	}

	/**
	 * @return the maxWarcSize
	 */
	public long getMaxWarcSize() {
		return maxWarcSize;
	}

	/**
	 * @param maxWarcSize the maxWarcSize to set
	 */
	public void setMaxWarcSize(long maxWarcSize) {
		this.maxWarcSize = maxWarcSize;
	}


	/**
	 * @return the exitOnError
	 */
	public boolean isExitOnError() {
		return exitOnError;
	}


	/**
	 * @param exitOnError the exitOnError to set
	 */
	public void setExitOnError(boolean exitOnError) {
		this.exitOnError = exitOnError;
	}
}
