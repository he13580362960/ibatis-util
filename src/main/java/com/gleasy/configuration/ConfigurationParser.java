package com.gleasy.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.mysql.jdbc.StringUtils;

public class ConfigurationParser {
	public static Configuration parse(String configFilePath){
		File configFile = new File(configFilePath);
		Configuration configuration = new Configuration();
		SAXReader reader = new SAXReader();
		Document doc;
		try {
			doc = reader.read(configFile);
			return parseXml(doc,configuration);
		} catch (DocumentException e) {
			//e.printStackTrace();
		}
		return configuration;
	}
	private static Configuration parseXml(Document doc, Configuration configuration){
		Element root = doc.getRootElement();
		List<Element> elements = root.elements();
		for(Element element : elements){
			String name = element.getName();
			if("includeTables".equals(name)){
				parseIncludeTables(element,configuration);
			}
			if("url".equals(name)){
				parseUrl(element,configuration);
			}
			if("packagePrefix".equals(name)){
				parsePackagePrefix(element,configuration);
			}
		}
		return configuration;
	}
	
	private static void parseUrl(Element element, Configuration configuration){
		configuration.setUrl(element.getText().trim());
	}
	
	private static void parsePackagePrefix(Element element, Configuration configuration){
		configuration.setPackagePrefix(element.getText().trim());
	}
	
	private static void parseIncludeTables(Element element, Configuration configuration){
		List<Element> values = element.elements();
		for(Element value : values){
			String name = value.getName();
			if("value".equals(name) && !StringUtils.isEmptyOrWhitespaceOnly(value.getText())){
				configuration.addIncludeTables(value.getText());
			}
		}
	}
}
