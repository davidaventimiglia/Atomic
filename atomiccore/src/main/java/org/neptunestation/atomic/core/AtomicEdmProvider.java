package org.neptunestation.atomic.core;

import java.sql.*;
import java.util.*;
import javax.naming.*;
import javax.sql.*;
import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.edm.provider.*;
import org.apache.olingo.odata2.api.exception.*;

public abstract class AtomicEdmProvider extends EdmProvider {
    public static String PROVIDER = "PROVIDER";
    protected String username = null;
    protected String password = null;
    protected Properties params = null;

    public static Boolean parseYesNo (String v) {
        if (v==null) return new Boolean(false);
        if (v.equalsIgnoreCase("YES")) return new Boolean(true);
        return false;}

    public AtomicEdmProvider (Properties params, String username, String password) {
        super();
        if (params==null) throw new IllegalArgumentException("The 'params' argument is required.");
        this.params = params;
        this.username = username;
        this.password = password;}

    protected Connection getConn () throws NamingException, SQLException {
	return username==null && password==null ? getDataSource().getConnection() : getDataSource().getConnection(username, password);}

    protected DataSource getDataSource () throws NamingException, SQLException {
        return ((DataSource)((Context)(new InitialContext()).lookup("java:comp/env")).lookup("jdbc/AtomicDB"));}

    protected EntityType makeEntityType (DatabaseMetaData meta, String catalog, String schema, String table) throws NamingException, SQLException {
        EntityType e = new EntityType();
        e.setName(table);
        e.setDocumentation(makeDocumentation(meta, catalog, schema, table));
        e.setProperties(makeProperties(meta, catalog, schema, table));
        e.setKey(makeKey(meta, catalog, schema, table));
        e.setNavigationProperties(makeNavigationProperties(meta, catalog, schema, table));
        return e;}

    protected List<NavigationProperty> makeNavigationProperties (DatabaseMetaData meta, String catalog, String schema, String table) {
        List<NavigationProperty> navigationProperties = new ArrayList<NavigationProperty>();
        return navigationProperties;}

    protected Documentation makeDocumentation (DatabaseMetaData meta, String catalog, String schema, String table) throws NamingException, SQLException {
        Documentation d = new Documentation();
        d.setLongDescription(String.format("%s.%s.%s", catalog, schema, table));
        d.setSummary(String.format("%s", table));
        return d;}

    protected List<Property> makeProperties (DatabaseMetaData meta, String catalog, String schema, String table) throws NamingException, SQLException {
	try (ResultSet r = meta.getColumns(catalog, schema, table, null)) {
	    List<Property> properties = new ArrayList<Property>();
	    while (r.next()) properties.add(makeProperty(catalog, schema, table, r.getString("COLUMN_NAME"), r.getInt("DATA_TYPE"), r.getString("COLUMN_DEF"), new Integer(r.getString("COLUMN_SIZE")), parseYesNo(r.getString("IS_NULLABLE")), new Integer(r.getString("DECIMAL_DIGITS")), new Integer(r.getString("DECIMAL_DIGITS"))));
	    return properties;}}

    protected Property makeProperty (String catalog, String schema, String table, String columnName, int dataType, String defaultValue, Integer maxLength, Boolean nullable, Integer precision, Integer scale) {
        SimpleProperty p = new SimpleProperty();
        p.setName(columnName);
        p.setType(getType(dataType));
        Facets f = new Facets();
        f.setDefaultValue(defaultValue);
        f.setMaxLength(maxLength);
        f.setNullable(nullable);
        f.setPrecision(precision);
        f.setScale(scale);
        p.setFacets(f);
        return p;}

