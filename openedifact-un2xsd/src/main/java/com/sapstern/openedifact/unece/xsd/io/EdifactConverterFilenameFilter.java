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
