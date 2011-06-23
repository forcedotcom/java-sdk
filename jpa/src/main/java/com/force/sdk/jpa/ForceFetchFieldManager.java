/**
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.force.sdk.jpa;

import java.lang.reflect.Array;
import java.math.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.*;
import java.util.regex.Pattern;

import javax.jdo.identity.StringIdentity;

import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.*;
import org.datanucleus.store.fieldmanager.AbstractFieldManager;
import org.datanucleus.store.query.Query;
import org.datanucleus.store.types.sco.SCOUtils;

import com.force.sdk.jpa.model.PicklistValueEnum;
import com.force.sdk.jpa.query.ForceQueryUtils;
import com.force.sdk.jpa.table.ColumnImpl;
import com.force.sdk.jpa.table.TableImpl;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.*;
import com.sforce.ws.types.Time;
import com.sforce.ws.util.Base64;

/**
 * 
 * Field manager for retrieving records from Force.com.  This class handles
 * parsing the different field types that come back from SOQL requests and
 * returning the proper objects to DataNucleus.
 *
 * @author Fiaz Hossain
 */
public class ForceFetchFieldManager extends AbstractFieldManager {

    private static final Pattern MULTI_SELECT_VALUE_SEPARATOR = Pattern.compile(";");
    private static final Pattern NO_PROTOCOL_URL_PATTERN = Pattern.compile("^(:|/)*[.]*");
        
    private final ObjectProvider objectProvider;
    private final ExecutionContext ec;
    private final AbstractClassMetaData acmd;
    private final ForceStoreManager storeManager;
    private final ForceManagedConnection mconn;
    private final XmlObject sobject;
    private final TableImpl table;
    private final Query query;
    
    /**
     * 
     * Creates a manager that will retrieve the specified field values of a particular entity with a known id.
     * 
     * @param objectProvider  the object provider
     * @param storeManager  the store manager
     * @param mconn  the managed connection for Force.com API connections
     * @param pkValue  the id of the object we're fetching
     * @param fieldNumbers  the numbers of the fields we want to fetch
     * @param query the query object
     * @throws ConnectionException  thrown if the query to Force.com fails
     */
    public ForceFetchFieldManager(ObjectProvider objectProvider, ForceStoreManager storeManager,
        ForceManagedConnection mconn, Object pkValue,  int[] fieldNumbers, Query query)
        throws ConnectionException {
        
        this.objectProvider = objectProvider;
        this.ec = objectProvider.getExecutionContext();
        this.acmd = objectProvider.getClassMetaData();
        this.storeManager = storeManager;
        this.mconn = mconn;
        this.table = storeManager.getTable(objectProvider.getClassMetaData());
        if (pkValue != null) {
            QueryResult qr = ((PartnerConnection) mconn.getConnection()).query(
                    new ForceQueryUtils(objectProvider.getExecutionContext(), mconn, null, null, null, null)
                    .buildQueryWithPK(table, objectProvider.getClassMetaData(), fieldNumbers, (String) pkValue, 0));
            if (qr.getSize() == 0) {
                throw new NucleusObjectNotFoundException();
            }
            this.sobject = qr.getRecords()[0];
        } else {
            this.sobject = new SObject();
        }
        
        this.query = query;
    }
    
    /**
     * Instantiates a fetch field manager with an sobject.
     * 
     * @param objectProvider the object provider
     * @param storeManager the store manager
     * @param mconn the managed connection object with connections to the Force.com APIs
     * @param sobject  the sObject we're retrieving fields from
     * @param query the query object
     */
    public ForceFetchFieldManager(ObjectProvider objectProvider, ForceStoreManager storeManager,
            ForceManagedConnection mconn, XmlObject sobject, Query query) {
        this.objectProvider = objectProvider;
        this.ec = objectProvider.getExecutionContext();
        this.acmd = objectProvider.getClassMetaData();
        this.storeManager = storeManager;
        this.mconn = mconn;
        this.table = storeManager.getTable(acmd);
        this.sobject = sobject;
        this.query = query;
    }
    
