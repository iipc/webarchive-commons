<%@ page 
  session="false"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"

  import="java.io.*"
  import="java.util.*"
  import="java.text.*"
  import="java.net.*"
  import="java.util.regex.Pattern"

  import="org.apache.nutch.html.Entities"
  import="org.apache.nutch.searcher.*"
  import="org.apache.nutch.searcher.Summary.Fragment"
  import="org.apache.nutch.plugin.*"
  import="org.apache.nutch.clustering.*"
  import="org.apache.hadoop.conf.Configuration"
  import="org.archive.access.nutch.NutchwaxConfiguration"
  import="org.archive.access.nutch.NutchwaxQuery"
  import="org.archive.access.nutch.NutchwaxBean"
  import="org.archive.util.ArchiveUtils"

%><%!
  public static final DateFormat FORMAT =
    new SimpleDateFormat("yyyyMMddHHmmss");
  public static final DateFormat DISPLAY_FORMAT =
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static final String COLLECTION_KEY = "collection";
  private static final String COLLECTION_QUERY_PARAM_KEY = COLLECTION_KEY + ":";
%><%
  Configuration nutchConf = NutchwaxConfiguration.getConfiguration(application);
  NutchBean bean = NutchwaxBean.get(application, nutchConf);

  // Set the character encoding to use when interpreting request values 
  request.setCharacterEncoding("UTF-8");

  bean.LOG.info("query request from " + request.getRemoteAddr());

  // get query from request
  String queryString = request.getParameter("query");
  if (queryString == null) {
    queryString = "";
  }
  String htmlQueryString = Entities.encode(queryString);

  int start = 0;          // first hit to display
  String startString = request.getParameter("start");
  if (startString != null)
    start = Integer.parseInt(startString);

  int hitsPerPage = 10;          // number of hits to display
  String hitsString = request.getParameter("hitsPerPage");
  if (hitsString != null)
    hitsPerPage = Integer.parseInt(hitsString);

  // Add in 'sort' parameter.
  String sort = request.getParameter("sort");
  boolean reverse =
    sort!=null && "true".equals(request.getParameter("reverse"));

  // De-Duplicate handling.  Look for duplicates field and for how many
  // duplicates per results to return. Default duplicates field is 'site'
  // and duplicates per results default is '1' (Used to be '2' but now
  // '1' so can have an index with dups not show dups when used doing
  // straight searches).
  String dedupField = request.getParameter("dedupField");
  if (dedupField == null || dedupField.length() == 0) {
    dedupField = "site";
  }
  int hitsPerDup = 1;
  String hitsPerDupString = request.getParameter("hitsPerDup");
  if (hitsPerDupString != null && hitsPerDupString.length() > 0) {
    hitsPerDup = Integer.parseInt(hitsPerDupString);
  } else {
    // If 'hitsPerSite' present, use that value.
    String hitsPerSiteString = request.getParameter("hitsPerSite");
    if (hitsPerSiteString != null && hitsPerSiteString.length() > 0) {
      hitsPerDup = Integer.parseInt(hitsPerSiteString);
    }
  }

  // If a 'collection' parameter present, always add to query.
  String collection = request.getParameter(COLLECTION_KEY);
  if (collection != null && queryString != null && queryString.length() > 0) {
      int collectionIndex = queryString.indexOf(COLLECTION_QUERY_PARAM_KEY);
      if (collectionIndex < 0) {
        queryString = queryString + " " + COLLECTION_QUERY_PARAM_KEY +
            collection;
      }
  }
     
  // Make up query string for use later drawing the 'rss' logo.
  String params = "&hitsPerPage=" + hitsPerPage +
    (sort == null ? "" : "&sort=" + sort + (reverse? "&reverse=true": "") +
    (dedupField == null ? "" : "&dedupField=" + dedupField));
    
  Query query = NutchwaxQuery.parse(queryString, nutchConf);
  bean.LOG.info("query: " + query.toString());

  String language =
    ResourceBundle.getBundle("org.nutch.jsp.search", request.getLocale())
    .getLocale().getLanguage();
  String requestURI = HttpUtils.getRequestURL(request).toString();

  // URLEncoder.encode the queryString rather than just use htmlQueryString.
  // The former will take care of other than just html entities in case its
  // needed.
  String rss = request.getContextPath() + "/opensearch?query=" +
      URLEncoder.encode(queryString, "UTF-8") + "&hitsPerDup=" + hitsPerDup +
      ((start != 0)? "&start=" + start: "") + params;

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%
  // To prevent the character encoding declared with 'contentType' page
  // directive from being overriden by JSTL (apache i18n), we freeze it
  // by flushing the output buffer. 
  // see http://java.sun.com/developer/technicalArticles/Intl/MultilingualJSP/
  out.flush();
%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/i18n" prefix="i18n" %>
<i18n:bundle baseName="org.nutch.jsp.search"/>
<html lang="<%= language %>">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<head>
<title>Internet Archive: <i18n:message key="title"/></title>
<link rel="shortcut icon" href="img/logo-16.jpg" type="image/x-icon"/>
<jsp:include page="/include/style.html"/>
</head>

