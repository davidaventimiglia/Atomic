package org.neptunestation.atomic.core;

import java.sql.*;
import java.util.*;
import javax.naming.*;
import org.apache.olingo.odata2.api.edm.*;
import org.apache.olingo.odata2.api.edm.provider.*;
import org.apache.olingo.odata2.api.exception.*;

public class BottomUpAtomicEdmProvider extends AtomicEdmProvider {
    public BottomUpAtomicEdmProvider (Properties params, String username, String password) {
        super(params, username, password);}

    public class AtomicRoot {
        private Map<String, AtomicSchema> schemas = new HashMap<>();
        public AtomicRoot (DatabaseMetaData m) throws NamingException, SQLException {
	    try (ResultSet r = m.getTablePrivileges(null, null, null)) {while (r.next()) addOrUpdateSchema(m, r);}
	    try (ResultSet r = m.getCrossReference(null, null, null, null, null, null)) {while (r.next()) addOrUpdateAssociation(m, r);}
	    for (Schema s : getSchemas()) for (EntityContainer ec : s.getEntityContainers()) {ec.setDefaultEntityContainer(true); break;}}
	public void addOrUpdateAssociation (DatabaseMetaData m, ResultSet r) throws SQLException {
	    schemas.get(r.getString(2)).addOrUpdateEnd1Association(m, r);
	    schemas.get(r.getString(6)).addOrUpdateEnd2Association(m, r);}
        public void addOrUpdateSchema (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    if (!schemas.containsKey(r.getString(2))) {
                AtomicSchema s = new AtomicSchema(m, r);
                schemas.put(s.getNamespace(), s);}
            schemas.get(r.getString(2)).addOrUpdateEntityContainer(m, r);
            if (m.getUserName().equals(r.getString(5)) && "select".equalsIgnoreCase(r.getString(6))) schemas.get(r.getString(2)).addOrUpdateEntityType(m, r);}
        public List<Schema> getSchemas () {
            return new ArrayList<Schema>(schemas.values());}}

    public class AtomicSchema extends Schema {
        private Map<String, AtomicEntityContainer> entityContainers = new HashMap<>();
        private Map<String, AtomicEntityType> entityTypes = new HashMap<>();
        private Map<String, AtomicAssociation> associations = new HashMap<>();
        public AtomicSchema (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    super();
	    setAnnotationAttributes(new ArrayList<AnnotationAttribute>());
	    setAnnotationElements(new ArrayList<AnnotationElement>());
	    setAssociations(new ArrayList<Association>());
	    setComplexTypes(new ArrayList<ComplexType>());
	    setEntityContainers(new ArrayList<EntityContainer>());
	    setEntityTypes(new ArrayList<EntityType>());
	    setUsings(new ArrayList<Using>());
            setNamespace("" + r.getString(2));}
	public void addOrUpdateEnd1Association (DatabaseMetaData m, ResultSet r) throws SQLException {
	    if (!associations.containsKey(r.getString(2))) {
		AtomicAssociation a = new AtomicAssociation(m, r);
		associations.put(a.getName(), a);
		getAssociations().add(a);}}
	public void addOrUpdateEnd2Association (DatabaseMetaData m, ResultSet r) throws SQLException {
	    if (!associations.containsKey(r.getString(6))) {
		AtomicAssociation a = new AtomicAssociation(m, r);
		associations.put(a.getName(), a);
		getAssociations().add(a);}}
	public void addOrUpdateComplexTypes () {}
        public void addOrUpdateEntityContainer (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    if (!entityContainers.containsKey(r.getString(2))) {
                AtomicEntityContainer ec = new AtomicEntityContainer(m, r);
                entityContainers.put(ec.getName(), ec);
                getEntityContainers().add(ec);}
            if (m.getUserName().equals(r.getString(5)) && "select".equalsIgnoreCase(r.getString(6))) entityContainers.get(r.getString(2)).addOrUpdateEntitySet(m, r);}
        public void addOrUpdateEntityType (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    if (!entityTypes.containsKey(r.getString(2))) {
                AtomicEntityType et = new AtomicEntityType(m, r);
                entityTypes.put(et.getName(), et);
                getEntityTypes().add(et);}}}

