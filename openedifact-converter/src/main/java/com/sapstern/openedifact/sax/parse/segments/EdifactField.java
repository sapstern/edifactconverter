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

/*
 * EdifactSegment.java
 *
 * Created on 3. Februar 2006, 14:58
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sapstern.openedifact.sax.parse.segments;

import java.io.Serializable;
import java.util.LinkedList;

import java.util.List;



public class EdifactField extends AbstractEdifactField implements Serializable
{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -7047966512672525123L;
	//Constructor for non composite field
    public EdifactField( String fieldTagName, String fieldValue, String releaseChar )
    {    	
    	super();
    	this.fieldTagName = fieldTagName;
        this.fieldValue = removeReleaseChar(fieldValue, releaseChar);        
        isComposite = false;
        
    }
    //Constructor for composite field
    public EdifactField( String fieldTagName )
    {    
    	super();
        this.fieldTagName = fieldTagName;
        isComposite = true;
        subFields = new LinkedList<EdifactSubField>();
        
    }    
   public String fieldValue = null;
   public String fieldTagName = null;
   public List<EdifactSubField> subFields = null;
   public boolean isComposite;
}
