package com.sapstern.openedifact.segmenttest;
import static org.junit.Assert.assertThat;

import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.matchers.CompareMatcher;
import org.xmlunit.xpath.JAXPXPathEngine;

import com.sapstern.openedifact.sax.EdifactSaxParserToXML;
import com.sapstern.openedifact.sax.EdifactSaxParserToXMLIF;


public class EDIFACT_2_XML_ORDERS_Test {
	
	static GenDataORDERS01B genDataORDERS01B = null;
	static EdifactSaxParserToXMLIF theXmlParser = null;
	static Logger logger;
	
	@BeforeClass public static void beforeClass() throws Exception
	{
		genDataORDERS01B = new GenDataORDERS01B();
		//System.out.println( "@BeforeClass" );
		logger = java.util.logging.Logger.getAnonymousLogger();
		logger.setLevel(Level.OFF);
	} 
	@Before public void setUp() throws SAXException
	{
		theXmlParser = EdifactSaxParserToXML.factory("UTF-8", logger);
	}
	@Test
	public void IMD01() throws Exception
	{
		checkSegment (new String[]{"IMD","01"});		
	}
	
	private static void checkSegment (String[] element) throws Exception
	{
		String res = genDataORDERS01B.getEdi(element);
		String edifactXml = nodeToString (theXmlParser.parseEdifact(res), "//S_" + element[0]);
		
		String controlXml = genDataORDERS01B.getXml(element);;

		assertThat(edifactXml, 
				CompareMatcher.isSimilarTo(controlXml).withNodeMatcher(
						new DefaultNodeMatcher(ElementSelectors.byName)).ignoreWhitespace());
	
	}
	
	private static String nodeToString(String edifactXml, String xpath) throws Exception {
		Source source = Input.from(edifactXml).build();
		JAXPXPathEngine xpathEngine = new JAXPXPathEngine();
		Node node = xpathEngine.selectNodes(xpath, source).iterator().next();
	    StringWriter buf = new StringWriter();
	    Transformer xform = TransformerFactory.newInstance().newTransformer();
	    xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    xform.setOutputProperty(OutputKeys.INDENT, "yes");
	    xform.transform(new DOMSource(node), new StreamResult(buf));

	    return(buf.toString().replace("\r", "").replace("\n", ""));
	}
}
