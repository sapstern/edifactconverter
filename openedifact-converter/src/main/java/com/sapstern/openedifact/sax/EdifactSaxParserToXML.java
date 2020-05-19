/**
 * Free Message Converter Copyleft 2007 - 2014 Matthias Fricke mf@sapstern.com


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.sapstern.openedifact.sax;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import com.sapstern.openedifact.sax.parse.segments.EdifactField;
import com.sapstern.openedifact.sax.parse.segments.EdifactSegment;
import com.sapstern.openedifact.sax.parse.segments.EdifactSubField;
import com.sapstern.openedifact.transform.StringTokenizerEscape;

/**
 *  <BR><BR>
 * <B>Class/Interface Description: </B><BR>
 * Instance class for SAX parsing of UN/EDIFACT to EDIFACT-XML (ISO TS 20625)<BR>
 * <BR><BR>
 * @author Matthias Fricke
 * <DT><B>Known Bugs:</B><BR><!-- Keep in mind to update method bug lists -->
 * none <BR><BR>
 * <DT><B>History:</B>
 * <PRE><!-- Do not use tabs in the history table! Do not extend table width! rel.inc defines release and increment no -->
 * date       name           rel.inc  changes
 * ----------------------------------------------------------------------------------------------------------------------------
 * 20.02.06   fricke         1.0 created
 * 2008       frost          1.1 bugfixing 
 * 19.04.14   fricke         2.0 Adapted to the XML/EDIFACT standard  (ISO TS 20625)
 * 18.09.17	  fricke		 2.1 Refactoring
 * 22.05.18	  fricke		 2.2 bugfix +: problem (https://sourceforge.net/p/edifactconverter/tickets/2/)
 * 24.05.18   fricke         2.3 bugfix FTX+ACB+++This is a test' problem (https://sourceforge.net/p/edifactconverter/tickets/4/)
 * 08.11.19   fricke         2.4 bugfix parsing composites like IMD+F++A:::PRODUCT 1' Product 1 was in wrong field <D_3055>PRODUCT 1</D_3055> it should be in and is now <D_7008>PRODUCT 1</D_7008>
 * 11.11.19   wasawasa       2.5 bugfix if segment string ends with seperator like +someData+' senseless but possible
 * -------------------------------------------------------------------------------------------------------------------------------</PRE>
 *
 *****************************************************************/
public class EdifactSaxParserToXML extends AbstractEdifactParser implements XMLReader, EdifactSaxParserToXMLIF 
{	
	private Transformer transformer = null;

	private ContentHandler contentHandler = null;
	private AttributesImpl attribs = new AttributesImpl();
	private static AttributesImpl compositeAttribs = new AttributesImpl();
	private AttributesImpl rootAttribs = new AttributesImpl();
	private static String namespaceURI = "";

	private String encoding = "UTF-8";
	private String segmentDelimiter = "'";
	private String fieldDelimiter = "+";
	private String componentDataSeparator = ":";
	private String releaseChar = "?";
	private String decimalSep = ".";

	private boolean edielSwedish = false;

	private java.util.logging.Logger logger = null;




	private Document xsdDOM = null;

	@SuppressWarnings("unused")
	private String theMessageName = null;

	static
	{
		compositeAttribs.addAttribute(namespaceURI, "", "composite", "CDATA", "true");
	}

	/**
	 * Factory (with logging)for this parser
	 * @param encoding
	 * @return
	 * @throws SAXException
	 */
	public static EdifactSaxParserToXMLIF factory(String encoding) throws SAXException
	{
		EdifactSaxParserToXML theParser = new EdifactSaxParserToXML(encoding);
		return theParser;

	}

	/**
	 * Factory for this parser
	 * @param encoding
	 * @return
	 * @throws SAXException
	 */
	public static EdifactSaxParserToXMLIF factory(String encoding, java.util.logging.Logger logger) throws SAXException
	{
		EdifactSaxParserToXML theParser = new EdifactSaxParserToXML(encoding, logger);
		return theParser;

	}

	/**
	 * @param encoding
	 * @throws SAXException
	 */
	private EdifactSaxParserToXML(String encoding) throws SAXException
	{
		super();
		init(encoding, null);
	}

	/**
	 * @param encoding
	 * @param logger
	 * @throws SAXException
	 */
	private EdifactSaxParserToXML(String encoding, java.util.logging.Logger logger) throws SAXException
	{
		super();
		this.logger = logger;
		//this.logger.setLevel(Level.FINEST);
		init(encoding, logger);
	}

