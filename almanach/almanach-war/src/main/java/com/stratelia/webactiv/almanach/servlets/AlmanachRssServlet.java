package com.stratelia.webactiv.almanach.servlets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import com.silverpeas.peasUtil.RssServlet;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBmHome;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachRuntimeException;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class AlmanachRssServlet extends RssServlet {
  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.peasUtil.RssServlet#getListElements(java.lang.String,
   * int)
   */
  public Collection getListElements(String instanceId, int nbReturned)
      throws RemoteException {
    // récupération de la liste des 10 prochains événements de l'Almanach
    Collection result = new ArrayList();

    Collection allEvents = getAlmanachBm().getAllEvents(
        new EventPK("", "", instanceId));
    net.fortuna.ical4j.model.Calendar calendarAlmanach = getAlmanachBm()
        .getICal4jCalendar(allEvents, "fr");

    Calendar currentDay = GregorianCalendar.getInstance();
    Collection events = getAlmanachBm().getListRecurrentEvent(calendarAlmanach,
        null, "", instanceId);
    if (events != null) {
      Iterator it = events.iterator();
      EventDetail eventDetail;
      int nb = 1;
      while (nb <= nbReturned && it.hasNext()) {
        eventDetail = (EventDetail) it.next();
        if (eventDetail.getStartDate().after(currentDay.getTime())) {
          result.add(eventDetail);
          nb++;
        }
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.peasUtil.RssServlet#getElementTitle(java.lang.Object,
   * java.lang.String)
   */
  public String getElementTitle(Object element, String userId) {
    EventDetail event = (EventDetail) element;
    return event.getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.peasUtil.RssServlet#getElementLink(java.lang.Object,
   * java.lang.String)
   */
  public String getElementLink(Object element, String userId) {
    EventDetail event = (EventDetail) element;
    return event.getPermalink();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.peasUtil.RssServlet#getElementDescription(java.lang.Object,
   * java.lang.String)
   */
  public String getElementDescription(Object element, String userId) {
    EventDetail event = (EventDetail) element;
    return event.getDescription();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.peasUtil.RssServlet#getElementDate(java.lang.Object)
   */
  public Date getElementDate(Object element) {
    EventDetail event = (EventDetail) element;
    Calendar calElement = GregorianCalendar.getInstance();
    calElement.setTime(event.getStartDate());
    String hourMinute = event.getStartHour(); // hh:mm
    if (hourMinute != null && hourMinute.trim().length() > 0) {
      /*
       * int hour = new Integer(hourMinute.substring(0, 2)).intValue() - 1; //-1
       * car bug d'affichage du fil RSS qui affiche toujours 1h en trop
       */
      int hour = DateUtil.extractHour(hourMinute);
      int minute = DateUtil.extractMinutes(hourMinute);
      calElement.set(Calendar.HOUR_OF_DAY, hour);
      calElement.set(Calendar.MINUTE, minute);
    } else {
      /*
       * calElement.set(Calendar.HOUR_OF_DAY, -1);//-1 car bug d'affichage du
       * fil RSS qui affiche toujours 1h en trop
       */
      calElement.set(Calendar.HOUR_OF_DAY, 0);
      calElement.set(Calendar.MINUTE, 0);
    }
    return calElement.getTime();
  }

  public String getElementCreatorId(Object element) {
    EventDetail event = (EventDetail) element;
    return event.getCreatorId();
  }

  private AlmanachBm getAlmanachBm() {
    AlmanachBm almanachBm = null;
    try {
      AlmanachBmHome almanachBmHome = (AlmanachBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.ALMANACHBM_EJBHOME, AlmanachBmHome.class);
      almanachBm = almanachBmHome.create();
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachRssServlet.getAlmanachBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return almanachBm;
  }
}