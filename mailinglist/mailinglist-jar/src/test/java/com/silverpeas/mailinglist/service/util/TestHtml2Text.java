package com.silverpeas.mailinglist.service.util;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import com.silverpeas.mailinglist.service.util.Html2Text;

import junit.framework.TestCase;

public class TestHtml2Text extends TestCase {
	HtmlCleaner parser = new Html2Text(200);

	public void testParse() throws Exception {
		String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">"
				+ "<html><head><meta content=\"text/html;charset=ISO-8859-1\" "
				+ "http-equiv=\"Content-Type\"><title></title></head><body "
				+ "bgcolor=\"#ffffff\" text=\"#000000\">Hello World <i>Salut les "
				+ "copains </i></body></html>";
		Reader reader = new StringReader(html);
		parser.parse(reader);
		String summary = parser.getSummary();
		assertNotNull(summary);
		assertEquals("Hello World Salut les copains", summary);
	}

	public void testParseBigContent() throws Exception {
		Reader reader = new InputStreamReader(TestHtml2Text.class
				.getResourceAsStream("lemonde.html"));
		parser.parse(reader);
		String summary = parser.getSummary();
		assertNotNull(summary);
		assertEquals("<!------ OAS SETUP end ------> Oa name=\"top\"> Politique < " +
				"< Recherchez depuis sur Le Monde.fr < <!-- info_sq_1_zone --> " +
				"A la Une Le Desk Vid�os International *Elections am�ricaines Europe " +
				"Politique *M", summary);

	}
	
	public void testParseInraContent() throws Exception {
    Reader reader = new InputStreamReader(TestHtml2Text.class
        .getResourceAsStream("mailInra.html"));
    parser.parse(reader);
    String summary = parser.getSummary();
    assertNotNull(summary);
    assertEquals("Bonjour, Lors de la pr�sention des nouveaux outils effectu�e " +
    		"le 6 avril, il a �t� �mis l'id�e par un DU de cr�er dans l'espace GU " +
    		"TOULOUSE de SILVERPEAS un espace priv� par unit� facilitant l'organisa", 
    		summary);

  }
}
