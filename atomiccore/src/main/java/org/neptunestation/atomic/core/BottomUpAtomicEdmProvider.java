package org.neptunestation.atomic.core;

import java.sql.*;
import java.util.*;
import javax.naming.*;
import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.edm.provider.*;
import org.apache.olingo.odata2.api.exception.*;

public class BottomUpAtomicEdmProvider extends AtomicEdmProvider {
    public static String DEFERRABILITY = "DEFERRABILITY";
    public static String DELETE_RULE = "DELETE_RULE";
    public static String FKCOLUMN_NAME = "FKCOLUMN_NAME";
    public static String FKTABLE_CAT = "FKTABLE_CAT";
    public static String FKTABLE_NAME = "FKTABLE_NAME";
    public static String FKTABLE_SCHEM = "FKTABLE_SCHEM";
    public static String FK_NAME = "FK_NAME";
    public static String GRANTEE = "GRANTEE";
    public static String GRANTOR = "GRANTOR";
    public static String IS_GRANTABLE = "IS_GRANTABLE";
    public static String KEY_SEQ = "KEY_SEQ";
    public static String PKCOLUMN_NAME = "PKCOLUMN_NAME";
    public static String PKTABLE_CAT = "PKTABLE_CAT";
    public static String PKTABLE_NAME = "PKTABLE_NAME";
    public static String PKTABLE_SCHEM = "PKTABLE_SCHEM";
    public static String PK_NAME = "PK_NAME";
    public static String PRIVILEGE = "PRIVILEGE";
    public static String TABLE_CAT = "TABLE_CAT";
    public static String TABLE_NAME = "TABLE_NAME";
    public static String TABLE_SCHEM = "TABLE_SCHEM";
    public static String UPDATE_RULE = "UPDATE_RULE";

    public BottomUpAtomicEdmProvider (Properties params, String username, String password) {
        super(params, username, password);}

    class AtomicRoot {
        Map<String, AtomicSchema> schemas = new HashMap<>();
        public AtomicRoot (DatabaseMetaData m) throws NamingException, SQLException {
	    try (ResultSet r = m.getTablePrivileges(null, null, null)) {while (r.next()) addSchema(m, r);}
	    try (ResultSet r = m.getCrossReference(null, null, null, null, null, null)) {while (r.next()) schemas.get(r.getString(PKTABLE_SCHEM)).addAssociation(m, r);}}
        void addSchema (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    if (!schemas.containsKey(r.getString(TABLE_SCHEM))) {
                AtomicSchema s = new AtomicSchema(this, m, r);
                schemas.put(s.getNamespace(), s);}
            schemas.get(r.getString(TABLE_SCHEM)).addEntityContainer(m, r);
            if (m.getUserName().equals(r.getString(GRANTEE)) && "select".equalsIgnoreCase(r.getString(PRIVILEGE))) schemas.get(r.getString(TABLE_SCHEM)).addEntityType(m, r);}}

    class AtomicSchema extends Schema {
	AtomicRoot root;
        Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
        Map<String, AtomicAssociation> associations = new HashMap<>();
        Map<String, AtomicComplexType> complexTypes = new HashMap<>();
        Map<String, AtomicEntityContainer> entityContainers = new HashMap<>();
        Map<String, AtomicEntityType> entityTypes = new HashMap<>();
        Map<String, AtomicUsing> usings = new HashMap<>();
        AtomicSchema (AtomicRoot root, DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    super();
	    this.root = root;
            setNamespace(r.getString(TABLE_SCHEM));}
	@Override public List<AnnotationAttribute> getAnnotationAttributes () {
	    return new ArrayList<AnnotationAttribute>(annotationAttributes.values());}
	@Override public List<AnnotationElement> getAnnotationElements () {
	    return new ArrayList<AnnotationElement>(annotationElements.values());}
	@Override public List<Association> getAssociations () {
	    return new ArrayList<Association>(associations.values());}
	@Override public List<ComplexType> getComplexTypes () {
	    return new ArrayList<ComplexType>(complexTypes.values());}
	@Override public List<EntityContainer> getEntityContainers () {
	    return new ArrayList<EntityContainer>(entityContainers.values());}
	@Override public List<EntityType> getEntityTypes () {
	    return new ArrayList<EntityType>(entityTypes.values());}
	@Override public List<Using> getUsings () {
	    return new ArrayList<Using>(usings.values());}
	AtomicEntityType getEntityType (String name) {
	    return entityTypes.get(name);}
	AtomicEntityContainer getEntityContainer (String name) {
	    return entityContainers.get(name);}
	void addAssociation (DatabaseMetaData m, ResultSet r) throws SQLException {
	    if (!associations.containsKey(r.getString(FK_NAME))) {
		AtomicAssociation a = new AtomicAssociation(this, m, r);
		AtomicEntityType e = entityTypes.get(a.getEnd1().getRole());
		e.getNavigationProperties().add(new AtomicNavigationProperty(a, e, m, r));
		associations.put(a.getName(), a);}}
	void addComplexTypes () {}
        void addEntityContainer (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    if (!entityContainers.containsKey(r.getString(TABLE_SCHEM))) {
                AtomicEntityContainer ec = new AtomicEntityContainer(this, m, r);
                entityContainers.put(ec.getName(), ec);}
            if (m.getUserName().equals(r.getString(GRANTEE)) && "select".equalsIgnoreCase(r.getString(PRIVILEGE)))
		entityContainers.get(r.getString(TABLE_SCHEM)).addEntitySet(m, r);}
        void addEntityType (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    if (!entityTypes.containsKey(r.getString(TABLE_SCHEM))) {
                AtomicEntityType et = new AtomicEntityType(this, m, r);
                entityTypes.put(et.getName(), et);}}}

