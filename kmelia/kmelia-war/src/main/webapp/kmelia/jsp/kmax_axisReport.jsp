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
<%@ page language="java" %>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail, java.util.Collection, java.util.Iterator"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="javax.ejb.RemoveException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.kmelia.control.KmeliaSessionController"%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>

<%@ page import="com.silverpeas.util.i18n.*"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%!
String getAxisAllLabel(int valueMaxLength, KmeliaSessionController kmeliaScc) {

      int nbSpaces = (valueMaxLength - kmeliaScc.getString("AllComponents").length()) / 2;
      String allLabel = "";
      for (int i = 1; i <= nbSpaces; i++)
          allLabel += ".";
      allLabel = kmeliaScc.getString("AllComponents");
      for (int j = 1; j <= nbSpaces; j++)
          allLabel += ".";
      return allLabel;
}

String getTimeAxis(KmeliaSessionController kmeliaScc, ResourceLocator timeSettings, String defaultValue) throws CreateException, SQLException, NamingException, RemoteException, FinderException {

      List keys = kmeliaScc.getTimeAxisKeys();
      StringBuffer axis = new StringBuffer(1000);
      axis.append("<div align=\"center\">");
        axis.append("<TABLE width=\"99%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" class=\"intfdcolor\">");
          axis.append("<TR>");
            axis.append("<TD>");
             axis.append("<div align=\"center\" class=\"textePetitBold\">"+kmeliaScc.getString("TimeAxis")+"</div>");
              axis.append("<TABLE width=\"100%\" border=\"0\" cellspacing=\"2\" cellpadding=\"0\" class=\"intfdcolor4\">");
                axis.append("<TR>");
                  axis.append("<TD>");
                    axis.append("<div align=\"center\">");
                          axis.append("<select name=\"\" size=\"1\"\">");
				      String key = "";
				      StringBuffer values = new StringBuffer(1000);
				      String value = "";
						String selectValue = ""; 
						int valueMaxLength = 0;
       
						for (int i = 0; i < keys.size(); i++) {
							selectValue = "";
							key = ((Integer) keys.get(i)).toString();
							value = timeSettings.getString(key);
					       
							if (value.length() > valueMaxLength)
								valueMaxLength = value.length();
						
							if (key.equals(defaultValue))
								selectValue = "selected";
						       
							values.append("<option value=\""+key+"\""+selectValue+">"+value+"</option>");
						}
      
                          axis.append("<option value=\"X\">"+kmeliaScc.getString("AllComponents")+"</option>");
                          axis.append(values);
                      axis.append("</select>");
                    axis.append("</div>");
                  axis.append("</TD>");
                axis.append("</TR>");
              axis.append("</TABLE>");
            axis.append("</TD>");
        axis.append("</TABLE>");
      axis.append("</div>");
    return axis.toString();
}

