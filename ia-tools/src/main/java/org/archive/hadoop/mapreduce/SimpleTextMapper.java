package org.archive.hadoop.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.archive.util.StringFieldExtractor;
import org.archive.util.StringFieldExtractor.StringTuple;

public class SimpleTextMapper extends Mapper<Object, Text, Text, Text>
		implements Configurable {

	private static String TEXT_OUTPUT_DELIM_CONFIG = "text.output.delim";
	public static int MODE_GLOBAL = 0;
	public static int MODE_FULL = 1;

	private Configuration conf;
	private Text key = new Text();
	private Text remainder = new Text();
	private String delim = " ";
	private char delimC = ' ';
	private int keyCols = 2;
	StringBuilder sb = new StringBuilder();
	StringFieldExtractor sfe = new StringFieldExtractor(delimC,keyCols);

	public void map(Object y, Text value, Context context) throws IOException,
			InterruptedException {
		StringTuple st = sfe.split(value.toString());
		key.set(st.first);
		remainder.set(st.second == null ? "" : st.second);
		context.write(key, remainder);
	}

	public Configuration getConf() {
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
		delim = conf.get(TEXT_OUTPUT_DELIM_CONFIG, delim);
		if(delim != null) {
			sfe.setDelim(delim.charAt(0));
		}
		
	}
}
