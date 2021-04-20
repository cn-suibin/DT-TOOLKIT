/**
 *    Copyright 2009-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor.statement;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.lanyu.multdao.mybatis.MongoUtil;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * @author  suibin
 * 基于mybatis 3.5.6版本
 * 实现了读写分离，读从mongodb，写走数据库。mongodb读不存在时走数据库。
 * 版本1.0
 */
public class PreparedStatementHandler extends BaseStatementHandler {

  public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
  }
  //insert,update,delete走这里，这里是写数据库的操作，如果在这里双写mongodb会影响性能。
  @Override
  public int update(Statement statement) throws SQLException {
    PreparedStatement ps = (PreparedStatement) statement;
    ps.execute();
    int rows = ps.getUpdateCount();
    Object parameterObject = boundSql.getParameterObject();
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
    keyGenerator.processAfter(executor, mappedStatement, ps, parameterObject);
    return rows;
  }

  @Override
  public void batch(Statement statement) throws SQLException {
    PreparedStatement ps = (PreparedStatement) statement;
    ps.addBatch();
  }
  private Statement getMongoStatement(String mongoSql){
    //从mongod
    Statement stmt = null;
    if(mongoSql!=null && !mongoSql.equals("")) {


      try {

        ResultSet rst = null;
        Connection connectionMongo = MongoUtil.getConnection();
        stmt = connectionMongo.createStatement();
        //sql="select * from ucenter_userinfo where username = '2' and status = 0";
        rst = stmt.executeQuery(mongoSql);

      } catch (ClassNotFoundException e) {

      } catch (SQLException e) {
        //mongodb查询不到,返回
        //System.out.println("mongodb未查到数据，返回原查询：" + e);
        stmt = null;
        //e.printStackTrace();
      }
    }
    return stmt;
  }

  private static String getParameterValue(Object obj) {
    String value = null;
    if (obj instanceof String) {
      value = "'" + obj.toString() + "'";
      value = value.replaceAll("\\\\", "\\\\\\\\");
      value = value.replaceAll("\\$", "\\\\\\$");
    } else if (obj instanceof java.util.Date) {

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
  //select,create走这里，后续沿用mybatis的逻辑，比如查询缓存等不受影响。
  @Override
  public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
    //PreparedStatement ps = (PreparedStatement) statement;
    //ps.execute();
    //return resultSetHandler.handleResultSets(ps);
    //mogodb的SQL查询逻辑
    boolean isCURD=true;
    Object parameterObject = parameterHandler.getParameterObject();
    String sql=boundSql.getSql().replaceAll("[\\s]+", " ");
    parameterObject= boundSql.getParameterObject();
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    for (int i = 0; i < parameterMappings.size(); i++) {
      ParameterMapping parameterMapping = parameterMappings.get(i);
      String propertyName = parameterMapping.getProperty();
      if(boundSql.hasAdditionalParameter(propertyName)){
          //这个是create等语句，忽略
        isCURD=false;
      }else{
        //这个是select语句，处理
        //参数的名字，属性
        MetaObject metaObject1 =configuration.newMetaObject(parameterObject);
        Object value = metaObject1.getValue(propertyName);
        sql = sql.replaceFirst("\\?", getParameterValue(value));
      }

    }
    //System.out.println(sql);
    if(isCURD){
      Statement statement1=getMongoStatement(sql);
      if(statement1==null){
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        return resultSetHandler.handleResultSets(ps);
      }
      return resultSetHandler.handleResultSets(statement1);
    }else{
      //curd之外的语句mongod不支持，交给关系数据库
      PreparedStatement ps = (PreparedStatement) statement;
      ps.execute();
      return resultSetHandler.handleResultSets(ps);
    }


  }

  @Override
  public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {
    PreparedStatement ps = (PreparedStatement) statement;
    ps.execute();
    return resultSetHandler.handleCursorResultSets(ps);
  }

  @Override
  protected Statement instantiateStatement(Connection connection) throws SQLException {
    String sql = boundSql.getSql();
    if (mappedStatement.getKeyGenerator() instanceof Jdbc3KeyGenerator) {
      String[] keyColumnNames = mappedStatement.getKeyColumns();
      if (keyColumnNames == null) {
        return connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
      } else {
        return connection.prepareStatement(sql, keyColumnNames);
      }
    } else if (mappedStatement.getResultSetType() == ResultSetType.DEFAULT) {
      return connection.prepareStatement(sql);
    } else {
      return connection.prepareStatement(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
    }
  }

  @Override
  public void parameterize(Statement statement) throws SQLException {
    parameterHandler.setParameters((PreparedStatement) statement);
  }

}
