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
<%@ page isELIgnored="false"%>
<%@ page import="com.ecyrd.jspwiki.*"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ page import="javax.servlet.jsp.jstl.fmt.*"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<fmt:setLocale value="${userLanguage}" />
<fmt:setBundle basename="templates.default" />
<%
  WikiContext c = WikiContext.findContext(pageContext);
  int attCount = c.getEngine().getAttachmentManager().listAttachments(
      c.getPage()).size();
  String attTitle = LocaleSupport.getLocalizedMessage(pageContext,
      "attach.tab");
  if (attCount != 0)
    attTitle += " (" + attCount + ")";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html id="top" xmlns="http://www.w3.org/1999/xhtml">

<head>
<view:looknfeel />
<title><fmt:message key="upload.title">
  <fmt:param>
    <wiki:Variable var="applicationname" />
  </fmt:param>
</fmt:message></title>
<wiki:Include page="commonheader.jsp" />
<meta name="robots" content="noindex,nofollow" />
</head>

<body>
<view:browseBar link="#" path="${pageName}" />
<view:window>
  <div id="wikibody" class="${prefs['orientation']}"><wiki:Include page="Header.jsp" />

  <div id="content">

  <div id="page"><%@ include file="PageActionsTop.jsp"%> <view:tabs>
    <c:set var="tabViewTitle"><%=LocaleSupport.getLocalizedMessage(pageContext,
        "view.tab")%></c:set>
    <c:set var="viewAction" value="<%=c.getURL(WikiContext.VIEW, c.getPage().getName())%>" />
    <view:tab label="${tabViewTitle}" action="${viewAction}" selected="false" />
    <wiki:PageExists>
      <c:set var="tabAttachTitle"><%=attTitle%></c:set>
      <c:set var="attachAction" value="<%=c.getURL(WikiContext.VIEW, c.getPage().getName())%>" />
      <view:tab label="${tabAttachTitle}" action="${attachAction}&attach=true" selected="true" />
      <c:set var="tabInfoTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "info.tab")%></c:set>
      <c:set var="infoAction" value="<%=c.getURL(WikiContext.INFO, c.getPage().getName())%>" />
      <view:tab label="${tabInfoTitle}" action="${infoAction}" selected="false" />
    </wiki:PageExists>
    <c:set var="tabHelpTitle"><%=LocaleSupport.getLocalizedMessage(pageContext,
                    "edit.tab.help")%></c:set>
    <c:set var="helpAction" value="<%=c.getURL(WikiContext.VIEW, "EditPageHelp")%>" />
    <view:tab label="${tabHelpTitle}" action="javascript: showHelp();" selected="false" />
  </view:tabs> <view:frame>
    <wiki:PageExists>
      <wiki:Include page="AttachmentTab.jsp" />
    </wiki:PageExists>
  </view:frame> <wiki:Include page="PageActionsBottom.jsp" /></div>
  <wiki:Include page="Favorites.jsp" />
  <div class="clearbox"></div>
  </div>
  <wiki:Include page="Footer.jsp" /></div>
</view:window>
</body>

</html>