    class AtomicNavigationProperty extends NavigationProperty {
	AtomicEntityType entityType;
        Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
	@Override public List<AnnotationAttribute> getAnnotationAttributes () {
	    return new ArrayList<AnnotationAttribute>(annotationAttributes.values());}
	@Override public List<AnnotationElement> getAnnotationElements () {
	    return new ArrayList<AnnotationElement>(annotationElements.values());}
	AtomicNavigationProperty (Association a, AtomicEntityType e, DatabaseMetaData m, ResultSet r) throws SQLException {
	    super();
	    this.entityType = e;
	    setFromRole(r.getString(PKTABLE_NAME));
	    setName(r.getString(FKTABLE_NAME));
	    setRelationship(new FullQualifiedName(r.getString(PKTABLE_SCHEM), a.getName()));
	    setToRole(r.getString(FKTABLE_NAME));}}

    class AtomicAssociation extends Association {
	AtomicSchema schema;
        Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
	@Override public List<AnnotationAttribute> getAnnotationAttributes () {
	    return new ArrayList<AnnotationAttribute>(annotationAttributes.values());}
	@Override public List<AnnotationElement> getAnnotationElements () {
	    return new ArrayList<AnnotationElement>(annotationElements.values());}
	public AtomicAssociation (AtomicSchema s, DatabaseMetaData m, ResultSet r) throws SQLException {
	    super();
	    this.schema = s;
	    setName(r.getString(FK_NAME));
	    setEnd1(new AtomicAssociationEnd(this, m, r, true));
	    setEnd2(new AtomicAssociationEnd(this, m, r, false));
	    setReferentialConstraint(new AtomicReferentialConstraint(this, m, r, getEnd1(), getEnd2()));}}

    class AtomicReferentialConstraint extends ReferentialConstraint {
	AtomicAssociation association;
        Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
	@Override public List<AnnotationAttribute> getAnnotationAttributes () {
	    return new ArrayList<AnnotationAttribute>(annotationAttributes.values());}
	@Override public List<AnnotationElement> getAnnotationElements () {
	    return new ArrayList<AnnotationElement>(annotationElements.values());}
	public AtomicReferentialConstraint (AtomicAssociation a, DatabaseMetaData m, ResultSet r, AssociationEnd end1, AssociationEnd end2) {
	    super();
	    this.association = a;
	    setDependent(new AtomicReferentialConstraintRole(this, m, r, end2));
	    setPrincipal(new AtomicReferentialConstraintRole(this, m, r, end1));}}

    class AtomicReferentialConstraintRole extends ReferentialConstraintRole {
	AtomicReferentialConstraint referentialConstraint;
        Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
	@Override public List<AnnotationAttribute> getAnnotationAttributes () {
	    return new ArrayList<AnnotationAttribute>(annotationAttributes.values());}
	@Override public List<AnnotationElement> getAnnotationElements () {
	    return new ArrayList<AnnotationElement>(annotationElements.values());}
	@Override public List<PropertyRef> getPropertyRefs () {
	    return new ArrayList<PropertyRef>();}
	public AtomicReferentialConstraintRole (AtomicReferentialConstraint rc, DatabaseMetaData m, ResultSet r, AssociationEnd end) {
	    super();
	    this.referentialConstraint = rc;
	    setRole(end.getRole());
	    setPropertyRefs(new ArrayList<PropertyRef>());}}

