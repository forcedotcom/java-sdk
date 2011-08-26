---

layout: doc
title: Data Type Mappings

---
# Data Type Mappings

Database.com has its own built-in list of data types optimized for building business applications. This section describes how the Database.com JPA provider maps Java data types to Database.com types and vice versa.

## Mapping Java to Database.com Data Types

This table maps commonly used Java data types to data types in Database.com. It shows which Database.com data type is created when you use a Java data type. This table is intended as a quick reference for Java developers who are not familiar with data types in Database.com.

Some of the Database.com data types are created with length or precision constraints. The <code>@Column</code> annotations in the Standard JPA Annotation column represent the default constraints that are implied for each data type. You don't have to add the <code>@Column</code> standard JPA annotation when you add a Java field, but the field behaves as if they are present. You can add annotations if you want to modify the default constraints.

<table cellpadding="4" cellspacing="0" border="1">
<thead align="left">
<tr>
<th width="25.581395348837205%" >Java Data Type</th>

<th width="37.20930232558139%" >Database.com Data Type</th>

<th width="37.20930232558139%" >Standard JPA Annotation</th>

</tr>

</thead>

<tbody>
<tr>
<td width="25.581395348837205%" >boolean</td>

<td width="37.20930232558139%" >Checkbox</td>

<td width="37.20930232558139%" >N/A</td>

</tr>

<tr>
<td width="25.581395348837205%" >byte</td>

<td width="37.20930232558139%" >Text (255) <sup>1</sup></td>

<td width="37.20930232558139%" >@Column(length=255)</td>

</tr>

<tr>
<td width="25.581395348837205%" >char</td>

<td width="37.20930232558139%" >Text (255)</td>

<td width="37.20930232558139%" >@Column(length=255)</td>

</tr>

<tr>
<td width="25.581395348837205%" >short</td>

<td width="37.20930232558139%" >Number (6, 0)<sup>2</sup></td>

<td width="37.20930232558139%" >@Column(precision=6, scale=0)</td>

</tr>

<tr>
<td width="25.581395348837205%" >int</td>

<td width="37.20930232558139%" >Number (11, 0)</td>

<td width="37.20930232558139%" >@Column(precision=11, scale=0)</td>

</tr>

<tr>
<td width="25.581395348837205%" >long</td>

<td width="37.20930232558139%" >Number (18, 0)</td>

<td width="37.20930232558139%" >@Column(precision=18, scale=0)</td>

</tr>

<tr>
<td width="25.581395348837205%" >double</td>

<td width="37.20930232558139%" >Number (16, 2)</td>

<td width="37.20930232558139%" >@Column(precision=16, scale=2)</td>

</tr>

<tr>
<td width="25.581395348837205%" >float</td>

<td width="37.20930232558139%" >Number (16, 2)</td>

<td width="37.20930232558139%" >@Column(precision=16, scale=2)</td>

</tr>

<tr>
<td width="25.581395348837205%" >Boolean</td>

<td width="37.20930232558139%" >Checkbox</td>

<td width="37.20930232558139%" >N/A</td>

</tr>

<tr>
<td width="25.581395348837205%" >Byte</td>

<td width="37.20930232558139%" >Text (255)</td>

<td width="37.20930232558139%" >@Column(length=255)</td>

</tr>

<tr>
<td width="25.581395348837205%" >Character</td>

<td width="37.20930232558139%" >Text (255)</td>

<td width="37.20930232558139%" >@Column(length=255)</td>

</tr>

<tr>
<td width="25.581395348837205%" >Short</td>

<td width="37.20930232558139%" >Number (6, 0)</td>

<td width="37.20930232558139%" >@Column(precision=6, scale=0)</td>

</tr>

<tr>
<td width="25.581395348837205%" >Integer</td>

<td width="37.20930232558139%" >Number (11, 0)</td>

<td width="37.20930232558139%" >@Column(precision=11, scale=0)</td>

</tr>

<tr>
<td width="25.581395348837205%" >Long</td>

<td width="37.20930232558139%" >Number (18, 0)</td>

<td width="37.20930232558139%" >@Column(precision=18, scale=0)</td>

</tr>

<tr>
<td width="25.581395348837205%" >Double</td>

<td width="37.20930232558139%" >Number (16, 2)</td>

<td width="37.20930232558139%" >@Column(precision=16, scale=2)</td>

</tr>

<tr>
<td width="25.581395348837205%" >Float</td>

<td width="37.20930232558139%" >Number (16, 2)</td>

<td width="37.20930232558139%" >@Column(precision=16, scale=2)</td>

</tr>

<tr>
<td width="25.581395348837205%" >String</td>

<td width="37.20930232558139%" >Text (255)</td>

