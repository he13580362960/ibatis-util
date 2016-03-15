package com.gleasy.ibatis;

import java.io.File;

public class GenerateClassNames {
	public static void main(String[] args) {
		File file = new File("com/gleasy/gtalk/bean");
		String[] files = file.list();
		for (String f : files) {
			System.out.print("\"");
			System.out.print("com.gleasy.gtalk.domain." + f.replace(".java", ""));
			System.out.print("\"");
			System.out.println(",");
		}
	}
}