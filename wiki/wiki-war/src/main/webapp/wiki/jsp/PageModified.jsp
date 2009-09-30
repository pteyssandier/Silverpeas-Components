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
<%@ page isELIgnored ="false" %> 
<%@ page import="org.apache.log4j.*" %>
<%@ page import="org.apache.commons.lang.*" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.ui.EditorManager" %>
<%@ page errorPage="Error.jsp" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

<%! 
    Logger log = Logger.getLogger("JSPWiki");
%>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context and check for authorization
    WikiContext wikiContext = wiki.createContext( request, WikiContext.CONFLICT );
    if(!wikiContext.hasAccess( response )) return;
    String pagereq = wikiContext.getName();

    String usertext = (String)session.getAttribute( EditorManager.REQ_EDITEDTEXT );

    // Make the user and conflicting text presentable for display.
    usertext = StringEscapeUtils.escapeXml( usertext );
    usertext = TextUtil.replaceString( usertext, "\n", "<br />" );

    String conflicttext = wiki.getText(pagereq);
    conflicttext = StringEscapeUtils.escapeXml( conflicttext );
    conflicttext = TextUtil.replaceString( conflicttext, "\n", "<br />" );

    pageContext.setAttribute( "conflicttext",
                              conflicttext,
                              PageContext.REQUEST_SCOPE );

    log.info("Page concurrently modified "+pagereq);
    pageContext.setAttribute( "usertext",
                              usertext,
                              PageContext.REQUEST_SCOPE );

    // Set the content type and include the response content
    response.setContentType("text/html; charset="+wiki.getContentEncoding() );
    String contentPage = wiki.getTemplateManager().findJSP( pageContext,
                                                            wikiContext.getTemplate(),
                                                            "ViewTemplate.jsp" );
%><wiki:Include page="<%=contentPage%>" />

