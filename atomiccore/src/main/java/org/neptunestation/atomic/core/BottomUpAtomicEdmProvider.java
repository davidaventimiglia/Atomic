package org.neptunestation.atomic.core;

import java.sql.*;
import java.util.*;
import javax.naming.*;
import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.edm.provider.*;
import org.apache.olingo.odata2.api.exception.*;

public class BottomUpAtomicEdmProvider extends AtomicEdmProvider {
    protected List<Schema> schemas = new ArrayList<>();
    protected List<EntityContainer> entityContainers = new ArrayList<>();
    protected List<EntitySet> entitySets = new ArrayList<>();

    public BottomUpAtomicEdmProvider (Properties params, String username, String password) {
        super(params, username, password);}

    public class AtomicRoot {
        private Map<String, AtomicSchema> schemas = new HashMap<>();
        public AtomicRoot (DatabaseMetaData m) throws NamingException, SQLException {
            for (ResultSet r = m.getTables(null, null, null, null); r.next();) addSchema(m, r);
            for (Schema s : getSchemas()) if (!s.getEntityContainers().isEmpty()) s.getEntityContainers().get(0).setDefaultEntityContainer(true);}
        public AtomicSchema addSchema (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
            if (schemas.get(""+r.getString(1))==null) {
                AtomicSchema s = new AtomicSchema(m, r);
                schemas.put(s.getNamespace(), s);}
            schemas.get(""+r.getString(1)).addEntityContainer(m, r);
            schemas.get(""+r.getString(1)).addEntityType(m, r);
            return schemas.get(""+r.getString(1));}
        public List<Schema> getSchemas () {
            return new ArrayList<Schema>(schemas.values());}}

    public class AtomicSchema extends Schema {
        private Map<String, AtomicEntityContainer> entityContainers = new HashMap<>();
        private Map<String, AtomicEntityType> entityTypes = new HashMap<>();
        public AtomicSchema (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
            setNamespace("" + r.getString(1));}
        public AtomicEntityContainer addEntityContainer (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
            if (entityContainers.get(""+r.getString(2))==null) {
                AtomicEntityContainer ec = new AtomicEntityContainer(m, r);
                entityContainers.put(ec.getName(), ec);
                getEntityContainers().add(ec);}
            entityContainers.get(""+r.getString(2)).addEntitySet(m, r);
            return entityContainers.get(""+r.getString(2));}
        public AtomicEntityType addEntityType (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
            if (entityTypes.get(""+r.getString(2))==null) {
                AtomicEntityType et = new AtomicEntityType(m, r);
                entityTypes.put(et.getName(), et);
                getEntityTypes().add(et);}
            return entityTypes.get(""+r.getString(3));}
        @Override public List<EntityContainer> getEntityContainers () {
            if (super.getEntityContainers()==null) super.setEntityContainers(new ArrayList<EntityContainer>());
            return super.getEntityContainers();}
        @Override public List<EntityType> getEntityTypes () {
            if (super.getEntityTypes()==null) super.setEntityTypes(new ArrayList<EntityType>());
            return super.getEntityTypes();}}

    public class AtomicEntityType extends EntityType {
        public AtomicEntityType (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
            setName(""+r.getString(3));
            setDocumentation(makeDocumentation(m, r.getString(1), r.getString(2), r.getString(3)));
            setProperties(makeProperties(m, r.getString(1), r.getString(2), r.getString(3)));
            setKey(makeKey(m, r.getString(1), r.getString(2), r.getString(3)));}}

    public class AtomicEntityContainer extends EntityContainer {
        private Map<String, AtomicEntitySet> entitySets = new HashMap<>();
        public AtomicEntityContainer (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
            setName("" + r.getString(2));}
        public AtomicEntitySet addEntitySet (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
            if (entitySets.get(""+r.getString(3))==null) {
                AtomicEntitySet es = new AtomicEntitySet(m, r);
                entitySets.put(es.getName(), es);
                getEntitySets().add(es);}
            return entitySets.get(""+r.getString(3));}
        @Override public List<EntitySet> getEntitySets () {
            if (super.getEntitySets()==null) super.setEntitySets(new ArrayList<EntitySet>());
            return super.getEntitySets();}}

    public class AtomicEntitySet extends EntitySet {
        public AtomicEntitySet (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
            setName(r.getString(3));
            setEntityType(new FullQualifiedName(String.format("%s.%s", r.getString(1), r.getString(2)), r.getString(3)));}}

    @Override public List<Schema> getSchemas () throws ODataException {
        try {
            Connection conn = getConn();
            DatabaseMetaData meta = conn.getMetaData();
            return (new AtomicRoot(meta)).getSchemas();}
        catch (Throwable e) {throw new ODataException(e);}}}
