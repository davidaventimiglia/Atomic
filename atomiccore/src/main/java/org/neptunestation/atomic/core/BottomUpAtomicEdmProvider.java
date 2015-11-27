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

    public class AtomicRoot {
        private Map<String, AtomicSchema> schemas = new HashMap<>();
        public AtomicRoot (DatabaseMetaData m) throws NamingException, SQLException {
	    try (ResultSet r = m.getTablePrivileges(null, null, null)) {while (r.next()) addSchema(this, m, r);}
	    try (ResultSet r = m.getCrossReference(null, null, null, null, null, null)) {while (r.next()) addAssociation(getSchema(r.getString(PKTABLE_SCHEM)), m, r);}
	    for (Schema s : getSchemas()) for (EntityContainer ec : s.getEntityContainers()) {ec.setDefaultEntityContainer(true); break;}}
	public void addAssociation (AtomicSchema s, DatabaseMetaData m, ResultSet r) throws SQLException {
	    schemas.get(r.getString(PKTABLE_SCHEM)).addAssociation(s, m, r);}
        public void addSchema (AtomicRoot root, DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    if (!schemas.containsKey(r.getString(TABLE_SCHEM))) {
                AtomicSchema s = new AtomicSchema(root, m, r);
                schemas.put(s.getNamespace(), s);}
            schemas.get(r.getString(TABLE_SCHEM)).addEntityContainer(m, r);
            if (m.getUserName().equals(r.getString(GRANTEE)) && "select".equalsIgnoreCase(r.getString(PRIVILEGE))) schemas.get(r.getString(TABLE_SCHEM)).addEntityType(m, r);}
	public AtomicSchema getSchema (String name) {
	    return schemas.get(name);}
        public List<Schema> getSchemas () {
            return new ArrayList<Schema>(schemas.values());}}

    public class AtomicSchema extends Schema {
	public AtomicRoot root;
        private Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        private Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
        private Map<String, AtomicAssociation> associations = new HashMap<>();
        private Map<String, AtomicComplexType> complexTypes = new HashMap<>();
        private Map<String, AtomicEntityContainer> entityContainers = new HashMap<>();
        private Map<String, AtomicEntityType> entityTypes = new HashMap<>();
        private Map<String, AtomicUsing> usings = new HashMap<>();
        public AtomicSchema (AtomicRoot root, DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    super();
	    this.root = root;
            setNamespace("" + r.getString(TABLE_SCHEM));}
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
	public AtomicEntityType getEntityType (String name) {
	    return entityTypes.get(name);}
	public AtomicEntityContainer getEntityContainer (String name) {
	    return entityContainers.get(name);}
	public void addAssociation (AtomicSchema parent, DatabaseMetaData m, ResultSet r) throws SQLException {
	    if (!associations.containsKey(r.getString(FK_NAME))) {
		AtomicAssociation a = new AtomicAssociation(parent, m, r);
		AtomicEntityType e = entityTypes.get(a.getEnd1().getRole());
		e.getNavigationProperties().add(new AtomicNavigationProperty(a, e, m, r));
		associations.put(a.getName(), a);}}
	public void addComplexTypes () {}
        public void addEntityContainer (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    if (!entityContainers.containsKey(r.getString(TABLE_SCHEM))) {
                AtomicEntityContainer ec = new AtomicEntityContainer(m, r);
                entityContainers.put(ec.getName(), ec);}
            if (m.getUserName().equals(r.getString(GRANTEE)) && "select".equalsIgnoreCase(r.getString(PRIVILEGE)))
		entityContainers.get(r.getString(TABLE_SCHEM)).addEntitySet(m, r);}
        public void addEntityType (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    if (!entityTypes.containsKey(r.getString(TABLE_SCHEM))) {
                AtomicEntityType et = new AtomicEntityType(m, r);
                entityTypes.put(et.getName(), et);}}}

    public class AtomicNavigationProperty extends NavigationProperty {
	public EntityType parent;
        private Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        private Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
	@Override public List<AnnotationAttribute> getAnnotationAttributes () {
	    return new ArrayList<AnnotationAttribute>(annotationAttributes.values());}
	@Override public List<AnnotationElement> getAnnotationElements () {
	    return new ArrayList<AnnotationElement>(annotationElements.values());}
	public AtomicNavigationProperty (Association a, EntityType p, DatabaseMetaData m, ResultSet r) throws SQLException {
	    super();
	    this.parent = p;
	    setFromRole(p.getName());
	    setName(r.getString(PKCOLUMN_NAME));
	    setRelationship(new FullQualifiedName(r.getString(PKTABLE_SCHEM), a.getName()));
	    setToRole(r.getString(FKTABLE_NAME));}}

    public class AtomicAssociation extends Association {
	public AtomicSchema schema;
        private Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        private Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
	@Override public List<AnnotationAttribute> getAnnotationAttributes () {
	    return new ArrayList<AnnotationAttribute>(annotationAttributes.values());}
	@Override public List<AnnotationElement> getAnnotationElements () {
	    return new ArrayList<AnnotationElement>(annotationElements.values());}
	public AtomicAssociation (AtomicSchema schema, DatabaseMetaData m, ResultSet r) throws SQLException {
	    super();
	    this.schema = schema;
	    setName(r.getString(FK_NAME));
	    setEnd1(new AtomicAssociationEnd(this, m, r, true));
	    setEnd2(new AtomicAssociationEnd(this, m, r, false));
	    setReferentialConstraint(new AtomicReferentialConstraint(this, m, r, getEnd1(), getEnd2()));
	}}

    public class AtomicReferentialConstraint extends ReferentialConstraint {
	public AtomicAssociation association;
        private Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        private Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
	@Override public List<AnnotationAttribute> getAnnotationAttributes () {
	    return new ArrayList<AnnotationAttribute>(annotationAttributes.values());}
	@Override public List<AnnotationElement> getAnnotationElements () {
	    return new ArrayList<AnnotationElement>(annotationElements.values());}
	public AtomicReferentialConstraint (AtomicAssociation association, DatabaseMetaData m, ResultSet r, AssociationEnd end1, AssociationEnd end2) {
	    super();
	    this.association = association;
	    setDependent(new AtomicReferentialConstraintRole(this, m, r, end2));
	    setPrincipal(new AtomicReferentialConstraintRole(this, m, r, end1));}}

    public class AtomicReferentialConstraintRole extends ReferentialConstraintRole {
	public AtomicReferentialConstraint referentialConstraint;
        private Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        private Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
	@Override public List<AnnotationAttribute> getAnnotationAttributes () {
	    return new ArrayList<AnnotationAttribute>(annotationAttributes.values());}
	@Override public List<AnnotationElement> getAnnotationElements () {
	    return new ArrayList<AnnotationElement>(annotationElements.values());}
	@Override public List<PropertyRef> getPropertyRefs () {
	    return new ArrayList<PropertyRef>();}
	public AtomicReferentialConstraintRole (AtomicReferentialConstraint referentialConstraint, DatabaseMetaData m, ResultSet r, AssociationEnd end) {
	    super();
	    this.referentialConstraint = referentialConstraint;
	    setRole(end.getRole());
	    setPropertyRefs(new ArrayList<PropertyRef>());}}

    public class AtomicAssociationEnd extends AssociationEnd {
	public AtomicAssociation association;
        private Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        private Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
	@Override public List<AnnotationAttribute> getAnnotationAttributes () {
	    return new ArrayList<AnnotationAttribute>(annotationAttributes.values());}
	@Override public List<AnnotationElement> getAnnotationElements () {
	    return new ArrayList<AnnotationElement>(annotationElements.values());}
	public AtomicAssociationEnd (AtomicAssociation association, DatabaseMetaData m, ResultSet r, boolean end1) throws SQLException {
	    super();
	    this.association = association;
	    setRole(association.schema.getEntityContainer(r.getString(PKTABLE_SCHEM)).getEntitySet(end1 ? r.getString(PKTABLE_NAME) : r.getString(FKTABLE_NAME)).getName());
	    setMultiplicity(EdmMultiplicity.MANY);
	    setType(association.schema.getEntityContainer(r.getString(PKTABLE_SCHEM)).getEntitySet(end1 ? r.getString(PKTABLE_NAME) : r.getString(FKTABLE_NAME)).getEntityType());
	    setOnDelete(new AtomicOnDelete(m, r));}}

    public class AtomicOnDelete extends OnDelete {
        private Map<String, AtomicAnnotationAttribute> annotationAttributes = new HashMap<>();
        private Map<String, AtomicAnnotationElement> annotationElements = new HashMap<>();
	@Override public List<AnnotationAttribute> getAnnotationAttributes () {
	    return new ArrayList<AnnotationAttribute>(annotationAttributes.values());}
	@Override public List<AnnotationElement> getAnnotationElements () {
	    return new ArrayList<AnnotationElement>(annotationElements.values());}
	public AtomicOnDelete (DatabaseMetaData m, ResultSet r) throws SQLException {
	    super();
	    setAction("importedKeyCascade".equals(r.getString(DELETE_RULE)) ? EdmAction.Cascade : EdmAction.None);}}

    public class AtomicAnnotationAttribute extends AnnotationAttribute {}

    public class AtomicAnnotationElement extends AnnotationElement {}

    public class AtomicComplexType extends ComplexType {}

    public class AtomicUsing extends Using {}

    public class AtomicEntityType extends EntityType {
        public AtomicEntityType (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    super();
            setName(r.getString(TABLE_NAME));
            setDocumentation(makeDocumentation(m, r.getString(TABLE_CAT), r.getString(TABLE_SCHEM), r.getString(TABLE_NAME)));
            setProperties(makeProperties(m, r.getString(TABLE_CAT), r.getString(TABLE_SCHEM), r.getString(TABLE_NAME)));
	    setNavigationProperties(makeNavigationProperties(m, r.getString(TABLE_CAT), r.getString(TABLE_SCHEM), r.getString(TABLE_NAME)));
            setKey(makeKey(m, r.getString(TABLE_CAT), r.getString(TABLE_SCHEM), r.getString(TABLE_NAME)));}}

    public class AtomicEntityContainer extends EntityContainer {
        private Map<String, AtomicEntitySet> entitySets = new HashMap<>();
        public AtomicEntityContainer (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    super();
            setName("" + r.getString(TABLE_SCHEM));}
	@Override public List<EntitySet> getEntitySets () {
	    return new ArrayList<EntitySet>(entitySets.values());}
	public EntitySet getEntitySet (String name) {
	    return entitySets.get(name);}
        public void addEntitySet (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    if (!entitySets.containsKey(r.getString(TABLE_NAME))) {
                AtomicEntitySet es = new AtomicEntitySet(m, r);
                entitySets.put(es.getName(), es);}}}

    public class AtomicEntitySet extends EntitySet {
        public AtomicEntitySet (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    super();
            setName(r.getString(TABLE_NAME));
            setEntityType(new FullQualifiedName(String.format("%s.%s", r.getString(TABLE_CAT), r.getString(TABLE_SCHEM)), r.getString(TABLE_NAME)));}}

    @Override public List<Schema> getSchemas () throws ODataException {
	try (Connection conn = getConn()) {return (new AtomicRoot(conn.getMetaData())).getSchemas();}
        catch (Throwable e) {throw new ODataException(e);}}}