<td width="37.20930232558139%" >@Column(length=fieldLength)</td>

</tr>

<tr>
<td width="25.581395348837205%" >java.util.Date</td>

<td width="37.20930232558139%" >Date</td>

<td width="37.20930232558139%" >@Temporal(TemporalType.DATE)</td>

</tr>

<tr>
<td width="25.581395348837205%" >Calendar</td>

<td width="37.20930232558139%" >Date/Time</td>

<td width="37.20930232558139%" >@Temporal(TemporalType.TIMESTAMP)</td>

</tr>

<tr>
<td width="25.581395348837205%" >GregorianCalendar</td>

<td width="37.20930232558139%" >Date/Time</td>

<td width="37.20930232558139%" >@Temporal(TemporalType.TIMESTAMP)</td>

</tr>

<tr>
<td width="25.581395348837205%" >BigInteger</td>

<td width="37.20930232558139%" >Number (18, 0)</td>

<td width="37.20930232558139%" >@Column(precision=18, scale=0)</td>

</tr>

<tr>
<td width="25.581395348837205%" >BigDecimal</td>

<td width="37.20930232558139%" >Currency</td>

<td width="37.20930232558139%" >@Column(precision=16, scale=2)</td>

</tr>

<tr>
<td width="25.581395348837205%" >byte[]</td>

<td width="37.20930232558139%" >See <a href="database-com-datatypes#binaryFields">Binary (Base64) Fields</a>.</td>

<td width="37.20930232558139%" >N/A</td>

</tr>

<tr>
<td width="25.581395348837205%" >String[]</td>

<td width="37.20930232558139%" >Picklist (Multi-Select) for selected values</td>

<td width="37.20930232558139%" >@Enumerated(EnumType.STRING) OR @Enumerated(EnumType.ORDINAL)</td>

</tr>

<tr>
<td width="25.581395348837205%" >enum</td>

<td width="37.20930232558139%" >Picklist for available
values</td>

<td width="37.20930232558139%" >N/A</td>

</tr>

<tr>
<td width="25.581395348837205%" >URL</td>

<td width="37.20930232558139%" >URL</td>

<td width="37.20930232558139%" >N/A</td>

</tr>

</tbody>

</table>

<p>
<sup>1</sup> The number in parentheses represents the field length.</p>

<p>
<sup>2</sup> The values in parentheses represent the decimal precision
and decimal scale for the number. Precision
is the number of digits in a number. Scale is the number of digits
to the right of the decimal point in a number. For example, the number
256.99 has a precision of 5 and a scale of 2.</p>

<p>
<b>Note:</b> The sum of the precision and scale can't exceed 18. If it does, the precision is automatically reduced so that the sum satisfies that limit.</p>

<a name="mapForceToJava"> </a>
## Mapping Database.com to Java Data Types

This table maps Database.com data types to Java data types. It shows which Java data types are allowed for each Java data type. Some Database.com data types can map to more than one Java data type. This table is intended as a quick reference for Java developers who are familiar with data types in Database.com and want to see how they map to data types in Java and JPA.

The table includes a column for the standard JPA annotation used for each data type.


<table cellpadding="4" cellspacing="0" border="1">
<thead align="left">
<tr>
<th width="22.669491525423734%" >Database.com Data Type</th>

<th width="18.43220338983051%" >Java Data Type</th>

<th width="30.720338983050848%" >Standard JPA Annotation</th>

</tr>

</thead>

<tbody>
<tr>
<td width="22.669491525423734%" >Auto Number</td>

<td width="18.43220338983051%" ><ul>
<li>int</li>

<li>Integer</li>

</ul>
</td>

<td width="30.720338983050848%" >N/A</td>

</tr>

<tr>
<td width="22.669491525423734%" >Formula</td>

<td width="18.43220338983051%" >String</td>

<td width="30.720338983050848%" >@Column(length = <em>fieldLength</em>) <sup>1</sup></td>

</tr>

<tr>
<td width="22.669491525423734%" >Lookup Relationship</td>

<td width="18.43220338983051%" >Matches class of referenced object</td>

<td width="30.720338983050848%" >@ManyToOne
</td>

</tr>

<tr>
<td width="22.669491525423734%" >Master-Detail Relationship</td>

<td width="18.43220338983051%" >Matches class of referenced object</td>

<td width="30.720338983050848%" >@ManyToOne
</td>

</tr>

<tr>
<td width="22.669491525423734%" >Checkbox</td>

<td width="18.43220338983051%" ><ul>
<li>boolean</li>

<li>Boolean</li>

</ul>
</td>

<td width="30.720338983050848%" >N/A</td>

</tr>

<tr>
<td width="22.669491525423734%" >Currency</td>

