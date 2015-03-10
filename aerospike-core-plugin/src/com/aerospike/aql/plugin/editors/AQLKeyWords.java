/* 
 * Copyright 2012-2014 Aerospike, Inc.
 *
 * Portions may be licensed to Aerospike, Inc. under one or more contributor
 * license agreements.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.aerospike.aql.plugin.editors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aerospike.aql.AQL;
import com.aerospike.core.CoreActivator;

public class AQLKeyWords {
	private static List<String> keyWordList = null;
	private static String[] keywords = {  
		"aggregate",
		"and",
		"as",
		"asc",
		"between", 
		"by",
		"create", 
		"desc", 
		"delete",
		"drop", 
		"execute", 
		"from",
		"get",
		"index",
		"indexes",
		"insert",
		"into", 
		"on", 
		"order",
		"package",
		"packages",
		"register",
		"remove",
		"select",
		"set",
		"sets",
		"show",
		"stat",
		"values", 
		"where" 
		 }; 

	private static String[] constants = { "pk", "numeric", "string" };

	public static String[] getKeywords() {
		if (keyWordList == null){
			keyWordList = AQL.getKeyWords();
//			Pattern pattern = Pattern.compile("'(.+)'");
//
//			URL url;
//			try {
//				url = new URL("platform:/plugin/" + CoreActivator.PLUGIN_ID + "/AQLast.tokens");
//				URLConnection conn = url.openConnection();
//				InputStream inputStream = conn.getInputStream();
//				BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
//				String inputLine;
//
//				boolean keep = false;
//				while ((inputLine = in.readLine()) != null) {
//					if (inputLine.startsWith("'aggregate'")) {
//						keep = true;
//						keyWordList = new ArrayList<String>();
//					}
//					if (!keep) continue;
//					Matcher matcher = pattern.matcher(inputLine);
//					if (matcher.find()){
//						keyWordList.add(matcher.group(1));
//					}
//					//System.out.println(inputLine);
//				}
//				in.close();
//
//			} catch (IOException e) {
//				e.printStackTrace();
//				return keywords;
//			}
		}
		return keyWordList.toArray(new String[keyWordList.size()]);
	}

	public static String[] getConstants() {
		return constants;
	} 

	
}
