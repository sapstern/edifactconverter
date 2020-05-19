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



package com.sapstern.openedifact.sax.parse.segments;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;



public class EdifactSegment implements Serializable
{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 6850710192580655266L;
	/** Creates a new instance of EdifactSegment */
    public EdifactSegment( String name, boolean isGroupSegment )
    {
        this.segmentName = name;
        this.segmentFields = new LinkedList<EdifactField>();
        this.childSegments = new LinkedList<EdifactSegment>();
        this.isGroupSegment = isGroupSegment;
        
    }
   public Element theElement = null;
   public boolean isGroupSegment = false;
   public String segmentName = null;
   public List<EdifactField> segmentFields = null;
   public List<EdifactSegment> childSegments = null;
   public String segmentString = null;

}
