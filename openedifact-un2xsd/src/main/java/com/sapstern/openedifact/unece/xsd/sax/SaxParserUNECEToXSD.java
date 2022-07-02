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

package com.sapstern.openedifact.unece.xsd.sax;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import com.sapstern.openedifact.unece.xsd.data.CompositeDataDefinition;
import com.sapstern.openedifact.unece.xsd.data.CompositeDefinition;
import com.sapstern.openedifact.unece.xsd.data.DataDef;
import com.sapstern.openedifact.unece.xsd.data.SegmentData;
import com.sapstern.openedifact.unece.xsd.data.SegmentStructureData;
import com.sapstern.openedifact.unece.xsd.data.SegmentStructureElement;
import com.sapstern.openedifact.unece.xsd.data.TypeDef;


/**
 *  <BR><BR>
 * <B>Class/Interface Description: </B><BR>
 * Instance class for SAX parsing of UN/ECE EDIFACT message description files to to XSD's
 * Was originally uses for parsing SEF files<BR>
 * <BR><BR>
 * @author Matthias Fricke
 * <DT><B>Known Bugs:</B><BR><!-- Keep in mind to update method bug lists -->
 * none <BR><BR>
 * <DT><B>History:</B>
 * <PRE><!-- Do not use tabs in the history table! Do not extend table width! rel.inc defines release and increment no -->
 * date       name           rel.inc  changes
 * -------------------------------------------------------------------------------------------------
 * 20.02.07   fricke         1.0      created 
 * 01.10.13	  fricke		 2.0      swapped from proprietary SEF format to UN/ECE website data descriptions
 * 11.11.19   fricke         2.1      changed definition of UNA / UNB segment to mandatory
 * 17.06.20   fricke         2.2      add license info to xsd files
 * -------------------------------------------------------------------------------------------------</PRE>
 *
 *****************************************************************/
public class SaxParserUNECEToXSD  implements XMLReader
{

   public static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
   public static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
   public static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

   private Transformer transformer = null;
   
   private ContentHandler contentHandler = null;

   private static AttributesImpl attribs = new AttributesImpl();
   private static AttributesImpl uneceParseAttribs = null;
   private static String namespaceURI = "";
   private List<SegmentData> theSegmentList = null;
   private List<SegmentStructureData> theSegmentStructureList = null;
   // MFRI bei Aufbau ControlSegments abfragen
   private Hashtable<String, Hashtable<String, String>> theDataElementTab = null;
   private Hashtable<String, CompositeDefinition> theCompositeTab = null;
  // MFRI bei Aufbau ControlSegments abfragen
   private Hashtable<String, SegmentStructureData> theSegmentTab = null;
   
   private Hashtable<String, Hashtable<String, String>> theElementTypeTab = null;
   
   private Hashtable<String, Integer> nonDeterministicObjects = null;
   
   private HashSet<String> usedElementsSet = null;
   
   private String messageName = null;
   private String messageType = null;
   private String theEncoding = null;
   
   static 
   {
	   attribs.addAttribute( namespaceURI, "", "composite", "CDATA", "true");
   }

   static final String LICENSE_STRING = "";
   