    protected EdmSimpleTypeKind getType (int sqlType) {
        switch (sqlType) {
        // case Types.ARRAY: return EdmSimpleTypeKind.Byte;
        case Types.BIGINT: return EdmSimpleTypeKind.Int64;
        case Types.BINARY: return EdmSimpleTypeKind.Binary;
        case Types.BIT: return EdmSimpleTypeKind.Boolean;
        // case Types.BLOB: return EdmSimpleTypeKind.Byte;
        case Types.BOOLEAN: return EdmSimpleTypeKind.Boolean;
        case Types.CHAR: return EdmSimpleTypeKind.String;
        // case Types.CLOB: return EdmSimpleTypeKind.Byte;
        case Types.DATALINK: return EdmSimpleTypeKind.String;
        case Types.DATE: return EdmSimpleTypeKind.DateTime;
        case Types.DECIMAL: return EdmSimpleTypeKind.Decimal;
        // case Types.DISTINCT: return EdmSimpleTypeKind.Byte;
        case Types.DOUBLE: return EdmSimpleTypeKind.Double;
        case Types.FLOAT: return EdmSimpleTypeKind.Double;
        case Types.INTEGER: return EdmSimpleTypeKind.Int32;
        // case Types.JAVA_OBJECT: return EdmSimpleTypeKind.Byte;
        case Types.LONGNVARCHAR: return EdmSimpleTypeKind.String;
        case Types.LONGVARBINARY: return EdmSimpleTypeKind.Binary;
        case Types.LONGVARCHAR: return EdmSimpleTypeKind.String;
        case Types.NCHAR: return EdmSimpleTypeKind.String;
        // case Types.NCLOB: return EdmSimpleTypeKind.Byte;
        // case Types.NULL: return EdmSimpleTypeKind.Byte;
        // case Types.NUMERIC: return EdmSimpleTypeKind.Byte;
        case Types.NVARCHAR: return EdmSimpleTypeKind.String;
        // case Types.OTHER: return EdmSimpleTypeKind.Byte;
        case Types.REAL: return EdmSimpleTypeKind.Double;
        // case Types.REF: return EdmSimpleTypeKind.Byte;
        case Types.ROWID: return EdmSimpleTypeKind.String;
        case Types.SMALLINT: return EdmSimpleTypeKind.Byte;
        case Types.SQLXML: return EdmSimpleTypeKind.String;
        // case Types.STRUCT: return EdmSimpleTypeKind.Byte;
        case Types.TIME: return EdmSimpleTypeKind.DateTime;
        case Types.TIMESTAMP: return EdmSimpleTypeKind.DateTime;
        case Types.TINYINT: return EdmSimpleTypeKind.SByte;
        case Types.VARBINARY: return EdmSimpleTypeKind.Binary;
        case Types.VARCHAR: return EdmSimpleTypeKind.String;
        default: return EdmSimpleTypeKind.Byte;}}

    protected Key makeKey (DatabaseMetaData meta, String catalog, String schema, String table) throws NamingException, SQLException {
        Key k = new Key();
        k.setKeys(makeKeyProperties(meta, catalog, schema, table));
        return k;}

    protected List<PropertyRef> makeKeyProperties (DatabaseMetaData meta, String catalog, String schema, String table) throws NamingException, SQLException {
	try (ResultSet r = meta.getPrimaryKeys(null, schema, table)) {
	    List<PropertyRef> keyProperties = new ArrayList<PropertyRef>();
	    while (r.next()) keyProperties.add(makeKeyProperty(catalog, schema, table, r.getString("COLUMN_NAME")));
	    if (keyProperties.isEmpty()) return makePseudoKeyProperties(meta, catalog, schema, table);
	    return keyProperties;}}

    protected List<PropertyRef> makePseudoKeyProperties (DatabaseMetaData meta, String catalog, String schema, String table) throws NamingException, SQLException {
	try (ResultSet r = meta.getColumns(null, schema, table, null)) {
	    List<PropertyRef> keyProperties = new ArrayList<PropertyRef>();
	    while (r.next()) keyProperties.add(makeKeyProperty(catalog, schema, table, r.getString("COLUMN_NAME")));
	    return keyProperties;}}

    protected PropertyRef makeKeyProperty (String catalog, String schema, String table, String columnName) {
        PropertyRef p = new PropertyRef();
        p.setName(columnName);
        return p;}

    protected List<ComplexType> makeComplexTypes (DatabaseMetaData meta, String catalog) {return new ArrayList<ComplexType>();}

    protected List<Association> makeAssociations (DatabaseMetaData meta, String catalog) {
        List<Association> associations = new ArrayList<Association>();
        return associations;}

    protected EntitySet makeEntitySet (String catalog, String schema, String setName) throws NamingException, SQLException {
        EntitySet s = new EntitySet();
        s.setName(setName);
        s.setEntityType(new FullQualifiedName(catalog, setName));
        return s;}

