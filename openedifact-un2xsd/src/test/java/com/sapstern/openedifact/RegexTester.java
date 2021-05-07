package com.sapstern.openedifact;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pattern pattern = Pattern.compile("[MC]\\s+[a-z][a-z]?\\.\\.[0-9]+");
		Matcher matcher = pattern.matcher("010   8395  Returnable package freight payment C  an..3");
		if(matcher.find()){
			System.out.println(matcher.group());
		}
		matcher = pattern.matcher("020   8393  Returnable package load contents, coded        C  an..3");
		if(matcher.find()){
			System.out.println(matcher.group());
		}
		matcher = pattern.matcher("020    1131  Code list identification code             C      an..17");
		if(matcher.find()){
			System.out.println(matcher.group());
		}
	}

}
