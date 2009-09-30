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
package com.stratelia.silverpeas.connecteurJDBC.control;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Vector;

import javax.ejb.RemoveException;

import com.stratelia.silverpeas.connecteurJDBC.control.ejb.ConnecteurJDBCBm;
import com.stratelia.silverpeas.connecteurJDBC.control.ejb.ConnecteurJDBCBmHome;
import com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoDetail;
import com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCConnectionInfoPK;
import com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCRuntimeException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBmHome;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.XMLConfigurationStore;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Title: Connecteur JDBC Description: Ce composant a pour objet de permettre de
 * r�cup�rer rapidement et simplement des donn�es du syst�me d'information de
 * l'entreprise. Copyright: Copyright (c) 2001 Company: Strat�lia
 * 
 */

public class ConnecteurJDBCSessionController extends
    AbstractComponentSessionController {

  private ConnecteurJDBCBm connecteurJDBCEjb = null;
  private SearchEngineBm searchEngineEjb = null;

  private String table = null;
  private String column = null;
  private String selected = null;
  private String lastColumns = "";
  private String columnReq = "*";
  private String compare = "*";
  private String columnValue = "";
  private String validreq = "";
  private String fullreq = "";
  private String sort_Type = "";

  private boolean connectionOpened = false;

  private ConnecteurJDBCConnectionInfoDetail connecteurJDBCDetail = null;

  private String[] driversNames;
  private String[] driversDisplayNames;
  private Vector driversUrls;
  private String[] driversClassNames;
  private String[] driversDescriptions;
  private String[] tableNames = null;
  private String[] columnNames = null;

  ResultSet tables_rs;
  ResultSetMetaData tables_rsmd;
  private Driver driver = null; // current driver
  private ResultSet rs;
  private DatabaseMetaData dbMetaData;
  private int rowNumber; // Current row number

  private Vector selectedColumn = new Vector(); // the columns selected in the

  // SQL request

  /**
   * Constructeur
   * 
   */
  public ConnecteurJDBCSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) throws ConnecteurJDBCRuntimeException {
    super(mainSessionCtrl, componentContext,
        "com.stratelia.silverpeas.connecteurJDBC.multilang.connecteurJDBC");
    loadDrivers();
    initConnecteur();
  }

  private ConnecteurJDBCBm getConnecteurJDBCBm() throws ConnecteurJDBCException {
    if ((this.connecteurJDBCEjb == null)) {
      try {
        ConnecteurJDBCBmHome connecteurJDBCEjbHome = (ConnecteurJDBCBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.CONNECTEURJDBC_EJBHOME,
                ConnecteurJDBCBmHome.class);
        connecteurJDBCEjb = connecteurJDBCEjbHome.create();

      } catch (Exception e) {
        throw new ConnecteurJDBCException(
            "connecteurJDBCSessionControl.getConnecteurJDBCBm()",
            SilverpeasException.ERROR, "connecteurJDBC.EX_EJB_CREATION_FAIL", e);
      }
    }
    return connecteurJDBCEjb;
  }

  public SearchEngineBm getSearchEngine() throws ConnecteurJDBCException {
    if (this.searchEngineEjb == null) {
      try {
        SearchEngineBmHome home = (SearchEngineBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.SEARCHBM_EJBHOME,
                SearchEngineBmHome.class);
        this.searchEngineEjb = home.create();
      } catch (Exception e) {
        throw new ConnecteurJDBCException(
            "connecteurJDBCSessionControl.getsearchEngine()",
            SilverpeasException.ERROR,
            "connecteurJDBC.EX_CANT_GET_SEARCHENGINE", e);
      }
    }
    return this.searchEngineEjb;
  }

  /****************************************************************/
  /*
   * public inteface for the jsp page
   * /***************************************************************
   */

  private void initConnecteur() throws ConnecteurJDBCRuntimeException {
    if (connecteurJDBCDetail == null)
      try {
        // load and return the current jdbc settings for the component
        ConnecteurJDBCBm connecteurJDBCBm = getConnecteurJDBCBm();
        Collection c = connecteurJDBCBm
            .getConnectionList(new ConnecteurJDBCConnectionInfoPK("",
                getSpaceId(), getComponentId()));
        if (c.size() > 1)
          throw new ConnecteurJDBCRuntimeException(
              "connecteurJDBCSessionControl.initConnecteur()",
              SilverpeasException.FATAL,
              "connecteurJDBC.EX_THERE_MUST_BE_ONLY_ONE_CONECTION");

        Iterator i = c.iterator();
        if (i.hasNext()) {
          connecteurJDBCDetail = (ConnecteurJDBCConnectionInfoDetail) i.next();
        }
      } catch (Exception e) {
        throw new ConnecteurJDBCRuntimeException(
            "connecteurJDBCSessionControl.initConnecteur()",
            SilverpeasException.FATAL,
            "connecteurJDBC.EX_INIT_CONNECTEUR_FAIL", e);
      }
  }

  public void closeConnection() {
    if (connectionOpened)
      try {
        if (rs != null)
          rs.close();
        connectionOpened = false;
      } catch (SQLException e) {
        SilverTrace.warn("connecteurJDBC",
            "ConnecteurJDBCSessionControl.closeConnection()",
            "connecteurJDBC.MSG_CONNECTION_NOT_CLOSED", null, e);

      }

  }

  public void startConnection() {
    String temp = getSQLreq();
    SilverTrace.info("connecteurJDBC",
        "ConnecteurJDBCSessionController.startConnection()",
        "root.MSG_GEN_ENTER_METHOD", "temp : " + temp);
    rowNumber = 0;

    if (!connectionOpened && (connecteurJDBCDetail != null)) {
      try {
        Properties info = new Properties();
        info.setProperty("user", getLogin());
        info.setProperty("password", getPassword());
        Connection con = getDriver().connect(getJDBCurl(), info);
        dbMetaData = con.getMetaData();
        Statement stmt = con.createStatement();
        if ((temp == null) || (temp.equals(""))) {
          rs = null;
        } else
          rs = stmt.executeQuery(temp);
        connectionOpened = true;
      } catch (SQLException e) {
        SilverTrace.warn("connecteurJDBC",
            "ConnecteurJDBCSessionControl.startConnection()",
            "connecteurJDBC.MSG_CONNECTION_NOT_STARTED", "request : "
                + getSQLreq(), e);
      }
    }

  }

  public void startConnection(String temp) {
    SilverTrace.info("connecteurJDBC",
        "ConnecteurJDBCSessionController.startConnection()",
        "root.MSG_GEN_ENTER_METHOD", "temp : " + temp);
    rowNumber = 0;

    if (!connectionOpened && (connecteurJDBCDetail != null)) {
      try {
        Properties info = new Properties();
        info.setProperty("user", getLogin());
        info.setProperty("password", getPassword());
        Connection con = getDriver().connect(getJDBCurl(), info);
        dbMetaData = con.getMetaData();
        Statement stmt = con.createStatement();
        if ((temp == null) || (temp.equals(""))) {
          rs = null;
        } else
          rs = stmt.executeQuery(temp);
        connectionOpened = true;
      } catch (Exception e) {
        SilverTrace.warn("connecteurJDBC",
            "ConnecteurJDBCSessionControl.startConnection()",
            "connecteurJDBC.MSG_CONNECTION_NOT_STARTED", "request : "
                + getSQLreq(), e);
      }
    }

  }

  /**
   * set the valide request
   */
  public void setValidRequest(String validreq) {
    SilverTrace.info("connecteurJDBC",
        "ConnecteurJDBCSessionController.setValidRequest()",
        "root.MSG_GEN_ENTER_METHOD", "validreq : " + validreq);
    this.validreq = validreq;
  }

  /**
   * get the last valide request
   */
  public String getLastValidRequest() {
    return validreq;
  }

  /**
   * set the full request
   */
  public void setFullRequest(String fullreq) {
    SilverTrace.info("connecteurJDBC",
        "ConnecteurJDBCSessionController.setFullRequest()",
        "root.MSG_GEN_ENTER_METHOD", "fullreq : " + fullreq);
    this.fullreq = fullreq;
  }

  /**
   * get the full request
   */
  public String getFullRequest() {
    if (fullreq == null || "".equals(fullreq))
      return getSQLreq();
    else
      return fullreq;
  }

  /**
   * set the sort type (asc or desc) of the request
   */
  public void setSortType(String sort_Type) {
    this.sort_Type = sort_Type;
  }

  /**
   * get the sort type asc or desc
   */
  public String getSortType() {
    return sort_Type;
  }

  /**
   * get the liste of the table names
   */
  public String[] getTableNames() throws ConnecteurJDBCException {
    startConnection();
    Vector tableVector = new Vector();
    try {
      tables_rs = dbMetaData.getTables(null, null, null, null);
      tables_rsmd = tables_rs.getMetaData();
      while (tables_rs.next()) {
        tableVector.addElement(tables_rs.getString("TABLE_NAME"));
      }
      int n = tableVector.size();
      tableNames = new String[n];
      for (int i = 0; i < n; i++) {
        tableNames[i] = (String) tableVector.elementAt(i);
      }
    } catch (Exception e) {
      throw new ConnecteurJDBCException(
          "connecteurJDBCSessionControl.getTableNames()",
          SilverpeasException.ERROR, "connecteurJDBC.EX_CANT_GET_TABLES_NAMES",
          e);
    }
    closeConnection();
    return tableNames;
  }

  /**
   * get the table corresponding to the tableName
   */
  private ResultSet getTable(String tableName) throws ConnecteurJDBCException {
    ResultSet resultset = null;
    try {
      resultset = dbMetaData.getColumns(null, null, tableName, null);
    } catch (SQLException e) {
      throw new ConnecteurJDBCException(
          "connecteurJDBCSessionControl.getTable()", SilverpeasException.ERROR,
          "connecteurJDBC.EX_GET_TABLE_FAIL", "from table : " + tableName, e);
    }
    return resultset;
  }

  /**
   * get the columns names corrsponding to a table name
   */
  public String[] getColumnNames(String tableName)
      throws ConnecteurJDBCException {
    ResultSet table_rs = getTable(tableName);
    Vector columnVector = new Vector();
    try {
      while (table_rs.next()) {
        columnVector.addElement(table_rs.getString("COLUMN_NAME"));
      }
    } catch (SQLException e) {
      throw new ConnecteurJDBCException(
          "connecteurJDBCSessionControl.getTable()", SilverpeasException.ERROR,
          "connecteurJDBC.EX_GET_COLUMNS_NAMES_FAIL", "from table : "
              + tableName, e);
    }
    int n = columnVector.size();
    columnNames = new String[n];
    for (int i = 0; i < n; i++) {
      columnNames[i] = (String) columnVector.elementAt(i);
    }
    return columnNames;
  }

  /**
   * set the table of the SQL request
   */
  public void setTable(String table) {
    this.table = table;
  }

  /**
   * get the table of the SQL request
   */
  public String getTable() {
    return table;
  }

  /**
   * set the column of the SQL request
   */
  public void setColumn(String column) {
    this.column = column;
  }

  /**
   * get the column of the SQL request
   */
  public String getColumn() {
    return column;
  }

  public void setSelectedColumn(Vector selectedColumn) {
    this.selectedColumn = selectedColumn;
  }

  public Vector getSelectedColumn() {
    return selectedColumn;
  }

  public void setSelected(String selected) {
    this.selected = selected;
  }

  public String getSelected() {
    return selected;
  }

  public void setLastColumn(String lastColumns) {
    this.lastColumns = lastColumns;
  }

  public String getLastColumn() {
    return lastColumns;
  }

  /**
   * get the column selected to restrict the request results in the page
   * connecteurJDBC.jsp
   */
  public String getColumnReq() {
    return columnReq;
  }

  /**
   * set the column selected to restrict the request results
   */
  public void setColumnReq(String columnReq) {
    this.columnReq = columnReq;
  }

  /**
   * get the symbole of comparison
   */
  public String getCompare() {
    return compare;
  }

  /**
   * set the symbole of comparaison
   */
  public void setCompare(String compare) {
    this.compare = compare;
  }

  /**
   * get the value with wich a column is compared to restrict the sql request
   * results
   */
  public String getColumnValue() {
    return columnValue;
  }

  /**
   * set the value with wich a column is compared
   */
  public void setColumnValue(String columnValue) {
    this.columnValue = columnValue;
  }

  /**
   * get the number of the columns of the SQL request results
   */
  public int getColumnCount() {
    int ret = 0;
    if (rs != null)
      try {
        ResultSetMetaData md = rs.getMetaData();
        if (md == null) {
          ret = 1; // I assume that there is at least 1 column.
        } else {
          ret = md.getColumnCount();
        }
      } catch (SQLException e) {
        SilverTrace.warn("connecteurJDBC",
            "ConnecteurJDBCSessionControl.getColumnCount()",
            "connecteurJDBC.MSG_CANT_GET_Column_Count", null, e);
      }
    return ret;
  }

  /**
   * get the name of the column number i
   */
  public String getColumnName(int i) {
    String ret = "";
    if (rs != null)
      try {
        ResultSetMetaData md = rs.getMetaData();
        if (md == null) {
          ret = "Col 1";
        } else {
          ret = md.getColumnName(i);
        }
      } catch (SQLException e) {
        SilverTrace.warn("connecteurJDBC",
            "ConnecteurJDBCSessionControl.getColumnName()",
            "connecteurJDBC.MSG_CANT_GET_COLUMN_NAME", "column number : " + i,
            e);
      }
    return ret;
  }

  /**
   * get the type of the column number i
   */
  public String getColumnType(int i) {
    String ret = "";
    if (rs != null)
      try {
        ResultSetMetaData md = rs.getMetaData();
        if (md == null) {
          ret = "Col 1";
        } else {
          ret = md.getColumnTypeName(i);
        }
      } catch (SQLException e) {
        SilverTrace.warn("connecteurJDBC",
            "ConnecteurJDBCSessionControl.getColumnType()",
            "connecteurJDBC.MSG_CANT_GET_COLUMN_TYPE", "column number : " + i,
            e);
      }
    return ret;
  }

  public boolean getNext() {
    boolean ret = false;
    if (rs != null) {
      if (getRowLimit() == 0 || rowNumber < getRowLimit()) {
        rowNumber++;
        try {
          ret = rs.next();
        } catch (SQLException e) {
          SilverTrace.warn("connecteurJDBC",
              "ConnecteurJDBCSessionControl.getNext()",
              "connecteurJDBC.MSG_CANT_GET_NEXT_ROW", null, e);
        }
      }
    }
    return ret;
  }

  public String getString(int i) {
    String ret = "";
    if (rs != null)
      try {
        ret = rs.getString(i);
      } catch (SQLException e) {
        SilverTrace.warn("connecteurJDBC",
            "ConnecteurJDBCSessionControl.getString()",
            "connecteurJDBC.MSG_CANT_GET_COLUMN_VALUE", null, e);
      }
    return ret;
  }

  public boolean isConnectionConfigured() {
    return (connecteurJDBCDetail != null);
  }

  // setters (� utiliser via les jsp)
  public void setJDBCdriverName(String JDBCdriverName)
      throws ConnecteurJDBCException {

    if (connecteurJDBCDetail == null) {
      connecteurJDBCDetail = new ConnecteurJDBCConnectionInfoDetail();
      connecteurJDBCDetail.setPK(new ConnecteurJDBCConnectionInfoPK("",
          getSpaceId(), getComponentId()));
      newConnecteur();
    }

    updateConnecteur();
  }

  public void setJDBCurl(String JDBCurl) throws ConnecteurJDBCException {
    if (connecteurJDBCDetail == null) {
      connecteurJDBCDetail = new ConnecteurJDBCConnectionInfoDetail();
      connecteurJDBCDetail.setPK(new ConnecteurJDBCConnectionInfoPK("",
          getSpaceId(), getComponentId()));
      newConnecteur();
    }
    if (connecteurJDBCDetail != null)
      connecteurJDBCDetail.setJDBCurl(JDBCurl);
    updateConnecteur();
  }

  public void setSQLreq(String SQLreq) throws ConnecteurJDBCException {
    if (connecteurJDBCDetail == null) {
      connecteurJDBCDetail = new ConnecteurJDBCConnectionInfoDetail();
      connecteurJDBCDetail.setPK(new ConnecteurJDBCConnectionInfoPK("",
          getSpaceId(), getComponentId()));
      newConnecteur();
    }
    if (connecteurJDBCDetail != null)
      connecteurJDBCDetail.setSQLreq(SQLreq);
    updateConnecteur();
  }

  public void setLogin(String login) throws ConnecteurJDBCException {
    if (connecteurJDBCDetail == null) {
      connecteurJDBCDetail = new ConnecteurJDBCConnectionInfoDetail();
      connecteurJDBCDetail.setPK(new ConnecteurJDBCConnectionInfoPK("",
          getSpaceId(), getComponentId()));
      newConnecteur();
    }
    if (connecteurJDBCDetail != null)
      connecteurJDBCDetail.setLogin(login);
    updateConnecteur();
  }

  public void setPassword(String password) throws ConnecteurJDBCException {
    if (connecteurJDBCDetail == null) {
      connecteurJDBCDetail = new ConnecteurJDBCConnectionInfoDetail();
      connecteurJDBCDetail.setPK(new ConnecteurJDBCConnectionInfoPK("",
          getSpaceId(), getComponentId()));
      newConnecteur();
    }
    if (connecteurJDBCDetail != null)
      connecteurJDBCDetail.setPassword(password);
    updateConnecteur();
  }

  public void setRowLimit(int rowLimit) throws ConnecteurJDBCException {
    if (connecteurJDBCDetail == null) {
      connecteurJDBCDetail = new ConnecteurJDBCConnectionInfoDetail();
      connecteurJDBCDetail.setPK(new ConnecteurJDBCConnectionInfoPK("",
          getSpaceId(), getComponentId()));
      newConnecteur();
    }
    if (connecteurJDBCDetail != null)
      connecteurJDBCDetail.setRowLimit(rowLimit);
    updateConnecteur();
  }

  // getters
  public String getJDBCdriverName() {
    if (connecteurJDBCDetail != null)
      return (connecteurJDBCDetail.getJDBCdriverName());
    else
      return (null);
  }

  public String getJDBCurl() {
    if (connecteurJDBCDetail != null)
      return (connecteurJDBCDetail.getJDBCurl());
    else
      return (null);
  }

  public String getSQLreq() {
    if (connecteurJDBCDetail != null)
      return (connecteurJDBCDetail.getSQLreq());
    else
      return (null);

  }

  public String getLogin() {
    if (connecteurJDBCDetail != null)
      return (connecteurJDBCDetail.getLogin());
    else
      return ("");

  }

  public String getPassword() {
    if (connecteurJDBCDetail != null)
      return (connecteurJDBCDetail.getPassword());
    else
      return ("");

  }

  public int getRowLimit() {
    if (connecteurJDBCDetail != null)
      return (connecteurJDBCDetail.getRowLimit());
    else
      return (-1);
  }

  private int searchDriverIndice(String driverName) {
    SilverTrace.info("connecteurJDBC",
        "ConnecteurJDBCSessionController.searchDriverIndice()",
        "connecteurJDBC.MSG_DRIVER_NAME", "DriverName : " + driverName);
    int i = 0;
    while (i < driversNames.length && !driversNames[i].equals(driverName)) {
      SilverTrace.info("connecteurJDBC",
          "ConnecteurJDBCSessionController.searchDriverIndice()",
          "connecteurJDBC.MSG_DRIVER_NAME", "driver n�" + i + "="
              + driversNames[i]);
      i++;
    }
    return (i);
  }

  private Driver registerAndInstanciateDriver(String driverName)
      throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    SilverTrace.info("connecteurJDBC",
        "ConnecteurJDBCSessionControl.registerAndInstanciateDriver()",
        "root.MSG_GEN_ENTER_METHOD", "driverName=" + driverName);
    SilverTrace.info("connecteurJDBC",
        "ConnecteurJDBCSessionControl.registerAndInstanciateDriver()",
        "root.MSG_GEN_PARAM_VALUE", "driversClassNames.length="
            + driversClassNames.length);

    String driverClass = driversClassNames[searchDriverIndice(driverName)];

    return (Driver) Class.forName(driverClass).newInstance();

  }

  // register existing drivers
  public void loadDrivers() {
    try {
      InputStream m_ConfigFileInputStream;
      String configFileStr = "settings/connecteurJDBCSettings";
      m_ConfigFileInputStream = ResourceLocator.getResourceAsStream(this, null,
          configFileStr, ".xml");
      XMLConfigurationStore m_XMLConfig = new XMLConfigurationStore(null,
          m_ConfigFileInputStream, "ConnecteurJDBC-configuration");
      driversNames = m_XMLConfig.getValues("Drivers");
      m_ConfigFileInputStream.close();
      driversDisplayNames = new String[driversNames.length];
      driversClassNames = new String[driversNames.length];
      driversDescriptions = new String[driversNames.length];
      driversUrls = new Vector();
      for (int j = 0; j < driversNames.length; j++) {
        SilverTrace.info("connecteurJDBC",
            "ConnecteurJDBCSessionControl.loadDrivers()",
            "connecteurJDBC.MSG_DRIVER_NAME", "DriverName=" + driversNames[j]);

        m_ConfigFileInputStream = ResourceLocator.getResourceAsStream(this,
            null, configFileStr, ".xml");
        m_XMLConfig = new XMLConfigurationStore(null, m_ConfigFileInputStream,
            driversNames[j] + "-configuration");

        driversDisplayNames[j] = m_XMLConfig.getString("DriverName");
        driversClassNames[j] = m_XMLConfig.getString("ClassName");
        driversDescriptions[j] = m_XMLConfig.getString("Description");
        driversUrls.addElement(m_XMLConfig.getValues("JDBCUrls"));
        m_ConfigFileInputStream.close();
      }

    } catch (Exception e) {
      SilverTrace.warn("connecteurJDBC",
          "ConnecteurJDBCSessionControl.loadDrivers()",
          "connecteurJDBC.MSG_load_DRIVERS_FAIL", null, e);
    }
  }

  public Collection getDriversDescriptions() {
    return (Arrays.asList(driversDescriptions));
  }

  public Collection getAvailableDriversNames() {
    return (Arrays.asList(driversNames));
  }

  // returns available drivers names
  public Collection getAvailableDriversDisplayNames() {
    return (Arrays.asList(driversDisplayNames));
  }

  // returns jdbc urls for driver
  public Collection getJDBCUrlsForDriver(String driverName) {
    String[] str = (String[]) driversUrls
        .elementAt(searchDriverIndice(driverName));
    return (Arrays.asList(str));
  }

  // returns description for driver
  public String getDescriptionForDriver(String driverName) {
    String str = (String) driversDescriptions[searchDriverIndice(driverName)];
    return (str);
  }

  public void updateConnection(String JDBCdriverName, String JDBCurl,
      String login, String password, int rowLimit)
      throws ConnecteurJDBCException {
    if (connecteurJDBCDetail != null) {
      connecteurJDBCDetail.setJDBCdriverName(JDBCdriverName);
      connecteurJDBCDetail.setJDBCurl(JDBCurl);
      connecteurJDBCDetail.setLogin(login);
      connecteurJDBCDetail.setPassword(password);
      connecteurJDBCDetail.setRowLimit(rowLimit);
      updateConnecteur();
    } else {
      connecteurJDBCDetail = new ConnecteurJDBCConnectionInfoDetail();
      connecteurJDBCDetail.setPK(new ConnecteurJDBCConnectionInfoPK("",
          getSpaceId(), getComponentId()));
      connecteurJDBCDetail.setJDBCdriverName(JDBCdriverName);
      connecteurJDBCDetail.setJDBCurl(JDBCurl);
      connecteurJDBCDetail.setLogin(login);
      connecteurJDBCDetail.setPassword(password);
      connecteurJDBCDetail.setRowLimit(rowLimit);
      newConnecteur();
    }
  }

  private void updateConnecteur() throws ConnecteurJDBCException {
    if (connecteurJDBCDetail != null)
      try {
        // load and return the current jdbc settings for the component
        ConnecteurJDBCBm connecteurJDBCBm = getConnecteurJDBCBm();
        connecteurJDBCBm.updateConnection(connecteurJDBCDetail);
      } catch (Exception e) {
        throw new ConnecteurJDBCException(
            "connecteurJDBCSessionControl.updateConnecteur()",
            SilverpeasException.FATAL,
            "connecteurJDBC.EX_UPDATE_CONNECTION_FAIL", null, e);
      }
  }

  private void newConnecteur() throws ConnecteurJDBCException {
    if (connecteurJDBCDetail != null)
      try {
        // load and return the current jdbc settings for the component
        ConnecteurJDBCBm connecteurJDBCBm = getConnecteurJDBCBm();
        connecteurJDBCDetail.setPK(connecteurJDBCBm
            .addConnection(connecteurJDBCDetail));
      } catch (Exception e) {
        throw new ConnecteurJDBCException(
            "connecteurJDBCSessionControl.newConnecteur()",
            SilverpeasException.FATAL, "connecteurJDBC.EX_NEW_CONNECTION_FAIL",
            null, e);
      }
  }

  private Driver getDriver() {
    if (driver == null) {
      try {
        String driverName = connecteurJDBCDetail.getJDBCdriverName();
        if (driverName == null || driverName.length() == 0) {
          SilverTrace
              .info("connecteurJDBC",
                  "ConnecteurJDBCSessionControl.getDriver()",
                  "root.MSG_GEN_PARAM_VALUE",
                  "driverName undefined ! default one used instead = "
                      + driverName);
          driverName = driversNames[0];
        }
        driver = registerAndInstanciateDriver(driverName);
      } catch (Exception e) {
        SilverTrace.warn("connecteurJDBC",
            "ConnecteurJDBCSessionControl.initConnecteur()",
            "connecteurJDBC.MSG_INIT_CONNECTEUR_FAIL", null, e);
      }
    }
    return driver;
  }

  public String checkConnection(String JDBCurl, String login, String password) {
    if ((JDBCurl == null) || (login == null) || (password == null)) {
      return getString("erreurParametresConnectionIncorrects");
    } else {
      Connection con = null;
      try {
        Properties info = new Properties();
        info.setProperty("user", login);
        info.setProperty("password", password);
        con = getDriver().connect(JDBCurl, info);
      } catch (Exception e) {
        SilverTrace.warn("connecteurJDBC",
            "ConnecteurJDBCSessionControl.checkConnection()",
            "connecteurJDBC.MSG_CHECK_CONNECTION_FAIL", "login : " + login
                + "password : " + password + "JDBCurl : " + JDBCurl, e);
        return (SilverTrace.getTraceMessage(
            "connecteurJDBC.MSG_CHECK_CONNECTION_FAIL", getLanguage()));
      } finally {
        if (con != null) {
          try {
            con.close();
          } catch (SQLException e) {
          }
        }
      }
      return (null);
    }
  }

  public String checkRequest(String request) {
    SilverTrace.info("connecteurJDBC",
        "ConnecteurJDBCSessionController.checkRequest()",
        "root.MSG_GEN_ENTER_METHOD", "request : " + request);
    String temp = request.trim();

    try {
      if ((getDriver() == null) || (getJDBCurl() == null)) {
        return getString("erreurParametresConnectionIncorrects");
      } else if ((request == null) || (request.trim().equals(""))) {
        return getString("erreurRequeteVide");
      } else if (!temp.startsWith("select") && !temp.startsWith("SELECT")) {
        return getString("erreurModifTable");
      } else {
        Properties info = new Properties();
        info.setProperty("user", getLogin());
        info.setProperty("password", getPassword());
        Connection con = getDriver().connect(getJDBCurl(), info);
        if (con == null) {
          return getString("erreurParametresConnectionIncorrects");
        } else {
          Statement stmt = con.createStatement();
          rs = stmt.executeQuery(request);
          // il fallait cliquer deux fois sur consultation c'est pourquoi ce
          // commentaire!!!!!
          /*
           * if (rs!=null) rs.close();
           */
        }
      }
    } catch (SQLException e) {
      SilverTrace.warn("connecteurJDBC",
          "ConnecteurJDBCSessionControl.checkConnection()",
          "connecteurJDBC.MSG_CHECK_REQUEST_FAIL", "request : " + request, e);
      return (e.getMessage());
    } catch (MissingResourceException e) {
      SilverTrace
          .warn("connecteurJDBC",
              "ConnecteurJDBCSessionControl.checkConnection()",
              "connecteurJDBC.MSG_MISSING_MESSAGE_ERROR", "request : "
                  + request, e);
      return "can't find error Message";
    }
    return (null);
  }

  public void close() {
    try {
      if (connecteurJDBCEjb != null)
        connecteurJDBCEjb.remove();
    } catch (RemoteException e) {
      SilverTrace.error("connecteurJDBCSession",
          "ConnecteurJDBCSessionController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("connecteurJDBCSession",
          "ConnecteurJDBCSessionController.close", "", e);
    }

    try {
      if (searchEngineEjb != null)
        searchEngineEjb.remove();
    } catch (RemoteException e) {
      SilverTrace.error("connecteurJDBCSession",
          "ConnecteurJDBCSessionController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("connecteurJDBCSession",
          "ConnecteurJDBCSessionController.close", "", e);
    }

  }

}
