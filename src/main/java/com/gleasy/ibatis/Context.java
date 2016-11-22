package com.gleasy.ibatis;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import com.gleasy.domain.Table;
import com.gleasy.domain.TableColumn;


public class Context {
	private Connection connection;
	private String packagePrefix;
	private List<Table> tables = new ArrayList<Table>();;
	private String userName = null;
	private DatabaseMetaData data;
	
	
	

	

	public Connection getConnection() {
		return connection;
	}

	public List<Table> getTables() {
		return tables;
	}
	
	public String getPackagePrefix() {
		return packagePrefix;
	}

	public Context(String url,String driverClass,String userName,String password,String packagePrefix) throws Exception{
		this.userName = userName;
		Class.forName(driverClass);
		this.connection = DriverManager.getConnection(url, userName, password);
		this.packagePrefix = packagePrefix;
		data = this.connection.getMetaData();
		this.getDataBaseTables();
	}
	
	public void getDataBaseTables() throws Exception{
		ResultSet rs = data.getTables(this.connection.getCatalog(), this.userName, null, new String[]{"TABLE","VIEW"});
		while(rs.next()){
			String tableName = rs.getString("TABLE_NAME");
			Table table = new Table(tableName);
			this.tables.add(table);
			getFields(table);
		}
	}
	
	public void getFields(Table table) throws Exception{
		ResultSet rs = data.getColumns(this.connection.getCatalog(),this.userName,table.getName(),null);
		while(rs.next()){
			String name = rs.getString("COLUMN_NAME");
			String jdbcType = rs.getString("TYPE_NAME");
			String comment = rs.getString("REMARKS");
			TableColumn column = new TableColumn(name, jdbcType, comment);
			table.addColumn(column);
		}
	}
	
	public static void main(String[] args) throws Exception {
		Context generator = new Context("jdbc:oracle:thin:@120.132.13.31:1521:orcl", "oracle.jdbc.driver.OracleDriver", "CNNGMOB_ADMIN", "chang3m3", "com.ming");
	}
}
