package com.sapstern.openedifact.unece.xsd.io;

import java.io.File;

import java.io.FilenameFilter;
/**
 * @author matthias
 *
 */
public class EdifactConverterFilenameFilter implements FilenameFilter{

	@Override
	public boolean accept(File dir, String name) {
	   System.out.println("Directory: "+dir + "=> name of current file: "+name);
	   
   	   
   	   
         if(name.endsWith(".zip")||name.endsWith(".ZIP")) 
         {              
               return true;              
         }
         
         return false;
      
	}

}
