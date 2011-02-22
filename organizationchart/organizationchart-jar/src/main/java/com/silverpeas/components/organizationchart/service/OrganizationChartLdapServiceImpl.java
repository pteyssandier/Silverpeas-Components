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

package com.silverpeas.components.organizationchart.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.silverpeas.components.organizationchart.model.OrganizationalChart;
import com.silverpeas.components.organizationchart.model.OrganizationalChartType;
import com.silverpeas.components.organizationchart.model.OrganizationalPerson;
import com.silverpeas.components.organizationchart.model.OrganizationalRole;
import com.silverpeas.components.organizationchart.model.OrganizationalUnit;
import com.silverpeas.components.organizationchart.model.PersonCategory;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class OrganizationChartLdapServiceImpl implements OrganizationChartService {
  private OrganizationChartConfiguration config = null;

  public void configure(OrganizationChartConfiguration config) {
    this.config = config;
  }

  @Override
  public OrganizationalChart getOrganizationChart(String baseOu, OrganizationalChartType type) {
    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_ENTER_METHOD");

    Hashtable<String, String> env = config.getEnv();

    List<OrganizationalPerson> ouMembers = null;
    List<OrganizationalUnit> units = null;

    // beginning node of the search
    String rootOu = (StringUtil.isDefined(baseOu)) ? baseOu : config.getLdapRoot();

    // Parent definition = top of the chart
    String[] ous = rootOu.split(",");
    String[] firstOu = ous[0].split("=");
    OrganizationalUnit parent = new OrganizationalUnit(firstOu[1], rootOu, config.getLdapAttUnit());

    DirContext ctx = null;
    try {
      ctx = new InitialDirContext(env);
      SearchControls ctls = new SearchControls();
      ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
      ctls.setCountLimit(0);

      // get organization unit members
      ouMembers = getOUMembers(ctx, ctls, rootOu, type);
      parent.setHasMembers((ouMembers.size() > 0));

      // get sub organization units
      if (type == OrganizationalChartType.TYPE_UNITCHART) {
        units = getSubOrganizationUnits(ctx, ctls, rootOu);
      }

    } catch (NamingException e) {
      SilverTrace.error("organizationchart",
          "OrganizationChartLdapServiceImpl.getOrganizationChart",
          "organizationChart.ldap.closing.context.error", e);
      return null;
    } finally {
      if (ctx != null) {
        try {
          ctx.close();
        } catch (NamingException e) {
          SilverTrace.error("organizationchart",
              "OrganizationChartLdapServiceImpl.getOrganizationChart",
              "organizationChart.ldap.conection.error", e);
        }
      }
    }

    boolean silverpeasUserLinkable = StringUtil.isDefined(config.getDomainId());

    OrganizationalChart chart = null;
    switch (type) {
      case TYPE_UNITCHART:
        chart = new OrganizationalChart(parent, units, ouMembers, silverpeasUserLinkable);
        break;

      default:
        Set<PersonCategory> categories = getCategories(ouMembers);
        chart = new OrganizationalChart(parent, ouMembers, categories, silverpeasUserLinkable);
        break;
    }

    return chart;
  }

  /**
   * Get person list for a given OU.
   * @param ctx Search context
   * @param ctls Search controls
   * @param rootOu rootOu
   * @param type type
   * @return a List of OrganizationalPerson objects.
   * @throws NamingException
   */
  private List<OrganizationalPerson> getOUMembers(DirContext ctx,
      SearchControls ctls, String rootOu,
      OrganizationalChartType type) throws NamingException {

    List<OrganizationalPerson> personList = new ArrayList<OrganizationalPerson>();

    NamingEnumeration<SearchResult> results =
        ctx.search(rootOu, "(objectclass=" + config.getLdapClassPerson() + ")", ctls);
    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_PARAM_VALUE",
        "users retrieved !");

    int i = 0;

    while (results != null && results.hasMore()) {
      SearchResult entry = (SearchResult) results.next();
      if (StringUtil.isDefined(entry.getName())) {
        Attributes attrs = entry.getAttributes();
        if (isUserActive(config.getLdapAttActif(), attrs)) {
          OrganizationalPerson person =
              loadOrganizationalPerson(i, attrs, entry.getNameInNamespace(), type);
          personList.add(person);
          i++;
        }
      }
    }

    Function<OrganizationalPerson, String> personToSortedValue =
        new Function<OrganizationalPerson, String>() {
      @Override
      public String apply(OrganizationalPerson obj) {
        return obj.getName() + obj.getId();
      }
    };
    Collections.sort(personList,
        Ordering.from(String.CASE_INSENSITIVE_ORDER).onResultOf(personToSortedValue));

    return personList;
  }

  /**
   * Get sub organization units of a given OU.
   * @param ctx Search context
   * @param ctls Search controls
   * @param rootOu rootOu
   * @return a List of OrganizationalUnit objects.
   * @throws NamingException
   */
  private List<OrganizationalUnit> getSubOrganizationUnits(DirContext ctx,
      SearchControls ctls, String rootOu) throws NamingException {

    ArrayList<OrganizationalUnit> units = new ArrayList<OrganizationalUnit>();

    NamingEnumeration<SearchResult> results =
        ctx.search(rootOu, "(objectclass=" + config.getLdapClassUnit() + ")", ctls);

    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_PARAM_VALUE",
        "services retrieved !");

    while (results != null && results.hasMore()) {
      SearchResult entry = (SearchResult) results.next();
      Attributes attrs = entry.getAttributes();
      String ou = getFirstAttributeValue(attrs.get(config.getLdapAttUnit()));
      String completeOu = entry.getNameInNamespace();
      OrganizationalUnit unit = new OrganizationalUnit(ou, completeOu, config.getLdapAttUnit());
      units.add(unit);
    }

    for (OrganizationalUnit unit : units) {
      boolean hasSubOrganizations =
          hasResults(unit.getCompleteName(), "(objectclass=" + config.getLdapClassUnit() + ")",
          ctx, ctls);
      unit.setHasSubUnits(hasSubOrganizations);

      boolean hasMembers =
          hasResults(unit.getCompleteName(), "(objectclass=" + config.getLdapClassPerson() + ")",
          ctx, ctls);
      unit.setHasMembers(hasMembers);
    }

    return units;
  }

  /**
   * Launch a search and return true if there are results.
   * @param baseDN baseDN for search
   * @param filter search filter
   * @param ctx search context
   * @param ctls search controls
   * @return true is at least one result is found.
   * @throws NamingException
   */
  private boolean hasResults(String baseDN, String filter, DirContext ctx, SearchControls ctls)
      throws NamingException {
    NamingEnumeration<SearchResult> results = ctx.search(baseDN, filter, ctls);
    return (results != null && results.hasMoreElements());
  }

  /**
   * Get all distinct person categories represented by a given person list.
   * @param personList the person list
   * @return a Set of PersonCategory
   */
  private Set<PersonCategory> getCategories(List<OrganizationalPerson> personList) {
    Set<PersonCategory> categories = new TreeSet<PersonCategory>();

    for (OrganizationalPerson person : personList) {
      // if person is a key Actor of organizationUnit, it will appear in main Cell, so ignore that
      // actor's category
      if (!person.isVisibleOnCenter() && person.getVisibleCategory() != null) {
        categories.add(person.getVisibleCategory());
      }
    }

    return categories;
  }

  /**
   * Get Person details.
   */
  public Map<String, String> getOrganizationalPersonDetails(OrganizationalPerson[] org, int id) {
    for (OrganizationalPerson pers : org) {
      if (pers.getId() == id) {
        Map<String, String> details = pers.getDetail();
        return details;
      }
    }
    return null;
  }

  /**
   * Build a OrganizationalPerson object by retrieving attributes values
   * @param id person Id
   * @param attrs ldap attributes
   * @param dn dn
   * @param type organizationChart type
   * @return a loaded OrganizationalPerson object
   */
  private OrganizationalPerson loadOrganizationalPerson(int id,
      Attributes attrs, String dn, OrganizationalChartType type) {

    // Full Name
    String fullName = getFirstAttributeValue(attrs.get(config.getLdapAttName()));

    // Function
    String function = getFirstAttributeValue(attrs.get(config.getLdapAttTitle()));

    // Description
    String description = getFirstAttributeValue(attrs.get(config.getLdapAttDesc()));

    // login
    String login = getFirstAttributeValue(attrs.get(config.getLdapAttAccount()));

    // service
    String service = getFirstAttributeValue(attrs.get(config.getLdapAttUnit()));
    try {
      if (service == null) {
        LdapName ldapName = new LdapName(dn);
        for (Rdn rdn : ldapName.getRdns()) {
          if (rdn.getType().equalsIgnoreCase("ou")) {
            service = rdn.getValue().toString();
            break;
          }
        }
      }
    } catch (InvalidNameException e1) {
      SilverTrace.warn("organizationchart",
          "OrganizationChartLdapServiceImpl.getOrganizationalPersonDetails",
          "organizationchart.ERROR_GET_SERVICE", "dn : " + dn, e1);
    }

    // build OrganizationalPerson object
    OrganizationalPerson person =
        new OrganizationalPerson(id, -1, fullName, function, description, service, login);

    // build details map
    Map<String, String> details = new HashMap<String, String>();

    // Determines attributes to be returned
    Map<String, String> attributesToReturn = null;
    switch (type) {
      case TYPE_UNITCHART:
        attributesToReturn = config.getUnitsChartOthersInfosKeys();
        break;

      default:
        attributesToReturn = config.getPersonnsChartOthersInfosKeys();
    }

    // get only the attributes defined in the organizationChart parameters
    for (Entry<String, String> attribute : attributesToReturn.entrySet()) {
      Attribute att = attrs.get(attribute.getKey());
      if (att != null) {
        try {
          String detail = "";
          if (att.size() > 1) {
            NamingEnumeration<?> vals = att.getAll();
            while (vals.hasMore()) {
              String val = (String) vals.next();
              detail += val + ", ";
            }
            detail = detail.substring(0, detail.length() - 2);
          } else {
            detail = getFirstAttributeValue(att);
          }
          details.put(attribute.getValue(), detail);
        } catch (NamingException e) {
          SilverTrace.warn("organizationchart",
              "OrganizationChartLdapServiceImpl.getOrganizationalPersonDetails",
              "organizationchart.ERROR_GET_DETAIL", "attribute : " + attribute.getKey(), e);
        }
      }
    }
    person.setDetail(details);

    // defined the boxes with personns inside
    if (function != null) {
      switch (type) {
        case TYPE_UNITCHART:
          defineUnitChartRoles(person, function);
          break;

        default:
          defineDetailledChartRoles(person, function);
          break;
      }
    } else {
      person.setVisibleCategory(new PersonCategory("Personnel"));
    }

    return person;
  }

  /**
   * Determine if person has a specific Role in organization Chart.
   * @param person person
   * @param function person's function
   */
  private void defineUnitChartRoles(OrganizationalPerson person, String function) {

    // Priority to avoid conflicted syntaxes : right, left and then central
    boolean roleDefined = false;

    // right
    for (OrganizationalRole role : config.getUnitsChartRightLabel()) {
      if (isFunctionMatchingRole(function, role)) {
        person.setVisibleOnRight(true);
        person.setVisibleRightRole(role);
        roleDefined = true;
        break;
      }
    }

    // left
    if (!roleDefined) {
      for (OrganizationalRole role : config.getUnitsChartLeftLabel()) {
        if (isFunctionMatchingRole(function, role)) {
          person.setVisibleOnLeft(true);
          person.setVisibleLeftRole(role);
          roleDefined = true;
          break;
        }
      }
    }

    // central
    if (!roleDefined) {
      for (OrganizationalRole role : config.getUnitsChartCentralLabel()) {
        if (isFunctionMatchingRole(function, role)) {
          person.setVisibleOnCenter(true);
          person.setVisibleCenterRole(role);
          roleDefined = true;
          break;
        }
      }
    }
  }

  /**
   * Determine if person has a specific Role in organization person Chart.
   * @param person person
   * @param function person's function
   */
  private void defineDetailledChartRoles(OrganizationalPerson pers, String function) {

    // Priority to avoid conflicted syntaxes : central then categories
    boolean roleDefined = false;

    // central
    for (OrganizationalRole role : config.getPersonnsChartCentralLabel()) {
      if (isFunctionMatchingRole(function, role)) {
        pers.setVisibleOnCenter(true);
        pers.setVisibleCenterRole(role);
        roleDefined = true;
        break;
      }
    }

    // categories
    if (!roleDefined) {
      int order = 0;
      for (OrganizationalRole role : config.getPersonnsChartCategoriesLabel()) {
        if (isFunctionMatchingRole(function, role)) {
          pers.setVisibleCategory(new PersonCategory(role.getLabel(), role.getLdapKey(), order));
          roleDefined = true;
          break;
        }
        order++;
      }
    }

    if (!roleDefined) {
      pers.setVisibleCategory(new PersonCategory("Personnel"));
    }
  }

  /**
   * Checks if given function is matching given role
   * @param function function to check
   * @param role role
   * @return true if function is matching given role
   */
  private boolean isFunctionMatchingRole(String function, OrganizationalRole role) {
    return ((role != null)
        && StringUtil.isDefined(role.getLdapKey()) && function.toLowerCase().indexOf(
        role.getLdapKey()) != -1);
  }

  /**
   * Get first Ldap attribute value.
   * @param att Ldap attribute
   * @return the first attribute value as String
   */
  private String getFirstAttributeValue(Attribute att) {
    String result = null;

    try {
      if (att != null) {
        String val = (String) att.get();
        if (StringUtil.isDefined(val)) {
          result = val;
        }
      }
    } catch (NamingException e1) {
      SilverTrace.warn("organizationchart",
          "OrganizationChartLdapServiceImpl.getFirstAttributeValue",
          "organizationchart.ERROR_GET_ATTRIBUTE_VALUE", "attribute : " + att.getID(), e1);
    }

    return result;
  }

  /**
   * Check if a user is active
   * @param activeAttribute ldap attribute to check f user is active
   * @param attrs ldap attributes
   * @return true is user is active or no activeAttribute is specified.
   */
  private boolean isUserActive(String activeAttribute, Attributes attrs) {
    // if no ldap attribute specified in configuration, let's consider user is always valid
    if (activeAttribute == null)
      return true;

    String actif = getFirstAttributeValue(attrs.get(activeAttribute));
    return !StringUtil.getBooleanValue(actif);
  }

}
