/**
 * User: Aliaksei_Budnikau
 * Date: Nov 26, 2002
 */
package com.stratelia.webactiv.quickinfo;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;

public class QuickInfoContentManager implements ContentInterface {

  private ContentManager contentManager;
  private PublicationBm currentPublicationBm;
  public final static String CONTENT_ICON = "quickinfoSmall.gif";

  /**
   * Find all the SilverContent with the given list of SilverContentId
   * 
   * @param ids
   *          list of silverContentId to retrieve
   * @param peasId
   *          the id of the instance
   * @param userId
   *          the id of the user who wants to retrieve silverContent
   * @param userRoles
   *          the roles of the user
   * @return a List of SilverContent
   */
  public List getSilverContentById(List ids, String peasId, String userId,
      List userRoles) {
    if (getContentManager() == null)
      return new ArrayList();

    return getHeaders(makePKArray(ids, peasId));
  }

  public int getSilverObjectId(String pubId, String peasId) {
    SilverTrace.info("quickinfo",
        "QuickInfoContentManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
    try {
      return getContentManager().getSilverContentId(pubId, peasId);
    } catch (Exception e) {
      // throw new
      // KmeliaRuntimeException("KmeliaContentManager.getSilverObjectId()",SilverpeasRuntimeException.ERROR,"kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID",
      // e);
      return 0;
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   * 
   * @param con
   *          a Connection
   * @param pubDetail
   *          the content to register
   * @param userId
   *          the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, PublicationDetail pubDetail,
      String userId, boolean isVisible) throws ContentManagerException {
    SilverTrace.info("quickinfo",
        "QuickInfoContentManager.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", String.valueOf(pubDetail));
    SilverContentVisibility scv = new SilverContentVisibility(pubDetail
        .getBeginDate(), pubDetail.getEndDate(), isVisible);
    return getContentManager().addSilverContent(con, pubDetail.getPK().getId(),
        pubDetail.getPK().getComponentName(), userId, scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content
   * is a PublicationDetail
   * 
   * @param pubDetail
   *          the content
   */
  public void updateSilverContentVisibility(PublicationDetail pubDetail,
      boolean isVisible) throws ContentManagerException {
    int silverContentId = getContentManager().getSilverContentId(
        pubDetail.getPK().getId(), pubDetail.getPK().getComponentName());
    if (silverContentId != -1) {
      SilverContentVisibility scv = new SilverContentVisibility(pubDetail
          .getBeginDate(), pubDetail.getEndDate(), isVisible);
      SilverTrace.info("quickinfo",
          "QuickInfoContentManager.updateSilverContentVisibility()",
          "root.MSG_GEN_ENTER_METHOD", String.valueOf(scv));
      getContentManager().updateSilverContentVisibilityAttributes(scv,
          pubDetail.getPK().getComponentName(), silverContentId);
      ClassifyEngine.clearCache();
    } else {
      createSilverContent(null, pubDetail, pubDetail.getCreatorId(), isVisible);
    }
  }

  /**
   * delete a content. It is registered to contentManager service
   * 
   * @param con
   *          a Connection
   * @param pubPK
   *          the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, PublicationPK pubPK)
      throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(pubPK.getId(),
        pubPK.getComponentName());
    if (contentId != -1) {
      SilverTrace.info("quickinfo",
          "QuickInfoContentManager.deleteSilverContent()",
          "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId()
              + ", contentId = " + contentId);
      getContentManager().removeSilverContent(con, contentId,
          pubPK.getComponentName());
    }
  }

  /**
   * return a list of publicationPK according to a list of silverContentId
   * 
   * @param idList
   *          a list of silverContentId
   * @param peasId
   *          the id of the instance
   * @return a list of publicationPK
   */
  private ArrayList makePKArray(List idList, String peasId) {
    ArrayList pks = new ArrayList();
    PublicationPK pubPK = null;
    Iterator iter = idList.iterator();
    String id = null;
    // for each silverContentId, we get the corresponding publicationId
    while (iter.hasNext()) {
      int contentId = ((Integer) iter.next()).intValue();
      try {
        id = getContentManager().getInternalContentId(contentId);
        pubPK = new PublicationPK(id, "useless", peasId);
        pks.add(pubPK);
      } catch (ClassCastException ignored) {
        ;// ignore unknown item
      } catch (ContentManagerException ignored) {
        ;// ignore unknown item
      }
    }
    return pks;
  }

  /**
   * return a list of silverContent according to a list of publicationPK
   * 
   * @param ids
   *          a list of publicationPK
   * @return a list of publicationDetail
   */
  private List getHeaders(List ids) {
    PublicationDetail pubDetail = null;
    ArrayList headers = new ArrayList();
    try {
      ArrayList publicationDetails = (ArrayList) getPublicationBm()
          .getPublications(ids);
      for (int i = 0; i < publicationDetails.size(); i++) {
        pubDetail = (PublicationDetail) publicationDetails.get(i);
        pubDetail.setIconUrl(CONTENT_ICON);
        headers.add(pubDetail);
      }
    } catch (RemoteException e) {
      ;// skip unknown and ill formed id.
    }
    return headers;
  }

  private PublicationBm getPublicationBm() {
    if (currentPublicationBm == null) {
      try {
        PublicationBmHome publicationBmHome = (PublicationBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
                PublicationBmHome.class);
        currentPublicationBm = publicationBmHome.create();
      } catch (Exception e) {
        throw new PublicationRuntimeException(
            "QuickInfoContentManager.getPublicationBm()",
            PublicationRuntimeException.ERROR,
            "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return currentPublicationBm;
  }

  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace.fatal("quickinfo",
            "QuickInfoContentManager.getContentManager()",
            "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

}
