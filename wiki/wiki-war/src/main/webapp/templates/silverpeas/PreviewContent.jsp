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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.ui.EditorManager" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<fmt:setLocale value="${userLanguage}"/>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<view:setBundle basename="templates.default"/>
<%-- Inserts page content for preview. --%>
<wiki:TabbedSection>
<wiki:Tab id="previewcontent" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "preview.tab")%>'>

  <div class="information">
    <fmt:message key="preview.info"/>
    <wiki:Editor/>
  </div>

  <div class="previewcontent">
    <wiki:Translate><%=EditorManager.getEditedText(pageContext)%></wiki:Translate>
  </div>

  <div class="information">
    <fmt:message key="preview.info"/>
  </div>

</wiki:Tab>
</wiki:TabbedSection>