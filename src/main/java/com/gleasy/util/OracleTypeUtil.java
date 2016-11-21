package com.gleasy.util;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class OracleTypeUtil implements TypeUtil{
	private Map<String,String> typeMap = new HashMap<String, String>();
	
	public OracleTypeUtil() {
	}

	@Override
	public String jdbcToJava(String jdbcType) {
		return typeMap.get(jdbcType);
	}
	
}
