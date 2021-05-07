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

package com.sapstern.openedifact.unece.xsd.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sapstern.openedifact.unece.xsd.log.AbstractLogger;

public class DirectoryProcessor extends AbstractLogger{

	static final int BUFFER = 2048;
	
	

	public static File[] getFiles(String dirName) {

		File[] resultArry = null;
		File currentFile = new File(dirName);
		

		List<File> resultFilesList = new LinkedList<File>(); 
		// returns pathnames for files and directory

		getFileList(currentFile.listFiles(), resultFilesList);  


		Iterator<File> theIterator = resultFilesList.iterator();
		resultArry = new File[resultFilesList.size()];
		for (int i=0; i<resultFilesList.size();i++)
			resultArry[i]=theIterator.next();


		return resultArry;

	}
	/**
	 * recursive traversal of directory tree
	 * @param resultArry
	 * @param resultFilesList
	 */
	private static void getFileList(File[] resultArry, List<File> resultFilesList) {
		if (resultArry==null)
			return;
		for (int i=0;i<resultArry.length;i++)
		{
			if(resultArry[i].isDirectory())
				getFileList(resultArry[i].listFiles(), resultFilesList);
			else
				if(	  resultArry[i].getName().toUpperCase().endsWith("EDED.ZIP")
					||resultArry[i].getName().toUpperCase().endsWith("EDCD.ZIP")
					||resultArry[i].getName().toUpperCase().endsWith("EDSD.ZIP")
					||resultArry[i].getName().toUpperCase().endsWith("UNCL.ZIP")
					||resultArry[i].getName().toUpperCase().endsWith("EDMD.ZIP")) 
					resultFilesList.add(resultArry[i]);
		}
	}
	public static void processFile(String dirName) throws IOException
	{
		LOGGER.info("Directory: "+dirName);
		File[] theFiles = getFiles(dirName);

		for (int i=0;i<theFiles.length;i++)
		{
			File currentFile = theFiles[i];
			LOGGER.info("File: "+currentFile.getName());

			String filenameUPPER = currentFile.getName();

			filenameUPPER = filenameUPPER.toUpperCase();
			if ((
					filenameUPPER.startsWith("EDCD")
					||filenameUPPER.startsWith("EDED")
					||filenameUPPER.startsWith("EDMD")
					||filenameUPPER.startsWith("EDSD")
					||filenameUPPER.startsWith("UNCL")
					||filenameUPPER.startsWith("IDCD")
					||filenameUPPER.startsWith("IDMD")
					||filenameUPPER.startsWith("IDSD")					
					)
					&& filenameUPPER.endsWith(".ZIP") 					
					)
			{				
				processFileInZip(dirName, currentFile, filenameUPPER);

			}

		}

	}
	/**
	 * @param dirName
	 * @param currentFile
	 * @param filenameUPPER
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void processFileInZip(String dirName, File currentFile, String filenameUPPER)throws FileNotFoundException, IOException {
			 
		ZipInputStream zin = new    ZipInputStream(new BufferedInputStream(new FileInputStream(currentFile))); 
		
		writeFilefromZip(zin, dirName+"/"+filenameUPPER.substring(0, 4)+"/");
									      
		zin.close();
	}
	
	/**
	 * recursive traversal of directory tree inside zip from UNECE
	 * 
	 * @param zin 
	 * @param theDir
	 * @throws IOException
	 */
	private static void writeFilefromZip(ZipInputStream zin, String theDir) throws IOException{
		
		ZipEntry entry = zin.getNextEntry();
		if(entry==null)
			return;
		if(entry.isDirectory()) {			
			writeFilefromZip(zin, theDir);
			return;
		}
		
		String fileName = entry.getName();
		if(fileName.indexOf("/")!=-1){
			int startIndex = fileName.lastIndexOf("/");
			startIndex = startIndex + 1;
			fileName = fileName.substring(startIndex, fileName.length());
		}
		fileName = fileName.toUpperCase();	
		FileOutputStream fos = new FileOutputStream(new File(theDir+fileName));
		BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
		int count;
		byte data[] = new byte[BUFFER];
		while ((count = zin.read(data, 0, BUFFER)) != -1) 
		{
			dest.write(data, 0, count);
		}
		dest.flush();
		dest.close();
		writeFilefromZip(zin, theDir);
			
	}
	
	public static void main(String[] args)
	{
		String dirName = null;

		dirName = parseArgs(args);
		try {
			processFile(dirName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	/**
	 * Parst die Argumentenliste (commandline) nach user, password, URL.<BR>
	 */
	static String parseArgs(String args[])
	{
		if ((args == null) || (args.length == 0))
			return null;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-directoryName"))
				return args[++i];


		}
		return null;
	}

}
