# -edifactconverter-

# -edifactconverter-

First Steps

you need maven, ant and jdk 1.8 installed on your computer.

Open a command line shell in a local directory.

Clone the repository into that directory. (Have a look at the git documentation on how to do that) You the get a lot of sub directories which hold many maven sub projects.

openedifact-converter => Holds the converter jar after the build.
openedifact-sample-01 => Not used so far.
openedifact-un2xsd => Holds the xsd generator jar after build (if you want to generate xsd files yourself).
openedifact-xsd-libs- => Holds the jar files containing the Edifact xsd files for that Edifact release.
Issue a "mvn clean install" on the command line. It should then start the build which takes some time.
As a result, you find the resulting jar files under the respective "target" sub directory.

The Parser:

The parser works in such a way that it uses the appropriate xsd file to analyse the data to be processed. The xsd schema files must be present on the classpath in order to work, otherwise you will get an exception. For each message type to be processed there must be an appropriate xsd schema file.

E.g. if you want to parse 07a INVOIC, then you need an EDIFACTINTERCHANGE_INVOIC_07A.xsd on the classpath. The filename is important here, it must always look like this: EDIFACTINTERCHANGE_[Messagetype]_[Version].xsd

How to use the converter

Put the openedifact-converter-2.2-SNAPSHOT.jar (the version might change) file on the classpath

Put the sxd schema files you need on the classpath

To convert from flat UN/EDIFACT to XML you proceed like this:

com.openedi.sax.EdifactSaxParserToXML theXmlParser = EdifactSaxParserToXML .factory("UTF-8"); "<Encoding e.g. UTF-8" );

String edifactXml = xmlParser.parseEdifact(in); "InputSource an Edifact flat file instance as InputSource or String

To convert from flat from XML to UN/EDIFACT you proceed like this:
com.openedi.sax.EdifactSaxParserToFlat theFlatParser = EdifactSaxParserToFlat.factory( false ); "false means non validating

String flatEdifact = flatParser.parse( "" );

2018.04.30: There is a new method which can be used for parsing, if and only if the EDIFACT-XML carries a namespaceprefix for each XML tag:

String parse(String inputXML, String nameSpacePrefix) throws Exception;

This might be necessary, as the determination of the message version and therewith the XSD which has to be used for parsing is been done with simple String methods. And here in order to get the offsets correct, we might need the namespace prefix.

There is a conveniance class to run the converter directly from the command line.

After having build the project (see above), you can call the conveter like this:

Create a testing directoy

Copy openedifact-converter-2.2-SNAPSHOT.jar as well as the xsd jars for the version you like to use (e.g. openedifact-xsd-libs-07A-1.1-SNAPSHOT.jar) in your testing directory

Issue java -cp openedifact-converter-2.2-SNAPSHOT.jar:openedifact-xsd-libs--1.1-SNAPSHOT.jar com.sapstern.openedifact.run.ConverterRunner -process flat/xml -fileIn your xml/edi file -ns you can put your xml namespace here only if converting to flat

