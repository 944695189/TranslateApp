package com.example.dell.myapplication;

import java.sql.Connection;
import java.sql.DriverManager;

public class MySQLConnections {
    private String driver = "";
    private String dbURL = "";
    private String user = "";
    private String password = "";
    private static MySQLConnections connection = null;
    public MySQLConnections() throws Exception {
        driver = "com.mysql.jdbc.Driver";//驱动类
        dbURL = "jdbc:mysql://rm-cn-nwy3ibzy80028p9o.rwlb.rds.aliyuncs.com:3306/login";
        //dbURL = "jdbc:mysql://localhost:3306/mydatabase";

        user = "root";
        password = "321524446x";
        System.out.println("dbURL:" + dbURL);
    }
    public void main(String[] args){

        MySQLConnections.getConnection();
    }
    public static Connection getConnection() {
        Connection conn = null;
        if (connection == null) {
            try {
                connection = new MySQLConnections();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        try {
            Class.forName(connection.driver);
            conn = DriverManager.getConnection(connection.dbURL,
                    connection.user, connection.password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}