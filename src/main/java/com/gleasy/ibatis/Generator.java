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
	private static String NEW_LINE = "\r\n";
	private static String SPACE = "\t";
	
	private String getSpace(int lv){
		String space = "";
		while(lv>0){
			space+=SPACE;
			lv--;
		}
		return space;
	}
	
	public Generator(Context context) {
		super();
		this.context = context;
	}
	
	public String getBeanXmlMap(Table table){
		StringBuffer sb = new StringBuffer();
		List<TableColumn> columns = table.getColumns();
		
		sb.append("<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE sqlMap PUBLIC '-//ibatis.apache.org//DTD SQL Map 2.0//EN' 'http://ibatis.apache.org/dtd/sql-map-2.dtd'>"+NEW_LINE);
		sb.append("<sqlMap namespace='"+table.getPropertyName()+"'>"+NEW_LINE);
		
		//resultMap
		sb.append("<resultMap id='"+table.getPropertyName()+"Map' class='java.util.HashMap'>"+NEW_LINE);
		for(TableColumn column:columns){
			sb.append(getSpace(1)+"<result column='"+column.getName()+"' property='"+column.getPropertyName()+"' jdbcType='"+column.getJdbcType()+"' />"+NEW_LINE);
		}
		sb.append("</resultMap>"+NEW_LINE);
		sb.append("</sqlMap>"+NEW_LINE);
		
		//params
		sb.append("<sql id='params'>"+NEW_LINE);
		for(TableColumn column:columns){
			sb.append(getSpace(1)+"<isNotNull prepend=' and ' property='"+column.getPropertyName()+"'>"+column.getName()+" = #"+column.getPropertyName()+"# </isNotNull>"+NEW_LINE);
		}
		sb.append("</sql>"+NEW_LINE);
		
		//listparams
		sb.append("<sql id='paramsIn'>"+NEW_LINE);
		for(TableColumn column:columns){
			sb.append(getSpace(1)+"<isNotNull prepend=' and ' property='"+column.getPropertyName()+"s'>"+NEW_LINE);
			sb.append(getSpace(2)+column.getName()+" in"+NEW_LINE);
			sb.append(getSpace(2)+"<iterate open='(' close=')' conjunction=',' property='"+column.getPropertyName()+"s'>#"+column.getPropertyName()+"s[]#</iterate>"+NEW_LINE);
			sb.append(getSpace(1)+"</isNotNull>"+NEW_LINE);
		}
		sb.append("</sql>"+NEW_LINE);
		
		//find
		sb.append("<select id='find' resultMap='"+table.getPropertyName()+"Map' parameterClass='java.util.Map'>"+NEW_LINE);
		sb.append(getSpace(1)+"SELECT * FROM "+table.getName()+" WHERE 1=1 <include refid='params'/>"+NEW_LINE);
		sb.append("</select>");
		return sb.toString();
	}
	
	public void generateXmlMap(Table table) throws Exception{
		String xmlStr = getBeanXmlMap(table);
		String folderPath = this.context.getPackagePrefix().replace(".", "/") + "/xml";
		String file = this.context.getPackagePrefix().replace(".", "/") + "/xml/" + table.getBeanName() + ".map.xml";;
		writeFile(folderPath,file,xmlStr);
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
		this.generate();
	}
	
	public void generate() throws Exception{
		this.deleteFile(new File(context.getPackagePrefix().replace(".", "/")));
		List<Table> tables = context.getTables();
		for(Table table:tables){
			if(includeTables != null && !includeTables.isEmpty()){
				if(!includeTables.contains(table.getName())){
					continue;
				}
			}
			this.generateXmlMap(table);
		}
	}
	
	public void deleteFile(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File f : file.listFiles()) {
					this.deleteFile(f);
				}
			}
			file.delete();
		}
	}
	
	public static void main(String[] args) throws Exception {
		Context context = new Context("jdbc:oracle:thin:@120.132.13.31:1521:orcl", "oracle.jdbc.driver.OracleDriver", "CNNGMOB_ADMIN", "chang3m3", "com.ming");
		Generator generator = new Generator(context);
		generator.generate();
	}
	
}
