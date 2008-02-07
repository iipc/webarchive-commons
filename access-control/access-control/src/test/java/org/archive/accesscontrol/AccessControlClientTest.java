package org.archive.accesscontrol;

import java.util.Date;

import org.archive.accesscontrol.AccessControlClient;
import org.archive.accesscontrol.model.HttpRuleDao;

import junit.framework.TestCase;

public class AccessControlClientTest extends TestCase {
    public static final String ORACLE_URL = "http://localhost:8080/exclusions-oracle-0.0.1-SNAPSHOT/";
    private AccessControlClient client;
        
    protected void setUp() throws Exception {
        super.setUp();
        client = new AccessControlClient(new HttpRuleDao(ORACLE_URL));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        client = null;
    }
    
    public void testBasicOkToShow() throws Exception {
        //System.out.println(client.getPolicy("http://www.archive.org/secret/page.html", new Date(), new Date()));
    }
    
}
