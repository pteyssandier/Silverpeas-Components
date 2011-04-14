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
package com.stratelia.silverpeas.connecteurJDBC.control;

import com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail;
import com.stratelia.webactiv.persistence.PersistenceException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Collections;
import com.stratelia.silverpeas.connecteurJDBC.control.ejb.ConnecteurJDBCBm;
import com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoPK;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.ArrayList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class ConnecteurJDBCSessionControllerTest {

  public ConnecteurJDBCSessionControllerTest() {
  }

  private ConnecteurJDBCSessionController getNewInstance() {
    MainSessionController controller = mock(MainSessionController.class);
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("connecteurJDBC10");
    return new ConnecteurJDBCSessionController(controller, context) {

      @Override
      protected ConnecteurJDBCBm getConnecteurJDBCBm() throws ConnecteurJDBCException {
        try {
          ConnecteurJDBCBm ejb = mock(ConnecteurJDBCBm.class);
          List<ConnecteurJDBCConnectionInfoDetail> connecteurs = new ArrayList<ConnecteurJDBCConnectionInfoDetail>();
          when(ejb.getConnectionList(any(ConnecteurJDBCConnectionInfoPK.class))).thenReturn(
              connecteurs);
          return ejb;
        } catch (RemoteException ex) {
          throw new ConnecteurJDBCException("ConnecteurJDBCSessionController",
              SilverpeasException.ERROR, "", ex);
        } catch (PersistenceException ex) {
          throw new ConnecteurJDBCException("ConnecteurJDBCSessionController",
              SilverpeasException.ERROR, "", ex);
        }
      }
    };
  }

  private ConnecteurJDBCSessionController getExistingInstance() {
    MainSessionController controller = mock(MainSessionController.class);
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("connecteurJDBC10");
    return new ConnecteurJDBCSessionController(controller, context) {

      @Override
      protected ConnecteurJDBCBm getConnecteurJDBCBm() throws ConnecteurJDBCException {
        try {
          ConnecteurJDBCBm ejb = mock(ConnecteurJDBCBm.class);
          ConnecteurJDBCConnectionInfoDetail detail = new ConnecteurJDBCConnectionInfoDetail();
          detail.setInstanceId("connecteurJDBC10");
          detail.setRowLimit(10);
          List<ConnecteurJDBCConnectionInfoDetail> connecteurs = Collections.singletonList(detail);
          when(ejb.getConnectionList(any(ConnecteurJDBCConnectionInfoPK.class))).thenReturn(
              connecteurs);
          return ejb;
        } catch (RemoteException ex) {
          throw new ConnecteurJDBCException("ConnecteurJDBCSessionController",
              SilverpeasException.ERROR, "", ex);
        } catch (PersistenceException ex) {
          throw new ConnecteurJDBCException("ConnecteurJDBCSessionController",
              SilverpeasException.ERROR, "", ex);
        }
      }
    };
  }

  /**
   * Test of isConnectionConfigured method, of class ConnecteurJDBCSessionController.
   */
  @org.junit.Test
  public void testIsConnectionConfigured() {
    System.out.println("isConnectionConfigured");
    ConnecteurJDBCSessionController instance = getNewInstance();
    boolean result = instance.isConnectionConfigured();
    assertFalse(result);    
    instance = getExistingInstance();
    result = instance.isConnectionConfigured();
    assertTrue(result);
   
  }

  /**
   * Test of setJDBCdriverName method, of class ConnecteurJDBCSessionController.
   */
  @org.junit.Test
  public void testSetJDBCdriverName() throws Exception {
    String JDBCdriverName = "JDBC driver for Postgres";
    ConnecteurJDBCSessionController instance = getNewInstance();
    assertNull(instance.getJDBCdriverName());
    instance.setJDBCdriverName(JDBCdriverName);
    assertEquals("", instance.getJDBCdriverName());
  }

  /**
   * Test of setJDBCurl method, of class ConnecteurJDBCSessionController.
   */
  @org.junit.Test
  public void testSetJDBCurl() throws Exception {
    String JDBCurl = "jdbc:postgresql://localhost:5432/SilverpeasV5";
    ConnecteurJDBCSessionController instance = getNewInstance();
    assertNull(instance.getJDBCurl());
    instance.setJDBCurl(JDBCurl);
    assertEquals(JDBCurl, instance.getJDBCurl());
  }

  /**
   * Test of setSQLreq method, of class ConnecteurJDBCSessionController.
   */
  @org.junit.Test
  public void testSetSQLreq() throws Exception {
    String SQLreq = "SELECT * FROM sc_connecteurjdbc_connectinfo";
    ConnecteurJDBCSessionController instance = getNewInstance();
    assertEquals(null, instance.getSQLreq());
    instance.setSQLreq(SQLreq);
    assertEquals(SQLreq, instance.getSQLreq());
  }

  /**
   * Test of setLogin method, of class ConnecteurJDBCSessionController.
   */
  @org.junit.Test
  public void testSetLogin() throws Exception {
    String login = "monLogin";
    ConnecteurJDBCSessionController instance = getNewInstance();
    assertEquals("", instance.getLogin());
    instance.setLogin(login);
    assertEquals(login, instance.getLogin());
  }

  /**
   * Test of setPassword method, of class ConnecteurJDBCSessionController.
   */
  @org.junit.Test
  public void testSetPassword() throws Exception {
    String password = "myPassword";
    ConnecteurJDBCSessionController instance = getNewInstance();
    assertEquals("", instance.getPassword());
    instance.setPassword(password);
    assertEquals(password, instance.getPassword());
  }
  /**
   * Test of getRowLimit method, of class ConnecteurJDBCSessionController.
   */
  @org.junit.Test
  public void testGetRowLimit() {
    ConnecteurJDBCSessionController instance = getNewInstance();
    int expResult = -1;
    int result = instance.getRowLimit();
    assertEquals(expResult, result);
    instance = getExistingInstance();
    expResult = 10;
    result = instance.getRowLimit();
    assertEquals(expResult, result);
  }

  /**
   * Test of getDriversDescriptions method, of class ConnecteurJDBCSessionController.
   */
  @org.junit.Test
  public void testGetDriversDescriptions() {
    ConnecteurJDBCSessionController instance = getNewInstance();
    List<String> expResult = new ArrayList<String>();
    expResult.add("Standard JDBC driver for MS SQL Server");
    expResult.add("Standard JDBC driver for Oracle");
    expResult.add("Standard JDBC driver for Postgres");
    expResult.add("Standard JDBC-ODBC driver");
    List<String> result = (List<String>) instance.getDriversDescriptions();
    assertNotNull(result);
    assertEquals(4, result.size());
    assertArrayEquals(expResult.toArray(new String[4]), result.toArray(new String[4]));
  }

  /**
   * Test of getAvailableDriversNames method, of class ConnecteurJDBCSessionController.
   */
  @org.junit.Test
  public void testGetAvailableDriversNames() {
    ConnecteurJDBCSessionController instance = getNewInstance();
    List<String> expResult = new ArrayList<String>();
    expResult.add("StandardJDBCDriver");
    expResult.add("StandardJDBCDriverOracle");
    expResult.add("StandardJDBCDriverPostgres");
    expResult.add("StandardJDBCODBCDriver");
    List<String> result = (List<String>) instance.getAvailableDriversNames();
    assertNotNull(result);
    assertEquals(4, result.size());
    assertArrayEquals(expResult.toArray(new String[4]), result.toArray(new String[4]));
  }

  /**
   * Test of getAvailableDriversDisplayNames method, of class ConnecteurJDBCSessionController.
   */
  @org.junit.Test
  public void testGetAvailableDriversDisplayNames() {
    ConnecteurJDBCSessionController instance = getNewInstance();
    List<String> expResult = new ArrayList<String>();
    expResult.add("Standard JDBC driver for MS SQL Server");
    expResult.add("Standard JDBC driver for Oracle");
    expResult.add("Standard JDBC driver for Postgres");
    expResult.add("Standard JDBC-0DBC driver");
    List<String> result = (List<String>) instance.getAvailableDriversDisplayNames();
    assertNotNull(result);
    assertEquals(4, result.size());
    assertArrayEquals(expResult.toArray(new String[4]), result.toArray(new String[4]));
  }

  /**
   * Test of getJDBCUrlsForDriver method, of class ConnecteurJDBCSessionController.
   */
  @org.junit.Test
  public void testGetJDBCUrlsForDriver() {
    ConnecteurJDBCSessionController instance = getNewInstance();
    List<String> expResult = new ArrayList<String>();
    expResult.add("Standard JDBC driver for MS SQL Server");
    expResult.add("Standard JDBC driver for Oracle");
    expResult.add("Standard JDBC driver for Postgres");
    expResult.add("Standard JDBC-0DBC driver");
    List<String> result = (List<String>) instance.getAvailableDriversDisplayNames();
    assertNotNull(result);
    assertEquals(4, result.size());
    assertArrayEquals(expResult.toArray(new String[4]), result.toArray(new String[4]));
  }

  /**
   * Test of getDescriptionForDriver method, of class ConnecteurJDBCSessionController.
   */
  @org.junit.Test
  public void testGetDescriptionForDriver() {
    String driverName = "StandardJDBCDriverPostgres";
    ConnecteurJDBCSessionController instance = getNewInstance();
    String expResult = "Standard JDBC driver for Postgres";
    String result = instance.getDescriptionForDriver(driverName);
    assertEquals(expResult, result);
  }

  /**
   * Test of updateConnection method, of class ConnecteurJDBCSessionController.
   */
  @org.junit.Test
  public void testUpdateConnection() throws Exception {
    System.out.println("updateConnection");
    String JDBCdriverName = "StandardJDBCDriverPostgres";
    String JDBCurl = "jdbc:postgresql://localhost:5432/SilverpeasV5";
    String login = "silver";
    String password = "silver";
    int rowLimit = 20;
    ConnecteurJDBCSessionController instance = getExistingInstance();
    instance.updateConnection(JDBCdriverName, JDBCurl, login, password, rowLimit);
    assertEquals(password, instance.getPassword());
    assertEquals(login, instance.getLogin());
    assertEquals(rowLimit, instance.getRowLimit());
    assertEquals(JDBCurl, instance.getJDBCurl());
    assertArrayEquals(new String[]{"jdbc:postgresql://localhost:5432/Silverpeas"}, 
        instance.getJDBCUrlsForDriver(JDBCdriverName).toArray(new String[1]));   
  }


}
