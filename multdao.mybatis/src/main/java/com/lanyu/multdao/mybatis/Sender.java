/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.lanyu.multdao.mybatis;


import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The background thread that handles the sending of produce requests to the Kafka cluster. This thread makes metadata
 * requests to renew its view of the cluster and then sends produce requests to the appropriate nodes.
 */
public class Sender implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Sender.class);
    //引入锁
    private ReentrantLock  reentrantLock = new ReentrantLock();
    ScheduledExecutorService service;
    Sender(ScheduledExecutorService service){
        this.service=service;
    }
    Statement stmt = null;
    ResultSet rst = null;
    private	static Connection con = null;
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        //If instance has not been created yet, create it
        if (con == null) {
            synchronized (Sender.class) {
                if (con == null) {
                    Class.forName("mongodb.jdbc.MongoDriver");
                    String url = "jdbc:mongo://127.0.0.1:27017/bcactc_20200623?debug=true&rebuildschema=true";

                    con = DriverManager.getConnection(url, "root", "123456");
                }
            }
        }
        return con;
    }

   // public Condition condition = lock.newCondition();
    /**
     * The main run loop for the sender thread
     */
    public void run() {

        if(Hazelcast.getAllHazelcastInstances().size()>0){

            //由缓存里批量获取发送给mongodb,
            //优点一，事务直接到内存里不阻塞，极快
            //优点二，如果在拦截器层做入库，事务某种原因提交失败，失败粒度是事务本身，定时器拉取则失败粒度是事务列表。
            //事务粒度补偿的情况下，补偿后事务和事务之间的顺序被打乱，因为事务出问题了，不能阻止后续事务的进行，并发事务的情况下也不能阻止同时的事务执行。
            //事务列表粒度补偿的情况下，异常情况则中断拉取是数据，等恢复后再执行拉取动作，不会改变事务的顺序。
            //缺点：会有一点延迟，但保证了事务的顺序，保证了数据的一致性。
            //System.out.println(mapp2.toArray().length);
            //对比:等长消费？全部消费？Hazelcast生产-消费者模式最好。https://perkins4j2.github.io/posts/48149/
            //Hazelcast的消费者队列
            try{
                IQueue<List> queue= Hazelcast.getHazelcastInstanceByName("hazelcast-instance").getQueue("CommitedSqlist");
                //监听生产者
                while (true) {
                    //防止顺序不一致，加锁
                    if (reentrantLock.tryLock()) {
                        List sqls = null;
                        try {
                            Object getQ= queue.take();
                            if(getQ instanceof List){
                                sqls = (List) getQ;
                                log.info("=======正在同步sql事务，事务内容：" + sqls);
                                for (Object sql : sqls) {
                                    stmt = getConnection().createStatement();
                                    stmt.execute((String) sql);
                                }
                                log.info("=======同步sql事务成功，事务内容：" + sqls);
                            }else if(getQ instanceof String){
                                stmt = getConnection().createStatement();
                                stmt.execute((String) getQ);
                                log.info("=======同步sql成功：" + getQ);
                            }

                        } catch (SQLException | ClassNotFoundException | InterruptedException e) {
                            log.error("事务同步失败:"+sqls+"详情:"+e);
                        } finally {
                            // 释放锁
                            reentrantLock.unlock();
                        }
                    }
                }
            }catch (Exception e){
                log.error("创建监听失败:"+e);
            }
            log.info("=======sql同步监听器已启动，listen...========");
            this.service.shutdown();
        }



    }

}
