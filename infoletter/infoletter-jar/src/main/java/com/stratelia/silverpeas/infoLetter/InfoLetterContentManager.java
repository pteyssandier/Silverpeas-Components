package com.stratelia.silverpeas.infoLetter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.infoLetter.control.ServiceFactory;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterDataInterface;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublicationPdC;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * The infoletter implementation of ContentInterface.
 */
public class InfoLetterContentManager implements ContentInterface
{
   /**
	* Find all the SilverContent with the given list of SilverContentId
	* @param ids list of silverContentId to retrieve
	* @param peasId the id of the instance
	* @param userId the id of the user who wants to retrieve silverContent
	* @param userRoles the roles of the user
	* @return a List of SilverContent
	*/
   public List getSilverContentById(List ids, String peasId, String userId, List userRoles)
   {
	   if (getContentManager() == null) return new ArrayList();

	   return getHeaders(makePKArray(ids), peasId);
   }

   public int getSilverObjectId(String pubId, String peasId) {
		SilverTrace.info("infoletter","InfoLetterContentManager.getSilverObjectId()", "root.MSG_GEN_ENTER_METHOD", "pubId = "+pubId);
		try {
			return getContentManager().getSilverContentId(pubId, peasId);
		} catch (Exception e) {
			throw new InfoLetterException("InfoLetterContentManager.getSilverObjectId()",SilverpeasException.ERROR,"infoletter.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
		}
	}

	/**
	* add a new content. It is registered to contentManager service
	* @param con a Connection
	* @param ilPub the content to register
	* @param userId the creator of the content
	* @return the unique silverObjectId which identified the new content
	*/
   public int createSilverContent(Connection con, InfoLetterPublicationPdC ilPub, String userId) throws ContentManagerException
   {
	  SilverContentVisibility scv = new SilverContentVisibility(isVisible(ilPub));
	  SilverTrace.info("infoletter","InfoLetterContentManager.createSilverContent()", "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "+scv.toString());
	  return getContentManager().addSilverContent(con, ilPub.getId(), ilPub.getInstanceId(), userId, scv);
   }

   /**
	* update the visibility attributes of the content. Here, the type of content is a PublicationDetail
	* @param ilPub the content
	* @param silverObjectId the unique identifier of the content
	*/
   public void updateSilverContentVisibility(InfoLetterPublicationPdC ilPub) throws ContentManagerException
   {
	  int silverContentId = getContentManager().getSilverContentId(ilPub.getId(), ilPub.getInstanceId());
	  SilverContentVisibility scv = new SilverContentVisibility(isVisible(ilPub));
	  SilverTrace.info("infoletter","InfoLetterContentManager.updateSilverContentVisibility()", "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "+scv.toString());
	  if (silverContentId == -1) {
			createSilverContent(null, ilPub, ilPub.getCreatorId());
	  } else {
			getContentManager().updateSilverContentVisibilityAttributes(scv, ilPub.getInstanceId(), silverContentId);
			ClassifyEngine.clearCache();
	  }
	  
   }

   /**
	* delete a content. It is registered to contentManager service
	* @param con a Connection
	* @param pubId the identifiant of the content to unregister
	* @param componentId the identifiant of the component instance where the content to unregister is
	*/
   public void deleteSilverContent(Connection con, String pubId, String componentId) throws ContentManagerException
   {
	  int contentId = getContentManager().getSilverContentId(pubId, componentId);
	  if (contentId != -1)
	  {
			SilverTrace.info("infoletter","InfoLetterContentManager.deleteSilverContent()", "root.MSG_GEN_ENTER_METHOD", "pubId = "+pubId+", contentId = "+contentId);
			getContentManager().removeSilverContent(con, contentId, componentId);
	  }
   }

   private boolean isVisible(InfoLetterPublicationPdC ilPub) 
   {	
		return (ilPub.getPublicationState() == 2);
   }

   /**
	* return a list of ids according to a list of silverContentId
	* @param idList a list of silverContentId
	* @return a list of ids
	*/
   private ArrayList makePKArray(List idList)
   {
      Iterator		iter	= idList.iterator();
	  String		id		= null;
	  ArrayList		pks     = new ArrayList();

	  //for each silverContentId, we get the corresponding infoLetterPublicationId
      while (iter.hasNext())
      {
		    int contentId = ((Integer) iter.next()).intValue();
			try
			{
				id = getContentManager().getInternalContentId(contentId);
				pks.add(id);
			}
			catch (ClassCastException ignored)
			{
			   // ignore unknown item
			}
			catch (ContentManagerException ignored)
			{
			   // ignore unknown item
			}
      }
	  return pks;
   }   

	/**
	* return a list of silverContent according to a list of publicationPK
	* @param ids a list of publicationPK
	* @param peasId the id of the instance
	* @return a list of publicationDetail
	*/
	private List getHeaders(List ids, String peasId)
	{
		Iterator iter					= ids.iterator();
		ArrayList headers				= new ArrayList();
		InfoLetterPublicationPdC ilPub		= null;
		String pubId					= null;
        IdPK pubPK						= null;

		while (iter.hasNext())
		{
			pubId = (String) iter.next();
			pubPK = new IdPK();
            pubPK.setId(pubId);

			ilPub = getDataInterface().getInfoLetterPublication(pubPK);
			ilPub.setInstanceId(peasId);
			headers.add(ilPub);
		}
		
		return headers;
	}

	private ContentManager getContentManager()
	{
	   if (contentManager == null)
		{
		   try
			{
		      contentManager = new ContentManager();
			}
			catch (Exception e)
			{
			    SilverTrace.fatal("infoletter", "InfoLetterContentManager", "root.EX_UNKNOWN_CONTENT_MANAGER", e);
			}
		}
		return contentManager;
	}

	private InfoLetterDataInterface getDataInterface()
	{
		if (dataInterface == null)
		{
			dataInterface = ServiceFactory.getInfoLetterData();
		}
		return dataInterface;
	}

	private ContentManager	contentManager			= null;
	private InfoLetterDataInterface dataInterface	= null;
}