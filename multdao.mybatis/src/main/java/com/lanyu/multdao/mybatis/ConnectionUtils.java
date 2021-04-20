package com.lanyu.multdao.mybatis;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: suibin
 * @Date: 2021/04/16/10:48
 * @Description:
 */
public class ConnectionUtils {
    private ConnectionUtils(){

    }
    //饿汉式单例
    private static ConnectionUtils connectionUtils=new ConnectionUtils();
    public static ConnectionUtils getInstance(){
        return connectionUtils;
    }
    private ThreadLocal<Connection> threadLocal=new ThreadLocal<>();//保存当前线程的连接
    //从当前线程获取连接
    public Connection getCurrentThreadConn(Connection conn) throws SQLException{
        Connection connection=threadLocal.get();
        if(connection==null){
            connection=conn;
            threadLocal.set(connection);

        }
        return connection;
    }

}
