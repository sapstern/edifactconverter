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

public class DirectoryProcessor {

	static final int BUFFER = 2048;

	public static File[] getFiles(String dirName) {

		File[] resultArry = null;
		File currentFile = new File(dirName);
		EdifactConverterFilenameFilter fileNameFilter = new EdifactConverterFilenameFilter();

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
		System.out.println("Directory: "+dirName);
		File[] theFiles = getFiles(dirName);

		for (int i=0;i<theFiles.length;i++)
		{
			File currentFile = theFiles[i];
			System.out.println("File: "+currentFile.getName());

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
		String currentDir = filenameUPPER.substring(0, 4);
		FileInputStream fis = new FileInputStream(currentFile); 
		ZipInputStream zin = new    ZipInputStream(new BufferedInputStream(fis)); 
		BufferedOutputStream dest = null;

		ZipEntry entry;
		while((entry = zin.getNextEntry()) != null) 
		{
			System.out.println("Extracting: " +entry);
			int count;
			byte data[] = new byte[BUFFER];
			// write the files to the disk
			FileOutputStream fos = new FileOutputStream(new File(dirName+"/"+currentDir+"/"+entry.getName()));
			dest = new BufferedOutputStream(fos, BUFFER);
			while ((count = zin.read(data, 0, BUFFER)) != -1) 
			{
				dest.write(data, 0, count);
			}
			dest.flush();
			dest.close();
		}				      

		zin.close();
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
