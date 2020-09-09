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

package com.sapstern.openedifact.unece.xsd.gen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.sapstern.openedifact.unece.xsd.data.CompositeDataDefinition;
import com.sapstern.openedifact.unece.xsd.data.CompositeDefinition;
import com.sapstern.openedifact.unece.xsd.data.SegmentData;
import com.sapstern.openedifact.unece.xsd.data.SegmentStructureData;
import com.sapstern.openedifact.unece.xsd.data.SegmentStructureElement;
import com.sapstern.openedifact.unece.xsd.data.TypeDef;
import com.sapstern.openedifact.unece.xsd.log.AbstractLogger;
import com.sapstern.openedifact.unece.xsd.sax.SaxParserUNECEToXSD;
import com.sapstern.openedifact.unece.xsd.utils.StringChecker;

public class Generator extends AbstractLogger implements TypeDef{

	private String theProjectDirectory = "/home/matthias/gitDieter/openedifact-un2xsd/src/test/UNECE";
	private String theEncoding = "ISO-8859-1";
	private String theVersion = "96A";
	private String messageType = "EDIFACTINTERCHANGE";

	private String theMessageDirectory = theProjectDirectory+"/<VERSION>/IN/EDMD";
	private String theDirectory = theProjectDirectory+"/<VERSION>/IN";

	private Hashtable<String, Integer> tabOffsetVersion = null;
	private Hashtable<String, Integer> tabOffsetGeneric = null;

	


	/**
	 * @param theVersion
	 * @param theProjectDirectory
	 * @param theEncoding
	 */
	public Generator(String theVersion, String theProjectDirectory, String  theEncoding, String messageType, String theMessageDirectory, String theDirectory) throws Exception
	{
		this.theVersion = theVersion;
		this.theProjectDirectory = theProjectDirectory;
		this.theEncoding = theEncoding;
		this.messageType = messageType;
		this.theMessageDirectory = theMessageDirectory;
		this.theDirectory = theDirectory;

		tabOffsetGeneric = getOffsetValues("XXX");
		tabOffsetVersion = getOffsetValues(theVersion);		
		//XSDFileGenerator.setLogLevel(LOGGER);
	}

	/**
	 * Special encoding treatment for old versions
	 */
	private boolean isWinEncoding(String theVersion) {
		if (	  theVersion.equals("96A")
				||theVersion.equals("96B")
				||theVersion.equals("97A")
				||theVersion.equals("97B")
				||theVersion.equals("98A")
				||theVersion.equals("98B")
				||theVersion.equals("99A")
				||theVersion.equals("99B")
				||theVersion.equals("00A")
				||theVersion.equals("00B")
				||theVersion.equals("01A")
				||theVersion.equals("01B"))
		{
			return true;
		}
		else
			return false;
	}



