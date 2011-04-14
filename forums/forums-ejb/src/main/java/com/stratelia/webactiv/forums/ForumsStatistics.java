/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.forums;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.EJBException;

import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.webactiv.forums.models.ForumPK;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBM;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBMHome;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;

/*
 * CVS Informations
 *
 * $Id: ForumsStatistics.java,v 1.3 2009/03/31 08:10:57 neysseri Exp $
 *
 * $Log: ForumsStatistics.java,v $
 * Revision 1.3  2009/03/31 08:10:57  neysseri
 * eShop : Livraison 2
 *
 * Revision 1.2  2004/06/22 16:26:10  neysseri
 * implements new SilverContentInterface + nettoyage eclipse
 *
 * Revision 1.1.1.1  2002/08/06 14:47:57  nchaix
 * no message
 *
 * Revision 1.1  2002/04/17 17:20:34  mguillem
 * alimentation des stats de volume
 *
 * Revision 1.1  2002/04/05 16:58:24  mguillem
 * alimentation des stats de volume
 *
 */

/**
 * Class declaration
 * @author
 */
public class ForumsStatistics implements ComponentStatisticsInterface {

  private ForumsBM forumsBM = null;

  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList couples = new ArrayList();
    ArrayList forums = getForums(spaceId, componentId);
    Forum forum;
    for (int i = 0, n = forums.size(); i < n; i++) {
      forum = (Forum) forums.get(i);
      UserIdCountVolumeCouple couple = new UserIdCountVolumeCouple();
      couple.setUserId(Integer.toString(forum.getId()));
      couple.setCountVolume(1);
      couples.add(couple);
    }
    return couples;
  }

  private ForumsBM getForumsBM() {
    if (forumsBM == null) {
      try {
        ForumsBMHome forumsBMHome = (ForumsBMHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.FORUMSBM_EJBHOME, ForumsBMHome.class);
        forumsBM = forumsBMHome.create();
      } catch (Exception e) {
        throw new EJBException(e);
      }
    }
    return forumsBM;
  }

  public ArrayList getForums(String spaceId, String componentId)
      throws RemoteException {
    return getForumsBM().getForums(new ForumPK(componentId, spaceId));
  }

}
