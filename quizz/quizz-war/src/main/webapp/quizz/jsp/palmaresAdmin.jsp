<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

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
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.quizz.control.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.*"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.control.*"%>
<%@ page import="com.stratelia.webactiv.util.score.model.*"%>
<%@ page import="com.stratelia.webactiv.util.score.control.*"%>

<%@ include file="checkQuizz.jsp" %>
<%
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String folderSrc = iconsPath + "/util/icons/delete.gif";
%>

<HTML>
<HEAD>
	<TITLE>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</TITLE>
  <%
  ResourceLocator settings = quizzScc.getSettings();
  String space = quizzScc.getSpaceLabel();
  String component = quizzScc.getComponentLabel();
  String m_Context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

  String currentQuizzTitle="";
  int currentQuizzPoints=0;
  %>
<script language="JavaScript1.2">
function deleteQuizz(quizz_id)
{
  var rep = confirm('<%=resources.getString("QuizzDeleteThisQuizz")%>');
  if (rep==true)
    self.location="deleteQuizz.jsp?quizz_id="+quizz_id
}
function notifyPopup(context,compoId,users,groups)
{
    SP_openWindow(context+'/RnotificationUser/jsp/Main.jsp?popupMode=Yes&editTargets=No&compoId=' + compoId + '&theTargetsUsers='+users+'&theTargetsGroups='+groups, 'notifyUserPopup', '700', '400', 'menubar=no,scrollbars=no,statusbar=no');
}
</script>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
</head>
<body bgcolor=#FFFFFF leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

  <%
  //objet window
  Window window = gef.getWindow();
  window.setWidth("100%");

  //browse bar
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(space);
  browseBar.setComponentName(component);
  browseBar.setExtraInformation(resources.getString("QuizzList"));

  OperationPane operationPane = window.getOperationPane();
  operationPane.addOperation(m_context + "/util/icons/quizz_to_add.gif",resources.getString("QuizzNewQuizz"), "quizzCreator.jsp");

  out.println(window.printBefore());

  Frame frame = gef.getFrame();

  //onglets
  TabbedPane tabbedPane1 = gef.getTabbedPane();
  tabbedPane1.addTab(resources.getString("QuizzOnglet1"),"quizzAdmin.jsp",true);
  tabbedPane1.addTab(resources.getString("QuizzSeeResult"),"quizzResultAdmin.jsp",false);
  
  out.println(tabbedPane1.print());
  out.println(frame.printBefore());

 //Tableau
  ArrayPane arrayPane = gef.getArrayPane("QuizzList","palmaresAdmin.jsp?quizz_id="+request.getParameter("quizz_id"),request,session);

  ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
  arrayColumn0.setSortable(false);
  arrayPane.addArrayColumn(resources.getString("GML.name"));
  arrayPane.addArrayColumn(resources.getString("GML.description"));
  arrayPane.addArrayColumn(resources.getString("QuizzCreationDate"));
  arrayPane.addArrayColumn(resources.getString("GML.operation"));


  Collection quizzList = quizzScc.getAdminQuizzList();
  Iterator i = quizzList.iterator();
  while (i.hasNext()) {
    QuestionContainerHeader quizzHeader = (QuestionContainerHeader) i.next();
		IconPane folderPane1 = gef.getIconPane();
	Icon folder1 = folderPane1.addIcon();
	folder1.setProperties(folderSrc, "", "javascript:deleteQuizz("+quizzHeader.getPK().getId()+");");

    ArrayLine arrayLine = arrayPane.addArrayLine();
    arrayLine.addArrayCellLink("<img src=\"icons/palmares_30x15.gif\" border=0>","palmaresAdmin.jsp?quizz_id="+quizzHeader.getPK().getId());
    arrayLine.addArrayCellLink(quizzHeader.getTitle(),"quizzQuestionsNew.jsp?QuizzId="+quizzHeader.getPK().getId()+"&Action=ViewQuizz");
    arrayLine.addArrayCellText(Encode.javaStringToHtmlString(quizzHeader.getDescription()));
    
    Date creationDate = DateUtil.parse(quizzHeader.getCreationDate());
	ArrayCellText arrayCellText5 = arrayLine.addArrayCellText(resources.getOutputDate(creationDate));
	arrayCellText5.setCompareOn(creationDate);
	
    arrayLine.addArrayCellIconPane(folderPane1);
    if (quizzHeader.getPK().getId().equals(request.getParameter("quizz_id")))
    {
      currentQuizzTitle=quizzHeader.getTitle();
      currentQuizzPoints=quizzHeader.getNbMaxPoints();
    }
  }
  out.println(arrayPane.print());
%>

