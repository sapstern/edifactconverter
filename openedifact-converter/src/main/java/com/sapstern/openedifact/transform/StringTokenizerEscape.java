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

package com.sapstern.openedifact.transform;

import java.util.LinkedList;
import java.util.List;

/**
 * @author O1706
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class StringTokenizerEscape {
	protected int pos = 0;
	String buffer = null;
	String delimiter = null; // ' segment + field : subfield
	boolean returnDelims = false;
	String escChar = null; // ? in EDIFACT

	java.util.LinkedList<String> ll = null; 
	java.util.LinkedList<String> ll1 = null; //MFRI
	public StringTokenizerEscape(String str, String delim, boolean returnDelims, String escapeChar) {
		//System.err.println("str == " + str);
		this.buffer = str;
		this.delimiter = delim;
		this.returnDelims = returnDelims;
		this.escChar = escapeChar;
		ll = new java.util.LinkedList<String>();
		while (hasMoreTokens2()) {
			nextToken2();
		}
		java.util.LinkedList<String> lltmp = new java.util.LinkedList<String>();
		while (!ll.isEmpty()) {
			// remove empty strings
			String s = (String) ll.removeFirst();
			if (!s.equals("")) {
				lltmp.add(s);
			}
		}
		ll.clear();
		ll.addAll(lltmp);
		lltmp.clear();
		if (returnDelims == false) {
			// remove delimiters
			while (!ll.isEmpty()) {
				String s = (String) ll.removeFirst();
				if (!s.equals(delimiter)) {
					lltmp.add(s);
				}
			}
			ll.clear();
			ll.addAll(lltmp);
			lltmp.clear();			
		}
       ll1 = new LinkedList<String>(ll);
	}
	private String nextToken2() {
		StringBuffer bufferDynamic = new StringBuffer("");
		boolean delimExit = false;
		int i = this.pos;
		if (buffer == null) {
			return null;
		}
		while (true) {
			delimExit = false;
			if (i > this.buffer.length() - 1) {
				this.pos = i;
				break;
			} else if (this.buffer.charAt(i) == this.escChar.charAt(0)) {
				// found escape character: add next character and move to next next character
				// add the escape char
				bufferDynamic.append(this.buffer.charAt(i));
				i++;
				if (i > this.buffer.length() - 1) {
					this.pos = buffer.length();
					break;
				}
				//add the escaped character
				bufferDynamic.append(this.buffer.charAt(i));
				//move to next character
				i++;
			} else if (this.buffer.charAt(i) == this.delimiter.charAt(0)) {
				// delimiter --> return token
				i++; // skip delimiter and move to next char			    
				this.pos = i;
				delimExit = true;
				break;
			} else {
				// append current char and move to next char
				bufferDynamic.append(this.buffer.charAt(i));				
				i++;
			}

		}
		ll.add(bufferDynamic.toString());
		if(delimExit){
			ll.add(delimiter);
		}		
		return (null);
	}
	private boolean hasMoreTokens2() {
		if (buffer == null) {
			return false;
		}
		return (!(this.pos > this.buffer.length() - 1));
	}
	public boolean hasMoreTokens() {
			return (! ll.isEmpty());
		
	}
	public String nextToken() {
		return (String) ll.removeFirst();
	}
	public List<String> getAllTokens()
	{
		return ll1;
	}
}
