package org.neptunestation.atomic.standalone;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.catalina.*;
import org.apache.catalina.deploy.*;
import org.apache.catalina.startup.*;
import org.apache.catalina.valves.*;
import org.neptunestation.filterpack.filters.*;
import org.neptunestation.olingo.odata2.jdbc.processor.core.*;

public class Atomic {
    private static boolean debug = false;
    private static String jdbcDriver;
    private static String jdbcUrl;
    private static int httpPort = 80;
    private static String contextPath;
    private static Tomcat tomcat;
    private static Context ctx;
    private static AccessLogValve log;
    public static String getVersion () {
	return String.format("%s/%s", Atomic.class.getPackage().getImplementationTitle(), Atomic.class.getPackage().getImplementationVersion());}

    public static void main (String[] args) throws Exception {
        if (args.length!=1) {printUsage(args); return;}
        init(args);
        if (args[0].equals("usage")) printUsage(args);
        if (args[0].equals("help")) printUsage(args);
        if (args[0].equals("start")) start(args);
        if (args[0].equals("print-bootstrap-xsl")) printBootStrapXSL(args);
        if (args[0].equals("print-format-xsl")) printFormatXSL(args);}
        
    public static void printBootStrapXSL (String[] args) {
        System.out.println(ctx.findFilterDef("addXSLStyleSheet").getFilter());}

    public static void printFormatXSL (String[] args) {
        System.out.println(new Scanner(ClassLoader.getSystemResourceAsStream("atomic.xsl")).useDelimiter("\\Z").next());}

    public static void start (String[] args) throws Exception {
        System.out.println(new Scanner(ClassLoader.getSystemResourceAsStream("atomic_splash.txt")).useDelimiter("\\Z").next().replace("$PORT$", httpPort+""));
        System.out.println("jdbc-driver: " + jdbcDriver);
        System.out.println("jdbc-url: " + jdbcUrl);
        System.out.println("http-port: " + httpPort);
        System.out.println("context-path: " + contextPath);
        System.out.println("debug: " + debug);
        System.out.println("server: " + tomcat.getServer());
        System.out.println("service: " + tomcat.getService());
        System.out.println("engine: " + tomcat.getEngine());
        System.out.println("host: " + tomcat.getHost());
        tomcat.getServer().await();}

    public static void printUsage (String[] args) {
        System.out.println("\n" +
                           "ATOMIC\n" +
                           "======\n" +
                           "\n" +
                           "System Properties:\n" +
                           "\tjdbcDriver - JDBC driver class name (default: none)\n" +
                           "\tjdbcUrl - JDBC URL (default: none)\n" +
                           "\thttpPort - HTTP Port (default: 80)\n" +
                           "\tcontextPath - URL Context Path (default: '')\n" +
                           "\tdebug - Debug output in [true, false] (default: false)\n" +
                           "\n" +
                           "Commands:\n" +
                           "\tstart - Start server.\n" +
                           "\tprint-bootstrap-xsl - Print bootstrap XSL.\n" +
                           "\tprint-format-xsl - Print format XSL.\n");}

