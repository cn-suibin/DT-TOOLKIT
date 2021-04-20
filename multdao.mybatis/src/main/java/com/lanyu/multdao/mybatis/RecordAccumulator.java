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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class acts as a queue that accumulates records into {@link org.apache.kafka.common.record.MemoryRecords}
 * instances to be sent to the server.
 * <p>
 * The accumulator uses a bounded amount of memory and append calls will block when that memory is exhausted, unless
 * this behavior is explicitly disabled.
 */
public final class RecordAccumulator {

    private static final Logger log = LoggerFactory.getLogger(RecordAccumulator.class);

    private volatile boolean closed;
    private final AtomicInteger flushesInProgress;
    private final AtomicInteger appendsInProgress;
    //private final int batchSize;
    //private final CompressionType compression;
    //private final long lingerMs;
    //private final long retryBackoffMs;
    //private final BufferPool free;
    //private final Time time;
    //TopicPartition 分区 -》  Deque<RecordBatch> 队列
    private final ConcurrentMap<String, Deque<List>> batches;
    private final IncompleteRecordBatches incomplete;
    // The following variables are only accessed by the sender thread, so we don't need to protect them.
    private final Set<Long> muted;
    private int drainIndex;

    /**
     * Create a new record accumulator
     * 
     * @param batchSize The size to use when allocating {@link org.apache.kafka.common.record.MemoryRecords} instances
     * @param totalSize The maximum memory the record accumulator can use.
     * @param compression The compression codec for the records
     * @param lingerMs An artificial delay time to add before declaring a records instance that isn't full ready for
     *        sending. This allows time for more records to arrive. Setting a non-zero lingerMs will trade off some
     *        latency for potentially better throughput due to more batching (and hence fewer, larger requests).
     * @param retryBackoffMs An artificial delay time to retry the produce request upon receiving an error. This avoids
     *        exhausting all retries in a short period of time.
     * @param metrics The metrics
     * @param time The time instance to use
     */
    public RecordAccumulator(//int batchSize,
                             //long totalSize,
                             //CompressionType compression,
                             //long lingerMs,
                             //long retryBackoffMs,
                             //Time time
    )  {
        this.drainIndex = 0;
        this.closed = false;
        this.flushesInProgress = new AtomicInteger(0);
        this.appendsInProgress = new AtomicInteger(0);
        //this.batchSize = batchSize;
        //this.compression = compression;
        //this.lingerMs = lingerMs;
        //this.retryBackoffMs = retryBackoffMs;
        //CopyOnWriteMap的这样的一个数据类型。
        //这个数据结构在jdk里面是没有的，是kafka自己设计的。
        //这个数据结构设计得很好，因为有了这个数据结构
        //整体的提升了封装批次的这个流程的性能！！

        //JDK  juc包下面：CopyOnWriteArrayList

        //this.batches = new CopyOnWriteMap<>();//这个是线程安全的无序的ConcurrentMap，比这个是无序的ConcurrentHashMap的效率高。都能防止第一次覆盖的情况。
        //都能预防：由于put()和putVal()代码没有同步，插入一个value的时候会进行判空处理，在多线程的时候，如果正好2个线程都检查到对应位置是空的，都会插进去的话，先插进去的就会被后插进去的节点覆盖，而不是都挂在后面。就会出现数据错误，导致线程不安全

        this.batches = new CopyOnWriteMap<>();
        String metricGrpName = "producer-metrics";
        //this.free = new BufferPool(totalSize, batchSize, time, metricGrpName);
        this.incomplete = new IncompleteRecordBatches();
        this.muted = new HashSet<>();
        //this.time = time;
        //registerMetrics(metrics, metricGrpName);

    }
/* 度量初始化相关
    private void registerMetrics(Metrics metrics, String metricGrpName) {
        MetricName metricName = metrics.metricName("waiting-threads", metricGrpName, "The number of user threads blocked waiting for buffer memory to enqueue their records");
        Measurable waitingThreads = new Measurable() {
            public double measure(MetricConfig config, long now) {
                return free.queued();
            }
        };
        metrics.addMetric(metricName, waitingThreads);

        metricName = metrics.metricName("buffer-total-bytes", metricGrpName, "The maximum amount of buffer memory the client can use (whether or not it is currently used).");
        Measurable totalBytes = new Measurable() {
            public double measure(MetricConfig config, long now) {
                return free.totalMemory();
            }
        };
        metrics.addMetric(metricName, totalBytes);

        metricName = metrics.metricName("buffer-available-bytes", metricGrpName, "The total amount of buffer memory that is not being used (either unallocated or in the free list).");
        Measurable availableBytes = new Measurable() {
            public double measure(MetricConfig config, long now) {
                return free.availableMemory();
            }
        };
        metrics.addMetric(metricName, availableBytes);

        Sensor bufferExhaustedRecordSensor = metrics.sensor("buffer-exhausted-records");
        metricName = metrics.metricName("buffer-exhausted-rate", metricGrpName, "The average per-second number of record sends that are dropped due to buffer exhaustion");
        bufferExhaustedRecordSensor.add(metricName, new Rate());
    }
*/
    /**
     * Add a record to the accumulator, return the append result
     * <p>
     * The append result will contain the future metadata, and flag for whether the appended batch is full or a new batch is created
     * <p>
     *
     * @param tp The topic/partition to which this record is being sent
     * @param timestamp The timestamp of the record
     * @param key The key for the record
     * @param value The value for the record
     * @param callback The user-supplied callback to execute when the request is complete
     * @param maxTimeToBlock The maximum time in milliseconds to block for buffer memory to be available
     */
    public  boolean append(String tp,
                                     //long timestamp,
                                     List msgs
                                     //Callback callback,
                                     //long maxTimeToBlock
    ) {

        appendsInProgress.incrementAndGet();
        try {

            //如果队列时空，则创建一个队列
            Deque<List> dq = getOrCreateDeque(tp);
            /**
             * 假设我们现在有线程一，线程二，线程三
             *
             */
            synchronized (dq) {
                //第一步，线程一进来了
                //线程二 被锁
                //线程三 被锁

                //第二步，线程一结束了
                //线程二，解锁进来了
                //线程三，被锁
                if (closed)
                    throw new IllegalStateException("Cannot send after the producer is closed.");

                boolean appendResult = tryAppend( msgs, dq);
                //第一步，线程一 进来的时候，第一次进来的时候查询了队列不存在数据，返回false不添加数据

                //第二步,线程二 进来的时候，队列中查询到数据，因此添加一条，线程二返回结束
                //第三步,线程三 进来的时候，队列中查询到数据，因此添加一条，线程三返回结束。。。。。其他线程一直追加数据并返回结束。
                if(appendResult){
                    return appendResult;
                }
                dq.addLast(msgs);
                return true;
            }//释放锁

        } finally {
            appendsInProgress.decrementAndGet();
        }
    }

