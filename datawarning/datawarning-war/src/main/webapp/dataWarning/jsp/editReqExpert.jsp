<%@ include file="checkDataWarning.jsp" %>
<%
	String requete = (String)request.getAttribute("requete");
	String valeurRetour = (String)request.getAttribute("valeurRetour");
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("operationPaneReqExpert")%></TITLE>
<%
	out.println(gef.getLookStyleSheet());
%>
</HEAD>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF" onload=document.form.SQLReq.focus()>
<%
	//operation Pane 
	operationPane.addOperation(resource.getIcon("DataWarning.visuReq"), resource.getString("operationPaneReqVisu"), "javascript:onClick=viewRequete()");
%>	
<script language="JavaScript">
	function isValidText(input, textFieldLength) 
	{
		if (input.length <= textFieldLength)
			return true;
		else
			return false;
	}

	function isReqDebWithSelect()
	{
		var req = document.form.SQLReq.value;
		if(req.substring(0, 6) == "select")
			return true;
		return false;
	}

	function ClosePopup_onValider()
	{
		if(!isValidText(document.form.SQLReq.value, 2000))
			alert('<%=resource.getString("erreurChampsTropLong")%>');
		else if(document.form.SQLReq.value == "")
			alert('<%=resource.getString("erreurChampsVide")%>');
		else if(!isReqDebWithSelect())
			alert('<%=resource.getString("requeteSelect")%>');
		else
		{
			document.form.SQLReq.value = document.form.SQLReq.value.replace(/\n/gi," ");
			document.form.action = "SaveRequete";
			document.form.submit();
		}
	}

	function viewRequete()
	{
		var formatReq = document.form.SQLReq.value.replace(/\n/gi," ");
		if(document.form.SQLReq.value == null || document.form.SQLReq.value == "")
			alert('<%=resource.getString("erreurRequeteVide")%>');
		else
			SP_openWindow("PreviewReq?SQLReq=" + formatReq, "Previsu_Req", "800", "300", "scrollbars=1");
	}
</script>
<%
	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
<CENTER>
<FORM name="form" action=""  Method="POST">
	<TABLE CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=contourintfdcolor>
		<TR CLASS=intfdcolor4>
			<TD align="left" valign=top>
				<span class="txtlibform"><%=resource.getString("champRequete")%> : </span>
			</TD>		
			<TD align="left" valign=top>
				<TEXTAREA NAME="SQLReq" COLS=100 ROWS="25" window.focus><%if(requete != null) out.print(requete);%></TEXTAREA >
			</TD>
		</TR>
	</TABLE>
</FORM>
<%
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonValider"), "javascript:onClick=ClosePopup_onValider()", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonAnnuler"), "javascript:onClick=window.close()", false));
    out.println(buttonPane.print());
%>
</CENTER>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
	if((valeurRetour != null) && (valeurRetour.length() > 0))
	{
%>
<script language=javascript>
		alert("<%=valeurRetour%>");
</script>
<%
	}
%>
</BODY>
</HTML>