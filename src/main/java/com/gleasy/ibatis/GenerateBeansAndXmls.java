package com.gleasy.ibatis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rudy 4072883@qq.com
 * @since 2013-7-1
 */
public class GenerateBeansAndXmls {
	private Statement stmt;
	private String packagePrefix;
	static boolean isSqlServer = true;
	public GenerateBeansAndXmls(String url, String packagePrefix) throws ClassNotFoundException, SQLException {
		
		if(isSqlServer){
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		}else{
			Class.forName("com.mysql.jdbc.Driver");
		}
		
		this.stmt = DriverManager.getConnection(url).createStatement();
		this.packagePrefix = packagePrefix;
	}

	private List<String> getTables() throws SQLException {
		List<String> tables = new ArrayList<String>();
		String sql = "show tables";
		if(isSqlServer){
			sql = "SELECT Name FROM SysObjects Where XType='U' or XType='V'  ORDER BY Name";
		}
		ResultSet rs = stmt.executeQuery(sql);
		while (rs.next()) {
			tables.add(rs.getString(1));
		}
		return tables;
	}

	private Map<String, String> getFields(String table) throws SQLException {
		Map<String, String> fields = new LinkedHashMap<String, String>();
		String sql = "SHOW FULL FIELDS FROM " + table;
		if(isSqlServer){
			//sql = "SELECT (case when a.colorder=1 then d.name else null end) 表名,  a.colorder 字段序号,a.name 字段名,(case when COLUMNPROPERTY( a.id,a.name,'IsIdentity')=1 then '√'else '' end) 标识, (case when (SELECT count(*) FROM sysobjects  WHERE (name in (SELECT name FROM sysindexes  WHERE (id = a.id) AND (indid in  (SELECT indid FROM sysindexkeys  WHERE (id = a.id) AND (colid in  (SELECT colid FROM syscolumns WHERE (id = a.id) AND (name = a.name)))))))  AND (xtype = 'PK'))>0 then '√' else '' end) 主键,b.name 类型,a.length 占用字节数,  COLUMNPROPERTY(a.id,a.name,'PRECISION') as 长度,  isnull(COLUMNPROPERTY(a.id,a.name,'Scale'),0) as 小数位数,(case when a.isnullable=1 then '√'else '' end) 允许空,  isnull(e.text,'') 默认值,isnull(g.[value], ' ') AS [说明]FROM  syscolumns a left join systypes b on a.xtype=b.xusertype  inner join sysobjects d on a.id=d.id and d.xtype='U' and d.name<>'dtproperties' left join syscomments e on a.cdefault=e.id  left join sys.extended_properties g on a.id=g.major_id AND a.colid=g.minor_id left join sys.extended_properties f on d.id=f.class and f.minor_id=0 where b.name is not null and d.name='"+table+"' order by a.id,a.colorder";
			sql = "SELECT a.name,b.name as type,'' as com FROM syscolumns a, systypes b WHERE a.xusertype = b.xusertype  AND a.id = object_id('"+table+"')";
		}
		ResultSet rs = stmt.executeQuery(sql);
		while (rs.next()) {
			String key = "";
			String value = "";
			if(isSqlServer){
				key = rs.getString(1);
				value = rs.getString(2);
			}else{
				key = rs.getString(1);
				value = rs.getString(2);
			}
			
			fields.put(key, value);
		}
		return fields;
	}

	private Map<String, String> getComments(String table) throws SQLException {
		Map<String, String> comments = new LinkedHashMap<String, String>();
		String sql = "SHOW FULL FIELDS FROM " + table;
		if(isSqlServer){
			//sql = "SELECT (case when a.colorder=1 then d.name else null end) 表名,  a.colorder 字段序号,a.name 字段名,(case when COLUMNPROPERTY( a.id,a.name,'IsIdentity')=1 then '√'else '' end) 标识, (case when (SELECT count(*) FROM sysobjects  WHERE (name in (SELECT name FROM sysindexes  WHERE (id = a.id) AND (indid in  (SELECT indid FROM sysindexkeys  WHERE (id = a.id) AND (colid in  (SELECT colid FROM syscolumns WHERE (id = a.id) AND (name = a.name)))))))  AND (xtype = 'PK'))>0 then '√' else '' end) 主键,b.name 类型,a.length 占用字节数,  COLUMNPROPERTY(a.id,a.name,'PRECISION') as 长度,  isnull(COLUMNPROPERTY(a.id,a.name,'Scale'),0) as 小数位数,(case when a.isnullable=1 then '√'else '' end) 允许空,  isnull(e.text,'') 默认值,isnull(g.[value], ' ') AS [说明]FROM  syscolumns a left join systypes b on a.xtype=b.xusertype  inner join sysobjects d on a.id=d.id and d.xtype='U' and d.name<>'dtproperties' left join syscomments e on a.cdefault=e.id  left join sys.extended_properties g on a.id=g.major_id AND a.colid=g.minor_id left join sys.extended_properties f on d.id=f.class and f.minor_id=0 where b.name is not null and d.name='"+table+"' order by a.id,a.colorder";
			sql = "SELECT a.name,b.name as type,'' as com FROM syscolumns a, systypes b WHERE a.xusertype = b.xusertype  AND a.id = object_id('"+table+"')";
		}
		ResultSet rs = stmt.executeQuery(sql);
		while (rs.next()) {
			String key = "";
			String comment = "";
			if(isSqlServer){
				key = rs.getString(1);
				comment = rs.getString(3);
			}else{
				key = rs.getString(1);
				comment = rs.getString(9);
			}
			
			comments.put(key, comment);
		}
		return comments;
	}

