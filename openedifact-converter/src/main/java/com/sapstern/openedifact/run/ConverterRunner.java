package com.sapstern.openedifact.run;

import java.io.*;
import java.util.Scanner;

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

	static int processToRun = -1;
	static String inputString = null;
	static String namespacePrefix = "";
	

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
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case PROCESS_2_XML:
			try {
				EdifactSaxParserToXMLIF theXmlParser = EdifactSaxParserToXML.factory("UTF-8");
				String xmlData = theXmlParser.parseEdifact(inputString);
				PrintWriter p = new PrintWriter(new FileOutputStream(new File("out.xml")));
				System.out.println(xmlData);
				p.write(xmlData);
				p.close();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
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
			
			if(args[i].equals("-ns"))
				namespacePrefix = args[++i];
				


		}
	}

}
