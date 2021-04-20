package com.lanyu.multdao.mybatis;

import org.springframework.beans.factory.annotation.Value;

import java.sql.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: suibin
 * @Date: 2021/04/19/5:37
 * @Description:
 */
public class MongoUtil {

    private static String mongodbHost="localhost:27017";

    @Value("${mult.dao.mongodb.host}")
    public  void setMongodbHost(String mongodbHost) {
        this.mongodbHost = mongodbHost;
    }

    private static String mongodbDatabase="bcactc_20200623";

    @Value("${mult.dao.mongodb.database}")
    public  void setMongodbDatabase(String mongodbDatabase) {
        this.mongodbDatabase = mongodbDatabase;
    }


    private static String username="";

    @Value("${mult.dao.mongodb.username}")
    public void setUsername(String username) {
        this.username = username;
    }


    private static String password="";

    @Value("${mult.dao.mongodb.password}")
    public  void setPassword(String password) {
        this.password = password;
    }

    private MongoUtil(){}

    Statement stmt = null;
    ResultSet rst = null;
    private	static Connection con = null;
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        //If instance has not been created yet, create it
        if (con == null) {
            synchronized (Sender.class) {
                if (con == null) {
                    Class.forName("mongodb.jdbc.MongoDriver");
                    String url="jdbc:mongo://"+mongodbHost+"/"+mongodbDatabase+"?debug=true&rebuildschema=true";
                    con = DriverManager.getConnection(url,username,password);

                }
            }
        }
        return con;
    }

}
