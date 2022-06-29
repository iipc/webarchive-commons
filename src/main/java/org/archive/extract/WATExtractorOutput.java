package org.archive.extract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.net.UnknownHostException;
import java.util.Date;

import org.archive.format.gzip.GZIPMemberWriter;
import org.archive.format.gzip.GZIPMemberWriterCommittedOutputStream;
import org.archive.format.http.HttpHeaders;
import org.archive.format.json.JSONUtils;
import org.archive.format.warc.WARCRecordWriter;
import org.archive.resource.MetaData;
import org.archive.resource.Resource;
import org.archive.util.IAUtils;
import org.archive.util.DateUtils;
import org.archive.util.StreamCopy;
import org.archive.util.io.CommitedOutputStream;
import com.github.openjson.JSONException;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.logging.Logger;

public class WATExtractorOutput implements ExtractorOutput {
	WARCRecordWriter recW;
	private boolean wroteFirst;
	private GZIPMemberWriter gzW;
	private static int DEFAULT_BUFFER_RAM = 1024 * 1024;
	private int bufferRAM = DEFAULT_BUFFER_RAM;
	private final static Charset UTF8 = Charset.forName("UTF-8");
	private String outputFile;
	
	private static final Logger LOG = Logger.getLogger(WATExtractorOutput.class.getName());
	
	public WATExtractorOutput(OutputStream out, String outputFile) {
		gzW = new GZIPMemberWriter(out);
		recW = new WARCRecordWriter();
		wroteFirst = false;
		this.outputFile = outputFile;
	}

	private CommitedOutputStream getOutput() {
		return new GZIPMemberWriterCommittedOutputStream(gzW,bufferRAM);
	}

	public void output(Resource resource) throws IOException {
		StreamCopy.readToEOF(resource.getInputStream());
		MetaData top = resource.getMetaData().getTopMetaData();
		CommitedOutputStream cos;
		if(!wroteFirst) {
			cos = getOutput();
			writeWARCInfo(cos,top);
			cos.commit();
			wroteFirst = true;
		}
		String envelopeFormat = JSONUtils.extractSingle(top, "Envelope.Format");
		if(envelopeFormat == null) {
			// hrm...
			throw new IOException("Missing Envelope.Format");
		}
		cos = getOutput();
		if(envelopeFormat.startsWith("ARC")) {
			writeARC(cos,top);
		} else if(envelopeFormat.startsWith("WARC")) {
			writeWARC(cos,top);
		} else {
			// hrm...
			throw new IOException("Unknown Envelope.Format");
		}
		cos.commit();
	}

