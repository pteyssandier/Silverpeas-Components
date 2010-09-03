<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="check.jsp" %>
<html>
<head>
<%
	String zipUrl = (String) request.getAttribute("ZipURL");
	String name = (String) request.getAttribute("Name");
	Long sizeZipP = (Long) request.getAttribute("Size");
	
	Long sizeMaxP = (Long) request.getAttribute("SizeMax");
	
	long sizeZip = sizeZipP.longValue();
	long sizeMax = sizeMaxP.longValue();

	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "download.jsp");

Board	board		 = gef.getBoard();

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<table>
<tr>
	<td class="txtlibform">
		<%=resource.getString("silverCrawler.fileZip")%> : 
	</td>
	<td>
		<img border="0" src="<%=resource.getIcon("silverCrawler.zip")%>" >
	</td>
	<td>
	<%
		if (name == null || name.equals("null"))
		{%>
			<%=resource.getString("silverCrawler.sizeMax")%> (<%=sizeMax%> Mo)
		<%}
		else
		{
			if ("".equals(name))
			{%>
				<%=resource.getString("silverCrawler.noFileZip")%>
			<%}
			else
			{%>
				<a href="<%=zipUrl%>"><%=name%></a>&nbsp;(<%=FileRepositoryManager.formatFileSize(sizeZip)%>)
			<%}
		}%>
	</td>
</tr>
</table>	
		

<%
out.println(board.printAfter());
out.println(frame.printMiddle());
ButtonPane buttonPane = gef.getButtonPane();
Button button = (Button) gef.getFormButton(resource.getString("GML.close"), "javaScript:window.close();", false);
buttonPane.addButton(button);
out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>