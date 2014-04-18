package fatworm.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.List;
import java.util.Scanner;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import fatworm.parser.FatwormLexer;
import fatworm.parser.FatwormParser;
import fatworm.util.PlanMaker;

class Main {
	static String flname = "testcases_revised/myTest/test.fwt";
//	static String flname = "test.fwt";
	public static void main(String[] args) {
		try {
			File file = new File(flname);
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			String command;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty() || line.charAt(0) == '@') {
					System.out.println(line);
					continue;
				}
				command = "";
				while (line!=null && !line.contains(";")) {
					command += line+" ";
					line = reader.readLine();
				}				
				command += prefixOf(line, ';');
//				System.out.println(command);
				System.out.println(PlanMaker.makePlan(command));
			}
			
		} catch (Exception e) {
			System.err.print(e);
			e.printStackTrace();
		}
	}
	
	private static String prefixOf(String str, char split) {
		assert(str != null);
		String ans = "";
		for (int i = 0; i < str.length(); ++i) 
			if (str.charAt(i)==split)
				break;
			else ans += str.charAt(i);
		return ans;
	}
}