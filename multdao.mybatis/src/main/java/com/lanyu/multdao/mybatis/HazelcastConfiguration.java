package com.lanyu.multdao.mybatis;

import com.hazelcast.config.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration(value = "multdao-hazelcast")
public class HazelcastConfiguration {

  @Bean
  public Config hazelCastConfig() {
      return new Config()
              .setInstanceName("hazelcast-instance")
              .addMapConfig(
                      new MapConfig()
                              .setName("TxCache")
                              .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                              .setEvictionPolicy(EvictionPolicy.LRU)
                              .setTimeToLiveSeconds(3600))
              .addQueueConfig(
                        new QueueConfig().setName("CommitedSqlist")
              );//事务缓存一小时
  }
}