    public static void init (String[] args) throws Exception {
	debug = false;
	try {
	    // LogManager.getLogManager().readConfiguration(ClassLoader.getSystemResourceAsStream("logging.properties"));
	    
	    jdbcDriver = System.getProperty("jdbc-driver");
	    jdbcUrl = System.getProperty("jdbc-url");
	    if (jdbcDriver==null || jdbcUrl==null) {printUsage(args); return;}
	    httpPort = 80;
	    try {httpPort = Integer.parseInt(System.getProperty("http-port")==null ? "80" : System.getProperty("http-port"));}
	    catch (Throwable t) {System.err.println("The 'http-port' system property must be an integer.");}
	    contextPath = System.getProperty("context-path")==null ? "" : System.getProperty("context-path");
	    try {debug = Boolean.parseBoolean(System.getProperty("debug"));}
	    catch (Throwable t) {System.err.println("The 'debug' system property must have a value in [true, false].");}

	    tomcat = new Tomcat();
	    tomcat.setBaseDir(String.format("atomic.%s", httpPort));
	    tomcat.getService().setName(getVersion());
	    tomcat.getEngine().setName(getVersion());
            tomcat.enableNaming();
	    tomcat.setPort(httpPort);
	    tomcat.setSilent(true);
	    tomcat.getConnector().setProperty("server", getVersion());

	    ctx = tomcat.addContext(contextPath, new File(".").getAbsolutePath());

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
	    ctx.addServletMapping("/atomic/*", "atomic");
	    ctx.addServletMapping("/atomic.debug/*", "atomic");

	    Tomcat.addServlet(ctx, "xslt", new  HttpServlet() {
		    protected void doGet (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			res.getWriter().println(new Scanner(ClassLoader.getSystemResourceAsStream("atomic.xsl")).useDelimiter("\\Z").next());}});
	    ctx.addServletMapping("/atomic.xsl", "xslt");

	    FilterDef addXSLStyleSheet = new FilterDef();
	    addXSLStyleSheet.setFilterName("addXSLStyleSheet");
	    addXSLStyleSheet.setFilter(new XSLTStyleSheetInjectorFilter());
	    addXSLStyleSheet.addInitParameter("XSL_URL", "atomic.xsl");
	    ctx.addFilterDef(addXSLStyleSheet);
	    FilterDef removeXSLStyleSheet = new FilterDef();
	    removeXSLStyleSheet.setFilterName("removeXSLStyleSheet");
	    removeXSLStyleSheet.setFilter(new XSLTStyleSheetStripperFilter());
	    ctx.addFilterDef(removeXSLStyleSheet);
	    FilterDef removeXMLComments = new FilterDef();
	    removeXMLComments.setFilterName("removeXMLComments");
	    removeXMLComments.setFilter(new XMLCommentsStripperFilter());
	    ctx.addFilterDef(removeXMLComments);
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
	    // FilterDef convertPut = new FilterDef();
	    // convertPut.setFilterName("convertPut");
	    // convertPut.setFilter(new RequestHeaderRewrite());
	    // ctx.addFilterDef(convertPut);

	    FilterMap addClientCachingMap = new FilterMap();
	    addClientCachingMap.setFilterName("addClientCaching");
	    addClientCachingMap.addURLPattern("/atomic/*");
	    addClientCachingMap.addURLPattern("/atomic.debug/*");
	    ctx.addFilterMap(addClientCachingMap);
	    FilterMap compressResponseMap = new FilterMap();
	    compressResponseMap.setFilterName("compressResponse");
	    compressResponseMap.addURLPattern("/atomic/*");
	    compressResponseMap.addURLPattern("/atomic.debug/*");
	    ctx.addFilterMap(compressResponseMap);
	    FilterMap removeXMLCommentsMap = new FilterMap();
	    removeXMLCommentsMap.setFilterName("removeXMLComments");
	    removeXMLCommentsMap.addURLPattern("/atomic/$metadata");
	    ctx.addFilterMap(removeXMLCommentsMap);
	    FilterMap disableFirefoxFeedReaderMap = new FilterMap();
	    disableFirefoxFeedReaderMap.setFilterName("disableFirefoxFeedReader");
	    disableFirefoxFeedReaderMap.addURLPattern("/atomic/*");
	    disableFirefoxFeedReaderMap.addURLPattern("/atomic.debug/*");
	    ctx.addFilterMap(disableFirefoxFeedReaderMap);
	    FilterMap removeXSLStyleSheetMap = new FilterMap();
	    removeXSLStyleSheetMap.setFilterName("removeXSLStyleSheet");
	    removeXSLStyleSheetMap.addURLPattern("/atomic/$metadata");
	    ctx.addFilterMap(removeXSLStyleSheetMap);
	    FilterMap addXSLStyleSheetMap = new FilterMap();
	    addXSLStyleSheetMap.setFilterName("addXSLStyleSheet");
	    addXSLStyleSheetMap.addURLPattern("/atomic/*");
	    ctx.addFilterMap(addXSLStyleSheetMap);
	    // FilterMap convertPutMap = new FilterMap();
	    // convertPutMap.setFilterName("convertPut");
	    // convertPutMap.addURLPattern("/atomic/*");
	    // ctx.addFilterMap(convertPutMap);

	    AccessLogValve log = new AccessLogValve();
	    log.setPattern("common");
	    ctx.getPipeline().addValve(log);
            tomcat.start();}
	catch (Throwable t) {if (debug) t.printStackTrace(System.err);}}}
