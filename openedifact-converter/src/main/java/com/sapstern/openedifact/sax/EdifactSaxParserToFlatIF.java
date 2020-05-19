package com.sapstern.openedifact.sax;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public interface EdifactSaxParserToFlatIF extends ContentHandler, DTDHandler, EntityResolver, ErrorHandler {

	String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	String parse(String inputXML) throws SAXException, IOException;
	
	String parse(String inputXML, String nameSpacePrefix) throws SAXException, UnsupportedEncodingException, IOException;

	void emit(String s) throws SAXException;

	void nl() throws SAXException;
	
	void initGlobalVariables();
	

	

}