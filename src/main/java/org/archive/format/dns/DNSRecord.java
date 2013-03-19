package org.archive.format.dns;

public class DNSRecord {
	private String name;
	private int ttl;
	private String netClass;
	private String type;
	private String value;
	public DNSRecord(String name, int ttl, String netClass, String type, String value) {
		this.name = name;
		this.ttl = ttl;
		this.netClass = netClass;
		this.type = type;
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public int getTtl() {
		return ttl;
	}
	public String getNetClass() {
		return netClass;
	}
	public String getType() {
		return type;
	}
	public String getValue() {
		return value;
	}
	public static DNSRecord parse(String line) throws DNSParseException {
		String a[] = line.split("\\s+");
		try {
			if(a.length == 5) {
				return new DNSRecord(a[0],Integer.parseInt(a[1]),a[2],a[3],a[4]);
			} else {
				throw new DNSParseException("Wrong number of fields:" + line);
			}
		} catch (NumberFormatException e) {
			throw new DNSParseException("BAD TTL field:" + line);
		}
	}
}
