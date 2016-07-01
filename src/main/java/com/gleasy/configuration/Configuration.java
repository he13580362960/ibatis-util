package com.gleasy.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class Configuration {
	private List<String> includeTables = new ArrayList<String>();
	private String url = null;
	private String packagePrefix = null;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPackagePrefix() {
		return packagePrefix;
	}
	public void setPackagePrefix(String packagePrefix) {
		this.packagePrefix = packagePrefix;
	}
	public void addIncludeTables(String tableName){
		includeTables.add(tableName);
	}
	public List<String> getIncludeTables() {
		return includeTables;
	}
	public void setIncludeTables(List<String> includeTables) {
		this.includeTables = includeTables;
	}
}
