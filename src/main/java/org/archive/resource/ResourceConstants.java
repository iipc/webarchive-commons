package org.archive.resource;

public interface ResourceConstants {
	
	public final static int [] CR_NL_CHARS = { 13, 10 };
	
	public static final String CONTAINER            = "Container";
	
	public static final String CONTAINER_COMPRESSED = "Compressed";
	public static final String CONTAINER_FILENAME   = "Filename";
	public static final String CONTAINER_OFFSET     = "Offset";

	
	public static final String GZIP                 = "Gzip-Metadata";

	public static final String GZIP_DEFLATE_LENGTH  = "Deflate-Length";
	public static final String GZIP_FOOTER_LENGTH   = "Footer-Length";
	public static final String GZIP_HEADER_LENGTH   = "Header-Length";
	public static final String GZIP_INFLATED_CRC    = "Inflated-CRC";
	public static final String GZIP_INFLATED_LENGTH = "Inflated-Length";
	public static final String GZIP_FILENAME        = "Filename";
	public static final String GZIP_COMMENT_LENGTH  = "Comment-Length";
	public static final String GZIP_HEADER_CRC      = "Header-CRC";
	public static final String GZIP_FEXTRA          = "F-Extra";
	public static final String GZIP_FEXTRA_NAME     = "Name";
	public static final String GZIP_FEXTRA_VALUE    = "Value";	


	public static final String ENVELOPE             = "Envelope";

	public static final String ENVELOPE_FORMAT      = "Format";
	public static final String ENVELOPE_FORMAT_ARC  = "ARC";
	public static final String ENVELOPE_FORMAT_WARC = "WARC";
	public static final String ENVELOPE_FORMAT_WARC_1_0 = "WARC/1.0";

	public static final String WARC_HEADER_LENGTH   = "WARC-Header-Length";
	public static final String WARC_HEADER_METADATA = "WARC-Header-Metadata";

	public static final String ARC_HEADER_LENGTH    = "ARC-Header-Length";
	public static final String ARC_HEADER_METADATA  = "ARC-Header-Metadata";

	
	public static final String METADATA_KV_NAME = "Name";
	public static final String METADATA_KV_VALUE = "Value";
	
	/* 
	 * ARC & WARC constants defined currently in:
	 * org.archive.format{arc.ARC,warc.WARC}Constants.java
	 */

	public static final String PAYLOAD_METADATA     = "Payload-Metadata";
	public static final String PAYLOAD_LENGTH       = "Actual-Content-Length";
	public static final String PAYLOAD_DIGEST       = "Block-Digest";
	public static final String PAYLOAD_SLOP_BYTES   = "Trailing-Slop-Length";
	public static final String PAYLOAD_LEADING_SLOP_BYTES   = "Leading-Slop-Length";
	public static final String PAYLOAD_CONTENT_TYPE = "Actual-Content-Type";

	// Payload-Metadata.Actual-Content-Type values:
	public static final String PAYLOAD_TYPE_HTTP_REQUEST     = "application/http; msgtype=request";
	public static final String PAYLOAD_TYPE_HTTP_RESPONSE    = "application/http; msgtype=response";
	public static final String PAYLOAD_TYPE_FILEDESC         = "alexa/filedesc";
	public static final String PAYLOAD_TYPE_WARCINFO         = "application/warc-fields";
	public static final String PAYLOAD_TYPE_WARC_META_FIELDS = "application/metadata-fields";
	public static final String PAYLOAD_TYPE_DNS              = "text/dns";
	public static final String PAYLOAD_TYPE_DAT              = "alexa/dat";

	// .Payload-Metadata.X-Metadata
	public static final String HTTP_REQUEST_METADATA     = "HTTP-Request-Metadata";
	public static final String HTTP_RESPONSE_METADATA    = "HTTP-Response-Metadata";
	public static final String FILEDESC_METADATA         = "Filedesc-Metadata";
	public static final String WARCINFO_METADATA         = "WARC-Info-Metadata";
	public static final String WARC_META_FIELDS_METADATA = "WARC-Metadata-Metadata";
	public static final String DNS_METADATA              = "DNS-Metadata";
	public static final String DAT_METADATA              = "DAT-Metadata";

	public static final String FILEDESC_MAJOR          = "Major-Version";
	public static final String FILEDESC_MINOR          = "Minor-Version";
	public static final String FILEDESC_ORGANIZATION   = "Organization";
	public static final String FILEDESC_FORMAT         = "Format";
	public static final String FILEDESC_DATA           = "Data";

	public static final String DNS_DATE     = "Date";
	public static final String DNS_NAME     = "Name";
	public static final String DNS_TTL      = "TTL";
	public static final String DNS_NETCLASS = "Net-Class";
	public static final String DNS_TYPE     = "Type";
	public static final String DNS_VALUE    = "Value";
	public static final String DNS_ENTRIES  = "Entries";

	public static final String WARC_META_FIELDS_CORRUPT   = "FIELDS_CORRUPT";
	public static final String WARC_META_FIELDS_LIST   = "Metadata-Records";

	public static final String HTTP_REQUEST_MESSAGE  = "Request-Message";
	public static final String HTTP_RESPONSE_MESSAGE = "Response-Message";

	public static final String HTTP_MESSAGE_VERSION  = "Version";
	public static final String HTTP_MESSAGE_METHOD   = "Method";
	public static final String HTTP_MESSAGE_PATH     = "Path";
	public static final String HTTP_MESSAGE_STATUS   = "Status";
	public static final String HTTP_MESSAGE_REASON   = "Reason";

	public static final String HTTP_HEADERS_CORRUPT      = "Headers-Corrupt";
	public static final String HTTP_HEADERS_LENGTH       = "Headers-Length";
	public static final String HTTP_HEADERS_LIST         = "Headers";
	
	public static final String HTTP_ENTITY_LENGTH        = "Entity-Length";
	public static final String HTTP_ENTITY_DIGEST        = "Entity-Digest";
	public static final String HTTP_ENTITY_TRAILING_SLOP = "Entity-Trailing-Slop-Bytes";
	
	public static final String HTML_METADATA    = "HTML-Metadata";
	public static final String HTML_HEAD        = "Head";
	public static final String HTML_LINKS       = "Links";
	public static final String HTML_TITLE       = "Title";
	public static final String HTML_BASE        = "Base";
	public static final String HTML_LINK_TAGS   = "Link";
	public static final String HTML_META_TAGS   = "Metas";
	public static final String HTML_SCRIPT_TAGS = "Scripts";

}
