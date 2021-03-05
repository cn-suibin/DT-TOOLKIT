package test;
/*
 * create by suibin
 * API-对象内存计算工具
 * 2017-10-27
 */
public class ItemSizeTest {  
    static final Runtime runTime=Runtime.getRuntime();  
    /*
    public static void main(String[] args) throws Exception {  
        final int count = 100000;  
        Object[] us=new Object[count];  
          
        long heap1 = 0;  
          
        for (int i = -1; i < count; ++i) {  
        	Object user=null ;  
            user=new Item(Long.parseLong("234322221"), 14); 
        	//user=(Object)Factory.createObj("com.lanyu.community.test.Item");
            //user=new Item(new Long("234322221"), 14); //new 对象固定4字节 
            if (i >= 0)  
                us[i] = user;  
            else {  
                user = null;   
                heap1 = getUsedMemory();   
            }  
        }  
        long heap2 = getUsedMemory();  
        System.out.println("user大小:"+((float)heap2-heap1)/count+" bytes");  
        for (int i = 0; i < count; i++) {  
            us[i]=null;  
        }  
        runTime.gc();  
    }  
    */
    static long getUsedMemory(){  
        return runTime.totalMemory()-runTime.freeMemory();  
    }  
}  