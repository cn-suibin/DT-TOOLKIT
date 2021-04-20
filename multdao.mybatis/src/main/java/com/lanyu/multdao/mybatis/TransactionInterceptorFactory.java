package com.lanyu.multdao.mybatis;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.druid.proxy.jdbc.TransactionInfo;
import com.google.common.collect.Lists;
import com.hazelcast.core.Hazelcast;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author  suibin
 * 基于mybatis 3.5.6版本
 * 关于CUD的拦截器。
 * 版本1.0
 */
@Intercepts(
        {
                @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
                //@Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
                //@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
                //@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                //@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        }
)
public class TransactionInterceptorFactory implements Interceptor
{
   // private ThreadLocal<List> firstCache=new ThreadLocal<List>();
   // private static final ConcurrentHashMap<Long, List> cacheList = new ConcurrentHashMap<>();
    List sublist=Collections.synchronizedList(Lists.newArrayList());
    @Autowired
    DataSourceTransactionManager dt;
    @SuppressWarnings("unused")
    private Properties properties;
    private static final Logger logger = LoggerFactory.getLogger(TransactionInterceptorFactory.class);

    /*
    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        System.out.println("开始拦截.....");
        Object proceed = invocation.proceed();
        System.out.println("结束拦截.....");
        return proceed;
    }

     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //缓存没有初始化，则直接返回。Hazelcast只有在应用启动之后生效，而拦截器在应用启动前就执行拦截。
        if(Hazelcast.getAllHazelcastInstances().size()==0){
            return invocation.proceed();
        }
        Object o1=invocation.getMethod();
        Object o2=invocation.proceed();
        Object target = invocation.getTarget();
        Connection connection = (Connection)invocation.getArgs()[0];

        long comein_thread_id=connection.hashCode();
        //connection=ConnectionUtils.getInstance().getCurrentThreadConn(connection);

        //int c2=connection.hashCode();
        //DatabaseMetaData ddf= connection.getMetaData();
        //String d=connection.getCatalog();
        //int a=connection.getHoldability();
        //connection.ge
        Object[] args = invocation.getArgs();
        //DruidPooledConnection dpc= (DruidPooledConnection) args[0];
        //Thread comein_thread= dpc.getOwnerThread();
        //long comein_thread_id=comein_thread.getId();
        //TransactionInfo tranInfo=dpc.getTransactionInfo();
        //String txId="";//事务ID，第一次执行SQL后才生成，不好控制所以废弃用线程ID替代。
        //if(tranInfo!=null){
        //    txId=tranInfo.getId()+"";
       // }
        //txId=connid+"";
        if (target instanceof Executor) {
            final Executor executor = (Executor) target;
            Object parameter = args[1];
            boolean isUpdate = args.length == 2;
            MappedStatement ms = (MappedStatement) args[0];
            if (!isUpdate && ms.getSqlCommandType() == SqlCommandType.SELECT) {
                RowBounds rowBounds = (RowBounds) args[2];
                ResultHandler resultHandler = (ResultHandler) args[3];
                BoundSql boundSql;
                if (args.length == 4) {
                    boundSql = ms.getBoundSql(parameter);
                } else {
                    // 几乎不可能走进这里面,除非使用Executor的代理对象调用query[args[6]]
                    boundSql = (BoundSql) args[5];
                }

                CacheKey cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
                return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
            } else if (isUpdate) {

            }
        } else {
            //mapper方式走如下代码
            // StatementHandler


            ////////////////////////////
            StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
            MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
            // 如果是insert操作， 或者 @SqlParser(filter = true) 跳过该方法解析 ， 不进行验证
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            //String idd=mappedStatement.getId();
            // 针对定义了rowBounds，做为mapper接口方法的参数
            BoundSql boundSql1 = (BoundSql) metaObject.getValue("delegate.boundSql");
            Object paramObj = boundSql1.getParameterObject();

            final RoutingStatementHandler sh = (RoutingStatementHandler)target;

            // rsh.getParameterHandler().getParameterObject()
            BoundSql boundSql = sh.getBoundSql();
            String sql=boundSql.getSql().replaceAll("[\\s]+", " ");
            Object parameterObject= boundSql.getParameterObject();
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

            if (parameterMappings != null) {
                for (int i = 0; i < parameterMappings.size(); i++) {
                    //获取某个参数
                    ParameterMapping parameterMapping = parameterMappings.get(i);
                    if (parameterMapping.getMode() != ParameterMode.OUT) {
                        Object value;
                        //参数的名字，属性
                        String propertyName = parameterMapping.getProperty();

                        //先从附加的
                        if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
                            value = boundSql.getAdditionalParameter(propertyName);
                            int x=0;
                        } else if (parameterObject == null) {
                            value = null;
                        //} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                            //typeHandlerRegistry注册了某个类的处理
                        //    value = parameterObject;
                        } else {

                            //默认的MetaObject 的处理，根据参数获取值,这个将和boundSql一起解释
                            MetaObject metaObject1 = mappedStatement.getConfiguration().newMetaObject(parameterObject);
                            value = metaObject1.getValue(propertyName);
                            int d22=1;
                        }
                        sql = sql.replaceFirst("\\?", getParameterValue(value));
                    }
                }
            }

            //boolean s= TransactionSynchronizationManager.isActualTransactionActive();
            //List<TransactionSynchronization>  fg=TransactionSynchronizationManager.getSynchronizations();

            //TransactionStatus ts= TransactionAspectSupport.currentTransactionStatus();
            //boolean s=ts.isCompleted();

            if (SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType()) ) {
                //查询的时候

            }else{
                //获取是否@Transactional注解，当没有加注解时isFromTrans返回false
                DataSource dataSource=dt.getDataSource();
                boolean isFromTrans= DataSourceUtils.isConnectionTransactional(connection,dataSource);
                if(!isFromTrans){//如果不是事务
                    Hazelcast.getHazelcastInstanceByName("hazelcast-instance").getQueue("CommitedSqlist").put(sql);
                    return invocation.proceed();
                }


                //System.out.println(sql);
                Object value = (Object) Hazelcast.getHazelcastInstanceByName("hazelcast-instance").getMap("TxCache").get("syn_sqls"+comein_thread_id);
                //if(txId!=null&&!txId.equals("")){
                    if(value!=null){
                        sublist= (List) value;
                    }else{
                        //不存在该线程时
                        sublist=Collections.synchronizedList(Lists.newArrayList());
                        //cacheList.clear();//多线程情况下不能清空会清除别人的。
                    }

                    sublist.add(sql);
                    //保证每条SQL的插入顺序
                    Hazelcast.getHazelcastInstanceByName("hazelcast-instance").getMap("TxCache").put("syn_sqls"+comein_thread_id, sublist);

               // }

                //Object testv = (Object) Hazelcast.getHazelcastInstanceByName("hazelcast-instance").getMap("TxCache").get("syn_sqls"+comein_thread_id);
                int sdd=1;
            }



        }
        return invocation.proceed();

    }



    public static String getSql(Configuration configuration, BoundSql boundSql, String sqlId, long time) {
        String sql = showSql(configuration, boundSql);
        StringBuilder str = new StringBuilder(100);
        str.append("【sqlId】").append(sqlId);
        str.append("【SQL耗时-").append(time).append("-毫秒】");
        str.append("【SQL】").append(sql);
        //logger.debug(SQLFormatter.format(str.toString()));
        logger.debug(str.toString());
        return str.toString();
    }

    private static String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
            value = value.replaceAll("\\\\", "\\\\\\\\");
            value = value.replaceAll("\\$", "\\\\\\$");
        } else if (obj instanceof Date) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime((Date) obj);
            int z=calendar.get(Calendar.MILLISECOND);
            if(z>=500) {
                calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND)+1);
            }

            //DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.CHINA);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //df.setTimeZone(tz);

            value = "'" + format.format(calendar.getTime()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }

        }
        return value;
    }

    public static String showSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));

            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    }
                }
            }
        }
        return sql;
    }

    @Override
    public Object plugin(Object target)
    {
        //System.out.println("生成代理对象....");
        //return Plugin.wrap(target, this);
        // 当目标类是StatementHandler类型时，才包装目标类，否者直接返回目标本身,减少目标被代理的次数
        return (target instanceof RoutingStatementHandler)? Plugin.wrap(target,this):target;
    }
    //实现插件参数传递
    @Override
    public void setProperties(Properties properties)
    {
        //开启一个线程消费
        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
        service.scheduleAtFixedRate(new Sender(service), 1, 1, TimeUnit.SECONDS);
        //System.out.println(properties.get("who"));
        this.properties = properties;
    }

}