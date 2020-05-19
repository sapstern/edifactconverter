package com.sapstern.openedifact.sax;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sapstern.openedifact.sax.parse.segments.EdifactField;
import com.sapstern.openedifact.sax.parse.segments.EdifactSegment;

public interface EdifactSaxParserToXMLIF extends ContentHandler, DTDHandler, EntityResolver, ErrorHandler {

	String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	// ===========================================================
	// XML Reader Interface Implementation
	// ===========================================================
	void parse(InputSource source) throws IOException, SAXException;

	Object getProperty(String s);

	void setProperty(String s, Object o);

	/**
	 * @param flatEdifact
	 * @return
	 * @throws SAXException 
	 */
	List<EdifactSegment> setupRawSegmentList(String flatEdifact) throws SAXException;

	/**
	 * @param fieldTagName
	 * @param subFieldValue
	 * @param fieldObject
	 */
	void addSubField(String fieldTagName, String subFieldValue, EdifactField fieldObject);

	void parse(String uri);

	/**
	 * @param inputEDIFACT
	 * @return
	 * @throws TransformerException
	 * @throws UnsupportedEncodingException
	 */
	String parseEdifact(String inputEDIFACT) throws TransformerException, UnsupportedEncodingException,IOException;

	ByteArrayOutputStream parseEdifact(InputSource inputEDIFACT) throws TransformerException, UnsupportedEncodingException, IOException;

}