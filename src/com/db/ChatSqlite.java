package com.db;

import java.sql.*;
import org.sqlite.JDBC;

/**
 * Created by leiyang on 2016/11/3.
 */
public class ChatSqlite {

    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;

    public void init() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1433;DatabaseName=chat", "sa", "ly19960205");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeSQL(String sqlstr){
        try {
            preparedStatement = connection.prepareStatement(sqlstr);
            resultSet = preparedStatement.executeQuery();
//            displayResult();
//            closeSQL();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void insertToSQl(String sqlstr) {
        executeSQL(sqlstr);
    }
    public void createTable(String sqlstr){
        executeSQL(sqlstr);
    }
    public void querySQL(String sqlstr){
        executeSQL(sqlstr);
    }
    public void deleteSQL(String sqlstr){
        executeSQL(sqlstr);
    }
    public void displayResult(){
        try {
            while (resultSet.next()) {
                System.out.println("时间："+resultSet.getString("mTime"));
                System.out.println("发件人："+resultSet.getString("fromName"));
                System.out.println("收件人："+resultSet.getString("toName"));
                System.out.println("信息："+resultSet.getString("msg"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeSQL(){
        try {
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}
