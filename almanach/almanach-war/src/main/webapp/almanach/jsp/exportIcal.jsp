<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<fmt:message var="status" key="${requestScope.messageKey}"/>
<c:set var="icsName" value="${requestScope.icsName}"/>
<c:set var="icsURL" value="${requestScope.icsURL}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <view:looknfeel />
  </head>
  <body>
  	<view:browseBar clickable="false"/>
    <view:window>
      <view:frame>
      	<c:if test="${icsName ne null and fn:length(icsName) > 0}">
      		<div class="inlineMessage-ok">
      			<b><c:out value="${status}"/></b><br/><br/>
      			<a href="<c:out value='${icsURL}'/>"><c:out value="${icsName}"/></a>
      		</div>	
      	</c:if>
      	<c:if test="${icsName eq null or fn:length(icsName) == 0}">
      		<div class="inlineMessage-nok">
      			<b><c:out value="${status}"/></b>
      		</div>	
      	</c:if>
      </view:frame>
    </view:window>
  </body>
</html>