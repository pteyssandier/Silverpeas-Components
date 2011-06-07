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

package com.silverpeas.scheduleevent.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.scheduleevent.control.ScheduleEventSessionController;
import com.silverpeas.scheduleevent.view.ScheduleEventVO;

public class ScheduleEventDeleteDateRequestHandler extends ScheduleEventActionDateRequestHandler {

  private ScheduleEventRequestHandler forwardRequestHandler = null;

  public void setForwardRequestHandler(ScheduleEventRequestHandler forwardRequestHandler) {
    this.forwardRequestHandler = forwardRequestHandler;
  }

  @Override
  public String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception {
    if (forwardRequestHandler != null) {
      return deleteSelectedDateAndForwardRequestHandler(function, scheduleeventSC, request);
    } else {
      throw UndefinedForwardRequestHandlerException();
    }
  }

  private String deleteSelectedDateAndForwardRequestHandler(String function,
      ScheduleEventSessionController scheduleeventSC, HttpServletRequest request) throws Exception {
    removeDateOptions(scheduleeventSC, request);
    return forwardRequestHandler.getDestination(function, scheduleeventSC, request);
  }

  private void removeDateOptions(ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception {
    ScheduleEventVO current = (ScheduleEventVO) scheduleeventSC.getCurrentScheduleEventVO();
    current.deleteDate(request.getParameter("dateToDelete"));
  }

  private Exception UndefinedForwardRequestHandlerException() {
    return new Exception(
        "No forward request defines for" + this.getClass());
  }
}
