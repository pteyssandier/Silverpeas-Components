<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ include file="check.jsp" %>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");

out.println(window.printBefore());

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("projectManager.Projet"), "#", true);
tabbedPane.addTab(resource.getString("projectManager.Taches"), "#", false);
tabbedPane.addTab(resource.getString("projectManager.Commentaires"), "#", false);
tabbedPane.addTab(resource.getString("projectManager.Gantt"), "#", false);
out.println(tabbedPane.print());

out.println(frame.printBefore());

Board board = gef.getBoard();
out.println(board.printBefore());
%>
<table CELLPADDING=5>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.NotDefined")%></TD>
</TR>
</table>
<%
out.println(board.printAfter());
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>