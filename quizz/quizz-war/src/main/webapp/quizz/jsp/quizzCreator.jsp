<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="javax.naming.Context,javax.naming.InitialContext,javax.rmi.PortableRemoteObject"%>
<%@ page import="javax.ejb.RemoveException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.quizz.control.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.question.model.Question "%>
<%@ page import="com.stratelia.webactiv.util.answer.model.Answer "%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.*"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.control.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>

<jsp:useBean id="quizzUnderConstruction" scope="session" class="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail" />

<%@ include file="checkQuizz.jsp" %>

<%
//Récupération des paramètres
String action = "";
String quizzId = "";
String title = "";
String description = "";
String creationDate = "";
String beginDate = "";
String endDate = "";
String nbQuestions = "";
String notice="";
String nbAnswersNeeded = "1";
String nbAnswersMax = "1";
String nextAction = "";

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

String space = quizzScc.getSpaceLabel();
String component = quizzScc.getComponentLabel(); 

Button validateButton = null;
Button cancelButton = null;
//QuestionContainerDetail questionContainerDetail = null;

action = (String) request.getParameter("Action");
quizzId = (String) request.getParameter("QuizzId");
title = (String) request.getParameter("title");
description = (String) request.getParameter("description");
beginDate = (String) request.getParameter("beginDate");
endDate = (String) request.getParameter("endDate");
nbQuestions = (String) request.getParameter("nbQuestions");
notice=(String) request.getParameter("notice");
nbAnswersNeeded = (String) request.getParameter("nbAnswersNeeded");
nbAnswersMax = (String) request.getParameter("nbAnswersMax");
//Mise a jour de l'espace
if (action == null) {
	action = "CreateQuizz";
}

%>
<HTML>
<HEAD>
<TITLE>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript1.2">
function sendData() {
    if (isCorrectForm()) {
        document.quizzForm.submit();
    }
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.quizzForm.title.value);
     var nbQuestions = document.quizzForm.nbQuestions.value;
     var notice= document.quizzForm.notice.value;
     var description= document.quizzForm.description.value;
     var nbAnswersNeeded =  document.quizzForm.nbAnswersNeeded.value;
     var nbAnswersMax =  document.quizzForm.nbAnswersMax.value;
     var beginDate = document.quizzForm.beginDate.value;
     var endDate = document.quizzForm.endDate.value;
     var yearBegin = extractYear(beginDate, '<%=quizzScc.getLanguage()%>'); 
     var monthBegin = extractMonth(beginDate, '<%=quizzScc.getLanguage()%>');
     var dayBegin = extractDay(beginDate, '<%=quizzScc.getLanguage()%>');
     var yearEnd = extractYear(endDate, '<%=quizzScc.getLanguage()%>'); 
     var monthEnd = extractMonth(endDate, '<%=quizzScc.getLanguage()%>');
     var dayEnd = extractDay(endDate, '<%=quizzScc.getLanguage()%>');
     var re = /(\d\d\/\d\d\/\d\d\d\d)/i;

     if (isWhitespace(title)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++; 
     }
     if (isWhitespace(description)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.description")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++; 
     }
     else
     {
        if (!isValidTextArea(document.quizzForm.description)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.description")%>' <%=resources.getString("MustContainsLessCar")%> <%=DBUtil.TextAreaLength%> <%=resources.getString("Caracters")%>\n";
           errorNb++; 
        }
     }
     if (!isValidTextArea(document.quizzForm.notice)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNotice")%>' <%=resources.getString("MustContainsLessCar")%> <%=DBUtil.TextAreaLength%> <%=resources.getString("Caracters")%>\n";
           errorNb++; 
        }

     if (isWhitespace(beginDate)) {
     } else {
           if (beginDate.replace(re, "OK") != "OK") {
		     errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationBeginDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
		     errorNb++;
           } else {
                 if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) {
		     errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationBeginDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
		     errorNb++;
                 }
           }
     }
     if (isWhitespace(endDate)) {
     } else {
           if (endDate.replace(re, "OK") != "OK") {
               errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationEndDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
               errorNb++;
	   } else {
           if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
               errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationEndDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
               errorNb++;
           } else {
               if ((isWhitespace(beginDate) == false) && (isWhitespace(endDate) == false)) {
                     if (isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin) == false) {
                            errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationEndDate")%>' <%=resources.getString("GML.MustContainsPostDate")%>\n";
                            errorNb++;
                     }
               }      
           }
	   }
     }
     if (isWhitespace(nbQuestions)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbQuestionPerPage")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++; 
     } else {
           if (isInteger(nbQuestions) == false) {
               errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbQuestionPerPage")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
               errorNb++; 
           } else {
                if (nbQuestions <= 0) {
                   errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbQuestionPerPage")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
                   errorNb++;
                }
           }
     }
     if (!isWhitespace(nbAnswersMax)) {
           if (isInteger(nbAnswersMax) == false) {
               errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbPossibleAnswer")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
               errorNb++; 
           } 
           else {
                if (nbAnswersMax <= 0) {
                   errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbPossibleAnswer")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
                   errorNb++;
                }
           }
     }
     if (!isWhitespace(nbAnswersNeeded)) {
          if (isInteger(nbAnswersNeeded) == false) {
               errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbAnswerNeeded")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
               errorNb++; 
           } else {
                if (nbAnswersNeeded <= 0) {
                   errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbAnswerNeeded")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
                   errorNb++;
                }
                else
                {
                    if (Number(nbAnswersNeeded) > Number(nbAnswersMax))
                    {
                      errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbAnswerNeeded")%>' <%=resources.getString("MustContainsInfNumber")%> '<%=resources.getString("QuizzCreationNbPossibleAnswer")%>'\n";
                      errorNb++;
                    }
                }
           }
     }
          
     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContain")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContain")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}