	/**
	 * The constructor to be used for this SAX parser instance
	 */
	public SaxParserUNECEToXSD(String messageName, String theEncoding) throws SAXException
	{
		super();

		this.theEncoding = theEncoding;
		this.nonDeterministicObjects = new Hashtable<String, Integer>();
		this.usedElementsSet = new HashSet<String>();
		
		try
		{
          SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

          TransformerHandler handler = saxTransformerFactory.newTransformerHandler();
          transformer    = handler.getTransformer();
          transformer.setOutputProperty(OutputKeys.ENCODING, theEncoding);
          transformer.setOutputProperty(OutputKeys.INDENT, "yes");
          transformer.setOutputProperty(OutputKeys.METHOD, "xml");
          transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
          
          uneceParseAttribs = new AttributesImpl();
          uneceParseAttribs.addAttribute( "http://www.w3.org/2001/XMLSchema", "xmlns:xsd", "xmlns:xsd", "CDATA", "http://www.w3.org/2001/XMLSchema");
          uneceParseAttribs.addAttribute( "urn:sapstern.com:EDIFACTINTERCHANGE_"+messageName, "targetNamespace", "targetNamespace", "CDATA", "urn:sapstern.com:EDIFACTINTERCHANGE_"+messageName);
          uneceParseAttribs.addAttribute( "urn:sapstern.com:EDIFACTINTERCHANGE_"+messageName, "xmlns", "xmlns", "CDATA", "urn:sapstern.com:EDIFACTINTERCHANGE_"+messageName);
          theElementTypeTab = new Hashtable<String, Hashtable<String,String>>();

		}
		catch ( TransformerConfigurationException tce )
		{
			throw new SAXException(tce);
		}
	}
	
	
	//===========================================================
	// XML Reader Interface Implementation
	//===========================================================
	public void parse(InputSource source) throws IOException, SAXException
	{
	   messageName = messageName.replace("D.", "");
	   //contentHandler.characters(ch, start, length);
	   contentHandler.startDocument();
	   contentHandler.startElement(namespaceURI, "xsd:schema", "xsd:schema", uneceParseAttribs);
	   createAnnotationComment("Free Message Converter Copyleft 2007 - 2020 Matthias Fricke mf@sapstern.com", true, false);
	   createAnnotationComment("Licensed under the Apache License, Version 2.0 (the \"License\");", false, false);
	   createAnnotationComment("You may obtain a copy of the License at", false, false);
	   createAnnotationComment("     http://www.apache.org/licenses/LICENSE-2.0", false, false);
	   createAnnotationComment("Unless required by applicable law or agreed to in writing, software", false, false);
	   createAnnotationComment("distributed under the License is distributed on an \"AS IS\" BASIS,", false, false);
	   createAnnotationComment("WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.", false, false);
	   createAnnotationComment("See the License for the specific language governing permissions and", false, false);
	   createAnnotationComment("limitations under the License.", false, true);
	   AttributesImpl  messageTypeAttribs = new AttributesImpl();
	   messageTypeAttribs.addAttribute( "", "name", "name", "CDATA", messageType );	   
	   contentHandler.startElement(namespaceURI, "xsd:element", "xsd:element", messageTypeAttribs);
	   contentHandler.startElement(namespaceURI, "xsd:complexType", "xsd:complexType", new AttributesImpl());
	   contentHandler.startElement(namespaceURI, "xsd:sequence", "xsd:sequence", new AttributesImpl());
	   String theMsegment = "M_"+messageName; 
	   setupSegmentDeclaration(contentHandler, "ref", "ref", "S_UNA", "0", null, true);	  	  	   
	   setupSegmentDeclaration(contentHandler, "ref", "ref", "S_UNB", null, null, true);
	   setupSegmentDeclaration(contentHandler, "ref", "ref", "S_UNG", "0", "unbounded", true);
	   setupSegmentDeclaration(contentHandler, "ref", "ref", ""+theMsegment , null, "unbounded", true);
	   setupSegmentDeclaration(contentHandler, "ref", "ref", "S_UNE", "0", "unbounded", true);
	   setupSegmentDeclaration(contentHandler, "ref", "ref", "S_UNZ", null, null, true);	   
	   contentHandler.endElement(namespaceURI, "xsd:sequence", "xsd:sequence" );
	   contentHandler.endElement(namespaceURI, "xsd:complexType", "xsd:complexType" );
	   contentHandler.endElement(namespaceURI, "xsd:element", "xsd:element" );
	   //Top level Segment start
	   setupSegment(theMsegment, 0);
	   setupSegmentStructure();	 
	   setupContorlSegments("S_UNA");
 	   setupContorlSegments("S_UNB");
 	   setupContorlSegments("S_UNG");
 	   setupContorlSegments("S_UNE");
	   setupContorlSegments("S_UNZ");
	   setupComposite();
	   setupDataElements();
       setupElementTypes();	   
	   
	   
	   contentHandler.endElement( namespaceURI, "xsd:schema", "xsd:schema" );
	   contentHandler.endDocument();
	}


