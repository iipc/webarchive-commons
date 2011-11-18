package org.archive.format.arc;

import java.util.Date;
import java.util.logging.Logger;

import org.archive.util.DateUtils;

public class ARCMetaData implements ARCConstants {
	private static final Logger LOG = 
		Logger.getLogger(ARCMetaData.class.getName());
	private static final long serialVersionUID = 1L;

	private String url;
	private String ip;
	private Date date;
	private String dateS;
	private String mime;
	private long length;
	private long headerLength;

	private long leadingNL;
	
	public String getUrl()               { return url;         }
	public void setUrl(String url)       { this.url = url;     }

	public String getIP()                { return ip;          }
	public void setIP(String ip)        { this.ip = ip;      }

	public String getDateString()        { return dateS;    }
	public void setDateString(String ds) { this.dateS = ds;  }

	public String getMime()              { return mime;     }
	public void setMime(String mime)     { this.mime = mime; }

	public Date getDate()                {  return date; }
	public void setDate(Date date) {
		dateS = DateUtils.get14DigitDate(date);
		this.date = date; 
	}
	public void setDateBoth(Date date, String ds) {
		this.date = date;
		setDateString(ds);
	}


	public long getLength()              { return length;          }
	public void setLength(long length)   { this.length = length;   }
	public void setLength(String length) {
		try {
			this.length = Long.parseLong(length);
		} catch (NumberFormatException e) {
			LOG.warning(e.getMessage());
		}
	}
	public long getHeaderLength() { return headerLength; }
	public void setHeaderLength(long headerLength) {
		this.headerLength = headerLength;
	}
	public long getLeadingNL() {
		return leadingNL;
	}
	public void setLeadingNL(long leadingNL) {
		this.leadingNL = leadingNL;
	}
}
