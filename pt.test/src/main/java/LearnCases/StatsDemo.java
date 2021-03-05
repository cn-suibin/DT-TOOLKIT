package LearnCases;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多任务并行+线程池统计
 * 创建者 科帮网  https://blog.52itstyle.com
 * 创建时间    2018年4月17日
 */
public class StatsDemo {
    final static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    
    final static String startTime = sdf.format(new Date());
    
    /**
	 * IO密集型任务  = 一般为2*CPU核心数（常出现于线程中：数据库数据交互、文件上传下载、网络数据传输等等）
	 * CPU密集型任务 = 一般为CPU核心数+1（常出现于线程中：复杂算法）
	 * 混合型任务  = 视机器配置和复杂度自测而定
	 */
	private static int corePoolSize = Runtime.getRuntime().availableProcessors();
	/**
	 * public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,
	 *                           TimeUnit unit,BlockingQueue<Runnable> workQueue)
	 * corePoolSize用于指定核心线程数量
	 * maximumPoolSize指定最大线程数
	 * keepAliveTime和TimeUnit指定线程空闲后的最大存活时间
	 * workQueue则是线程池的缓冲队列,还未执行的线程会在队列中等待
	 * 监控队列长度，确保队列有界
	 * 不当的线程池大小会使得处理速度变慢，稳定性下降，并且导致内存泄露。如果配置的线程过少，则队列会持续变大，消耗过多内存。
	 * 而过多的线程又会 由于频繁的上下文切换导致整个系统的速度变缓——殊途而同归。队列的长度至关重要，它必须得是有界的，这样如果线程池不堪重负了它可以暂时拒绝掉新的请求。
	 * ExecutorService 默认的实现是一个无界的 LinkedBlockingQueue。
	 */
	private static ThreadPoolExecutor executor  = new ThreadPoolExecutor(corePoolSize, corePoolSize+1, 10l, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(1000));
	
	
///	
    
    private static int test() throws InterruptedException {
        Map<String, Integer> map = new HashMap<String, Integer>();
        ExecutorService pool = Executors.newCachedThreadPool();
        for (int i = 0; i < 8; i++) {
            pool.execute(new MyTask(map));
        }
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.DAYS);
 
        return map.get(MyTask.KEY);
    }

 
static class MyTask implements Runnable {
 
    public final Object lock = new Object();
 
    public static final String KEY = "key";
 
    private Map<String, Integer> map;
 