ArrayList getAxis(KmeliaSessionController kmeliaScc, boolean axisLinked, ArrayList combination, String translation) throws CreateException, SQLException, NamingException, RemoteException, FinderException {
      List list = kmeliaScc.getAxis();
      Iterator iterator = list.iterator();
      ArrayList axisList = new ArrayList();
      if (iterator.hasNext()) {
            String axisName = "";
            int axisNb = 0;
            StringBuffer axis = new StringBuffer(1000);
            String selectValue = "";
            while (iterator.hasNext()) {
                  NodeDetail node = (NodeDetail) iterator.next();
                  if (node.getLevel() == 2) {
                      //It's an axis
                      axisName = node.getName(translation);
                      axisNb++;
                      if (axis.length()>0) {
                                          axis.append("</select>");
                                        axis.append("</div>");
                                      axis.append("</TD>");
                                    axis.append("</TR>");
                                  axis.append("</TABLE>");
                                axis.append("</TD>");
                            axis.append("</TABLE>");
                          axis.append("</div>");
                          axisList.add(axis.toString());
                          axis = new StringBuffer(1000);
                      }
                      axis.append("<div align=\"center\">");
                        axis.append("<TABLE width=\"99%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" class=\"intfdcolor\">");
                          axis.append("<TR>");
                            axis.append("<TD>");
                              if (axisLinked)
                                  axis.append("<div align=\"center\" class=\"textePetitBold\"><A href=\"javaScript:axisManage('"+node.getNodePK().getId()+"')\" class=\"textePetitBold\">"+Encode.javaStringToHtmlString(node.getName(translation))+"</a></div>");
                              else
                                  axis.append("<div align=\"center\" class=\"textePetitBold\">"+Encode.javaStringToHtmlString(node.getName(translation))+"</div>");
                              axis.append("<TABLE width=\"100%\" border=\"0\" cellspacing=\"2\" cellpadding=\"0\" class=\"intfdcolor4\">");
                                axis.append("<TR>");
                                  axis.append("<TD>");
                                    axis.append("<div align=\"center\">");
                                        if (axisLinked)
                                          axis.append("<select name=\""+node.getNodePK().getId()+"\" size=\"1\" onChange=\"positionManage(this)\">");
                                        else
                                          axis.append("<select name=\""+node.getNodePK().getId()+"\" size=\"1\"\">");
                                        axis.append("<option value=\""+node.getPath()+node.getNodePK().getId()+"\">"+kmeliaScc.getString("AllComponents")+"</option>");
                  } else if (node.getLevel() == 3) {
                      selectValue = "";
										  if (combination.contains(node.getPath()+node.getId()))
                           selectValue = "selected";
						 				  axis.append("<option value=\""+node.getPath()+node.getNodePK().getId()+"|"+Encode.javaStringToHtmlString(axisName)+"\" class=\"intfdcolor51\" "+selectValue+">"+Encode.javaStringToHtmlString(node.getName(translation))+"</option>");
	                  } else {
	                      String spaces = "";
	                      for (int i=0; i<node.getLevel()-3; i++)
	                        spaces += "&nbsp;&nbsp;";
		                    selectValue = "";
											  if (combination.contains(node.getPath()+node.getId()))
	                            selectValue = "selected";
											  axis.append("<option value=\""+node.getPath()+node.getNodePK().getId()+"|"+Encode.javaStringToHtmlString(axisName)+"\" class=\"intfdcolor5\" "+selectValue+">"+spaces+Encode.javaStringToHtmlString(node.getName(translation))+"</option>");
	                  }
 			           }
                      axis.append("</select>");
                    axis.append("</div>");
                  axis.append("</TD>");
                axis.append("</TR>");
              axis.append("</TABLE>");
            axis.append("</TD>");
        axis.append("</TABLE>");
      axis.append("</div>");
      axisList.add(axis.toString());
      }

      return axisList;
}

String displayAxisCombinationToUsers(KmeliaSessionController kmeliaScc, GraphicElementFactory gef, ArrayList combination, String timeCriteria, String translation) throws CreateException, SQLException, NamingException, RemoteException, FinderException {
    String result = displayAxis(kmeliaScc, gef, false, true, combination, timeCriteria, kmeliaScc.isTimeAxisUsed(), null, translation);
    return result;
}

String displayAxisToUsers(KmeliaSessionController kmeliaScc, GraphicElementFactory gef, String translation) throws CreateException, SQLException, NamingException, RemoteException, FinderException {
    String result = displayAxis(kmeliaScc, gef, false, true, new ArrayList(), null, kmeliaScc.isTimeAxisUsed(), null, translation);
    return result;
}

String displayAxisToPublish(KmeliaSessionController kmeliaScc, GraphicElementFactory gef, String translation) throws CreateException, SQLException, NamingException, RemoteException, FinderException {
	String result;
	ArrayList currentCombination = new ArrayList();
	if (kmeliaScc.getCurrentCombination() != null)
		currentCombination = kmeliaScc.getCurrentCombination();
    result = displayAxis(kmeliaScc, gef, false, true, currentCombination, null, false, null, translation);
    return result;
}

String displayAxisToAdmins(KmeliaSessionController kmeliaScc, GraphicElementFactory gef, String translation) throws CreateException, SQLException, NamingException, RemoteException, FinderException {
    String result = displayAxis(kmeliaScc, gef, true, false, new ArrayList(), null, false, kmeliaScc.getString("AdminExplaination"), translation);
    return result;
}