	private String getPropertyType(String jdbcType) {
		if (jdbcType.toLowerCase().startsWith("varchar")
				|| jdbcType.toLowerCase().startsWith("nvarchar")) {
			return "String";
		} else if (jdbcType.toLowerCase().startsWith("bigint")) {
			return "Long";
		} else if (jdbcType.toLowerCase().startsWith("numeric")) {
			return "Double";
		} else if (jdbcType.toLowerCase().startsWith("smallint")
				|| jdbcType.toLowerCase().startsWith("int")) {
			return "Integer";
		} else if (jdbcType.toLowerCase().startsWith("tinyint")) {
			return "Byte";
		} else if (jdbcType.toLowerCase().startsWith("datetime")) {
			return "Date";
		} else if (jdbcType.toLowerCase().startsWith("date")) {
			return "java.sql.Date";
		} else if (jdbcType.toLowerCase().startsWith("time")) {
			return "java.sql.Time";
		} else if (jdbcType.toLowerCase().startsWith("text")) {
			return "String";
		} else if (jdbcType.toLowerCase().startsWith("double")) {
			return "Double";
		}else if (jdbcType.toLowerCase().startsWith("float")) {
			return "Float";
		}else if (jdbcType.toLowerCase().startsWith("decimal")) {
			return "Double";
		}
		return "NULL";
	}

	private String getPropertyName(String fieldName) {
		fieldName = fieldName.toLowerCase();
		String[] items = fieldName.split("_");
		String propertyName = items[0];
		for (int i = 1; i < items.length; i++) {
			String item = items[i];
			
			propertyName += item.substring(0, 1).toUpperCase() + item.substring(1).toLowerCase();
			//System.out.println(fieldName+";"+item+";"+propertyName);
		}
		
		return propertyName;
	}

	private String getPropertyGetterName(String fieldName) {
		String[] items = fieldName.split("_");
		String propertyGetterName = "get";
		for (String item : items) {
			propertyGetterName += item.substring(0, 1).toUpperCase() + item.substring(1).toLowerCase();
		}
		return propertyGetterName;
	}

	private String getPropertySetterName(String fieldName) {
		String[] items = fieldName.split("_");
		String propertyGetterName = "set";
		for (String item : items) {
			propertyGetterName += item.substring(0, 1).toUpperCase() + item.substring(1).toLowerCase();
		}
		return propertyGetterName;
	}

	private String getBeanName(String table) {
		String[] items = table.split("_");
		String beanName = "";
		for (String item : items) {
			beanName += item.substring(0, 1).toUpperCase() + item.substring(1).toLowerCase();
		}
		return beanName;
	}

	private String getBeanString(String table) throws SQLException {
		Map<String, String> fields = this.getFields(table);
		Map<String, String> comments = this.getComments(table);
		StringBuffer bean = new StringBuffer();
		bean.append("package " + this.packagePrefix + ".domain;");

		bean.append("import java.util.Date;");
		bean.append("import java.util.Map;");
		bean.append("import java.io.Serializable;");

		bean.append("public class " + this.getBeanName(table) + " implements Serializable {");
		Set<String> fieldNames = fields.keySet();

		for (String fieldName : fieldNames) {
			String fieldType = fields.get(fieldName);
			bean.append("/**" + comments.get(fieldName) + "*/");
			bean.append("private " + this.getPropertyType(fieldType) + " " + this.getPropertyName(fieldName) + ";");
		}

		bean.append("public " + this.getBeanName(table) + "() {}");

		for (String fieldName : fieldNames) {
			String fieldType = fields.get(fieldName);
			bean.append("public " + this.getPropertyType(fieldType) + " " + this.getPropertyGetterName(fieldName) + "() {");
			bean.append("return " + this.getPropertyName(fieldName) + ";");
			bean.append("}");

			bean.append("public void " + this.getPropertySetterName(fieldName) + "(" + this.getPropertyType(fieldType) + " " + this.getPropertyName(fieldName)
					+ ") {");
			bean.append("this." + this.getPropertyName(fieldName) + " = " + this.getPropertyName(fieldName) + ";");
			bean.append("}");
		}
		bean.append("}");
		return bean.toString();
	}

	private String getDaoString(String beanName) throws SQLException {
		StringBuffer bean = new StringBuffer();
		bean.append("package " + this.packagePrefix + ".dao;");
		bean.append("import com.gleasy.dao.BaseGlobalDAO;");
		bean.append("import " + this.packagePrefix + ".domain." + beanName + ";");
		bean.append("public interface " + beanName + "Dao extends BaseGlobalDAO<" + beanName + "> {");
		bean.append("}");
		return bean.toString();
	}

