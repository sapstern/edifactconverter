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



package com.sapstern.openedifact.transform;


import java.util.LinkedList;
import java.util.List;




public class ElementProperties
{
    
    /** Creates a new instance of ElementProperties */
    public ElementProperties ()
    {
        previousElementNames = new LinkedList<String>();

    }

    public  String elementName = null;
    //Is this element part of a composite sequence, but not the first sibling in this sequence
    public boolean isComposite = false;
    //Is this the first sibling of a composite sequence of Elements
    //Used in EdifactParserToFlat.startElement ()
    public boolean isFirstSiblingOfCompositeRow = false;
    
    public boolean isEDIFACTSegment = false;

    public String elementType = "noType";

    public String xsdName;

    public boolean isRootSegment = false;
    
    public boolean isEmpty = false;
    
    public List<String> previousElementNames = null;
    //Does the type definition of this node contain an attribute with name composite
    public boolean isCompositeType = false;
    //Does this node have a child node with name composite
    public boolean isCompositeChild = false;

    public String nameOfCompositeSubField = null;
    
    public List<String> childElementNames = null; 
}
