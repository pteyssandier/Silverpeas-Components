<%@ include file="checkDataWarning.jsp" %>
<%
	String[] allTables = (String[])request.getAttribute("allTables");
	DataWarningQuery query = (DataWarningQuery)request.getAttribute("dataQuery");
%>
<HTML>
<Head>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">
	function getColumnList()
	{
		document.editForm.action = "SelectColumns";
		document.editForm.table.value = document.editForm.tables.options[document.editForm.tables.selectedIndex].value;
		document.editForm.submit();
	}
	function writeTableName()
	{
		document.editForm.table.value = document.editForm.tables.options[document.editForm.tables.selectedIndex].value;
		document.editForm.action = "SaveSelectTable";
		document.editForm.submit();
	}
	function viewRequete()
	{
		var req = "select * from " + document.editForm.tables.options[document.editForm.tables.selectedIndex].value;
		SP_openWindow("PreviewReq?SQLReq=" + req, "Previsu_Req", "800", "300", "scrollbars=1");
	}
</Script>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	//operation Pane 
	operationPane.addOperation(resource.getIcon("DataWarning.visuReq"), resource.getString("operationPaneReqVisu"), "javascript:onClick=viewRequete()");

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
<form name="editForm" method="post" action="" >
<input type="hidden" name="table">
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0"><!--tablcontour-->
	<tr align=center> 
		<td nowrap>
			<table border="0" cellspacing="2" cellpadding="5" class=intfdcolor width="100%"><!--tabl1-->
				<tr align=center class="intfdcolor4"> 
					<td nowrap> 
						<table cellpadding=0 cellspacing=0 border=0 width="100%">
           					<tr> 
             					<td valign=top>
									<span class="txtlibform"><%=resource.getString("popupSelection1")%> : </span>
								</td>
								<td valign=middle>
									<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="1%" bgcolor="#000000">
										<TR>
											<TD><!--Cadre bleu-->
												<TABLE CELLPADDING=2 CELLSPACING=1 BORDER=0 WIDTH="1%">
													<tr>
														<td nowrap align="left" CLASS=intfdcolor>
															<span class=selectNS>
																<select name="tables" size="1">
<%
																	boolean isSelected = false;
																	for(int i=0; i<allTables.length; i++) 
																	{
																		out.print("<option value="+allTables[i]);
																		if ((query.getQuery().indexOf(allTables[i]) >= 0) && (!isSelected))
																		{
																			out.println(" selected");
																			isSelected = true;
																		}
																		out.println(">"+allTables[i]+"</option>");
																	}
%>
																</select>
															</span>
														</td>
													</tr>
												</TABLE>
											</TD>
										</TR>
									</TABLE>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
<br>
<%
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonSuivant"), "javascript:onClick=getColumnList()", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonTerminer"), "javascript:onClick=writeTableName()", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonAnnuler"), "javascript:onClick=window.close()", false));
	out.println(buttonPane.print());
%>
</center>
</form>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>