<body>
<jsp:include page="/header.html"/>

 <form name="search" action="search.jsp" method="get">
 <input name="query" size=44 value="<%=htmlQueryString%>">
 <input type="hidden" name="hitsPerPage" value="<%=hitsPerPage%>">
 <% if (collection != null) { %>
    <input type="hidden" name="collection" value="<%=collection%>">
 <% } %>
 <input type="submit" value="<i18n:message key="search"/>">
 <small><a href="http://archive-access.sourceforge.net/projects/nutch/help-queries.html">Help</a></small>
<% if (sort != null) { %>
    <input type="hidden" name="sort" value="<%=sort%>">
    <input type="hidden" name="reverse" value="<%=reverse%>">
<% } %>
 </form>
<%
   long startTime = System.currentTimeMillis();
   Hits hits = null;
   int end = -1;
   do {
       try {
           hits = bean.search(query, start + hitsPerPage, hitsPerDup,
               dedupField, sort, reverse);
       } catch (IOException e) {
          hits = new Hits(0, new Hit[0]);	
       }
       // Handle case where start is beyond how many hits we have.
       end = (int)Math.min(hits.getLength(), start + hitsPerPage);
       if (hits.getLength() <= 0 || end > start) {
           break;
       }
       while (end <= start && start > 1) {
           System.out.println("START " + start);
           start -= hitsPerPage;
           if (start < 1) {
               start = 1;
           }
       }
   } while (true);

   long searchTime = System.currentTimeMillis() - startTime;
%>

Search took <%= searchTime/1000.0 %> seconds.

<i18n:message key="hits">
  <i18n:messageArg value="<%=new Long((end==0)?0:(start+1))%>"/>
  <i18n:messageArg value="<%=new Long(end)%>"/>
  <i18n:messageArg value="<%=new Long(hits.getTotal())%>"/>
</i18n:message>

<%
    // be responsive
   out.flush();

   int length = end-start;
   int realEnd = (int)Math.min(hits.getLength(), start + hitsPerPage);

   Hit[] show = hits.getHits(start, realEnd-start);
   HitDetails[] details = bean.getDetails(show);
   Summary[] summaries = bean.getSummary(details, query);

   bean.LOG.info("total hits: " + hits.getTotal());

   String collectionsHost = nutchConf.get("wax.host", "examples.com");
%>


<br><br>

<%
  for (int i = 0; i < length; i++) {      // display the hits
    Hit hit = show[i];
    HitDetails detail = details[i];
    String title = detail.getValue("title");
    String id = "idx=" + hit.getIndexNo() + "&id=" + hit.getIndexDocNo();
    
    Date date = new Date(Long.valueOf(detail.getValue("date")).longValue()*1000);
    String archiveDate = FORMAT.format(date);
    String archiveDisplayDate = DISPLAY_FORMAT.format(date);
    String archiveCollection = detail.getValue("collection");
    String url = detail.getValue("url");
    // If the collectionsHost includes a path do not add archiveCollection.
    // See http://sourceforge.net/tracker/index.php?func=detail&aid=1288990&group_id=118427&atid=681140.
    String target = null;
    String allVersions = null;
    if (collectionsHost.indexOf('/') > 0) {
        target = "http://" + collectionsHost + "/" + archiveDate + "/" + url;
        allVersions = "http://" + collectionsHost + "/*/" + url;
    } else {
        target = "http://" + collectionsHost + "/" + archiveCollection + "/"
            + archiveDate + "/" + url;
        allVersions = "http://" + collectionsHost + "/" + archiveCollection
            + "/*/" + url;
    }
    if (title == null || title.equals("")) {      // use url for docs w/o title
      title = url;
    }
    
    // Build the summary
    StringBuffer sum = new StringBuffer();
    Fragment[] fragments = summaries[i].getFragments();
    for (int j=0; j<fragments.length; j++) {
      if (fragments[j].isHighlight()) {
        sum.append("<span class=\"highlight\">")
           .append(Entities.encode(fragments[j].getText()))
           .append("</span>");
      } else if (fragments[j].isEllipsis()) {
        sum.append("<span class=\"ellipsis\"> ... </span>");
      } else {
        sum.append(Entities.encode(fragments[j].getText()));
      }
    }
    String summary = sum.toString();

    %>
    <b><a href="<%=target%>"><%=Entities.encode(title)%></a></b>
    <% if (!"".equals(summary)) { %>
    <br><%=summary%>
    <% } %>
    <br>
    <small>
    <span class="url"><%=Entities.encode(url)%></span>
    <%@ include file="./more.jsp" %>
    -
    <%=archiveDisplayDate%>
    -
    <a href="<%=allVersions%>">other versions</a>
    <% if (hit.moreFromDupExcluded()) {
    String more =
    "query="+URLEncoder.encode("site:"+hit.getDedupValue()+" "+queryString)
    +"&start="+start+"&hitsPerPage="+hitsPerPage+"&hitsPerDup="+0; %>
    -
    <a href="search.jsp?<%=more%>"><i18n:message key="moreFrom"/>
     <%=hit.getDedupValue()%></a>
    <% } %>
    -
    <a href="explain.jsp?<%=id%>&query=<%=URLEncoder.encode(queryString)%>">explain</a>
    </small>
    <br><br>
<% } %>
<!--
   Debugging info
