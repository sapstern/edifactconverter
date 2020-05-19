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
package com.sapstern.openedifact.unece.xsd.data;

public interface TypeDef
{
	public static final int TYPE_COMPOSITE = 0; 
	public static final int TYPE_DATAELEMENT = 1;
	public static final String SEGMENT_NAME_CHARS_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	/** The following static offset references are necessary as these values may vary for old versions **/
	/** The fields will be looked up at runtime using reflection API**/
	
	/** Name **/
	public static final int NAME_START_OFFSET_96A = 6;
	public static final int NAME_END_OFFSET_96A = 10;
	public static final int NAME_START_OFFSET_96B = 6;
	public static final int NAME_END_OFFSET_96B = 10;
	public static final int NAME_START_OFFSET_97A = 6;
	public static final int NAME_END_OFFSET_97A = 10;
	public static final int NAME_START_OFFSET_97B = 6;
	public static final int NAME_END_OFFSET_97B = 10;
	public static final int NAME_START_OFFSET = 7;
	public static final int NAME_END_OFFSET = 11;
	
	/** Mandatory Flag **/
	public static final int SEGMENT_MANDATORY_START_OFFSET_96A = 60;
	public static final int SEGMENT_MANDATORY_END_OFFSET_96A = 61;
	public static final int SEGMENT_MANDATORY_START_OFFSET_96B = 60;
	public static final int SEGMENT_MANDATORY_END_OFFSET_96B = 61;
	public static final int SEGMENT_MANDATORY_START_OFFSET_97A = 60;
	public static final int SEGMENT_MANDATORY_END_OFFSET_97A = 61;
	public static final int SEGMENT_MANDATORY_START_OFFSET_97B = 60;
	public static final int SEGMENT_MANDATORY_END_OFFSET_97B = 61;
	public static final int SEGMENT_MANDATORY_START_OFFSET_98A = 60;
	public static final int SEGMENT_MANDATORY_END_OFFSET_98A = 61;
	public static final int SEGMENT_MANDATORY_START_OFFSET_98B = 60;
	public static final int SEGMENT_MANDATORY_END_OFFSET_98B = 61;
	public static final int SEGMENT_MANDATORY_START_OFFSET = 56;
	public static final int SEGMENT_MANDATORY_END_OFFSET = 57;
	
	
	/** Composite Offsets **/
	public static final int COMPOSITE_MANDATORY_START_OFFSET_96A = 59;
	public static final int COMPOSITE_MANDATORY_END_OFFSET_96A = 60;
	public static final int COMPOSITE_MANDATORY_START_OFFSET_96B = 59;
	public static final int COMPOSITE_MANDATORY_END_OFFSET_96B = 60;
	public static final int COMPOSITE_MANDATORY_START_OFFSET_97A = 59;
	public static final int COMPOSITE_MANDATORY_END_OFFSET_97A = 60;
	public static final int COMPOSITE_MANDATORY_START_OFFSET_97B = 59;
	public static final int COMPOSITE_MANDATORY_END_OFFSET_97B = 60;
	public static final int COMPOSITE_MANDATORY_START_OFFSET_98A = 59;
	public static final int COMPOSITE_MANDATORY_END_OFFSET_98A = 60;
	public static final int COMPOSITE_MANDATORY_START_OFFSET_98B = 59;
	public static final int COMPOSITE_MANDATORY_END_OFFSET_98B = 60;	
	public static final int COMPOSITE_MANDATORY_START_OFFSET = 55;
	public static final int COMPOSITE_MANDATORY_END_OFFSET = 56;
	
	/** Data Element Offsets **/
	public static final int DATA_ELEMENT_START_OFFSET_96A = 3;
	public static final int DATA_ELEMENT_END_OFFSET_96A = 7;
	public static final int DATA_ELEMENT_START_OFFSET_96B = 3;
	public static final int DATA_ELEMENT_END_OFFSET_96B = 7;
	public static final int DATA_ELEMENT_START_OFFSET_97A = 3;
	public static final int DATA_ELEMENT_END_OFFSET_97A = 7;
	public static final int DATA_ELEMENT_START_OFFSET_97B = 3;
	public static final int DATA_ELEMENT_END_OFFSET_97B = 7;
	public static final int DATA_ELEMENT_START_OFFSET_98A = 3;
	public static final int DATA_ELEMENT_END_OFFSET_98A = 7;
	public static final int DATA_ELEMENT_START_OFFSET_98B = 3;
	public static final int DATA_ELEMENT_END_OFFSET_98B = 7;
	public static final int DATA_ELEMENT_START_OFFSET = 5;
	public static final int DATA_ELEMENT_END_OFFSET = 9;
	
}
