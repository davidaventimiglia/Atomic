package org.neptunestation.atomic.standalone;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public abstract class SQLCollections {
    public static Iterable<Map<String, String>> create (final ResultSet r) {
    	return 
    	    (Iterable<Map<String, String>>)
    	    Proxy
    	    .newProxyInstance(Iterable.class.getClassLoader(),
    			      new Class[] {ResultSet.class},
    			      new InvocationHandler() {
    				  @Override public Object invoke (Object proxy, Method method, Object[] args) throws Exception {
				      if (method.getDeclaringClass().equals(Iterable.class) && method.getName().equals("iterator"))
					  return new Iterator<Map<String, String>>() {
					      private ResultSetMetaData m = r.getMetaData();
					      private boolean hasNext = true;
					      private Map<String, String> map;
					      @Override public boolean hasNext () {
						  return hasNext;}
					      @Override public Map<String, String> next () {
						  if (!hasNext()) throw new NoSuchElementException();
						  map = new HashMap<>();
						  try {
						      hasNext = r.next();
						      for (int i=1; i<=m.getColumnCount(); i++) map.put(m.getColumnName(i), r.getString(i));
						      return map;}
						  catch (Exception e) {throw new IllegalStateException(e);}}
					      @Override public void remove () {
						  throw new UnsupportedOperationException();}};
				      throw new UnsupportedOperationException();}});}}
