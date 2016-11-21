package com.gleasy.domain;

public class TableColumn{
	private String name;
	private String jdbcType;
	private String comment;
	public TableColumn(){}
	public TableColumn(String name, String jdbcType, String comment) {
		super();
		this.name = name;
		this.jdbcType = jdbcType;
		this.comment = comment;
	}
	
	public String getPropertyName(){
		String fieldName = this.name.toLowerCase();
		String[] items = fieldName.split("_");
		String propertyName = items[0];
		for (int i = 1; i < items.length; i++) {
			String item = items[i];
			propertyName += item.substring(0, 1).toUpperCase() + item.substring(1).toLowerCase();
		}
		return propertyName;
	}
	
	public String getGetter(){
		return getSetterOrGetter("get");
	}
	
	public String getSetter(){
		return getSetterOrGetter("set");
	}
	
	public String getSetterOrGetter(String prefix){
		String[] items = this.name.split("_");
		String methodName = prefix;
		for (String item : items) {
			methodName += item.substring(0, 1).toUpperCase() + item.substring(1).toLowerCase();
		}
		return methodName;
	}

	@Override
	public String toString() {
		return "TableColumn [name=" + name + ", jdbcType=" + jdbcType
				+ ", comment=" + comment + "]";
	}
}