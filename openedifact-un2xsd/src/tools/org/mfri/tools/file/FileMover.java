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

package org.mfri.tools.file;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Just an utility class
 * @author matthias
 *
 */

public class FileMover
{
	public static String theWorkspaceDirectory = null;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		parseargs(args);
		if (theWorkspaceDirectory==null)
			return;
		File theDirectoryFile = new File(theWorkspaceDirectory);
		try
		{
			getFile(theDirectoryFile);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void getFile(File theFile) throws IOException
	{
		if (theFile==null)
			return;

		File[] arryFile = theFile.listFiles();
		for (int i=0;i<arryFile.length;i++)
		{
			if(arryFile[i].getName().startsWith("openedifact-xsd-libs-")&&arryFile[i].isDirectory())
			{
				File[] theDirArry = arryFile[i].listFiles();
				for (int j=0;j<theDirArry.length;j++)
				{
					if(theDirArry[j].getName().endsWith("target")&&theDirArry[j].isDirectory())
					{
						File[]	theResultArryFiles = theDirArry[j].listFiles();
						for (int z=0;z<theResultArryFiles.length;z++)
						{
							if(theResultArryFiles[z].getName().startsWith("openedifact-xsd-libs-")&&theResultArryFiles[z].getName().endsWith("SNAPSHOT.jar")&&!theResultArryFiles[z].isDirectory())
							{
								File resultFile = new File("target/"+theResultArryFiles[z].getName());								
								FileUtils.copyFile(theResultArryFiles[z], resultFile);
							}
						}	 
					}
				}


			}

		}
		
	}
	
	public static void parseargs (String[] args)
	{
		for (int i=0;i<args.length;i++)
		{
			if (args[i].equals("-dir"))
			{
				i++;
				theWorkspaceDirectory = args[i];
			}
		}

	}

}
