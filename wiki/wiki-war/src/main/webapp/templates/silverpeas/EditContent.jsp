<%@ page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>
<%@ page import="com.ecyrd.jspwiki.*"%>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*"%>
<fmt:setLocale value="${userLanguage}" />
<fmt:setBundle basename="templates.default" />
<%
  WikiContext c = WikiContext.findContext(pageContext);
			int attCount = c.getEngine().getAttachmentManager()
					.listAttachments(c.getPage()).size();
			String attTitle = LocaleSupport.getLocalizedMessage(pageContext,
					"attach.tab");
			if (attCount != 0)
				attTitle += " (" + attCount + ")";
%>

<view:tabs>
  <c:set var="tabContentTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "edit.tab.edit")%></c:set>
  <view:tab label="${tabContentTitle}" action="javascript: hideHelp()" selected="true" />
  <wiki:PageExists>
    <c:set var="tabAttachTitle"><%=attTitle%></c:set>
    <c:set var="attachAction" value="<%=c.getURL(WikiContext.VIEW, c.getPage().getName())%>" />
    <view:tab label="${tabAttachTitle}" action="${attachAction}&attach=true" selected="false" />
    <c:set var="tabInfoTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "info.tab")%></c:set>
    <c:set var="infoAction" value="<%=c.getURL(WikiContext.INFO, c.getPage().getName())%>" />
    <view:tab label="${tabInfoTitle}" action="${infoAction}" selected="false" />
  </wiki:PageExists>
  <c:set var="tabHelpTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "edit.tab.help")%></c:set>
  <c:set var="helpAction" value="<%=c.getURL(WikiContext.VIEW, "EditPageHelp")%>" />
  <view:tab label="${tabHelpTitle}" action="javascript: showHelp();" selected="false" />
</view:tabs>
<view:frame>

  <div id="helpZone" style="display: none;"><wiki:InsertPage page="EditPageHelp" /> <wiki:NoSuchPage page="EditPageHelp">
    <div class="error"><fmt:message key="comment.edithelpmissing">
      <fmt:param>
        <wiki:EditLink page="EditPageHelp">EditPageHelp</wiki:EditLink>
      </fmt:param>
    </fmt:message></div>
  </wiki:NoSuchPage></div>

  <div id="editZone">
    <wiki:CheckLock mode="locked" id="lock">
      <div class="error">
        <fmt:message key="edit.locked">
          <fmt:param>
            <c:out value="${lock.locker}" />
          </fmt:param>
          <fmt:param>
            <c:out value="${lock.timeLeft}" />
          </fmt:param>
        </fmt:message>
      </div>
    </wiki:CheckLock>
    <wiki:CheckVersion mode="notlatest">
      <div class="warning">
        <fmt:message key="edit.restoring">
          <fmt:param>
            <wiki:PageVersion />
          </fmt:param>
        </fmt:message>
      </div>
  </wiki:CheckVersion> 
  <wiki:Editor />
  </div>
</view:frame>

<script language="javascript">
	function getStyleObject(objectId) {
		// cross-browser function to get an object's style object given its id
		if (document.getElementById && document.getElementById(objectId)) {
			// W3C DOM
			return document.getElementById(objectId).style;
		} else if (document.all && document.all(objectId)) {
			// MSIE 4 DOM
			return document.all(objectId).style;
		} else if (document.layers && document.layers[objectId]) {
			// NN 4 DOM.. note: this won't find nested layers
			return document.layers[objectId];
		} else {
			return false;
		}
	}

	function showHelp() {
		var helpStyle = getStyleObject('helpZone');
		var editStyle = getStyleObject('editZone');

		editStyle.display = 'none';
		helpStyle.display = 'block';
	}

	function hideHelp() {
		var helpStyle = getStyleObject('helpZone');
		var editStyle = getStyleObject('editZone');

		helpStyle.display = 'none';
		editStyle.display = 'block';
	}
</script>
