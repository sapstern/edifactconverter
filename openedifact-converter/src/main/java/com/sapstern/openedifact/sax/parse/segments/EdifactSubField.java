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

public class EdifactSubField extends AbstractEdifactField implements Serializable
{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 7408287177704809336L;
	/** Creates a new instance of EdifactSubField */
    public EdifactSubField( String subFieldTagName, String subFieldValue, String releaseChar )
    {
    	super();
        this.subFieldTagName = subFieldTagName;
        this.subFieldValue = super.removeReleaseChar(subFieldValue, releaseChar);
    }
    public String subFieldValue = null;
    public String subFieldTagName = null;
}
