Builds a fat jar bundled with converter and xsd files (maintain pom.xml for EDIFACT versions included).

Build:

mvn clean compile assembly:single

Can be run from commandline:

java -cp openedifact-converter-runner-2.2-SNAPSHOT-jar-with-dependencies.jar com.sapstern.openedifact.run.ConverterRunner -process flat -fileIn ../src/test/resources/INVOIC_01B_EDI_XML_1.xml

Can be called like this (groovy example script):

**
 * @author sapstern
 *
 */
import com.sap.gateway.ip.core.customdev.util.Message;
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

import java.util.HashMap;



def Message processData(Message message) {
	
	def	map = message.getHeaders();
	def int theProcess = map.get("theProcess")	
	def body = 	message.getBody(String);

	ByteArrayOutputStream out = new ByteArrayOutputStream(convert(theProcess, body));

	message.setBody(out.toByteArray());

return message;

}

def String convert(int processFlag, String inputString){

	def int PROCESS_2_FLAT = 0;  
	def int PROCESS_2_XML = 1;

	def boolean isNamespace = false;
	def String namespacePrefix = "";
	def Level theLevel = null;
	
	
	switch(processFlag)
		{
		case PROCESS_2_FLAT:			
				EdifactSaxParserToFlatIF theFlatParser = EdifactSaxParserToFlat.factory(false);					
				return  theFlatParser.parse(inputString, namespacePrefix);
		case PROCESS_2_XML:
				Logger theLogger = null;
				if (theLevel!=null)
					theLogger = initLogging(theLevel);
				else
					theLogger = initLogging(Level.INFO);
				EdifactSaxParserToXMLIF theXmlParser = EdifactSaxParserToXML.factory("UTF-8", theLogger, isNamespace);
				return theXmlParser.parseEdifact(inputString); 
			break;
		}
}

def Logger initLogging (Level level) {
		Logger theLogger = java.util.logging.Logger.getAnonymousLogger();
		Handler handlerObj = new ConsoleHandler();
		handlerObj.setLevel(level);
		theLogger.addHandler(handlerObj);
		theLogger.setLevel(level);
		theLogger.setUseParentHandlers(false);
		return theLogger;
}