    /**
     * 
     * Instantiates a fetch field manager with an sobject and execution context.
     * 
     * @param ec  the execution context for this query
     * @param acmd  the class metadata object for the entity we're retrieving
     * @param storeManager the store manager
     * @param mconn  the managed connection object with connections to the Force.com APIs
     * @param sobject the sObject we're retrieving fields from
     * @param query  the query object
     */
    public ForceFetchFieldManager(ExecutionContext ec, AbstractClassMetaData acmd, ForceStoreManager storeManager,
            ForceManagedConnection mconn, XmlObject sobject, Query query) {
        this.objectProvider = null;
        this.ec = ec;
        this.acmd = acmd;
        this.storeManager = storeManager;
        this.mconn = mconn;
        this.table = storeManager.getTable(acmd);
        this.sobject = sobject;
        this.query = query;
    }
    
    @Override
    public boolean fetchBooleanField(int fieldNumber) {
        String ret = fetchStringField(fieldNumber);
        return ret != null ? Boolean.parseBoolean(ret) : false;
    }
    
    @Override
    public byte fetchByteField(int fieldNumber) {
        String ret = fetchStringField(fieldNumber);
        return ret != null ? Byte.parseByte(ret) : 0;
    }
    
    @Override
    public char fetchCharField(int fieldNumber) {
        String ret = fetchStringField(fieldNumber);
        return ret != null && ret.length() > 0 ? ret.charAt(0) : 0;
    }
    
    @Override
    public double fetchDoubleField(int fieldNumber) {
        String ret = fetchStringField(fieldNumber);
        return ret != null ? Double.parseDouble(ret) : 0;
    }
    
    @Override
    public float fetchFloatField(int fieldNumber) {
        String ret = fetchStringField(fieldNumber);
        return ret != null ? Float.parseFloat(ret) : 0;
    }
    
    @Override
    public int fetchIntField(int fieldNumber) {
        String ret = fetchStringField(fieldNumber);
        return ret != null ? Double.valueOf(ret).intValue() : 0;
    }
    
    @Override
    public long fetchLongField(int fieldNumber) {
        String ret = fetchStringField(fieldNumber);
        if (ret == null) return 0L;
        try {
            return Long.parseLong(ret);
        } catch (NumberFormatException ne) {
            return Double.valueOf(ret).longValue();
        }
    }
    
