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

import java.io.InputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sapstern.openedifact.sax.parse.segments.EdifactSegment;

public abstract class AbstractEdifactParser extends DefaultHandler 
{

	public static final String EDIFACT_ROOT_SEGMENT_NAME = "EDIFACTINTERCHANGE";
	protected List<EdifactSegment> rawListOfAllEdifactSegments = null;
	protected Hashtable<String, Node> theElementTab = null;
	
	
	
	/**
	 * @param xsdFileName
	 * @return
	 * @throws SAXException
	 */
	protected Document setupXSDDOM(String xsdFileName) throws SAXException {

		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// Turn off validation
			factory.setValidating(false);


			// Create a validating DOM parser
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream is = getIS(xsdFileName);			
			return builder.parse(is);
			 
		} catch (Exception e) {			
			throw new SAXException(e);
		} 

	}
	/**
	 * @param xsdFileName
	 * @return
	 * @throws SAXException
	 */
	protected InputStream getIS(String xsdFileName) throws SAXException {
		InputStream is = null;
		try {
		is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xsdFileName);
		if(is==null)
			is = this.getClass().getResourceAsStream(xsdFileName);
		if(is==null)
			is = this.getClass().getClassLoader().getResourceAsStream(xsdFileName);
		} catch (Exception ignore) {}
		if (is==null)
			throw new SAXException("Unable to load resource: "+xsdFileName);
		return is;
	}
	/**
	 * @param xsdDOM
	 * @return
	 * @throws SAXException
	 */
	protected Hashtable<String, Node>  getElementTable(Document xsdDOM) throws SAXException {		

		Hashtable<String, Node> tab = new Hashtable<String, Node>();
		NodeList theList = xsdDOM.getElementsByTagName("xsd:element");
		for (int i=0; i<theList.getLength();i++)
		{
			if ( theList.item(i) instanceof Element && theList.item(i).getAttributes().getNamedItem("name") != null 
					&& (   theList.item(i).getAttributes().getNamedItem("name").getNodeValue().startsWith("G_") 
							|| theList.item(i).getAttributes().getNamedItem("name").getNodeValue().startsWith("C_")
							|| theList.item(i).getAttributes().getNamedItem("name").getNodeValue().startsWith("M_")
							|| theList.item(i).getAttributes().getNamedItem("name").getNodeValue().startsWith("S_")
							|| theList.item(i).getAttributes().getNamedItem("name").getNodeValue().startsWith("EDIFACT")
							)) 
			{

				tab.put(theList.item(i).getAttributes().getNamedItem("name").getNodeValue(), theList.item(i)); 
			}
		}

		return tab;		
	}

	/**
	 * @param childNodes
	 * @return
	 */
	protected List<Element> getNodeListOfComplexType(NodeList childNodes)
	{
		
		// TODO Auto-generated method stub
		for (int i=0;i<childNodes.getLength();i++)
		{
			Node node = childNodes.item(i);
			//System.out.println("getNodeListOfComplexType()"+node.getNodeName());
			if(node.getNodeName().equals("xsd:complexType"))
				return getNodeListOfComplexType(node.getChildNodes());

			if ( node.getNodeName().equals("xsd:sequence") )
			{
				List<Element> resultElements = new LinkedList<Element>();
				NodeList localList = node.getChildNodes();
				for(int j=0; j<localList.getLength();j++)
				{
					Node theNode = localList.item(j);
					if ( theNode instanceof Element )
						resultElements.add((Element)theNode);
				}
				return resultElements;
			}
		}
		return null;
	}

	

	/**
	 * Output of the XML hierarcy
	 * @param rootSegment
	 * @param currentDepth
	 */
	public void printSegmentTree(EdifactSegment rootSegment, String currentDepth) 
	{
		// TODO Auto-generated method stub
		if ( currentDepth.length() > 0 )
			System.out.println(currentDepth+">"+rootSegment.segmentName);
		else
			System.out.println(rootSegment.segmentName);
		for ( int i=0;i<rootSegment.segmentFields.size();i++ )
		{
			
			if (rootSegment.segmentFields.get(i).isComposite)
			{
				System.out.println(currentDepth+"->"+rootSegment.segmentFields.get(i).fieldTagName);
				for (int j=0;j<rootSegment.segmentFields.get(i).subFields.size();j++)
				{
					System.out.println(currentDepth+"-->"+rootSegment.segmentFields.get(i).subFields.get(j).subFieldTagName+" Value "+rootSegment.segmentFields.get(i).subFields.get(j).subFieldValue);
				}
			}
			else
				System.out.println(currentDepth+">"+rootSegment.segmentFields.get(i).fieldTagName+" Value "+rootSegment.segmentFields.get(i).fieldValue);
		}
		for (int i = 0; i < rootSegment.childSegments.size(); i++)
		{
			printSegmentTree(rootSegment.childSegments.get(i), currentDepth);
		}
		currentDepth = currentDepth+"-";
	}

}
