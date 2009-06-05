package com.silverpeas.mailinglist.web;

import java.sql.SQLException;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;

import com.meterware.httpunit.ClientProperties;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;
import com.silverpeas.mailinglist.AbstractSilverpeasDatasourceSpringContextTests;

public class TestMailingListActivity extends
    AbstractSilverpeasDatasourceSpringContextTests {

  private static String MESSAGE_BASE = "destination/activity/message/";

  public void testSimpleUser() throws Exception {
    WebConversation connection = new WebConversation();
    WebResponse loginPage = connection.getResponse(buildUrl("silverpeas/"));
    assertNotNull(loginPage);
    WebForm loginForm = loginPage.getFormWithName("EDform");
    assertNotNull(loginForm);
    loginForm.setParameter("Login", "SilverAdmin");
    loginForm.setParameter("Password", "SilverAdmin");
    HttpUnitOptions.setScriptingEnabled(false);
    WebResponse welcomePage = loginForm.submit();
    assertNotNull(welcomePage);
    String[] frameNames = welcomePage.getFrameNames();
    assertEquals(5, frameNames.length);
    WebResponse navigationFrame = connection.getFrameContents("bottomFrame");
    assertNotNull(navigationFrame);
    navigationFrame = connection.getFrameContents("SpacesBar");
    assertNotNull(navigationFrame);
    WebLink mailingListLink = navigationFrame.getLinkWith("Liste de diffusion");
    assertNotNull(mailingListLink);
    HttpUnitOptions.setScriptingEnabled(false);
    WebResponse activityPage = connection.getResponse(buildUrl(mailingListLink
        .getURLString()));
    assertNotNull(activityPage);
    WebTable browseBar = activityPage.getTableWithID("browseBar");
    assertNotNull(browseBar);
    assertEquals("MGI Coutier > Liste de diffusion > Activit�", browseBar
        .getCellAsText(0, 0));
    assertEquals("Main", browseBar.getTableCell(0, 0).getLinkWith(
        "Liste de diffusion").getURLString());
    WebTable tableDescription = activityPage.getTableWithID("description");
    assertNotNull(tableDescription);
    String description = tableDescription.getCellAsText(1, 0);
    assertEquals("Liste de diffusion de test", description);
    WebTable tableAddress = activityPage.getTableWithID("subscribedAddress");
    assertNotNull(tableAddress);
    String subscribedAddress = tableAddress.getCellAsText(1, 0);
    assertEquals("thesimpsons@silverpeas.com", subscribedAddress);
    WebTable tableHistory = activityPage.getTableWithID("activities");
    assertNotNull(tableHistory);
    assertEquals("Historique des Messages", tableHistory.getCellAsText(0, 0));
    assertEquals("2007", tableHistory.getCellAsText(2, 0));
    assertEquals("", tableHistory.getCellAsText(2, 1));
    assertEquals("", tableHistory.getCellAsText(2, 2));
    assertEquals("", tableHistory.getCellAsText(2, 3));
    assertEquals("", tableHistory.getCellAsText(2, 4));
    assertEquals("", tableHistory.getCellAsText(2, 5));
    assertEquals("", tableHistory.getCellAsText(2, 6));
    assertEquals("", tableHistory.getCellAsText(2, 7));
    assertEquals("", tableHistory.getCellAsText(2, 8));
    assertEquals("", tableHistory.getCellAsText(2, 9));
    assertEquals("", tableHistory.getCellAsText(2, 10));
    assertEquals("2", tableHistory.getCellAsText(2, 11));
    assertEquals("2", tableHistory.getCellAsText(2, 12));
    assertEquals("1", tableHistory.getCellAsText(3, 1));
    assertEquals("3", tableHistory.getCellAsText(3, 2));
    assertEquals("3", tableHistory.getCellAsText(3, 3));
    assertEquals("", tableHistory.getCellAsText(3, 4));
    assertEquals("", tableHistory.getCellAsText(3, 5));
    assertEquals("", tableHistory.getCellAsText(3, 6));
    assertEquals("", tableHistory.getCellAsText(3, 7));
    assertEquals("", tableHistory.getCellAsText(3, 8));
    assertEquals("", tableHistory.getCellAsText(3, 9));
    assertEquals("", tableHistory.getCellAsText(3, 10));
    assertEquals("", tableHistory.getCellAsText(3, 11));
    assertEquals("", tableHistory.getCellAsText(3, 12));
    assertEquals("list/mailinglist45/currentYear/2007/currentMonth/11",
        tableHistory.getTableCell(2, 12).getLinkWith("2").getURLString());
    assertEquals("list/mailinglist45/currentYear/2008/currentMonth/2",
        tableHistory.getTableCell(3, 3).getLinkWith("3").getURLString());
    WebTable tableMessages = activityPage.getTableWithID("messages");
    assertNotNull(tableMessages);
    assertEquals("Message r�cents", tableMessages.getCellAsText(0, 0));
    assertEquals("Simple database message 3", tableMessages.getCellAsText(1, 0));
    assertEquals(MESSAGE_BASE + 3, tableMessages.getTableCell(1, 0)
        .getLinkWith("Simple database message 3").getURLString());
    assertEquals("bart.simpson@silverpeas.com - 02/03/2008 10:34:15",
        tableMessages.getCellAsText(2, 0));
    assertEquals(
        "Bonjour famille Simpson, j\'esp�re que vous allez bien. Ici "
            + "tout se passe bien et Krusty est tr�s sympathique. Surtout depuis que "
            + "Tahiti Bob est retourn� en prison. Je dois remplacer l\'homme canon "
            + "dans ...", tableMessages.getCellAsText(3, 0));
    assertEquals(MESSAGE_BASE + 3,
        tableMessages.getTableCell(3, 0).getLinks()[0].getURLString());
    assertEquals("Simple database message 4", tableMessages.getCellAsText(4, 0));
    assertEquals(MESSAGE_BASE + 4, tableMessages.getTableCell(4, 0)
        .getLinkWith("Simple database message 4").getURLString());
    assertEquals("bart.simpson@silverpeas.com - 02/03/2008 10:12:15",
        tableMessages.getCellAsText(5, 0));
    assertEquals(
        "Bonjour famille Simpson, j\'esp�re que vous allez bien. Ici "
            + "tout se passe bien et Krusty est tr�s sympathique. Surtout depuis que "
            + "Tahiti Bob est retourn� en prison. Je dois remplacer l\'homme canon "
            + "dans ...", tableMessages.getCellAsText(6, 0));
    assertEquals(MESSAGE_BASE + 4,
        tableMessages.getTableCell(6, 0).getLinks()[0].getURLString());
    assertEquals("Simple database message 1", tableMessages.getCellAsText(7, 0));
    assertEquals(MESSAGE_BASE + 1, tableMessages.getTableCell(7, 0)
        .getLinkWith("Simple database message 1").getURLString());
    assertEquals("bart.simpson@silverpeas.com - 01/03/2008 10:34:15",
        tableMessages.getCellAsText(8, 0));
    assertEquals(
        "Bonjour famille Simpson, j\'esp�re que vous allez bien. Ici "
            + "tout se passe bien et Krusty est tr�s sympathique. Surtout depuis que "
            + "Tahiti Bob est retourn� en prison. Je dois remplacer l\'homme canon "
            + "dans ...", tableMessages.getCellAsText(9, 0));
    assertEquals(MESSAGE_BASE + 1,
        tableMessages.getTableCell(9, 0).getLinks()[0].getURLString());
    assertEquals("Simple database message 11", tableMessages.getCellAsText(10,
        0));
    assertEquals(MESSAGE_BASE + 11, tableMessages.getTableCell(10, 0)
        .getLinkWith("Simple database message 11").getURLString());
    assertEquals("bart.simpson@silverpeas.com - 21/02/2008 10:34:15",
        tableMessages.getCellAsText(11, 0));
    assertEquals(
        "Bonjour famille Simpson, j\'esp�re que vous allez bien. Ici "
            + "tout se passe bien et Krusty est tr�s sympathique. Surtout depuis que "
            + "Tahiti Bob est retourn� en prison. Je dois remplacer l\'homme canon "
            + "dans ...", tableMessages.getCellAsText(12, 0));
    assertEquals(MESSAGE_BASE + 11, tableMessages.getTableCell(12, 0)
        .getLinks()[0].getURLString());
    WebTable tableTabbedPane = activityPage.getTableWithID("tabbedPane");
    assertNotNull(tableTabbedPane);
    tableTabbedPane = tableTabbedPane.getTableCell(0, 0).getTables()[0];
    assertNotNull(tableTabbedPane);
    assertEquals("Activit�", tableTabbedPane.getCellAsText(0, 2));
    assertEquals("Liste des Messages", tableTabbedPane.getCellAsText(0, 5));
    assertFalse(activityPage.getText().indexOf("Mod�ration") > 0);
    assertFalse(activityPage.getText().indexOf("Abonn�s Ext�rieurs") > 0);
  }

  public void testAdmin() throws Exception {
    WebConversation connection = new WebConversation();
    WebResponse loginPage = connection.getResponse(buildUrl("silverpeas/"));
    assertNotNull(loginPage);
    WebForm loginForm = loginPage.getFormWithName("EDform");
    assertNotNull(loginForm);
    loginForm.setParameter("Login", "bsimpson");
    loginForm.setParameter("Password", "bart");
    HttpUnitOptions.setScriptingEnabled(false);
    WebResponse welcomePage = loginForm.submit();
    assertNotNull(welcomePage);
    String[] frameNames = welcomePage.getFrameNames();
    assertEquals(5, frameNames.length);
    WebResponse navigationFrame = connection.getFrameContents("bottomFrame");
    assertNotNull(navigationFrame);
    navigationFrame = connection.getFrameContents("SpacesBar");
    assertNotNull(navigationFrame);
    WebLink mailingListLink = navigationFrame.getLinkWith("Liste de diffusion");
    assertNotNull(mailingListLink);
    HttpUnitOptions.setScriptingEnabled(false);
    WebResponse activityPage = connection.getResponse(buildUrl(mailingListLink
        .getURLString()));
    assertNotNull(activityPage);
    WebTable browseBar = activityPage.getTableWithID("browseBar");
    assertNotNull(browseBar);
    assertEquals("MGI Coutier > Liste de diffusion > Activit�", browseBar
        .getCellAsText(0, 0));
    assertEquals("Main", browseBar.getTableCell(0, 0).getLinkWith(
        "Liste de diffusion").getURLString());
    WebTable tableDescription = activityPage.getTableWithID("description");
    assertNotNull(tableDescription);
    String description = tableDescription.getCellAsText(1, 0);
    assertEquals("Liste de diffusion de test", description);
    WebTable tableAddress = activityPage.getTableWithID("subscribedAddress");
    assertNotNull(tableAddress);
    String subscribedAddress = tableAddress.getCellAsText(1, 0);
    assertEquals("thesimpsons@silverpeas.com", subscribedAddress);
    WebTable tableHistory = activityPage.getTableWithID("activities");
    assertNotNull(tableHistory);
    assertEquals("Historique des Messages", tableHistory.getCellAsText(0, 0));
    assertEquals("2007", tableHistory.getCellAsText(2, 0));
    assertEquals("", tableHistory.getCellAsText(2, 1));
    assertEquals("", tableHistory.getCellAsText(2, 2));
    assertEquals("", tableHistory.getCellAsText(2, 3));
    assertEquals("", tableHistory.getCellAsText(2, 4));
    assertEquals("", tableHistory.getCellAsText(2, 5));
    assertEquals("", tableHistory.getCellAsText(2, 6));
    assertEquals("", tableHistory.getCellAsText(2, 7));
    assertEquals("", tableHistory.getCellAsText(2, 8));
    assertEquals("", tableHistory.getCellAsText(2, 9));
    assertEquals("", tableHistory.getCellAsText(2, 10));
    assertEquals("2", tableHistory.getCellAsText(2, 11));
    assertEquals("2", tableHistory.getCellAsText(2, 12));
    assertEquals("1", tableHistory.getCellAsText(3, 1));
    assertEquals("3", tableHistory.getCellAsText(3, 2));
    assertEquals("3", tableHistory.getCellAsText(3, 3));
    assertEquals("", tableHistory.getCellAsText(3, 4));
    assertEquals("", tableHistory.getCellAsText(3, 5));
    assertEquals("", tableHistory.getCellAsText(3, 6));
    assertEquals("", tableHistory.getCellAsText(3, 7));
    assertEquals("", tableHistory.getCellAsText(3, 8));
    assertEquals("", tableHistory.getCellAsText(3, 9));
    assertEquals("", tableHistory.getCellAsText(3, 10));
    assertEquals("", tableHistory.getCellAsText(3, 11));
    assertEquals("", tableHistory.getCellAsText(3, 12));
    assertEquals("list/mailinglist45/currentYear/2007/currentMonth/11",
        tableHistory.getTableCell(2, 12).getLinkWith("2").getURLString());
    assertEquals("list/mailinglist45/currentYear/2008/currentMonth/2",
        tableHistory.getTableCell(3, 3).getLinkWith("3").getURLString());
    WebTable tableMessages = activityPage.getTableWithID("messages");
    assertNotNull(tableMessages);
    assertEquals("Message r�cents", tableMessages.getCellAsText(0, 0));
    assertEquals("Simple database message 3", tableMessages.getCellAsText(1, 0));
    assertEquals(MESSAGE_BASE + 3, tableMessages.getTableCell(1, 0)
        .getLinkWith("Simple database message 3").getURLString());
    assertEquals("bart.simpson@silverpeas.com - 02/03/2008 10:34:15",
        tableMessages.getCellAsText(2, 0));
    assertEquals(
        "Bonjour famille Simpson, j\'esp�re que vous allez bien. Ici "
            + "tout se passe bien et Krusty est tr�s sympathique. Surtout depuis que "
            + "Tahiti Bob est retourn� en prison. Je dois remplacer l\'homme canon "
            + "dans ...", tableMessages.getCellAsText(3, 0));
    assertEquals(MESSAGE_BASE + 3,
        tableMessages.getTableCell(3, 0).getLinks()[0].getURLString());
    assertEquals("Simple database message 4", tableMessages.getCellAsText(4, 0));
    assertEquals(MESSAGE_BASE + 4, tableMessages.getTableCell(4, 0)
        .getLinkWith("Simple database message 4").getURLString());
    assertEquals("bart.simpson@silverpeas.com - 02/03/2008 10:12:15",
        tableMessages.getCellAsText(5, 0));
    assertEquals(
        "Bonjour famille Simpson, j\'esp�re que vous allez bien. Ici "
            + "tout se passe bien et Krusty est tr�s sympathique. Surtout depuis que "
            + "Tahiti Bob est retourn� en prison. Je dois remplacer l\'homme canon "
            + "dans ...", tableMessages.getCellAsText(6, 0));
    assertEquals(MESSAGE_BASE + 4,
        tableMessages.getTableCell(6, 0).getLinks()[0].getURLString());
    assertEquals("Simple database message 1", tableMessages.getCellAsText(7, 0));
    assertEquals(MESSAGE_BASE + 1, tableMessages.getTableCell(7, 0)
        .getLinkWith("Simple database message 1").getURLString());
    assertEquals("bart.simpson@silverpeas.com - 01/03/2008 10:34:15",
        tableMessages.getCellAsText(8, 0));
    assertEquals(
        "Bonjour famille Simpson, j\'esp�re que vous allez bien. Ici "
            + "tout se passe bien et Krusty est tr�s sympathique. Surtout depuis que "
            + "Tahiti Bob est retourn� en prison. Je dois remplacer l\'homme canon "
            + "dans ...", tableMessages.getCellAsText(9, 0));
    assertEquals(MESSAGE_BASE + 1,
        tableMessages.getTableCell(9, 0).getLinks()[0].getURLString());
    assertEquals("Simple database message 11", tableMessages.getCellAsText(10,
        0));
    assertEquals(MESSAGE_BASE + 11, tableMessages.getTableCell(10, 0)
        .getLinkWith("Simple database message 11").getURLString());
    assertEquals("bart.simpson@silverpeas.com - 21/02/2008 10:34:15",
        tableMessages.getCellAsText(11, 0));
    assertEquals(
        "Bonjour famille Simpson, j\'esp�re que vous allez bien. Ici "
            + "tout se passe bien et Krusty est tr�s sympathique. Surtout depuis que "
            + "Tahiti Bob est retourn� en prison. Je dois remplacer l\'homme canon "
            + "dans ...", tableMessages.getCellAsText(12, 0));
    assertEquals(MESSAGE_BASE + 11, tableMessages.getTableCell(12, 0)
        .getLinks()[0].getURLString());
    WebTable tableTabbedPane = activityPage.getTableWithID("tabbedPane");
    assertNotNull(tableTabbedPane);
    tableTabbedPane = tableTabbedPane.getTableCell(0, 0).getTables()[0];
    assertNotNull(tableTabbedPane);
    assertEquals("Activit�", tableTabbedPane.getCellAsText(0, 2));
    assertEquals("Liste des Messages", tableTabbedPane.getCellAsText(0, 5));
    assertEquals("Mod�ration", tableTabbedPane.getCellAsText(0, 8));
    assertEquals("Abonn�s Ext�rieurs", tableTabbedPane.getCellAsText(0, 11));
  }

  protected String buildUrl(String path) {
    return "http://localhost:8000/" + path;
  }

  protected String[] getConfigLocations() {
    return new String[] { "spring-checker.xml", "spring-notification.xml",
        "spring-hibernate.xml", "spring-datasource.xml" };
  }

  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        TestMailingListActivity.class
            .getResourceAsStream("test-mailinglist-messages-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  protected void onSetUp() {
    super.onSetUp();
    HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
    HttpUnitOptions.setExceptionsThrownOnScriptError(false);
    HttpUnitOptions.setScriptingEnabled(true);
    ClientProperties.getDefaultProperties().setAcceptCookies(true);
    ClientProperties.getDefaultProperties().setAutoRedirect(true);
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
      DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected void onTearDown() {
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

}