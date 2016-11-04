package com.db;

import java.sql.*;

public class Practice {
    public static void main(String[] arg) {
        PreparedStatement ps = null;
        Connection ct = null;
        ResultSet rs = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            ct = DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1433;DatabaseName=chat", "sa", "ly19960205");
            ps = ct.prepareStatement("select * from chatTest");
            rs = ps.executeQuery();
            while (rs.next()) {
                String s = rs.getString("name");
                System.out.println(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }


    }
}