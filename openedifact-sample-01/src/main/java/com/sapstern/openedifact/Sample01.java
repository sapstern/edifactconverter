package com.sapstern.openedifact;



import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.sapstern.openedifact.sax.EdifactSaxParserToXML;
import com.sapstern.openedifact.sax.EdifactSaxParserToXMLIF;


/**
 * Simple Showcase
 *
 */
public class Sample01 
{
    public static void main( String[] args ) throws Exception
    {
    	

        InputStream inputStream = Sample01.class.getResourceAsStream("/orders.edi");
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
        String contents = in.readLine();
    	EdifactSaxParserToXMLIF theXmlParser = EdifactSaxParserToXML.factory("UTF-8");
    	String edifactXml = theXmlParser.parseEdifact(contents);
		
        System.out.println(edifactXml);
    }
    
}
