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
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="javax.naming.Context,javax.naming.InitialContext,javax.rmi.PortableRemoteObject"%>

<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="publicationsList.jsp.inc" %>
<%@ include file="kmax_axisReport.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%!
  String publicationAddSrc;
  String publicationSrc;
  String fullStarSrc;
  String emptyStarSrc;
  String unbalancedSrc;
  String topicBasketSrc;
  String pubToValidateSrc;
%>

<% 
//Récupération des paramètres
String 	action 		= (String) request.getParameter("Action");
String 	profile 	= (String) request.getParameter("Profile");
String 	translation = (String) request.getAttribute("Language");

//Icons
publicationAddSrc 	= m_context + "/util/icons/publicationAdd.gif";
publicationSrc 		= m_context + "/util/icons/publication.gif";
fullStarSrc 		= m_context + "/util/icons/starFilled.gif";
emptyStarSrc 		= m_context + "/util/icons/starEmpty.gif";
unbalancedSrc 		= m_context + "/util/icons/kmelia_declassified.gif";
topicBasketSrc		= m_context + "/util/icons/pubTrash.gif";
pubToValidateSrc	= m_context + "/util/icons/publicationstoValidate.gif";
String exportComponentSrc	= m_context + "/util/icons/exportComponent.gif";

ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", kmeliaScc.getLanguage());

if (action == null) {
	action = "KmaxView";
}
%>

<HTML>
<HEAD>
<TITLE></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/ajax/prototype.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/rico.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/ricoAjax.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript1.2">
var subscriptionWindow = window;
var favoriteWindow = window;
var topicUpdateWindow = window;
var topicAddWindow = window;
var exportComponentWindow = window;

function search() {
    z = "";
    nbSelectedAxis = 0;
    timeCriteria = "X";
    timeCriteriaUsed = 0;
	<% if (kmeliaScc.isTimeAxisUsed()) { %>
	    timeCriteria = document.axisForm.elements[document.axisForm.length - 1].value;
	    timeCriteriaUsed = 1;
	<% } %>
	for (var i=0; i<document.axisForm.length - timeCriteriaUsed; i++) {
    	if (document.axisForm.elements[i].value.length != 0) {
            if (nbSelectedAxis != 0)
                z += ",";
            nbSelectedAxis = 1;
            truc = document.axisForm.elements[i].value.split("|");
            z += truc[0];
        }
    }
    if (nbSelectedAxis != 1) {
		window.alert("Vous devez sélectionnez au moins un axe !");
    } else {
		document.managerForm.TimeCriteria.value = timeCriteria;
		document.managerForm.SearchCombination.value = z;
		document.managerForm.action = "KmaxSearch";
		document.managerForm.submit();
    }
}

function publicationAdd(){
	if(subscriptionWindow.name=="subscriptionWindow")
		subscriptionWindow.close();
	if (favoriteWindow.name=="favoriteWindow")
		favoriteWindow.close();
	if (topicAddWindow.name=="topicAddWindow")
		topicAddWindow.close();
	if (topicUpdateWindow.name=="topicUpdateWindow")
		topicUpdateWindow.close();
	document.pubForm.action = "NewPublication";
	document.pubForm.submit();
}

function publicationGoTo(id){
	if(subscriptionWindow.name=="subscriptionWindow")
		subscriptionWindow.close();
	if (favoriteWindow.name=="favoriteWindow")
		favoriteWindow.close();
	if (topicAddWindow.name=="topicAddWindow")
		topicAddWindow.close();
	if (topicUpdateWindow.name=="topicUpdateWindow")
		topicUpdateWindow.close();
	document.pubForm.action = "ViewPublication";
    document.pubForm.PubId.value = id;
    document.pubForm.submit();
}

function viewUnbalanced() {
  document.managerForm.action = "KmaxViewUnbalanced";
  document.managerForm.submit();
}

function viewBasket() {
  document.managerForm.Id.value = "1";
  document.managerForm.action = "GoToTopic";
  document.managerForm.submit();
}

