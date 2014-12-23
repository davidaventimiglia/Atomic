package org.neptunestation.atomic.standalone;

import java.io.*;
import java.util.*;
import org.apache.catalina.*;
import org.apache.catalina.deploy.*;
import org.apache.catalina.startup.*;
import org.neptunestation.atomic.core.*;

public class Atomic {
    public static void main (String[] args) throws Exception {
	try {
	    String jdbcDriver = System.getProperty("jdbc-driver");
	    String jdbcUrl = System.getProperty("jdbc-url");
	    String httpPort = System.getProperty("http-port");
	    String contextPath = System.getProperty("context-path");
	    System.out.println("jdbc-driver = " + jdbcDriver);
	    System.out.println("jdbc-url = " + jdbcUrl);
	    System.out.println("http-port = " + httpPort);
	    System.out.println("context-path = " + contextPath);
	    System.out.println(new Scanner(ClassLoader.getSystemResourceAsStream("atomic_splash.txt")).useDelimiter("\\Z").next().replace("$PORT$", httpPort));
	    Tomcat tomcat = new Tomcat();
            tomcat.enableNaming();
	    tomcat.setPort(Integer.parseInt(httpPort));
	    Context ctx = tomcat.addContext(contextPath, new File(".").getAbsolutePath());
            ContextResource resource = new ContextResource();
            resource.setName("jdbc/AtomicDB");
            resource.setAuth("Container");
            resource.setType("javax.sql.DataSource");
            resource.setScope("Sharable");
            resource.setProperty("driverClassName", jdbcDriver);
            resource.setProperty("url", jdbcUrl);
            resource.setProperty("removeAbandoned", "true");
            resource.setProperty("removeAbandonedTimeout", "10");
            resource.setProperty("logAbandoned=", "true");
            resource.setProperty("username", "");
            resource.setProperty("password", "");
            resource.setProperty("maxActive", "20");
            resource.setProperty("maxIdle", "20");
            resource.setProperty("initialSize", "0");
            resource.setProperty("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
            resource.setProperty("alternateUsernameAllowed", "true");
            resource.setProperty("maxWait=", "-1");
            ctx.getNamingResources().addResource(resource);
	    Wrapper w = Tomcat.addServlet(ctx, "atomic", new AtomicServlet());
	    ctx.addServletMapping("/*", "atomic");
	    tomcat.start();
	    tomcat.getServer().await();}
	catch (Exception t) {
	    t.printStackTrace(System.err);
	    System.out.println("Atomic has encountered a fatal error and is shutting down.");
	    System.exit(1);}}}
