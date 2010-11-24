/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.portlets;

import java.io.IOException;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ValidatorException;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.kmelia.KmeliaTransversal;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import java.util.Calendar;

public class LastPublicationsPortlet extends GenericPortlet implements FormNames {

  @Override
  public void doView(RenderRequest request, RenderResponse response)
      throws PortletException, IOException {
    PortletSession session = request.getPortletSession();
    MainSessionController m_MainSessionCtrl = (MainSessionController) session
        .getAttribute("SilverSessionController", PortletSession.APPLICATION_SCOPE);

    String spaceId = (String) session.getAttribute(
        "Silverpeas_Portlet_SpaceId", PortletSession.APPLICATION_SCOPE);

    PortletPreferences pref = request.getPreferences();
    int nbPublis = 5;
    if(StringUtil.isInteger(pref.getValue("nbPublis", "5"))) {
      nbPublis = Integer.parseInt(pref.getValue("nbPublis", "5"));
    }
    int maxAge = 0;
    if(StringUtil.isInteger(pref.getValue("maxAge","0"))) {
      maxAge = Integer.parseInt(pref.getValue("maxAge","0"));
    }
    KmeliaTransversal kmeliaTransversal = new KmeliaTransversal(m_MainSessionCtrl);
    List<PublicationDetail> publications;
    if(maxAge > 0) {
      maxAge = -1 * maxAge;
      Calendar calend = Calendar.getInstance();
      calend.add(Calendar.DAY_OF_MONTH, maxAge);
      publications = kmeliaTransversal.getUpdatedPublications(spaceId, calend.getTime(), nbPublis);
    } else {
      publications = kmeliaTransversal.getPublications(spaceId, nbPublis);
    }
    request.setAttribute("Publications", publications);
    include(request, response, "portlet.jsp");
  }

  @Override
  public void doEdit(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "edit.jsp");
  }

  /** Include "help" JSP. */
  @Override
  public void doHelp(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "help.jsp");
  }

  /** Include a page. */
  private void include(RenderRequest request, RenderResponse response,
      String pageName) throws PortletException {
    response.setContentType(request.getResponseContentType());
    if (!StringUtil.isDefined(pageName)) {
      // assert
      throw new NullPointerException("null or empty page name");
    }
    try {
      PortletRequestDispatcher dispatcher = getPortletContext()
          .getRequestDispatcher("/portlets/jsp/lastPublications/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      throw new PortletException(ioe);
    }
  }

  /*
   * Process Action.
   */
  @Override
  public void processAction(ActionRequest request, ActionResponse response)
      throws PortletException {
    if (request.getParameter(SUBMIT_FINISHED) != null) {
      //
      // handle "finished" button on edit page
      // return to view mode
      //
      processEditFinishedAction(request, response);
    } else if (request.getParameter(SUBMIT_CANCEL) != null) {
      //
      // handle "cancel" button on edit page
      // return to view mode
      //
      processEditCancelAction(request, response);
    }
  }

  /*
   * Process the "cancel" action for the edit page.
   */
  private void processEditCancelAction(ActionRequest request,
      ActionResponse response) throws PortletException {
    response.setPortletMode(PortletMode.VIEW);
  }

  /*
   * Process the "finished" action for the edit page. Set the "url" to the value specified in the
   * edit page.
   */
  private void processEditFinishedAction(ActionRequest request,
      ActionResponse response) throws PortletException {
    String nbPublis = request.getParameter(TEXTBOX_NB_ITEMS);
    String maxAge = request.getParameter(TEXTBOX_MAX_AGE);
    String displayDescription = request.getParameter("displayDescription");

    // Check if it is a number
    try {
      int nb = Integer.parseInt(nbPublis);
      Integer.parseInt(maxAge);

      if (nb < 0 || nb > 30) {
        throw new NumberFormatException();
      }

      // store preference
      PortletPreferences pref = request.getPreferences();
      try {
        pref.setValue("nbPublis", nbPublis);
        pref.setValue("maxAge", maxAge);
        pref.setValue("displayDescription", displayDescription);
        pref.store();
      } catch (ValidatorException ve) {
        getPortletContext().log("could not set nbPublis", ve);
        throw new PortletException("IFramePortlet.processEditFinishedAction", ve);
      } catch (IOException ioe) {
        getPortletContext().log("could not set nbPublis", ioe);
        throw new PortletException("IFramePortlet.prcoessEditFinishedAction", ioe);
      }
      response.setPortletMode(PortletMode.VIEW);

    } catch (NumberFormatException e) {
      response.setRenderParameter(ERROR_BAD_VALUE, "true");
      response.setPortletMode(PortletMode.EDIT);
    }
  }
}
