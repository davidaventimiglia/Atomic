package org.neptunestation.atomic.standalone;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.catalina.*;
import org.apache.catalina.deploy.*;
import org.apache.catalina.startup.*;
import org.neptunestation.atomic.core.*;
import org.neptunestation.filterpack.filters.*;

public class Atomic {
    public static void main (String[] args) throws Exception {
	try {
	    LogManager.getLogManager().readConfiguration(ClassLoader.getSystemResourceAsStream("logging.properties"));
	    
	    String jdbcDriver = System.getProperty("jdbc-driver");
	    String jdbcUrl = System.getProperty("jdbc-url");
	    String httpPort = System.getProperty("http-port");
	    String contextPath = System.getProperty("context-path");

	    System.out.println(new Scanner(ClassLoader.getSystemResourceAsStream("atomic_splash.txt")).useDelimiter("\\Z").next().replace("$PORT$", httpPort));
	    System.out.println("jdbc-driver: " + jdbcDriver);
	    System.out.println("jdbc-url: " + jdbcUrl);
	    System.out.println("http-port: " + httpPort);
	    System.out.println("context-path: " + contextPath);

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

	    Tomcat.addServlet(ctx, "atomic", new AtomicServlet());
	    ctx.addServletMapping("/atomic.svc/*", "atomic");

	    Tomcat.addServlet(ctx, "xslt", new  HttpServlet() {
		    protected void doGet (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			res.getWriter().println(new Scanner(ClassLoader.getSystemResourceAsStream("atomic.xsl")).useDelimiter("\\Z").next());}});
	    ctx.addServletMapping("/atomic.xsl", "xslt");

	    FilterDef addXSLStyleSheet = new FilterDef();
	    addXSLStyleSheet.setFilterName("addXSLStyleSheet");
	    addXSLStyleSheet.setFilter(new XSLTStyleSheetInjectorFilter());
	    addXSLStyleSheet.addInitParameter("XSL_URL", "atomic.xsl");
	    ctx.addFilterDef(addXSLStyleSheet);
	    FilterDef addClientCaching = new FilterDef();
	    addClientCaching.setFilterName("addClientCaching");
	    addClientCaching.setFilter(new SimpleHeaderInjectorFilter());
	    addClientCaching.addInitParameter("Cache-Control", "max-age=600");
	    ctx.addFilterDef(addClientCaching);
	    FilterDef compressResponse = new FilterDef();
	    compressResponse.setFilterName("compressResponse");
	    compressResponse.setFilter(new CompressionFilter());
	    ctx.addFilterDef(compressResponse);
	    FilterDef disableFirefoxFeedReader = new FilterDef();
	    disableFirefoxFeedReader.setFilterName("disableFirefoxFeedReader");
	    disableFirefoxFeedReader.setFilter(new MozillaWebFeedSpoofingFilter());
	    ctx.addFilterDef(disableFirefoxFeedReader);
	    FilterMap addClientCachingMap = new FilterMap();
	    addClientCachingMap.setFilterName("addClientCaching");
	    addClientCachingMap.addURLPattern("/atomic.svc/*");
	    ctx.addFilterMap(addClientCachingMap);
	    FilterMap compressResponseMap = new FilterMap();
	    compressResponseMap.setFilterName("compressResponse");
	    compressResponseMap.addURLPattern("/atomic.svc/*");
	    ctx.addFilterMap(compressResponseMap);
	    FilterMap disableFirefoxFeedReaderMap = new FilterMap();
	    disableFirefoxFeedReaderMap.setFilterName("disableFirefoxFeedReader");
	    disableFirefoxFeedReaderMap.addURLPattern("/atomic.svc/*");
	    ctx.addFilterMap(disableFirefoxFeedReaderMap);
	    FilterMap addXSLStyleSheetMap = new FilterMap();
	    addXSLStyleSheetMap.setFilterName("addXSLStyleSheet");
	    addXSLStyleSheetMap.addURLPattern("/atomic.svc/*");
	    ctx.addFilterMap(addXSLStyleSheetMap);
	    
	    tomcat.start();
	    tomcat.getServer().await();}
	catch (Exception t) {
	    t.printStackTrace(System.err);
	    System.out.println("Atomic has encountered a fatal error and is shutting down.");
	    System.exit(1);}}}
