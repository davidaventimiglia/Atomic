package org.neptunestation.atomic.standalone;

import java.beans.*;
import java.beans.Introspector.*;
import java.io.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;
import javax.sql.rowset.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import org.stringtemplate.v4.*;

public abstract class SQLCollections {
    public static Map<String, Object> introspect(Object obj) throws Exception {
	Map<String, Object> result = new HashMap<String, Object>();
	for (PropertyDescriptor pd : Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors())
	    if (pd.getReadMethod()!=null) result.put(pd.getName(), pd.getReadMethod().invoke(obj));
	return result;}

    public static void main (String[] args) throws ClassNotFoundException, SQLException, Exception {
	// Use the constructor that accepts a Reader
	// STGroup group = new STGroupString("group simple; vardef(type,name) ..."); // templates from abovenew StringReader(templates));
	// ST t = group.getInstanceOf("vardef");
	// t.add("type", "int");
	// t.add("name", "foo");
	// System.out.println(t);

	java.sql.Statement s = DriverManager.getConnection(args[1], args[2], args[3]).createStatement();

	// System.out.println("1:  RowSets");
	// {
	//     JdbcRowSet jrs = aFactory.createJdbcRowSet();
	//     jrs.setCommand(args[4]);
	//     jrs.setUrl(args[1]);
	//     jrs.setUsername(args[2]);
	//     jrs.setPassword(args[3]);
	//     for (Map<String, SQLValue> m : asIterable(jrs)) System.out.println(m);
	//     for (Map<String, SQLValue> m : asIterable(jrs)) {for (Object v : m.values()) {System.out.println(introspect(v)); break;} break;}}
	// System.out.println();

	// System.out.println("2:  WebRowSets");
	// RowSetProvider.newFactory().createWebRowSet().writeXml(s.executeQuery(args[4]), System.out);
	// System.out.println();

	// System.out.println("3:  Properties");
	// for (Map<String, String> m : asIterable(asIterable(s.executeQuery(args[4])))) {Properties p = new Properties(); p.putAll(m); p.store(System.out, "Made with Atomic");}
	// System.out.println();

	Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource(SQLCollections.class.getResourceAsStream("/atomic2.xsl")));
	for (Properties p : asIterable(asIterable(s.executeQuery(args[4])))) {
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    p.storeToXML(buffer, "Made with Atomic");
	    t.transform(new StreamSource(new ByteArrayInputStream(buffer.toByteArray())), new StreamResult(System.out));
	    System.out.print(System.getProperty("record.separator", ""));}
	
	// System.out.println("4:  Maps");
	// for (Map<String, SQLValue> m : asIterable(s.executeQuery(args[4]))) System.out.println(m);
	// System.out.println();

	// System.out.println("5:  JavaBeans");
	// for (Map<String, SQLValue> m : asIterable(s.executeQuery(args[4]))) {for (Object v : m.values()) {System.out.println(introspect(v)); break;} break;}
	// System.out.println();
    }

    public static interface SQLValue {
	String getCatalogName () throws SQLException;
	String getColumnClassName () throws SQLException;
	int getColumnCount () throws SQLException;
	int getColumnDisplaySize () throws SQLException;
	String getColumnLabel () throws SQLException;
	String getColumnName () throws SQLException;
	int getColumnType () throws SQLException;
	String getColumnTypeName () throws SQLException;
	int getPrecision () throws SQLException;
	int getScale () throws SQLException;
	String getSchemaName () throws SQLException;
	String getTableName () throws SQLException;
	boolean isAutoIncrement () throws SQLException;
	boolean isCaseSensitive () throws SQLException;
	boolean isCurrency () throws SQLException;
	boolean isDefinitelyWritable () throws SQLException;
	int isNullable () throws SQLException;
	boolean isReadOnly () throws SQLException;
	boolean isSearchable () throws SQLException;
	boolean isSigned () throws SQLException;
	boolean isWritable () throws SQLException;}

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
	public SQLValueImpl () {}
	public SQLValueImpl (int i, ResultSet r) {
	    this.r = r;
	    this.i = i;}
	public String toString () throws IllegalStateException {
	    try {return r.getString(i);}
	    catch (Exception e) {throw new IllegalStateException(e);}}}

    public static Iterable<Properties> asIterable (final Iterable<Map<String, SQLValue>> it) {
	return new Iterable<Properties>() {
	    @Override public Iterator<Properties> iterator () {
		return new Iterator<Properties>() {
		    final Iterator<Map<String, SQLValue>> proxy = it.iterator();
		    @Override public boolean hasNext () {return proxy.hasNext();}
		    @Override public Properties next () {
			Properties p = new Properties();
		    	for (Map.Entry<String, SQLValue> e : proxy.next().entrySet()) p.setProperty(e.getKey(), e.getValue().toString());
		    	return p;}};}};}

    public static Iterable<Map<String, SQLValue>> asIterable (final ResultSet r) throws SQLException {
	if (r instanceof RowSet) ((RowSet)r).execute();
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