	private void init(String encoding, java.util.logging.Logger logger)	throws SAXException
	{
		if (logger == null)
		{
			this.logger = java.util.logging.Logger.getAnonymousLogger();
			this.logger.setLevel(Level.FINEST);
			logger = this.logger;
		}

		this.logger.entering("EdifactSaxParserToXML", "init");
		this.encoding = encoding;
		try
		{
			SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) SAXTransformerFactory
					.newInstance();
			logger.finest("got saxTransformerFactory");
			TransformerHandler handler = saxTransformerFactory
					.newTransformerHandler();
			logger.finest("got TransformerHandler");
			transformer = handler.getTransformer();
			logger.finest("got transformer");
			transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		}
		catch (TransformerConfigurationException tce)
		{
			logger.throwing("EdifactSaxParserToXML", "init", tce);
			throw new SAXException(tce);
		}
	}

	/**
	 * SAX parser processing for each EdifactSegment object
	 * 
	 * @param EdifactSegment currentSegment: The current EdifactSegment to be processed by
	 *                       our reverse SAX parser
	 * @throws SAXException
	 */
	private void parseCurrentSegment(EdifactSegment currentSegment)	throws SAXException
	{
		logger.entering("EdifactSaxParserToXML", "parseCurrentSegment");
		List<EdifactField> eFields = currentSegment.segmentFields;

		for (int count = 0; count < eFields.size(); count++)
		{
			EdifactField fieldObject = (EdifactField) eFields.get(count);
			String value = fieldObject.fieldValue;

			if (fieldObject.isComposite)
			{
				contentHandler.startElement(namespaceURI, "",
						fieldObject.fieldTagName, null);
				List<EdifactSubField> subFieldList = fieldObject.subFields;
				for (int j = 0; j < subFieldList.size(); j++)
				{
					EdifactSubField subFieldObject = (EdifactSubField) subFieldList
							.get(j);
					if (subFieldObject.subFieldTagName != null)
					{
						contentHandler.startElement(namespaceURI, "", subFieldObject.subFieldTagName, attribs);
						contentHandler.characters(subFieldObject.subFieldValue.toCharArray(), 0, subFieldObject.subFieldValue.length());
						contentHandler.endElement(namespaceURI, "",	subFieldObject.subFieldTagName);
					}
				}
			}
			else
			{
				contentHandler.startElement(namespaceURI, "",
						fieldObject.fieldTagName, attribs);
				contentHandler.characters(value.toCharArray(), 0, value
						.length());
			}
			contentHandler.endElement(namespaceURI, "",
					fieldObject.fieldTagName);
		}
		logger.exiting("EdifactSaxParserToXML", "parseCurrentSegment");
	}

	/**
	 * Recursive traversal of the tree of EdifactSegment objects mehtod calls
	 * the SAX XML parser for the current EdifactSegment object to be processed
	 * 
	 * @param EdifactSegment segment: The EdifactSegment object to be processed
	 * @throws SAXException
	 */
	private void traverseEdifactSegment(EdifactSegment segment)	throws SAXException
	{
		logger.entering("EdifactSaxParserToXML", "traverseEdifactSegment");
		logger.log(Level.FINE, "segment " + segment);
		contentHandler.startElement(namespaceURI, "", segment.segmentName,	attribs);
		parseCurrentSegment(segment);
		if (segment.childSegments.isEmpty())
		{
			contentHandler.endElement(namespaceURI, "", segment.segmentName);
			return;
		}
		else
		{
			for (int i = 0; i < segment.childSegments.size(); i++)
			{
				traverseEdifactSegment((EdifactSegment) segment.childSegments.get(i));
			}
			contentHandler.endElement(namespaceURI, "", segment.segmentName);
		}
		logger.exiting("EdifactSaxParserToXML", "traverseEdifactSegment");
	}

	// ===========================================================
	// XML Reader Interface Implementation
	// ===========================================================
	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToXMLIF#parse(org.xml.sax.InputSource)
	 */
	@Override
	public void parse(InputSource source) throws IOException, SAXException
	{
		// reading one char at a time is very bad performance...
		// we want a byte array so that we can read it several times
		// and check charset encoding before turning the bytes into string
		// most of the time we will be happy with ISO-8859-1 = UNOC
		// but sometimes we need ISO-646 = UNOB = ASCII
		//
		// Swedish characters are represented as follows
		//
		// r = r.replaceAll("\\É", "@");
		// r = r.replaceAll("\\Å", "]");
		// r = r.replace("Ö".charAt(0), "\\".charAt(0));
		// r = r.replaceAll("\\Ä", "[");
		// r = r.replaceAll("\\Ü", "^");
		//
		// r = r.replaceAll("\\é", "`");
		// r = r.replaceAll("\\ä", "{");
		// r = r.replaceAll("\\ö", "|");
		// r = r.replaceAll("\\å", "}");
		// r = r.replaceAll("\\ü", "~");

		// microsoft has some information "EDI Character Sets"
		// http://msdn.microsoft.com/en-us/library/bb246115.aspx
		// 

		logger.entering("EdifactSaxParserToXML", "parse");

		java.io.BufferedInputStream bis = new java.io.BufferedInputStream(
				source.getByteStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] bytebuf = new byte[4096];
		int len;
		while ((len = bis.read(bytebuf)) != -1)
		{
			out.write(bytebuf, 0, len);
		}
		out.flush();
		byte[] byteEdifact = out.toByteArray();
		String rawEdifact = null;

		rawEdifact = new String(byteEdifact, encoding);
		logger.log(Level.FINE, "rawEdifact ok");

		if (rawEdifact.matches(".*UNB\\+UNOC.*"))
		{
			// we are fine do nothing
			logger.log(Level.FINE, "match .*UNB\\+UNOC.*");
		}
		else
			if (rawEdifact.matches(".*UNB\\+UNOB.*|.*UNB\\+UNOA.*"))
			{
				logger.log(Level.FINE, "match .*UNB\\+UNOB.*|.*UNB\\+UNOA.*");
				rawEdifact = new String(byteEdifact, "ASCII");

				// EDIEL swedish specials
				if (edielSwedish)
				{
					logger.log(Level.FINE, "ediel Swedish");
					String r = rawEdifact;
					r = r.replaceAll("\\@", "É"); // do not konvert email
					// addresses
					r = r.replaceAll("\\]", "Å");
					r = r.replaceAll("\\\\", "Ö");
					r = r.replaceAll("\\[", "\\Ä");
					r = r.replaceAll("\\^", "Ü");
					r = r.replaceAll("\\`", "é");
					r = r.replaceAll("\\{", "\\ä");
					r = r.replaceAll("\\|", "ö");
					r = r.replaceAll("\\}", "\\å");
					r = r.replaceAll("\\~", "ü");
					rawEdifact = r;
				}
			}
			else
			{ // default to UNOB
				logger.log(Level.FINE, "default UNOB");
				rawEdifact = new String(byteEdifact, "ASCII");
				// EDIEL swedish specials
				if (edielSwedish)
				{
					logger.log(Level.FINE, "ediel Swedish");
					String r = rawEdifact;
					r = r.replaceAll("\\@", "É"); // do not konvert email
					// addresses
					r = r.replaceAll("\\]", "Å");
					r = r.replaceAll("\\\\", "Ö");
					r = r.replaceAll("\\[", "\\Ä");
					r = r.replaceAll("\\^", "Ü");
					r = r.replaceAll("\\`", "é");
					r = r.replaceAll("\\{", "\\ä");
					r = r.replaceAll("\\|", "ö");
					r = r.replaceAll("\\}", "\\å");
					r = r.replaceAll("\\~", "ü");
					rawEdifact = r;
				}
			}

		rawEdifact = rawEdifact.toString().trim();
		// rawEdifact = rawEdifact.replaceAll("\n", ""); //01ottfro
		// rawEdifact = rawEdifact.replaceAll("\r", ""); //01ottfro
		int indexOfSegmentDelimiter = rawEdifact.indexOf("UNB+");
		if (indexOfSegmentDelimiter == -1)
		{
			logger.log(Level.FINE, "throw Can not find UNB segment for:\n"
					+ rawEdifact);
			throw new SAXException("Can not find UNB segment for:\n "
					+ rawEdifact);
		}
		if ( rawEdifact.indexOf("UNA+") != - 1 )
		{
			segmentDelimiter = rawEdifact.substring(indexOfSegmentDelimiter - 1, indexOfSegmentDelimiter);
			// logger.finest("Index segment delimiter:
			// "+indexOfSegmentDelimiter+" SegmentDelimiter
			// :"+segmentDelimiter+"\n"+rawEdifact);
			componentDataSeparator = rawEdifact.substring(3, 4);
			fieldDelimiter = rawEdifact.substring(4, 5);
			releaseChar = rawEdifact.substring(6, 7);
			decimalSep = rawEdifact.substring(5, 6);
		}
		Hashtable<String, String> initialValues = getInitialValues(rawEdifact, segmentDelimiter, componentDataSeparator);
		// String messageName = (String)initialValues.get("messageName");
		StringBuffer buffyRootTagName = new StringBuffer();
		buffyRootTagName.append((String) initialValues.get("messageOrganization"));
		buffyRootTagName.append("_");
		buffyRootTagName.append((String) initialValues.get("messageName"));
		buffyRootTagName.append("_");
		buffyRootTagName.append((String) initialValues.get("messageVersion"));
		String xsdFileName = buffyRootTagName.toString();
		logger.log(Level.INFO, "xsdFileName " + xsdFileName);

		rootAttribs.addAttribute("http://www.w3.org/2001/XMLSchema-instance",
				"xmlns:xsi", "xmlns:xsi", "CDATA",
				"http://www.w3.org/2001/XMLSchema-instance");
		rootAttribs.addAttribute("http://www.w3.org/2001/XMLSchema-instance",
				"", "xsi:noNamespaceSchemaLocation", "CDATA", xsdFileName
				+ ".xsd");
		rootAttribs.addAttribute("http://www.w3.org/2001/XMLSchema-instance",
				"", "xmlns:ns0", "CDATA", "urn:sapstern.com:" + xsdFileName);

		contentHandler.startDocument();

		EdifactSegment tree;
		try
		{
			tree = setupTreeOfEdifactSegments(rawEdifact, xsdFileName);
		}
		catch (ParserConfigurationException e)
		{
			// TODO Auto-generated catch block
			throw new SAXException(e);
		}
		traverseEdifactSegment(tree);
		contentHandler.endDocument();
		logger.exiting("EdifactSaxParserToXML", "parse");
	}

	public void setContentHandler(ContentHandler handler)
	{
		contentHandler = handler;
	}

	public ContentHandler getContentHandler()
	{
		return contentHandler;
	}

	public boolean getFeature(String s)
	{
		return false;
	}

	public void setFeature(String s, boolean b)
	{
	}

	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToXMLIF#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String s)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToXMLIF#setProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setProperty(String s, Object o)
	{
	}

	public void setEntityResolver(EntityResolver e)
	{
	}

	public EntityResolver getEntityResolver()
	{
		return null;
	}

	public void setDTDHandler(DTDHandler d)
	{
	}

	public DTDHandler getDTDHandler()
	{
		return null;
	}

	public void setErrorHandler(ErrorHandler handler)
	{
	}

	public ErrorHandler getErrorHandler()
	{
		return null;
	}



	/**
	 * @param flatEdifact
	 * @param messageName
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private EdifactSegment setupTreeOfEdifactSegments(String flatEdifact, String messageName) throws IOException, SAXException,	ParserConfigurationException
	{
		logger.entering("EdifactSaxParserToXML", "setupTreeOfEdifactSegments");
		logger.finest("messageName " + messageName);
		EdifactSegment rootSegment = new EdifactSegment(EDIFACT_ROOT_SEGMENT_NAME, true);
		this.theMessageName = messageName;
		xsdDOM = setupXSDDOM(messageName + ".xsd");

		//setup Hashtable of DOM Elements
		this.theElementTab = getElementTable(xsdDOM);

		this.rawListOfAllEdifactSegments = setupRawSegmentList(flatEdifact);
		Element theRootElement = xsdDOM.getDocumentElement();
		//rootSegment contains all the other segments in correct order
		logger.finest(new java.util.Date(System.currentTimeMillis()).toString()+": "+"EdifactSaxParserToXML().setupTreeOfEdifactSegments()==> start setup tree" );
		rootSegment = setupSegment(rootSegment, theRootElement);
		logger.finest(new java.util.Date(System.currentTimeMillis()).toString()+": "+"EdifactSaxParserToXML().setupTreeOfEdifactSegments()==> finish setup tree" );		
		logger.finest(new java.util.Date(System.currentTimeMillis()).toString()+": "+"EdifactSaxParserToXML().setupTreeOfEdifactSegments()==> start refining tree" );
		walkSegmentTreeFillSegmentFields(rootSegment);
		logger.finest(new java.util.Date(System.currentTimeMillis()).toString()+": "+"EdifactSaxParserToXML().setupTreeOfEdifactSegments()==> finish refining tree" );		
		//printSegmentTree(rootSegment, "");
		//CKE
		logger.finest("resultList ok");


		logger.finest("rootSegment ok");
		logger.exiting("EdifactSaxParserToXML", "setupTreeOfEdifactSegments");

		return rootSegment;
	}








	/**
	 * retrieves a Hashtable of initial values from UNB/UNH segment
	 * 
	 * @param flatEdifactMessage :
	 *            the input message string
	 * @param segmentDelimiter :
	 *            most cases '
	 * @param subFieldDelimiter :
	 *            most cases :
	 * @return : the message name / the message version / the UNA segment
	 *         content
	 * @throws SAXException :
	 *             thrown if we can't find message name and / or version
	 */
	private Hashtable<String, String> getInitialValues(	String flatEdifactMessage, String segmentDelimiter,	String subFieldDelimiter) throws SAXException
	{
		Hashtable<String, String> resultTab = new Hashtable<String, String>();
		StringTokenizer toki = new StringTokenizer(flatEdifactMessage, segmentDelimiter);

		while (toki.hasMoreTokens())
		{
			String edifactSegment = toki.nextToken().trim();
			if (edifactSegment.startsWith("UNH"))
			{
				resultTab = getMessageNameAndVersion(subFieldDelimiter,	edifactSegment);			
				break;
			}
		}
		resultTab.put("messageOrganization", EDIFACT_ROOT_SEGMENT_NAME);
		return resultTab;
	}

	/**
	 * @param subFieldDelimiter
	 * @param theUnhSegment
	 * @return
	 */
	private Hashtable<String, String> getMessageNameAndVersion(String subFieldDelimiter, String theUnhSegment)
	{
		Hashtable<String, String> resultTab = new Hashtable<String, String>();
		String theParseString = null;
		StringTokenizer theFieldTokenizer = new StringTokenizer(theUnhSegment,
				fieldDelimiter);
		while (theFieldTokenizer.hasMoreElements())
			theParseString = theFieldTokenizer.nextToken();
		StringTokenizer theTokenizer = new StringTokenizer(theParseString,
				subFieldDelimiter);
		int theNumberOfTokens = theTokenizer.countTokens();
		for (int theIndex = 0; theIndex < theNumberOfTokens; theIndex++)
		{
			String theLocalToken = (String) theTokenizer.nextToken();
			switch (theIndex)
			{
			case 0:
				resultTab.put("messageName", theLocalToken);
				break;
			case 2:
				resultTab.put("messageVersion", theLocalToken);
				break;
			}
		}
		return resultTab;
	}
	/**
	 * @param theResultSegment
	 * @param currentDomElement
	 * @return
	 */
	protected EdifactSegment setupSegment(EdifactSegment theResultSegment, Element currentDomElement)
	{

		// Sind noch weitere Kinder vorhanden			
		if (currentDomElement.hasChildNodes())
		{
			NodeList nodes = currentDomElement.getChildNodes();
			//Iteriere ueber die Kinder des aktuellen DOM Elementes
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);

				if (node instanceof Element)
				{
					// a child element to process
					Element child = (Element) node;

					//Hier kommt ggf eine Liste zurueck, weil wir mehrere Edifact Segmente des gleichen Typs hintereinander haben koennen
					List<EdifactSegment> theSegmentList = findEdifactSegmentForElement(child);
					if (theSegmentList != null)
					{
						// rekursiver Aufruf fuer das, bzw die Kindersegment
						// (schauen, ob da noch weitere Kinder drunter sind
						for (int k = 0; k < theSegmentList.size(); k++)
						{
							EdifactSegment currentSegment = theSegmentList.get(k);
							currentSegment = setupSegment(currentSegment, child);
							theResultSegment.childSegments.add(currentSegment);
						}
					}
				}
			}
			return theResultSegment;
		}
		else
			return theResultSegment;
	}
	/**
	 * Schaut in der Liste der Edifact Segmente nach, ob fuer das xsd Segment Element ein EDIFACT Segment vorhanden ist
	 * Es koennen ggf mehrere Segmente sein, deshalb wird eine Liste zurueckgegeben 
	 * @param currentElement 
	 * @return
	 */	
	protected List<EdifactSegment> findEdifactSegmentForElement(Element currentElement)
	{
		List<EdifactSegment> resultList = new LinkedList<EdifactSegment>();

		List<Element> theCildNodesOfComplexType = getNodeListOfComplexType(currentElement.getChildNodes());

		if( theCildNodesOfComplexType == null )
			return resultList;
		for ( int j=0; j<theCildNodesOfComplexType.size();j++)
		{
			Element theElement = theCildNodesOfComplexType.get(j);

			String nameOfAttrib = theElement.getAttribute("ref");
			//Handle group and meta elements

			if (nameOfAttrib.startsWith("G_") | nameOfAttrib.startsWith("M_")) 
			{	        	
				//First: get the cardinality
				long maxOccurs = getMaxOccurs(theElement);
				//Element theCheckElement = getElementByAttributeValueFromDOM( nameOfAttrib, this.theDOMElementList );
				Element theCheckElement = (Element)this.theElementTab.get(nameOfAttrib);
				List<Element> theCheckCildNodesOfComplexType = getNodeListOfComplexType(theCheckElement.getChildNodes());
				for ( int i=0; i<maxOccurs; i++ )
				{
					//check whether we have valid segments for this segment group 
					//if (globalIndexOfNextEdifactSegment>=rawListOfAllEdifactSegments.size())
					//break;	        			        		
					if (theCheckCildNodesOfComplexType==null)
						break;
					boolean foundSegment = false;
					for (Element localElement : theCheckCildNodesOfComplexType) {
						String nameOfAttribOfCheckNode = localElement.getAttribute("ref");
						EdifactSegment checkEdifactSegment = rawListOfAllEdifactSegments.get(0);
						if (checkEdifactSegment.segmentName.equals(nameOfAttribOfCheckNode))
							foundSegment = true;
						break;
					}
					if(!foundSegment)
						break;
					//success, we have found a Edifact segment in the string of raw Edifact which
					//belongs to the current xsd child G_ or M_ element
					EdifactSegment theCurrentSegment = new EdifactSegment(nameOfAttrib, true);
					//Element theElement = getElementByAttributeValueFromDOM(nameOfAttrib, this.theDOMElementList);   		
					Element theElementFromEleTab = (Element)this.theElementTab.get(nameOfAttrib);	 
					//recursive call of setupSegment for this segment group
					theCurrentSegment = setupSegment(theCurrentSegment, theElementFromEleTab );

					if (theCurrentSegment.childSegments != null && !theCurrentSegment.childSegments.isEmpty())
						resultList.add(theCurrentSegment);

				}
				continue;
				//end of G_ or M_ processing
			}
			if (rawListOfAllEdifactSegments==null||rawListOfAllEdifactSegments.isEmpty())
				break;
			//Lookup of segment inside UN/EDIFCT string

			while (!rawListOfAllEdifactSegments.isEmpty())
			{

				EdifactSegment currentSegment = rawListOfAllEdifactSegments.get(0);

				if (currentSegment.segmentName.equals(nameOfAttrib))
				{

					//success, we have found a Edifact segment in the string of raw Edifact which
					//belongs to the current xsd child element

					//Assign associated DOM Element to segment object we need it later while setting up the fields of the segment
					//currentSegment.theElement = getElementByAttributeValueFromDOM(nameOfAttrib, this.theDOMElementList);   		
					currentSegment.theElement = (Element)this.theElementTab.get(nameOfAttrib);	        		   		
					resultList.add(currentSegment);
					rawListOfAllEdifactSegments.remove(0);
					//lookup of next segment,if it has a different segment name we get out of the loop

					if ( !rawListOfAllEdifactSegments.isEmpty() )
					{
						EdifactSegment nextSegment = rawListOfAllEdifactSegments.get(0);
						if ( !nextSegment.segmentName.equals(nameOfAttrib) )
							break;
						else
						{
							//Check the cardinality
							if (getMaxOccurs(theElement) <= 1)
								break;
						}
					}

				}
				else
					break;
			}
		}
		return resultList;
	}
	/**
	 * @param theNode
	 * @return
	 */
	protected long getMaxOccurs(Node theNode)
	{
		long result = 1;
		String stringMaxOccurs = ((Element)theNode).getAttribute("maxOccurs");
		if (stringMaxOccurs !=null && !stringMaxOccurs.equals(""))
		{
			if (stringMaxOccurs.equalsIgnoreCase("unbounded"))
				result = 9999999;
			else
				result = Long.parseLong(stringMaxOccurs);
		}
		return result;
	}
	/**
	 * @param rootSegment
	 */
	protected void walkSegmentTreeFillSegmentFields(EdifactSegment rootSegment) throws SAXException
	{
		// Here we implement the split of the raw segment string in the segment fields
		refineEdifactSegment(rootSegment);
		for (int i = 0; i < rootSegment.childSegments.size(); i++)
		{
			EdifactSegment theChildSegment = rootSegment.childSegments.get(i);
			walkSegmentTreeFillSegmentFields(theChildSegment);
		}

	}
	/**
	 * Setup of all the segment fields and subfields for all the EdifactSegment
	 * objects<br>
	 * 
	 * @param EdifactSegment
	 *            segmentObject: The root EdifactSegment object containing<br>
	 *            all the other children EdifactSegment segment objects<br>
	 * @return EdifactSegment : The properly setup root segment<br>
	 */
	protected EdifactSegment refineEdifactSegment(EdifactSegment segmentObject) throws SAXException
	{
		logger.entering("EdifactSaxParserToXML", "refineEdifactSegment");
		logger.log(Level.FINE, "segment " + segmentObject.segmentName);
		//Input wasawasa 20191111
		stripSegmentString (segmentObject);
		//Spezialbehandlung UNA Segment notwendig CKE
		if (segmentObject.segmentName.equals("S_UNA"))
			return processUNASegmentObject(segmentObject);
		//obtain a StringTokenizer including the separator tokens (+ in most of the cases)
		StringTokenizerEscape loopToki = new StringTokenizerEscape(segmentObject.segmentString, fieldDelimiter, true, releaseChar);
		List<String> tokenList = loopToki.getAllTokens();
		boolean isNextFieldEmpty = false; 
		if (tokenList.isEmpty())
			return segmentObject;
		segmentObject.segmentFields = new LinkedList<EdifactField>();
		// Hier muss ueber den aktuellen DOM Subtree geloopt werden und nicht ueber den Edifact String
		Element domElement = segmentObject.theElement;
		List<Element> childElementList = getNodeListOfComplexType(domElement.getChildNodes());


		int index = 0;
		//Iteriere ueber die Kinder des aktuellen DOM Elementes
		for (int i = 0; i < childElementList.size(); i++)
		{
			Element locElem = childElementList.get(i);

			if (isNextFieldEmpty) 
			{
				isNextFieldEmpty=false;
				continue;
			}                     
			// a child element to process				
			String nameOfAttrib = locElem.getAttribute("ref");
			//Handle data elements
			if (nameOfAttrib.startsWith("D_"))
			{		
				if (tokenList.size() <= index)
					break;
				index = processDOMElementToken(tokenList, index, nameOfAttrib, segmentObject.segmentFields);
				continue;
			}
			//Handle elements of complex type eg.: UNOC:3
			if (nameOfAttrib.startsWith("C_"))
			{

				EdifactField currentCompositeField = new EdifactField(nameOfAttrib);

				if (tokenList.size() <= index)		        		
					break;		        	
				//get token
				String currentToken = tokenList.get(index);
				if (currentToken.equals(fieldDelimiter))
				{						
					index++;
					if (tokenList.size() <= index)
						break;						
					//This should always be as the tokenizer produces the delimiters as tokens, except for the beginning
					currentToken = tokenList.get(index);
					if (currentToken.equals(fieldDelimiter))
						//empty Field like BGM+++						
						continue;						
					else 
					{
						int nextIndex = 0;
						nextIndex= nextIndex + index;
						nextIndex++;
						int nextNextIndex = nextIndex;
						nextNextIndex++;
						if(tokenList.size() > nextIndex && tokenList.size() >= nextNextIndex && tokenList.get(nextIndex).equals(fieldDelimiter) && tokenList.get(nextNextIndex).equals(fieldDelimiter))
						{
							isNextFieldEmpty=true;
						}
					} 
				}


				//Lookup the DOM Element holding the definition of the current composite field
				Node theCurrentChildNodeDef = this.theElementTab.get(nameOfAttrib);
				if(theCurrentChildNodeDef==null)
					throw new SAXException("No DOM Node found for: "+nameOfAttrib+" probably XSD not correct");

				List<Element> childElementsOfChildList = null;
				for (int p=0;p<theCurrentChildNodeDef.getChildNodes().getLength();p++)
				{
					Node nodeOfChildDef = theCurrentChildNodeDef.getChildNodes().item(p);

					if (nodeOfChildDef instanceof Element)
					{
						if (nodeOfChildDef.getNodeName().contains("xsd:complexType"))
						{			    				
							childElementsOfChildList = getNodeListOfComplexType(nodeOfChildDef.getChildNodes());
							break;
						}			    				
					}
				}
				//split composite field
				StringTokenizerEscape fieldTokenizerSubFields = new StringTokenizerEscape(currentToken, componentDataSeparator, true, releaseChar);
				List<String> subFieldTokenList = fieldTokenizerSubFields.getAllTokens();
//				if (segmentObject.segmentName.equals("S_IMD")||segmentObject.segmentName.equals("S_AGR"))
//					System.out.println(segmentObject.segmentName+" name of composite: "+nameOfAttrib+" Size of composite element list: "+childElementsOfChildList.size()+" size of subfield token list: "+subFieldTokenList.size());
				boolean wasComponentDataSeparatorLast = false;
				int indexSubfields = 0;
				//Match composite DOM structure to composite from EDI 
				for (int j = 0; j < childElementsOfChildList.size(); j++)
				{
					Element theCurrentElement =  childElementsOfChildList.get(j);
//					if (segmentObject.segmentName.equals("S_IMD")||segmentObject.segmentName.equals("S_AGR"))
//						System.out.println(theCurrentElement.getAttribute("ref"));
					if ( subFieldTokenList.size() <= indexSubfields )
						break;
					String currentSubFieldToken = subFieldTokenList.get(indexSubfields);		    					
					if (currentSubFieldToken.equals(componentDataSeparator))
					{

						if ( subFieldTokenList.size() <= indexSubfields )
							break;
											
						if (indexSubfields!=0 && !wasComponentDataSeparatorLast)
						{	
							//We have to use the same DOM element during the next loop cycle
							//so we decrease the index by 1
							j--;
						}
						wasComponentDataSeparatorLast=true;
						indexSubfields++;				
					}
					else
					{
						wasComponentDataSeparatorLast=false;
						EdifactSubField currentSubField = new EdifactSubField(theCurrentElement.getAttribute("ref"), currentSubFieldToken, releaseChar);
						currentCompositeField.subFields.add(currentSubField);
						indexSubfields++;	
					}

				}
				//Remove the composite field token
				tokenList.remove(index);
				//increase the index
				index++;
				segmentObject.segmentFields.add(currentCompositeField);		    			
			}
		}

		logger.exiting("EdifactSaxParserToXML", "refineEdifactSegment");
		return segmentObject;
	}

	/**
	 * @param tokenList
	 * @param index
	 * @param nameOfAttrib
	 * @return
	 */
	private int processDOMElementToken(List<String> tokenList, int index, String nameOfAttrib, List<EdifactField> fieldList)
	{
		//get token
		String currentToken = tokenList.get(index);

		if (!currentToken.equals(fieldDelimiter))
		{
			//found a value for the current field from DOM so we setup a field and attach it to list of fields
			EdifactField currendField = new EdifactField(nameOfAttrib,currentToken, releaseChar);	
			fieldList.add(currendField);
			index++;//MFRI20180524
		}
		else
		{
			index++;
			if (tokenList.size() <= index )
				return index;
			currentToken = tokenList.get(index);
			if (!currentToken.equals(fieldDelimiter))
			{
				//found a value for the current field from DOM so we setup a field and attach it to list of fields
				EdifactField currendField = new EdifactField(nameOfAttrib,currentToken, releaseChar);	
				fieldList.add(currendField);
				index++;//MFRI20180524
			}

		}
		//index++;//MFRI20180524

		return index;
	}

	private EdifactSegment processUNASegmentObject(EdifactSegment segmentObject)
	{
		// TODO Auto-generated method stub

		List<EdifactField> theFields = new LinkedList<EdifactField>();
		theFields.add( new EdifactField("D_UNA1", componentDataSeparator, "") );
		theFields.add( new EdifactField("D_UNA2", fieldDelimiter, "") );
		theFields.add( new EdifactField("D_UNA3", decimalSep, "") );
		theFields.add( new EdifactField("D_UNA4", releaseChar, "") );
		theFields.add( new EdifactField("D_UNA5", "*", "") );

		theFields.add( new EdifactField("D_UNA6", segmentDelimiter, "") );
		segmentObject.segmentFields = theFields;
		return segmentObject;
	}


	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToXMLIF#setupRawSegmentList(java.lang.String)
	 */
	@Override
	public List<EdifactSegment> setupRawSegmentList(String flatEdifact) throws SAXException
	{
		logger.entering("EdifactSaxParserToXML", "setupRawSegmentList");

		List<EdifactSegment> resultList = new LinkedList<EdifactSegment>();
		// Setup a tokenizer holding all the segments
		// StringTokenizer toki = new StringTokenizer(flatEdifact,
		// segmentDelimiter);
		StringTokenizerEscape toki = new StringTokenizerEscape(flatEdifact,	segmentDelimiter, false, releaseChar);
		// logger.finest("Edifact Segment: "+flatEdifact);

		while (toki.hasMoreTokens())
		{

			String edifactSegment = toki.nextToken().trim();

			EdifactSegment segmentObject = getEdifactSegment(edifactSegment);
			resultList.add(segmentObject);
		}
		logger.exiting("EdifactSaxParserToXML", "setupRawSegmentList");
		return resultList;
	}

	/**
	 * @param edifactSegment
	 * @return
	 */
	private EdifactSegment getEdifactSegment(String edifactSegment) throws SAXException
	{
		logger.entering("EdifactSaxParserToXML", "getEdifactSegment");
		if (edifactSegment.length() < 3 )
			throw new SAXException("Errornous EDIFACT segment, aborting: "+edifactSegment);
		EdifactSegment segmentObject = new EdifactSegment("S_" + edifactSegment.substring(0,3), false);
		segmentObject.segmentString = edifactSegment.substring(3);
		logger.exiting("EdifactSaxParserToXML", "getEdifactSegment");
		return segmentObject;
	}



	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToXMLIF#addSubField(java.lang.String, java.lang.String, com.sapstern.openedifact.sax.parse.segments.EdifactField)
	 */
	@Override
	public void addSubField(String fieldTagName, String subFieldValue, EdifactField fieldObject)
	{

		EdifactSubField subField = new EdifactSubField(fieldTagName, subFieldValue, releaseChar);
		fieldObject.subFields.add(subField);
	}

	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToXMLIF#parse(java.lang.String)
	 */
	@Override
	public void parse(String uri)
	{
	}


	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToXMLIF#parseEdifact(java.lang.String)
	 */
	@Override
	public String parseEdifact(String inputEDIFACT)	throws TransformerException, IOException
	{
		logger.finest(new java.util.Date(System.currentTimeMillis()).toString()+": "+"EdifactSaxParserToXML().parseEifact( )==> start transformation" );
		byte[] textBytes = inputEDIFACT.getBytes(encoding);
		ByteArrayInputStream barryIn = new ByteArrayInputStream(textBytes);
		InputSource inputSource = new InputSource(barryIn);

		inputSource.setEncoding(encoding);

		ByteArrayOutputStream barryOut = parseEdifact(inputSource);
		String result = new String(barryOut.toByteArray(), encoding);
		logger.finest(new java.util.Date(System.currentTimeMillis()).toString()+": "+"EdifactSaxParserToXML().parseEifact()==> finished transformation" );
		return result;
	}

	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToXMLIF#parseEdifact(org.xml.sax.InputSource)
	 */
	@Override
	public ByteArrayOutputStream parseEdifact(InputSource inputEDIFACT)	throws TransformerException, UnsupportedEncodingException, IOException
	{
		logger.finest(new java.util.Date(System.currentTimeMillis()).toString()+": "+"EdifactSaxParserToXML().parseEifact( )==> start transformation" );
		logger.entering("EdifactSaxParserToXML", "parseEdifact");
		XMLReader parser = this;
		logger.log(Level.FINE, "saxSource next");
		SAXSource saxSource = new SAXSource(parser, inputEDIFACT);
		logger.log(Level.FINE, "saxSource ok");
		logger.log(Level.FINE, "set encoding next");
		inputEDIFACT.setEncoding(encoding);
		logger.log(Level.FINE, "encoding set ok");
		ByteArrayOutputStream barryOut = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(barryOut);
		logger.log(Level.FINE, "streamResult ok");
		transformer.transform(saxSource, streamResult);
		logger.log(Level.FINE, "transform ok");

		barryOut.flush();
		logger.finest(new java.util.Date(System.currentTimeMillis()).toString()+": "+"EdifactSaxParserToXML().parseEifact()==> finished transformation" );
		logger.exiting("EdifactSaxParserToXML", "parseEdifact");
		return barryOut;
	}


	/**
	 * Input wasawasa 20191111
	* Trennzeichen am Ende sind im Edifact zulässig, aber nicht sinnvoll.
	* Die bringen auch den Parser ins stolpern.
	* Bevor ich den Parser umbaue, werfe ich den unnötigen Ballast einfach weg.
	*/
	protected void stripSegmentString (EdifactSegment segmentObject)
	{
	  if (segmentObject == null) return;
	  String segmentString = segmentObject.segmentString;
	  if ((segmentString == null) || (segmentString.isEmpty())) return;
	  // ToDo: Bei ganz kurzen Strings muss man noch mal drüber nachdenken
	  while (true)
	    {
	    if (segmentString.length() < 2) return;
	    // Ist das vorletzte Zeichen der Releas Character, dann darf am Ende ein Delimiter stehen
	    char lc = segmentString.charAt(segmentString.length() - 1);
	    char sc = segmentString.charAt(segmentString.length() - 2);
	    // Ist das vorletzte Zeichen ein Releas Char, ist alles gut
	    if (sc == releaseChar.charAt(0)) return;
	    // Wenn das letzte Zeichen kein Separator und kein Delimiter ist, ist auch gut.
	    if (!(lc == fieldDelimiter.charAt(0)) && !(lc == componentDataSeparator.charAt(0))) return;
	    segmentString = segmentString.substring(0, segmentString.length() -1);
	    segmentObject.segmentString = segmentString;
	  }
	}



}