String displayAxis(KmeliaSessionController kmeliaScc, GraphicElementFactory gef, boolean axisLinked, boolean searchEnabled, ArrayList combination,  String timeCriteriaValue, boolean timeAxisEnabled, String explaination, String translation) throws CreateException, SQLException, NamingException, RemoteException, FinderException {
    StringBuffer result = new StringBuffer(1000);
    ArrayList axisList = getAxis(kmeliaScc, axisLinked, combination, translation);
    
    if (timeAxisEnabled && axisList.size() > 0) {
        //get the time axis
        ResourceLocator timeSettings = new ResourceLocator("com.stratelia.webactiv.kmelia.multilang.timeAxisBundle", kmeliaScc.getLanguage());
        axisList.add(getTimeAxis(kmeliaScc, timeSettings,  timeCriteriaValue));
    }
    
    Board board = gef.getBoard();

    int nbCol = 3;
    result.append("<form name=\"axisForm\" Action=\"\" method=\"Post\">");
    result.append(board.printBefore());
    //result.append("<table width=\"98%\" border=\"0\" cellspacing=\"1\" cellpadding=\"2\" class=\"intfdcolor5\" align=center>\n");
	//result.append("<tr align=center>\n");
    //result.append("<td class=\"intfdcolor\"  align=center>\n");
    result.append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" align=center>");

	result.append("<tr>\n");
	result.append("<td colspan="+(nbCol+1)+" class=\"intfdcolor4\" align=center>\n"); 
	result.append("&nbsp;&nbsp;</td>\n");
	result.append("</tr>\n");

    Iterator i = axisList.iterator();
    if (i.hasNext()) {
          int j=1;
          boolean endRaw = false;
          while (i.hasNext()) {
            String axis = (String) i.next();
            if (j==1) {
              result.append("<tr>\n");
              result.append("<td class=\"intfdcolor4\" valign=\"top\">&nbsp;</td>\n");
              endRaw = false;
            }
            if (j<=nbCol){
              result.append("<td class=\"intfdcolor4\" valign=\"top\">\n");
              result.append(axis);
              result.append("</td>");
              j++;
            }
            if (j>nbCol) {
              result.append("\t</tr>");
              endRaw=true;
              j=1;
            }
          }
          if (!endRaw) {
              int nbTd = nbCol-j+1;
              int k=1;
              while (k<=nbTd) {
                      result.append("<td class=\"intfdcolor4\" valign=\"top\">&nbsp;</td>\n");
                      k++;
              }
              result.append("</tr>\n");
          }
    }
    if (searchEnabled && (axisList.size() > 0)) {
	    result.append("<tr><td class=\"intfdcolor4\" colspan=\""+nbCol+1+"\" align=\"center\"><BR>");
    }

	result.append("</td></tr>");
	result.append("</table>");
	result.append("<br/>");
	result.append(board.printAfter());
    //result.append("</td></tr></table>");
	
	if (searchEnabled && (axisList.size() > 0)) {
	    ButtonPane lebouton = gef.getButtonPane();
        Button validerButton = gef.getFormButton(kmeliaScc.getString("Validate"), "javaScript:search()", false);
        lebouton.addButton(validerButton);
        lebouton.setVerticalPosition();
        lebouton.setHorizontalPosition();
		result.append("<BR><center>");
		result.append(lebouton.print());
		result.append("</center>");}

	result.append("</form>");
	return result.toString();
}