    @Override
    public Object fetchObjectField(int fieldNumber) {
        return fetchObjectField(acmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber), null);
    }
    
    @Override
    public short fetchShortField(int fieldNumber) {
        String ret = fetchStringField(fieldNumber);
        return ret != null ? Double.valueOf(ret).shortValue() : 0;
    }
    
    /**
     * Fetch the field value from the sObject and return it as a string. Most of the fetch fields will
     * delegate to this one and then parse it into the proper type (like an int or double)
     * {@inheritDoc} 
     */
    @Override
    public String fetchStringField(int fieldNumber) {
        ColumnImpl column = table.getColumnAt(acmd, fieldNumber);
        Object ret = sobject.getField(column.getFieldName());
        return ret != null ? (String) ret : null;
    }

    /**
     * 
     * For fields containing objects rather than strings or primitive types, this method returns the proper type of the
     * object depending on the field type. On occasion we use this method to fetch the proper objects for use in expressions
     * and must override the actual value of the object, in that case use the override value instead of getting it from the
     * sObject
     * 
     * @param ammd  the member metadata for the field/property holding the value
     * @param valueOverride the value to use in case of an override where you don't want what's in the sObject
     *                      (used for expressions)
     * @return  the value cast to its proper object type
     */
    public Object fetchObjectField(AbstractMemberMetaData ammd, Object valueOverride) {
        if (ammd.getCollection() != null || ammd.getMap() != null) {
            // we have a collection (@OneToMany relationship) of child objects, recursively populate them
            final AbstractClassMetaData cmd =
                PersistenceUtils.getMemberElementClassMetaData(ammd, ec.getClassLoaderResolver(), ec.getMetaDataManager());
            Collection childrenColl = null;
            Map childrenMap = null;
            int mapKeyPosition = (cmd != null ? cmd.getPKMemberPositions()[0] : -1);
            if (ammd.getCollection() != null) {
                childrenColl = Set.class.isAssignableFrom(ammd.getType()) ?  new LinkedHashSet() : new ArrayList();
            } else {
                childrenMap = new LinkedHashMap();
                if (ammd.getKeyMetaData() != null) {
                    // Read the @MapKey(name="name") data and use that to get mapKeyPosition
                    mapKeyPosition = cmd.getAbsolutePositionOfMember(ammd.getKeyMetaData().getMappedBy());
                }
            }

            // Reset column to the related field
            ColumnImpl column = table.getColumnFor(cmd, ammd);
            
            String relationshipName = column.getSelectFieldName();
            
            Iterator<XmlObject> rel = sobject.getChildren(relationshipName);
            while (rel.hasNext()) {
                Iterator<XmlObject> subs = rel.next().getChildren("records");
                
                // This is used for Native query
                int[] fieldsToLoad = cmd != null ? cmd.getDFGMemberPositions() : null;
                
                try {
                    while (subs.hasNext()) {
                        final XmlObject child = subs.next();
                        Object value = ec.findObjectUsingAID(new Type(ec.getClassLoaderResolver().
                                classForName(cmd.getFullClassName())),
                                ForceQueryUtils.getFieldValues2(cmd, fieldsToLoad, mconn, storeManager, child, query),
                                query == null ? true : query.getIgnoreCache(), true);
                        
                        if (childrenColl != null) {
                            childrenColl.add(value);
                        } else {
                            childrenMap.put(PersistenceUtils.getMemberValue(cmd, mapKeyPosition, value), value);
                        }
                    }
                } catch (Exception e) {
                    throw new NucleusUserException(e.getMessage(), e);
                }
            }
            if (objectProvider != null) {
                  Object ret = childrenColl != null ? childrenColl : childrenMap;
                return SCOUtils.newSCOInstance(objectProvider, ammd, ammd.getType(),
                        (ret != null ? ret.getClass() : null), ret, false, false, false);
            } else {
                // This is for Collection MEMBEROF and Map Key, Value, Entry operation only. The collection will be read only.
                return childrenColl != null ? Collections.unmodifiableCollection(childrenColl)
                                                : Collections.unmodifiableMap(childrenMap);
            }
        } else if (ammd.getEmbeddedMetaData() != null) {
            return fetchEmbeddedObject(ammd);
        }

        ColumnImpl column = table.getColumnFor(acmd, ammd);
        Object o = valueOverride != null ? valueOverride : sobject.getField(column.getSelectFieldName());
        if (o == null) return null;
        Calendar cal;
        Object tvalue;
        switch (column.getType()) {
        case _boolean:
            return Boolean.parseBoolean((String) o);
        case _int:
        case percent:
            if (ammd.getType() == Long.class || ammd.getType() == long.class) {
                try {
                    return Long.parseLong((String) o);
                } catch (NumberFormatException ne) {
                    return Double.valueOf((String) o).longValue();
                }
            } else if (ammd.getType() == Short.class || ammd.getType() == short.class)
                return Double.valueOf((String) o).shortValue();
            else if (ammd.getType() == Float.class || ammd.getType() == float.class)
                return Float.parseFloat((String) o);
            else if (ammd.getType() == Double.class || ammd.getType() == double.class)
                return Double.parseDouble((String) o);
            else if (ammd.getType() == BigInteger.class) {
                DecimalFormat f = new DecimalFormat("0.##################E0");
                f.setGroupingUsed(false);
                return new BigInteger(f.parse((String) o, new ParsePosition(0)).toString());
            } else {
                return Double.valueOf((String) o).intValue();
            }
        case _double:
            if (ammd.getType() == Float.class || ammd.getType() == float.class) {
                return Float.parseFloat((String) o);
            } else {
                return Double.parseDouble((String) o);
            }
        case currency:
            return new BigDecimal((String) o, new MathContext(column.getField().getPrecision()))
                        .setScale(column.getField().getScale(), RoundingMode.HALF_DOWN);
        case date:
            tvalue = new DateCodec().deserialize((String) o).getTime();
            return objectProvider != null ? SCOUtils.newSCOInstance(objectProvider, ammd, ammd.getType(),
                    (tvalue != null ? tvalue.getClass() : null), tvalue, false, false, false) : tvalue;
        case datetime:
            cal = new CalendarCodec().deserialize((String) o);
            if (ammd.getType() == Date.class) {
                tvalue = cal.getTime();
            } else if (ammd.getType() == Time.class) {
                tvalue = new Time(cal);
            } else {
                tvalue = cal;
            }
            return objectProvider != null ? SCOUtils.newSCOInstance(objectProvider, ammd, ammd.getType(),
                    (tvalue != null ? tvalue.getClass() : null), tvalue, false, false, false) : tvalue;
        case reference:
            // This is from @ManyToOne relationship, Create an entity and return it
            if (o instanceof XmlObject) {
                try {
                    AbstractClassMetaData cmd =
                        ec.getMetaDataManager().getMetaDataForClass(ammd.getTypeName(), ec.getClassLoaderResolver());
                    // We use the same sobject but with relationship name prefix
                    return ec.findObjectUsingAID(new Type(ec.getClassLoaderResolver().
                            classForName(cmd.getFullClassName())),
                            ForceQueryUtils.getFieldValues2(cmd, cmd.getDFGMemberPositions(),
                                                                mconn, storeManager, (XmlObject) o, query),
                            query == null ? true : query.getIgnoreCache(), true);
                } catch (Exception e) {
                    throw new NucleusUserException(e.getMessage(), e);
                }
            } else {
                // We return just a hollow object with ID that will have its fields fetched later
                return ec.findObject(new StringIdentity(ammd.getType(), (String) o), false, false, ammd.getTypeName());

            }
        case picklist:
            if (ammd.getType().isEnum()) {
                if (PersistenceUtils.isOrdinalEnum(ammd))
                    return ammd.getType().getEnumConstants()[Integer.parseInt((String) o)];
                
                // PicklistValueEnums should have a static fromValue method to load up enum values
                else if (PicklistValueEnum.class.isAssignableFrom(ammd.getType())) {
                    try {
                        return ammd.getType().getMethod("fromValue", String.class).invoke(ammd.getType(), (String) o);
                    } catch (Exception e) {
                        throw new NucleusUserException("Unable to invoke fromValue(String) for enum " + ammd.getType(), e);
                    }
                }
                return Enum.valueOf(ammd.getType(), (String) o);
            }
            break;
        case multipicklist:
            String[] values = MULTI_SELECT_VALUE_SEPARATOR.split((String) o);
            if (ammd.getType().getComponentType().isEnum()) {
                Class type = ammd.getType().getComponentType();
                Enum<?>[] enumObjects = (Enum<?>[]) Array.newInstance(type, values.length);
                Enum<?>[] enumValues = (Enum<?>[]) ammd.getType().getComponentType().getEnumConstants();
                boolean isOrdinal = PersistenceUtils.isOrdinalEnum(ammd);
                for (int i = 0; i < values.length; i++) {
                    enumObjects[i] = isOrdinal ? enumValues[Integer.parseInt(values[i])] : Enum.valueOf(type, values[i]);
                }
                return enumObjects;
            } else {
                return values;
            }
        case url:
            try {
                return new URL((String) o);
            } catch (MalformedURLException me) {
                try {
                    // Try replacing any leading ':' and '/' with a proper protocol
                    return new URL(NO_PROTOCOL_URL_PATTERN.matcher((String) o).replaceAll("http://"));
                } catch (MalformedURLException me2) {
                    throw new NucleusUserException(me2.getMessage());
                }
            }
        case string:
            if (ammd.getType() == Byte.class || ammd.getType() == byte.class) {
                return Byte.parseByte((String) o);
            } else if (ammd.getType() == Character.class || ammd.getType() == char.class) {
                if (o != null) {
                    return ((String) o).charAt(0);
                }
            }
            break;
        case base64:
            if (ammd.getType() == byte[].class || ammd.getType() == Byte[].class) {
                return Base64.decode(((String) o).getBytes());
            } else {
                throw new NucleusUserException("Bad datatype for base64 encoding: " + ammd.getTypeName());
            }
        default:
        }
        return o;
    }
    
    private Object fetchEmbeddedObject(AbstractMemberMetaData ammd) {
        if (objectProvider == null) return null;
        AbstractClassMetaData cmd =
            storeManager.getMetaDataManager().getMetaDataForClass(ammd.getType(), ec.getClassLoaderResolver());
        Object obj = null;
        try {
            obj = ec.getClassLoaderResolver().classForName(cmd.getFullClassName(), true).newInstance();
            for (AbstractMemberMetaData eammd : ammd.getEmbeddedMetaData().getMemberMetaData()) {
                PersistenceUtils.setFieldValue(ammd.getType(), cmd, cmd.getAbsolutePositionOfMember(eammd.getName()),
                                                obj, fetchObjectField(eammd, null));
            }
            ec.findObjectProviderForEmbedded(obj, objectProvider, ammd);
        } catch (Exception e) {
            throw new NucleusUserException(e.getMessage(), e);
        }
        return obj;
    }
}
