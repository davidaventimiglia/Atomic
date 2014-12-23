package org.neptunestation.atomic.core;

import java.sql.*;
import java.util.*;
import javax.naming.*;
import javax.sql.*;
import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.edm.provider.*;
import org.apache.olingo.odata2.api.exception.*;

public class TopDownAtomicEdmProvider extends AtomicEdmProvider {
    public TopDownAtomicEdmProvider (Properties params, String username, String password) {
        super(params, username, password);}
    public class AtomicSchema extends Schema {
        public AtomicSchema (DatabaseMetaData m, String catalog) throws NamingException, SQLException {
            setNamespace(catalog);
            setEntityTypes(makeEntityTypes(m, catalog));
            setComplexTypes(makeComplexTypes(m, catalog));
            setAssociations(makeAssociations(m, catalog));
            setEntityContainers(makeEntityContainers(m, catalog));}}
    protected List<EntityType> makeEntityTypes (DatabaseMetaData meta, String catalog) throws NamingException, SQLException {
        List<EntityType> entityTypes = new ArrayList<EntityType>();
        ResultSet r = meta.getTables(catalog, null, null, null);
        while (r.next()) entityTypes.add(makeEntityType(meta, catalog, r.getString("TABLE_SCHEM"), r.getString("TABLE_NAME")));
        r.close();
        return entityTypes;}
    protected List<EntityContainer> makeEntityContainers (DatabaseMetaData meta, String catalog) throws NamingException, SQLException {
        List<EntityContainer> entityContainers = new ArrayList<EntityContainer>();
        ResultSet r = meta.getSchemas();
        while (r.next()) entityContainers.add(makeEntityContainer(meta, catalog, r));
        r.close();
        return entityContainers;}
    protected EntityContainer makeEntityContainer (DatabaseMetaData meta, String catalog, ResultSet r) throws NamingException, SQLException {
        EntityContainer e = new EntityContainer();
        e.setName(r.getString("TABLE_SCHEM"));
        e.setDefaultEntityContainer(false);
        e.setEntitySets(makeEntitySets(meta, catalog, r.getString("TABLE_SCHEM")));
        e.setAssociationSets(makeAssociationSets());
        e.setFunctionImports(makeFunctionImports());
        return e;}
    protected List<EntitySet> makeEntitySets (DatabaseMetaData meta, String catalog, String schema) throws NamingException, SQLException {
        List<EntitySet> entitySets = new ArrayList<EntitySet>();
        ResultSet r = meta.getTables(null, schema, null, null);
        while (r.next()) entitySets.add(makeEntitySet(catalog, schema, r.getString("TABLE_NAME")));
        r.close();
        return entitySets;}
    @Override public List<Schema> getSchemas () throws ODataException {
        try {
            List<Schema> schemas = new ArrayList<Schema>();
            Connection conn = getConn();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet r = meta.getCatalogs();
            while (r.next()) schemas.add(new AtomicSchema(meta, r.getString("TABLE_CAT")));
            r.close();
            conn.close();
            return schemas;}
        catch (Throwable e) {throw new ODataException(e);}}}
