package org.neptunestation.atomic.core;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import javax.naming.*;
import javax.sql.*;
import org.apache.olingo.odata2.api.commons.*;
import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.ep.*;
import org.apache.olingo.odata2.api.ep.entry.*;
import org.apache.olingo.odata2.api.exception.*;
import org.apache.olingo.odata2.api.processor.*;
import org.apache.olingo.odata2.api.uri.*;
import org.apache.olingo.odata2.api.uri.info.*;

public class AtomicSingleProcessor extends ODataSingleProcessor {
    private String username = null;
    private String password = null;
    private Properties params = null;

    public AtomicSingleProcessor (Properties params, String username, String password) {
        super();
        if (params==null) throw new IllegalArgumentException("The 'params' argument is required.");
        this.params = params;
        this.username = username;
        this.password = password;}

    protected Connection getConn () throws NamingException, SQLException {
        if (username==null && password==null) return getDataSource().getConnection();
        return getDataSource().getConnection(username, password);}

    protected DataSource getDataSource () throws NamingException, SQLException {
        return ((DataSource)((Context)(new InitialContext()).lookup("java:comp/env")).lookup("jdbc/AtomicDB"));}

    protected int getKeyValue (KeyPredicate key) throws ODataException {
        EdmProperty property = key.getProperty();
        EdmSimpleType type = (EdmSimpleType)property.getType();
        return type.valueOfString(key.getLiteral(), EdmLiteralKind.DEFAULT, property.getFacets(), Integer.class);}

    protected Object getColumnObject (ResultSet r, ResultSetMetaData m, int i) throws SQLException {
        switch (m.getColumnType(i)) {
        case Types.ARRAY: return r.getArray(i);
        // case Types.BIGINT: return r.getBigInt(i);
        // case Types.BINARY: return r.getBinary(i);
        // case Types.BIT: return r.getBit(i);
        case Types.BLOB: return r.getBlob(i);
        case Types.BOOLEAN: return r.getBoolean(i);
        // case Types.CHAR: return r.getChar(i);
        case Types.CLOB: return r.getClob(i);
        // case Types.DATALINK: return r.getDatalink(i);
        case Types.DATE: return r.getDate(i);
        // case Types.DECIMAL: return r.getDecimal(i);
        // case Types.DISTINCT: return r.getDistinct(i);
        case Types.DOUBLE: return r.getDouble(i);
        case Types.FLOAT: return r.getFloat(i);
        case Types.INTEGER: return r.getInt(i);
        // case Types.JAVA_OBJECT: return r.getJava_OBJECT(i);
        // case Types.LONGNVARCHAR: return r.getLongnvarchar(i);
        // case Types.LONGVARBINARY: return r.getLongvarbinary(i);
        // case Types.LONGVARCHAR: return r.getLongvarchar(i);
        // case Types.NCHAR: return r.getNchar(i);
        case Types.NCLOB: return r.getNClob(i);
        // case Types.NULL: return r.getNull(i);
        // case Types.NUMERIC: return r.getNumeric(i);
        // case Types.NVARCHAR: return r.getNvarchar(i);
        // case Types.OTHER: return r.getOther(i);
        case Types.REAL: return r.getFloat(i);
        // case Types.REF: return r.getRef(i);
        // case Types.ROWID: return r.getRowid(i);
        case Types.SMALLINT: return r.getInt(i);
        case Types.SQLXML: return r.getSQLXML(i);
        // case Types.STRUCT: return r.getStruct(i);
        case Types.TIME: return r.getTime(i);
        case Types.TIMESTAMP: return r.getTimestamp(i);
        case Types.TINYINT: return r.getInt(i);
        // case Types.VARBINARY: return r.getVarbinary(i);
        case Types.VARCHAR: return r.getString(i);
        default: return r.getObject(i);}}

    protected List<Map<String, Object>> getEntityData (String query) throws SQLException, NamingException {
        try (Connection c = getConn();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(query)) {
                ResultSetMetaData m = r.getMetaData();
                int n = m.getColumnCount();
                List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
                while (r.next()) {
                    Map<String, Object> datum = new HashMap<String, Object>();
                    for (int i=1; i<=n; i++) datum.put(m.getColumnName(i), getColumnObject(r, m, i));
                    data.add(datum);}
                return data;}}