    public MyTask(Map<String, Integer> map) {
        this.map = map;
    }
 
    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            synchronized (lock) {
                this.addup();
            }
        }
    }
 
    private void addup() {
        if (!map.containsKey(KEY)) {
            map.put(KEY, 1);
        } else {
            map.put(KEY, map.get(KEY) + 1);
        }
    }	
}

	public static void getConcurr() throws InterruptedException {
			ConcurrentHashMap<String, AtomicInteger> map = new ConcurrentHashMap<String,AtomicInteger>();
			AtomicInteger integer = new AtomicInteger(1);//原子类合用解决线程安全
			map.put("key", integer);
			ExecutorService executorService = Executors.newFixedThreadPool(100);
			for (int i = 0; i < 1000; i++) {
			    executorService.execute(new Runnable() {
			        @Override
			        public void run() {
			            map.get("key").incrementAndGet();
			        }
			    });
			}
			Thread.sleep(3000); //模拟等待执行结束
			System.out.println("------" + map.get("key") + "------");
			executorService.shutdown();
	}

	///
    public static void main(String[] args) throws InterruptedException {
    	//getSyc();//同步
        //getFuture();//异步
    	//threadCommunication();//线程通信
    	getConcurr();
    }

    
    public static void threadCommunication() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(4);
     

        for(int i=0; i<4; i++){
        	executor.execute(new Runner(countDownLatch));
        }
        Thread.sleep(1000);
        System.out.println("好，准备开始跑了");
        synchronized (countDownLatch){
          countDownLatch.notifyAll();
        }
        countDownLatch.await();
        System.out.println("ok，已经跑完了");
      }

    
    
    public static void getSyc() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(5);
        //使用execute方法
  		executor.execute(new Stats("任务A", 1000, latch));
  		executor.execute(new Stats("任务B", 1000, latch));
  		executor.execute(new Stats("任务C", 1000, latch));
  		executor.execute(new Stats("任务D", 1000, latch));
  		executor.execute(new Stats("任务E", 1000, latch));
        latch.await();// 等待所有人任务结束
        System.out.println("所有的统计任务执行完成:" + sdf.format(new Date()));
    }
    
    public static void getFuture() {
    	List<Future<String>> resultList = new ArrayList<Future<String>>(); 
        //使用submit提交异步任务，并且获取返回值为future
    	resultList.add((Future<String>) executor.submit(new StatsF("任务A", 1000)));
    	resultList.add((Future<String>) executor.submit(new StatsF("任务B", 1000)));
    	resultList.add((Future<String>) executor.submit(new StatsF("任务C", 1000)));
    	resultList.add((Future<String>) executor.submit(new StatsF("任务D", 1000)));
    	resultList.add((Future<String>) executor.submit(new StatsF("任务E", 1000)));
  	   //遍历任务的结果
        for (Future<String> fs : resultList) { 
            try { 
                System.out.println(fs.get());//打印各个线任务执行的结果，调用future.get() 阻塞主线程，获取异步任务的返回结果
            } catch (InterruptedException e) { 
                e.printStackTrace(); 
            } catch (ExecutionException e) { 
                e.printStackTrace(); 
            } finally { 
                //启动一次顺序关闭，执行以前提交的任务，但不接受新任务。如果已经关闭，则调用没有其他作用。
            	executor.shutdown(); 
            } 
        } 
        System.out.println("所有的统计任务执行完成:" + sdf.format(new Date()));
    }
    
    static  class Runner implements Runnable {
    	 
    	  CountDownLatch countDownLatch;
    	 
    	  public Runner(CountDownLatch countDownLatch) {
    	    this.countDownLatch = countDownLatch;
    	  }
    	 
    	  @Override
    	  public void run() {
    	 
    	    System.out.println(Thread.currentThread().getName()+"准备开始跑步了");
    	    try {
    	      synchronized (countDownLatch) {
    	        countDownLatch.wait();
    	        System.out.println(Thread.currentThread().getName() + "开始跑");
    	        countDownLatch.countDown();
    	      }
    	    } catch (InterruptedException e) {
    	      System.out.println("等待报错了！"+e);
    	    }
    	 
    	  }
    	}
    
    static class StatsF implements Callable<String>  {
        String statsName;
        int runTime;

        public StatsF(String statsName, int runTime) {
            this.statsName = statsName;
            this.runTime = runTime;
        }

        public String call() {
            try {
                System.out.println(statsName+ " do stats begin at "+ startTime);
                //模拟任务执行时间
                Thread.sleep(runTime);
                System.out.println(statsName + " do stats complete at "+ sdf.format(new Date()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return call();
        }
    }
  //注意这里是否添加@Scope("prototype")注解
    @Component
    @Scope("prototype")//注解把TaskThread设置为多例模式,线程安全
    static class Stats implements Runnable  {
        String statsName;
        int runTime;
        CountDownLatch latch;
        //ThreadLocal  对象，单例模式下可以保证成员变量的线程安全和独立性。
        public ThreadLocal<Integer> valueLocal =  new ThreadLocal < Integer > () {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };
        public Stats(String statsName, int runTime, CountDownLatch latch) {
            this.statsName = statsName;
            this.runTime = runTime;
            this.latch = latch;
        }

        public void run() {
            try {
                System.out.println(statsName+ " do stats begin at "+ startTime);
                //模拟任务执行时间
                Thread.sleep(runTime);
                System.out.println(statsName + " do stats complete at "+ sdf.format(new Date()));
                latch.countDown();//单次任务结束，计数器减一
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}