function doPagination(index)
{
	ajaxEngine.sendRequest('refreshPubList','ElementId=pubList',"ComponentId=<%=componentId%>","Index="+index);
	return;
}

function sortGoTo(selectedIndex) {
	if (selectedIndex != 0 && selectedIndex != 1) {
		var sort = document.publicationsForm.sortBy[selectedIndex].value;
		ajaxEngine.sendRequest('refreshPubList','ElementId=pubList',"ComponentId=<%=componentId%>","Index=0","Sort="+sort);
		return;
	}
}

function init()
{
	ajaxEngine.registerRequest('refreshPubList', '<%=m_context%>/RAjaxPublicationsListServlet/dummy');
	ajaxEngine.registerAjaxElement('pubList');
	ajaxEngine.sendRequest('refreshPubList','ElementId=pubList',"ComponentId=<%=componentId%>");
}

function exportComponent()
{
	exportComponentWindow = SP_openWindow("exportTopic.jsp","exportComponentWindow",700,250,"scrollbars=yes, resizable=yes");
}

function exportPublications()
{
	exportComponentWindow = SP_openWindow("exportTopic.jsp?TopicId=dummy","exportComponentWindow",700,250,"scrollbars=yes, resizable=yes");
}

</script>
</HEAD>
<BODY onLoad="init()">
<%
Window window = gef.getWindow();

BrowseBar browseBar = window.getBrowseBar();
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "KmaxMain");
browseBar.setI18N("KmaxMain", translation);