    @Override public ODataResponse readEntitySet (GetEntitySetUriInfo uriInfo, String contentType) throws ODataException {
        if (uriInfo.getNavigationSegments().size()==0) try {
                List<Map<String, Object>> data = getEntityData(String.format(" select * from %s ", uriInfo.getStartEntitySet().getName()));
                if (data.isEmpty()) throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
                return EntityProvider.writeFeed(contentType, uriInfo.getStartEntitySet(), data, EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());}
            catch (Throwable t) {throw new ODataException(t);}
        throw new ODataNotImplementedException();}

    @Override public ODataResponse readEntity (GetEntityUriInfo uriInfo, String contentType) throws ODataException {
        if (uriInfo.getNavigationSegments().size()==0) try {
                List<String> pexps = new ArrayList<String>();
                for (KeyPredicate p : uriInfo.getKeyPredicates()) pexps.add(String.format("\"%s\" = '%s'", p.getProperty().getName(), p.getLiteral()));
                List<Map<String, Object>> data = getEntityData(String.format(" select * from %s where %s ", uriInfo.getStartEntitySet().getName(), pexps.toString().replaceAll(",", " and ").replaceAll("\\[", " ").replaceAll("]", " ")));
                if (data.isEmpty()) throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
                return EntityProvider.writeEntry(contentType, uriInfo.getStartEntitySet(), data.get(0), EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());}
            catch (Throwable t) {throw new ODataException(t);}
        throw new ODataNotImplementedException();}

    @Override public ODataResponse createEntity (PostUriInfo uriInfo, InputStream content, String requestContentType, String contentType) throws ODataException {
        if (uriInfo.getNavigationSegments().size() > 0) throw new ODataNotImplementedException();
        if (uriInfo.getStartEntitySet().getEntityType().hasStream()) throw new ODataNotImplementedException();
        EntityProviderReadProperties properties = EntityProviderReadProperties.init().mergeSemantic(false).build();
        ODataEntry entry = EntityProvider.readEntry(requestContentType, uriInfo.getStartEntitySet(), content, properties);
        Map<String, Object> data = entry.getProperties();
        String sep = null;
        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();
        for (Map.Entry e : data.entrySet()) {
            cols.append(sep).append(e.getKey());
            vals.append(sep).append(e.getValue());
            if (sep==null) sep = ",";}
        String query = String.format("insert int %s (%s) values (%s)");
        try (Connection c = getConn();
             Statement s = c.createStatement()) {
                s.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
                try (ResultSet r = s.getGeneratedKeys()) {if (r.next()) data.put("foo", r.getString(1));} catch (Throwable t) {throw new ODataException(t);}}
        catch (Throwable t) {throw new ODataException(t);}
        //serialize the entry, Location header is set by OData Library
        return EntityProvider.writeEntry(contentType, uriInfo.getStartEntitySet(), entry.getProperties(), EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());}

    @Override public ODataResponse updateEntity (PutMergePatchUriInfo uriInfo, InputStream content, String requestContentType, boolean merge, String contentType) throws ODataException {
        throw new ODataNotImplementedException();}
        // EntityProviderReadProperties properties = EntityProviderReadProperties.init().mergeSemantic(false).build();
        // ODataEntry entry = EntityProvider.readEntry(requestContentType, uriInfo.getTargetEntitySet(), content, properties);
        // //if something goes wrong in deserialization this is managed via the ExceptionMapper,
        // //no need for an application to do exception handling here an convert the exceptions in HTTP exceptions
        // Map<String, Object> data = entry.getProperties();
        // int key = getKeyValue(uriInfo.getKeyPredicates().get(0));

        // //if there is no entry with this key available, one should return "404 Not Found"
        // //return ODataResponse.status(HttpStatusCodes.NOT_FOUND).build();

        // //we can return Status Code 204 No Content because the URI Parsing already guarantees that
        // //a) only valid URIs are dispatched (also checked against the metadata)
        // //b) 404 Not Found is already returned above, when the entry does not exist 
        // return ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();}

    protected int destroyEntity (DeleteUriInfo uriInfo, String contentType) throws ODataException {
        try (Connection c = getConn();
             Statement s = c.createStatement();) {
                return s.executeUpdate(String.format("delete from %s where %s = %s", uriInfo.getStartEntitySet().getName(), uriInfo.getKeyPredicates().get(0).getProperty().getName(), uriInfo.getKeyPredicates().get(0).getLiteral()));}
        catch (Throwable e) {throw new ODataException(e);}}

    @Override public ODataResponse deleteEntity (DeleteUriInfo uriInfo, String contentType) throws ODataException {
        if (uriInfo.getNavigationSegments().size() != 0) throw new ODataNotImplementedException();
        if (destroyEntity(uriInfo, contentType)==0) throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
        return ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();}}
