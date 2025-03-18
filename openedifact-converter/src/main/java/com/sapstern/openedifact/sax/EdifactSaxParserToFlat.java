/**
 * Free Message Converter Copyleft 2007 - 2017 Matthias Fricke mf@sapstern.com


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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  <BR><BR>
 * <B>Class/Interface Description: </B><BR>
 * Instance class for SAX parsing of EDIFACT-XML (ISO TS 20625) to UN/EDIFACT<BR>
 * <BR><BR>
 * @author Matthias Fricke
 * <DT><B>Known Bugs:</B><BR><!-- Keep in mind to update method bug lists -->
 * none <BR><BR>
 * <DT><B>History:</B>
 * <PRE><!-- Do not use tabs in the history table! Do not extend table width! rel.inc defines release and increment no -->
 * date       name           rel.inc  changes
 * -----------------------------------------------------------------------------------------------------------
 * 28.09.05   fricke         1.0      created 
 * 17.02.09	  Otto Frost     1.1	  Bugfix: handling of Edifact release char
 * 19.06.14   fricke         2.0      Adapted to the XML/EDIFACT standard  (ISO TS 20625)
 * 11.08.17   fricke         2.1      Fix problem with release char MFRI20170811
 * 18.09.2017 fricke		 2.2	  refactoring
 * 26.01.2018 fricke         2.3	  incorporate check of fields per segment against DOM xsd	
 * 24.04.2018 fricke		 2.4      check of fields per segment against DOM xsd completly rewritten
 * 07.05.2018 fricke		 2.5	  bugfixing for check of fields per segment 
 * 22.07.2018 fricke	     2.6      parsing of segment structure rewritten
 * 13.03.2024 fricke         2.7      incorporated minor bugfix proposal by github user Abifen 
 * 31.08.2024 fricke         2.8      reworked UNA processing to deal with <ns1:S_UNA>:+.? &apos;</ns1:S_UNA>
 * -----------------------------------------------------------------------------------------------------------</PRE>
 *
 *****************************************************************/
/**
 * @author matthias
 *
 */
public class EdifactSaxParserToFlat extends AbstractEdifactParser implements EdifactSaxParserToFlatIF {


	private SAXParser saxParser = null;
	private DefaultHandler handler = null;
	private SAXParserFactory factory = null;

	private StringBuilder buffy = null;

	private boolean isUNA01 = false;

	private boolean isUNA02 = false;

	private boolean isUNA03 = false;

	private boolean isUNA04 = false;

	private boolean isUNA05 = false;

	private boolean isUNA06 = false;

	private boolean isSUNA = false;

	private StringBuffer sbUNA = new StringBuffer("");
	private String segmentDelimiter = "'";
	private String fieldDelimiter = "+";
	private String componentDataSeparator = ":";
	private String releaseChar = "?";
	private String repetitionChar = " ";
	private String messageName = null;
	private String messageVersion = null;

	private Element currentXSDSegment = null;


	private Document theXSDDom = null;


	private List<Hashtable<String, String>> currentSegmentXSDMemberList = null;

	private List<Hashtable<String, String>> currentSegmentInstanceMemberList = null;  //MFRI20180721
	private StringBuilder buffyCharacters = null;
	private String currentComposite = null;
	//Set if nsPrf is known in advance
	private String namespacePrefix = null;
	private String xmlEncoding = null;
	



	/**
	 * The constructor to be used internally for this SAX parser instance
	 * if nsPref is known in advance
	 * @throws ParserConfigurationException 
	 */
	private EdifactSaxParserToFlat(boolean isValidate, String nsPref, String xmlEncoding) throws SAXException, ParserConfigurationException {

		this(isValidate, nsPref);
		this.xmlEncoding = xmlEncoding;

	}
	/**
	 * The constructor to be used internally for this SAX parser instance
	 * if nsPref is known in advance
	 * @throws ParserConfigurationException 
	 */
	private EdifactSaxParserToFlat(boolean isValidate, String nsPref) throws SAXException, ParserConfigurationException {

		this(isValidate);
		this.namespacePrefix = nsPref;

	}

	/**
	 * The constructor to be used internally for this SAX parser instance
	 * @throws ParserConfigurationException 
	 */
	private EdifactSaxParserToFlat(boolean isValidate) throws SAXException, ParserConfigurationException {
		super();
		factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(isValidate);
		saxParser = factory.newSAXParser();
		this.xmlEncoding = "UTF8";
		handler = this;


	}

