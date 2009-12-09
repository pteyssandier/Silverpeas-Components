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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.ecyrd.jspwiki;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import org.apache.log4j.Logger;

import com.ecyrd.jspwiki.url.DefaultURLConstructor;

/**
 * This provides a master servlet for dealing with short urls. It mostly does redirects to the
 * proper JSP pages. It also intercepts the servlet shutdown events and uses it to signal wiki
 * shutdown.
 * @author Andrew Jaquith
 * @since 2.2
 */
public class WikiServlet extends HttpServlet {
  private static final long serialVersionUID = 3258410651167633973L;
  private WikiEngine m_engine;
  static final Logger log = Logger.getLogger(WikiServlet.class.getName());

  /**
   * {@inheritDoc}
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    m_engine = WikiEngine.getInstance(config);

    log.info("WikiServlet initialized.");
  }

  /**
   * Destroys the WikiServlet; called by the servlet container when shutting down the webapp. This
   * method calls the protected method {@link WikiEngine#shutdown()}, which sends
   * {@link com.ecyrd.jspwiki.event.WikiEngineEvent#SHUTDOWN} events to registered listeners.
   * @see javax.servlet.GenericServlet#destroy()
   */
  public void destroy() {
    log.info("WikiServlet shutdown.");
    m_engine.shutdown();
    super.destroy();
  }

  /**
   * {@inheritDoc}
   */
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {
    doGet(req, res);
  }

  /**
   * {@inheritDoc}
   */
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {
    String pageName = DefaultURLConstructor.parsePageFromURL(req, m_engine
        .getContentEncoding());

    log.info("Request for page: " + pageName);

    if (pageName == null)
      pageName = m_engine.getFrontPage(); // FIXME: Add special pages as well

    String jspPage = m_engine.getURLConstructor().getForwardPage(req);

    RequestDispatcher dispatcher = req
        .getRequestDispatcher("/" + jspPage + "?page="
        + m_engine.encodeName(pageName) + "&" + req.getQueryString());

    dispatcher.forward(req, res);
  }
}
