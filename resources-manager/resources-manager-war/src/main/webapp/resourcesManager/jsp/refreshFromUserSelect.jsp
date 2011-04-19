<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@ include file="check.jsp"%>

<%
Collection managers = (Collection) request.getAttribute("Managers");
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript">

function refresh() 
{
	var tdManagers = window.opener.document.getElementById("managers");
	tdManagers.innerHTML = "";
	
	var inputManagerIds = window.opener.document.getElementById("managerIds");
	
	<%
	if (managers != null) {
		Iterator it = managers.iterator();
		String managerNames = "";
		String managerIds = "";
		while(it.hasNext())
		{
		  UserDetail manager = (UserDetail) it.next();
      managerNames += manager.getDisplayedName()+"<br/>";
      managerIds += manager.getId()+",";
		}
		%>
		  tdManagers.innerHTML = "<%=managerNames%>";
		  inputManagerIds.value = "<%=managerIds%>";
		<%
	}
	%>
	window.close();
}
</script>
</HEAD>
<BODY onLoad="refresh()">
</BODY>
</HTML>