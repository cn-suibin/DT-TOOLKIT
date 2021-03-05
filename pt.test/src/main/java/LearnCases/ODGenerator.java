package LearnCases;

import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.util.StopWatch;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单编号策略
 *
 * @project common-utils
 * @fileName ODGenerator.java
 * @Description
 * @author
 * @date
 * @version 1.0.0
 */
public abstract class ODGenerator {
    private static final FastDateFormat pattern = FastDateFormat.getInstance("yyyyMMddHHmmss");
    private static final AtomicInteger atomicInteger = new AtomicInteger(1);
    private static ThreadLocal<StringBuilder> threadLocal = new ThreadLocal<StringBuilder>();

    /**
     * 长码生成策略
     *  20201116114351753590384993
     * @param lock uuid
     * @return
     */
    public static String getC(String lock) {
        if (Objects.isNull(threadLocal.get())) {
            lock = Objects.isNull(lock) ? UUID.randomUUID().toString() : lock;
            StringBuilder builder = new StringBuilder(pattern.format(Instant.now().toEpochMilli()));// 取系统当前时间作为订单号前半部分
            builder.append(Math.abs(lock.hashCode()));// HASH-CODE
            builder.append(atomicInteger.getAndIncrement());// 自增顺序
            threadLocal.set(builder);
        }
        return threadLocal.get().toString();
    }

    /**
     * 短码生成策略
     * 1307891882965
     * @param lock
     * @return
     */
    public static String getD(String lock) {
        if (Objects.isNull(threadLocal.get())) {
            lock = Objects.isNull(lock) ? UUID.randomUUID().toString() : lock;
            StringBuilder builder = new StringBuilder(ThreadLocalRandom.current().nextInt(0, 999)); // 随机数
            builder.append(Math.abs(lock.hashCode()));// HASH-CODE
            builder.append(atomicInteger.getAndIncrement());// 自增顺序
            threadLocal.set(builder);
        }
        return threadLocal.get().toString();
    }

    /**
     * 1000个线程并发测试
     *
     * @param args
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Set<String> set = new HashSet<String>();
        FutureTask<String> task = null;
        StopWatch watchTime = new StopWatch();
        watchTime.start();
        for (int i = 0; i < 1000; i++) {
            Callable<String> callable = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    // System.out.println("当前线程:>>>>> ".concat(Thread.currentThread().getId()+""));
                    //return getC(null);
                    return getD(null);
                }
            };
            task = new FutureTask<String>(callable);
            new Thread(task).start();
            set.add(task.get());
            System.out.println(task.get());
        }
        watchTime.stop();
        System.out.println(watchTime.getTotalTimeMillis());
        System.out.println(set.size());
    }
}