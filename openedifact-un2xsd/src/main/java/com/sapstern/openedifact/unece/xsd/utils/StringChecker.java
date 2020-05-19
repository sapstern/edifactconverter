package com.sapstern.openedifact.unece.xsd.utils;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




/**
 * Operations on {@link java.lang.String} that are
 * <code>null</code> safe.

 *
 * @see java.lang.String
 * @author <a href="http://jakarta.apache.org/turbine/">Apache Jakarta Turbine</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author Daniel L. Rall
 * @author <a href="mailto:gcoladonato@yahoo.com">Greg Coladonato</a>
 * @author <a href="mailto:ed@apache.org">Ed Korthof</a>
 * @author <a href="mailto:rand_mcneely@yahoo.com">Rand McNeely</a>
 * @author Stephen Colebourne
 * @author <a href="mailto:fredrik@westermarck.com">Fredrik Westermarck</a>
 * @author Holger Krauth
 * @author <a href="mailto:alex@purpletech.com">Alexander Day Chaffee</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author Arun Mammen Thomas
 * @author Gary Gregory
 * @author Phil Steitz
 * @author Al Chou
 * @author Michael Davey
 * @author Reuben Sivan
 * @author Chris Hyzer
 * @author Scott Johnson
 * @since 1.0
 * @version $Id$
 */
public class StringChecker {

  /**
   * 
   * Checks if the String contains any character in the given set of characters.
   * 
   * 
   * 
   * A <code>null</code> String will return <code>false</code>. A <code>null</code> search string will return
   * <code>false</code>.
   * 
   * 
   * <pre>
   * StringUtils.containsAny(null, *)            = false
   * StringUtils.containsAny("", *)              = false
   * StringUtils.containsAny(*, null)            = false
   * StringUtils.containsAny(*, "")              = false
   * StringUtils.containsAny("zzabyycdxx", "za") = true
   * StringUtils.containsAny("zzabyycdxx", "by") = true
   * StringUtils.containsAny("aba","z")          = false
   * </pre>
   * 
   * @param str
   *            the String to check, may be null
   * @param searchChars
   *            the chars to search for, may be null
   * @return the <code>true</code> if any of the chars are found, <code>false</code> if no match or null input
   * @since 2.4
   */
  public static boolean containsAny(String str, String searchChars) {
      if (searchChars == null) {
          return false;
      }
      return containsAny(str, searchChars.toCharArray());
  }

  // ContainsAny
  //-----------------------------------------------------------------------
  /**
   * Checks if the String contains any character in the given
   * set of characters.
   *
   * A <code>null</code> String will return <code>false</code>.
   * A <code>null</code> or zero length search array will return <code>false</code>.
   *
   * <pre>
   * StringUtils.containsAny(null, *)                = false
   * StringUtils.containsAny("", *)                  = false
   * StringUtils.containsAny(*, null)                = false
   * StringUtils.containsAny(*, [])                  = false
   * StringUtils.containsAny("zzabyycdxx",['z','a']) = true
   * StringUtils.containsAny("zzabyycdxx",['b','y']) = true
   * StringUtils.containsAny("aba", ['z'])           = false
   * </pre>
   *
   * @param str  the String to check, may be null
   * @param searchChars  the chars to search for, may be null
   * @return the <code>true</code> if any of the chars are found,
   * <code>false</code> if no match or null input
   * @since 2.4
   */
  public static boolean containsAny(String str, char[] searchChars) {
      if (str == null || str.length() == 0 || searchChars == null || searchChars.length == 0) {
          return false;
      }
      for (int i = 0; i < str.length(); i++) {
          char ch = str.charAt(i);
          for (int j = 0; j < searchChars.length; j++) {
              if (searchChars[j] == ch) {
                  return true;
              }
          }
      }
      return false;
  }
  // Empty checks
  //-----------------------------------------------------------------------
  /**
   * Checks if a String is empty ("") or null.
   *
   * <pre>
   * StringUtils.isEmpty(null)      = true
   * StringUtils.isEmpty("")        = true
   * StringUtils.isEmpty(" ")       = false
   * StringUtils.isEmpty("bob")     = false
   * StringUtils.isEmpty("  bob  ") = false
   * </pre>
   *
   * NOTE: This method changed in Lang version 2.0.
   * It no longer trims the String.
   * That functionality is available in isBlank().
   *
   * @param str  the String to check, may be null
   * @return <code>true</code> if the String is empty or null
   */
  public static boolean isEmpty(String str) {
      return str == null || str.length() == 0;
  }
  // ContainsAll
  //-----------------------------------------------------------------------
  /**
   * Checks if all the String characters are in the given
   * set of characters.
   *
   * A <code>null</code> String will return <code>false</code>.
   * A <code>null</code> or zero length search array will return <code>false</code>.
   *
   * <pre>
   * StringUtils.containsAny(null, *)                = false
   * StringUtils.containsAny("", *)                  = false
   * StringUtils.containsAny(*, null)                = false
   * StringUtils.containsAny(*, [])                  = false
   * StringUtils.containsAny("zzabyycdxx",['z','a']) = true
   * StringUtils.containsAny("zzabyycdxx",['b','y']) = true
   * StringUtils.containsAny("aba", ['z'])           = false
   * </pre>
   *
   * @param str  the String to check, may be null
   * @param searchChars  the chars to search for, may be null
   * @return the <code>true</code> if any of the chars are found,
   * <code>false</code> if no match or null input
   * @since 2.4
   */
  public static boolean containsAll(String str, char[] searchChars) {
	  boolean[] isAllInCharsetArry = new boolean[searchChars.length];
	  for (int i=0; i<isAllInCharsetArry.length;i++)
		  isAllInCharsetArry[i]=false;
      if (str == null || str.length() == 0 || searchChars == null || searchChars.length == 0) {
          return false;
      }
      for (int i = 0; i < str.length(); i++) {
          char ch = str.charAt(i);
          
          for (int j = 0; j < searchChars.length; j++) {
              if (searchChars[j] == ch) {
                  isAllInCharsetArry[j]=true;
              }
          }
      }
      for (int i=0; i<isAllInCharsetArry.length;i++)
		  if (isAllInCharsetArry[i]==false)
			  return false;
      return true;
  }

}