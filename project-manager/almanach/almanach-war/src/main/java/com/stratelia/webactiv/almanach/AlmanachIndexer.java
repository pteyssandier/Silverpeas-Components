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
package com.stratelia.webactiv.almanach;

//import com.stratelia.webactiv.beans.admin.*;
import com.stratelia.webactiv.almanach.control.AlmanachSessionController;
import com.stratelia.webactiv.applicationIndexer.control.*;
import com.stratelia.webactiv.almanach.control.*;
import com.stratelia.webactiv.almanach.model.*;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import java.util.Iterator;
import com.stratelia.silverpeas.silvertrace.*;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;

public class AlmanachIndexer implements ComponentIndexerInterface {

  private AlmanachSessionController scc = null;

  public void index(MainSessionController mainSessionCtrl,
      ComponentContext context) throws Exception {

    scc = new AlmanachSessionController(mainSessionCtrl, context);

    indexEvents();
  }

  private void indexEvents() throws Exception {
    SilverTrace.info("almanach", "AlmanachIndexer.indexEvents()",
        "root.MSG_GEN_ENTER_METHOD");

    Iterator it = scc.getAllEvents().iterator();
    while (it.hasNext()) {
      EventDetail event = (EventDetail) (it.next());

      // index event itself
      scc.indexEvent(event);

      // index possible attachments to the event
      AttachmentController.attachmentIndexer(event.getPK());
    }
  }
}