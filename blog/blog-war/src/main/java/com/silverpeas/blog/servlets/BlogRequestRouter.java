package com.silverpeas.blog.servlets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.blog.control.BlogSessionController;
import com.silverpeas.blog.model.Category;
import com.silverpeas.blog.model.PostDetail;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Event;

public class BlogRequestRouter extends ComponentRequestRouter
{
    /**
     * This method has to be implemented in the component request rooter class.
     * returns the session control bean name to be put in the request object
     * ex : for almanach, returns "almanach"
     */
    public String getSessionControlBeanName()
	 {
	    return "Blog";
	 }

    /**
     * Method declaration
     *
     *
     * @param mainSessionCtrl
     * @param componentContext
     *
     * @return
     *
     * @see
     */
    public ComponentSessionController createComponentSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
    {
        return new BlogSessionController(mainSessionCtrl, componentContext);
    }
    
    // recherche du profile de l'utilisateur
	public String getFlag(String[] profiles)
	{
		String flag = "user";
		for (int i = 0; i < profiles.length; i++)
		{
			if (profiles[i].equals("admin"))
			{
				return profiles[i];
			}
		}
		return flag;
	}

    /**
     * This method has to be implemented by the component request rooter
     * it has to compute a destination page
     * @param function The entering request function (ex : "Main.jsp")
     * @param componentSC The component Session Control, build and initialised.
     * @return The complete destination URL for a forward (ex : "/almanach/jsp/almanach.jsp?flag=user")
     */
    public String getDestination(String function, ComponentSessionController componentSC, HttpServletRequest request)
    {
        String destination = "";
        BlogSessionController  blogSC = (BlogSessionController)componentSC;
        SilverTrace.info("blog", "BlogRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "User=" + componentSC.getUserId() + " Function=" + function);
        String rootDest = "/blog/jsp/";
        
        // param�tres g�n�raux
        request.setAttribute("Profile", getFlag(blogSC.getUserRoles()));

        try
        {
            if (function.startsWith("Main"))
            {
            	// r�cup�ration des derniers billets par date d'�v�nements
            	Collection<PostDetail> posts = blogSC.lastPosts();
            	request.setAttribute("Posts", posts);
            	// r�cup�ration des param�tres communs
            	setCommonParam(blogSC, request);
            	
            	//creation d'une liste d'event par rapport aux posts du mois
            	String beginDate = blogSC.getCurrentBeginDateAsString();
            	String endDate = blogSC.getCurrentEndDateAsString();
            	Collection<PostDetail> postsByMonth = blogSC.postsByArchive(beginDate, endDate);
            	Collection<Event> events = getEvents(blogSC, postsByMonth);
            	request.setAttribute("Events", events);
            	
            	request.setAttribute("DateCalendar", blogSC.getCurrentBeginDateAsString());
            	request.setAttribute("IsUsePdc", blogSC.isPdcUsed());
            	
            	// appel de la page d'accueil 
	            destination = rootDest + "accueil.jsp";
            }
            else if (function.equals("NewPost"))
            {
            	request.setAttribute("AllCategories", blogSC.getAllCategories());
            	request.setAttribute("UserName", blogSC.getUserDetail(blogSC.getUserId()).getDisplayedName());
            	request.setAttribute("IsUsePdc", blogSC.isPdcUsed());
            	
            	// appel de la page de cr�ation
            	destination = rootDest + "postManager.jsp";
            }
            else if (function.equals("CreatePost"))
            {
             	// r�cup�ration des param�tres pour cr�ation 
             	String title = request.getParameter("Title");
             	String categoryId = request.getParameter("CategoryId");
             	String date = request.getParameter("DateEvent");
             	Date dateEvent = DateUtil.stringToDate(date, blogSC.getLanguage());
             	String postId = blogSC.createPost(title, categoryId, dateEvent);
            	
            	// appel de la page pour saisir le contenu du billet
            	request.setAttribute("PostId", postId);
            	destination = getDestination("ViewContent", blogSC, request);
            }
            else if (function.equals("EditPost"))
            {
            	String postId = request.getParameter("PostId");
            	PostDetail post = blogSC.getPost(postId);
            	request.setAttribute("Post", post);
            	request.setAttribute("UserName", blogSC.getUserDetail(post.getPublication().getCreatorId()).getDisplayedName());
            	request.setAttribute("AllCategories", blogSC.getAllCategories());
            	request.setAttribute("IsUsePdc", blogSC.isPdcUsed());
            	
            	// appel de la page de modification
            	destination = rootDest + "postManager.jsp";
            }
            else if (function.equals("UpdatePost"))
            {
            	String postId = request.getParameter("PostId");
            	String title = request.getParameter("Title");
				String categoryId = request.getParameter("CategoryId");
				String date = request.getParameter("DateEvent");
             	Date dateEvent = DateUtil.stringToDate(date, blogSC.getLanguage());
             	
				// MAJ base
				blogSC.updatePost(postId, title, categoryId, dateEvent);
				
				destination = getDestination("ViewContent", blogSC, request);
            }
            else if (function.equals("DeletePost"))
            {
            	String postId = request.getParameter("PostId");
            	blogSC.deletePost(postId);
            	
            	destination = getDestination("Main", blogSC, request);
            }
            else if (function.equals("ViewContent"))
            {
            	String postId = request.getParameter("PostId");
				if (postId == null || postId.length() == 0 || "null".equals(postId))
					postId = (String) request.getAttribute("PostId");
				
            	PostDetail post = blogSC.getPost(postId);
            	PublicationDetail pub = post.getPublication();
				request.setAttribute("CurrentPublicationDetail", pub);
				request.setAttribute("UserId", blogSC.getUserId());
            	// visualisation et modification du contenu Wysiwyg du billet
            	destination = rootDest + "toWysiwyg.jsp";
            }
            else if (function.equals("FromWysiwyg"))
			{
				destination = getDestination("ViewPost", blogSC, request);
			}
            else if (function.equals("ViewPost"))
            {
            	// visualisation d'un billet avec les commentaires
            	String postId = request.getParameter("PostId");
				if (postId == null || postId.length() == 0 || "null".equals(postId))
					postId = (String) request.getAttribute("PostId");
            	PostDetail post = blogSC.getPost(postId);
            	request.setAttribute("Post", post);
            	setCommonParam(blogSC, request);
            	request.setAttribute("AllComments", blogSC.getAllComments(postId));
            	//creation d'une liste d'event par rapport � posts
            	String beginDate = blogSC.getCurrentBeginDateAsString();
            	String endDate = blogSC.getCurrentEndDateAsString();
            	Collection<PostDetail> posts = blogSC.postsByArchive(beginDate, endDate);
            	Collection<Event> events = getEvents(blogSC, posts);
            	request.setAttribute("Events", events);
            	request.setAttribute("DateCalendar", beginDate);
            	destination = rootDest + "viewPost.jsp";
            }
            else if (function.equals("DeleteComment"))
            {
            	String id = request.getParameter("CommentId");
            	blogSC.deleteComment(id);
             	destination = getDestination("ViewPost", componentSC, request);
            }
            else if (function.equals("UpdateComment"))
            {
            	String postId = request.getParameter("PostId");
            	blogSC.sendSubscriptionsNotification(postId, "commentUpdate");
            	request.setAttribute("PostId", postId);
            	destination = getDestination("ViewPost", componentSC, request);
            }
            else if (function.equals("PostByCategory"))
            {
            	// r�cup�ration des param�tres
            	String categoryId = request.getParameter("CategoryId");
            	if (categoryId == null || categoryId.length() == 0 || "null".equals(categoryId))
            		categoryId = (String) request.getAttribute("CategoryId");
            	// r�cup�ration des billets par cat�gorie
            	request.setAttribute("Posts", blogSC.postsByCategory(categoryId));
            	setCommonParam(blogSC, request);
            	//creation d'une liste d'event par rapport � posts
            	String beginDate = blogSC.getCurrentBeginDateAsString();
            	String endDate = blogSC.getCurrentEndDateAsString();
            	Collection<PostDetail> posts = blogSC.postsByArchive(beginDate, endDate);
            	Collection<Event> events = getEvents(blogSC, posts);
            	request.setAttribute("Events", events);
            	request.setAttribute("DateCalendar", beginDate);
            	
	            destination = rootDest + "accueil.jsp";
            }
            else if (function.equals("PostByArchive"))
            {
            	// r�cup�ration des param�tres
            	String beginDate = request.getParameter("BeginDate");
            	if (beginDate == null || beginDate.length() == 0 || "null".equals(beginDate))
            		beginDate = (String) request.getAttribute("BeginDate");
            	String endDate = request.getParameter("EndDate");
            	if (endDate == null || endDate.length() == 0 || "null".equals(endDate))
            		endDate = (String) request.getAttribute("EndDate");
            	
            	// r�cup�ration des billets par archive
            	Collection<PostDetail> posts = blogSC.postsByArchive(beginDate, endDate);
            	request.setAttribute("Posts", posts);
            	setCommonParam(blogSC, request);
            	//creation d'une liste d'event par rapport � posts
            	Collection<Event> events = getEvents(blogSC, posts);
            	request.setAttribute("Events", events);
            	request.setAttribute("DateCalendar", blogSC.getCurrentBeginDateAsString());
            	
	            destination = rootDest + "accueil.jsp";
            }
            else if (function.equals("PostByDay"))
            {
            	// r�cup�ration des param�tres
            	String day = request.getParameter("Day");
            	String date = DateUtil.date2SQLDate(day, blogSC.getLanguage());
            	SilverTrace.info("blog", "BlogRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "date =" + date);
            	// r�cup�ration des billets par archive
            	request.setAttribute("Posts", blogSC.postsByDate(date));
            	setCommonParam(blogSC, request);
            	//creation d'une liste d'event par rapport � posts
            	String beginDate = blogSC.getCurrentBeginDateAsString();
            	String endDate = blogSC.getCurrentEndDateAsString();
            	Collection<PostDetail> posts = blogSC.postsByArchive(beginDate, endDate);
            	Collection<Event> events = getEvents(blogSC, posts);
            	request.setAttribute("Events", events);
            	request.setAttribute("DateCalendar", beginDate);
            	
	            destination = rootDest + "accueil.jsp";
            }
            else if (function.equals("PreviousMonth"))
            {
            	//modifier les dates de d�but et de fin de mois
            	blogSC.previousMonth();
            	// mettre les dates dans la request
            	request.setAttribute("BeginDate", blogSC.getCurrentBeginDateAsString());
            	request.setAttribute("EndDate", blogSC.getCurrentEndDateAsString());
            	destination = getDestination("PostByArchive", blogSC, request);
            }
            else if (function.equals("NextMonth"))
            {
            	// modifier les dates de d�but et de fin de mois
            	blogSC.nextMonth();
            	// mettre les dates dans la request
            	request.setAttribute("BeginDate", blogSC.getCurrentBeginDateAsString());
            	request.setAttribute("EndDate", blogSC.getCurrentEndDateAsString());
            	destination = getDestination("PostByArchive", blogSC, request);
            }
            else if (function.equals("AddComment"))
            {
            	// r�cup�ration des param�tres
            	String message = request.getParameter("Message");
            	String postId = (String)request.getParameter("PostId"); 
            	// ajout du commentaire
            	blogSC.addComment(postId, message);
            	// retour � la page de visualisation du billet 
            	destination = getDestination("ViewPost", blogSC, request);
            }
            else if (function.equals("ViewCategory"))
            {
            	request.setAttribute("Categories", blogSC.getAllCategories());
				destination = rootDest + "viewCategory.jsp";
            }
            else if (function.equals("NewCategory"))
            {
            	request.setAttribute("UserName", blogSC.getUserDetail(blogSC.getUserId()).getDisplayedName());
            	
            	destination = rootDest + "categoryManager.jsp";
            }
            else if (function.equals("CreateCategory"))
            {
            	// r�cup�ration des param�tres
            	String name = request.getParameter("Name");
            	String description = request.getParameter("Description");
				NodeDetail node = new NodeDetail("unknown", name, description, null, null, null, "0", "unknown");
				Category category = new Category(node);
            	blogSC.createCategory(category);
            	
            	destination = getDestination("ViewCategory", blogSC, request);
            }
            else if (function.equals("EditCategory"))
            {
            	// r�cup�ration des param�tres
            	String categoryId = request.getParameter("CategoryId");
            	Category category = blogSC.getCategory(categoryId);
            	request.setAttribute("Category", category);
            	
				destination = rootDest + "categoryManager.jsp";
            }
            else if (function.equals("UpdateCategory"))
            {
            	String categoryId = request.getParameter("CategoryId");
				Category category = blogSC.getCategory(categoryId);
				String name = request.getParameter("Name");
				category.setName(name);
				String desc = request.getParameter("Description");
				category.setDescription(desc);
				// MAJ base
				blogSC.updateCategory(category);
            	
            	destination = getDestination("ViewCategory", blogSC, request);
            }
            else if (function.equals("DeleteCategory"))
            {
            	String categoryId = request.getParameter("CategoryId");
            	blogSC.deleteCategory(categoryId);
            	
            	destination = getDestination("ViewCategory", blogSC, request);
            }
            else if (function.equals("ToAlertUser"))
			{
				String postId = request.getParameter("PostId");
				try
				{
					destination = blogSC.initAlertUser(postId);
				}
				catch (Exception e)
				{
					SilverTrace.warn("blog", "BlogRequestRouter.getDestination()", "root.EX_USERPANEL_FAILED", "function = " + function, e);
				}
			}
            else if (function.equals("Search"))
            {
            	String wordSearch = request.getParameter("WordSearch");
    		 	SilverTrace.info("blog", "BlogRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "word =" + wordSearch);

    		 	Collection<PostDetail> posts = blogSC.getResultSearch(wordSearch);
            	request.setAttribute("Posts", posts);
    		 	SilverTrace.info("blog", "BlogRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "posts =" + posts);
    		 	setCommonParam(blogSC, request);
            	request.setAttribute("DateCalendar", blogSC.getCurrentBeginDateAsString());
            	
	            destination = rootDest + "accueil.jsp";
            }
            else if (function.equals("PdcPositions"))
			{
				// traitement du plan de classement
				String postId = request.getParameter("PostId");
				if (postId == null || postId.length() == 0 || "null".equals(postId))
					postId = (String) request.getAttribute("PostId");
            	PostDetail post = blogSC.getPost(postId);
            	request.setAttribute("Post", post);
				
				request.setAttribute("SilverObjetId", new Integer(blogSC.getSilverObjectId(postId)));
				
				destination = rootDest + "pdcPositions.jsp";
			}
            else if (function.startsWith("searchResult")) 
			{
				// traiter les recherches 
				String id = request.getParameter("Id");
				String type = request.getParameter("Type");
		        SilverTrace.info("blog", "BlogRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "Type=" + type);

				if (type.equals("Publication"))
				{
					// traitement des billets
					SilverTrace.info("blog", "BlogRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "Id=" + id);
					request.setAttribute("PostId", id);
					destination = getDestination("ViewPost", blogSC, request);
				}
				else if (type.equals("Node") || type.equals("Topic"))
				{
					// traitement des cat�gories
					request.setAttribute("CategoryId", id);
					destination = getDestination("PostByCategory", blogSC, request);
				}
				else if (type.startsWith("Comment"))
				{
					// traitement des commentaires
					request.setAttribute("PostId", id);
					destination = getDestination("ViewPost", blogSC, request);
				}
				else
				{
					destination = getDestination("Main", blogSC, request);
				}
			}
            else if (function.startsWith("portlet")) 
			{
            	// r�cup�ration des derniers billets
             	request.setAttribute("Posts", blogSC.lastPosts());
            	// appel de la page de portlet
				destination = rootDest + "portlet.jsp";
			}
            else if (function.equals("AddSubscription"))
            {
            	// ajout aux abonnements
            	blogSC.addSubscription("0");
            	destination = getDestination("Main", blogSC, request);
            }
            else
			{
				destination = rootDest + function;
			}
        }
        catch (Exception e)
        {
            request.setAttribute("javax.servlet.jsp.jspException", e);
            destination = "/admin/jsp/errorpageMain.jsp";
        }

        SilverTrace.info("blog", "BlogRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
        return destination;
    }
    
    private Collection<Event> getEvents(BlogSessionController blogSC, Collection<PostDetail> posts)
    {
    	Collection<Event> events = new ArrayList<Event>();
    	Date dateEvent;
 		Iterator<PostDetail> it = (Iterator<PostDetail>) posts.iterator();
    	 while (it.hasNext()) 
    	 {
    		 PostDetail post = (PostDetail) it.next();
    		 
    		 // chercher la date de l'�v�nement
    		 String pubId = post.getPublication().getPK().getId();
    		 try 
    		 {
				dateEvent = blogSC.getDateEvent(pubId);
			} catch (RemoteException e) {
				dateEvent = post.getPublication().getCreationDate();
			}
    		 Event event = new Event(post.getPublication().getPK().getId(), post.getPublication().getName(), dateEvent, dateEvent, null, 0);
    		 events.add(event);	
    	 }
    	
    	return events;
    }
    
    private void setCommonParam(BlogSessionController blogSC, HttpServletRequest request)
    {
    	request.setAttribute("Categories", blogSC.getAllCategories());
    	request.setAttribute("Archives", blogSC.getAllArchives());
    	request.setAttribute("Links", blogSC.getAllLinks());
    	request.setAttribute("Url",blogSC.getComponentUrl());
    	request.setAttribute("RSSUrl", blogSC.getRSSUrl());
    	request.setAttribute("IsUsePdc", blogSC.isPdcUsed());
    }
}