<table border="1">
<tr>
<td>isExact:<%=hits.totalIsExact()%></td>
<td>total:<%=hits.getTotal()%></td>
<td>getLength:<%=hits.getLength()%></td>
<td>start:<%=start%></td>
<td>end:<%=end%></td>
<td>hitsPerPage:<%=hitsPerPage%></td>
</tr>
</table>
-->
<%
  if (hits.getLength() >= end || hits.getLength() > start) {
    long pagesAvailable = (long) (hits.getTotal() / hitsPerPage) + 1 ;
    if (hits.getLength() == end) {
        // We're on the last page if end == hits.getLength()
        pagesAvailable = (long) (hits.getLength() / hitsPerPage);
        if ((hits.getLength() % hitsPerPage) != 0) {
            pagesAvailable++;
        }
    }
    long currentPage = (long) ((start + 1) / hitsPerPage + 1) ;
    int maxPagesToShow = 20;
    long displayMin = (long) (currentPage - (0.5 * maxPagesToShow) );

    if (displayMin < 1) {
      displayMin = 1;
    }

    long displayMax = displayMin + maxPagesToShow - 1 ;
    if (displayMax > pagesAvailable) {
      displayMax = pagesAvailable;
    }

%>
<!--
   Debugging info
<table border="1">
<tr>
<td>pagesAvailable:<%=pagesAvailable%></td>
<td>currentPage:<%=currentPage%></td>
<td>displayMin:<%=displayMin%></td>
<td>displayMax:<%=displayMax%></td>
</tr>
</table>
-->
<center>
<% 
  if (currentPage > 1) {
    long previousPageStart = (currentPage - 2) * hitsPerPage;
    String previousPageUrl = request.getContextPath() + "/search.jsp?" +
      "query=" + htmlQueryString + 
      "&start=" + previousPageStart + 
      "&hitsPerPage=" + hitsPerPage +
      "&hitsPerDup=" + hitsPerDup +
      "&dedupField=" + dedupField;
    if (sort != null) {
      previousPageUrl = previousPageUrl + 
      "&sort=" + sort +
      "&reverse=" + reverse;
    }
%>

<a href="<%=previousPageUrl%>"><b>Previous</b></a>&nbsp

<%
  }
%>

<%
  for (long pageIndex = displayMin; pageIndex <= displayMax; pageIndex++) {
    long pageStart = (pageIndex - 1) * hitsPerPage;
    String pageUrl = "search.jsp?" +
      "query=" + htmlQueryString + 
      "&start=" + pageStart + 
      "&hitsPerPage=" + hitsPerPage +
      "&hitsPerDup=" + hitsPerDup +
      "&dedupField=" + dedupField;
    if (sort != null) {
      pageUrl = pageUrl + 
      "&sort=" + sort +
      "&reverse=" + reverse;
    }
    if (pageIndex != currentPage) {
%>
    <a href="<%=pageUrl%>"><%=pageIndex%></a>&nbsp;
<%
    }
        else {
%>
    <b><%=pageIndex%></b>
<% 
    } 
  }
%>

<%
  if (currentPage < pagesAvailable) {
    long nextPageStart = currentPage * hitsPerPage;
    String nextPageUrl = request.getContextPath() + "/search.jsp?" +
      "query=" + htmlQueryString +
      "&start=" + nextPageStart +
      "&hitsPerPage=" + hitsPerPage +
      "&hitsPerDup=" + hitsPerDup +
      "&dedupField=" + dedupField;
    if (sort != null) {
      nextPageUrl = nextPageUrl +
      "&sort=" + sort +
      "&reverse=" + reverse;
    }
%>

<a href="<%=nextPageUrl%>"><b>Next</b></a>&nbsp

<%
  }
%>

</center>

<%
  }

if ((!hits.totalIsExact() && (hits.getLength() <= start+hitsPerPage))) {
%>
    <form name="search" action="search.jsp" method="get">
    <input type="hidden" name="query" value="<%=htmlQueryString%>">
    <input type="hidden" name="start" value="<%=end%>">
    <input type="hidden" name="hitsPerPage" value="<%=hitsPerPage%>">
    <input type="hidden" name="hitsPerDup" value="<%=hitsPerDup%>">
    <input type="hidden" name="dedupField" value="<%=dedupField%>">
    <input type="submit" value="<i18n:message key="next"/>">
<% if (sort != null) { %>
    <input type="hidden" name="sort" value="<%=sort%>">
    <input type="hidden" name="reverse" value="<%=reverse%>">
<% } %>
    </form>
<%
    }
%>
<p />
<table align="right">
<tr><td ><a href="<%=rss%>"><img border="0" src="img/rss.png" alt="OpenSearchResults"/></a></td></tr>
</table>

<p>
<a href="http://www.nutch.org/">
<img border="0" src="img/poweredbynutch_01.gif">
</a>
</p>
</body>
</html>