	private void setupContorlSegments(String theSegmentName) throws SAXException
	{
		AttributesImpl  unaTypeAttribs = new AttributesImpl();
		unaTypeAttribs.addAttribute( "", "name", "name", "CDATA", theSegmentName );	   
		contentHandler.startElement(namespaceURI, "xsd:element", "xsd:element", unaTypeAttribs);
		contentHandler.startElement(namespaceURI, "xsd:complexType", "xsd:complexType", new AttributesImpl());
		contentHandler.startElement(namespaceURI, "xsd:sequence", "xsd:sequence", new AttributesImpl());
		SegmentStructureData theSegmentStructureData = theSegmentTab.get(theSegmentName);
		
		if (theSegmentStructureData!=null)
		{
			for ( int i=0; i<theSegmentStructureData.theMemberList.size();i++)
			{
				SegmentStructureElement currentElement = theSegmentStructureData.theMemberList.get(i);
				AttributesImpl  memberTypeAttribs = new AttributesImpl();
				memberTypeAttribs.addAttribute( "", "ref", "ref", "CDATA", ""+currentElement.theName );	
				if ( currentElement.isManadatory.equals("0") )
					memberTypeAttribs.addAttribute( "", "minOccurs", "minOccurs", "CDATA", "0" );
				contentHandler.startElement(namespaceURI, "xsd:element", "xsd:element", memberTypeAttribs);

				contentHandler.endElement(namespaceURI, "xsd:element", "xsd:element" );
			}
		}
		   
		contentHandler.endElement(namespaceURI, "xsd:sequence", "xsd:sequence" );
		contentHandler.endElement(namespaceURI, "xsd:complexType", "xsd:complexType" );
		contentHandler.endElement(namespaceURI, "xsd:element", "xsd:element" );
		//Composite und Datenelemente, die zum UNA / UNB / und UNZ gehoehren ind die entsprechenden Listen anhaengen
		if (theSegmentStructureData!=null)
		{
			for ( int i=0; i<theSegmentStructureData.theMemberList.size();i++)
			{
				SegmentStructureElement currentElement = theSegmentStructureData.theMemberList.get(i);
				
				if ( currentElement.theType == TypeDef.TYPE_COMPOSITE )
				{
					CompositeDefinition theCompositeDef = theCompositeTab.get(currentElement.theName);
					if ( theCompositeDef != null )
					{						
						theCompositeTab.put(theCompositeDef.theName ,theCompositeDef);
						
						this.usedElementsSet.add(theCompositeDef.theName);
						Iterator<CompositeDataDefinition> theIterator = theCompositeDef.theDataList.iterator();
						while ( theIterator.hasNext() )
						{
							CompositeDataDefinition theCompositeDataDef = theIterator.next(); 
							Hashtable<String, String> theDataElementDef = theDataElementTab.get(theCompositeDataDef.theName); 
							
							if ( theDataElementDef != null )
							{
								theDataElementTab.put(theDataElementDef.get("name"),theDataElementDef);
								this.usedElementsSet.add(theDataElementDef.get("name"));
							}							
						}
					}
				}
				if ( currentElement.theType == TypeDef.TYPE_DATAELEMENT )
				{
					Hashtable<String, String> theDataElementDef = theDataElementTab.get(currentElement.theName); 
					
					if ( theDataElementDef != null )
					{
						theDataElementTab.put(theDataElementDef.get("name"),theDataElementDef);
						this.usedElementsSet.add(theDataElementDef.get("name"));
					}
				}
			}
		}
		
	}


	private void setupSegmentStructure() throws SAXException
	{
		Iterator<SegmentStructureData> theSegmentStructureIterator = theSegmentStructureList.iterator();
		   while(theSegmentStructureIterator.hasNext())
		   {
			   SegmentStructureData theData = theSegmentStructureIterator.next();
				AttributesImpl  theAttribs = new AttributesImpl();
				theAttribs.addAttribute( "", "name", "name", "CDATA", theData.theNameOfSegment );
				contentHandler.startElement(namespaceURI, "xsd:element", "xsd:element", theAttribs);
				if(theData.theDescription != null)
				{
					createAnnotationComment(theData.theDescription, true, true);
				}

				contentHandler.startElement(namespaceURI, "xsd:complexType", "xsd:complexType", new AttributesImpl());
				contentHandler.startElement(namespaceURI, "xsd:sequence", "xsd:sequence", new AttributesImpl());
				theData.theMemberList = (List<SegmentStructureElement>) checkDuplicateMembers(theData.theMemberList);
				Iterator<SegmentStructureElement> theListIterator = theData.theMemberList.iterator();

				while ( theListIterator.hasNext())
				{

					SegmentStructureElement theCDataDef = theListIterator.next();
					this.usedElementsSet.add(theCDataDef.theName);
					
					AttributesImpl  theElemAttribs = new AttributesImpl();
					theElemAttribs.addAttribute( "", "ref", "ref", "CDATA", ""+theCDataDef.theName );
					if ( theCDataDef.isManadatory.equals("0") )
						theElemAttribs.addAttribute( "", "minOccurs", "minOccurs", "CDATA", "0" );
					contentHandler.startElement(namespaceURI, "xsd:element", "xsd:element", theElemAttribs);
					contentHandler.endElement(namespaceURI, "xsd:element", "xsd:element" );
					
				}
				contentHandler.endElement(namespaceURI, "xsd:sequence", "xsd:sequence" );
				contentHandler.endElement(namespaceURI, "xsd:complexType", "xsd:complexType" );
				contentHandler.endElement(namespaceURI, "xsd:element", "xsd:element" );

			   
		   }
	}