if (action.equals("KmaxView")) {
    
    //Display operations by profile
    if (profile.equals("admin") || profile.equals("publisher") || profile.equals("writer")) {
		OperationPane operationPane = window.getOperationPane();
		operationPane.addOperation(publicationAddSrc, kmeliaScc.getString("PubCreer"), "javascript:onClick=publicationAdd()");
		if (kmeliaScc.isWizardEnabled())
    	{
    		// ajout assistant de publication
    		operationPane.addOperation(resources.getIcon("kmelia.wizard"), resources.getString("kmelia.Wizard"), "WizardStart");
    	}
		operationPane.addLine();
		operationPane.addOperation(unbalancedSrc, kmeliaScc.getString("PubDeclassified"), "javascript:onClick=viewUnbalanced()");
		operationPane.addOperation(topicBasketSrc, kmeliaScc.getString("PubBasket"), "javascript:onClick=viewBasket()");
		
		if (profile.equals("admin") || profile.equals("publisher")) {
	    	operationPane.addLine();
	        operationPane.addOperation(pubToValidateSrc, kmeliaScc.getString("ToValidate"), "ViewPublicationsToValidate");
	    }

	    if (profile.equals("admin") && "yes".equals(settings.getString("kmax.exportComponentAllowed")) && kmeliaScc.isExportComponentAllowed())
			{
				operationPane.addLine();
				operationPane.addOperation(exportComponentSrc, kmeliaScc.getString("kmelia.ExportComponent"), "javascript:onClick=exportComponent()");
			}
    }

    
    TabbedPane tabbedPane = gef.getTabbedPane();
    if (profile.equals("admin")) {
        tabbedPane.addTab(kmeliaScc.getString("Consultation"), "#", true);
        tabbedPane.addTab(kmeliaScc.getString("Management"), "KmaxAxisManager", false);
    }

    Frame frame = gef.getFrame();
    out.println(window.printBefore());
    
    frame.addTop(displayAxisToUsers(kmeliaScc, gef, translation));

    if (profile.equals("admin"))
        out.println(tabbedPane.print());

    out.println(frame.print());
    out.println(window.printAfter());

} else if (action.equals("KmaxSearchResult")) {

    browseBar.setI18N("KmaxSearchResult", translation);

    Collection publications = kmeliaScc.getSessionPublicationsList();
	ArrayList combination = kmeliaScc.getSessionCombination();
	String timeCriteria = kmeliaScc.getSessionTimeCriteria();
		  
    //Display operations by profile
    if (profile.equals("admin") || profile.equals("publisher") || profile.equals("writer")) {
			OperationPane operationPane = window.getOperationPane();
			operationPane.addOperation(publicationAddSrc, kmeliaScc.getString("PubCreer"), "javascript:onClick=publicationAdd()");
			if (kmeliaScc.isWizardEnabled())
        	{
        		// ajout assistant de publication
        		operationPane.addOperation(resources.getIcon("kmelia.wizard"), resources.getString("kmelia.Wizard"), "WizardStart");
        	}
			operationPane.addLine();
			operationPane.addOperation(unbalancedSrc, kmeliaScc.getString("PubDeclassified"), "javascript:onClick=viewUnbalanced()");
			//Basket
			operationPane.addOperation(topicBasketSrc, kmeliaScc.getString("PubBasket"), "javascript:onClick=viewBasket()");
			
			if (profile.equals("admin") || profile.equals("publisher")) {
		    	operationPane.addLine();
		        operationPane.addOperation(pubToValidateSrc, kmeliaScc.getString("ToValidate"), "ViewPublicationsToValidate");
		    }
			if (profile.equals("admin") && "yes".equals(settings.getString("kmax.exportComponentAllowed")) && kmeliaScc.isExportComponentAllowed())
			{
				operationPane.addLine();
				operationPane.addOperation(exportComponentSrc, kmeliaScc.getString("kmax.ExportPublicationsFound"), "javascript:onClick=exportPublications()");
			}
			
    }
      
      TabbedPane tabbedPane = gef.getTabbedPane();
      if (profile.equals("admin")) {
        tabbedPane.addTab(kmeliaScc.getString("Consultation"), "KmaxView", true);
        tabbedPane.addTab(kmeliaScc.getString("Management"), "KmaxAxisManager", false);
      }

      Frame frame = gef.getFrame();
      out.println(window.printBefore());
      
      if (profile.equals("admin"))
          out.println(tabbedPane.print());
      
	  out.println(frame.printBefore());
      
      out.println(displayAxisCombinationToUsers(kmeliaScc, gef, combination, timeCriteria, translation));
      Board board = gef.getBoard();
      out.println("<div id=\"pubList\">");
      out.println("<br/>");
      out.println(board.printBefore());
      out.println("<br/><center>"+resources.getString("kmelia.inProgressPublications")+"<br/><br/><img src=\""+resources.getIcon("kmelia.progress")+"\"/></center><br/>");
      out.println(board.printAfter());
      out.println("</div>");

      out.println(frame.printAfter());
      out.println(window.printAfter());

} else if (action.equals("KmaxViewUnbalanced")) {
	  
	browseBar.setI18N("KmaxViewUnbalanced", translation);

	Collection publications = kmeliaScc.getSessionPublicationsList();
	ArrayList combination = new ArrayList();

	Frame frame = gef.getFrame();
	out.println(window.printBefore());
	out.println(frame.printBefore());
	
	Board board = gef.getBoard();
	 out.println("<div id=\"pubList\">");
	 out.println("<br/>");
	 out.println(board.printBefore());
	 out.println("<br/><center>"+resources.getString("kmelia.inProgressPublications")+"<br/><br/><img src=\""+resources.getIcon("kmelia.progress")+"\"/></center><br/>");
	 out.println(board.printAfter());
    out.println("</div>");
	
	out.println(frame.printAfter());
	out.println(window.printAfter());
}
%>
<form name="managerForm" action="KmaxAxisManager" method="Post">
	<input type="hidden" name="AxisId">
	<input type="hidden" name="AxisName">
	<input type="hidden" name="AxisDescription">
	<input type="hidden" name="ComponentId">
	<input type="hidden" name="ComponentName">
	<input type="hidden" name="ComponentDescription">
	<input type="hidden" name="SearchCombination">
	<input type="hidden" name="TimeCriteria">
	<input type="hidden" name="Id">
</form>

<FORM NAME="pubForm" METHOD="POST">
	<input type="hidden" name="PubId">
</FORM>

</BODY>
</HTML>