String displayAxisManageView(KmeliaSessionController kmeliaScc, GraphicElementFactory gef, String axisId, String mandatoryFieldSrc, ResourcesWrapper resources, String translation)
	 throws CreateException, SQLException, NamingException, RemoteException, FinderException {
    NodeDetail axis = kmeliaScc.getNodeHeader(axisId);
    StringBuffer result = new StringBuffer(1000);
    
    Board board = gef.getBoard();
    result.append(board.printBefore());
    //result.append("<TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor>");
      //result.append("<tr>"); 
        //result.append("<td>"); 
          result.append("<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\">");
          result.append("<form name=\"axisManagerForm\" Method=\"Post\">");
          result.append("<input type=\"hidden\" name=\"Id\" value=\""+axisId+"\">");
            result.append("<tr>");
              result.append("<td colspan=\"6\" class=\"txtnav\"><b>"+kmeliaScc.getString("AxisInformations")+" :</b></td>");
            result.append("</tr>");

            result.append(I18NHelper.getFormLine(resources, axis, translation));
            
            result.append("<tr>");
              result.append("<td width=\"20%\" class=\"txtlibform\">"+kmeliaScc.getString("AxisTitle")+" : </td>");
              result.append("<td>"); 
                result.append("<input type=\"text\" id=\"nodeName\" name=\"Name\" value=\""+Encode.javaStringToHtmlString(axis.getName(translation))+"\" size=\"61\" maxlength=\"50\">&nbsp;<img src=\""+mandatoryFieldSrc+"\" border=0 width=5 height=5>");
              result.append("</td>");
              result.append("<td>&nbsp;</td>");
              result.append("<td>&nbsp;</td>");
              result.append("<td>&nbsp;</td>");
              result.append("<td>&nbsp;</td>");
            result.append("</tr>");
            result.append("<tr>"); 
              result.append("<td width=\"20%\" class=\"txtlibform\">"+kmeliaScc.getString("AxisDescription")+" : </td>");
              result.append("<td>"); 
                result.append("<textarea id=\"nodeDesc\" name=\"Description\" cols=\"60\" rows=\"5\" wrap=\"PHYSICAL\">"+axis.getDescription(translation)+"</textarea>");
              result.append("</td>");
              result.append("<td colspan=\"4\">&nbsp;");
              result.append("</td>");
            result.append("</tr>");
            result.append("<tr>");
              result.append("<td colspan=\"6\" align=left>( <img border=\"0\" src=\""+mandatoryFieldSrc+"\" width=\"5\" height=\"5\"> : "+kmeliaScc.getString("ChampsObligatoires")+" ) </td>");
            result.append("</tr>");
            result.append("<tr>"); 
              result.append("<td colspan=\"6\" align=\"center\">"); 

                ButtonPane lebouton = gef.getButtonPane();
                Button ajouterButton = gef.getFormButton(kmeliaScc.getString("AddComponent"), "javaScript:addPositionToAxis('"+axisId+"')", false);
                Button validerButton = gef.getFormButton(kmeliaScc.getString("Validate"), "javaScript:axisUpdate()", false);
                Button supprimerButton = gef.getFormButton(kmeliaScc.getString("DeleteAxis"), "javaScript:axisDelete()", false);

                lebouton.addButton(validerButton);
                lebouton.addButton(ajouterButton);
                lebouton.addButton(supprimerButton);

                lebouton.setVerticalPosition();
                lebouton.setHorizontalPosition();

				result.append("</td>");
            result.append("</tr>");
            result.append("</form>");
          result.append("</table>");

        result.append(board.printAfter());
          
	result.append("<center><br>");
	result.append(lebouton.print());
	result.append("</br></center>");
	result.append("<script language=\"javascript\">document.axisManagerForm.Name.focus();</script>");
    return result.toString();
}

