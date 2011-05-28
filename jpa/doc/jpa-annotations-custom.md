---

layout: doc
title: Custom Database.com Annotations

---
# Custom Database.com Annotations

As well as supporting standard JPA annotations, Database.com has defined custom annotations for behavior specific to Database.com:

<dl>
  <dt>@CustomObject</dt>
    <dd>Use <code>@CustomObject</code> to enable feed tracking in Chatter for a new custom object (Database.com entity) or mark it as read only.</dd>
    <dd><a href="#AnnotationCustomObject">Read More</a></dd>
  
  <dt>@CustomField</dt>
    <dd>Use <code>@CustomField</code> to set some of the attributes of a custom field when you create a field in a Java class that maps to
a new custom field in Database.com.</dd>
    <dd><a href="#AnnotationCustomField">Read More</a></dd>

  <dt>@PicklistValue</dt>
    <dd>Use <code>@PicklistValue</code> to define which enum values are available by default in a non-restricted multi-select picklist.</dd>
    <dd><a href="force-datatypes#enumFields">Read More</a></dd>
  
  <dt>@JoinFilter</dt>
    <dd>Use <code>@JoinFilter</code> to include a WHERE clause to filter the returned child collection or map in an implicit JPQL query
join.</dd>
    <dd><a href="jpa-queries#AnnotationJoinFilter">Read More</a></dd>
</dl>

<a name="AnnotationCustomObject"> </a>
## @CustomObject

Use <code>@CustomObject</code> to enable feed tracking in Chatter for a custom object (Database.com entity) or mark it as read only.

### Enabling Feed Tracking
Enable objects for feed tracking
so people can follow records of that object type and see Chatter feed updates when records of that object type are created. For
example, if you enable feed tracking for a Student custom object, users can follow Student records and see feed updates when
they create students.

To create a new custom object and enable feed tracking for it:

- Add an <code>@Entity</code> annotation to the class. The custom object is not created if you don't include this annotation, as well as
the <code>@CustomObject</code> annotation.
- Set the <code>enableFeeds</code> attribute of <code>@CustomObject</code> to true. The default value for this attribute is false.

For example:

    @Entity
    @CustomObject(enableFeeds = true)
    public class Student
    {
        // class details here
    }
    
The custom object is created when your application starts and the Java class loads. When the custom object is created, custom
fields corresponding to all of the fields in the class are also created.

Note: If you add a <code>@CustomObject</code> annotation to a Java class that already exists as a custom object in Database.com, the <code>@CustomObject</code>
annotation is ignored. For example, if a Student custom object already exists in your organization and feed tracking in Chatter
is not enabled for it, adding <code>@CustomObject(enableFeeds = true)</code> to the Student Java class does not enable feed
tracking. This annotation only affects feed tracking when a new custom object is created.

### Marking Object (Entity) Read-Only
Use the <code>readOnlySchema</code> attribute to mark an entity as read only. If you set <code>readOnlySchema = true</code>, it ensures that the schema will not be deleted when the **force.purgeOnDelete** property is set in your application's `persistence.xml` file. 

<a name="AnnotationCustomField"> </a>
## @CustomField

Use <code>@CustomField</code> to set some of the attributes of a custom field when you create a field in a Java class that maps to a new
custom field in Database.com. For example, you can set the name, description, and length of the new custom field. If the custom
field already exists, the <code>@CustomField</code> annotation has no effect.

The custom field attributes that you can set are:

<table cellpadding="4" cellspacing="0" border="1">

<thead align="left">
<tr>
<th width="24.129930394431554%" >Custom Field Attribute</th>

<th width="11.1368909512761%" >Field Type</th>

<th width="41.06728538283062%" >Description</th>

<th width="23.665893271461716%" >Default Value</th>

</tr>

</thead>

<tbody>
<tr>
<td width="24.129930394431554%" >childRelationshipName</td>

<td width="11.1368909512761%" >String</td>

<td width="41.06728538283062%" >The child relationship name for lookup or master-detail fields.
If a value is not specified for this field, it is automatically set
to <em>ParentObjectName</em>_<em>ChildObjectName</em>s__r, where <em>ParentObjectName</em> is the parent object for a lookup field
or the master object for a master-detail field. If this name is more
than 40 characters, it is truncated.<p>
For more information about
relationship names, see the <cite><a href="http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_calls_soql_relationships.htm#understanding_relationships" target="_blank" title="HTML(New Window)">Web Services API Developer's Guide</a></cite>.</p>
</td>

<td width="23.665893271461716%" >See the Description column.</td>

</tr>

<tr>
<td width="24.129930394431554%" >description</td>

<td width="11.1368909512761%" >String</td>

<td width="41.06728538283062%" >Description of the field.</td>

<td width="23.665893271461716%" >""</td>

</tr>

<tr>
<td width="24.129930394431554%" >enableFeeds</td>

<td width="11.1368909512761%" >boolean</td>