    public class AtomicAssociation extends Association {
	public AtomicAssociation (DatabaseMetaData m, ResultSet r) throws SQLException {
	    super();
	    setName(r.getString(12));
	    setEnd1(new AtomicAssociationEnd());
	    setEnd2(new AtomicAssociationEnd());
	    setReferentialConstraint(new AtomicReferentialConstraint());
	}}

    public class AtomicReferentialConstraint extends ReferentialConstraint {
	public AtomicReferentialConstraint () {
	    super();
	    setAnnotationAttributes(new ArrayList<AnnotationAttribute>());
	    setAnnotationElements(new ArrayList<AnnotationElement>());
	    setDocumentation(new Documentation());
	    setDependent(new AtomicReferentialConstraintRole());
	    setPrincipal(new AtomicReferentialConstraintRole());}}

    public class AtomicReferentialConstraintRole extends ReferentialConstraintRole {
	public AtomicReferentialConstraintRole () {
	    super();
	    setAnnotationAttributes(new ArrayList<AnnotationAttribute>());
	    setAnnotationElements(new ArrayList<AnnotationElement>());
	    setRole("foo");
	    setPropertyRefs(new ArrayList<PropertyRef>());}}

    public class AtomicAssociationEnd extends AssociationEnd {
	public AtomicAssociationEnd () {
	    super();
	    setAnnotationAttributes(new ArrayList<AnnotationAttribute>());
	    setAnnotationElements(new ArrayList<AnnotationElement>());
	    setDocumentation(new Documentation());
	    setMultiplicity(EdmMultiplicity.MANY);
	    setOnDelete(new AtomicOnDelete());
	    setRole("foo");
	    setType(new FullQualifiedName("foo", "bar"));}}

    public class AtomicOnDelete extends OnDelete {
	public AtomicOnDelete () {
	    super();
	    setAction(EdmAction.Cascade);
	    setAnnotationAttributes(new ArrayList<AnnotationAttribute>());
	    setAnnotationElements(new ArrayList<AnnotationElement>());
	    setDocumentation(new Documentation());}}

    public class AtomicComplexType extends ComplexType {
	public AtomicComplexType () {super();}}

    public class AtomicEntityType extends EntityType {
        public AtomicEntityType (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    super();
            setName(r.getString(3));
            setDocumentation(makeDocumentation(m, r.getString(1), r.getString(2), r.getString(3)));
            setProperties(makeProperties(m, r.getString(1), r.getString(2), r.getString(3)));
            setKey(makeKey(m, r.getString(1), r.getString(2), r.getString(3)));}}

    public class AtomicEntityContainer extends EntityContainer {
        private Map<String, AtomicEntitySet> entitySets = new HashMap<>();
        public AtomicEntityContainer (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    super();
	    setEntitySets(new ArrayList<EntitySet>());
            setName("" + r.getString(2));}
        public void addOrUpdateEntitySet (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    if (!entitySets.containsKey(r.getString(3))) {
                AtomicEntitySet es = new AtomicEntitySet(m, r);
                entitySets.put(es.getName(), es);
                getEntitySets().add(es);}}}

    public class AtomicEntitySet extends EntitySet {
        public AtomicEntitySet (DatabaseMetaData m, ResultSet r) throws NamingException, SQLException {
	    super();
            setName(r.getString(3));
            setEntityType(new FullQualifiedName(String.format("%s.%s", r.getString(1), r.getString(2)), r.getString(3)));}}

    @Override public List<Schema> getSchemas () throws ODataException {
	try (Connection conn = getConn()) {return (new AtomicRoot(conn.getMetaData())).getSchemas();}
        catch (Throwable e) {throw new ODataException(e);}}}