</script>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
if (action.equals("SendNewQuizz")) {
      if (beginDate != null) {
          if (beginDate.length()>0)
            beginDate = resources.getDBDate(beginDate);
          else
            beginDate = null;
      }
      if (endDate != null) {
          if (endDate.length()>0)
            endDate = resources.getDBDate(endDate);
          else
            endDate = null;
      }

	  /*if(nbAnswersMax == null || nbAnswersMax.trim().length() == 0) {
			nbAnswersMax = "0";
	  }

	  if(nbAnswersNeeded == null || nbAnswersNeeded.trim().length() == 0) {
			nbAnswersNeeded = "0";
	  }  */

      QuestionContainerHeader questionContainerHeader = new QuestionContainerHeader(null, title, description,notice, null, null, beginDate, endDate, false, 0, new Integer(nbQuestions).intValue(),new Integer(nbAnswersMax).intValue(),new Integer(nbAnswersNeeded).intValue(),0);
      QuestionContainerDetail questionContainerDetail = (QuestionContainerDetail) session.getAttribute("quizzUnderConstruction");
      questionContainerDetail.setHeader(questionContainerHeader);
      session.setAttribute("quizzUnderConstruction", questionContainerDetail);
} //End if action = ViewResult
else if (action.equals("CreateQuizz")) {
      cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "Main.jsp", false);
      validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
      session.removeAttribute("quizzUnderConstruction");
      title = "";
      creationDate = resources.getOutputDate(new Date());
      beginDate = creationDate;
      endDate = "";
      nbQuestions = "1";
      notice="";
      description="";
      nbAnswersNeeded = "1";
      nbAnswersMax = "1";
      nextAction="SendNewQuizz";
      
      Window window = gef.getWindow();
      Frame frame =gef.getFrame();
      Board board = gef.getBoard();

      BrowseBar browseBar = window.getBrowseBar();
      browseBar.setDomainName(space);
      browseBar.setComponentName(component);
      browseBar.setExtraInformation(resources.getString("QuizzCreation"));

      out.println(window.printBefore());
      out.println(frame.printBefore());
      out.println(board.printBefore());