	private String getDaoImplString(String beanName) throws SQLException {
		StringBuffer bean = new StringBuffer();
		bean.append("package " + this.packagePrefix + ".dao.ibatis;");
		bean.append("import com.gleasy.dao.ibatis.BaseGlobalDAOImpl;");
		bean.append("import " + this.packagePrefix + ".dao." + beanName + "Dao;");
		bean.append("import " + this.packagePrefix + ".domain." + beanName + ";");
		bean.append("public class " + beanName + "DaoImpl extends BaseGlobalDAOImpl<" + beanName + "> implements " + beanName + "Dao {");
		bean.append("}");
		return bean.toString();
	}

	private String getServiceString(String beanName) throws SQLException {
		StringBuffer bean = new StringBuffer();
		bean.append("package " + this.packagePrefix + ".service;");
		bean.append("import com.gleasy.service.BaseServiceGlobal;");
		bean.append("import " + this.packagePrefix + ".domain." + beanName + ";");
		//bean.append("public interface " + beanName + "Service {");
		bean.append("public interface " + beanName + "Service extends BaseServiceGlobal<" + beanName + "> {");
		bean.append("}");
		return bean.toString();
	}

	private String getServiceImplString(String beanName, String beanInstanceName) throws SQLException {
		StringBuffer bean = new StringBuffer();
		bean.append("package " + this.packagePrefix + ".service.impl;");
		//bean.append("import " + this.packagePrefix + ".cache." + beanName + "Cache;");
		bean.append("import " + this.packagePrefix + ".domain." + beanName + ";");
		bean.append("import " + this.packagePrefix + ".dao." + beanName + "Dao;");
		bean.append("import " + this.packagePrefix + ".service." + beanName + "Service;");
		bean.append("import com.gleasy.service.impl.BaseServiceGlobalImpl;");
		bean.append("import org.apache.commons.logging.Log;");
		bean.append("import org.apache.commons.logging.LogFactory;");
		//bean.append("public class " + beanName + "ServiceImpl implements " + beanName + "Service {");
		bean.append("public class " + beanName + "ServiceImpl extends BaseServiceGlobalImpl<"+ beanName+"> implements " + beanName + "Service {");
		bean.append("public final Log log = LogFactory.getLog(" + beanName + "ServiceImpl.class);");
		bean.append("private " + beanName + "Dao " + beanInstanceName + "Dao;");
		//bean.append("private " + beanName + "Cache " + beanInstanceName + "Cache;");
		bean.append("");
		bean.append("public void set" + beanName + "Dao(" + beanName + "Dao " + beanInstanceName + "Dao) {");
		bean.append("this." + beanInstanceName + "Dao = " + beanInstanceName + "Dao;");
		bean.append("setBaseGlobalDAO(this." + beanInstanceName + "Dao);");
		bean.append("}");
		/*
		bean.append("public void set" + beanName + "Cache(" + beanName + "Cache " + beanInstanceName + "Cache) {");
		bean.append("this." + beanInstanceName + "Cache = " + beanInstanceName + "Cache;");
		bean.append("}");
		*/
		bean.append("}");
		return bean.toString();
	}

	private String getCacheString(String beanName, String beanInstanceName) throws SQLException {
		StringBuffer bean = new StringBuffer();
		bean.append("package " + this.packagePrefix + ".cache;");
		bean.append("import " + this.packagePrefix + ".domain." + beanName + ";");
		bean.append("import com.gleasy.service.BusinessLevelException;");
		bean.append("import java.util.List;");
		bean.append("public interface " + beanName + "Cache {");
		bean.append("public void put(" + beanName + " " + beanInstanceName + ") throws BusinessLevelException;");
		bean.append("public " + beanName + " get(Long flagId, Long id) throws BusinessLevelException;");
		bean.append("public void remove(" + beanName + " " + beanInstanceName + ") throws BusinessLevelException;");
		bean.append("public List<" + beanName + "> mget(Long flagId, List<Long> ids) throws BusinessLevelException;");
		bean.append("}");
		return bean.toString();
	}

