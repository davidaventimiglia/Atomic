package org.neptunestation.atomic.standalone;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.olingo.odata2.api.*;
import org.apache.olingo.odata2.core.servlet.*;
import org.neptunestation.olingo.odata2.jdbc.processor.core.*;

public class AtomicServlet extends ODataServlet {
    public static String NOAUTH = "NOAUTH";

    protected Hashtable<String, String> params = new Hashtable<String, String> ();

    @Override public void init (ServletConfig config) throws ServletException {
        params.put(ODataServiceFactory.FACTORY_LABEL, AtomicServiceFactory.class.getName());
        System.setProperty(AtomicServiceFactory.DEBUG, "" + config.getInitParameter(AtomicServiceFactory.DEBUG));
        System.setProperty(JDBCEdmProvider.PROVIDER, config.getInitParameter(JDBCEdmProvider.PROVIDER)!=null ? config.getInitParameter(JDBCEdmProvider.PROVIDER) : JDBCEdmProvider.class.getName());
        super.init(config);}

    @Override protected void service (final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if ("true".equalsIgnoreCase(getInitParameter(AtomicServlet.NOAUTH)+"")) {super.service(request, response); return;}
        if (request.getHeader("Authorization")!=null) {super.service(request, response); return;}
        response.setHeader("WWW-Authenticate", "BASIC realm = \"Atomic\"");
        response.sendError(response.SC_UNAUTHORIZED);}

    @Override public String getInitParameter (String name) {
        if (params.containsKey(name)) return params.get(name);
        return super.getInitParameter(name);}

    @Override public Enumeration<String> getInitParameterNames () {
        return new Enumeration<String> () {
            private Enumeration<String> e1 = params.keys();
            private Enumeration<String> e2 = AtomicServlet.super.getInitParameterNames();
            public boolean hasMoreElements () {
                if (e1.hasMoreElements()) return true;
                return e2.hasMoreElements();}
            public String nextElement () {
                if (e1.hasMoreElements()) return e1.nextElement();
                return e2.nextElement();}};}}
