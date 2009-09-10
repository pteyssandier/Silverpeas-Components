<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.CategoryDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.ResourceDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.ResourceReservableDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.ReservationDetail"%>
<%@ page import="java.util.List" %>
<%@ include file="check.jsp" %>
<% 
//R�cup�ration des d�tails de l'ulisateur
List 			list 						= (List) request.getAttribute("listResourcesReservable");
int 				nbCategories 				= ((Integer)request.getAttribute("nbCategories")).intValue();
ReservationDetail 	reservation 				= (ReservationDetail) request.getAttribute("reservation");
List 			listResourcesProblem 		= (List) request.getAttribute("listResourcesProblem");
List 			listResourceEverReserved 	= (List) request.getAttribute("listResourceEverReserved");
String 				idModifiedReservation 		= (String)request.getAttribute("idReservation");

String evenement = reservation.getEvent();
String raison = Encode.javaStringToHtmlParagraphe(reservation.getReason());
String lieu = reservation.getPlace();

int tableTab = (nbCategories*37)+10;

// boutons de validation du formulaire
Board	board		 = gef.getBoard();
ButtonPane buttonPane = gef.getButtonPane();
Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javaScript:verification()", false);
Button cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "Main",false);
buttonPane.addButton(validateButton);
buttonPane.addButton(cancelButton);

//String qui permet de r�cup�rer la liste des ids des ressources r�serv�es
String listResourceIdReserved="";
boolean noResource = true;

