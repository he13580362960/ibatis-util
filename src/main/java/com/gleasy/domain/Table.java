package com.gleasy.domain;

import java.util.ArrayList;
import java.util.List;

public class Table {
	private String name;
	private List<TableColumn> columns = new ArrayList<TableColumn>();
	public Table(String name) {
		super();
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<TableColumn> getColumns() {
		return columns;
	}
	
	public void addColumn(TableColumn column){
		this.columns.add(column);
	}
	
	public String getBeanName() {
		String[] items = this.name.split("_");
		String beanName = "";
		for (String item : items) {
			beanName += item.substring(0, 1).toUpperCase() + item.substring(1).toLowerCase();
		}
		return beanName;
	}
}
