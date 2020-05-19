/**
 * Free Message Converter Copyleft 2007 - 2014 Matthias Fricke mf@sapstern.com


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
package com.sapstern.openedifact.unece.xsd;

import java.io.File;
import java.io.IOException;

import com.sapstern.openedifact.unece.xsd.gen.Generator;


public class XSDFileGenerator 
{

	private static String theProjectDirectory = "/home/matthias/gitDieter/openedifact-un2xsd/src/test/UNECE";
	private static String theMessageDirectory = theProjectDirectory+"/<VERSION>/IN/EDMD";
	private static String theDirectory = theProjectDirectory+"/<VERSION>/IN";
	private static String messageType = "EDIFACTINTERCHANGE";
	private static String theVersion = "";
	private static String theEncoding = "ISO-8859-1";


	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		parseArgs(args);
		theMessageDirectory = theMessageDirectory.replace("<VERSION>", theVersion);
		theDirectory = theDirectory.replace("<VERSION>", theVersion);
		Generator theGenerator = null;
		try {
			
			theGenerator = new Generator(theVersion, theProjectDirectory, theEncoding, messageType, theMessageDirectory, theDirectory);
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(-1);
		} 
		
		File theDirectoryFile = new File ( theMessageDirectory );
		String[] arryFileNames = theDirectoryFile.list();
		System.out.println(theMessageDirectory);
		for ( int z=0; z<arryFileNames.length; z++ )
		{
			if (arryFileNames[z].equalsIgnoreCase("CVS") )
				continue;

			try {
				theGenerator.process(arryFileNames[z]);
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	/**
	 * Parst die Argumentenliste (commandline) nach directoryName, messageType, theVersion.<BR>
	 */
	static void parseArgs(String args[])
	{
		if ((args == null) || (args.length == 0))
			return;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-directoryName"))
			{
				theProjectDirectory = args[++i];
				theMessageDirectory = theProjectDirectory+"/<VERSION>/IN/EDMD";
				theDirectory = theProjectDirectory+"/<VERSION>/IN";
			}
			if (args[i].equals("-messageType"))
				messageType = args[++i];
			if (args[i].equals("-version"))
				theVersion = args[++i];
			if (args[i].equals("-encoding"))
				theEncoding = args[++i];

		}
	}

}