String displayComponentManageView(KmeliaSessionController kmelia, GraphicElementFactory gef, String componentId, String path, String mandatoryFieldSrc, ResourcesWrapper resources, String translation) throws CreateException, SQLException, NamingException, RemoteException, FinderException {
    NodeDetail nodeDetail = kmelia.getNodeHeader(componentId);
    StringBuffer result = new StringBuffer(1000);
    Board board = gef.getBoard();
    result.append(board.printBefore());
          result.append("<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\">");
          result.append("<form name=\"axisManagerForm\" Method=\"Post\">");
          result.append("<input type=\"hidden\" name=\"Id\" value=\""+componentId+"\">");
            result.append("<tr>");
              result.append("<td colspan=\"6\" class=\"txtnav\"><b>"+kmelia.getString("ComponentInformations")+" :</b></td>");
            result.append("</tr>");
            result.append("<tr>"); 
              result.append("<td width=\"20%\" class=\"txtlibform\">"+kmelia.getString("ComponentFather")+ " : </td>");
              result.append("<td>"); 
                result.append(path);
              result.append("</td>");
              result.append("<td>&nbsp;</td>");
              result.append("<td>&nbsp;</td>");
              result.append("<td>&nbsp;</td>");
              result.append("<td>&nbsp;</td>");
            result.append("</tr>");
            result.append(I18NHelper.getFormLine(resources, nodeDetail, translation));
            result.append("<tr>"); 
              result.append("<td width=\"20%\" class=\"txtlibform\">"+kmelia.getString("ComponentTitle")+" : </td>");
              result.append("<td>"); 
                result.append("<input type=\"text\" id=\"nodeName\" name=\"Name\" value=\""+Encode.javaStringToHtmlString(nodeDetail.getName(translation))+"\" size=\"61\" maxlength=\"50\">&nbsp;<img src=\""+mandatoryFieldSrc+"\" border=0 width=5 height=5>");
              result.append("</td>");
              result.append("<td>&nbsp;</td>");
              result.append("<td>&nbsp;</td>");
              result.append("<td>&nbsp;</td>");
              result.append("<td>&nbsp;</td>");
            result.append("</tr>");
            result.append("<tr>"); 
              result.append("<td width=\"20%\" class=\"txtlibform\">"+kmelia.getString("ComponentDescription")+" : </td>");
              result.append("<td>"); 
                result.append("<textarea id=\"nodeDesc\" name=\"Description\" cols=\"60\" rows=\"5\" wrap=\"PHYSICAL\">"+nodeDetail.getDescription(translation)+"</textarea>");
              result.append("</td>");
              result.append("<td colspan=\"4\">&nbsp;");
              result.append("</td>");
            result.append("</tr>");
            result.append("<tr>");
              result.append("<td colspan=\"6\" align=left>( <img border=\"0\" src=\""+mandatoryFieldSrc+"\" width=\"5\" height=\"5\"> : "+kmelia.getString("ChampsObligatoires")+" ) </td>");
            result.append("</tr>");
          
            result.append("<tr>");
              result.append("<td colspan=\"6\" align=\"center\">"); 

                ButtonPane lebouton = gef.getButtonPane();
                Button ajouterButton = gef.getFormButton(kmelia.getString("AddComponent"), "javaScript:addPositionToPosition('"+componentId+"')", false);
                Button validerButton = gef.getFormButton(kmelia.getString("Validate"), "javaScript:positionUpdate()", false);
                Button supprimerButton = gef.getFormButton(kmelia.getString("DeleteComponent"), "javaScript:positionDelete()", false);

                lebouton.addButton(validerButton);
                lebouton.addButton(ajouterButton);
                lebouton.addButton(supprimerButton);

                lebouton.setVerticalPosition();
                lebouton.setHorizontalPosition();

                result.append("</td>");
            result.append("</tr>");
            result.append("</form>");
    result.append("</table>");
    result.append(board.printAfter());
	result.append("<center><br>");
	result.append(lebouton.print());
	result.append("</br></center>");
	result.append("<script language=\"javascript\">document.axisManagerForm.Name.focus();</script>");
    return result.toString();
}

String displayPath(Collection path, boolean linked, int beforeAfter, String translation) {
      String linkedPathString = new String();
      String pathString = new String();
      int nbItemInPath = path.size();
      Iterator iterator = path.iterator();
      boolean alreadyCut = false;
      int i = 0;
      while (iterator.hasNext()) {
            NodeDetail nodeInPath = (NodeDetail) iterator.next();
            if (nodeInPath.getLevel() != 1) {
              if ((i <= beforeAfter) || (i + beforeAfter >= nbItemInPath - 1)){
                  linkedPathString += "<a href=\"javascript:onClick=topicGoTo('"+nodeInPath.getNodePK().getId()+"')\">"+Encode.javaStringToHtmlString(nodeInPath.getName(translation))+"</a>";
                  pathString += Encode.javaStringToHtmlString(nodeInPath.getName(translation));
                  if (iterator.hasNext()) {
                        linkedPathString += " > ";
                        pathString += " > ";
                  }
             } else {
                  if (!alreadyCut) {
                        linkedPathString += " ... > ";
                        pathString += " ... > ";
                        alreadyCut = true;
                  }
             }
             i++;
           }
      }
      if (linked)
          return linkedPathString;
      else
          return pathString;
}


%>