// Permet de r�cup�rer l'id de la cat�gorie courante
String idTemoin="";
%>
<html>
	<head>
	<%
		out.println(gef.getLookStyleSheet());
	%>
	<script type="text/javascript" src="<%=m_context%>/resourcesManager/jsp/javaScript/rico.js"></script>
	<script type='text/javascript'>
	Rico.loadModule('Accordion');

	Rico.onLoad( function() {
	  new Rico.Accordion( $$('div.toggleratStart'), $$('div.elementatStart'), {panelHeight:100} );
	});
	</script>
	
	<script language=JavaScript>
	
	function ajouterRessource(resourceId, categoryId) {
		var elementResource = document.getElementById(resourceId);
		var elementlisteReservation = document.getElementById("listeReservation");
		var theImage = "image"+resourceId ;
		document.images[theImage].src = "<%=m_context%>/util/icons/delete.gif";
		
		elementlisteReservation.appendChild(elementResource);
	}
	
	function enleverRessource(resourceId, categoryId) {
		var elementResource = document.getElementById(resourceId);
		var elementCategory = document.getElementById(categoryId);
		var theImage = "image"+resourceId ;
		document.images[theImage].src = "<%=m_context%>/util/icons/ok.gif";		

		elementCategory.appendChild(elementResource);
	}
	
	function switchResource(resourceId, categoryId)
	{
		//window.alert("reservee ? "+isResourceReservee(resourceId))
		if (isResourceReservee(resourceId))
		{
			clearCategory(categoryId);
			
			enleverRessource(resourceId, categoryId);			
		}
		else
		{
			ajouterRessource(resourceId, categoryId);
			
			if (isCategoryEmpty(categoryId))
				addEmptyResource(categoryId);
		}
	}
	
	function addEmptyResource(categoryId)
	{
		//window.alert("empty");
		var emptyElement = document.createElement("div");
		emptyElement.id = "-1";
		emptyElement.innerHTML = "<span class=\"noRessource\"><center><%=resource.getString("resourcesManager.noResource")%></center></span>";
		
		var elementCategory = document.getElementById(categoryId);
		elementCategory.appendChild(emptyElement);
	}
			
	
	function clearCategory(categoryId)
	{
		var category = document.getElementById(categoryId);
		var resources = category.childNodes;
		for (var r=0; r<resources.length; r++)
		{
			if (resources[r].nodeName == 'DIV' && resources[r].id == "-1")
				category.removeChild(resources[r]);
		}
	}
	
	function isCategoryEmpty(categoryId)
	{
		var category = document.getElementById(categoryId);
		var resources = category.childNodes;
		for (var r=0; r<resources.length; r++)
		{
			if (resources[r].nodeName == 'DIV')
				return false;
		}
		return true;
	}
	
	function isResourceReservee(resourceId)
	{
		var listeReservation = document.getElementById("listeReservation");
		var resources = listeReservation.childNodes;
		for (var r=0; r<resources.length; r++)
		{
			if (resources[r].nodeName == 'DIV' && resources[r].id == resourceId)
				return true;
		}
		return false;
	}
	
	function getResourcesReservees()
	{
		var listeReservation = document.getElementById("listeReservation");
		var resources = listeReservation.childNodes;
		var resourceIds = "";
		for (var r=0; r<resources.length; r++)
		{
			if (resources[r].nodeName == 'DIV')
				resourceIds += resources[r].id+",";
		}
		resourceIds = resourceIds.substring(0, resourceIds.length-1);
		//window.alert(resourceIds);
		return resourceIds;
	}
	
	function verification(){
		document.frmResa.listeResa.value = getResourcesReservees();
		document.frmResa.submit();
	}
	function retour() {
		window.history.back();
		}

	</script>
	
	<style type="text/css">	  
	.titrePanier {
		margin: 3px;
		font-size: 13px;
		font-weight: bold;
		text-align: center;	
	}
	div.hover {
		color: #666666;
		cursor: pointer;
	}
	
	div.selected {
		font-weight: bold; /* En gras */
	}
	
	
	.toggleratStart {
		color: #000000;
		margin-top: 2px;
		padding: 5px;
		background-image: url(<%=m_context%>/admin/jsp/icons/silverpeasV4/fondOff.gif);
		background-repeat: repeat-x;
	}
	
	.elementatStart{
		padding-left: 0px;
		overflow: auto;
	}
	
	.noRessource{
		font-style: italic;	
		color: #666666;
	}

	#accordion {
		height: 200px;
		overflow:auto;
		padding: 0px;
		border-style: outset;
		border-width: 1px 1 1 1;
		border-color: #B3BFD1;
		background-color: #FFFFFF;
	}
	
	#listeReservation {
		height: 200px;
		overflow:auto;
		padding: 0px;
		border-style: outset;
		border-width: 1px 1 1 1;
		border-color: #B3BFD1;
		background-color: #FFFFFF;
	}
	
	</style>
	
 	</head>
	<body>
	<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"Main");
	browseBar.setPath("<a href=\"javascript:retour()\">"+resource.getString("resourcesManager.reservationParametre")+"</a>");
	browseBar.setExtraInformation(resource.getString("resourcesManager.resourceSelection"));

		out.println(window.printBefore());
		out.println(tabbedPane.print());
		out.println(frame.printBefore());
		
		if(listResourcesProblem != null)
		{
			out.println(board.printBefore());
			out.println("<h4>"+resource.getString("resourcesManager.resourceUnReservable")+"</h4>");
			for(int i=0;i<listResourcesProblem.size();i++)
			{ 
				ResourceDetail resourceProblem = (ResourceDetail)listResourcesProblem.get(i);
				out.println(resource.getString("resourcesManager.ressourceNom")+" : "+resourceProblem.getName()+"<br/>");
			}
			out.println(board.printAfter());
			out.println("<br />");
		}
		
		out.println(board.printBefore());
	%>
		 
<TABLE ALIGN="CENTER" CELLPADDING="3" CELLSPACING="0" BORDER="0" WIDTH="100%">
	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.evenement"));%> :</td>
		<td width="100%"><%=evenement%></td>
	</tr>
		
	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.dateDebutReservation"));%> :</TD>
		<td><%=resource.getOutputDateAndHour(reservation.getBeginDate())%></td>
	</tr>

	<tr>
	<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.dateFinReservation"));%> :</td> 
		<td><%=resource.getOutputDateAndHour(reservation.getEndDate())%></td>	
	</tr>

	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.raisonReservation"));%> :</td> 
		<td><%=raison%></TD>
	</tr>

	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.lieuReservation"));%> :</td>
		<td><%=lieu%></TD>
	</tr>
</TABLE>
<%out.println(board.printAfter());%>		
<br />
<%
	/*out.println(board.printBefore());
	out.println(resource.getString("resourcesManager.instructionReservation"));
	out.println(board.printAfter());
	out.println("<br/>");*/
