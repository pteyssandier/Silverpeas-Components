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

package com.silverpeas.scheduleevent.service.model.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.silverpeas.scheduleevent.service.model.ScheduleEventBean;

public class ScheduleEvent implements ScheduleEventBean, Serializable {

  private static final long serialVersionUID = 1L;
  public String id;
  public String title;
  public String description;
  public Date creationDate;
  public int author;
  public Set<DateOption> dates = new HashSet<DateOption>();
  public Set<Contributor> contributors =
      new HashSet<Contributor>();
  public Set<Response> responses = new HashSet<Response>();
  public int status;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  @Override
  public int getAuthor() {
    return author;
  }

  @Override
  public void setAuthor(int author) {
    this.author = author;
  }

  @Override
  public Set<DateOption> getDates() {
    return dates;
  }

  @Override
  public void setDates(Set<DateOption> dates) {
    this.dates = dates;
  }

  @Override
  public Set<Contributor> getContributors() {
    return contributors;
  }

  @Override
  public void setContributors(Set<Contributor> contributors) {
    this.contributors = contributors;
  }

  @Override
  public Set<Response> getResponses() {
    return responses;
  }

  @Override
  public void setResponses(Set<Response> responses) {
    this.responses = responses;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public void setStatus(int status) {
    this.status = status;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ScheduleEvent other = (ScheduleEvent) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
  
}
