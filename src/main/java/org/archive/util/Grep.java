package org.archive.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Kinda like /usr/bin/grep.
 */
public class Grep {
	protected static Grep parseArguments(String[] args) throws Exception {
		Grep g = new Grep();

		for (String arg: args) {
			if (arg.equals("-v") || arg.equals("--invert-match")) {
				g.setInvertMatch(true);
			} else if (arg.matches("-k\\d+")) { 
				g.setField(Integer.valueOf(arg.substring(2)));
			} else if (arg.matches("--key=\\d+")) {
				g.setField(Integer.valueOf(arg.substring(6)));
			} else if (arg.matches("-g\\d+")) {
				g.setGroup(Integer.valueOf(arg.substring(2)));
			} else if (arg.matches("--group=\\d+")) {
				g.setGroup(Integer.valueOf(arg.substring(8)));
			} else if (arg.matches("-o") || arg.equals("--only-matching")) {
				g.setGroup(0);
			} else if (g.getRegex() == null) {
				g.setRegex(Pattern.compile(arg)); // throws exception if invalid regex
			} else {
				g.addFile(arg);
			}
		}

		return g;
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			printHelp(System.err);
			System.exit(1);
		} else if (args[0].equals("-h") || args[0].equals("-?") || args[0].equals("--help")) {
		    printHelp(System.out);
		    System.exit(0);
		}

		Grep g = parseArguments(args);
		g.doTheGrepThing();
	}

	protected static void printHelp(PrintStream out) {
	    out.println("Usage:");
	    out.println("  jgrep [-v] [-kPOS] [-gGROUP] [-o] PATTERN [FILE]...");
	    out.println();
	    out.println("Sort of like grep(1), but using java regular expressions, and with a couple of");
	    out.println("unique options.");
	    out.println();
	    out.println("Options:");
	    out.println("  -v, --invert-match    Select non-matching lines");
	    out.println("  -k, --key=POS         Match the given whitespace-delimited field, somewhat");
	    out.println("                        analogous to sort -k");
	    out.println("  -g, --group=GROUP     Print the value of the specified capturing group,");
	    out.println("                        instead of the whole matching line");
	    out.println("  -o, --only-matching   Print only the part of a matching line that matches,");
	    out.println("                        equivalent to --group=0");
	    out.println("  -h, --help");
	}

    protected boolean invert;
	protected Pattern regex;
	protected List<String> files;
	protected Integer field;
	protected Integer group;

	protected Grep() {
		super();
	}

	/* jgrep -kPOS */
	protected void setField(Integer field) {
		this.field = field;
	}

	/* jgrep -v */
	protected void setInvertMatch(boolean invert) {
		this.invert = invert;
	}
	
	/* jgrep -g */
	protected void setGroup(Integer group) {
		this.group = group;
	}

	protected void setRegex(Pattern regex) {
		this.regex = regex;
	}

	protected void addFile(String path) {
		if (files == null) {
			this.files = new LinkedList<String>();
		}

		files.add(path);
	}

	protected Pattern getRegex() {
		return regex;
	}

	protected void doTheGrepThing() throws Exception {
		if (this.regex == null) {
			throw new Exception("no regex?");
		}

		if (files != null) {
			if (files.size() == 1) {
				grep(new BufferedReader(new InputStreamReader(new FileInputStream(files.get(0)), UTF_8)), "");
			} else {
				for (String path : files) {
					grep(new BufferedReader(new InputStreamReader(new FileInputStream(path), UTF_8)), path + ": ");
				}
			}
		} else {
			grep(new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset())), "");
		}
	}

	protected void grep(BufferedReader reader, String prefix) throws IOException {
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			boolean matches;
			Matcher matcher;
			if (field != null) {
				int n = field - 1;
				String[] fields = line.split("\\s+", n+2);
				if (fields.length < n+1) {
					// line doesn't have enough fields, not a match
					continue;
				} else {
					matcher = regex.matcher(fields[n]);
					matches = matcher.find();
				}
			} else {
				matcher = regex.matcher(line);
				matches = matcher.find();
			}

			if (matches && !invert) {
				if (group != null) {
					System.out.println(prefix + matcher.group(group));
				} else {
					System.out.println(prefix + line);
				}
			} else if (!matches && invert) {
				System.out.println(prefix + line);
			}
		}
	}
}