    class AtomicAssociationEnd extends AssociationEnd {
	AtomicAssociation association;
        Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
	@Override public List<AnnotationAttribute> getAnnotationAttributes () {
	    return new ArrayList<AnnotationAttribute>(annotationAttributes.values());}
	@Override public List<AnnotationElement> getAnnotationElements () {
	    return new ArrayList<AnnotationElement>(annotationElements.values());}
	public AtomicAssociationEnd (AtomicAssociation a, DatabaseMetaData m, ResultSet r, boolean end1) throws SQLException {
	    super();
	    this.association = a;
	    setRole(association.schema.getEntityContainer(r.getString(PKTABLE_SCHEM)).getEntitySet(end1 ? r.getString(PKTABLE_NAME) : r.getString(FKTABLE_NAME)).getName());
	    setMultiplicity(EdmMultiplicity.MANY);
	    setType(association.schema.getEntityContainer(r.getString(PKTABLE_SCHEM)).getEntitySet(end1 ? r.getString(PKTABLE_NAME) : r.getString(FKTABLE_NAME)).getEntityType());
	    setOnDelete(new AtomicOnDelete(this, m, r));}}

    class AtomicOnDelete extends OnDelete {
	AtomicAssociationEnd associationEnd;
        Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
	@Override public List<AnnotationAttribute> getAnnotationAttributes () {
	    return new ArrayList<AnnotationAttribute>(annotationAttributes.values());}
	@Override public List<AnnotationElement> getAnnotationElements () {
	    return new ArrayList<AnnotationElement>(annotationElements.values());}
	public AtomicOnDelete (AtomicAssociationEnd e, DatabaseMetaData m, ResultSet r) throws SQLException {
	    super();
	    this.associationEnd = e;
	    setAction("importedKeyCascade".equals(r.getString(DELETE_RULE)) ? EdmAction.Cascade : EdmAction.None);}}

    class AtomicAnnotationAttribute extends AnnotationAttribute {}

    class AtomicAnnotationElement extends AnnotationElement {}

    class AtomicComplexType extends ComplexType {}

    class AtomicUsing extends Using {}

    class AtomicEntityType extends EntityType {
	AtomicSchema schema;
        public AtomicEntityType (AtomicSchema s, DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    super();
	    this.schema = s;
            setName(r.getString(TABLE_NAME));
            setDocumentation(makeDocumentation(m, r.getString(TABLE_CAT), r.getString(TABLE_SCHEM), r.getString(TABLE_NAME)));
            setProperties(makeProperties(m, r.getString(TABLE_CAT), r.getString(TABLE_SCHEM), r.getString(TABLE_NAME)));
	    setNavigationProperties(new ArrayList<NavigationProperty>());
            setKey(makeKey(m, r.getString(TABLE_CAT), r.getString(TABLE_SCHEM), r.getString(TABLE_NAME)));}}

    class AtomicEntityContainer extends EntityContainer {
	AtomicSchema schema;
        Map<String, AtomicEntitySet> entitySets = new HashMap<>();
        public AtomicEntityContainer (AtomicSchema s, DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    super();
	    this.schema = s;
            setName(r.getString(TABLE_SCHEM));}
	@Override public List<EntitySet> getEntitySets () {
	    return new ArrayList<EntitySet>(entitySets.values());}
	EntitySet getEntitySet (String name) {
	    return entitySets.get(name);}
        void addEntitySet (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    if (!entitySets.containsKey(r.getString(TABLE_NAME))) {
                AtomicEntitySet es = new AtomicEntitySet(this, m, r);
                entitySets.put(es.getName(), es);}}}

    class AtomicEntitySet extends EntitySet {
	AtomicEntityContainer entityContainer;
        public AtomicEntitySet (AtomicEntityContainer ec, DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    super();
	    this.entityContainer = ec;
            setName(r.getString(TABLE_NAME));
            setEntityType(new FullQualifiedName(String.format("%s.%s", r.getString(TABLE_CAT), r.getString(TABLE_SCHEM)), r.getString(TABLE_NAME)));}}

    @Override public List<Schema> getSchemas () throws ODataException {
	try (Connection conn = getConn()) {return new ArrayList<Schema>((new AtomicRoot(conn.getMetaData())).schemas.values());}
        catch (Throwable e) {throw new ODataException(e);}}}