	private void writeWARCInfo(OutputStream recOut, MetaData md) throws IOException {
		// filename is given in the command line
		String filename = outputFile;
		if (filename == null || filename.length() == 0) {
			// if no filename by command line, we construct a default filename base on container filename
			filename = JSONUtils.extractSingle(md, "Container.Filename");
			if (filename == null) {
				throw new IOException("No Container.Filename...");
			}
			if (filename.endsWith(".warc") || filename.endsWith(".warc.gz")) {
				filename = filename.replaceFirst("\\.warc$", ".warc.wat.gz");
				filename = filename.replaceFirst("\\.warc\\.gz$", ".warc.wat.gz");
			} else if (filename.endsWith(".arc") || filename.endsWith(".arc.gz")) {
				filename = filename.replaceFirst("\\.arc$", ".arc.wat.gz");
				filename = filename.replaceFirst("\\.arc\\.gz$", ".arc.wat.gz");
			}
		}
		// removing path from filename
		File tmpFile = new File(filename);
		filename = tmpFile.getName();
		HttpHeaders headers = new HttpHeaders();
		headers.add("software", IAUtils.COMMONS_VERSION);
		headers.addDateHeader("extractedDate", new Date());

		// add ip, hostname
		try {
			InetAddress host = InetAddress.getLocalHost();
			headers.add("ip", host.getHostAddress());
			headers.add("hostname", host.getCanonicalHostName());
		} catch (UnknownHostException e) {
			LOG.warning("unable to obtain local crawl engine host :\n"+e.getMessage());
        }

		headers.add("format", IAUtils.WARC_FORMAT);
		headers.add("conformsTo", IAUtils.WARC_FORMAT_CONFORMS_TO);
		// optional arguments
		if(IAUtils.OPERATOR != null && IAUtils.OPERATOR.length() > 0) {
			headers.add("operator", IAUtils.OPERATOR);
		}
		if(IAUtils.PUBLISHER != null && IAUtils.PUBLISHER.length() > 0) {
			headers.add("publisher", IAUtils.PUBLISHER);
		}
		if(IAUtils.WAT_WARCINFO_DESCRIPTION != null && IAUtils.WAT_WARCINFO_DESCRIPTION.length() > 0) {
			headers.add("description", IAUtils.WAT_WARCINFO_DESCRIPTION);
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		headers.write(baos);
                recW.writeWARCInfoRecord(recOut,filename,baos.toByteArray());
	}

	private String extractOrIO(MetaData md, String path) throws IOException {
		String value = JSONUtils.extractSingle(md, path);
		if(value == null) {
			throw new IOException("No "+path+" found.");
		}
		return value;
	}

	private void writeARC(OutputStream recOut, MetaData md) throws IOException {
		String targetURI = extractOrIO(md, "Envelope.ARC-Header-Metadata.Target-URI");
		String capDateString = extractOrIO(md, "Envelope.ARC-Header-Metadata.Date");
		String filename = extractOrIO(md, "Container.Filename");
		String offset = extractOrIO(md, "Container.Offset");
		String recId = String.format("<urn:arc:%s:%s>",filename,offset);
		writeWARCMDRecord(recOut,md,targetURI,capDateString,recId);
	}

	private void writeWARC(OutputStream recOut, MetaData md) throws IOException {
		String warcType = extractOrIO(md, "Envelope.WARC-Header-Metadata.WARC-Type");
		String targetURI;
		if(warcType.equals("warcinfo")) {
			targetURI = JSONUtils.extractSingle(md, "Envelope.WARC-Header-Metadata.WARC-Filename");
		} else {
			targetURI = extractOrIO(md, "Envelope.WARC-Header-Metadata.WARC-Target-URI");
		}
		// handle date of generation in WARC format
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String capDateString = dateFormat.format(new Date());
		String recId = extractOrIO(md, "Envelope.WARC-Header-Metadata.WARC-Record-ID");
		writeWARCMDRecord(recOut,md,targetURI,capDateString,recId);
	}

	private void writeWARCMDRecord(OutputStream recOut, MetaData md, 
			String targetURI, String capDateString, String recId)
	throws IOException {

                ByteArrayOutputStream bos = new ByteArrayOutputStream();

		OutputStreamWriter osw = new OutputStreamWriter(bos, UTF8);
		String contents = md.toString();
		osw.write(contents, 0, contents.length());
		osw.flush();
//		ByteArrayInputStream bais = new ByteArrayInputStream(md.toString().getBytes("UTF-8"));
		Date capDate;
		try {
			capDate = DateUtils.getSecondsSinceEpoch(capDateString);

		} catch (ParseException e) {
			e.printStackTrace();
			// TODO... not the write thing...
			capDate = new Date();
		}
		
		recW.writeJSONMetadataRecord(recOut, bos.toByteArray(),
				targetURI, capDate, recId);
	}

	private static String transformWARCDate(final String input) {
		
		StringBuilder output = new StringBuilder(14);
		
		output.append(input.substring(0,4));
		output.append(input.substring(5,7));
		output.append(input.substring(8,10));
		output.append(input.substring(11,13));
		output.append(input.substring(14,16));
		output.append(input.substring(17,19));
		
		return output.toString();
	}
}