<td width="18.43220338983051%" ><ul>
<li>short</li>
<li>int</li>
<li>long</li>
<li>double</li>
<li>float</li>
<li>Short</li>
<li>Integer</li>
<li>Long</li>
<li>Double</li>
<li>Float</li>
<li>BigInteger</li>
<li>BigDecimal</li>
</ul>
</td>

<td width="30.720338983050848%" >@Column(precision=16,
scale=2)</td>

</tr>

<tr>
<td width="22.669491525423734%" >Date</td>

<td width="18.43220338983051%" >java.util.Date</td>

<td width="30.720338983050848%" >@Temporal(TemporalType.DATE)</td>

</tr>

<tr>
<td width="22.669491525423734%" >Date/Time</td>

<td width="18.43220338983051%" ><ul>
<li>Calendar</li>

<li>GregorianCalendar</li>

</ul>
</td>

<td width="30.720338983050848%" >@Temporal(TemporalType.TIMESTAMP)</td>

</tr>

<tr>
<td width="22.669491525423734%" >Email</td>

<td width="18.43220338983051%" >String</td>

<td width="30.720338983050848%" >@Column(length = <em>fieldLength</em>)</td>

</tr>

<tr>
<td width="22.669491525423734%" >Number</td>

<td width="18.43220338983051%" ><ul>
<li>short</li>
<li>int</li>
<li>long</li>
<li>double</li>
<li>float</li>
<li>Short</li>
<li>Integer</li>
<li>Long</li>
<li>Double</li>
<li>Float</li>
<li>BigInteger</li>
<li>BigDecimal</li>
</ul>
</td>

<td width="30.720338983050848%" >@Column(precision = <em>decimalPrecision</em>, scale = <em>decimalScale</em>) <sup>2</sup></td>

</tr>

<tr>
<td width="22.669491525423734%" >Percent</td>

<td width="18.43220338983051%" ><ul>
<li>short</li>
<li>int</li>
<li>long</li>
<li>double</li>
<li>float</li>
<li>Short</li>
<li>Integer</li>
<li>Long</li>
<li>Double</li>
<li>Float</li>
<li>BigInteger</li>
<li>BigDecimal</li>
</ul>
</td>

<td width="30.720338983050848%" >@Column(precision = <em>decimalPrecision</em>, scale = <em>decimalScale</em>)</td>

</tr>

<tr>
<td width="22.669491525423734%" >Phone</td>

<td width="18.43220338983051%" >String</td>

<td width="30.720338983050848%" >@Column(length = <em>fieldLength</em>)</td>

</tr>

<tr>
<td width="22.669491525423734%" >Picklist</td>

<td width="18.43220338983051%" >enum for available values<p>
String for selected value</p>
</td>

<td width="30.720338983050848%" >For available values: @Enumerated(EnumType.STRING) OR @Enumerated(EnumType.ORDINAL)</td>

</tr>

<tr>
<td width="22.669491525423734%" >Picklist (Multi-Select)</td>

<td width="18.43220338983051%" >enum for available values<p>
String[] for selected values</p>
</td>

<td width="30.720338983050848%" >For available values: @Enumerated(EnumType.STRING) OR @Enumerated(EnumType.ORDINAL)</td>

</tr>

<tr>
<td width="22.669491525423734%" >Text</td>

<td width="18.43220338983051%" ><ul>
<li>byte</li>
<li>String</li>
<li>char</li>
<li>Character</li>

</ul>
</td>

<td width="30.720338983050848%" >@Column(length = <em>fieldLength</em>)</td>

</tr>

<tr>
<td width="22.669491525423734%" >Text Area</td>

<td width="18.43220338983051%" >String</td>

<td width="30.720338983050848%" >@Column(length = <em>fieldLength</em>)</td>

</tr>

<tr>
<td width="22.669491525423734%" >Text Area (Long)</td>

<td width="18.43220338983051%" >Not currently supported</td>

<td width="30.720338983050848%" >N/A</td>

</tr>

<tr>
<td width="22.669491525423734%" >Text Area (Rich)</td>

<td width="18.43220338983051%" >Not currently supported</td>

<td width="30.720338983050848%" >N/A</td>

</tr>

<tr>
<td width="22.669491525423734%" >URL</td>

<td width="18.43220338983051%" >URL</td>

<td width="30.720338983050848%" >@Basic</td>

</tr>

</tbody>

</table>

<p>
<sup>1</sup> The <em>fieldLength</em> variable represents the field length.</p>

<p>
<sup>2</sup> The <em>decimalPrecision</em> and <em>decimalScale</em> variables represent the decimal precision and decimal scale respectively
for the number. Precision
is the number of digits in a number. Scale is the number of digits
to the right of the decimal point in a number. For example, the number
256.99 has a precision of 5 and a scale of 2.</p>

