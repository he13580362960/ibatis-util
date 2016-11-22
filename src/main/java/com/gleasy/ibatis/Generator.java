package com.gleasy.ibatis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import com.gleasy.domain.Table;
import com.gleasy.domain.TableColumn;

public class Generator {
	private Context context;
	private List<String> includeTables;

	public Generator(Context context) {
		super();
		this.context = context;
	}
	
	public String getBeanXmlMap(Table table){
		StringBuffer sb = new StringBuffer();
		List<TableColumn> columns = table.getColumns();
		
		sb.append("<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE sqlMap PUBLIC '-//ibatis.apache.org//DTD SQL Map 2.0//EN' 'http://ibatis.apache.org/dtd/sql-map-2.dtd'>");
		sb.append("<sqlMap namespace='"+table.getPropertyName()+"'>");
		
		//resultMap
		sb.append("<resultMap id='"+table.getPropertyName()+"Map' class='java.util.HashMap'>");
		for(TableColumn column:columns){
			sb.append("<result column='"+column.getName()+"' property='"+column.getPropertyName()+"' jdbcType='"+column.getJdbcType()+"' />");
		}
		sb.append("</resultMap>");
		sb.append("</sqlMap>");
		return sb.toString();
	}
	
	public void generateXmlMap() throws Exception{
		List<Table> tables = context.getTables();
		for(Table table:tables){
			if(includeTables != null && !includeTables.isEmpty()){
				if(!includeTables.contains(table.getName())){
					continue;
				}
			}
			String xmlStr = getBeanXmlMap(table);
			String folderPath = this.context.getPackagePrefix().replace(".", "/") + "/xml";
			String file = this.context.getPackagePrefix().replace(".", "/") + "/xml/" + table.getBeanName() + ".map.xml";;
			writeFile(folderPath,file,xmlStr);
		}
	}
	
	public void writeFile(String folderPath,String file,String content) throws Exception{
		File folder = new File(folderPath);
		folder.mkdirs();
		String path = file;
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		fw.write(content);
		fw.close();
	}
	
	public void generate(List<String> includeTables) throws Exception{
		this.includeTables = includeTables;
		this.generateXmlMap();
	}
	
	public void generate() throws Exception{
		this.generateXmlMap();
	}
	
	public static void main(String[] args) throws Exception {
		Context context = new Context("jdbc:oracle:thin:@120.132.13.31:1521:orcl", "oracle.jdbc.driver.OracleDriver", "CNNGMOB_ADMIN", "chang3m3", "com.ming");
		Generator generator = new Generator(context);
		generator.generate();
	}
	
}