	/**
	 * @param theMessageName
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 */
	public void process( String theMessageName ) throws IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
	{

		int startIndexOfMandatory = -1;
		int startIndexOfCardinality = -1;

		BufferedReader in =  null;
		in = getBufferedReader(theMessageDirectory	+ "/" + theMessageName);
		theMessageName = theMessageName.replace("D.", "");
		boolean isLineProcessing = false;
		int lineNumber = 0;

		Hashtable<Integer, String> lineTab = new Hashtable<Integer, String>();
		while (true)
		{				
			String line = in.readLine();
			if (line==null)
				break;
			if (line.startsWith("Pos"))
			{
				isLineProcessing = true;
				line = line.substring(line.indexOf("Tag"));
				startIndexOfMandatory = line.indexOf("S");
				startIndexOfCardinality = line.indexOf("R");
			}
			if (isLineProcessing)
			{

				int localLineNumber = getRgexNumber(line, 5);
				if (localLineNumber!=-1)
				{
					line = line.substring(5).trim();
					if (line.indexOf("X")==0)
						line = line.substring(1).trim();
					if (line.indexOf("*")==0)
						line = line.substring(1).trim();
					if (line.indexOf("+")==0)
						line = line.substring(1).trim();
					if (line.indexOf("Segment group")!=-1)
						line = "    "+line;
					lineTab.put(localLineNumber, line);
					lineNumber = localLineNumber;
				}
			}
		}
		in.close();

		if (isWinEncoding(theVersion))
		{
			for (int i=10; i<=lineNumber;i=i+10)
			{		
				String currentLine = lineTab.get(i);
				if(currentLine==null)
					continue;
				currentLine = normalizeLineEncoding(currentLine);
				lineTab.put(i, currentLine);
				//LOGGER.info(currentLine);
			}
		}

		List<SegmentData> theSegmentList = new LinkedList<SegmentData>();

		Hashtable<Integer, String> theLevelTab = new Hashtable<Integer, String>();
		theLevelTab.put(0, "M_" + theMessageName);
		for (int i=10; i<=lineNumber;i=i+10)
		{		
			String line = lineTab.get(i);
			if(line==null)
				continue;

			SegmentData data = new SegmentData();

			data.theSegmentDescr = line.substring(4, 46).trim();
			if (line.substring(startIndexOfMandatory, startIndexOfMandatory+1).equals("M"))
				data.isMandatory = true;
			else
				data.isMandatory = false;
			String theSegmentName = line.substring(0, 3);
			data.theCardinality = getRgexNumber(line.substring(startIndexOfCardinality), -1);

			int theFirstLevelChar = line.indexOf("|");
			int theFirstPlusIndex = line.indexOf("+");
			if (theFirstLevelChar == -1 && theFirstPlusIndex == -1)
				data.theLevel = 0;
			else if (theFirstPlusIndex != -1)
			{
				String theLevelTmp = line.substring(theFirstPlusIndex, line
						.length());
				data.theLevel = theLevelTmp.length();
			}
			else
			{
				String theLevelTmp = line.substring(theFirstLevelChar, line
						.length());
				data.theLevel = theLevelTmp.length();
			}
			int segGroupIndex = line.indexOf("Segment group");
			if (segGroupIndex!=-1)
			{
				segGroupIndex = segGroupIndex + "Segment group ".length();
				data.theLevel--;
				//Fehler: Groupnummer mehrstelli

				int theGroupNumber = getRgexNumber(line.substring(segGroupIndex), 6);
				data.theSegmentName = "G_" + theMessageName + "_SG"
						+ theGroupNumber;

				data.theNameOfSegmentGroup = theLevelTab.get(data.theLevel);
				int newlevel = data.theLevel + 1;
				theLevelTab.put(newlevel, data.theSegmentName);
			}
			else
			{
				data.theSegmentName = "S_" + theSegmentName;
				data.theNameOfSegmentGroup = theLevelTab.get(data.theLevel);
			}

			theSegmentList.add(data);
			LOGGER.info(data.theSegmentName + " " + data.theSegmentDescr	+ " " + data.isMandatory + " " + data.theCardinality + " "	+ data.theLevel + " " + data.theNameOfSegmentGroup);

		}
		// Hole Alle Segmente
		Hashtable<String, SegmentStructureData> theSegmentTab = getSegmentStructureTab(theDirectory + "/EDSD." + theVersion, null);
		theSegmentTab = getSegmentStructureTab(theDirectory + "/EDSD.XXX", theSegmentTab);
		// Hole die Segmente fuer den aktuellen Nachrichtentyp
		List<SegmentStructureData> theSegmentStructureList = getSegmentStructureDataList(theSegmentTab, theSegmentList);
		// Hole alle Composite Typen
		Hashtable<String, CompositeDefinition> theCompositeElementTab = getCompositeElementsTab(theDirectory + "/EDCD." + theVersion, null);
		theCompositeElementTab = getCompositeElementsTab(theDirectory + "/EDCD.XXX", theCompositeElementTab);
		// Hole alle Datenelemnte
		Hashtable<String, Hashtable<String, String>> theDataElementTab = getDataElementsTab(theDirectory	+ "/EDED." + theVersion, null);
		theDataElementTab = getDataElementsTab(theDirectory	+ "/EDED.XXX", theDataElementTab);

		BufferedWriter out = null;
		try
		{
			out = new BufferedWriter(new FileWriter( theProjectDirectory+"/"+theVersion+"/OUT/"	+ messageType + "_" + theMessageName + ".xsd"));
			SaxParserUNECEToXSD parser = new SaxParserUNECEToXSD(theMessageName, theEncoding);
			parser.setMessageName(theMessageName);
			parser.setMessageType(messageType);
			parser.setTheSegmentList(theSegmentList);
			parser.setTheSegmentStructureList(theSegmentStructureList);
			parser.setTheSegmentTab(theSegmentTab);
			parser.setTheCompositeTab(theCompositeElementTab);
			parser.setTheDataElementTab(theDataElementTab);

			String result = parser.parseUNECE();

			out.write(result.toCharArray());
			out.flush();
			out.close();

		}
		catch (UnsupportedEncodingException uee)
		{
			// TODO Auto-generated catch block
			uee.printStackTrace();
		}
		catch (SAXException se)
		{
			// TODO Auto-generated catch block
			se.printStackTrace();
		}
		catch (TransformerException te)
		{
			// TODO Auto-generated catch block
			te.printStackTrace();
		}
		catch (IOException ioe)
		{
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}
		finally
		{
			try
			{
				out.close();
			}
			catch (IOException ioe)
			{
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * @param currentLine
	 * @return
	 */
	private String normalizeLineEncoding(String currentLine) {
		//hack for old files encoded in asci
		currentLine = currentLine.replaceAll("Ä", "-");
		currentLine = currentLine.replaceAll("¿", "+");
		currentLine = currentLine.replaceAll("�", "-");
		currentLine = currentLine.replaceAll("Ŀ", "+");
		currentLine = currentLine.replaceAll("Ù", "+");
		currentLine = currentLine.replaceAll("³", "|");
		currentLine = currentLine.replace("-Á+", "-++");
		return currentLine;
	}

	/**
	 * @param theFileName
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	private BufferedReader getBufferedReader(String theFileName)	throws UnsupportedEncodingException, FileNotFoundException {
		BufferedReader in;
		if (isWinEncoding(theVersion))
			in =	new BufferedReader(new InputStreamReader(new FileInputStream(theFileName), "Cp1252"));
		else
			in =	new BufferedReader(new InputStreamReader(new FileInputStream(theFileName), "UTF8"));
		return in;
	}

	/**
	 * @param theXMLSnippet
	 * @return
	 */
	private int getRgexNumber(String theLine, int length)
	{
		if (length==-1)
			length = theLine.length();
		String regex = "START[0123456789]+"; // regex
		Pattern regPat = Pattern.compile(regex);


		try
		{
			Matcher matcher = regPat.matcher("START"+theLine.substring(0,length));
			if (matcher.find()) {
				int startIndex = "START".length();
				String findString = matcher.group();
				findString = findString.substring(startIndex, findString.length());
				return new Integer(findString);
			}
		}
		catch (Exception ignore){}
		return -1;

	}

	/**
	 * @param theXMLSnippet
	 * @return
	 */
	private String isMandatory(String theLine)
	{
		
		String regex = "M[ ]+[a]?[n]?[..]?"; // regex		
		Pattern regPat = Pattern.compile(regex);
		try {
		Matcher matcher = regPat.matcher(theLine);
		
		if (matcher.find())
				return "1";
		else 
			return "0";
		}catch (Exception e) {LOGGER.info(theLine);}
		return "0";
	}





	/**
	 * @param theSegmentTab
	 * @param theSegmentList
	 * @return
	 */
	private List<SegmentStructureData> getSegmentStructureDataList(	Hashtable<String, SegmentStructureData> theSegmentTab,	List<SegmentData> theSegmentList )
	{
		List<SegmentStructureData> theList = new LinkedList<SegmentStructureData>();
		Iterator<SegmentData> theIterator = theSegmentList.iterator();
		while ( theIterator.hasNext() )
		{
			SegmentData theSegment = theIterator.next();

			if (theSegmentTab.get(theSegment.theSegmentName)==null)
			{
				//	LOGGER.info("Not found in Tab: "+theSegment.theSegmentName);
				continue;
			}

			SegmentStructureData theSegmentStructure = theSegmentTab.get(theSegment.theSegmentName);
			//LOGGER.info(theSegment.theSegmentName);
			if ( !theList.contains(theSegmentStructure) )
				theList.add(theSegmentStructure);
		}
		return theList;
	}


	/**
	 * @param theFileName
	 * @param theTab
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws IOException 
	 */
	private Hashtable<String, SegmentStructureData> getSegmentStructureTab(String theFileName, Hashtable<String, SegmentStructureData> theTab) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException
	{
		Hashtable<String, SegmentStructureData> theSegmentTab = null;
		if ( theTab == null )
			theSegmentTab = new Hashtable<String, SegmentStructureData>();
		else
			theSegmentTab = theTab;

		BufferedReader in = getBufferedReader(theFileName);
		String line = null;

		SegmentStructureData theData = null;
		String theSegmentKey = null;

		//Erst mal nur Datei in Sublisten pro Segment zerlegen
		//Pro Segment stehen dann nur noch relevante Zeilen in der betreffenden Subliste
		List<List<String>> listList = new LinkedList<List<String>>();
		List<String> localSegmentList = null;
		boolean isSegment = false;

		while ((line = in.readLine())!=null)
		{
			if (line.length()==0)
				continue;
			if (isWinEncoding(theVersion))
				line = normalizeLineEncoding(line);
			LOGGER.info(line);
			if (line.indexOf("----------------------------------------------------------------------")!=-1)
			{
				isSegment = false;
				continue;
			}
			if (line.trim().startsWith("X "))
				line = line.replace("X", " ");

			if (line.trim().startsWith("* "))
				line = line.replace("*", " ");

			if (checkSegLine(line.trim().substring(0, 4)))
			{
				if (localSegmentList!=null)
					listList.add(localSegmentList);

				localSegmentList = new LinkedList<String>();
				localSegmentList.add(line);
				while((line = in.readLine())!=null)
				{
					if(getRgexNumber(line, 5)==-1)
					{
						continue;
					}

					localSegmentList.add(line);
					break;					
				}
				isSegment = true;
				continue;
			}
			if (!isSegment)
				continue;


			if (line.indexOf(" M")==-1 && line.indexOf(" M  an..")==-1 && line.indexOf(" C")==-1 && line.indexOf(" C  an..")==-1)
				line = line+in.readLine();
			localSegmentList.add(line);
		}
		if(localSegmentList!=null && !localSegmentList.isEmpty())
			listList.add(localSegmentList);
		in.close();

		for (int i=0;i<listList.size();i++)
		{
			//MFRI Ueberarbeiten
			List<String> localList = listList.get(i);
			for(int j=0;j<localList.size();j++)
			{
				String localLine = localList.get(j);

				int lineNumber = getRgexNumber(localLine, 5);

				localLine = localLine.substring(5).trim();

				String theSegmentNameToken = localLine.substring(0, 4);

				if (checkSegLine(theSegmentNameToken))
				{
					if (theData != null)
						theSegmentTab.put(theSegmentKey, theData);

					theData = new SegmentStructureData();						
					theSegmentKey = "S_" +theSegmentNameToken.trim();
					theData.theNameOfSegment = theSegmentKey;
					theData.theDescription = localLine.substring(5);
					theData.theMemberList = new LinkedList<SegmentStructureElement>();
					continue;
				}

				if (localLine.trim().startsWith("X "))				
					localLine = localLine.replace("X", " ");

				if (localLine.trim().startsWith("* "))				
					localLine = localLine.replace("*", " ");


				if(getRgexNumber(localLine.substring(1), 2)==-1)
					continue;

				SegmentStructureElement theElement = new SegmentStructureElement();
				String theNameToken = localLine.substring(0, 4);

				if (theNameToken.startsWith("C") || theNameToken.startsWith("S"))
				{
					theElement.theName = "C_"+theNameToken;	
					theElement.theType = TypeDef.TYPE_COMPOSITE;							
				}
				else
				{
					theElement.theName = "D_"+theNameToken;
					theElement.theType = TypeDef.TYPE_DATAELEMENT;
				}
				theElement.isManadatory = isMandatory(localLine);

				if (theData==null)
					LOGGER.info(theElement.theName);
				if (lineNumber!=-1)
					theData.theMemberList.add(theElement);
			}
		}

		if (theData != null)
			theSegmentTab.put(theSegmentKey, theData);

		return theSegmentTab;
	}


	/**
	 * @param theSegmentNameToken
	 * @return
	 */
	private boolean checkSegLine(String theSegmentNameToken)
	{
		if (theSegmentNameToken.trim().startsWith("X "))
			return false;
		if (!theSegmentNameToken.endsWith(" "))
			return false;
		if (StringChecker.containsAll(TypeDef.SEGMENT_NAME_CHARS_STRING, theSegmentNameToken.trim().toCharArray()))
		{
			return true;
		}
		return false;
	}

	/**
	 * @param theFileName
	 * @return
	 */
	private Hashtable<String, Integer> getOffsetTab(String theFileName) {
		Hashtable<String, Integer> tab = null;
		if (theFileName.endsWith(".XXX"))
			tab = tabOffsetGeneric;
		else
			tab = tabOffsetVersion;
		return tab;
	}

	/**
	 * @param theVersion2
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private Hashtable<String, Integer> getOffsetValues(String theVersionName) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException 
	{

		Hashtable<String, Integer> tab = new Hashtable<String, Integer>();

		tab.put("NAME_START",  extractValue(theVersionName, "NAME_START"));
		tab.put("NAME_END",  extractValue(theVersionName, "NAME_END"));	
		tab.put("SEGMENT_MANDATORY_START", extractValue(theVersionName, "SEGMENT_MANDATORY_START"));
		tab.put("SEGMENT_MANDATORY_END", extractValue(theVersionName, "SEGMENT_MANDATORY_END"));
		tab.put("DATA_ELEMENT_START",  extractValue(theVersionName, "DATA_ELEMENT_START"));
		tab.put("DATA_ELEMENT_END",  extractValue(theVersionName, "DATA_ELEMENT_END"));	
		tab.put("COMPOSITE_MANDATORY_START",  extractValue(theVersionName, "COMPOSITE_MANDATORY_START"));
		tab.put("COMPOSITE_MANDATORY_END",  extractValue(theVersionName, "COMPOSITE_MANDATORY_END"));	

		return tab;
	}

	/**
	 * Dynamic access to offset fields in class TypeDef for old versions (96A - 01B)
	 * @param theVersionName
	 * @param startOfFieldName
	 * @return the 
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	private int extractValue(String theVersionName, String startOfFieldName)	throws NoSuchFieldException, IllegalAccessException 
	{

		Class<TypeDef> typeDefClazz = TypeDef.class;
		Field nameStartOffsetField = null;
		try {
			nameStartOffsetField = typeDefClazz.getField(startOfFieldName+"_OFFSET_"+theVersionName);
		}
		catch (NoSuchFieldException nfe)
		{
			nameStartOffsetField = typeDefClazz.getField(startOfFieldName+"_OFFSET");
		}
		return nameStartOffsetField.getInt(null);

	}

	/**
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 */
	private Hashtable<String, CompositeDefinition> getCompositeElementsTab(String theFileName, Hashtable<String, CompositeDefinition> theTab) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
	{
		Hashtable<String, CompositeDefinition> theGlobCompositeTab = null;
		if ( theTab == null )
			theGlobCompositeTab = new Hashtable<String, CompositeDefinition>();
		else
			theGlobCompositeTab = theTab;
		try
		{

			BufferedReader in = getBufferedReader(theFileName);

			String line = null;

			CompositeDefinition theDefinition = null;
			String theCompositeKey = null;

			Hashtable<String, Integer> tab = getOffsetTab(theFileName);

			while ((line = in.readLine())!=null)
			{

				if (isWinEncoding(theVersion))
					line = normalizeLineEncoding(line);
				if (line.length() < 10)
					continue;

				if (line.substring(tab.get("NAME_START"), tab.get("NAME_START")+1).equals("C") || line.substring(tab.get("NAME_START"), tab.get("NAME_START")+1).equals("S"))
				{
					if (theDefinition != null)
						theGlobCompositeTab.put(theCompositeKey, theDefinition);

					theDefinition = new CompositeDefinition();
					theCompositeKey = "C_" + line.substring(tab.get("NAME_START"), tab.get("NAME_END"));
					theDefinition.theName = theCompositeKey;
					theDefinition.theDataList = new LinkedList<CompositeDataDefinition>();

				}
				try
				{
					String theNumberToken = line.substring(tab.get("NAME_START"), tab.get("NAME_END"));
					Integer.parseInt(theNumberToken);
					CompositeDataDefinition theDataDefinitition = new CompositeDataDefinition();
					theDataDefinitition.theName = "D_" + theNumberToken;

					// LOGGER.info(line);
					// Bei Zeilenumbruch auf die naechste Zeile wechseln
					if (line.length() < 56)
						setMandatory(in.readLine(), theDataDefinitition, tab.get("COMPOSITE_MANDATORY_START"), tab.get("COMPOSITE_MANDATORY_END"));
					else
						setMandatory(line, theDataDefinitition, tab.get("COMPOSITE_MANDATORY_START"), tab.get("COMPOSITE_MANDATORY_END"));

					theDefinition.theDataList.add(theDataDefinitition);

				}
				catch (NumberFormatException ignore){}
				if (line.substring(tab.get("NAME_START"), tab.get("NAME_START")+5).equals("Desc:"))
				{
					String theDescription = line.substring(tab.get("NAME_START")+5).trim();
					theDefinition.theDescription = theDescription;
				}
			}

			in.close();
			if (theDefinition != null)
				theGlobCompositeTab.put(theCompositeKey, theDefinition);

		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}


		return theGlobCompositeTab;
	}

	/**
	 * @param line
	 * @param theDataDefinitition
	 */
	private void setMandatory(String line, CompositeDataDefinition theDataDefinitition, int startIndex, int endIndex)
	{
		try {
			if (line.substring(startIndex, endIndex).equals("C"))
				theDataDefinitition.isManadatory = "0";
			else
				theDataDefinitition.isManadatory = "1";
		} catch (StringIndexOutOfBoundsException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			LOGGER.info("Dumping string: "+line);
			System.exit(-1);
		}
	}

	/**
	 * @param theFileName
	 * @param theTab
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 */
	private Hashtable<String, Hashtable<String, String>> getDataElementsTab(String theFileName, Hashtable<String, Hashtable<String, String>> theTab) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
	{
		// TODO Auto-generated method stub
		Hashtable<String, Hashtable<String, String>> theGlobTab = null;
		if ( theTab == null )
			theGlobTab = new Hashtable<String, Hashtable<String, String>>();
		else
			theGlobTab = theTab;

		try
		{

			BufferedReader in = new BufferedReader(new FileReader(theFileName));

			String line = null;
			Hashtable<String, String> theElemTab = null;
			String theTabKey = null;

			Hashtable<String, Integer> tab = getOffsetTab(theFileName);

			while (true)
			{
				line = in.readLine();
				if (line != null)
				{
					if (line.length() < 10)
						continue;
					String theNumberToken = null;
					try
					{
						theNumberToken = line.substring(tab.get("DATA_ELEMENT_START"), tab.get("DATA_ELEMENT_END"));
						Integer.parseInt(theNumberToken);
						if (theElemTab != null)
						{
							theGlobTab.put(theTabKey, theElemTab);
						}	
						theElemTab = new Hashtable<String, String>();
						theTabKey = "D_" + theNumberToken;
						theElemTab.put("name", theTabKey);

					}
					catch (NumberFormatException ignore)
					{
						//Spezialbehandlung UNA Datenelemente
						if (theNumberToken.contains("UNA") )
						{
							if (theElemTab != null)
							{
								theGlobTab.put(theTabKey, theElemTab);
							}	
							theElemTab = new Hashtable<String, String>();
							theTabKey = "D_" + theNumberToken;
							theElemTab.put("name", theTabKey);

						}

					}
					setDataElemTab("Desc", line, theElemTab, tab.get("DATA_ELEMENT_START"), tab.get("DATA_ELEMENT_END"));
					setDataElemTab("Repr", line, theElemTab, tab.get("DATA_ELEMENT_START"), tab.get("DATA_ELEMENT_END"));

				}
				else
				{
					in.close();
					break;
				}
			}
			if (theElemTab != null)
			{
				theGlobTab.put(theTabKey, theElemTab);
			}	


		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		return theGlobTab;
	}

	/**
	 * @param tokenName
	 * @param line
	 * @param theTab
	 */
	private void setDataElemTab(String tokenName, String line, Hashtable<String, String> theTab, int startIndex, int endIndex)
	{
		if (line.substring(startIndex, endIndex).equals(tokenName))
		{
			String theToken = line.substring(11).trim();
			theTab.put(tokenName, theToken);

		}
	}



}
