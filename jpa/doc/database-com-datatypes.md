---

layout: doc
title: Force.com Data Types

---
# Force.com Data Types

Force.com has its own built-in list of data types optimized for building business applications. To see how these data types map to standard Java data types, see [Mapping Force.com to Java Data Types](java-db-com-datatypes-map#mapForceToJava).

**Note**: Field names in Java classes must start with a lowercase letter to match bean-naming conventions. For example, use <code>String id</code> instead of <code>String Id</code>.

For information on primary key fields, see [Primary Keys](jpa-provider#primaryKeys).

The rest of this section highlights useful information about a subset of Force.com data types. For a list of all the data types, see [Mapping Force.com to Java Data Types](java-db-com-datatypes-map#mapForceToJava).

- [Text Fields](#text)
- [Number Fields](#number)
- [Auto Number Fields](#autoNumber)
- [Currency Fields](#currency)
- [Date Fields](#date)
- [Enumeration (Picklist) Fields](#picklist)
- [Restricted Versus Non-Restricted Picklists](#restrictedPicklist)
- [Email Fields](#email)
- [Phone Fields](#phone)
- [Percent Fields](#percent)
- [Formula Fields](#formula)
- [Relationship Fields](#relFields)
- [Binary (Base64) Fields](#binaryFields)

<a name="text"> </a>
## Text Fields
The Force.com Text data type is used for string and character fields.

The length of Text fields is controlled by the length attribute of the <code>@Column</code> annotation. For example:

    @Column(length=1)
    String flag;

This code defines a Text field called flag of length 1. The default length is 255.

<a name="number"> </a>
## Number Fields
The Force.com Number data type is used for numbers of various sizes. The precision and scale is set by the <code>@Column</code> annotation.
For example:

    @Column(precision=1, scale=0)
    int digit;

This code defines an integer that can range from 0 to 9. The default precision and scale are 18 and 0 for integers and 18 and
2 for floats and doubles. For more information, see [Mapping Force.com to Java Data Types](java-db-com-datatypes-map#mapForceToJava).

<a name="autoNumber"> </a>
## Auto Number Fields
To create a Force.com auto number field, use <code>@CustomField(type=FieldType.AutoNumber</code>) with an int or Integer
Java type. For example:

    @CustomField(type=FieldType.AutoNumber)
    int referenceNum;

The default start value for the number is 0. Use the optional <code>startValue</code> attribute for a different start value. Auto number fields are read only.

<a name="currency"> </a>
## Currency Fields
To create a Force.com currency field, use a BigDecimal Java type. You can also use another number type in Java, but you must add a  <code>@CustomField(type=FieldType.Currency)</code> annotation. For example:

    @CustomField(type=FieldType.Currency)
    double revenue;

<a name="date"> </a>
## Date Fields
Force.com has two date types: Date and Date/Time. Date doesn't include hours, minutes, and seconds. By default, the
Database.com JPA provider maps <code>java.util.Calendar</code> to Date/Time and <code>java.util.Date</code> to Date. You can customize
this behavior using the JPA <code>@Temporal</code> annotation. For example, the following two Java fields are both mapped to a Date/Time
field in Force.com.

    java.util.Calendar lastUpdated;

    @Temporal(TemporalType.TIMESTAMP)
    java.util.Date dateCreated;

Similarly, you can use the <code>@Temporal</code> annotation to map a <code>java.util.Calendar</code> to a Date field. This example maps
both Java fields to Date fields.

    java.util.Date birthDate;

    @Temporal(TemporalType.DATE)
    java.util.Calendar transactionDate;

The <code>java.util.GregorianCalendar</code> class can be used instead of <code>java.util.Calendar</code>.

<a name="picklist"> </a>
## Enumeration (Picklist) Fields
Force.com supports enumerations as a data type. Enumerations are referred to as picklists and multi-select picklists in Force.com
terminology. Define picklist values by using a Java enumeration. For example:

    public enum Varietal { Cabernet_Sauvignon, Syrah, Pinot_Noir, Zinfandel }

    @Enumerated
    private Varietal varietal;

By default, Force.com stores ordinal values for the selected value. For example, if you set the varietal variable to Syrah, the
value 1 is stored in the database. To store the value strings instead of ordinals, use <code>@Enumerated(EnumType.STRING)</code>.

    public enum Varietal { Cabernet_Sauvignon, Syrah, Pinot_Noir, Zinfandel }

    @Enumerated(EnumType.STRING)
    private Varietal varietal;

Force.com also supports multi-value selections. To pick multiple varietals for a single wine, use an array type instead.

    public enum Varietal { Cabernet_Sauvignon, Syrah, Pinot_Noir, Zinfandel }

    @Enumerated(EnumType.STRING)
    private Varietal[] varietal;

<a name="restrictedPicklist"> </a>
## Restricted Versus Non-Restricted Picklists
Force.com supports two types of picklists: restricted and non-restricted. A restricted picklist only allows valid enumeration
values. The field type should match the type of the enum representing the picklist values for picklists or an array of the enum
type for multi-select picklists. For example:

    public enum Varietal { Cabernet_Sauvignon, Syrah, Pinot_Noir, Zinfandel }

    @Enumerated(EnumType.STRING)
    private Varietal varietal;

    // If this was multi-select, type would be Varietal[] instead
    //private Varietal[] varietal;

A non-restricted picklist allows any string value and is not restricted to the enumeration values. Instead of using the enum
type, the field type is a String or String[] for picklists and multi-select picklists respectively. To define which enum values
available by default, use the custom <code>@PicklistValue</code> annotation. For example:

    public enum Varietal { Cabernet_Sauvignon, Syrah, Pinot_Noir, Zinfandel }

    @CustomField(type=FieldType.Picklist)
    // You could use @Enumerated instead of the @CustomField declaration
    //@Enumerated(EnumType.STRING)
    @PicklistValue(Varietal.class)
    // note the field type is String
    private String varietal;

    // If this was multi-select, type would be String[] instead
    //private String[] varietal;

Since non-restricted picklists are not limited to enum values, they don't support <code>@Enumerated(EnumType.ORDINAL)</code>. We recommend that you use <code>@Enumeration</code> for restricted picklists and <code>@CustomField(type=FieldType.Picklist)</code> for
non-restricted picklists.

<a name="email"> </a>
## Email Fields
To create a Force.com email field, use <code>@CustomField(type=FieldType.Email)</code> with a String Java type. For example:

    @CustomField(type=FieldType.Email)
    String orderEmail;

<a name="phone"> </a>
## Phone Fields
To create a Force.com phone field, use <code>@CustomField(type=FieldType.Phone)</code> with a String Java type. For example:

    @CustomField(type=FieldType.Email)
    String orderEmail;

<a name="percent"> </a>
## Percent Fields
To create a Force.com percent field, use <code>@CustomField(type=FieldType.Percent)</code> with any Java type that [maps to a Force.com Number field](java-db-com-datatypes-map), such as int or double. For example:

    @CustomField(type=FieldType.Percent)
    int mainVarietalPercent;

<a name="formula"> </a>
## Formula Fields
A formula is a field whose value is derived from other fields, expressions, or values. For more information about formula fields,
see [Building Formulas](http://na1.salesforce.com/help/doc/en/customize_formulas.htm).

To create a Force.com formula field, use a <code>@CustomField</code> annotation with a String Java type. For example:

    @CustomField(type=FieldType.Text, formula="formulaValue")
    String snazzyFormula;

Formula fields are read only. A formula can return a Currency, Date, DateTime, Number, Percent, or Text FieldType. To see how these types map to Java data types, see [Mapping Force.com to Java Data Types](java-db-com-datatypes-map#mapForceToJava).

Use the optional <code>treatBlankAs</code> attribute to control how blank values are interpreted. The default value is
<code>TreatBlanksAs.BlankAsBlank</code>.

<a name="relFields"> </a>
## Relationship Fields
Force.com supports two types of relationships between entities: Lookup and Master-Detail. You can perform
join queries on entities connected by a relationship field.

### Master-Detail
A master-detail relationship links entities so that the master record controls certain behaviors of any detail records. For
example, when a master record is deleted, the related detail records are also deleted. The ownership and security settings
of a detail record are determined by the master record and can't be changed for a detail record.

### Lookup
A lookup relationship is a foreign key to another entity, but it has no effect on deletion, record ownership, or security
settings.

For more details on working with Lookup and Master-Detail fields, see [Overview of Relationships](http://na1.salesforce.com/help/doc/en/overview_of_custom_object_relationships.htm). 

Define Lookup or Master-Detail relationship fields in Java classes by using a <code>@ManyToOne</code> annotation on the field in the
class on the many side of a relationship and a <code>@OneToMany</code> annotation on the field in the class on the one side of the relationship.
The following sections include code examples for Lookup and Master-Detail fields.

### @ManyToOne Annotation
Use the <code>@ManyToOne</code> annotation on a field to model the many side of a relationship. By default, the Database.com JPA provider
maps this annotation to a Lookup relationship field. Use an <code>@CustomField(type = FieldType.MasterDetail)</code>
annotation to define it as a Master-Detail relationship field instead.

For example, if a Wine Java entity has a many-to-one relationship to a Producer entity, the relationship is defined in the Wine
entity as a producer Lookup field with a <code>@ManyToOne</code> annotation.

    @ManyToOne
    private Producer producer;

To set the Lookup field name in Force.com to be something other than producer, use the <code>@Column</code> annotation.

    @Column(name = "wine_producer")
    @ManyToOne
    private Producer producer;

Change the Lookup field to a Master-Detail field by including an <code>@CustomField(type = FieldType.MasterDetail)</code> annotation.

    @ManyToOne
    @CustomField(type = FieldType.MasterDetail)
    private Producer producer;

### @OneToMany Annotation
Use the <code>@OneToMany</code> annotation to model the one side of a relationship. Every <code>@OneToMany</code> annotation must have a field
with a <code>@ManyToOne</code> annotation on the many side of the relationship. The Database.com JPA provider requires that an <code>@OneToMany</code>
annotation must include a <code>mappedBy</code> attribute with a value matching the foreign key field name in the related JPA entity.

For example, a Producer entity with a one-to-many relationship to the Wine entity could point to a collection of wines. The
<code>mappedBy</code> attribute value is producer to match the field name of the Lookup field in the Wine entity.

    @OneToMany(mappedBy="producer")
    private Collection<Wine> wines;

To automatically persist all child records in a collection when you persist the parent record, see the <code>CascadeType.PERSIST</code>
attribute for the parent <code>@OneToMany</code> field. You must explicitly set the parent field for each child record in the collection. In
this example, you must set the producer field for each Wine in the collection.

The Database.com JPA provider supports List, Set, and Map collections for an <code>@OneToMany</code> field on the one-side of the
relationship.The Database.com JPA provider dynamically builds these collections and doesn't explicitly store them in Database.com.

The default key for a Map is the id field. The value of the id field for a new object is <code>null</code> until the object is persisted to 
Database.com so you can only insert one new entry into a map using the default key. The alternative is to use the <code>@MapKey</code>
standard annotation to set a different field as the key. Entries must have a unique key value. To continue the earlier example,
the Producer entity could use a map to track the related wines. The key of the map is the wine name.

    @OneToMany(mappedBy="producer")
    @MapKey(name="wineName")
    private Map<String, Wine> wines;

### Unsupported JPA Annotations
The Database.com JPA provider doesn't support:

* **One-to-one relationships using the <code>@OneToOne</code> annotation**—Merge the entities into one entity instead.
* **Many-to-many relationships using the <code>@ManyToMany</code> annotation**—Use a custom junction entity with two <code>@ManyToOne</code>
relationship fields connecting the two entities instead.

<a name="cascade"> </a>
### Cascading Persistence and Deletion
JPA defines attributes that control cascading behavior for relationship fields. For example, if record A references record B, a
cascading deletion of record A would also delete record B. The Database.com JPA provider supports a subset of the JPA cascading
attributes for <code>@OneToMany</code> fields and ignores any cascading attributes for <code>@ManyToOne</code> fields. The supported attributes for
<code>@OneToMany</code> fields are:

#### CascadeType.PERSIST
When this attribute is enabled, persisting a parent record also persists all child records in its collection if you explicitly
set the parent field for each child record in the collection. Otherwise, it is up to you to persist child records individually
as well as the parent in the same transaction.

#### CascadeType.REMOVE
When this attribute is enabled for Lookup fields, removing a parent entity also removes all child entities in its collection.
Otherwise, it is up to you to remove child entities individually. This attribute is always enabled for Master-Detail fields.
Removing a parent Master-Detail always removes its children.

#### CascadeType.ALL
Shorthand for enabling <code>CascadeType.PERSIST</code> and <code>CascadeType.REMOVE</code>.
The following JPA attributes are not configurable for <code>@OneToMany</code> fields. This list explains the behavior related to these
attributes for the Database.com JPA provider.

#### CascadeType.MERGE
Merging (updating) a parent entity always merges the child entities in its collection.

#### CascadeType.REFRESH
Refreshing a parent entity always refreshes the child entities in its collection.

#### CascadeType.DETACH
Detaching a parent always detaches the child entities in its collection.

For more information on cascading attributes, see the [DataNucleus documentation](http://www.datanucleus.org/products/accessplatform.html).

<a name="binaryFields"> </a>
## Binary (Base64) Fields
Force.com only supports binary values for [Attachment](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_objects_attachment.htm), [Document](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_objects_document.htm), or [Scontrol](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_objects_scontrol.htm) standard objects. You can't use byte[] fields
to represent binary (blob) data in custom Database.com entities.

In Java classes representing Attachment, Document, or Scontrol standard objects, use a byte[] field for the base64–encoded
data in the Body or Binary field. The BodyLength field defines the length of the data in the Body or Binary field. In the
Document object, you can specify a URL to the document instead of storing the binary data directly in the record.

A sample Java class representing a Document includes body and bodyLength fields:

    public class Document {
        // base64-encoded binary data
        private byte[] body;
        // size of the binary data in bytes
        private int bodyLength;
        
        // more fields
    }