    protected List<AssociationSet> makeAssociationSets () {
        return new ArrayList<AssociationSet>();}

    protected List<FunctionImport> makeFunctionImports () {
        return new ArrayList<FunctionImport>();}

    @Override public EntityType getEntityType (FullQualifiedName edmFQName) throws ODataException {
        if (edmFQName==null) throw new IllegalArgumentException("FullQualifiedName is required.");
        try {
            EntityType entityType = null;
            String catalog = edmFQName.getNamespace();
            Connection conn = getConn();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet r = getConn().getMetaData().getTables(null, catalog, edmFQName.getName(), null);
            if (r.next()) entityType = makeEntityType(meta, catalog, r.getString("TABLE_SCHEM"), r.getString("TABLE_NAME"));
            r.close();
            conn.close();
            return entityType;}
        catch (Throwable e) {throw new ODataException(e);}}

    @Override public ComplexType getComplexType (FullQualifiedName edmFQName) throws ODataException {
        if (edmFQName==null) throw new IllegalArgumentException("FullQualifiedName is required.");
        return null;}

    @Override public Association getAssociation (FullQualifiedName edmFQName) throws ODataException {
        if (edmFQName==null) throw new IllegalArgumentException("FullQualifiedName is required.");
        // if ("foo".equals(edmFQName.getNamespace()))
        //     if ("foo".equals(edmFQName.getName()))
        //         return new Association().setName("foo")
        //             .setEnd1(new AssociationEnd().setType(null).setRole("foo").setMultiplicity(EdmMultiplicity.MANY))
        //             .setEnd2(new AssociationEnd().setType(null).setRole("foo").setMultiplicity(EdmMultiplicity.ONE));
        return null;}

    @Override public EntitySet getEntitySet (String entityContainer, String name) throws ODataException {
        if (entityContainer==null) throw new IllegalArgumentException("EntityContainer name is required.");
        if (name==null) throw new IllegalArgumentException("EntitySet name is required.");
        try {return makeEntitySet(null, entityContainer, name);}
        catch (Throwable e) {throw new ODataException(e);}}

    @Override public AssociationSet getAssociationSet (String entityContainer, FullQualifiedName association, String sourceEntitySetName, String sourceEntitySetRole) throws ODataException {
        if (entityContainer==null) throw new IllegalArgumentException("EntityContainer name is required.");
        if (association==null) throw new IllegalArgumentException("Association name is required.");
        if (sourceEntitySetName==null) throw new IllegalArgumentException("Source EntitySet name is required.");
        if (sourceEntitySetRole==null) throw new IllegalArgumentException("Source EntitySet role is required.");
        // if ("foo".equals(entityContainer))
        //     if (ASSOCIATION_CAR_MANUFACTURER.equals(association))
        //         return new AssociationSet().setName(ASSOCIATION_SET)
        //             .setAssociation(ASSOCIATION_CAR_MANUFACTURER)
        //             .setEnd1(new AssociationSetEnd().setRole(ROLE_1_2).setEntitySet(ENTITY_SET_NAME_MANUFACTURERS))
        //             .setEnd2(new AssociationSetEnd().setRole(ROLE_1_1).setEntitySet(ENTITY_SET_NAME_CARS));
        return null;}

    @Override public FunctionImport getFunctionImport (String entityContainer, String name) throws ODataException {
        if (entityContainer==null) throw new IllegalArgumentException("EntityContainer name is required.");
        if (name==null) throw new IllegalArgumentException("FunctionImport name is required.");
        // if (ENTITY_CONTAINER.equals(entityContainer))
        //     if (FUNCTION_IMPORT.equals(name))
        //         return new FunctionImport().setName(name)
        //             .setReturnType(new ReturnType().setTypeName(ENTITY_TYPE_1_1).setMultiplicity(EdmMultiplicity.MANY))
        //             .setHttpMethod("GET");
        return null;}

    @Override public EntityContainerInfo getEntityContainerInfo (String name) throws ODataException {
        if (name==null) return new EntityContainerInfo().setName("DefaultEntityContainer").setDefaultEntityContainer(true);
        return new EntityContainerInfo().setName(name);}}
