package com.sswiml.util;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取表相关
 */
public class JDBCUtil {

    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String DBURL = "jdbc:mysql://localhost:3306/testdb";
    private static final String DBUSER = "root";
    private static final String DBPASS = "dba";
    private Connection conn = null;
    private PreparedStatement pStat = null;
    private ResultSet rs = null;

    public  Connection getConnectionn() {
        try {
            Class.forName(DRIVER).newInstance();
            return DriverManager.getConnection(DBURL, DBUSER, DBPASS);
        } catch (Exception e) {
            return null;
        }
    }

    public void close() {
        try {
            if (rs != null)
                rs.close();
            if (pStat != null)
                pStat.close();
            if (conn != null)
                conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
}

    //获取表信息
    public void getTable(String tablename){
        if(conn==null){
            conn=getConnectionn();
        }

        DatabaseMetaData databaseMetaData= null;
        try {
            databaseMetaData = conn.getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ResultSet resultSet= null;
        try {
            resultSet = databaseMetaData
                    .getTables(null,null,tablename,new String[]{"TABLE"});
        } catch (SQLException e) {
            e.printStackTrace();
        }

        while (true){
            try {
                if (!resultSet.next()) break;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                System.out.println(resultSet.getString("TABLE_NAME"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    //获取字段信息
    public Map<String,Map<String,String>> getField(String tableName) {

        if(conn==null){
            conn=getConnectionn();
        }
        DatabaseMetaData databaseMetaData=null;
        Map<String,Map<String,String>> tableFieldMap=new HashMap<String,Map<String,String>>();
        try {
            databaseMetaData = conn.getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ResultSet resultSet=null;
        try {
           resultSet=databaseMetaData.getColumns(null,"%",tableName, "%");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while(true){
            try {
                if (!resultSet.next()) break;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
               // System.out.println(resultSet.getString("COLUMN_NAME"));
                Map<String,String> columnMap=new HashMap<String, String>();
                columnMap.put("TYPE_NAME",resultSet.getString("TYPE_NAME"));
                columnMap.put("COLUMN_SIZE",resultSet.getString("COLUMN_SIZE"));
                tableFieldMap.put(resultSet.getString("COLUMN_NAME").toLowerCase(),columnMap);

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return tableFieldMap;
    }

}
