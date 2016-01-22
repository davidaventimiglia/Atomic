package org.neptunestation.atomic.standalone;

import java.beans.*;
import java.beans.Introspector.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;
import javax.sql.rowset.*;

public abstract class SQLCollections {
    public static Map<String, Object> introspect(Object obj) throws Exception {
	Map<String, Object> result = new HashMap<String, Object>();
	for (PropertyDescriptor pd : Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors())
	    if (pd.getReadMethod()!=null) result.put(pd.getName(), pd.getReadMethod().invoke(obj));
	return result;}

    public static void main (String[] args) throws ClassNotFoundException, SQLException, Exception {
	Class.forName(args[0]);
	RowSetFactory aFactory = RowSetProvider.newFactory();
	JdbcRowSet jrs = aFactory.createJdbcRowSet();
	jrs.setCommand(args[4]);
	jrs.setUrl(args[1]);
	jrs.setUsername(args[2]);
	jrs.setPassword(args[3]);
	for (Map<String, SQLValue> m : asIterable(jrs)) System.out.println(m);
	for (Map<String, SQLValue> m : asIterable(jrs)) for (Object v : m.values()) {System.out.println(introspect(v)); return;}}

    public static class SQLValueImpl implements SQLValue {
	int i;
	ResultSet r;
	public String getCatalogName () throws SQLException {return r.getMetaData().getCatalogName(i);}
	public String getColumnClassName () throws SQLException {return r.getMetaData().getColumnClassName(i);}
	public int getColumnCount () throws SQLException {return r.getMetaData().getColumnCount();}
	public int getColumnDisplaySize () throws SQLException {return r.getMetaData().getColumnDisplaySize(i);}
	public String getColumnLabel () throws SQLException {return r.getMetaData().getColumnLabel(i);}
	public String getColumnName () throws SQLException {return r.getMetaData().getColumnName(i);}
	public int getColumnType () throws SQLException {return r.getMetaData().getColumnType(i);}
	public String getColumnTypeName () throws SQLException {return r.getMetaData().getColumnTypeName(i);}
	public int getPrecision () throws SQLException {return r.getMetaData().getPrecision(i);}
	public int getScale () throws SQLException {return r.getMetaData().getScale(i);}
	public String getSchemaName () throws SQLException {return r.getMetaData().getSchemaName(i);}
	public String getTableName () throws SQLException {return r.getMetaData().getTableName(i);}
	public boolean isAutoIncrement () throws SQLException {return r.getMetaData().isAutoIncrement(i);}
	public boolean isCaseSensitive () throws SQLException {return r.getMetaData().isCaseSensitive(i);}
	public boolean isCurrency () throws SQLException {return r.getMetaData().isCurrency(i);}
	public boolean isDefinitelyWritable () throws SQLException {return r.getMetaData().isDefinitelyWritable(i);}
	public int isNullable () throws SQLException {return r.getMetaData().isNullable(i);}
	public boolean isReadOnly () throws SQLException {return r.getMetaData().isReadOnly(i);}
	public boolean isSearchable () throws SQLException {return r.getMetaData().isSearchable(i);}
	public boolean isSigned () throws SQLException {return r.getMetaData().isSigned(i);}
	public boolean isWritable () throws SQLException {return r.getMetaData().isWritable(i);}
	public String getData () throws SQLException {return r.getString(i);}
	public SQLValueImpl (int i, ResultSet r) {
	    this.r = r;
	    this.i = i;}
	public String toString () throws IllegalStateException {
	    try {return String.format("\"%s\"", r.getString(i));}
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
						      for (int i=1; i<=m.getColumnCount(); i++) map.put(m.getColumnName(i), new SQLValueImpl(i, r));
						      didNext = false;
						      return map;}
						  catch (Exception e) {throw new IllegalStateException(e);}}
					      @Override public void remove () {throw new UnsupportedOperationException();}};
				      return method.invoke(args);}});}}
