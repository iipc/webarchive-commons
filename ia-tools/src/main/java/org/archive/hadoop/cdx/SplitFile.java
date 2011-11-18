package org.archive.hadoop.cdx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class SplitFile {
	public SplitLine[] lines;
	
	public SplitFile() {};
	public int size() {
		return lines.length;
	}
	public String getStart(int i) {
		return lines[i].start;
	}
	public String getEnd(int i) {
		return lines[i].end;
	}
	public String getName(int i) {
		return lines[i].name;
	}
	public void read(Reader r) throws IOException {
		BufferedReader br = new BufferedReader(r);
		ArrayList<SplitLine> tmp = new ArrayList<SplitLine>();
		while(true) {
			String line = br.readLine();
			if(line == null) {
				break;
			}
			tmp.add(new SplitLine(line));
		}
		if(tmp.size() == 0) {
			throw new IOException("Empty split file");
		}
		lines = tmp.toArray(new SplitLine[] {});
	}
	private class SplitLine {
		String name;
		String start;
		String end;
		public SplitLine(String line) {
			String parts[] = line.split("\\s");
			if(parts.length != 3) {
				throw new IllegalArgumentException("Bad split line:" + line);
			}
			name = parts[0];
			start = parts[1];
			end = parts[2];
		}
	}
}
