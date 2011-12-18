package org.archive.hadoop.cdx;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.archive.hadoop.mapreduce.CDXMapper;
import org.archive.hadoop.mapreduce.CDXMapper.StringPair;

public class CDXConverterTool implements Tool {
	
	Charset UTF8 = Charset.forName("utf-8");
	public final static String TOOL_NAME = "cdx-convert";
	public static final String TOOL_DESCRIPTION = 
		"A tool for converting old CDX lines from STDIN to SURT form on STDOUT";
	
	private Configuration conf;
	public void setConf(Configuration conf) {
		this.conf = conf;
	}
	public Configuration getConf() {
		return conf;
	}
	public int run(String[] args) throws Exception {
		CDXMapper mapper = new CDXMapper();
		mapper.setConf(getConf());
		BufferedReader br =
			new BufferedReader(new InputStreamReader(System.in,UTF8));
		PrintWriter pw = 
			new PrintWriter(new OutputStreamWriter(System.out,UTF8));
		while(true) {
			String cdxLine = br.readLine();
			if(cdxLine == null) {
				break;
			}
			StringPair pair = mapper.convert(cdxLine);
			if(pair != null) {
				pw.print(pair.first);
				pw.print(" ");
				pw.print(pair.second);
				pw.println();
			}
		}
		pw.flush();
		return 0;
	}	
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new CDXConverterTool(), args);
		System.exit(res);
	}


}