	private String getListActionString(String beanName, String beanInstanceName) throws SQLException {
		StringBuffer bean = new StringBuffer();
		bean.append("package " + this.packagePrefix + ".action;");
		bean.append("import java.util.HashMap;");
		bean.append("import java.util.List;");
		bean.append("import java.util.Map;");
		bean.append("import org.apache.log4j.Logger;");
		bean.append("import "+this.packagePrefix+".common.Const;");
		bean.append("import "+this.packagePrefix+".service.MapService;");
		bean.append("import com.gleasy.service.BusinessLevelException;");
		bean.append("import com.gleasy.util.SystemExitListener;");
		bean.append("import com.i139.gpf.commando.AbstractAction;");
		bean.append("public class "+beanName+"ListAction extends AbstractAction<Map<String,Object>>{");
		bean.append("private Logger logger = Logger.getLogger("+beanName+"ListAction.class);");
		bean.append("private MapService mapService;public void setMapService(MapService mapService) {this.mapService = mapService;}");
		bean.append("@Override ");
		bean.append("protected Map<String, Object> execute(Map<String, Object> params)throws Exception {");
		bean.append("if (SystemExitListener.isOver())throw new BusinessLevelException(Const.CODE_SYSTEM_EXIT, \"系统暂停服务\");");
		bean.append("List<HashMap> list = mapService.list(params, \""+beanInstanceName+".find\");");
		bean.append("Map<String, Object> result = new HashMap<String, Object>();");
		bean.append("result.put(\"list\", list);");
		bean.append("return result;");
		bean.append("}");
		bean.append("}");
		return bean.toString();
	}
	private String getCacheImplString(String beanName, String beanInstanceName) throws SQLException {
		StringBuffer bean = new StringBuffer();
		bean.append("package " + this.packagePrefix + ".cache.impl;");
		bean.append("import com.gleasy.library.redis.client.CacheRedisClient;");
		bean.append("import com.gleasy.library.redis.client.LocalRedisClient;");
		bean.append("import " + this.packagePrefix + ".cache." + beanName + "Cache;");
		bean.append("import " + this.packagePrefix + ".domain." + beanName + ";");
		bean.append("import com.gleasy.service.BusinessLevelException;");
		bean.append("import java.util.List;");
		bean.append("public class " + beanName + "CacheImpl implements " + beanName + "Cache {");
		bean.append("private CacheRedisClient cacheRedisClient;");
		bean.append("private LocalRedisClient localRedisClient;");
		bean.append("public void setCacheRedisClient(CacheRedisClient cacheRedisClient) {");
		bean.append("this.cacheRedisClient = cacheRedisClient;");
		bean.append("}");
		bean.append("public void setLocalRedisClient(LocalRedisClient localRedisClient) {");
		bean.append("this.localRedisClient = localRedisClient;");
		bean.append("}");
		bean.append("@Override ");
		bean.append("public void put(" + beanName + " " + beanInstanceName + ") throws BusinessLevelException {");
		bean.append("}");
		bean.append("@Override ");
		bean.append("public " + beanName + " get(Long flagId, Long id) throws BusinessLevelException {");
		bean.append("return null;");
		bean.append("}");
		bean.append("@Override ");
		bean.append("public void remove(" + beanName + " " + beanInstanceName + ") throws BusinessLevelException {");
		bean.append("}");
		bean.append("@Override ");
		bean.append("public List<" + beanName + "> mget(Long flagId, List<Long> ids) throws BusinessLevelException {");
		bean.append("return null;");
		bean.append("}");
		bean.append("}");
		return bean.toString();
	}

	private String getBeanInstanceName(String table) {
		return this.getBeanName(table).substring(0, 1).toLowerCase() + this.getBeanName(table).substring(1);
	}