%>
<center>
<table CELLPADDING=5 width="100%">
	<form name="quizzForm" Action="quizzCreator.jsp" method="POST">
    <tr><td class="txtlibform" valign="baseline" align=left width="100"><%=resources.getString("GML.name")%> :</td>
		<td><input type="text" name="title" size="50" maxlength="<%=DBUtil.TextFieldLength%>" value="<%=Encode.javaStringToHtmlString(title)%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
	</tr>
    <tr><td class="txtlibform" valign="baseline" align=left><%=resources.getString("QuizzCreationDate")%> :</td>
		<td><%=creationDate%></td>
	</tr>
    <tr><td class="txtlibform" valign="baseline" align=left><%=resources.getString("QuizzCreationBeginDate")%> :</td>
		<td><input type="text" name="beginDate" size="11" maxlength="<%=DBUtil.DateFieldLength%>" value="<%=beginDate%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
	</tr>
    <tr><td class="txtlibform" valign="baseline" align=left><%=resources.getString("QuizzCreationEndDate")%> :</td>
		<td><input type="text" name="endDate" size="11"  maxlength="<%=DBUtil.DateFieldLength%>" value="<%=endDate%>"></td>
	</tr>
    <tr><td class="txtlibform" valign="baseline" align=left><%=resources.getString("QuizzCreationNbQuestionPerPage")%> :</td>
		<td><input type="text" name="nbQuestions" size="5" maxlength="3" value="<%=nbQuestions%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
	</tr>
    <tr><td class="txtlibform" valign="baseline" align=left><%=resources.getString("QuizzCreationNbPossibleAnswer")%> :</td>
		<td><input type="text" name="nbAnswersMax" size="5" maxlength="3" value="<%=nbAnswersMax%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
	</tr>
    <tr><td class="txtlibform" valign="baseline" align=left><%=resources.getString("QuizzCreationNbAnswerNeeded")%> :</td>
		<td><input type="text" name="nbAnswersNeeded" size="5" maxlength="3" value="<%=nbAnswersNeeded%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
	</tr>
    <tr><td class="txtlibform" valign="top" align=left><%=resources.getString("GML.description")%> :</td>
		<td><textarea name="description" cols="49" wrap="VIRTUAL" rows="3"><%=Encode.javaStringToHtmlString(description)%></textarea>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
	</tr>
    <tr><td class="txtlibform" valign="top" align=left><%=resources.getString("QuizzCreationNotice")%> :</td>
		<td><textarea name="notice" cols="49" wrap="VIRTUAL" rows="3"><%=Encode.javaStringToHtmlString(notice)%></textarea></td>
	</tr>
    <tr><td colspan=2><input type="hidden" name="Action" value="<%=nextAction%>"></td>
	</tr>
	<tr><td class="intfdcolor4" valign="top" align=left colspan=2 nowrap><span class="txt">( <img src="<%=mandatoryField%>" width="5" height="5"> = <%=resources.getString("GML.requiredField")%> ) </span> 
		</td>
	</tr>
	</form>
</table>
<%
	out.println(board.printAfter());
    out.println(frame.printMiddle());
%>
<BR>
<center>
<%    
      ButtonPane buttonPane = gef.getButtonPane();
      buttonPane.addButton(validateButton);
      buttonPane.addButton(cancelButton);
      buttonPane.setHorizontalPosition();
      out.println(buttonPane.print());
%>
</center>
<%
    out.println(frame.printAfter());
	out.println(window.printAfter());
    out.println("</BODY></HTML>");
 } //End if action = ViewQuestion
if (action.equals("SendNewQuizz")) {
%>
<HTML>
<HEAD>
<script language="Javascript">
    function goToQuestionCreator() {
        document.questionForm.submit();
    }
</script>
</HEAD>
<BODY onLoad="goToQuestionCreator()">
<Form name="questionForm" Action="questionCreator.jsp" Method="POST" ENCTYPE="multipart/form-data">
<input type="hidden" name="Action" value="FirstQuestion">
</Form>
</BODY>
</HTML>
<% } %>