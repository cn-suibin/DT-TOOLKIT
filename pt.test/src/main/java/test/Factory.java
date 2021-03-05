package test;
public class Factory {
    
    public static Object createObj(String className) throws Exception{
            return (Object)Class.forName(className).newInstance();
    }
}