%>
		  <table width="100%" align="center" border="0" cellspacing="5">
		  <tr>
		  <td width="50%" valign="top">
		  
		  <div id="accordion">
			<div class="titrePanier"><center><%=resource.getString("resourcesManager.clickReservation")%></center></div>
			<div id="mesRessources">
			<%
			for (int r=0; r<list.size(); r++)
			{
				ResourceReservableDetail maResource = (ResourceReservableDetail)list.get(r);
				String resourceId = maResource.getResourceId();
				if(!idTemoin.equals(maResource.getCategoryId()))
				{
					if (r != 0)
					{
						if (noResource)
						{%>
							<div id="-1" class="noRessource">
								<center><%=resource.getString("resourcesManager.noResource")%></center>
							</div>
						<%
						}
						out.println("</div>");
						out.println("</div>");
						noResource = true;
					}
					%>
					<div class="toggleratStart"><%=maResource.getCategoryName()%></div>
					<div class="elementatStart">
						<div id="categ<%=maResource.getCategoryId()%>">
					<%
				}
				if(!resourceId.equals("0")) {
					
					//on entre dans ce if, donc il y a une ressource au moins disponible dans la cat�gory, donc on ne veut pas 
					// afficher "pas de ressource disponible dans cette cat�gorie"
					noResource = false;%>
					<div id="<%=resourceId%>" onClick="switchResource(<%=resourceId%>,'categ<%=maResource.getCategoryId()%>');" style="cursor: pointer;">
 						<table width="100%" cellspacing="0" cellpadding="0" border="0">
 							<tr>
 								<td width="80%" nowrap>&nbsp;-&nbsp;<%=maResource.getResourceName()%></td>
 								<td><img src="<%=m_context %>/util/icons/ok.gif" id="image<%=resourceId%>" align="middle"/></td>
 							</tr>
 						</table>								
					</div>
				<%}
				idTemoin = maResource.getCategoryId();
			}
			if (noResource)
			{%>
				<div id="-1" class="noRessource">			
					<%=resource.getString("resourcesManager.noResource")%>
				</div>
			<%}			
			%> 
			</div>
			  </div>
		  </div>
		  </td>
		  <td valign="top" width="50%">
		      <div id="listeReservation">
		      <div class="titrePanier"><center><% out.println(resource.getString("resourcesManager.resourcesReserved"));%></center></div>
		      <%if (listResourceEverReserved != null){ 
		    	  
		  			// la suppression ayant �t� faite, cette boucle permet d'afficher les resources qui n'ont pas pos�s probl�me
		  			for (int i=0;i<listResourceEverReserved.size();i++){
		  						ResourceDetail maRessource =(ResourceDetail)listResourceEverReserved.get(i);
		  		  				String NomResource = (String)maRessource.getName();
		  		  				String resourceId = (String)maRessource.getId();
		  		  				String categoryId = (String)maRessource.getCategoryId();
		  		  			%>
			  					<div id="<%=resourceId%>" onClick="switchResource(<%=resourceId%>,'categ<%=categoryId%>');" style="cursor: pointer;">
			  						<table width="100%" cellspacing="0" cellpadding="0" border="0">
			  							<tr>
			  								<td width="80%" nowrap>&nbsp;-&nbsp;<%=NomResource%> </td>
			  								<td><img src="<%=m_context %>/util/icons/delete.gif" id="image<%=resourceId%>" align="middle"/></td>
			  							</tr>
			  						</table>
			  					</div>
			  				<%
		  				}
		  			}
		  			%>
		      </div>
	</td></tr></table>
	<%
    out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
	out.println(frame.printAfter());
	out.println(window.printAfter());		
	%>
<form name="frmResa" method="post" action="FinalReservation">
	<input type="hidden" name="listeResa" value="">
	<input type="hidden" name="newResourceReservation" value="">
	<%if(idModifiedReservation != null){ %>	
		<input type="hidden" name="idModifiedReservation" value="<%=idModifiedReservation%>">
	<%}%>
</form>	
</body>
</html>