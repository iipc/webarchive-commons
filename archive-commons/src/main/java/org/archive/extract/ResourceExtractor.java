package org.archive.extract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.archive.RecoverableRecordFormatException;
import org.archive.format.gzip.GZIPFormatException;
import org.archive.resource.Resource;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceParseException;
import org.archive.resource.ResourceProducer;
import org.archive.url.WaybackURLKeyMaker;

public class ResourceExtractor implements ResourceConstants, Tool {
	
	private final static Logger LOG =
		Logger.getLogger(ResourceExtractor.class.getName());
	Charset UTF8 = Charset.forName("utf-8");
	public final static String TOOL_NAME = "extractor";
	public static final String TOOL_DESCRIPTION = 
		"A tool for extracting metadata from WARC, ARC, and WAT files";
	private OutputStream out;
	private Configuration conf;
	public void setConf(Configuration conf) {
		this.conf = conf;
	}
	public Configuration getConf() {
		return conf;
	}

	
//	private static final Logger LOG = 
//		Logger.getLogger(ResourceExtractor.class.getName());
	
	private static int USAGE(int exitCode) {
		System.err.println("Usage:\n");
		System.err.println("extractor [OPT] SRC");
		System.err.println("\tSRC is the local path, HTTP or HDFS URL to an " +
				"arc, warc, arc.gz, or warc.gz.");
		System.err.println("\tOPT can be one of:");		
		System.err.println("\t\t-cdxURL\tProduce output in old URL Wayback CDX format");
		System.err.println("\t\t-cdx\tProduce output in NEW-SURT-Wayback CDX format");
		System.err.println("\t\t\t (note that column 1 is NOT standard Wayback canonicalized)\n");
		System.err.println("\t\t-wat\tembed JSON output in a compressed WARC" +
				"wrapper, for storage, or sharing.");
		return exitCode;
	}


	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new ResourceExtractor(), args);
		System.exit(res);
	}
	
	private PrintWriter makePrintWriter(OutputStream os)
	{
		return new PrintWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));
	}

	public int run(String[] args) 
	throws IndexOutOfBoundsException, FileNotFoundException, IOException,
	ResourceParseException, URISyntaxException {
		// TODO: parse CLI arguments better
		if(args.length < 1) {
			return USAGE(1);
		}
		if(args.length > 3) {
			return USAGE(1);
		}
		int max = Integer.MAX_VALUE;
		OutputStream os = this.out == null ? System.out : this.out;
	    Logger.getLogger("org.archive").setLevel(Level.WARNING);
	    ExtractorOutput out;
	    int arg = 0;
	    if(args.length > 0) {
	    	if(args[0].equals("-strict")) {
	    		ProducerUtils.STRICT_GZ = true;
	    		arg++;
	    	}	   
	    }
	    String path = args[arg];
	    if(args.length == arg + 2) {
	    	if(args[arg].equals("-cdx")) {
	    		path = args[arg+1];
	    		out = new RealCDXExtractorOutput(makePrintWriter(os));
	    		
	    	} else if(args[arg].equals("-cdxURL")) {
	    		path = args[arg+1];
	    		out = new RealCDXExtractorOutput(makePrintWriter(os), new WaybackURLKeyMaker(false));

	    	} else if(args[arg].equals("-wat")) {
	    		path = args[arg+1];
	    		out = new WATExtractorOutput(os);
	    	} else {
	    		String filter = args[arg+1];
	    		out = new JSONViewExtractorOutput(os, filter);
	    	}
	    } else {
	    	out = new DumpingExtractorOutput(os);
	    }
	    ResourceProducer producer = ProducerUtils.getProducer(path);
	    if(producer == null) {
	    	return USAGE(1);
	    }
	    ResourceFactoryMapper mapper = new ExtractingResourceFactoryMapper();
	    ExtractingResourceProducer exProducer = 
	    	new ExtractingResourceProducer(producer, mapper);

	    Logger.getLogger("org.archive").setLevel(Level.WARNING);

		int count = 0;
		int incr = 1;
		while(count < max) {
			try {
				Resource r = exProducer.getNext();
				if(r == null) {
					break;
				}
				count += incr;
				
				out.output(r);
			} catch(GZIPFormatException e) {
				LOG.severe(String.format("%s: %s",exProducer.getContext(),e.getMessage()));
				//Log is not coming out for some damn reason....needs to be studied
				System.err.format("%s: %s",exProducer.getContext(),e.getMessage());
				
				if(ProducerUtils.STRICT_GZ) {
					throw e;
				}
				e.printStackTrace();
			} catch(ResourceParseException e) {
				LOG.severe(String.format("%s: %s",exProducer.getContext(),e.getMessage()));
				//Log is not coming out for some damn reason....needs to be studied
				System.err.format("%s: %s",exProducer.getContext(),e.getMessage());
				
				if(ProducerUtils.STRICT_GZ) {
					throw e;
				}
				e.printStackTrace();
			} catch(RecoverableRecordFormatException e) {
				// this should not get here - ResourceFactory et al should wrap as ResourceParseExceptions...
				LOG.severe(String.format("RECOVERABLE - %s: %s",exProducer.getContext(),e.getMessage()));
				//Log is not coming out for some damn reason....needs to be studied
				System.err.format("%s: %s",exProducer.getContext(),e.getMessage());

				e.printStackTrace();
				
			}
		}
		return 0;
	}
	/**
	 * @return the out
	 */
	public OutputStream getOut() {
		return out;
	}
	/**
	 * @param out the out to set
	 */
	public void setOut(OutputStream out) {
		this.out = out;
	}
}