	private void createAnnotationComment(String theText, boolean isAnnotationStart, boolean isAnnotationEnd) throws SAXException {
		if (isAnnotationStart)
			contentHandler.startElement(namespaceURI, "xsd:annotation", "xsd:annotation", new AttributesImpl());		
		contentHandler.startElement(namespaceURI, "xsd:documentation", "xsd:documentation", new AttributesImpl());
		contentHandler.characters(theText.toCharArray(), 0, theText.length());
		contentHandler.endElement(namespaceURI, "xsd:documentation", "xsd:documentation" );
		if (isAnnotationEnd)
			contentHandler.endElement(namespaceURI, "xsd:annotation", "xsd:annotation" );
	}


	private void setupComposite() throws SAXException
	{		
		Enumeration<CompositeDefinition> theIterator = theCompositeTab.elements();
		while (theIterator.hasMoreElements())
		{
			CompositeDefinition theDef = theIterator.nextElement();
			if(!this.usedElementsSet.contains(theDef.theName))
				continue;
			//Check for duplicate definitions of same dataelement in composite
			theDef.theDataList =(List<CompositeDataDefinition>) checkDuplicateMembers(theDef.theDataList);
			putComposite(theDef);
			Integer timesReplication = nonDeterministicObjects.get(theDef.theName);
			if (timesReplication==null)
				continue;
			int timesRepInt = timesReplication.intValue();
			String oldName = theDef.theName;
			for (int i=1;i<timesRepInt;i++)
			{
				String currentName = oldName+"_"+i;
				theDef.theName = currentName;
				putComposite(theDef);
			}
			nonDeterministicObjects.remove(theDef.theName);

		}
	}


	private void putComposite(CompositeDefinition theDef) throws SAXException {
		AttributesImpl  theAttribs = new AttributesImpl();
		theAttribs.addAttribute( "", "name", "name", "CDATA", theDef.theName );
		contentHandler.startElement(namespaceURI, "xsd:element", "xsd:element", theAttribs);
		if(theDef.theDescription != null)
		{
			contentHandler.startElement(namespaceURI, "xsd:annotation", "xsd:annotation", new AttributesImpl());
			contentHandler.startElement(namespaceURI, "xsd:documentation", "xsd:documentation", new AttributesImpl());
			contentHandler.characters(theDef.theDescription.toCharArray(), 0, theDef.theDescription.length());
			contentHandler.endElement(namespaceURI, "xsd:documentation", "xsd:documentation" );
			contentHandler.endElement(namespaceURI, "xsd:annotation", "xsd:annotation" );
		}
		contentHandler.startElement(namespaceURI, "xsd:complexType", "xsd:complexType", new AttributesImpl());
		contentHandler.startElement(namespaceURI, "xsd:sequence", "xsd:sequence", new AttributesImpl());			
		Iterator<CompositeDataDefinition> theListIterator = theDef.theDataList.iterator();
		while ( theListIterator.hasNext() )
		{
			CompositeDataDefinition theCDataDef = theListIterator.next();
			AttributesImpl  theElemAttribs = new AttributesImpl();
			theElemAttribs.addAttribute( "", "ref", "ref", "CDATA", ""+theCDataDef.theName );
			if ( theCDataDef.isManadatory.equals("0") )
				theElemAttribs.addAttribute( "", "minOccurs", "minOccurs", "CDATA", "0" );
			contentHandler.startElement(namespaceURI, "xsd:element", "xsd:element", theElemAttribs);
			contentHandler.endElement(namespaceURI, "xsd:element", "xsd:element" );

		}
		contentHandler.endElement(namespaceURI, "xsd:sequence", "xsd:sequence" );
		contentHandler.endElement(namespaceURI, "xsd:complexType", "xsd:complexType" );
		contentHandler.endElement(namespaceURI, "xsd:element", "xsd:element" );
	}