<!--  FIN TAG FORM-->
<%
out.println(frame.printMiddle());
out.println("<br>&nbsp;&nbsp;<span class=sousTitreFenetre>"+resources.getString("QuizzSeeResult")+"&nbsp;:&nbsp;"+currentQuizzTitle+"</span>");
%>
<!-- Resultats du quizz -->
<%
 //Tableau
  ArrayPane arrayPane2 = gef.getArrayPane("QuizzResult","palmaresAdmin.jsp?quizz_id="+request.getParameter("quizz_id"),request,session);

  arrayPane2.addArrayColumn(resources.getString("ScorePosition"));
  arrayPane2.addArrayColumn(resources.getString("GML.user"));
  arrayPane2.addArrayColumn(resources.getString("ScoreDate"));
  arrayPane2.addArrayColumn(resources.getString("ScoreLib"));

  Collection scoreList = quizzScc.getAdminPalmares(request.getParameter("quizz_id"));

  if (scoreList != null)
  {
    Iterator j = scoreList.iterator();
    while (j.hasNext()) {
      ScoreDetail scoreDetail = (ScoreDetail) j.next();
      UserDetail userDetail=quizzScc.getUserDetail(scoreDetail.getUserId());
      String firstName = "";
      String lastName = resources.getString("UserUnknown");
      String recipient="";
      if (userDetail != null)
      {
	      firstName = userDetail.getFirstName();
	      lastName = userDetail.getLastName();
	      recipient=userDetail.getId();
      }

      ArrayLine arrayLine = arrayPane2.addArrayLine();
      ArrayCellText arrayCellText3 = arrayLine.addArrayCellText(new Integer(scoreDetail.getPosition()).toString());
      arrayCellText3.setCompareOn(new Integer(scoreDetail.getPosition()));
      ArrayCellText arrayCellText2;
      if (!recipient.equals(""))
		  {
			arrayCellText2 = arrayLine.addArrayCellText("<A HREF=\"javascript:notifyPopup('" + m_Context + "','" + quizzScc.getComponentId() + "','" + recipient + "','')\">" + lastName + " " + firstName +"</A>");
		  }
      else
	 arrayCellText2 = arrayLine.addArrayCellText(lastName + " " + firstName);
      arrayCellText2.setCompareOn((String) (lastName + " " + firstName).toLowerCase());
      
		Date participationDate = DateUtil.parse(scoreDetail.getParticipationDate());
	    arrayLine.addArrayCellLink(resources.getOutputDate(participationDate),"quizzQuestionsNew.jsp?QuizzId="+request.getParameter("quizz_id")+"&Action=ViewResultAdmin&Page=0"+"&UserId="+scoreDetail.getUserId()+"&ParticipationId="+new Integer(scoreDetail.getParticipationId()).toString());
      
      ArrayCellText arrayCellText1 = arrayLine.addArrayCellText(new Integer(scoreDetail.getScore()).toString()+"/"+currentQuizzPoints);
      arrayCellText1.setCompareOn(new Integer(scoreDetail.getScore()));
    }
  }
  out.println(arrayPane2.print());
%>
<!-- fin résultats -->
<!-- moyenne -->
<div align="right">
  <table width="100%" border="0" cellspacing="0" cellpadding="5">
    <tr>
      <td width="100%" align="left" valign="middle" class="textePetitBold2">&nbsp;<%=resources.getString("QuizzNbVoters")%>&nbsp;:&nbsp;
        <%=quizzScc.getNbVoters(request.getParameter("quizz_id"))%>
      </td>
      <td width="100%" align="right" valign="middle"  class="textePetitBold2"><%=resources.getString("QuizzAverage")%>&nbsp;:&nbsp;</td>
      <td align="right">
        <table border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td rowspan="3"><img src="icons/tableProf_1.gif" width="7" height="70"></td>
            <td><img src="icons/tableProf_2.gif" width="55" height="6"></td>
            <td rowspan="3"><img src="icons/tableProf_4.gif" width="79" height="70"></td>
          </tr>
          <tr>
            <td height="42" bgcolor="#387B80"><span class="titreFenetre2"><div align="center">
<%
float average=quizzScc.getAveragePoints(request.getParameter("quizz_id"));
int averageInt=Math.round(average);
if (averageInt==average)
  out.println(averageInt);
else
  out.println(average);
%>
              <br>
              <img src="icons/1pxBlanc.gif" width="30" height="1"><br>
              <%=currentQuizzPoints%></div></span>
           </td>
          </tr>
          <tr>
            <td><img src="icons/tableProf_3.gif" width="55" height="22"></td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</div>
<!-- fin moyenne -->
<%
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</BODY>
</HTML>


