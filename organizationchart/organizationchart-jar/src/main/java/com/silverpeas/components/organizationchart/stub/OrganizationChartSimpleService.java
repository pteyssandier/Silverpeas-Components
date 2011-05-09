package com.silverpeas.components.organizationchart.stub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.silverpeas.components.organizationchart.model.OrganizationalChart;
import com.silverpeas.components.organizationchart.model.OrganizationalChartType;
import com.silverpeas.components.organizationchart.model.OrganizationalPerson;
import com.silverpeas.components.organizationchart.model.OrganizationalRole;
import com.silverpeas.components.organizationchart.model.OrganizationalUnit;
import com.silverpeas.components.organizationchart.model.PersonCategory;
import com.silverpeas.components.organizationchart.service.OrganizationChartConfiguration;
import com.silverpeas.components.organizationchart.service.OrganizationChartService;
import com.silverpeas.util.StringUtil;

public class OrganizationChartSimpleService implements OrganizationChartService {

  private OrganizationChartConfiguration config = null;

  @Override
  public OrganizationalChart getOrganizationChart(String baseOu, OrganizationalChartType type) {

    List<OrganizationalPerson> ouMembers = null;
    List<OrganizationalUnit> units = null;

    // beginning node of the search
    String rootOu = (StringUtil.isDefined(baseOu)) ? baseOu : config.getLdapRoot();

    // Parent definition = top of the chart
    String[] ous = rootOu.split(",");
    String[] firstOu = ous[0].split("=");
    OrganizationalUnit root = new OrganizationalUnit(firstOu[1], rootOu);
    if (StringUtil.isDefined(baseOu)) {
      root.setParentName("Racine");
      root.setParentOu(config.getLdapRoot());
    }
    
    // get members
    ouMembers = getOUMembers(rootOu, type);
    root.setHasMembers(!ouMembers.isEmpty());

    // get sub organization units
    if (type == OrganizationalChartType.TYPE_UNITCHART) {
      units = getSubOrganizationUnits(rootOu);
    }

    boolean silverpeasUserLinkable = StringUtil.isDefined(config.getDomainId());

    OrganizationalChart chart = null;
    switch (type) {
      case TYPE_UNITCHART:
        chart = new OrganizationalChart(root, units, ouMembers, silverpeasUserLinkable);
        break;

      default:
        Set<PersonCategory> categories = getCategories(ouMembers);
        chart = new OrganizationalChart(root, ouMembers, categories, silverpeasUserLinkable);
        break;
    }

    return chart;
  }

  @Override
  public Map<String, String> getOrganizationalPersonDetails(OrganizationalPerson[] org, int id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void configure(OrganizationChartConfiguration config) {
    config.setLdapRoot("OU=Bands,dc=mondomain,dc=com");
    config.setLdapAttUnit("");
    List<OrganizationalRole> rightRoles = new ArrayList<OrganizationalRole>();
    rightRoles.add(new OrganizationalRole("Chanteurs", "chanteur"));
    config.setUnitsChartRightLabel(rightRoles);

    List<OrganizationalRole> leftRoles = new ArrayList<OrganizationalRole>();
    leftRoles.add(new OrganizationalRole("Musiciens", "musicien"));
    config.setUnitsChartLeftLabel(leftRoles);
    
    List<OrganizationalRole> centralCategory = new ArrayList<OrganizationalRole>();
    centralCategory.add(new OrganizationalRole("Leader", "leader"));
    config.setPersonnsChartCentralLabel(centralCategory);
    
    List<OrganizationalRole> categories = new ArrayList<OrganizationalRole>();
    categories.add(new OrganizationalRole("Musiciens", "musicien"));
    config.setPersonnsChartCategoriesLabel(categories);
    
    this.config = config;
  }

  private List<OrganizationalPerson> getOUMembers(String rootOu, OrganizationalChartType type) {

    List<OrganizationalPerson> personList = new ArrayList<OrganizationalPerson>();

    if (rootOu.equalsIgnoreCase("ou=Metallica,ou=Bands,dc=mondomain,dc=com")) {

      OrganizationalPerson person1 =
          new OrganizationalPerson(0, -1, "James Hetfield", "leader", "Une description",
              "Metallica", "hetfield");
      HashMap<String, String> person1Detail = new HashMap<String, String>();
      person1Detail.put("Instrument", "Chant & guitare");
      person1.setDetail(person1Detail);
      defineChartRoles(person1, type);
      personList.add(person1);

      OrganizationalPerson person2 =
          new OrganizationalPerson(1, -1, "Kirk Hammett", "musicien", "Une autre description",
              "Metallica", "hammett");
      HashMap<String, String> person2Detail = new HashMap<String, String>();
      person2Detail.put("Instrument", "Guitare");
      person2.setDetail(person2Detail);
      defineChartRoles(person2, type);
      personList.add(person2);

      OrganizationalPerson person21 =
          new OrganizationalPerson(2, -1, "Lars Ulrich", "musicien", "Une autre description",
              "Metallica", "ulrich");
      HashMap<String, String> person21Detail = new HashMap<String, String>();
      person21Detail.put("Instrument", "Batterie");
      person21.setDetail(person21Detail);
      defineChartRoles(person21, type);
      personList.add(person21);
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

  private List<OrganizationalUnit> getSubOrganizationUnits(String rootOu) {

    ArrayList<OrganizationalUnit> units = new ArrayList<OrganizationalUnit>();

    if (rootOu.equalsIgnoreCase("OU=Bands,dc=mondomain,dc=com")) {
      OrganizationalUnit unit =
          new OrganizationalUnit("Metallica", "ou=Metallica,OU=Bands,dc=mondomain,dc=com");
      unit.setParentName("Bands");
      unit.setParentOu(rootOu);
      unit.setHasSubUnits(false);
      unit.setHasMembers(true);
      units.add(unit);

      OrganizationalUnit unit1 =
          new OrganizationalUnit("Slayer", "ou=Slayer,OU=Bands,dc=mondomain,dc=com");
      unit1.setParentName("Bands");
      unit1.setParentOu(rootOu);
      unit1.setHasSubUnits(false);
      unit1.setHasMembers(false);
      units.add(unit1);
    }

    return units;
  }
  
  private void defineChartRoles(OrganizationalPerson person, OrganizationalChartType type) {
    // defined the boxes with persons inside
    if (StringUtil.isDefined(person.getFonction())) {
      switch (type) {
        case TYPE_UNITCHART:
          defineUnitChartRoles(person, person.getFonction());
          break;

        default:
          defineDetailledChartRoles(person, person.getFonction());
          break;
      }
    } else {
      person.setVisibleCategory(new PersonCategory("Personnel"));
    }
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

}
