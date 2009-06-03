package com.silverpeas.whitePages.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.silverpeas.whitePages.control.CardManager;
import com.silverpeas.whitePages.model.Card;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.CompoSpace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * Ce filtre a pour effet de contr�ler que l'utilisateur courant n'a pas une
 * fiche � remplir dans une instance de whitePages. Si c'est le cas, 2 attributs
 * sont mis en sessions : - RedirectToComponentId : avec le componentId de
 * l'instance pour que le mecanisme de redirection le renvoie sur le composant -
 * FicheNonRemplie : avec le componentId de l'instance pour que le filtre mapp�
 * sur tous les routers des composants puisse intercepter au besoin et renvoyer
 * sur la fiche.
 *
 * @author Ludovic Bertin
 *
 */
public class LoginFilter implements Filter {

	public static final String ATTRIBUTE_FORCE_CARD_CREATION = "forceCardCreation";
	public static final String ATTRIBUTE_COMPONENT_ID = "RedirectToComponentId";
	/**
	 * Configuration du filtre, permettant de r�cup�rer les param�tres.
	 */
	FilterConfig config = null;

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpSession session = ((HttpServletRequest) request).getSession(true);

		/*
		 * Retrieve main session controller
		 */
		MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");

		/*
		 * If no main session controller, forward user to timeout page
		 */
		if (mainSessionCtrl == null)
	    {
			SilverTrace.warn("whitePages", "LoginFilter.doFilter", "root.MSG_GEN_SESSION_TIMEOUT", "NewSessionId=" + session.getId());
			RequestDispatcher dispatcher = request.getRequestDispatcher(GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout"));
			dispatcher.forward(request, response);
	    }

		/*
		 * Retrieves all instances of WhitePages for which user is Reader.
		 */
		else {
			String userId = mainSessionCtrl.getUserId();
			try {
				Admin admin = new Admin();
				CompoSpace[] availableInstances = admin.getCompoForUser(userId, "whitePages");

				for (int i=0; i<availableInstances.length; i++) {
					String instanceId = availableInstances[i].getComponentId();

					/* Retrieve component */
					ComponentInst instance = admin.getComponentInst(instanceId);

					/* Is user is administrator for that instance */
					boolean userIsAdmin = false;
					String[] activeProfiles = admin.getCurrentProfiles(userId, instance);
					for (int j=0; j<activeProfiles.length; j++) {
						if (activeProfiles[j].equals("admin"))
							userIsAdmin = true;
					}

					/* Is forcedCardFilling parameter turned on */
					String forcedCardFilling = admin.getComponentParameterValue(instanceId, "isForcedCardFilling");
					boolean isForcedCardFilling = ( (forcedCardFilling != null) && (forcedCardFilling.equals("yes")) );

					/* Redirect user if and only if user is no admin and forcedCardFilling parameter turned on */
					if (isForcedCardFilling && !userIsAdmin) {
						CardManager cardManager = CardManager.getInstance();
						Card userCard = cardManager.getUserCard(userId, instanceId);
						if ( (userCard == null) || (!cardManager.isPublicationClassifiedOnPDC(userCard)) ) {
							session.setAttribute(ATTRIBUTE_COMPONENT_ID, instanceId);
							session.setAttribute(ATTRIBUTE_FORCE_CARD_CREATION, instanceId);
							break;
						}
					}
				}
			} catch (Exception e) {
				SilverTrace.warn("whitePages", "LoginFilter.doFilter", "root.EX_IGNORED", e);
			}

			chain.doFilter(request, response);
		}
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.Filter#getFilterConfig()
	 */
	public FilterConfig getFilterConfig() {
		return config;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.Filter#setFilterConfig(javax.servlet.FilterConfig)
	 */
	public void setFilterConfig(FilterConfig arg0) {
		//this.config = config;
	}

	public void init(FilterConfig arg0) {
		//this.config = config;
	}

	public void destroy() {

	}
}
