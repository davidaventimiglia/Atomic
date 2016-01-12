package org.neptunestation.atomic.standalone;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;
import javax.sql.rowset.*;

public abstract class SQLCollections {
    public static void main (String[] args) throws ClassNotFoundException, SQLException {
	Class.forName(args[0]);
	RowSetFactory aFactory = RowSetProvider.newFactory();
	JdbcRowSet jrs = aFactory.createJdbcRowSet();
	jrs.setCommand(args[4]);
	jrs.setUrl(args[1]);
	jrs.setUsername(args[2]);
	jrs.setPassword(args[3]);
	for (Map<String, SQLValue> m : asIterable(jrs)) System.out.println(m);
	for (Map<String, SQLValue> m : asIterable(jrs)) System.out.println(m);
	for (Map<String, SQLValue> m : asIterable(jrs)) System.out.println(m);}

    static class SQLValueRowSet implements SQLValue {
	int i;
	RowSet r;
	public SQLValueRowSet (int i, RowSet r) {
	    this.r = r;
	    this.i = i;}
	public String toString () throws IllegalStateException {
	    try {return r.getString(i);}
	    catch (Exception e) {throw new IllegalStateException(e);}}}

    public static Iterable<Map<String, SQLValue>> asIterable (final RowSet r) throws SQLException {
	r.execute();
	return
	    (Iterable<Map<String, SQLValue>>)
	    Proxy
	    .newProxyInstance(Iterable.class.getClassLoader(),
			      new Class[] {Iterable.class},
			      new InvocationHandler() {
				  @Override public Object invoke (Object proxy, Method method, Object[] args) throws Exception {
				      if (method.getDeclaringClass().equals(Iterable.class) && method.getName().equals("iterator"))
					  return new Iterator<Map<String, SQLValue>>() {
					      private ResultSetMetaData m = r.getMetaData();
					      private boolean hasNext = false;
					      private boolean didNext = false;
					      @Override public boolean hasNext () {
						  if (!didNext) {
						      try {hasNext = r.next();} catch (Exception e) {hasNext = false;}
						      didNext = true;}
						  return hasNext;}
					      @Override public Map<String, SQLValue> next () {
						  if (!hasNext()) throw new NoSuchElementException();
						  try {
						      Map<String, SQLValue> map = new HashMap<>();
						      for (int i=1; i<=m.getColumnCount(); i++) map.put(m.getColumnName(i), new SQLValueRowSet(i, r));
						      didNext = false;
						      return map;}
						  catch (Exception e) {throw new IllegalStateException(e);}}
					      @Override public void remove () {throw new UnsupportedOperationException();}};
				      return method.invoke(args);}});}}