<td width="41.06728538283062%" >Indicates whether the field
is enabled for feed tracking (true) or not (false). To set this field to true, the enableFeeds attribute on the object containing
this field must also be true.</td>

<td width="23.665893271461716%" >false</td>

</tr>

<tr>
<td width="24.129930394431554%" >externalId</td>

<td width="11.1368909512761%" >boolean</td>

<td width="41.06728538283062%" >Indicates whether the field
is an external ID field (true) or not (false).</td>

<td width="23.665893271461716%" >false</td>

</tr>

<tr>
<td width="24.129930394431554%" >label</td>

<td width="11.1368909512761%" >String</td>

<td width="41.06728538283062%" >Label for the field.</td>

<td width="23.665893271461716%" >""</td>

</tr>

<tr>
<td width="24.129930394431554%" >length</td>

<td width="11.1368909512761%" >int</td>

<td width="41.06728538283062%" >Length of the field.</td>

<td width="23.665893271461716%" ></td>

</tr>

<tr>
<td width="24.129930394431554%" >name</td>

<td width="11.1368909512761%" >String</td>

<td width="41.06728538283062%" >The name of the field used for API access.</td>

<td width="23.665893271461716%" >""</td>

</tr>

<tr>
<td width="24.129930394431554%" >precision</td>

<td width="11.1368909512761%" >int</td>

<td width="41.06728538283062%" >The precision for number values.
Precision is the number of digits in a number. For example, the number
256.99 has a precision of 5.</td>

<td width="23.665893271461716%" ></td>

</tr>

<tr>
<td width="24.129930394431554%" >scale</td>

<td width="11.1368909512761%" >int</td>

<td width="41.06728538283062%" >The scale for the field. Scale is
the number of digits to the right of the decimal point in a number.
For example, the number 256.99 has a scale of 2.</td>

<td width="23.665893271461716%" ></td>

</tr>

<tr>
<td width="24.129930394431554%" >type</td>

<td width="11.1368909512761%" >String</td>

<td width="41.06728538283062%" >The field type for the field. The valid values are:<ul>
<li>FieldType.AutoNumber</li>
<li>FieldType.Lookup</li>
<li>FieldType.MasterDetail</li>
<li>FieldType.Checkbox</li>
<li>FieldType.Currency</li>
<li>FieldType.Date</li>
<li>FieldType.DateTime</li>
<li>FieldType.Email</li>
<li>FieldType.EncryptedText</li>
<li>FieldType.Number</li>
<li>FieldType.Percent</li>
<li>FieldType.Phone</li>
<li>FieldType.Picklist</li>
<li>FieldType.MultiselectPicklist</li>
<li>FieldType.Summary</li>
<li>FieldType.Text</li>
<li>FieldType.TextArea</li>
<!-- Not currently supported<li>FieldType.LongTextArea</li>-->
<li>FieldType.Summary</li>
<li>FieldType.Url</li>
<li>FieldType.Hierarchy</li>
<li>FieldType.File</li>
<li>FieldType.CustomDataType</li>
<li>FieldType.Html</li>

</ul>
<p>
For more information about field types, see <a href="http://na1.salesforce.com/help/doc/en/custom_field_types.htm">Custom Field Types</a>.</p>
</td>

<td width="23.665893271461716%" >Text</td>

</tr>

</tbody>

</table>

## Sample Class Using Custom Annotations

The following sample Java class uses custom Database.com JPA annotations.

    import java.util.Date;
    import javax.persistence.*;
    
    @Entity
    @CustomObject(enableFeeds = true)
    public class Student
    {
        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        String id;
    
        @CustomField(type = "Text", label="First Name", length=100)
        private String firstName;
    
        @CustomField(type = "Text", label="Last Name", length=100)
        private String lastName;
    
        @CustomField(type = "Text", label="Email", length=255)
        private String email;
    
        @CustomField(type = "Date", label="Date of Birth")
        private Date dateOfBirth;
    
        // Getters and setters not used by JPA.
        // They are used by your code when you are persisting records.
    
        public String getId() {
            return id;
    
        public String getFirstName() {
            return firstName;
        } 
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        } 
    
        public String getLastName() {
            return lastName;
        } 
        public void setLastName(String lastName) {
            this.lastName = lastName;
        } 
    
        public String getEmail() {
            return email;
        } 
        public void setEmail(String email) {
            this.email = email;
        } 
    
        public Date getDateOfBirth() {
            return dateOfBirth;
        } 
        public void setDateOfBirth(Date dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        } 
    
    }
    
## Mixing Standard and Custom Annotations

You can mix standard JPA and custom Database.com annotations in the same Java class. However, custom annotations take
precedence if there is an overlap in functionality. In the following example, the <code>@CustomField</code> annotation takes
precedence over the standard JPA <code>@Column</code> annotation. The counter field gets a precision of 11 and a scale of 0.

    @CustomField(precision=11, scale=0)
    @Column(precision=17, scale=0)
    private int counter;
