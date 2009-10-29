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
package com.silverpeas.blog.control.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.blog.BlogContentManager;
import com.silverpeas.blog.dao.PostDAO;
import com.silverpeas.blog.model.Archive;
import com.silverpeas.blog.model.BlogRuntimeException;
import com.silverpeas.blog.model.Category;
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.comment.control.CommentController;
import com.stratelia.silverpeas.comment.ejb.CommentBm;
import com.stratelia.silverpeas.comment.ejb.CommentBmHome;
import com.stratelia.silverpeas.comment.ejb.CommentRuntimeException;
import com.stratelia.silverpeas.comment.model.CommentPK;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBmHome;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBm;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBmHome;

/**
 * @author
 */
 public class BlogBmEJB implements SessionBean
 {

	 static SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

	 public Date getDateEvent(String pubId)
	 {
		 Connection con = initCon();
		 try
		 {
			 return PostDAO.getDateEvent(con, pubId);
		 }
		 catch (Exception e)
		 {
			throw new BlogRuntimeException("BlogBmEJB.createPost()",SilverpeasRuntimeException.ERROR,"post.MSG_POST_NOT_CREATE", e);
		 }
		 finally
		 {
			 fermerCon(con);
		 }
	 }

	 public String createPost(PostDetail post)
	 {
		 Connection con = initCon();
		 try
		{
			 //Création de la publication
		 	PublicationDetail pub = post.getPublication();
		 	pub.setStatus("Valid");
		 	PublicationPK pk = getPublicationBm().createPublication(pub);

		 	// création de la date d'évènement
		 	PostDAO.createDateEvent(con, pk.getId(), post.getDateEvent(), pk.getInstanceId());

		 	//Affectation de la publi à une categorie
		 	if (!(post.getCategoryId().equals("")) && (post.getCategoryId() != null) && (!post.getCategoryId().equals("null")))
		 		setCategory(pk, post.getCategoryId());

		 	// création du silverContent
		 	createSilverContent(con, pub, pub.getCreatorId());

		 	// envoie notification si abonnement
		 	sendSubscriptionsNotification(new NodePK("0", pub.getPK().getSpaceId(), pub.getPK().getInstanceId()), pub, "create");

			return pk.getId();

		}
		catch (Exception e)
		{
			throw new BlogRuntimeException("BlogBmEJB.createPost()",SilverpeasRuntimeException.ERROR,"post.MSG_POST_NOT_CREATE", e);
		}
		finally
		{
			fermerCon(con);
		}
	 }

	 public void sendSubscriptionsNotification(NodePK fatherPK, PublicationDetail pubDetail, String type)
	 {
			//send email alerts
			try {
				ArrayList<NodePK> descendantPKs = new ArrayList<NodePK>();
				descendantPKs.add(fatherPK);

				Collection subscriberIds = getSubscribeBm().getNodeSubscriberDetails(descendantPKs);

				OrganizationController orgaController = new OrganizationController();
				if (subscriberIds != null && subscriberIds.size() > 0)
				{
					//get only subscribers who have sufficient rights to read pubDetail
					Iterator it = subscriberIds.iterator();
					String userId = null;
					NodeDetail node = getNodeBm().getHeader(fatherPK);
					List newSubscribers = new ArrayList();
					while (it.hasNext())
					{
						userId = (String) it.next();

						if (orgaController.isComponentAvailable(fatherPK.getInstanceId(), userId))
						{
							if (!node.haveRights() || orgaController.isObjectAvailable(node.getRightsDependsOn(), ObjectType.NODE, fatherPK.getInstanceId(), userId))
								newSubscribers.add(userId);
						}
					}

					if (newSubscribers.size() > 0)
					{
						ResourceLocator	message		= new ResourceLocator("com.silverpeas.blog.multilang.blogBundle", "fr");
						ResourceLocator	message_en	= new ResourceLocator("com.silverpeas.blog.multilang.blogBundle", "en");

						//french notifications
						String subject 	= message.getString("blog.subjectSubscription");
						String body 	= getSubscriptionsNotificationBody(pubDetail, message, type);

						//english notifications
						String subject_en 	= message_en.getString("blog.subjectSubscription");
						String body_en 		= getSubscriptionsNotificationBody(pubDetail, message_en, type);

						NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL, subject, body);
						notifMetaData.addLanguage("en", subject_en, body_en);

						notifMetaData.setUserRecipients(new Vector(newSubscribers));
						notifMetaData.setLink(getPostUrl(pubDetail));
						notifMetaData.setComponentId(fatherPK.getInstanceId());
						notifyUsers(notifMetaData, pubDetail.getUpdaterId());
					}
				}
			} catch (Exception e) {
				SilverTrace.warn("blog","BlogBmEJB.sendSubscriptionsNotification()", "kmelia.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS","fatherId = "+fatherPK.getId()+", pubId = "+pubDetail.getPK().getId(),e);
			}
		}

	 public static String getPostUrl(PublicationDetail pubDetail)
	 {
		 return "/Rblog/" + pubDetail.getPK().getInstanceId() + "/searchResult?Type=Publication&Id=" + pubDetail.getPK().getId();
	 }

	 private void notifyUsers(NotificationMetaData notifMetaData, String senderId)
	 {
		 Connection con = null;
		 try
		 {
			 con = initCon();
			 notifMetaData.setConnection(con);
			 if (notifMetaData.getSender() == null || notifMetaData.getSender().length() == 0)
				 notifMetaData.setSender(senderId);
			 NotificationSender notifSender = new NotificationSender(notifMetaData.getComponentId());
			 notifSender.notifyUser(notifMetaData);
		 }
		 catch (NotificationManagerException e)
		 {
			 SilverTrace.warn("kmelia","KmeliaBmEJB.notifyUsers()", "kmelia.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS",e);
		 } finally {
			 fermerCon(con);
		 }
	 }

	 private String getSubscriptionsNotificationBody(PublicationDetail pubDetail, ResourceLocator message, String type)
	 {
		 StringBuffer messageText = new StringBuffer();
		 if (type.equals("create"))
		 {
			 messageText.append(message.getString("blog.bodySubscriptionCreate"));
		 }
		 else if (type.equals("update"))
		 {
			 messageText.append(message.getString("blog.bodySubscriptionUpdate"));
		 }
		 else if (type.equals("commentCreate"))
		 {
			 messageText.append(message.getString("blog.bodySubscriptionCommentCreate"));
		 }
		 else if (type.equals("commentUpdate"))
		 {
			 messageText.append(message.getString("blog.bodySubscriptionCommentUpdate"));
		 }
		 messageText.append(" : ").append(pubDetail.getName(message.getLanguage())).append("\n");
		 return messageText.toString();
	 }

	 public void updatePost(PostDetail post)
	 {
		 Connection con = initCon();
		 try
		{
			 //Suppression de l'ancienne category
			 getPublicationBm().removeAllFather(post.getPublication().getPK());

			 //Modification de la publi
			 getPublicationBm().setDetail(post.getPublication());

			 //Ajout de la nouvelle category
			 if (!(post.getCategoryId().equals("")) && (post.getCategoryId() != null) && (!post.getCategoryId().equals("null")))
				 setCategory(post.getPublication().getPK(), post.getCategoryId());

			 // modification de la date d'évènement
			 PostDAO.updateDateEvent(con, post.getPublication().getPK().getId(), post.getDateEvent());

		 	// envoie notification si abonnement
			 PublicationDetail pub = post.getPublication();
		 	sendSubscriptionsNotification(new NodePK("0", pub.getPK().getSpaceId(), pub.getPK().getInstanceId()), pub, "update");

		}
		catch (Exception e)
		{
			throw new BlogRuntimeException("BlogBmEJB.updatePost()",SilverpeasRuntimeException.ERROR,"post.MSG_POST_NOT_UPDATE", e);
		}
		finally
		{
			fermerCon(con);
		}
	 }

	 private void setCategory(PublicationPK pk, String categoryId)
	 {
		 NodePK nodePK = new NodePK(categoryId, pk.getInstanceId());
 		 try
 		 {
 			 getPublicationBm().addFather(pk, nodePK);
		 }
 		 catch (RemoteException e)
 		 {
			throw new BlogRuntimeException("BlogBmEJB.setCategory()",SilverpeasRuntimeException.ERROR,"post.MSG_CANT_SET_CATEGORY", e);
		}
	 }

	 public void deletePost(String postId, String instanceId)
	 {
		 Connection con = initCon();
		 try
		{
			 PublicationPK pubPK = new PublicationPK(postId, instanceId);

			 //Supprime la liaison avec la categorie
			 getPublicationBm().removeAllFather(pubPK);

			 // supprimer la date d'evenement
			 PostDAO.deleteDateEvent(con, pubPK.getId());

			 //Supprime les commentaires
			 ForeignPK foreignPK = new ForeignPK(postId, instanceId);
			 getCommentBm().deleteAllComments(foreignPK);

			 //Supprime le contenu Wysiwyg
			 WysiwygController.deleteFileAndAttachment(instanceId, postId);

			 //Supprime la publication
			 getPublicationBm().removePublication(pubPK);

			 // supprimer le silverContent
			 getBlogContentManager().deleteSilverContent(con, pubPK);
		}
		catch (Exception e)
		{
			throw new BlogRuntimeException("BlogBmEJB.deletePost()",SilverpeasRuntimeException.ERROR,"post.MSG_POST_NOT_DELETE", e);
		}
		finally
		{
			// fermer la connexion
			fermerCon(con);
		}
	 }

	 private Connection initCon()
	 {
		Connection con;
		// initialisation de la connexion
		try
		{
			con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
		}
		catch (UtilException e)
		{
			// traitement des exceptions
			throw new BlogRuntimeException("blogBmEJB.initCon()", SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
		}
		return con;
	 }

	 private void fermerCon(Connection con)
	 {
		try
		{
			con.close();
		}
		catch (SQLException e)
		{
			// traitement des exceptions
			throw new BlogRuntimeException("GalleryBmEJB.fermerCon()", SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
		}
	 }

	 public PostDetail getPost(PublicationPK pk)
	 {

		 PublicationDetail pub;
		 try {
			 pub = getPublicationBm().getDetail(pk);
		 } catch (RemoteException e) {
			 throw new BlogRuntimeException("BlogBmEJB.getPost()", SilverpeasRuntimeException.ERROR, "root.EX_RECORD_NOT_FOUND", "pk = "+pk.toString(), e);
		 }
		 return getPost(pub, new OrganizationController());
	 }

	 private PostDetail getPost(PublicationDetail pub, OrganizationController orga)
	 {
		 try
		 {
			Collection<NodePK> allCat = getPublicationBm().getAllFatherPK(pub.getPK());
			// la collection des catégories contient en fait une seule catégorie, la récupérer
			Category cat = null;
			if (!allCat.isEmpty())
			{
				Iterator<NodePK> it = (Iterator<NodePK>) allCat.iterator();
				NodePK nodePK = (NodePK) it.next();
				cat = (Category) getCategory(nodePK);
			}
			// rechercher le nombre de commentaire
			CommentPK foreign_pk = new CommentPK(pub.getPK().getId(), null, pub.getPK().getInstanceId());
			Collection comments =  getCommentBm().getAllComments(foreign_pk);

			// recherche de la date d'evenement
			Connection con = initCon();
			Date dateEvent;
			try
			{
				dateEvent = PostDAO.getDateEvent(con, pub.getPK().getId());
			}
			finally
			{
				fermerCon(con);
			}

			PostDetail post = new PostDetail(pub, cat, comments.size(), dateEvent);
			post.setCreatorName(orga.getUserDetail(pub.getCreatorId()).getDisplayedName());

			return post;
		}
		catch (Exception e)
		{
			throw new BlogRuntimeException("BlogBmEJB.getPost()",SilverpeasRuntimeException.ERROR,"post.MSG_POST_NOT_CREATE", e);
		}
	 }

	 public Collection<PostDetail> getAllPosts(String instanceId, int nbReturned)
	 {
		 PublicationPK pubPK = new PublicationPK("useless", instanceId);
		 Connection con = initCon();

		 Collection<PostDetail> posts = new ArrayList<PostDetail>();
		 try
		 {
			 // rechercher les publications classée par date d'évènement
			 Collection<String> lastEvents = PostDAO.getLastEvents(con, instanceId, nbReturned);

			 Collection<PublicationDetail> publications = getPublicationBm().getPublicationsByStatus("Valid", pubPK);
			 OrganizationController orgaController = new OrganizationController();

			 Iterator<String> itEvent = lastEvents.iterator();
			 while (itEvent.hasNext())
			 {
				 String pubId = (String) itEvent.next();
		    	 // pour cette publication, rechercher son détail et créer le post correspondant
		    	 Iterator<PublicationDetail> it = (Iterator<PublicationDetail>) publications.iterator();
		    	 while (it.hasNext())
		    	 {
		    		 PublicationDetail pub = (PublicationDetail) it.next();
		    		 if (pub.getPK().getId().equals(pubId))
		    			 posts.add(getPost(pub, orgaController));
		    	 }
			 }
	    	 return posts;
		 }
		 catch (Exception e)
		 {
			 throw new BlogRuntimeException("BlogBmEJB.getAllPosts()",SilverpeasRuntimeException.ERROR,"post.MSG_POST_NOT_CREATE", e);
		 }
		 finally
		 {
			fermerCon(con);
		 }
	 }
	 public Collection<PostDetail> getLastPosts(String instanceId)
	 {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		Date beginDate = getMonthFirstDay(calendar);
		Date endDate = getMonthLastDay(calendar);
		return getPostsByArchive(DateUtil.date2SQLDate(beginDate), DateUtil.date2SQLDate(endDate), instanceId);
	 }

	 public Date getOldestPost(String instanceId)
	 {
		 Connection con = initCon();
		 PublicationPK pubPK = new PublicationPK("useless", instanceId);
		 Date today = new Date();
		 Calendar calendar = Calendar.getInstance(Locale.FRENCH);
		 calendar.setTime(today);
		 Date date = today;
		 try
		 {
			 // rechercher dans les dates d'évenements
			 String pubId = PostDAO.getOldestEvent(con, instanceId);

			 Collection<PublicationDetail> publications = getPublicationBm().getPublicationsByStatus("Valid", pubPK);
	    	 // pour chaque publication, regarder la date
	    	 Iterator<PublicationDetail> it = (Iterator<PublicationDetail>) publications.iterator();
	    	 while (it.hasNext())
	    	 {
	    		 PublicationDetail pub = (PublicationDetail) it.next();
	    		 if (pub.getPK().getId().equals(pubId))
	    		  {
	    			  // la publication est plus ancienne
	    			  date = pub.getCreationDate();
	    		  }
	    	 }
	    	 return date;
		 }
		 catch (Exception e)
		 {
			 throw new BlogRuntimeException("BlogBmEJB.getLastPosts()",SilverpeasRuntimeException.ERROR,"post.MSG_POST_NOT_CREATE", e);
		 }
		 finally
		 {
			fermerCon(con);
		 }
	 }

	 public Collection<PostDetail> getPostsByCategory(String categoryId, String instanceId)
	 {
		 SilverTrace.info("blog", "BlogBmEJB.getPostsByCategory()", "root.MSG_GEN_PARAM_VALUE", "categoryId =" + categoryId);
		 Connection con = initCon();

		 NodePK pk = new NodePK(categoryId, null, instanceId);
		 Collection<PostDetail> posts = new ArrayList<PostDetail>();
		 try
		 {
			// rechercher les publications classée
			 Collection<String> lastEvents = PostDAO.getAllEvents(con, instanceId);

			 Collection<PublicationDetail> publications = getPublicationBm().getPubPKsInFatherPK(pk);
	    	  SilverTrace.info("blog", "BlogBmEJB.getPostsByCategory()", "root.MSG_GEN_PARAM_VALUE", "nb publications =" + publications.size());

	    	  Object[] allPubs = publications.toArray();
	    	  SilverTrace.info("blog", "BlogBmEJB.getPostsByCategory()", "root.MSG_GEN_PARAM_VALUE", "allPubs =" + allPubs.length);
	    	  Iterator<String> itEvent = lastEvents.iterator();
			  while (itEvent.hasNext())
			  {
				  String pubId = (String) itEvent.next();
		    	  int j;
		    	  for (int i = 0 ; i < allPubs.length ; i++)
		    	  {
		    		  j = allPubs.length - i - 1;
		    		  SilverTrace.info("blog", "BlogBmEJB.getPostsByCategory()", "root.MSG_GEN_PARAM_VALUE", "i =" + i + " j = " + j);
		    		  PublicationPK pubPK = (PublicationPK) allPubs[j];
		    		  SilverTrace.info("blog", "BlogBmEJB.getPostsByCategory()", "root.MSG_GEN_PARAM_VALUE", "pubPK =" + pubPK.getId());
		    		  if (pubPK.getId().equals(pubId))
		    			  posts.add(getPost(pubPK));
		    	  }
			  }

	    	  return posts;
		 }
		 catch (Exception e)
		 {
			 throw new BlogRuntimeException("BlogBmEJB.getPostsByCategory()",SilverpeasRuntimeException.ERROR,"post.MSG_POST_NOT_CREATE", e);
		 }
	 }

	 public Collection<PostDetail> getPostsByDate(String date, String instanceId)
	 {
		 return getPostsByArchive(date, date, instanceId);
	 }

	 public Collection<PostDetail> getPostsByArchive(String beginDate, String endDate, String instanceId)
	 {
		 SilverTrace.info("blog", "BlogBmEJB.getPostsByArchive()", "root.MSG_GEN_PARAM_VALUE", "dates =" + beginDate + "-" + endDate);

		 PublicationPK pubPK = new PublicationPK("useless", instanceId);
		 Connection con = initCon();
		 Collection<PostDetail> posts = new ArrayList<PostDetail>();
		 try
		 {
			 // rechercher les publications classée par date d'évènement
			 Collection<String> lastEvents = PostDAO.getEventsByDates(con, instanceId, beginDate, endDate);

	    	 //Collection<PublicationDetail> publications = getPublicationBm().getDetailBetweenDate(beginDate, endDate, instanceId);
	    	 Collection<PublicationDetail> publications = getPublicationBm().getPublicationsByStatus("Valid", pubPK);
	    	 OrganizationController orgaController = new OrganizationController();
	    	 Iterator<String> itEvent = lastEvents.iterator();
			 	while (itEvent.hasNext())
				{
			 		String pubId = (String) itEvent.next();
			    	// pour chaque publication, créer le post correspondant
			    	SilverTrace.info("blog", "BlogBmEJB.getPostsByArchive()", "root.MSG_GEN_PARAM_VALUE", "publications =" + publications.toString());
			    	Iterator<PublicationDetail> it = (Iterator<PublicationDetail>) publications.iterator();
			    	while (it.hasNext())
			    	{
			    		PublicationDetail pub = (PublicationDetail) it.next();
					   	if (pub.getPK().getId().equals(pubId))
					   		posts.add(getPost(pub, orgaController));
			    	}
				}
			 	SilverTrace.info("blog", "BlogBmEJB.getPostsByArchive()", "root.MSG_GEN_PARAM_VALUE", "posts =" + posts.toString());
			 	return posts;
		 }
		 catch (Exception e)
		 {
			 throw new BlogRuntimeException("BlogBmEJB.getPostsByArchive()",SilverpeasRuntimeException.ERROR,"post.MSG_POST_NOT_CREATE", e);
		 }
	 }

	 public Collection<PostDetail> getResultSearch(String word, String userId, String spaceId, String instanceId)
		{
			Collection<PostDetail> 	posts 	= new ArrayList<PostDetail>();
			List<String> 		postIds = new ArrayList<String>();
		 	SilverTrace.info("blog", "BlogBmEJB.getResultSearch()", "root.MSG_GEN_PARAM_VALUE", "word =" + word + " userId = " + userId + " instanceId = " + instanceId);
	        QueryDescription query = new QueryDescription(word);
	        query.setSearchingUser(userId);
	        query.addSpaceComponentPair(spaceId, instanceId);
	        MatchingIndexEntry[] result = null;
		 	SilverTrace.info("blog", "BlogBmEJB.getResultSearch()", "root.MSG_GEN_PARAM_VALUE", "query =" + query.getQuery());
		 	Connection con = initCon();

	        try
	        {
	        	SearchEngineBm searchEngineBm = getSearchEngineBm();
	        	searchEngineBm.search(query);
	 			result = searchEngineBm.getRange(0, searchEngineBm.getResultLength());
			 	SilverTrace.info("blog", "BlogBmEJB.getResultSearch()", "root.MSG_GEN_PARAM_VALUE", "result =" + result.length + "length = " + getSearchEngineBm().getResultLength());

	 			// création des billets à partir des résultats

			 	// rechercher la liste des posts trié par date
			 	Collection<String> allEvents = PostDAO.getAllEvents(con, instanceId);
			 	Iterator<String> it = allEvents.iterator();
			 	while (it.hasNext())
			 	{
			 		String pubId = it.next();

		 			for (int i=0; i< result.length; i++)
		 			{
		 				MatchingIndexEntry matchIndex = result[i];
		 				String objectType 	= matchIndex.getObjectType();
		 				String objectId		= matchIndex.getObjectId();
		 				if ("Publication".equals(objectType) || objectType.startsWith("Attachment"))
		 				{
		 					if (pubId.equals(objectId ) && !postIds.contains(objectId))
		 					{
		 						PublicationPK pubPK = new PublicationPK(objectId, matchIndex.getComponent());
			 					PostDetail post = getPost(pubPK);
			 					postIds.add(objectId);
			 					posts.add(post);
		 					}
		 				}
		 			}
			 	}

	        }
	        catch (Exception e)
			{
				throw new BlogRuntimeException("BlogSessionController.getResultSearch()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_ADD_OBJECT", e);
			}
	        finally
			 {
				fermerCon(con);
			 }
	        return posts;
		}

	 public String createCategory(Category category)
	 {
		try
		{
			NodePK nodePK = getNodeBm().createNode((NodeDetail) category, new NodeDetail());
			return nodePK.getId();
		}
		catch (Exception e)
		{
			throw new BlogRuntimeException("BlogBmEJB.createCategory()",SilverpeasRuntimeException.ERROR,"post.MSG_CATEGORY_NOT_CREATE", e);
		}
	 }

	 public void updateCategory(Category category)
	 {
		try
		{
			getNodeBm().setDetail(category);
		}
		catch (Exception e)
		{
			throw new BlogRuntimeException("BlogBmEJB.updateCategory()",SilverpeasRuntimeException.ERROR,"post.MSG_CATEGORY_NOT_UPDATE", e);
		}
	 }

	 public void deleteCategory(String id, String instanceId)
	 {
		 try
		{
			 NodePK nodePk = new NodePK(id, instanceId);

			 // recherche des billets sur cette catégorie
			 Collection<PostDetail> posts = getPostsByCategory(id, instanceId);
			 Iterator<PostDetail> it = (Iterator<PostDetail>) posts.iterator();
	    	  while (it.hasNext())
	    	  {
	    		 // suppression de la catégorie pour ces billets
	    		 PostDetail post = (PostDetail) it.next();
 			 	 getPublicationBm().removeFather(post.getPublication().getPK(), nodePk);
	    	  }
			 // suppression de la catégorie
			 getNodeBm().removeNode(nodePk);
		}
		catch (Exception e)
		{
			throw new BlogRuntimeException("BlogBmEJB.deleteCategory()",SilverpeasRuntimeException.ERROR,"post.MSG_CATEGORY_NOT_DELETE", e);
		}
	 }

	 public Category getCategory(NodePK pk)
	 {
		try
		{
		 	Category category = new Category(getNodeBm().getDetail(pk));
			return category;
		}
		catch (Exception e)
		{
			throw new BlogRuntimeException("BlogBmEJB.getCategory()",SilverpeasRuntimeException.ERROR,"post.MSG_CATEGORY_NOT_EXIST", e);
		}
	 }

	 public Collection<NodeDetail> getAllCategories(String instanceId)
	 {
		try
		{
			NodePK nodePK = new NodePK("0",instanceId);
			Collection<NodeDetail> categories = getNodeBm().getChildrenDetails(nodePK);
			return categories;
		}
		catch (Exception e)
		{
			throw new BlogRuntimeException("BlogBmEJB.getAllCategories()", SilverpeasRuntimeException.ERROR, "post.MSG_CATEGORIES_NOT_EXIST", e);
		}
	 }

	 public Collection<Archive> getAllArchives(String instanceId)
	 {
		 Connection con = initCon();
		 try
		 {
			 Archive archive;
			 Collection<Archive> archives = new ArrayList<Archive>();
			 Collection<String> dates = new ArrayList<String>();
			 Calendar calendar = Calendar.getInstance(Locale.FRENCH);

			 // rechercher tous les posts par date d'évènements
			 Collection<Date> lastEvents = PostDAO.getAllDateEvents(con, instanceId);

			 Iterator<Date> it = lastEvents.iterator();
			 while (it.hasNext())
			 {
				 Date dateEvent = it.next();
				 calendar.setTime(dateEvent);
				 int year = calendar.get(Calendar.YEAR);
		         int month = calendar.get(Calendar.MONTH);
		         // pour chaque date regarder si l'archive existe
		         archive = createArchive(calendar, dateEvent, year, month, instanceId);
	        	 if (archive != null)
	        	 {
		        	 if (!dates.contains(archive.getBeginDate()))
		        	 {
		        		 archives.add(archive);
		        		 dates.add(archive.getBeginDate());
		        	 }
	        	 }
			 }
			return archives;
		 }
		 catch (Exception e)
		 {
			 throw new BlogRuntimeException("BlogBmEJB.getAllArchives()", SilverpeasRuntimeException.ERROR, "post.MSG_CATEGORIES_NOT_EXIST", e);
		 }
		 finally
		 {
			fermerCon(con);
		 }
	 }

	/* public Collection<Archive> getAllArchives(String instanceId)
	 {
		try
		{
			Archive archive;
			// rechercher le post le plus ancien
			Date today = new Date();
			Calendar calendar = Calendar.getInstance(Locale.FRENCH);
			calendar.setTime(today);
			int yearToday = calendar.get(Calendar.YEAR);
			int monthToday = calendar.get(Calendar.MONTH);
			Date date = getOldestPost(instanceId);

			Collection<Archive> archives = new ArrayList<Archive>();
        	calendar.setTime(date);
        	int year = calendar.get(Calendar.YEAR);
        	int month = calendar.get(Calendar.MONTH);

        	if (year != yearToday)
        	{
        	// création des archives de l'année en cours (pas complète)
	        	for (int m = monthToday ; m >= 0 ; m--)
	        	{
	        		// pour chaque mois
	        		archive = createArchive(calendar, date, yearToday, m, instanceId);
	        		if (archive != null)
	        			archives.add(archive);
	        	}

	        	// création des archives des autres années
	        	for (int y = (yearToday -1) ; y > year ; y--)
	        	{
	        		// pour chaque année ...
	        		for (int m = 11 ; m >= 0 ; m--)
	            	{
	            		// pour chaque mois
	        			archive = createArchive(calendar, date, y, m, instanceId);
		        		if (archive != null)
		        			archives.add(archive);
	            	}
	        	}

	        	// création des archives de la première année (pas complète)
	        	for (int m = 11 ; m >= month ; m--)
	        	{
	        		// pour chaque mois
	        		archive = createArchive(calendar, date, year, m, instanceId);
	        		if (archive != null)
	        			archives.add(archive);
	        	}
        	}
        	else
        	{
        		for (int m = monthToday ; m >= month ; m--)
	        	{
	        		// pour chaque mois
        			archive = createArchive(calendar, date, yearToday, m, instanceId);
	        		if (archive != null)
	        			archives.add(archive);
	        	}
        	}
			return archives;
		}
		catch (Exception e)
		{
			throw new BlogRuntimeException("BlogBmEJB.getAllArchives()", SilverpeasRuntimeException.ERROR, "post.MSG_CATEGORIES_NOT_EXIST", e);
		}
	 } */

	 private Archive createArchive(Calendar calendar, Date date, int year, int month, String instanceId)
	 {
		Archive archive = null;
		calendar.set(Calendar.YEAR, year);
 		calendar.set(Calendar.MONTH, month);
 		Date beginDate = getMonthFirstDay(calendar);
 		Date endDate = getMonthLastDay(calendar);
 		// regarder s'il y a des évenements sur cette période
 		Collection<PostDetail> posts = getPostsByArchive(DateUtil.formatDate(beginDate), DateUtil.formatDate(endDate), instanceId);
 		if (posts.size() > 0)
 		{
	 		archive = new Archive("useless", DateUtil.date2SQLDate(beginDate), DateUtil.date2SQLDate(endDate));
	 		archive.setYear(Integer.toString(year));
	 		archive.setMonthId(Integer.toString(month));
 		}
		return archive;
	 }

	 public void indexBlog(String componentId) {
		 indexTopics(new NodePK("useless", componentId));
		 indexPublications(new PublicationPK("useless", componentId));
	 }

	 private void indexPublications(PublicationPK pubPK) {
		 Collection<PublicationDetail> pubs = null;
		 try {
			 pubs = getPublicationBm().getAllPublications(pubPK);
		 } catch (Exception e) {
			 throw new BlogRuntimeException("BlogBmEJB.indexPublications()",SilverpeasRuntimeException.ERROR,"kmelia.EX_IMPOSSIBLE_DINDEXER_LES_PUBLICATIONS", e);
		 }

		 if (pubs != null)
		 {
			 Iterator<PublicationDetail> it = pubs.iterator();
			 PublicationDetail 	pub = null;
			 while (it.hasNext()) {
				 pub = (PublicationDetail) it.next();

				 try
				 {
					 indexPublication(pub.getPK());
				 }
				 catch (Exception e)
				 {
					 throw new BlogRuntimeException("BlogBmEJB.indexPublications()",SilverpeasRuntimeException.ERROR,"kmelia.EX_IMPOSSIBLE_DINDEXER_LA_PUBLICATION", "pubPK = "+pub.getPK().toString(), e);
				 }
			}
		}
	 }

	 private void indexPublication(PublicationPK pubPK) throws RemoteException
	 {
		 //index publication itself
		 getPublicationBm().createIndex(pubPK);

		 //index external elements
		 indexExternalElementsOfPublication(pubPK);
	 }

	 private void indexTopics(NodePK nodePK) {
		 Collection<NodeDetail> nodes = null;
		 try {
			 nodes = getNodeBm().getAllNodes(nodePK);
			 if (nodes != null) {
				 Iterator<NodeDetail> it = nodes.iterator();
				 NodeDetail node = null;
				 while (it.hasNext()) {
					 node = (NodeDetail) it.next();
					 if (!node.getNodePK().getId().equals("0") && !node.getNodePK().getId().equals("1"))
						 getNodeBm().createIndex(node);
				 }
			 }
		} catch (Exception e) {
			throw new BlogRuntimeException("BlogBmEJB.indexTopics()",SilverpeasRuntimeException.ERROR,"kmelia.EX_IMPOSSIBLE_DINDEXER_LES_THEMES", e);
		}
	}

	public void addSubscription(NodePK topicPK, String userId)
	{
		SilverTrace.info("blog","BlogBmEJB.addSubscription()", "root.MSG_GEN_ENTER_METHOD");

        if (!checkSubscription(topicPK, userId))
        	return;

        try {
			getSubscribeBm().addSubscribe(userId, topicPK);
        } catch (Exception e) {
			SilverTrace.warn("blog","BlogBmEJB.addSubscription()", "kmelia.EX_SUBSCRIPTION_ADD_FAILED", "topicId = "+topicPK.getId(), e);
        }
		SilverTrace.info("blog", "BlogBmEJB.addSubscription()", "root.MSG_GEN_EXIT_METHOD");
    }

	public boolean checkSubscription(NodePK topicPK, String userId) {
        try {
			Collection subscriptions = getSubscribeBm().getUserSubscribePKsByComponent(userId, topicPK.getInstanceId());
            for (Iterator iterator = subscriptions.iterator(); iterator.hasNext();) {
                NodePK nodePK = (NodePK) iterator.next();
                if (topicPK.getId().equals(nodePK.getId())) {
                        return false;
                }
            }
            return true;

        } catch (Exception e) {
            throw new BlogRuntimeException("BlogBmEJB.checkSubscription()",SilverpeasRuntimeException.ERROR,
                    "blog.EX_IMPOSSIBLE_DOBTENIR_LES_ABONNEMENTS", e);
        }
    }

	 private void indexExternalElementsOfPublication(PublicationPK pubPK)
	 {
		 try
		 {
			 //index comments
			CommentController.indexCommentsByForeignKey(pubPK);
		 }
		 catch (Exception e)
		 {
			 SilverTrace.error("blog", "BlogBmEJB.indexExternalElementsOfPublication", "Indexing comments failed", "pubPK = "+pubPK.toString(), e);
		 }
	 }

	public void externalElementsOfPublicationHaveChanged(PublicationPK pubPK, String userId)
	{
		try {
			PublicationDetail pubDetail = getPublicationBm().getDetail(pubPK);
			pubDetail.setUpdaterId(userId);
			getPublicationBm().setDetail(pubDetail);

			// envoie notification si abonnement
		 	sendSubscriptionsNotification(new NodePK("0", pubPK.getSpaceId(), pubPK.getInstanceId()), pubDetail, "update");
		}
		catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	 private Date getMonthFirstDay(Calendar calendar)
	 {
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		return calendar.getTime();
	 }

	 private Date getMonthLastDay(Calendar calendar)
	 {
		int monthLastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, monthLastDay);
		return calendar.getTime();
	 }


	 public int getSilverObjectId(PublicationPK pubPK)
	 {
		SilverTrace.info("blog","BlogBmEJB.getSilverObjectId()", "root.MSG_GEN_ENTER_METHOD", "pubId = "+pubPK.getId());
		int					silverObjectId	= -1;
		PublicationDetail	pubDetail		= null;
		try {
			silverObjectId	= getBlogContentManager().getSilverObjectId(pubPK.getId(), pubPK.getInstanceId());
			if (silverObjectId == -1)
			{
				pubDetail		= getPublicationBm().getDetail(pubPK);
				silverObjectId	= createSilverContent(null, pubDetail, pubDetail.getCreatorId());
			}
		} catch (Exception e) {
			throw new BlogRuntimeException("BlogBmEJB.getSilverObjectId()",SilverpeasRuntimeException.ERROR,"blog.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
		}
		return silverObjectId;
	 }

	 private BlogContentManager getBlogContentManager()
	 {
	 	return new BlogContentManager();
	 }

	 private int createSilverContent(Connection con, PublicationDetail pubDetail, String creatorId)
	 {
	 	SilverTrace.info("blog","BlogBmEJB.createSilverContent()", "root.MSG_GEN_ENTER_METHOD", "pubId = "+pubDetail.getPK().getId());
	 	try {
			return getBlogContentManager().createSilverContent(con, pubDetail, creatorId);
		} catch (Exception e) {
			throw new BlogRuntimeException("BlogBmEJB.createSilverContent()",SilverpeasRuntimeException.ERROR,"blog.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
		}
	 }

	 public SubscribeBm getSubscribeBm() {
		  SubscribeBm subscribeBm = null;
	      try {
	    	  SubscribeBmHome subscribeBmHome = (SubscribeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.SUBSCRIBEBM_EJBHOME, SubscribeBmHome.class);
	    	  subscribeBm = subscribeBmHome.create();
	      } catch (Exception e) {
	    	  throw new BlogRuntimeException("BlogBmEJB.getSubscribeBm()",SilverpeasRuntimeException.ERROR,"blog.EX_IMPOSSIBLE_DE_FABRIQUER_SUBSCRIBEBM_HOME", e);
	      }
	      return subscribeBm;
	  }

	 private PublicationBm getPublicationBm()
	 {
		PublicationBm publicationBm = null;
		try
		{
			PublicationBmHome publicationBmHome = (PublicationBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
			publicationBm = publicationBmHome.create();
		}
		catch (Exception e)
		{
			throw new PublicationRuntimeException("BlogBmEJB.getPublicationBm()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
		}
		return publicationBm;
	 }

	 private NodeBm getNodeBm()
	 {
		NodeBm nodeBm = null;
		try
		{
			NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
			nodeBm = nodeBmHome.create();
		}
		catch (Exception e)
		{
			throw new PublicationRuntimeException("BlogBmEJB.getNodeBm()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
		}
		return nodeBm;
	 }

	 public CommentBm getCommentBm()
	 {
		CommentBm commentBm = null;
        try
        {
             CommentBmHome commentHome = (CommentBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.COMMENT_EJBHOME, CommentBmHome.class);
             commentBm =  commentHome.create();
        }
        catch (Exception e)
        {
            throw new CommentRuntimeException("GallerySessionController.getCommentBm()", SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
        }
        return commentBm;
    }

	 public SearchEngineBm getSearchEngineBm()
    {
		SearchEngineBm searchEngineBm = null;
        {
            try
            {
            	SearchEngineBmHome searchEngineHome = (SearchEngineBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.SEARCHBM_EJBHOME, SearchEngineBmHome.class);
            	searchEngineBm =  searchEngineHome.create();
            }
            catch (Exception e)
            {
                throw new CommentRuntimeException("BlogSessionController.getCommentBm()", SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
            }
        }
        return searchEngineBm;
    }

	public void ejbCreate()
	{
		// not implemented
	}

	public void setSessionContext(SessionContext context)
	{
		// not implemented
	}

	public void ejbRemove()
	{
		// not implemented
	}

	public void ejbActivate()
	{
		// not implemented
	}

	public void ejbPassivate()
	{
		// not implemented
	}
}
