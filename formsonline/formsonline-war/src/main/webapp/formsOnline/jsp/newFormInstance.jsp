<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="check.jsp" %>

<%@page import="java.util.List"%>
<%@page import="com.silverpeas.formsonline.model.FormDetail"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.silverpeas.publicationTemplate.PublicationTemplate"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.stratelia.webactiv.beans.admin.Group"%>
<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.DataRecord"%>
<%@page import="com.silverpeas.form.PagesContext"%>

<%
	Form formUpdate    = (Form) request.getAttribute("Form");
	DataRecord data    = (DataRecord) request.getAttribute("Data"); 
	String xmlFormName = (String) request.getAttribute("XMLFormName");

	// cr�ation du context
	PagesContext  context = new PagesContext
	               ("newInstanceForm", "0", resource.getLanguage(), false, "", null);

	context.setBorderPrinted(false);
%>


<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
	<%
	out.println(gef.getLookStyleSheet());
	%>
	<% formUpdate.displayScripts(out, context); %>
	
	<script type="text/javascript">
		function B_VALIDER_ONCLICK()
		{
			if (isCorrectForm())
			{
				document.newInstanceForm.submit();
			}
		}
	</script>
</head>
<body class="yui-skin-sam">

	<%=window.printBefore()%>
	<%=frame.printBefore()%>

	<form name="newInstanceForm" method="POST" action="SaveNewInstance" 
                    enctype="multipart/form-data">
	<% 
	formUpdate.display(out, context, data); 
	%>
	</form>
	
    <%=frame.printAfter()%>

	<%    
	Button cancel = (Button) gef.getFormButton(resource.getString("GML.cancel"), "OutBox", false);
	Button validate = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false);
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(validate);
	buttonPane.addButton(cancel);
	%>
	<br/>
	<center>
	<%=buttonPane.print()%>
	</center>
  	<%=window.printAfter()%>  

</body>
</html>