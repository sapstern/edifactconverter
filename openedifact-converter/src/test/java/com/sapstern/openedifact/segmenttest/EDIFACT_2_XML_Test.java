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


public class EDIFACT_2_XML_Test {
	
	static GenDataUTILMD11A genDataUTILMD11A = null;
	static EdifactSaxParserToXMLIF theXmlParser = null;
	static Logger logger;
	
	@BeforeClass public static void beforeClass() throws Exception
	{
		genDataUTILMD11A = new GenDataUTILMD11A();
		//System.out.println( "@BeforeClass" );
		logger = java.util.logging.Logger.getAnonymousLogger();
		logger.setLevel(Level.OFF);
	} 
	@Before public void setUp() throws SAXException
	{
		theXmlParser = EdifactSaxParserToXML.factory("UTF-8", logger);
	}
	@Test
	public void AGR01() throws Exception
	{
		checkSegment (new String[]{"AGR","01"});		
	}
	@Test
	public void CAV01() throws Exception
	{
		checkSegment (new String[]{"CCI","01"});		
	}
	@Test
	public void CCI01() throws Exception
	{
		checkSegment (new String[]{"CCI","01"});		
	}
	@Test
	public void IMD01() throws Exception
	{
		checkSegment (new String[]{"IMD","01"});		
	}
	@Test
	public void FTX01() throws Exception
	{
		checkSegment (new String[]{"FTX","01"});
	}
	@Test
	public void FTX02() throws Exception
	{
		checkSegment (new String[]{"FTX","02"});
	}
	@Test
	public void LOC01() throws Exception
	{
		checkSegment (new String[]{"LOC","01"});
	}
	@Test
	public void LOC02() throws Exception
	{
		checkSegment (new String[]{"LOC","02"});
	}
	@Test
	public void RFF01() throws Exception
	{
		checkSegment (new String[]{"RFF","01"});
	}
	@Test
	public void SEQ01() throws Exception
	{
		checkSegment (new String[]{"SEQ","01"});
	}
	private static void checkSegment (String[] element) throws Exception
	{
		String res = genDataUTILMD11A.getEdi(element);
		String edifactXml = nodeToString (theXmlParser.parseEdifact(res), "//S_" + element[0]);
		
		String controlXml = genDataUTILMD11A.getXml(element);;

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
