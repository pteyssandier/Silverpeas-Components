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
package com.stratelia.webactiv.almanach.control.ejb;

import java.util.Collection;
import java.rmi.RemoteException;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;

import com.stratelia.webactiv.almanach.model.*;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

public interface AlmanachBmBusinessSkeleton {

  /**
   * Get the events of the month
   * @author dlesimple
   * @param pk
   * @param date
   * @param String [] of instanceIds
   * @return Collection of Events
   */
  public Collection<EventDetail> getMonthEvents(EventPK pk, java.util.Date date,
      String[] instanceIds) throws RemoteException;

  /**
   * Get the events of the month
   * @author dlesimple
   * @param pk
   * @param date
   * @return Collection of Events
   */
  public Collection<EventDetail> getMonthEvents(EventPK pk, java.util.Date date)
      throws RemoteException;

  /**
   * this method provide a collection of event
   * @param : EventPk pk, to obtain the space and component @ return: java.util.Collection
   */
  public Collection<EventDetail> getAllEvents(EventPK pk) throws RemoteException;

  /**
   * Get all events of instanceId Almanachs
   * @param pk
   * @param String [] of instanceIds
   * @return Collection of Events
   */
  public Collection<EventDetail> getAllEvents(EventPK pk, String[] instanceIds)
      throws RemoteException;

  public Collection<EventDetail> getEvents(Collection<EventPK> pks) throws RemoteException;

  /**
   * addEvent() add an event entry in the database
   */
  public String addEvent(EventDetail event) throws RemoteException;

  /**
   * updateEvent() update the event entry, specified by the pk, in the database
   */
  public void updateEvent(EventDetail event) throws RemoteException;

  /**
   * removeEvent() remove the Event entry specified by the pk
   */
  public void removeEvent(EventPK pk) throws RemoteException;

  /**
   * getEventDetail() returns the EventDetail represented by the pk
   */
  public EventDetail getEventDetail(EventPK pk) throws RemoteException;

  public int getSilverObjectId(EventPK pk) throws RemoteException;

  public void createIndex(EventDetail detail) throws RemoteException;

  public Collection<EventDetail> getNextEvents(String instanceId, int nbReturned)
      throws RemoteException;

  public void addPeriodicity(Periodicity periodicity) throws RemoteException;

  public Periodicity getPeriodicity(String eventId) throws RemoteException;

  public void removePeriodicity(Periodicity periodicity) throws RemoteException;

  public void updatePeriodicity(Periodicity periodicity) throws RemoteException;

  public void addPeriodicityException(PeriodicityException exception)
      throws RemoteException;

  public Collection<PeriodicityException> getListPeriodicityException(String periodicityId)
      throws RemoteException;

  public void removeAllPeriodicityException(String periodicityId)
      throws RemoteException;

  public Calendar getICal4jCalendar(Collection<EventDetail> events, String language)
      throws RemoteException;

  public Collection<EventDetail> getListRecurrentEvent(Calendar calendarAlmanach,
      java.util.Calendar currentDay, String spaceId, String instanceId, boolean yearScope)
      throws RemoteException;

  public RRule generateRecurrenceRule(Periodicity periodicity)
      throws RemoteException;

  public ExDate generateExceptionDate(Periodicity periodicity)
      throws RemoteException;

  /**************************************************************************************/
  /* Interface - Fichiers joints */
  /**************************************************************************************/
  public Collection<AttachmentDetail> getAttachments(EventPK eventPK) throws RemoteException;

  public String getHTMLPath(EventPK eventPK) throws RemoteException;
}