	public String getBeanConfigString(String table) throws SQLException {
		Map<String, String> fields = this.getFields(table);
		StringBuffer bean = new StringBuffer();
		bean.append("<?xml version='1.0' encoding='UTF-8'?>\r\n");
		bean.append("<!DOCTYPE sqlMap PUBLIC '-//ibatis.apache.org//DTD SQL Map 2.0//EN' 'http://ibatis.apache.org/dtd/sql-map-2.dtd'>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<sqlMap namespace='" + this.getBeanInstanceName(table) + "'>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<typeAlias alias='" + this.getBeanName(table) + "' type='" + this.packagePrefix + ".domain" + "." + this.getBeanName(table) + "' />");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<resultMap id='" + this.getBeanInstanceName(table) + "Map' class='" + this.getBeanName(table) + "'>");
		Set<String> fieldNames = fields.keySet();

		for (String fieldName : fieldNames) {
			String fieldType = fields.get(fieldName);
			if ("DATETIME".equals(fieldType.toUpperCase())) {
				fieldType = "TIMESTAMP";
			}
			bean.append("<result column='" + fieldName + "' property='" + this.getPropertyName(fieldName) + "' jdbcType='"
					+ fieldType.toUpperCase().replaceAll("[(].*[)]", "") + "' />");
		}

		bean.append("</resultMap>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<insert id='save' parameterClass='" + this.getBeanName(table) + "'>");
		bean.append("insert into " + table + "(");

		for (String fieldName : fieldNames) {
			bean.append(fieldName + ",");
		}
		bean.setCharAt(bean.length() - 1, ')');
		bean.append("values(");

		for (String fieldName : fieldNames) {
			bean.append("#" + this.getPropertyName(fieldName) + "#,");
		}
		bean.setCharAt(bean.length() - 1, ')');

		bean.append("</insert>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<select id='get' resultMap='" + this.getBeanInstanceName(table) + "Map' parameterClass='" + this.getBeanName(table) + "'>");
		bean.append("select * from " + table + " where id = #id#");
		bean.append("</select>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<delete id='delete' parameterClass='" + this.getBeanName(table) + "'>");
		bean.append("delete from " + table + " where id = #id#");
		bean.append("</delete>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<delete id='deleteSelective' parameterClass='" + this.getBeanName(table) + "'>");
		bean.append("delete from " + table);
		bean.append("<dynamic prepend='where'>");
		for (String fieldName : fieldNames) {
			bean.append("<isNotNull prepend=' and ' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isNotNull>");
		}
		bean.append("</dynamic>");
		bean.append("</delete>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<delete id='deletePropertyAvailable' parameterClass='" + this.getBeanName(table) + "'>");
		bean.append("delete from " + table);
		bean.append("<dynamic prepend='where'>");
		for (String fieldName : fieldNames) {
			bean.append("<isPropertyAvailable prepend='and ' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isPropertyAvailable>");
		}
		bean.append("</dynamic>");
		bean.append("</delete>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<update id='update' parameterClass='" + this.getBeanName(table) + "' >");
		bean.append("update " + table);
		bean.append("<dynamic prepend='set'>");
		for (String fieldName : fieldNames) {
			bean.append("<isNotNull prepend=',' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isNotNull>");
		}
		bean.append("</dynamic>");
		bean.setCharAt(bean.length() - 1, ' ');
		bean.append("where id = #id#");
		bean.append("</update>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<update id='updateSelective' parameterClass='" + this.getBeanName(table) + "'>");
		bean.append("update " + table);
		bean.append("<dynamic prepend='set'>");
		for (String fieldName : fieldNames) {
			bean.append("<isNotNull prepend=',' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isNotNull>");
		}
		bean.append("</dynamic>");
		bean.append("where id = #id#");
		bean.append("</update>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<update id='updatePropertyAvailable' parameterClass='java.util.Map'>");
		bean.append("update " + table);
		bean.append("<dynamic prepend='set'>");
		for (String fieldName : fieldNames) {
			bean.append("<isPropertyAvailable prepend=',' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isPropertyAvailable>");
		}
		bean.append("</dynamic>");
		bean.append("where id = #id#");
		bean.append("</update>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<select id='search' resultMap='" + this.getBeanInstanceName(table) + "Map' parameterClass='" + this.getBeanName(table) + "'>");
		bean.append("select * from " + table);
		bean.append("<dynamic prepend='where'>");
		for (String fieldName : fieldNames) {
			bean.append("<isNotNull prepend='and ' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isNotNull>");
		}
		bean.append("</dynamic>");
		bean.append("</select>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<select id='searchPropertyAvailable' resultMap='" + this.getBeanInstanceName(table) + "Map' parameterClass='java.util.Map'>");
		bean.append("select * from " + table);
		bean.append("<dynamic prepend='where'>");
		for (String fieldName : fieldNames) {
			bean.append("<isPropertyAvailable prepend='and ' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isPropertyAvailable>");
		}
		bean.append("</dynamic>");
		bean.append("</select>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<select id='searchPage' resultMap='" + this.getBeanInstanceName(table) + "Map' parameterClass='java.util.Map'>");
		bean.append("select * from " + table);
		bean.append("<dynamic prepend='where'>");
		for (String fieldName : fieldNames) {
			bean.append("<isNotNull prepend='and ' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isNotNull>");
		}
		bean.append("</dynamic>");
		bean.append("limit #offset#,#pageSize#");
		bean.append("</select>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<select id='searchPropertyAvailablePage' resultMap='" + this.getBeanInstanceName(table) + "Map' parameterClass='java.util.Map'>");
		bean.append("select * from " + table);
		bean.append("<dynamic prepend='where'>");
		for (String fieldName : fieldNames) {
			bean.append("<isPropertyAvailable prepend='and ' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isPropertyAvailable>");
		}
		bean.append("</dynamic>");
		bean.append("limit #offset#,#pageSize#");
		bean.append("</select>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<select id='countSelective' resultClass='Integer' parameterClass='" + this.getBeanName(table) + "'>");
		bean.append("select count(*) from " + table);
		bean.append("<dynamic prepend='where'>");
		for (String fieldName : fieldNames) {
			bean.append("<isNotNull prepend='and ' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isNotNull>");
		}
		bean.append("</dynamic>");
		bean.append("</select>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<select id='countPropertyAvailable' resultClass='Integer' parameterClass='java.util.Map'>");
		bean.append("select count(*) from " + table);
		bean.append("<dynamic prepend='where'>");
		for (String fieldName : fieldNames) {
			bean.append("<isPropertyAvailable prepend='and ' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isPropertyAvailable>");
		}
		bean.append("</dynamic>");
		bean.append("</select>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("</sqlMap>");

		return bean.toString();
	}

	public String getBeanConfigStringMin(String table) throws SQLException {
		Map<String, String> fields = this.getFields(table);
		StringBuffer bean = new StringBuffer();
		bean.append("<?xml version='1.0' encoding='UTF-8'?>\r\n");
		bean.append("<!DOCTYPE sqlMap PUBLIC '-//ibatis.apache.org//DTD SQL Map 2.0//EN' 'http://ibatis.apache.org/dtd/sql-map-2.dtd'>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<sqlMap namespace='" + this.getBeanInstanceName(table) + "'>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<typeAlias alias='" + this.getBeanName(table) + "' type='" + this.packagePrefix + ".domain" + "." + this.getBeanName(table) + "' />");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<resultMap id='" + this.getBeanInstanceName(table) + "Map' class='java.util.HashMap'>");
		//bean.append("<resultMap id='" + this.getBeanInstanceName(table) + "Map' class='" + this.getBeanName(table) + "'>");
		Set<String> fieldNames = fields.keySet();

		for (String fieldName : fieldNames) {
			String fieldType = fields.get(fieldName);
			if ("DATETIME".equals(fieldType.toUpperCase())) {
				fieldType = "TIMESTAMP";
			}
			bean.append("<result column='" + fieldName + "' property='" + this.getPropertyName(fieldName) + "' jdbcType='"
					+ fieldType.toUpperCase().replaceAll("[(].*[)]", "") + "' />");
		}

		bean.append("</resultMap>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<insert id='save' parameterClass='" + this.getBeanName(table) + "'>");
		bean.append("insert into " + table + "(");

		for (String fieldName : fieldNames) {
			bean.append(fieldName + ",");
		}
		bean.setCharAt(bean.length() - 1, ')');
		bean.append("values(");

		for (String fieldName : fieldNames) {
			bean.append("#" + this.getPropertyName(fieldName) + "#,");
		}
		bean.setCharAt(bean.length() - 1, ')');

		bean.append("</insert>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<select id='get' resultMap='" + this.getBeanInstanceName(table) + "Map' parameterClass='" + this.getBeanName(table) + "'>");
		bean.append("select * from " + table + " where id = #id#");
		bean.append("</select>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<delete id='delete' parameterClass='" + this.getBeanName(table) + "'>");
		bean.append("delete from " + table + " where id = #id#");
		bean.append("</delete>");

		
		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<update id='update' parameterClass='" + this.getBeanName(table) + "' >");
		bean.append("update " + table);
		bean.append("<dynamic prepend='set'>");
		for (String fieldName : fieldNames) {
			bean.append("<isNotNull prepend=',' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isNotNull>");
		}
		bean.append("</dynamic>");
		//bean.setCharAt(bean.length() - 1, ' ');
		bean.append("where id = #id#");
		bean.append("</update>");
		
		bean.append("\r\n");
		bean.append("\r\n");
		/*
		bean.append("<update id='update' parameterClass='" + this.getBeanName(table) + "'>");
		bean.append("update " + table);
		
		bean.append(" set id = id");
		bean.append("<dynamic prepend=' '>");
		
		for (String fieldName : fieldNames) {
			
			bean.append("<isNotNull prepend=',' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isNotNull>");
		}
		bean.append("</dynamic>");
		bean.append(" where id = #id#");
		bean.append("</update>");
		
		bean.append("\r\n");
		bean.append("\r\n");*/

		bean.append("<select id='getById' resultMap='" + this.getBeanInstanceName(table) + "Map' parameterClass='java.util.Map'>");
		bean.append("select * from " + table + " where id = ");
		bean.append("#id#");
		bean.append("</select>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<select id='getByIds' resultMap='" + this.getBeanInstanceName(table) + "Map' parameterClass='java.util.Map'>");
		bean.append("select * from " + table + " where id in ");
		bean.append("<iterate open='(' close=')' conjunction=',' property='ids'>");
		bean.append("#ids[]#");
		bean.append("</iterate>");
		bean.append("</select>");

		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<delete id='deleteByIds' parameterClass='java.util.Map'>");
		bean.append("delete from " + table + " where id in ");
		bean.append("<iterate open='(' close=')' conjunction=',' property='ids'>");
		bean.append("#ids[]#");
		bean.append("</iterate>");
		bean.append("</delete>");

		bean.append("\r\n");
		bean.append("\r\n");
		
		bean.append("<sql id='params'>");
		//bean.append("<dynamic prepend=''>");
		for (String fieldName : fieldNames) {
			bean.append("<isNotNull prepend=' and ' property='" + this.getPropertyName(fieldName) + "'>");
			bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
			bean.append("</isNotNull>");
		}
		//bean.append("</dynamic>");
		
		bean.append("</sql>");
		bean.append("<sql id='listParams'>");
		//bean.append("<dynamic prepend=''>");
		for (String fieldName : fieldNames) {
			bean.append("<isNotNull prepend=' and ' property='" + this.getPropertyName(fieldName) + "s'>");
			bean.append(fieldName + " in");
			bean.append("<iterate open='(' close=')' conjunction=',' property='"+this.getPropertyName(fieldName)+"s'>#"+this.getPropertyName(fieldName)+"s[]#</iterate>");
			bean.append("</isNotNull>");
		}
		//bean.append("</dynamic>");
		
		bean.append("</sql>");
		
		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<select id='count' resultClass='Integer' parameterClass='java.util.Map'>");
		bean.append("select count(*) from " + table);
		bean.append(" where 1=1 ");
		bean.append("<dynamic prepend=''>");
		bean.append("<include refid='params'/>");
		bean.append("</dynamic>");
		bean.append("</select>");
		
		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("<select id='list' resultMap='" + this.getBeanInstanceName(table) + "Map' parameterClass='java.util.Map'>");
		if(isSqlServer){
			bean.append("SELECT TOP $pageSize$ * FROM (SELECT *,ROW_NUMBER() OVER (ORDER BY id) AS InnerPDTRRowNo7788 FROM " + table);
			//bean.append(") AS A ");
		}else{
			bean.append("select * from " + table);
		}
						
		if(isSqlServer){
			bean.append(" WHERE 1 = 1 ");
			bean.append("<dynamic prepend=''>");
			bean.append("<include refid='params'/>");
		}else{
			bean.append("<dynamic prepend='where 1 = 1'>");
			for (String fieldName : fieldNames) {
				bean.append("<isNotNull prepend=' and ' property='" + this.getPropertyName(fieldName) + "'>");
				bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
				bean.append("</isNotNull>");
			}
		}
		bean.append("</dynamic>");
		
		if(isSqlServer){
			bean.append(" ) AS A ");
			bean.append(" WHERE  InnerPDTRRowNo7788 > #pageIndex#*#pageSize#");
		}else{
			bean.append(" limit #offset#,#pageSize#");
		}
		bean.append("</select>");
		
		bean.append("\r\n");
		bean.append("\r\n");
		
		bean.append("<select id='find' resultMap='" + this.getBeanInstanceName(table) + "Map' parameterClass='java.util.Map'>");
		bean.append("select * from " + table);
		
		if(isSqlServer){
			bean.append(" WHERE 1 = 1 ");
			bean.append("<dynamic prepend=''>");
			bean.append("<include refid='params'/>");
		}else{
			bean.append("<dynamic prepend='where 1 = 1'>");
			for (String fieldName : fieldNames) {
				bean.append("<isNotNull prepend=' and ' property='" + this.getPropertyName(fieldName) + "'>");
				bean.append(fieldName + " = #" + this.getPropertyName(fieldName) + "#");
				bean.append("</isNotNull>");
			}
		}
		bean.append("</dynamic>");
		
		bean.append("</select>");
		
		bean.append("\r\n");
		bean.append("\r\n");

		bean.append("</sqlMap>");

		return bean.toString();
	}

	private void generateBean(String table) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/bean");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/bean/" + this.getBeanName(table) + ".java";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		fw.write(this.getBeanString(table));
		fw.close();
	}

