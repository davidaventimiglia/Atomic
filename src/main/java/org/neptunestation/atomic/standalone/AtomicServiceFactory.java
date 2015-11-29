package org.neptunestation.olingo.odata2.jdbc.processor.core;

import java.lang.reflect.*;
import java.util.*;
import javax.xml.bind.*;
import org.apache.olingo.odata2.api.*;
import org.apache.olingo.odata2.api.edm.provider.*;
import org.apache.olingo.odata2.api.ep.*;
import org.apache.olingo.odata2.api.exception.*;
import org.apache.olingo.odata2.api.processor.*;

public class AtomicServiceFactory extends ODataServiceFactory {
    public static final String DEBUG = "DEBUG";

    protected String decode (String s) {
        if (s==null) throw new IllegalArgumentException("The decode string is required.");
        return new String(DatatypeConverter.parseBase64Binary(s));}

    protected String[] parseAuthorizationHeader (String header) throws ODataException {
        if (header.split("\\s+").length<2) throw new ODataException("Authorization header is malformed.");
        if (!header.split("\\s+")[0].equalsIgnoreCase("BASIC")) throw new ODataException("Authorizaton header is not BASIC.");
        try {decode(header.split("\\s+")[1]);} catch (Exception e) {throw new ODataException("Authorization credentials are not Base64 encoded.");}
        return decode(header.split("\\s+")[1]).split(":");}

    public static class AtomicDebugCallback implements ODataDebugCallback {
        @Override public boolean isDebugEnabled () {return "true".equalsIgnoreCase(System.getProperty(DEBUG));}}

    public class AtomicErrorCallback implements ODataErrorCallback {
        @Override public ODataResponse handleError (ODataErrorContext context) throws ODataApplicationException { 
            context.getException().printStackTrace(System.err);
            return EntityProvider.writeErrorDocument(context);}}

    @Override public <T extends ODataCallback> T getCallback (Class<T> callbackInterface) { 
        if (callbackInterface==null) throw new IllegalArgumentException("ODataCallback parameter is required.");
        if (callbackInterface.isAssignableFrom(AtomicDebugCallback.class)) return (T) new AtomicDebugCallback();
        if (callbackInterface.isAssignableFrom(AtomicErrorCallback.class)) return (T) new AtomicErrorCallback();
        return (T) super.getCallback(callbackInterface);}

    @Override public ODataService createService (ODataContext ctx) throws ODataException {
        String[] credentials = ctx.getRequestHeader("Authorization")!=null ? parseAuthorizationHeader(ctx.getRequestHeader("Authorization")) : new String[]{null, null};
        String username = credentials.length>=1 ? credentials[0] : null;
        String password = credentials.length>=2 ? credentials[1] : null;
        try {
            Class c = Class.forName(System.getProperty(AtomicEdmProvider.PROVIDER));
            Constructor ctor = c.getDeclaredConstructor(Properties.class, String.class, String.class);
            ctor.setAccessible(true);
            AtomicEdmProvider provider = (AtomicEdmProvider)ctor.newInstance(System.getProperties(), username, password);
            return createODataSingleProcessorService(provider, new AtomicSingleProcessor(System.getProperties(), username, password));}
        catch (Throwable t) {throw new ODataException(t);}}}
