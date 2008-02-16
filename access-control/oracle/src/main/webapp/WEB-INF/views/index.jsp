<%@ include file="header.inc"%>

<form action="<c:url value="/admin"/>" method="GET"><input size="50" name="surt"
	value="(org,archive,www,)/foo" /> <input type="submit" value="Go!" />
</form>

<h2 class="breadcrumb"><a href="admin?surt=(org">(org</a>,<a
	href="admin?surt=(org,archive">archive</a></h2>

<ul>
	<li><a href="admin?surt=(org,archive,audio">audio</a></li>
	<li>web</li>
	<li>webteam</li>
	<li>www</li>
</ul>

<%@ include file="footer.inc"%>
