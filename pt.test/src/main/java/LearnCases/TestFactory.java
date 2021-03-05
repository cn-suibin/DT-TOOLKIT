package LearnCases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TestFactory {
    private static final Logger LOG = LoggerFactory.getLogger(TestFactory.class);

    public static final String ZOOKEEPER_WATCH_MANAGER_NAME = "pt.mult.dao.impl.TestFactory";

    public static TestFactory createTest() throws IOException {
        String watchManagerName = System.getProperty(ZOOKEEPER_WATCH_MANAGER_NAME);
        if (watchManagerName == null) {
            watchManagerName = TestFactory.class.getName();
        }
        try {
        	TestFactory watchManager = (TestFactory) Class.forName(watchManagerName).getConstructor().newInstance();
            LOG.info("Using {} as watch manager", watchManagerName);
            return watchManager;
        } catch (Exception e) {
            IOException ioe = new IOException("Couldn't instantiate " + watchManagerName, e);
            throw ioe;
        }
    }
    public void getT() {
    	System.out.println("-------------------");
    }
    public static void main(String args[]) {
    	try {
			TestFactory a=TestFactory.createTest();
			a.getT();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