	private List<?> checkDuplicateMembers(List<?> theDataList) {
		// TODO Auto-generated method stub
		List<DataDef> resultList = new LinkedList<DataDef>();
		Hashtable<String, Integer> theLocalCheckTab = new Hashtable<String, Integer>();
		
		Iterator<DataDef> theIterator = (Iterator<DataDef>)theDataList.iterator();
		
		while(theIterator.hasNext())
		{
			DataDef currentDef = theIterator.next();
			
			Integer currentCount = theLocalCheckTab.get(currentDef.theName);
			if (currentCount==null)
			{
				theLocalCheckTab.put(currentDef.theName, new Integer(1));
				this.usedElementsSet.add(currentDef.theName);
				resultList.add(currentDef);
				continue;
			}
			String oldName =  currentDef.theName;
			currentDef.theName = currentDef.theName+"_"+currentCount.intValue();
			this.usedElementsSet.add(currentDef.theName);
			resultList.add(currentDef);
			int newValue = currentCount.intValue();
			newValue++;			
			currentCount = new Integer(newValue);
			theLocalCheckTab.put(oldName, currentCount);			
			//Now chech on whther we have the Definition already in our global check table
			Integer currentGlobalCount = nonDeterministicObjects.get(oldName);
			if(currentGlobalCount==null)
			{
				//New entry for global table, just add it
				nonDeterministicObjects.put(oldName, currentCount);
				continue;
			}
			//Not new, lets compare the times we already have to replicate it
			if(currentGlobalCount>=currentCount)
				continue;
			nonDeterministicObjects.put(oldName, currentCount);
			
		}
		return resultList;
	}


	private void setupDataElements() throws SAXException
	{
		Enumeration<Hashtable<String, String>> theIterator = theDataElementTab.elements();
		while ( theIterator.hasMoreElements() )
		{
			Hashtable<String, String> theCurrentDataElemTab = theIterator.nextElement();

			if(!this.usedElementsSet.contains(theCurrentDataElemTab.get("name")))
					continue;				
			putElement(theCurrentDataElemTab);
			Integer timesReplication = nonDeterministicObjects.get(theCurrentDataElemTab.get("name"));
			if (timesReplication==null)
				continue;
			int timesRepInt = timesReplication.intValue();
			String oldName = theCurrentDataElemTab.get("name");
			for (int i=1;i<timesRepInt;i++)
			{
				String currentName = oldName+"_"+i;
				theCurrentDataElemTab.put("name", currentName);
				putElement(theCurrentDataElemTab);
			}
			nonDeterministicObjects.remove(theCurrentDataElemTab.get("name"));
		}
	}


	private void putElement(Hashtable<String, String> theCurrentDataElemTab) throws SAXException 
	{
		AttributesImpl  theAttribs = new AttributesImpl();
		theAttribs.addAttribute( "", "name", "name", "CDATA", theCurrentDataElemTab.get("name") );

		String theType = theCurrentDataElemTab.get("Repr");
		String typeOfAttrib = null; 
		String theTypeDefString = null;
		if (theType.startsWith("an"))
			typeOfAttrib = theTypeDefString = "string";
		else
		{
			typeOfAttrib = "decimal";
			theTypeDefString = "numeric";
		}
		String theLengthDescription = theType.replaceAll("a", "");
		theLengthDescription = theLengthDescription.replaceAll("n", "");				
		theTypeDefString = theTypeDefString+"1"+theLengthDescription;
		theAttribs.addAttribute( "", "type", "type", "CDATA", ""+theTypeDefString );
		contentHandler.startElement(namespaceURI, "xsd:element", "xsd:element", theAttribs);

		contentHandler.startElement(namespaceURI, "xsd:annotation", "xsd:annotation", new AttributesImpl());
		contentHandler.startElement(namespaceURI, "xsd:documentation", "xsd:documentation", new AttributesImpl());
		contentHandler.characters(theCurrentDataElemTab.get("Desc").toCharArray(), 0, theCurrentDataElemTab.get("Desc").length());
		contentHandler.endElement(namespaceURI, "xsd:documentation", "xsd:documentation" );
		contentHandler.endElement(namespaceURI, "xsd:annotation", "xsd:annotation" );

		Hashtable<String, String> theTab = new Hashtable<String, String>();
		theTab.put("typedef", theTypeDefString);
		theTab.put("type", typeOfAttrib);
		String maxLength = theType.replaceAll("a", "");
		maxLength = maxLength.replaceAll("n", "");
		maxLength = maxLength.replaceAll("\\.", "");
		theTab.put("length", maxLength);


		theElementTypeTab.put(theTypeDefString, theTab);

		contentHandler.endElement(namespaceURI, "xsd:element", "xsd:element" );
	}

