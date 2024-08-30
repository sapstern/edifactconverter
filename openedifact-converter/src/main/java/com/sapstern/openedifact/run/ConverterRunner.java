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

package com.sapstern.openedifact.run;

import java.io.*;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.sapstern.openedifact.sax.EdifactSaxParserToFlat;
import com.sapstern.openedifact.sax.EdifactSaxParserToFlatIF;
import com.sapstern.openedifact.sax.EdifactSaxParserToXML;
import com.sapstern.openedifact.sax.EdifactSaxParserToXMLIF;

public class ConverterRunner {

	static final int PROCESS_2_FLAT = 0;  
	static final int PROCESS_2_XML = 1;

	static boolean isNamespace = false;
	static int processToRun = -1;
	static String inputString = null;
	static String namespacePrefix = "";
	static Level theLevel = null;
	

	
	
	public static void main(String[] args) {

		parseArgs(args);
		
		switch(processToRun)
		{
		case PROCESS_2_FLAT:
			try {
				EdifactSaxParserToFlatIF theFlatParser = EdifactSaxParserToFlat.factory(false);					
				String ediData = theFlatParser.parse(inputString, namespacePrefix);
				PrintWriter p = new PrintWriter(new FileOutputStream(new File("out.edi")));
				System.out.println(ediData);
				p.write(ediData);
				p.close();
			} catch (SAXException  | ParserConfigurationException | IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			break;
		case PROCESS_2_XML:
			try {	
				Logger theLogger = null;
				if (theLevel!=null)
					theLogger = initLogging(theLevel);
				else
					theLogger = initLogging(Level.INFO);
				EdifactSaxParserToXMLIF theXmlParser = EdifactSaxParserToXML.factory("UTF-8", theLogger, isNamespace);
				String xmlData = theXmlParser.parseEdifact(inputString);
				PrintWriter p = new PrintWriter(new FileOutputStream(new File("out.xml")));
				System.out.println(xmlData);
				p.write(xmlData);
				p.close();
			} catch (SAXException | TransformerException | IOException  e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			break;
		}
	}

	/**
	 * Convenience method to control logging level
	 * @param theLogger
	 * @param level
	 * @return
	 */
	public static Logger initLogging (Level level) {
		Logger theLogger = java.util.logging.Logger.getAnonymousLogger();
		Handler handlerObj = new ConsoleHandler();
		handlerObj.setLevel(level);
		theLogger.addHandler(handlerObj);
		theLogger.setLevel(level);
		theLogger.setUseParentHandlers(false);
		return theLogger;
	}
	
	
	/**
	 * Parst die Argumentenliste (commandline) nach directoryName, messageType, theVersion.<BR>
	 */
	static void parseArgs(String[] args)
	{
		if ((args == null) || (args.length == 0))
			return;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-process"))
			{
				String theProcess = args[++i];
				if (theProcess.equals("flat"))
					processToRun = PROCESS_2_FLAT;
				if (theProcess.equals("xml"))
					processToRun = PROCESS_2_XML;
			}
			if (args[i].equals("-fileIn"))
			{
				StringBuilder fileContents = new StringBuilder();

			    Scanner scanner = null;
				try {
					scanner = new Scanner(new File(args[++i]));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    while(scanner.hasNextLine()) {
			            fileContents.append(scanner.nextLine());
			    }
			    inputString = fileContents.toString();
			    scanner.close();				
			}
			
			if (args[i].equals("-data"))
				inputString = args[++i];
			
			if(args[i].equals("-nsPrefix"))
				namespacePrefix = args[++i];
				
			if(args[i].equals("-outNs"))
				isNamespace = Boolean.parseBoolean(args[++i]);
			
			if(args[i].equals("-logLevel"))
			{
				theLevel = Level.parse(args[++i]);
			}
			
		}
	}

}
