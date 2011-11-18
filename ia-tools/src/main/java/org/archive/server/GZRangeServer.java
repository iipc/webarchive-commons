package org.archive.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.archive.format.gzip.GZIPFormatException;
import org.archive.format.gzip.GZIPMemberSeries;
import org.archive.format.gzip.GZIPSeriesMember;
import org.archive.streamcontext.SimpleStream;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import com.google.common.io.ByteStreams;
import com.google.common.io.LimitInputStream;

public class GZRangeServer extends AbstractHandler implements Tool {
	public final static String TOOL_NAME = "gzrange-server";
	public static final String TOOL_DESCRIPTION = 
		"Run a special gzrange HTTP server.";

	private Configuration conf;

	public void setConf(Configuration conf) {
		this.conf = conf;
	}
	public Configuration getConf() {
		return conf;
	}
	private static int USAGE(int code) {
		System.err.println("USAGE");
		System.err.println(TOOL_NAME + " [PORT]");
		System.err.println("run a Jetty HTTP server listening on PORT (or 8009 if omitted)");
		System.err.println("The server handles unbounded HTTP 1.1 Range requests for GZ members in a special");
		System.err.println("fashion: it will scan ahead to determing the compressed length of the gzip member");
		System.err.println("starting at the range start offset, and will return the exact number");
		System.err.println("of compressed bytes in the member, including setting the Content-Length response header.");
		return code;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new GZRangeServer(), args);
		System.exit(res);
	}

	public int run(String[] args) throws Exception {
		Logger.getLogger(GZIPMemberSeries.class.getName()).setLevel(Level.WARNING);
		int port = 8009;
		if(args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				return USAGE(1);
			}
		}
        Server server = new Server(port);
        server.setHandler(new GZRangeServer());
        server.start();
        server.join();
        return 0;
    }

	private long parseBytes(String range) {
		if(range == null) {
			return -1;
		}
		if(range.startsWith("bytes=")) {
			if(range.endsWith("-")) {
				String rem = range.substring(6,range.length()-1);
				try {
					return Long.parseLong(rem);
				} catch(NumberFormatException e) {
				}
			}
		}
		return -1;
	}
	
	private long getGZLength(InputStream is) 
	throws IOException, GZIPFormatException {
		
		SimpleStream s = new SimpleStream(is);
		GZIPMemberSeries gzs = new GZIPMemberSeries(s,"range",0,true);
		GZIPSeriesMember m = gzs.getNextMember();
		m.skipMember();
		return m.getCompressedBytesRead();
	}
	
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {
		
		File file = new File(target);
		if(file.isFile()) {
			if(file.canRead()) {
				
				String range = request.getHeader("Range");
				long offset = parseBytes(range);
				if(offset == -1) {
					range = request.getParameter("offset");
					if(range != null) {
						try {
							offset = Long.parseLong(range);
						} catch(NumberFormatException e) {
						}
					}
				}
				if(range == null) {
					range = "null";
				}
				if(offset == -1) {
					
					response.setContentType("text/plain;charset=utf-8");
			        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			        response.getWriter().println("Require \"Range\" header " +
			        		"or \"offset\" GET parameter");
			        
				} else {
					
					long length = file.length();
					if(offset > length) {

						response.setContentType("text/plain;charset=utf-8");
				        response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
				        response.getWriter().println("range past EOF");
						
					} else {
						// OK, find then end of the record:
						RandomAccessFile raf = new RandomAccessFile(file, "r");
						raf.seek(offset);
						FileInputStream fis = 
							new FileInputStream(raf.getFD());
						long gzLength = -1;
						try {
							gzLength = getGZLength(fis);
						} catch(GZIPFormatException e) {
							
						}
						if(gzLength == -1) {

							response.setContentType("text/plain;charset=utf-8");
					        response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
					        response.getWriter().println("corrupt range, or gzip alignment error");
							
						} else {
							raf.seek(offset);
							fis = 
								new FileInputStream(raf.getFD());
							response.setContentType("application/octet-stream");
							response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
							response.setContentLength((int)gzLength);
							LimitInputStream lis = 
								new LimitInputStream(fis, gzLength);
							long copied = ByteStreams.copy(lis,
									response.getOutputStream());
							if(copied != gzLength) {
								throw new IOException("Short copy Want(" +
										gzLength + ") copied(" + copied + ")");
							}
						}
					}
				}

			} else {				
		        response.setContentType("text/plain;charset=utf-8");
		        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		        response.getWriter().println("not readable\n");
			}
		} else {
	        response.setContentType("text/plain;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	        response.getWriter().println("not found\n");
		}
        ((Request)request).setHandled(true);
	}
}