	private void generateBeanConfig(String table) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/xml");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/xml/" + this.getBeanName(table) + ".map.xml";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		fw.write(this.getBeanConfigStringMin(table));
		fw.close();
	}

	private void generateDao(String table) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/dao");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/dao/" + this.getBeanName(table) + "Dao.java";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		fw.write(this.getDaoString(this.getBeanName(table)));
		fw.close();
	}

	private void generateDaoImpl(String table) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/daoImpl");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/daoImpl/" + this.getBeanName(table) + "DaoImpl.java";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		fw.write(this.getDaoImplString(this.getBeanName(table)));
		fw.close();
	}

	private void generateService(String table) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/service");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/service/" + this.getBeanName(table) + "Service.java";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		fw.write(this.getServiceString(this.getBeanName(table)));
		fw.close();
	}

	private void generateServiceImpl(String table) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/serviceImpl");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/serviceImpl/" + this.getBeanName(table) + "ServiceImpl.java";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		fw.write(this.getServiceImplString(this.getBeanName(table), this.getBeanInstanceName(table)));
		fw.close();
	}

	private void generateCache(String table) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/cache");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/cache/" + this.getBeanName(table) + "Cache.java";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		fw.write(this.getCacheString(this.getBeanName(table), this.getBeanInstanceName(table)));
		fw.close();
	}

	private void generateCacheImpl(String table) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/cacheImpl");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/cacheImpl/" + this.getBeanName(table) + "CacheImpl.java";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		fw.write(this.getCacheImplString(this.getBeanName(table), this.getBeanInstanceName(table)));
		fw.close();
	}
	
	private void generateListAction(String table) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/action");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/action/" + this.getBeanName(table) + "ListAction.java";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		fw.write(this.getListActionString(this.getBeanName(table), this.getBeanInstanceName(table)));
		fw.close();
	}


	private void generateSqlMapConfig(List<String> tables) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/config");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/config/" + "sqlMap.txt";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		for (String table : tables) {
			fw.write("<sqlMap resource=\"" + this.packagePrefix.replace(".", "/") + "/domain/" + this.getBeanName(table) + ".map.xml\" />");
			fw.write("\r\n");
		}
		fw.close();
	}

	private void generateDaoConfig(List<String> tables) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/config");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/config/" + "dao.txt";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		for (String table : tables) {
			fw.write("<bean id=\"" + this.getBeanInstanceName(table) + "Dao\" class=\"" + this.packagePrefix + ".dao.ibatis." + this.getBeanName(table)
					+ "DaoImpl\" />");
			fw.write("\r\n");
		}
		fw.close();
	}

	private void generateServiceConfig(List<String> tables) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/config");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/config/" + "service.txt";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		for (String table : tables) {
			fw.write("<bean id=\"" + this.getBeanInstanceName(table) + "Service\" class=\"" + this.packagePrefix + ".service.impl." + this.getBeanName(table)
					+ "ServiceImpl\">");
			fw.write("\r\n");
			fw.write("    <property name=\"" + this.getBeanInstanceName(table) + "Dao\" ref=\"" + this.getBeanInstanceName(table) + "Dao\" />");
			//fw.write("\r\n");
			//fw.write("    <property name=\"" + this.getBeanInstanceName(table) + "Cache\" ref=\"" + this.getBeanInstanceName(table) + "Cache\" />");
			fw.write("\r\n");
			fw.write("</bean>");
			fw.write("\r\n");
			fw.write("\r\n");
		}
		fw.close();
	}

	private void generateActionConfig(List<String> tables) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/config");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/config/" + "controller.txt";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
		for (String table : tables) {
			fw.write("<bean class=\"" + this.packagePrefix + ".action." + this.getBeanName(table)
					+ "ListAction\" parent=\"abstractAction\">");
			fw.write("\r\n");
			fw.write("  <property name=\"mapService\" ref=\"mapService\" />");
			fw.write("\r\n");
			fw.write("  <property name=\"path\" value=\"/"+this.getBeanInstanceName(table)+"/list\" />");
			fw.write("\r\n");
			fw.write("</bean>");
			fw.write("\r\n");
		}
		fw.close();
	}
	private void generateCacheConfig(List<String> tables) throws IOException, SQLException {
		File folder = new File(this.packagePrefix.replace(".", "/") + "/config");
		folder.mkdirs();
		String path = this.packagePrefix.replace(".", "/") + "/config/" + "cache.txt";
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");

		fw.write("<bean id=\"cacheRedisClient\" class=\"com.gleasy.library.redis.client.CacheRedisClient\">");
		fw.write("\r\n");
		fw.write("  <constructor-arg value=\"xxx_cache\" />");
		fw.write("\r\n");
		fw.write("</bean>");
		fw.write("\r\n");
		fw.write("\r\n");
		fw.write("<bean id=\"localRedisClient\" class=\"com.gleasy.library.redis.client.LocalRedisClient\">");
		fw.write("\r\n");
		fw.write("  <constructor-arg value=\"xxx_persist\" />");
		fw.write("\r\n");
		fw.write("</bean>");
		fw.write("\r\n");
		fw.write("\r\n");

		for (String table : tables) {
			fw.write("<bean id=\"" + this.getBeanInstanceName(table) + "Cache\" class=\"" + this.packagePrefix + ".cache.impl." + this.getBeanName(table)
					+ "CacheImpl\">");
			fw.write("\r\n");
			fw.write("  <property name=\"cacheRedisClient\" ref=\"cacheRedisClient\" />");
			fw.write("\r\n");
			fw.write("  <property name=\"localRedisClient\" ref=\"localRedisClient\" />");
			fw.write("\r\n");
			fw.write("</bean>");
			fw.write("\r\n");
			fw.write("\r\n");
		}
		fw.close();
	}
	public void generate() throws SQLException, IOException {
		this.generate(null);
	}
	public void generate(String tableName) throws SQLException, IOException {
		if(tableName != null){
			List<String> list = new ArrayList<String>();
			list.add(tableName);
			this.generateByList(list);
		}else{
			this.generateByList(null);
		}
		
	}
	public void generateByList(List<String> tableNames) throws SQLException, IOException {
		this.deleteFile(new File(this.packagePrefix.replace(".", "/")));

		if(tableNames != null){
			List<String> ts = new ArrayList<String>();
			
			for(String t : tableNames){
				ts.add(t.toLowerCase());
			}
			tableNames = ts;
		}
		
		List<String> t_tables = this.getTables();
		List<String> tables = new ArrayList<String>();
		if(t_tables != null){
			for (String table : t_tables) {
				if(tableNames != null){
					if(!tableNames.contains(table.toLowerCase())){
						continue;
					}
				}
				tables.add(table);
			}
		}
		for (String table : tables) {
			
			//this.generateBean(table);
			this.generateBeanConfig(table);
			//this.generateDao(table);
			//this.generateDaoImpl(table);
			//this.generateService(table);
			//this.generateServiceImpl(table);
			if(!isSqlServer){
				this.generateCache(table);
				this.generateCacheImpl(table);
			}
			this.generateListAction(table);
		}
		if(tables.size()>0){
			this.generateSqlMapConfig(tables);
			//this.generateDaoConfig(tables);
			//this.generateServiceConfig(tables);
			if(!isSqlServer){
				this.generateCacheConfig(tables);
			}
			this.generateActionConfig(tables);
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

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		//String url = "jdbc:mysql://183.129.228.207:3307/face-global?user=mysql3307&password=mysql3307";
		String url = "jdbc:sqlserver://123.59.87.196:1433;DatabaseName=AZPlatForm;user=kchf526pa;password=help7575;";
		//String url = "jdbc:sqlserver://183.131.153.162:1433;databaseName=implatform";
		String appName = "com.gleasy.biso";
		GenerateBeansAndXmls g = new GenerateBeansAndXmls(url, appName);
		List<String> list = new ArrayList<String>();
		/*
		list.add("pdtr_dsm_achieve_rate");
		list.add("pdtr_dsm_growth_rate");
		list.add("pdtr_dsm_product_sales_volume");
		list.add("pdtr_dsm_operative_site_sale_volume");
		list.add("pdtr_dsm_hospital_growth_target");
		*/
		list.add("DW_TM_SESSION_LOG");
		g.generateByList(list);
		//g.generate("pdtr_doctor_status");
		//g.generate();
//		List<String> tables = g.getTables();
//		if(tables != null){
//			System.out.println(tables);
//		}
	}
}