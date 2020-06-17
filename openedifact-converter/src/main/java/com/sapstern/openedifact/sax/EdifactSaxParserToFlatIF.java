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