	private void setupElementTypes() throws SAXException
	{
		Enumeration<Hashtable<String, String>> theEnum = theElementTypeTab.elements();
		while (theEnum.hasMoreElements())
		{
			Hashtable<String, String> tab = theEnum.nextElement();
			
			if(this.usedElementsSet.contains(tab.get("name")));
				setupRestriction(tab.get("type"), tab.get("typedef"), tab.get("length"));
		}
	}

	/**
	 * @param theType
	 * @param theAttribsRestri
	 * @throws SAXException
	 */
	private void setupRestriction(String theType, String typeOfAttrib, String theLength) throws SAXException {
		AttributesImpl theSimpleTypeAttr = new AttributesImpl();
		theSimpleTypeAttr.addAttribute( "", "name", "name", "CDATA", typeOfAttrib );
		contentHandler.startElement(namespaceURI, "xsd:simpleType", "xsd:simpleType",theSimpleTypeAttr);
		AttributesImpl  theAttribsRestri = new AttributesImpl();
		theAttribsRestri.addAttribute( "", "base", "base", "CDATA", "xsd:"+theType );
		contentHandler.startElement(namespaceURI, "xsd:restriction", "xsd:restriction", theAttribsRestri);
		AttributesImpl  theAttribsValueMin = new AttributesImpl();
		if (theType.equals("string"))
		{
		theAttribsValueMin.addAttribute( "", "value", "value", "CDATA", "1" );
		contentHandler.startElement(namespaceURI, "xsd:minLength", "xsd:minLength", theAttribsValueMin);
		contentHandler.endElement(namespaceURI, "xsd:minLength", "xsd:minLength" );
		AttributesImpl  theAttribsValueMax = new AttributesImpl();
		theAttribsValueMax.addAttribute( "", "value", "value", "CDATA", theLength );
		contentHandler.startElement(namespaceURI, "xsd:maxLength", "xsd:maxLength", theAttribsValueMax);
		contentHandler.endElement(namespaceURI, "xsd:maxLength", "xsd:maxLength" );
		}//xsd:totalDigits
		else
		{
			AttributesImpl  theAttribsTotalDigits = new AttributesImpl();
			theAttribsTotalDigits.addAttribute( "", "value", "value", "CDATA", theLength );
			contentHandler.startElement(namespaceURI, "xsd:totalDigits", "xsd:totalDigits", theAttribsTotalDigits);
			contentHandler.endElement(namespaceURI, "xsd:totalDigits", "xsd:totalDigits" );
		}
		
		contentHandler.endElement(namespaceURI, "xsd:restriction", "xsd:restriction" );
		contentHandler.endElement(namespaceURI, "xsd:simpleType", "xsd:simpleType" );
	}


	/**
	 * @param theSegmentGroupName
	 * @param theLevel
	 * @throws SAXException
	 */
	private void setupSegment(String theSegmentGroupName, int theLevel) throws SAXException {

		setupSegmentDeclaration(contentHandler, "name", "name", theSegmentGroupName, null, null, false);
		   contentHandler.startElement(namespaceURI, "xsd:complexType", "xsd:complexType", new AttributesImpl());
		   contentHandler.startElement(namespaceURI, "xsd:sequence", "xsd:sequence", new AttributesImpl());

		   List<String> sGlist = new LinkedList<String>(); 
		   for ( int i=0; i<theSegmentList.size();i++ )
		   {
			   SegmentData theData = theSegmentList.get(i);
			   try {
				   if (theData.theLevel == theLevel && theData.theNameOfSegmentGroup.equals(theSegmentGroupName))   
				   {
					   if ( theData.theSegmentName.startsWith("G_") )
						   sGlist.add(theData.theSegmentName);

					   theData.theLevel = -1;
					   theSegmentList.set(i, theData);

					   if ( theData.isMandatory == true )				   				   
						   setupSegmentDeclaration(contentHandler, "ref", "ref", ""+theData.theSegmentName, null, String.valueOf(theData.theCardinality), true);  
					   else
						   setupSegmentDeclaration(contentHandler, "ref", "ref", ""+theData.theSegmentName, "0", String.valueOf(theData.theCardinality), true);
				   }
			   }
			   catch (NullPointerException npe)
			   {
				   throw new SAXException("Nullpointer while processing segment group "+theSegmentGroupName+", segment data object name of sg "+theData.theNameOfSegmentGroup+" egment data object level "+theData.theLevel+" segment name "+theData.theSegmentName+", probably XSD corrupt "+npe);
			   }
		   }
		   contentHandler.endElement(namespaceURI, "xsd:sequence", "xsd:sequence" );
		   contentHandler.endElement(namespaceURI, "xsd:complexType", "xsd:complexType" );
		   contentHandler.endElement(namespaceURI, "xsd:element", "xsd:element" );
		   for ( int i=0; i< sGlist.size();i++)
		   {
			   int theLocalLevel = theLevel+1;
			   setupSegment(sGlist.get(i), theLocalLevel);
		   }

			   
		   
	}

	
	
