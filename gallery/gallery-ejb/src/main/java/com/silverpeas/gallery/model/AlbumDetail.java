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
package com.silverpeas.gallery.model;

import java.util.Collection;

import com.stratelia.webactiv.util.node.model.NodeDetail;
import java.util.ArrayList;
import java.util.List;

public class AlbumDetail extends NodeDetail {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private List<PhotoDetail> photos;
  private String permalink = null;

  public AlbumDetail(NodeDetail node) {
    setNodePK(node.getNodePK());
    setName(node.getName());
    setDescription(node.getDescription());
    setCreationDate(node.getCreationDate());
    setCreatorId(node.getCreatorId());
    setPath(node.getPath());
    setLevel(node.getLevel());
    setStatus(node.getStatus());
    setFatherPK(node.getFatherPK());
    setChildrenDetails(node.getChildrenDetails());
    setType(node.getType());
    setOrder(node.getOrder());
  }

  public List<PhotoDetail> getPhotos() {
    return photos;
  }

  public void setPhotos(Collection<PhotoDetail> photos) {
    this.photos = new ArrayList<PhotoDetail>(photos);
  }

  @Override
  public String getPermalink() {
    return permalink;
  }

  public void setPermalink(String permalink) {
    this.permalink = permalink;
  }
}