	/**
	 * Factory for this Parser
	 * @param isValidate
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws Exception
	 */
	public static EdifactSaxParserToFlatIF factory(boolean isValidate) throws SAXException, ParserConfigurationException
	{
		return new EdifactSaxParserToFlat(isValidate);
	}

	/**
	 * Factory for this Parser specify namespace prefix
	 * @param isValidate
	 * @param nsPref
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws Exception
	 */
	public static EdifactSaxParserToFlatIF factory(boolean isValidate, String nsPref) throws SAXException, ParserConfigurationException
	{
		return new EdifactSaxParserToFlat(isValidate, nsPref);
	}

	/**
	 * Factory for this Parser specify namespace prefix and encoding
	 * @param isValidate
	 * @param nsPref
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws Exception
	 */
	public static EdifactSaxParserToFlatIF factory(boolean isValidate, String nsPref, String xmlEncoding) throws SAXException, ParserConfigurationException
	{
		return new EdifactSaxParserToFlat(isValidate, nsPref, xmlEncoding);
	}

	@Override
	public String parse(String inputXML) throws SAXException, UnsupportedEncodingException, IOException {
		return parse(inputXML, namespacePrefix );
	}

	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#parse(java.lang.String)
	 */
	public String parse(String inputXML, String theNamespacePrefix) throws SAXException, UnsupportedEncodingException, IOException {

		if(theNamespacePrefix!=null && !theNamespacePrefix.equals("") && !theNamespacePrefix.endsWith(":"))
			theNamespacePrefix = theNamespacePrefix+":";
		else	
			theNamespacePrefix = getNamespacePrefix(inputXML, theNamespacePrefix);

		initGlobalVariables();
		inputXML = inputXML.replaceAll("&apos;", "'");

		int index1 = inputXML.indexOf("<"+theNamespacePrefix+"D_0065>") + ("<"+theNamespacePrefix+"D_0065>").length();
		int index2 = inputXML.indexOf("</"+theNamespacePrefix+"D_0065>");
		if(index1==-1||index2==-1)
			throw new SAXException("Parsing of UNH failed, no D_0065 found, possible problem with namespaceprefix, check XML");

		// 01ottfro should add code to handle string out of bounds
		// if <D_0065> not found
		messageName = inputXML.substring(index1, index2);
		index1 = inputXML.indexOf("<"+theNamespacePrefix+"D_0054>") + ("<"+theNamespacePrefix+"D_0054>").length();
		index2 = inputXML.indexOf("</"+theNamespacePrefix+"D_0054>");
		// 01ottfro should add code to handle string out of bounds
		// if <S00903> not found
		messageVersion = inputXML.substring(index1, index2);
		index1 = inputXML.indexOf("<"+theNamespacePrefix+"D_0051>") + ("<"+theNamespacePrefix+"D_0051>").length();
		index2 = inputXML.indexOf("</"+theNamespacePrefix+"D_0051>");
		// 01ottfro should add code to handle string out of bounds
		// if <S00904> not found
		//String messageType = inputXML.substring(index1, index2);

		StringBuffer buffyXsdName = new StringBuffer();

		buffyXsdName.append("EDIFACTINTERCHANGE");
		buffyXsdName.append("_");
		buffyXsdName.append(messageName);
		buffyXsdName.append("_");
		buffyXsdName.append(messageVersion);
		//String messageName = buffyXsdName.toString();
		buffyXsdName.append(".xsd");
		String xsdName = buffyXsdName.toString();
		//Obtain an XSD document object
		theXSDDom = setupXSDDOM(xsdName);		
		theElementTab = getElementTable(theXSDDom);


		if (saxParser.isValidating()) {
			saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			saxParser.setProperty(JAXP_SCHEMA_SOURCE, Thread.currentThread().getContextClassLoader().getResourceAsStream(xsdName));
		}

		buffy = new StringBuilder();
		if (xmlEncoding!=null && !xmlEncoding.equals(""))
			saxParser.parse(new ByteArrayInputStream(inputXML.getBytes(xmlEncoding)), handler);
		else
			saxParser.parse(new ByteArrayInputStream(inputXML.getBytes()), handler);
		return buffy.toString();
	}