	/**
	 * @param theContentHandler
	 * @param theSegmentName
	 * @param theMinOccurence
	 * @param theMaxOccurence
	 * @param isEndElement
	 * @throws SAXException
	 */
	private void setupSegmentDeclaration( ContentHandler theContentHandler, String attribName, String attribNameQ, String theSegmentName, String theMinOccurence, String theMaxOccurence, boolean isEndElement ) throws SAXException 
	{
		AttributesImpl  theAttribs = new AttributesImpl();
		   theAttribs.addAttribute( "", attribName, attribNameQ, "CDATA", theSegmentName );	   
		   if ( theMinOccurence!=null )
			   theAttribs.addAttribute( "", "minOccurs", "minOccurs", "CDATA", theMinOccurence );
		   if ( theMaxOccurence!=null && !theMaxOccurence.equals("1") )
			   theAttribs.addAttribute( "", "minOccurs", "maxOccurs", "CDATA", theMaxOccurence );
		   theContentHandler.startElement(namespaceURI, "xsd:element", "xsd:element", theAttribs);
		   if ( isEndElement )
			   theContentHandler.endElement(namespaceURI, "xsd:element", "xsd:element" );
	}


	
	public void parse( String uri )
	{
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

	public Object getProperty(String s)
	{
        return null;
	}

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
     * @return
     * @throws TransformerException
     * @throws UnsupportedEncodingException
     */
    public String parseUNECE() throws TransformerException, UnsupportedEncodingException
    {
     //mi.println(new java.util.Date(System.currentTimeMillis()).toString()+": "+"EdifactSaxParserToXML().parseEifact( "+inputEDIFACT+" )" );
   	 // construct SAXSource with our custom XMLReader
   	 InputSource inputSource = new InputSource();
   	 inputSource.setEncoding(theEncoding);
   	 XMLReader parser	= this;
   	 SAXSource saxSource	= new SAXSource(parser, inputSource); 
   	 ByteArrayOutputStream barryOut = new ByteArrayOutputStream(); 
   	 StreamResult streamResult = new StreamResult(barryOut);      
   	 transformer.transform( saxSource, streamResult );
   	 String result = new String( barryOut.toByteArray(),theEncoding );
   	 //mi.println(new java.util.Date(System.currentTimeMillis()).toString()+": "+"EdifactSaxParserToXML().parseEifact()==> Result: "+result );
   	 return result;
    }




	

	public String getMessageType() {
		return messageType;
	}


	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}


	public String getMessageName() {
		return messageName;
	}


	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}



	public void setTheSegmentList(List<SegmentData> theSegmentList) {
		this.theSegmentList = theSegmentList;
	}



	public void setTheSegmentStructureList(
			List<SegmentStructureData> theSegmentStructureList)
	{
		this.theSegmentStructureList = theSegmentStructureList;
	}


	public void setTheSegmentTab(Hashtable<String, SegmentStructureData> theSgmentTab)
	{
		this.theSegmentTab = theSgmentTab;
	}


	public void setTheDataElementTab(
			Hashtable<String, Hashtable<String, String>> theDataElementTab) {
		this.theDataElementTab = theDataElementTab;
	}


	public void setTheCompositeTab(
			Hashtable<String, CompositeDefinition> theCompositeTab) {
		this.theCompositeTab = theCompositeTab;
	}
	
	
}
