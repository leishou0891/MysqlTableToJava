package my.suya55.project.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.CaseFormat;

/**
 * Make Domain Object File use Mysql Tables.
 * @author Kim Seong Su
 * @since 2012.10.15
 */
public class App {
    public static final String MYSQL_URL 	= "jdbc:mysql://localhost:3306/***";
    public static final String MYSQL_USER_NAME	= "**";
    public static final String MYSQL_USER_PWD	= "***";
    public static final String GEN_FOLDER	= "/Users/suya55/Desktop/tmp";
    public static final String PACKAGE_NAME	= "com.test.persistence";
    public static final String DOMAIN_PACKAGE_NAME	= "com.test.domain";
    public static final String CODE=
	    	"package " +DOMAIN_PACKAGE_NAME+ ";\n"+
	    	"public class %s { \n" +
    		" %s \n" +
    		"\t@Override\n" +
    		"\tpublic String toString(){\n" +
    		"\t\treturn %s;\n" +
    		"\t}\n" +
    		"}";
    public static final String XML_CODE=
	    	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
		    "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n"+
		    "\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n"+
		    "<mapper namespace=\""+PACKAGE_NAME+".%sMapper\">\n"+
		    "<resultMap id=\"defaultResultMapper\" type=\""+DOMAIN_PACKAGE_NAME+".%s\">\n" +
		    "%s\n"+
		    "</resultMap>\n" +
		    "</mapper>";
    public static void main(String[] args) {
	try {
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

	Connection conn = null;
	try {
	    conn = DriverManager.getConnection(MYSQL_URL,MYSQL_USER_NAME,MYSQL_USER_PWD);
	} catch (SQLException ex) {
	    // handle any errors
	    System.out.println("SQLException: " + ex.getMessage());
	    System.out.println("SQLState: " + ex.getSQLState());
	    System.out.println("VendorError: " + ex.getErrorCode());
	}

	Statement stmt = null;
	ResultSet rs = null;
	List<String> tableList = new ArrayList<String>();
	try {
	    stmt = conn.createStatement();
	    rs = stmt.executeQuery("show table status");
	    while(rs.next()){
		tableList.add(rs.getString(1));
	    }
	    genFiles(tableList,stmt);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    private static void genFiles(List<String> tableList, Statement stmt) throws SQLException, IOException {
	for(String tableName : tableList){
	    String fieldString = "";
	    String xmlFieldString = "";
	    ResultSet rs = stmt.executeQuery("desc "+tableName);
	    String toString = "";
	    while(rs.next()){
		String fieldName = getCamelName(rs.getString(1));
		fieldString += "\tprivate "+getDataType(rs.getString(2))+" "+fieldName+";\n";
		toString += "\""+fieldName+"=\"+this."+fieldName+"+\",\"+ ";
		xmlFieldString+="\t<result property=\""+fieldName+"\" column=\""+rs.getString(1)+"\" />\n";
	    }
	    toString += "\"\"";
	    toString = toString.replaceFirst("\\+\\\",\\\"\\+ \\\"\\\"", "");
	    String className = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName);
	    File f = new File(GEN_FOLDER, className+".java");
	    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
	    bw.write(String.format(CODE, className ,fieldString,toString));
	    bw.flush();
	    bw.close();
	    File xml_f = new File(GEN_FOLDER, className+"Mapper.xml");
	    bw = new BufferedWriter(new FileWriter(xml_f));
	    bw.write(String.format(XML_CODE, className ,className,xmlFieldString));
	    bw.flush();
	    bw.close();
	    System.out.println(f.getAbsolutePath()+ " file generation success!");

	}
    }

    private static String getCamelName(String string) {
	return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, string);
    }

    private static String getDataType(String inStr) {
	String s = inStr.toLowerCase();
	if(s.contains("int")){
	    return "int";
	}else if(s.contains("varchar")){
	    return "String";
	}else if(s.contains("date")){
	    return "String";
	}else if(s.contains("time")){
	    return "String";
	}else if(s.contains("float")){
	    return "float";
	}
	return "";
    }

}
