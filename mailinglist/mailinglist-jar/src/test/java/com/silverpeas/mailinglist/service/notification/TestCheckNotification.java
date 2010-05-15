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

package com.silverpeas.mailinglist.service.notification;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.jms.QueueConnectionFactory;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.Reference;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.jvnet.mock_javamail.Mailbox;

import com.mockrunner.mock.jms.MockQueue;
import com.silverpeas.mailinglist.AbstractSilverpeasDatasourceSpringContextTests;
import com.silverpeas.mailinglist.jms.MockObjectFactory;
import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.InternalUser;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public class TestCheckNotification extends
    AbstractSilverpeasDatasourceSpringContextTests {

  private NotificationHelperImpl notificationHelper;

  public NotificationHelperImpl getNotificationHelper() {
    return notificationHelper;
  }

  public void setNotificationHelper(NotificationHelperImpl notificationHelper) {
    this.notificationHelper = notificationHelper;
  }

  protected String[] getConfigLocations() {
    return new String[] { "spring-checker.xml", "spring-notification.xml",
        "spring-hibernate.xml", "spring-datasource.xml" };
  }

  protected void registerMockJMS() throws Exception {
    InitialContext ic = new InitialContext();
    // Construct BasicDataSource reference
    Reference refFactory = new Reference("javax.jms.QueueConnectionFactory",
        "com.silverpeas.mailinglist.jms.MockObjectFactory", null);
    ic.rebind(JNDINames.JMS_FACTORY, refFactory);
    Reference refQueue = new Reference("javax.jms.Queue",
        "com.silverpeas.mailinglist.jms.MockObjectFactory", null);
    ic.rebind(JNDINames.JMS_QUEUE, refQueue);
    QueueConnectionFactory qconFactory = (QueueConnectionFactory) ic
        .lookup(JNDINames.JMS_FACTORY);
    assertNotNull(qconFactory);
    MockQueue queue = (MockQueue) ic.lookup(JNDINames.JMS_QUEUE);
    queue.clear();
  }

  @SuppressWarnings("unchecked")
  public void testNotifyArchivageNotModeratedOpen() throws Exception {
    registerMockJMS();
    MailingList list = ServicesFactory.getMailingListService().findMailingList(
        "101");
    Message message = ServicesFactory.getMessageService().getMessage("701");
    assertNotNull(message);
    assertNotNull(list);
    assertFalse(list.isModerated());
    assertTrue(list.isOpen());
    assertFalse(list.isNotify());
    assertEquals(
        "Liste archivage non modérée et ouverte avec un lecteur abonné", list
            .getName());
    assertEquals("thesimpsons@silverpeas.com", list.getSubscribedAddress());
    assertNotNull(list.getInternalSubscribers());
    assertEquals(1, list.getInternalSubscribers().size());
    assertNotNull(list.getReaders());
    assertEquals(1, list.getReaders().size());
    InternalUser reader = (InternalUser) list.getReaders().iterator().next();
    assertEquals("maggie.simpson@silverpeas.com", reader.getEmail());
    assertNotNull(list.getModerators());
    assertEquals(1, list.getModerators().size());
    InternalUser moderator = (InternalUser) list.getModerators().iterator().next();
    assertEquals("bart.simpson@silverpeas.com", moderator.getEmail());
    assertNotNull(list.getExternalSubscribers());
    assertEquals(0, list.getExternalSubscribers().size());
    assertNotNull(list.getGroupSubscribers());
    assertEquals(0, list.getGroupSubscribers().size());
    notificationHelper.notify(message, list);
    List<TextMessage> messages = MockObjectFactory
        .getMessages(JNDINames.JMS_QUEUE);
    assertNotNull(messages);
    assertEquals(0, messages.size());
  }

  @SuppressWarnings("unchecked")
  public void testNotifyArchivageModeratedOpen() throws Exception {
    registerMockJMS();
    MailingList list = ServicesFactory.getMailingListService().findMailingList(
        "102");
    Message message = ServicesFactory.getMessageService().getMessage("702");
    assertNotNull(message);
    assertNotNull(list);
    assertTrue(list.isModerated());
    assertFalse(list.isOpen());
    assertFalse(list.isNotify());
    assertEquals(
        "Liste archivage modérée et fermée avec un lecteur abonné", list
            .getName());
    assertEquals("thesimpsons@silverpeas.com", list.getSubscribedAddress());
    assertNotNull(list.getInternalSubscribers());
    assertEquals(1, list.getInternalSubscribers().size());
    assertNotNull(list.getReaders());
    assertEquals(1, list.getReaders().size());
    InternalUser reader = (InternalUser) list.getReaders().iterator().next();
    assertEquals("lisa.simpson@silverpeas.com", reader.getEmail());
    assertNotNull(list.getModerators());
    assertEquals(1, list.getModerators().size());
    InternalUser moderator = (InternalUser) list.getModerators().iterator().next();
    assertEquals("bart.simpson@silverpeas.com", moderator.getEmail());
    assertNotNull(list.getExternalSubscribers());
    assertEquals(0, list.getExternalSubscribers().size());
    assertNotNull(list.getGroupSubscribers());
    assertEquals(0, list.getGroupSubscribers().size());
    notificationHelper.notify(message, list);
    List<TextMessage> messages = MockObjectFactory
        .getMessages(JNDINames.JMS_QUEUE);
    assertNotNull(messages);
    assertEquals(1, messages.size());
    for (TextMessage alert : messages) {
      assertNotNull(alert.getText());
      NotificationData data = NotificationServerUtil
          .convertXMLToNotificationData(alert.getText());
      assertNotNull(data);
      String channel = data.getTargetChannel();
      assertEquals("SMTP", channel);
      String recipient = data.getTargetReceipt();
      assertNotNull(recipient);
      assertTrue("Erreur destinataire " + recipient,
          "lisa.simpson@silverpeas.com".equals(recipient));
      assertEquals(message.getSummary(), data.getMessage());
      String url = (String) data.getTargetParam().get("URL");
      assertNotNull(url);
      assertEquals(
          "http://localhost:8000/silverpeas//autoRedirect.jsp?domainId=0&"
              + "goto=%2FRmailinglist%2F102%2Fmessage%2F702", url);
      String source = (String) data.getTargetParam().get("SOURCE");
      assertNotNull(source);
      assertEquals("thesimpsons@silverpeas.com", source);
    }
  }


  protected void onTearDown() {
    Mailbox.clearAll();
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
      FileFolderManager.deleteFolder("c:\\tmp\\uploads\\componentId", false);
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

  protected void onSetUp() {
    Mailbox.clearAll();
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

  protected IDataSet getDataSet() throws DataSetException, IOException {
    if (isOracle()) {
      return new FlatXmlDataSet(TestCheckNotification.class
          .getResourceAsStream("test-check-notification-dataset.xml"));
    }
    return new FlatXmlDataSet(TestCheckNotification.class
        .getResourceAsStream("test-check-notification-dataset.xml"));
  }
}