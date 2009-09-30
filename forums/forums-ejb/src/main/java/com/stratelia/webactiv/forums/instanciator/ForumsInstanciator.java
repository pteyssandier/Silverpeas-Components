/*
 * ForumsInstanciator.java
 *
 * Cree le 27 Septembre 2000 
 */

package com.stratelia.webactiv.forums.instanciator;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygInstanciator;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * @author frageade
 * @version update by the Sébastien Antonio - Externalisation of the SQL request
 */
public class ForumsInstanciator extends SQLRequest implements
    ComponentsInstanciatorIntf {

  /** Creates new ForumsInstanciator */
  public ForumsInstanciator() {
    super("com.stratelia.webactiv.forums");
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("forums", "ForumsInstanciator.create()",
        "forums.MSG_CREATE_WITH_SPACE_AND_COMPONENT", "space : " + spaceId
            + "component : " + componentId);
  }

  /**
   * Delete some rows of an instance of a forum.
   * 
   * @param con
   *          (Connection) the connection to the data base
   * @param spaceId
   *          (String) the id of a the space where the component exist.
   * @param componentId
   *          (String) the instance id of the Silverpeas component forum.
   * @param userId
   *          (String) the owner of the component
   */
  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("forums", "ForumsInstanciator.delete()",
        "forums.MSG_DELETE_WITH_SPACE", "spaceId : " + spaceId);

    // read the property file which contains all SQL queries to delete rows
    setDeleteQueries();

    deleteDataOfInstance(con, componentId, "Subscription");
    deleteDataOfInstance(con, componentId, "Rights");
    deleteDataOfInstance(con, componentId, "Message");
    deleteDataOfInstance(con, componentId, "Forum");

    // delete wysiwyg stuff
    WysiwygInstanciator wysiwygI = new WysiwygInstanciator(
        "uselessButMandatory :)");
    wysiwygI.delete(con, spaceId, componentId, userId);

  }

  /**
   * Delete all data of one forum instance from the forum table.
   * 
   * @param con
   *          (Connection) the connection to the data base
   * @param componentId
   *          (String) the instance id of the Silverpeas component forum.
   * @param suffixName
   *          (String) the suffixe of a Forum table
   */
  private void deleteDataOfInstance(Connection con, String componentId,
      String suffixName) throws InstanciationException {

    Statement stmt = null;

    // get the delete query from the external file
    String deleteQuery = getDeleteQuery(componentId, suffixName);

    // execute the delete query
    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery);
      stmt.close();
    } catch (SQLException se) {
      InstanciationException ie = new InstanciationException(
          "ForumsInstanciator.deleteDataOfInstance()",
          SilverpeasException.ERROR, "root.DELETING_DATA_OF_INSTANCE_FAILED",
          "componentId = " + componentId + " deleteQuery = " + deleteQuery, se);
      throw ie;
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        InstanciationException ie = new InstanciationException(
            "ForumsInstanciator.deleteDataOfInstance()",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED",
            "componentId = " + componentId + " deleteQuery = " + deleteQuery,
            err_closeStatement);
        throw ie;
      }
    }

  }
}