	/**
	 * try to find the namespace prefix
	 * @param inputXML
	 * @param theNamespacePrefix
	 * @return
	 */
	private String getNamespacePrefix(String inputXML, String theNamespacePrefix) {

		theNamespacePrefix = "";
		if (inputXML.indexOf("<"+theNamespacePrefix+"D_0065>")==-1)
		{

			for (int i=0; i<9; i++)
			{
				String localNamespacePrefix = "ns" + i;
				if(inputXML.indexOf("<"+localNamespacePrefix+":D_0065>")!=-1)
				{
					theNamespacePrefix = localNamespacePrefix+":";
					break;
				}
			}
		}
		return theNamespacePrefix;
	}



	/**
	 * Initialize global variables
	 */
	public void initGlobalVariables()
	{
		// TODO Auto-generated method stub

		isUNA01 = false;
		isUNA02 = false;
		isUNA03 = false;
		isUNA04 = false;
		isUNA05 = false;
		isUNA06 = false;
		isSUNA = false;
		sbUNA = new StringBuffer("");
		segmentDelimiter = "'";
		fieldDelimiter = "+";
		componentDataSeparator = ":";
		releaseChar = "?";
		repetitionChar = " ";

	}
	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#emit(java.lang.String)
	 */

	public void emit(String s) throws SAXException {

		buffy.append(s);

	}

	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#nl()
	 */
	@Override
	public void nl() throws SAXException {


	}

	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
	}

	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		//nl();
	}

	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceURI, String sName, // simple name (localName)
			String qName, // qualified name
			Attributes attrs) throws SAXException {

		String eName = sName; // element name
		if ("".equals(eName))
			eName = qName; // namespaceAware = false


		/**
		 * 	UNA Default service characters (ISO 9735-1)
		 * 
		 * 	These chars should be used in an Edifact interchange
		 * 
		 *	Representation		Functionality
		 *	Colon			:	component data element separator
		 *	Plus sign		+	data element separator
		 *	Question mark	?	release character
		 *	Asterisk		*	repetition separator
		 *	Apostrophe		'	segment terminator		
		 *
		 */
		if (eName.equals("D_UNA1")) {
			isUNA01 = true;
			isUNA02 = isUNA03 = isUNA04 = isUNA05 = isUNA06 = false;
			return;
		}
		if (eName.equals("D_UNA2")) {
			isUNA02 = true;
			isUNA01 = isUNA03 = isUNA04 = isUNA05 = isUNA06 = false;
			return;
		}
		if (eName.equals("D_UNA3")) {
			isUNA03 = true;
			isUNA01 = isUNA02 = isUNA04 = isUNA05 = isUNA06 = false;
			return;
		}
		if (eName.equals("D_UNA4")) {
			isUNA04 = true;
			isUNA01 = isUNA02 = isUNA03 = isUNA05 = isUNA06 = false;
			return;
		}
		if (eName.equals("D_UNA5")) {
			isUNA05 = true;
			isUNA01 = isUNA02 = isUNA03 = isUNA04 = isUNA06 = false;
			return;
		}
		if (eName.equals("D_UNA6")) {
			isUNA06 = true;
			isUNA01 = isUNA02 = isUNA03 = isUNA04 = isUNA05 = false;
			return;
		}

		isUNA01 = isUNA02 = isUNA03 = isUNA04 = isUNA05 = isUNA06 = isSUNA = false;

		if (eName.startsWith("S_")) {

			currentComposite = null;
			//Aufsuchen des aktuellen XSD DOM Elements zum Segment
			currentXSDSegment = (Element)theElementTab.get(eName);	
			currentSegmentXSDMemberList = getSegmentMembers(getNodeListOfComplexType(currentXSDSegment.getChildNodes()), false, "");

			currentSegmentInstanceMemberList = new LinkedList<Hashtable<String,String>>(); //MFRI20180721
			if (eName.equals("S_UNA")) { //Abifen*** no fieldDelimiter for UNA segment
				emit(eName.substring(2));
				isSUNA  = true;
				buffyCharacters = new StringBuilder(); //MFRI cover UNA Segment <ns1:S_UNA>:+.? &apos;</ns1:S_UNA> 
				return;
			}
			//Beginn des EDI segmentstrings
			emit(eName.substring(2)+fieldDelimiter);
			return;
		}

		if (eName.startsWith("C_")) {
			currentComposite = eName;
			return;
		}

		if (eName.startsWith("D_")) 
		{
			buffyCharacters = new StringBuilder();					
			return;
		}

	}



	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String namespaceURI, String sName, // simple name
			String qName // qualified name
			) throws SAXException {
		String eName = sName; // element name
		if (eName.equals("S_UNA")) {
			isSUNA = false;
			if(isUNA06)
				isUNA06 = false;
			else
				processUNA();
			return;
		}
		if (eName.equals("D_UNA1")) {
			isUNA01 = false;
			return;
		}
		if (eName.equals("D_UNA2")) {
			isUNA02 = false;
			return;
		}
		if (eName.equals("D_UNA3")) {
			isUNA03 = false;
			return;
		}
		if (eName.equals("D_UNA4")) {
			isUNA04 = false;
			return;			
		}
		if (eName.equals("D_UNA5")) {
			isUNA05 = false;
			return;
		}
		if (eName.equals("D_UNA6")) {
			//isUNA06 = false;
			processUNA();
			return;
		}
		if ("".equals(eName))
			eName = qName; // namespaceAware = false
		if (eName.startsWith("D_")) {	
			Hashtable<String, String> currentDataTab = new Hashtable<String, String>();//MFRI20180721
			//Interseroh/Edeka Spezialbehandlung fuehrende Nullen in die Uhrzeit <C_S004><D_0019> haengen
			String theData = buffyCharacters.toString();
			//			if(eName.equals("D_0019"))
			//			{
			//				for(int i=0;i<4;i++)
			//				{
			//					if(theData.length()>=4)
			//						break;
			//					theData = "0"+theData;
			//				}		
			//			}			
			currentDataTab.put(eName, theData);
			if(currentComposite!=null)
				currentDataTab.put("COMPLEX_TAG_NAME", currentComposite);
			currentSegmentInstanceMemberList.add(currentDataTab); //MFRI20180721
		}

		if (eName.startsWith("S_")) {
			String ediSegment = getEdiSegment(currentSegmentXSDMemberList, currentSegmentInstanceMemberList);
			buffy.append(ediSegment);
			buffy.append(segmentDelimiter);
			return;
		}

		if (eName.startsWith("C_")) {	
			currentComposite = null;
			return;
		}

	}
	
	
	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#characters(char[], int, int)
	 */
	@Override
	public void characters(char buf[], int offset, int len) throws SAXException 
	{
		String s = new String(buf, offset, len);
		if(isSUNA) {
			if (isUNA01)
				// We know that we are now dealing with UNA segment contents
				// We may not get all chars in one call...
			{			
				componentDataSeparator = s;
				sbUNA.append(componentDataSeparator);
				return;	
			} 
			if (isUNA02)
				// We know that we are now dealing with UNA segment contents
				// We may not get all chars in one call...
			{			
				fieldDelimiter = s;
				sbUNA.append(fieldDelimiter);
				return;	
			} 
			if (isUNA03)
				// We know that we are now dealing with UNA segment contents
				// We may not get all chars in one call...
			{
				sbUNA.append(buf,offset,len);
				return;	
			} 
			if (isUNA04)
				// We know that we are now dealing with UNA segment contents
				// We may not get all chars in one call...
			{
				releaseChar = s;
				sbUNA.append(releaseChar);			
				return;	
			} 
			if (isUNA05)
				// We know that we are now dealing with UNA segment contents
				// We may not get all chars in one call...
			{

				repetitionChar = s;		
				sbUNA.append(repetitionChar);
				return;	
			} 
			if (isUNA06)
				// We know that we are now dealing with UNA segment contents
				// We may not get all chars in one call...
			{
				segmentDelimiter = s;
				sbUNA.append(segmentDelimiter);			
				return;	
			} 
			sbUNA.append(s); //UNA segment like <ns1:S_UNA>:+.? &apos;</ns1:S_UNA>
			return;
		}

		if (!s.trim().equals("")) { // //MFRI20170811 Logic of Otto Frost re incorporated
			//System.err.println("AQ"+s.trim()+"Q");
			s = s.replaceAll("\\"+releaseChar,releaseChar+releaseChar);            //MFRI20170811
			s = s.replaceAll("\\"+componentDataSeparator,releaseChar+componentDataSeparator);//MFRI20170811
			s = s.replaceAll("\\"+fieldDelimiter,releaseChar+fieldDelimiter);      //MFRI20170811
			s = s.replaceAll("\\"+segmentDelimiter,releaseChar+segmentDelimiter);  //MFRI20170811	

			buffyCharacters.append(s.trim());
		}

	}
	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#ignorableWhitespace(char[], int, int)
	 */
	@Override
	public void ignorableWhitespace(char buf[], int offset, int Len) throws SAXException {
		//nl ();
	}



	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#endPrefixMapping(java.lang.String)
	 */
	@Override
	public void endPrefixMapping(String arg0) throws SAXException
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#processingInstruction(java.lang.String, java.lang.String)
	 */
	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#setDocumentLocator(org.xml.sax.Locator)
	 */
	@Override
	public void setDocumentLocator(Locator arg0)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#skippedEntity(java.lang.String)
	 */
	@Override
	public void skippedEntity(String arg0) throws SAXException
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF#startPrefixMapping(java.lang.String, java.lang.String)
	 */
	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException
	{
		// TODO Auto-generated method stub

	}



	/**
	 * Extract LinkedList holding the segment member names (D_ or C_
	 * @param segmentChildNodes
	 * @return
	 */
	private List<Hashtable<String, String>>  getSegmentMembers(List<Element> segmentChildNodes, boolean isComplex, String complexName) {

		List<Hashtable<String, String>> theResultList = new LinkedList<Hashtable<String,String>>();

		if (segmentChildNodes==null)
			return theResultList;
		for (int i=0; i<segmentChildNodes.size();i++)
		{
			Element localElement = segmentChildNodes.get(i);
			if (       localElement.getAttributes().getNamedItem("ref") != null 
					&& (   localElement.getAttributes().getNamedItem("ref").getNodeValue().startsWith("C_")
							||     localElement.getAttributes().getNamedItem("ref").getNodeValue().startsWith("D_") 
							)
					)
			{		
				if(localElement.getAttributes().getNamedItem("ref").getNodeValue().startsWith("D_"))
				{
					Hashtable<String, String> tab = new Hashtable<String, String>();
					tab.put("TAG_NAME", localElement.getAttributes().getNamedItem("ref").getNodeValue());
					if(isComplex)
					{
						tab.put("SEPARATOR", componentDataSeparator);
						tab.put("COMPLEX_TAG_NAME", complexName);						
					}
					else
						tab.put("SEPARATOR", fieldDelimiter);
					theResultList.add(tab);
					continue;
				}
				if (localElement.getAttributes().getNamedItem("ref").getNodeValue().startsWith("C_"))
				{
					complexName = localElement.getAttributes().getNamedItem("ref").getNodeValue();
					isComplex=true;
					List<Element> childNodes = getNodeListOfComplexType((super.theElementTab.get(localElement.getAttributes().getNamedItem("ref").getNodeValue())).getChildNodes());
					List<Hashtable<String, String>> complexMemberlist = getSegmentMembers(childNodes, isComplex, complexName);
					theResultList.addAll(complexMemberlist);
					isComplex=false;
					int theIndex = theResultList.size();
					if (theIndex>0)
						theIndex = theIndex - 1;
					Hashtable<String, String> lastMemberTab = theResultList.remove(theIndex);
					lastMemberTab.put("SEPARATOR", fieldDelimiter);
					theResultList.add(lastMemberTab);
				}
			}
		}

		return theResultList;
	}
	
	/**
	 * @param currentSegmentXSDMemberList: List of XSD Elements for this segment
	 * @param currentSegmentInstanceMemberList: List holding actual tagnames/values for this segment from XML instance
	 * @return
	 */
	private String getEdiSegment(List<Hashtable<String, String>> currentSegmentXSDMemberList, List<Hashtable<String, String>> currentSegmentInstanceMemberList) {
		// TODO Auto-generated method stub

		List<String> resultBuilder = new LinkedList<String>();

		for (int i=0;i<currentSegmentXSDMemberList.size();i++)
		{
			//Poppen des obersten Elements des XSD Segment Stacks
			Hashtable<String, String> xsdMemberTab = currentSegmentXSDMemberList.get(i);
			if(currentSegmentInstanceMemberList.isEmpty())
				break;
			Hashtable<String, String> instanceMemberTab = currentSegmentInstanceMemberList.get(0);
			String currentXSDTagName = xsdMemberTab.get("TAG_NAME");
			String currentInstanceValue = instanceMemberTab.get(currentXSDTagName);
			if(currentInstanceValue!=null)
			{
				currentSegmentInstanceMemberList.remove(0);				
				resultBuilder.add(currentInstanceValue);				
			}
			if(currentSegmentInstanceMemberList.isEmpty())
				break;
			Hashtable<String, String> tabTrailer = getTrailingSeparators(currentSegmentXSDMemberList, i, currentSegmentInstanceMemberList.get(0));
			String trailerString = tabTrailer.get("TRAILER_STRING");
			i  = Integer.parseInt(tabTrailer.get("INDEX"));			
			resultBuilder.add(trailerString);			
		}
		StringBuilder result = new StringBuilder();
		for (int i=0;i<resultBuilder.size();i++)
		{
			result.append(resultBuilder.get(i));
		}
		return result.toString();
	}

	/**
	 * @param currentSegmentXSDMemberList: List of XSD Elements for this segment
	 * @param startIndex: Index to start in XSD list
	 * @param tabFromInstance: Hashtable holding next tagname of actual XML instance
	 * @return
	 */
	private Hashtable<String, String> getTrailingSeparators(List<Hashtable<String, String>> currentSegmentXSDMemberList, int startIndex, Hashtable<String, String> tabFromInstance) {


		StringBuilder resultBuilder = new StringBuilder("");
		boolean isFirstInMultipleComposites = true;
		//Tagname nach dem in xsd Liste gesucht wird
		String tagNameFromInstance = null;
		Enumeration<String> keys = tabFromInstance.keys();
		while (keys.hasMoreElements()) {
			String nextKey = keys.nextElement();
			if (nextKey.startsWith("D_"))
			{				
				tagNameFromInstance = new String(nextKey);
				break;
			}
		}
		String compositeTagNameFromInstance = tabFromInstance.get("COMPLEX_TAG_NAME");
		StringBuilder componentDataSeparatorTempBuilder = new StringBuilder("");
		for (int i=startIndex;i<currentSegmentXSDMemberList.size();i++)
		{

			//hier Tagnamen vergleichen und ggf Trailer dranhaengen
			//den Index hochzaechlen!! ggf rekursiv
			Hashtable<String, String> xsdMemberTab = currentSegmentXSDMemberList.get(i);			
			String currentXSDTagName = xsdMemberTab.get("TAG_NAME");
			String currentComplexTagNameFromXSD = xsdMemberTab.get("COMPLEX_TAG_NAME");
			if(!currentXSDTagName.equals(tagNameFromInstance))
			{
				//tag der Instanz nicht gefunden: weiterfuehren des TrailerStrings
				startIndex++;
				String separator = xsdMemberTab.get("SEPARATOR");
				if (separator.equals(fieldDelimiter))
				{						
					componentDataSeparatorTempBuilder = new StringBuilder("");
					resultBuilder.append(fieldDelimiter);
				}
				if (separator.equals(componentDataSeparator))
				{					
					componentDataSeparatorTempBuilder.append(componentDataSeparator);			
				}
			}
			else
			{

				if(          compositeTagNameFromInstance!=null
						&& ! compositeTagNameFromInstance.equals("")
						&&   compositeTagNameFromInstance.length()==8
						)
				{
					//Nur fuer Spezialfaelle, wenn es mehrere gleichnamige composites hintereinander geben kann
					//Dann hat der Composite Tagname eine Laenge von 8
					//z.B:					
					//<S_PIA>
					//	<D_4347>1</D_4347>
					//	<C_C212>
					//		<D_7140>16202</D_7140>
					//		<D_7143>SA</D_7143>
					//		<D_3055>91</D_3055>
					//	</C_C212>
					//	<C_C212_3>
					//		<D_7140>16203</D_7140>
					//		<D_7143>SA</D_7143>
					//		<D_3055>15</D_3055>
					//	</C_C212_3>
					//</S_PIA>
					if(currentComplexTagNameFromXSD!=null&&!currentComplexTagNameFromXSD.equals(compositeTagNameFromInstance))
					{
						if (isFirstInMultipleComposites)
						{
							isFirstInMultipleComposites = false;
							resultBuilder = new StringBuilder("");
						}
						continue;
					}
				}
				resultBuilder.append(componentDataSeparatorTempBuilder.toString());
				if(resultBuilder.toString().equals(""))
				{
					startIndex++;
					resultBuilder.append(xsdMemberTab.get("SEPARATOR"));
				}
				break;	
			}

		}
		Hashtable<String, String> resultTab = new Hashtable<String, String>();
		resultTab.put("TRAILER_STRING", resultBuilder.toString());
		startIndex--;
		resultTab.put("INDEX", ""+startIndex);
		return resultTab;
	}

	private void processUNA() throws SAXException {
		String sUNA = sbUNA.toString(); 
		if (sUNA.contains(releaseChar+segmentDelimiter))
			sUNA.replace(releaseChar+segmentDelimiter, releaseChar+" "+segmentDelimiter);
		emit(sUNA);
	}

}