    /**
     * If `RecordBatch.tryAppend` fails (i.e. the record batch is full), close its memory records to release temporary
     * resources (like compression streams buffers).
     */
    private boolean tryAppend(//long timestamp,
                              List msgs,
                              //Callback callback,
                              Deque<List> deque) {
        //首先要获取到队列里面一个批次
        List last = deque.peekLast();
        //第一次进来是没有批次的，所以last肯定为null

        //线程二进来的时候，这个last不为空
        if (last != null) {
            deque.add(msgs);
            return true;
        }
        //返回结果就是一个null值
        return false;
    }


    /**
     * Re-enqueue the given record batch in the accumulator to retry
     */
    public void reenqueue(List batch, long now) {
        //重试次数 累加
        //batch.attempts++;
        //上一次重试的时间
        //batch.lastAttemptMs = now;
        //batch.lastAppendTime = now;
        //batch.setRetry();
        //Deque<List> deque = getOrCreateDeque(batch.topicPartition);
        //synchronized (deque) {
            //重新放入到队列里面
            //放入到队头
        //    deque.addFirst(batch);
        //}
    }


    /**
     * @return Whether there is any unsent record in the accumulator.
     */
    public boolean hasUnsent() {
        for (Map.Entry<String, Deque<List>> entry : this.batches.entrySet()) {
            Deque<List> deque = entry.getValue();
            synchronized (deque) {
                if (!deque.isEmpty())
                    return true;
            }
        }
        return false;
    }


    public Deque<List> getDeque(String tp) {
        return batches.get(tp);
    }

    /**
     * Get the deque for the given topic-partition, creating it if necessary.
     */
    private Deque<List> getOrCreateDeque(String tp) {

        /**
         * CopyonWriteMap:
         *      get
         *      put
         *
         */
        //直接从batches里面获取当前分区对应的存储队列

        Deque<List> d = this.batches.get(tp);
        //我们现在用到是场景驱动的方式，代码第一次执行到这儿的死活
        //是获取不到队列的，也就是说d 这个变量的值为null
        if (d != null)
            return d;
        //代码继续执行，创建出来一个新的空队列，
        d = new ArrayDeque<>();
        //把这个空的队列存入batches 这个数据结构里面
        Deque<List> previous = this.batches.putIfAbsent(tp, d);
        if (previous == null)
            return d;
        else
            //直接返回新的结果
            return previous;
    }

    /**
     * Deallocate the record batch
     */
    public void deallocate(List batch) {
        //从某个数据结构里面移除 已经成功处理的批次
        incomplete.remove(batch);
        //释放内存，回收内存
        //free.deallocate(batch.records.buffer(), batch.records.initialCapacity());
    }
    
    /**
     * Are there any threads currently waiting on a flush?
     *
     * package private for test
     */
    boolean flushInProgress() {
        return flushesInProgress.get() > 0;
    }

    /* Visible for testing */
    public Map<String, Deque<List>> batches() {
        return Collections.unmodifiableMap(batches);
    }
    
    /**
     * Initiate the flushing of data from the accumulator...this makes all requests immediately ready
     */
    public void beginFlush() {
        this.flushesInProgress.getAndIncrement();
    }

    /**
     * Are there any threads currently appending messages?
     */
    private boolean appendsInProgress() {
        return appendsInProgress.get() > 0;
    }



    /**
     * This function is only called when sender is closed forcefully. It will fail all the
     * incomplete batches and return.
     */

    /**
     * Go through incomplete batches and abort them.
     */


    public void mutePartition(Long tp) {
        muted.add(tp);
    }

    public void unmutePartition(Long tp) {
        muted.remove(tp);
    }

    /**
     * Close this accumulator and force all the record buffers to be drained
     */
    public void close() {
        this.closed = true;
    }




    
    /*
     * A threadsafe helper class to hold RecordBatches that haven't been ack'd yet
     */
    private final static class IncompleteRecordBatches {
        private final Set<List> incomplete;

        public IncompleteRecordBatches() {
            this.incomplete = new HashSet<List>();
        }
        
        public void add(List batch) {
            synchronized (incomplete) {
                this.incomplete.add(batch);
            }
        }
        
        public void remove(List batch) {
            synchronized (incomplete) {
                boolean removed = this.incomplete.remove(batch);
                if (!removed)
                    throw new IllegalStateException("Remove from the incomplete set failed. This should be impossible.");
            }
        }
        
        public Iterable<List> all() {
            synchronized (incomplete) {
                return new ArrayList<>(this.incomplete);
            }
        }
    }

}
