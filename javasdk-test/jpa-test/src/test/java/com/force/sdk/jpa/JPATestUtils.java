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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.testng.Assert;

import com.force.sdk.jpa.entities.*;
import com.force.sdk.jpa.entities.TestEntity.PickValues;


/**
 * Util class used to intitialize TestEntities.
 * 
 * @author Jill Wetzler
 */
public final class JPATestUtils {
    
    public static final String LAZY_ATTR_ACCESS_ERROR =
        "yet this field was not detached when you detached the object. Either dont access this field,";

    private JPATestUtils() {  }
    
    /**
     * Test enum representing digits.
     * 
     * @author Jill Wetzler
     */
    enum Digit {
        AZERO (new Short("0").shortValue()),
        ONE (new Short("1").shortValue()),
        TWO (new Short("2").shortValue()),
        THREE (new Short("3").shortValue()),
        FOUR (new Short("4").shortValue()),
        FIVE (new Short("5").shortValue()),
        SIX (new Short("6").shortValue()),
        SEVEN (new Short("7").shortValue()),
        EIGHT (new Short("8").shortValue()),
        NINE (new Short("9").shortValue());

        short value;
        private Digit(short value) {
            this.value = value;
        }
    }
    
    static final String HALF_POEM  = "16-bit Intel 8088 chip by Charles Bukowski\n"
            + "with an Apple Macintosh\n"
            + "you can't run Radio Shack programs\n"
            + "in its disc drive.\n"
            + "nor can a Commodore 64\n"
            + "drive read a file\n"
            + "you have created on an\n"
            + "IBM Personal Computer.";
    static final String FULL_POEM = "16-bit Intel 8088 chip by Charles Bukowski\n"
            + "with an Apple Macintosh\n"
            + "you can't run Radio Shack programs\n"
            + "in its disc drive.\n"
            + "nor can a Commodore 64\n"
            + "drive read a file\n"
            + "you have created on an\n"
            + "IBM Personal Computer.\n"
            + "both Kaypro and Osborne computers use\n"
            + "the CP/M operating system\n"
            + "but can't read each other's\n"
            + "handwriting\n"
            + "for they format (write\n"
            + "on) discs in different\n"
            + "ways.\n"
            + "the Tandy 2000 runs MS-DOS but\n"
            + "can't use most programs produced for\n"
            + "the IBM Personal Computer\n"
            + "unless certain\n"
            + "bits and bytes are\n"
            + "altered\n"
            + "but the wind still blows over\n"
            + "Savannah\n"
            + "and in the Spring\n"
            + "the turkey buzzard struts and\n"
            + "flounces before his\n"
            + "hens.";
    
    static final String FULL_POEM_IN_HTML = "16-bit Intel 8088 chip - A poem by Charles Bukowski - American Poems \n"
            + "\n"
            + "<table align=\"center\" width=\"990\" border=\"0\"><tbody><tr align=\"center\"><td align=\"center\" "
                + "colspan=\"1\" rowspan=\"1\" width=\"250\">\n"
            + "        <div><a href=\"/\" target=\"_blank\"><img align=\"left\" src=\"/images/americanpoems-logo.jpg\" "
                            + "alt=\"American Poems Home\" height=\"120\" width=\"230\" border=\"0\"></img></a></div>\n"
            + "    </td><td align=\"center\" colspan=\"1\" rowspan=\"1\" width=\"740\">\n"
            + "        <table width=\"740\" border=\"0\"><tbody><tr><td colspan=\"2\" rowspan=\"1\" height=\"90\" "
                        + "width=\"740\">\n"
            + "    <img align=\"right\" src=\"/images/space.gif\" height=\"90\" width=\"1\" border=\"0\"></img>\n"
            + "\n"
            + "    \n"
            + "\n"
            + "\n"
            + "\n"
            + "            </td></tr><tr><td align=\"center\" colspan=\"1\" rowspan=\"1\" width=\"550\">\n"
            + "\n"
            + "                <a href=\"/poets\" target=\"_blank\">Poets</a> |\n"
            + "                <a href=\"/members\" target=\"_blank\">Members</a> |\n"
            + "                <a href=\"/poemotd.php\" target=\"_blank\">Poem of the Day</a> |\n"
            + "                <a href=\"/top40.php\" target=\"_blank\">Top 40</a> |\n"
            + "                <a href=\"/search.php\" target=\"_blank\">Search</a> |\n"
            + "                <a href=\"/comments.php\" target=\"_blank\">Comments</a> |\n"
            + "                <a href=\"/privacy.php\" target=\"_blank\">Privacy</a>\n"
            + "\n"
            + "                <br>\n"
            + "                January 4th, 2011 - we have <a href=\"/poets\" target=\"_blank\">234 poets</a>, "
                                + "8,023 poems and <a href=\"/comments.php\" target=\"_blank\">21,218 comments</a>.\n"
            + "            </td><td colspan=\"1\" rowspan=\"1\" width=\"190\">\n"
            + "                <div>\n"
            + "                    \n"
            + "                </div>\n"
            + "            <br></td></tr></tbody></table>\n"
            + "\n"
            + "    </td></tr></tbody></table>\n"
            + "\n"
            + "<table align=\"center\" width=\"990\" border=\"0\"><tbody><tr><td align=\"center\" colspan=\"5\" rowspan=\"1\" "
                + "height=\"7\"><img src=\"/gfx/bottom_wide.gif\" height=\"7\" width=\"990\"></img></td></tr></tbody></table>"
                + "<table align=\"center\" width=\"990\" border=\"0\"><tbody>"
                + "<tr align=\"center\"><td align=\"left\" colspan=\"1\" rowspan=\"1\" width=\"780\">\n"
            + "\n"
            + "        <table width=\"780\" border=\"0\"><caption>Charles Bukowski - 16-bit Intel 8088 chip</caption>"
                        + "<tbody><tr><td align=\"left\" colspan=\"1\" rowspan=\"1\">\n"
            + "        <p>\n"
            + "                        </p><pre>with an Apple Macintosh<br>you can&#39;t run Radio Shack programs<br>"
                                                + "in its disc drive.<br>nor can a Commodore 64<br>drive read a file<br>"
                                                + "you have created on an<br>IBM Personal Computer.<br>"
                                                + "both Kaypro and Osborne computers use<br>the CP/M operating system<br>"
                                                + "but can&#39;t read each other&#39;s<br>handwriting<br>"
                                                + "for they format (write<br>on) discs in different<br>ways.<br>"
                                                + "the Tandy 2000 runs MS-DOS but<br>"
                                                + "can&#39;t use most programs produced for<br>the IBM Personal Computer<br>"
                                                + "unless certain<br>bits and bytes are<br>altered<br>"
                                                + "but the wind still blows over<br>Savannah<br>"
                                                + "and in the Spring<br>the turkey buzzard struts and<br>"
                                                + "flounces before his<br>hens.</pre>\n"
            + "\n"
            + "            <br><div>\n"
            + "                <a href=\"http://addthis.com/bookmark.php?v=250\" target=\"_blank\">Share</a>\n"
            + "\n"
            + "                <span> </span>\n"
            + "                <a target=\"_blank\"></a>\n"
            + "                <a target=\"_blank\"></a>\n"
            + "                <a target=\"_blank\"></a>\n"
            + "                <span>|</span>\n"
            + "\n"
            + "                <a target=\"_blank\"></a>\n"
            + "            </div>\n"
            + "            \n"
            + "\n"
            + "            \n"
            + "\n"
            + "          </td></tr></tbody></table>\n"
            + "        <table width=\"780\" border=\"0\"><tbody><tr><td align=\"left\" colspan=\"1\" rowspan=\"1\">\n"
            + "            <div>\n"
            + "                <b>Added:</b> Feb 20 2003 | <b>Viewed:</b> 7273 times |"
                                + " <a href=\"/poets/Charles-Bukowski/123/comments\" target=\"_blank\">"
                                + "<img src=\"/gfx/comment.gif\" "
                                    + "alt=\"Comments and analysis of 16-bit Intel 8088 chip by Charles Bukowski\" "
                                    + "height=\"16\" width=\"16\" border=\"0\"></img> Comments</a> (1)\n"
            + "\n"
            + "<br><br>\n"
            + "<a href=\"/customessays.php\" target=\"_blank\">"
                + "Need Help Writing a Term Paper on <i>16-bit Intel 8088 chip</i>?</a>\n"
            + "\n"
            + "            </div>\n"
            + "\n"
            + "\n"
            + "\n"
            + "          </td></tr></tbody></table>\n"
            + "    <br>\n"
            + "        <table width=\"780\" border=\"0\"><caption>16-bit Intel 8088 chip - Comments and Information</caption>"
                        + "<tbody><tr><td align=\"left\" colspan=\"1\" rowspan=\"1\">\n"
            + "\n"
            + "        <p>\n"
            + "            \n"
            + "<b>Poet:</b> <a href=\"/poets/Charles-Bukowski\" target=\"_blank\">Charles Bukowski</a>\n"
            + "<br>\n"
            + "<b>Poem:</b>  16-bit Intel 8088 chip\n"
            + "<br>\n"
            + "\n"
            + "\n"
            + "<b>Poem of the Day:</b>\n"
            + "<a href=\"/poemotd_archive.php#April_2010\" target=\"_blank\">Apr 7 2010</a>\n"
            + "\n"
            + "<br>\n"
            + "\n"
            + "            </p><div>\n"
            + "<i>Comment 1 of 1, added on July 11th, 2007 at 3:01 PM.</i>\n"
            + "<br>\n"
            + "\n"
            + "<br>\n"
            + "That is because Nature is more or less consistent whereas man never is... :)\n"
            + "<br><br>\n"
            + "\n"
            + "<i>me</i> from <b>United States</b>\n"
            + "\n"
            + "<br>\n"
            + "</div>\n"
            + "<div align=\"right\">"
                + "<a href=\"/poets/Charles-Bukowski/123/comments\" target=\"_blank\">More Comments &gt;&gt;</a></div>      \n"
            + "        <p>\n"
            + "            Are you looking for more information on this poem?  Perhaps you are trying to analyze it?  "
                            + "The poem, 16-bit Intel 8088 chip, has received "
                            + "<a href=\"/poets/Charles-Bukowski/123/comments\" target=\"_blank\">one comment</a> so far.  "
                            + "<a href=\"/poets/Charles-Bukowski/123/comments\" target=\"_blank\">Click here</a> to read it, "
                            + "and perhaps post a comment of your own.\n"
            + "        </p>\n"
            + "\n"
            + "          </td></tr></tbody></table>\n"
            + "\n"
            + "    </td><td align=\"left\" colspan=\"1\" rowspan=\"1\" width=\"180\">\n"
            + "        <table width=\"180\" border=\"0\"><caption>Poem Info</caption>"
                        + "<tbody><tr><td colspan=\"1\" rowspan=\"1\">\n"
            + "                <p>\n"
            + "                </p>\n"
            + "\n"
            + "<br>\n"
            + "<b>Poet:</b> <a href=\"/poets/Charles-Bukowski\" target=\"_blank\">Charles Bukowski</a>\n"
            + "<br>\n"
            + "\n"
            + "<b>Poem:</b> 16-bit Intel 8088 chip\n"
            + "\n"
            + "<br>\n"
            + "\n"
            + "\n"
            + "\n"
            + "<b>Last read:</b> 2011-01-04 15:11:23\n"
            + "<br>\n"
            + "<b>Poem of the Day:</b>\n"
            + "<a href=\"/poemotd_archive.php#April_2010\" target=\"_blank\">Apr 7 2010</a>\n"
            + "<br>\n"
            + "\n"
            + "Viewed 7273 times.\n"
            + "<br>\n"
            + "Added Feb 20 2003.\n"
            + "\n"
            + "                \n"
            + "\n"
            + "          </td></tr></tbody></table>\n"
            + "    <br>\n"
            + "        <table width=\"180\" border=\"0\"><caption>Bukowski Info</caption>"
                        + "<tbody><tr><td colspan=\"1\" rowspan=\"1\">\n"
            + "\n"
            + "                <p>\n"
            + "                <a href=\"/poets/Charles-Bukowski#biography\" target=\"_blank\">Biography</a>\n"
            + "                <br>\n"
            + "\n"
            + "                <a href=\"/poets/Charles-Bukowski#poems\" target=\"_blank\">More Poems</a>\n"
            + "                <br>\n"
            + "                <span style=\"margin-left: 22px; \">(120 poems)</span>\n"
            + "                <br>\n"
            + "\n"
            + "                <a href=\"/poets/Charles-Bukowski#books\" target=\"_blank\">Books</a>\n"
            + "                </p>\n"
            + "          </td></tr><tr><td colspan=\"1\" rowspan=\"1\">\n"
            + "\n"
            + "                <p></p>\n"
            + "                \n"
            + "\n"
            + "\n"
            + "\n"
            + "\n"
            + "                \n"
            + "          <br></td></tr></tbody></table>\n"
            + "\n"
            + "    </td></tr></tbody></table><table align=\"center\" width=\"990\" border=\"0\">"
                    + "<tbody><tr><td align=\"center\" colspan=\"5\" rowspan=\"1\" height=\"7\">"
                    + "<img src=\"/gfx/bottom_wide.gif\" height=\"7\" width=\"990\"></img></td></tr></tbody></table>\n"
            + "\n"
            + "<table align=\"center\" width=\"990\" border=\"0\"><tbody><tr align=\"center\">"
                + "<td colspan=\"1\" rowspan=\"1\" height=\"50\" width=\"990\">\n"
            + "        Copyright &copy; 2000-2011 Gunnar Bengtsson.  All Rights Reserved. "
                + "<a href=\"/links.php\" target=\"_blank\">Links</a> | "
                + "<a href=\"/store.php\" target=\"_blank\">Bookstore</a></td></tr></tbody></table>";
    
    
    public static void initializeTestEntity(AnnotatedEntity entity) {
        entity.setName("foo bar");
        
        entity.setBoolType(true);
        entity.setBooleanObject(Boolean.TRUE);
        entity.setShortType(Short.MAX_VALUE);
        entity.setShortObject(Short.valueOf(Short.MAX_VALUE));
        entity.setIntType(Integer.MAX_VALUE);
        entity.setIntType(Integer.valueOf(Integer.MAX_VALUE));
        entity.setLongType(Long.valueOf(Integer.MAX_VALUE * 1000L));
        entity.setLongObject(Long.valueOf(Integer.MAX_VALUE * 1000L).longValue());
        entity.setDoubleType(Integer.MAX_VALUE * 1000L);
        entity.setDoubleObject(new Double(Integer.MAX_VALUE * 1000L));
        entity.setFloatType(Integer.MAX_VALUE * 1000L);
        entity.setFloatObject(new Float(Integer.MAX_VALUE * 1000L));
        entity.setByteType(Byte.MAX_VALUE);
        entity.setByteObject(Byte.valueOf(Byte.MAX_VALUE));
        entity.setCharType('a');
        entity.setCharacterObject(Character.valueOf('A'));
        
        entity.setBigDecimalObject(BigDecimal.valueOf(Integer.MAX_VALUE, 2));
        entity.setBigIntegerObject(BigInteger.valueOf(Integer.MAX_VALUE));
        entity.setStringObject(entity.getClass().getSimpleName());

        // Create a date only field fields
        Calendar cal = getCalendar(false);
        Calendar.getInstance();
        cal.set(2010, 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0); // We do not preserve ms resolution
        Date date = cal.getTime();
        entity.setDate(date);
        
        // add some time to it too
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 20);
        cal.set(Calendar.SECOND, 30);
        cal.set(Calendar.MILLISECOND, 0); // We do not preserve ms resolution
        entity.setDateTimeCal(cal);
        GregorianCalendar dateTimeGCal = new GregorianCalendar();
        dateTimeGCal.set(Calendar.MILLISECOND, 0); // We do not preserve ms resolution
        entity.setDateTimeGCal(dateTimeGCal);
        
        // Temporal dates
        entity.setDateTemporal(date);
        entity.setDateTimeTemporal(cal.getTime());
        
        // Picklist values
        entity.setPickValueDef(PickValues.THREE);
        entity.setPickValue(PickValues.TWO);
        entity.setPickValueOrdinal(PickValues.TWO);
        entity.setLiberalPickValueDef("FOUR");
        entity.setPickValueMultiDef(new PickValues[] {PickValues.THREE});
        entity.setPickValueMulti(new PickValues[] {PickValues.ONE, PickValues.TWO});
        entity.setPickValueMultiOrdinal(new PickValues[] {PickValues.ONE, PickValues.TWO});
        entity.setLiberalPickValueMultiDef(new String[] {"TWO", "FIVE"});
        
        // URL values
        try {
            entity.setUrl(new URL("http://localhost:8080"));
        } catch (MalformedURLException me) {
            throw new RuntimeException(me);
        }
        
        entity.setPhone("415-123-1234");
        entity.setEmail("foobar@salesforce.com");
        entity.setPercent(42);
        
        entity.setTextArea(HALF_POEM);
        entity.setLongTextArea(FULL_POEM);
        entity.setRichTextArea(FULL_POEM_IN_HTML);
        EmbeddedTestEntity emb = new EmbeddedTestEntity();
        emb.setEmbedded("embedded");
        entity.setEmbedded(emb);
    }
    
    public static void initializeTestEntity(AnnotatedEntity entity, Digit digit) {
        
        entity.setName(digit.toString());
        entity.setBoolType(true);
        entity.setBooleanObject(Boolean.valueOf(entity.getBoolType()));
        
        entity.setShortType(digit.value);
        entity.setShortObject(Short.valueOf(entity.getShortType()));
        
        entity.setIntType(digit.value);
        entity.setIntegerObject(Integer.valueOf(entity.getIntType()));
        
        entity.setLongType(digit.value);
        entity.setLongObject(Long.valueOf(entity.getLongType()));
        
        entity.setDoubleType(digit.value);
        entity.setDoubleObject(new Double(entity.getDoubleType()));
        
        entity.setFloatType(digit.value);
        entity.setFloatObject(new Float(entity.getFloatType()));
        
        entity.setByteType(Byte.parseByte(Short.toString(digit.value)));
        entity.setByteObject(Byte.valueOf(entity.getByteType()));
        
        entity.setCharType(digit.toString().charAt(0));
        entity.setCharacterObject(Character.valueOf(entity.getCharType()));
        
        entity.setBigDecimalObject(BigDecimal.valueOf(digit.value, 2));
        entity.setBigIntegerObject(BigInteger.valueOf(digit.value));
        entity.setStringObject(digit.toString());

        // Create a date only field fields
        Calendar cal = getCalendar(false);
        Calendar.getInstance();
        cal.set(2010, 1, 1, digit.value, digit.value, digit.value);
        cal.set(Calendar.MILLISECOND, digit.value); // We do not preserve ms resolution
        Date date = cal.getTime();
        entity.setDate(date);
        entity.setDateTemporal(date);
        
        // add some time to it too
        cal.set(Calendar.HOUR_OF_DAY, digit.value);
        cal.set(Calendar.MINUTE, digit.value);
        cal.set(Calendar.SECOND, digit.value);
        entity.setDateTimeCal(cal);
        entity.setDateTimeTemporal(cal.getTime());

        GregorianCalendar dateTimeGCal = new GregorianCalendar(2010, 1, 1, digit.value, digit.value, digit.value);
        dateTimeGCal.set(Calendar.MILLISECOND, digit.value); // We do not preserve ms resolution
        entity.setDateTimeGCal(dateTimeGCal);
                
        // Picklist values
        entity.setPickValueDef(PickValues.THREE);
        entity.setPickValue(PickValues.TWO);
        entity.setPickValueOrdinal(PickValues.TWO);
        entity.setLiberalPickValueDef("FOUR");
        entity.setPickValueMultiDef(new PickValues[] {PickValues.THREE});
        entity.setPickValueMulti(new PickValues[] {PickValues.ONE, PickValues.TWO});
        entity.setPickValueMultiOrdinal(new PickValues[] {PickValues.ONE, PickValues.TWO});
        entity.setLiberalPickValueMultiDef(new String[] {"TWO", "FIVE"});

        // URL values
        try {
            entity.setUrl(new URL("http://localhost:" + digit.value + digit.value + digit.value + digit.value));
        } catch (MalformedURLException me) {
            throw new RuntimeException(me);
        }
        
        entity.setPhone("415-123-" + digit.value + digit.value + digit.value + digit.value);
        entity.setEmail(digit.value + "foobar@salesforce.com");
        entity.setPercent(digit.value);
        entity.setTextArea(HALF_POEM);
        entity.setLongTextArea(FULL_POEM);
        entity.setRichTextArea(FULL_POEM_IN_HTML);
    }
    
    /**
     * Supply null values in entity where possible.
     * @param entity
     */
    public static void initializeNullTestEntity(AnnotatedEntity entity) {
        entity.setName(null);
        entity.setLongType(Long.valueOf(0));
        entity.setBoolType(false);
        entity.setBigDecimalObject(null);
        entity.setIntType(0);
        entity.setIntegerObject(null);
        entity.setStringObject(null);
        entity.setDate(null);
        entity.setDateTimeCal(null);
        entity.setDateTimeGCal(null);
        // Temporal dates
        entity.setDateTemporal(null);
        entity.setDateTimeTemporal(null);
        // Picklist values
        entity.setPickValueDef(null);
        entity.setPickValue(null);
        entity.setPickValueOrdinal(null);
        entity.setPickValueMultiDef(null);
        entity.setPickValueMulti(null);
        entity.setPickValueMultiOrdinal(null);
        // URL values
        entity.setUrl(null);
        entity.setPhone(null);
        entity.setEmail(null);
        entity.setPercent(0);
    }
    
    public static ParentTestEntity setMasterDetailRelationship(AnnotatedEntity entity) {
        ParentTestEntity parent = new ParentTestEntity();
        parent.init();
        entity.setParentMasterDetail(parent);
        return parent;
    }
    
    public static Calendar getCalendar(boolean dateAndTime) {
        Calendar cal = Calendar.getInstance();
        cal.set(2010, 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0); // We do not preserve ms resolution
        if (!dateAndTime) {
            // calendar w/ date only
            return cal;
        } else {
            // add some time to it too
            cal.set(Calendar.HOUR_OF_DAY, 10);
            cal.set(Calendar.MINUTE, 20);
            cal.set(Calendar.SECOND, 30);
            cal.set(Calendar.MILLISECOND, 0); // We do not preserve ms resolution
            return cal;
        }
    }
    
    /**
     * Convenience method to compare two TestEntity objects based on their fields via reflection.
     * This method will fail if returned types are complex types due to the equals comparison.
     * @param persisted The entity that was persited to the DB.
     * @throws ClassNotFoundException 
     * @throws Exception 
     */
    public static boolean annotatedEntityEqual(AnnotatedEntity current, AnnotatedEntity persisted) throws Exception {
        if (current == persisted) return true;
        if (persisted == null) return false;
        Object[] invokeArg = null;
        Method[] methods = AnnotatedEntity.class.getDeclaredMethods();
        int fieldCount = 0;
        for (Method m : methods) {
            if (!m.getName().startsWith("get")) continue;
            fieldCount++;
            String fieldName = m.getName().substring(3, 4).toLowerCase() +  m.getName().substring(4);
            // we ignore these since we can't compare them we test them individually outside this method
            if (fieldName.startsWith("auto")) continue;
            Object expected = m.invoke(current, invokeArg);
            Object actual = m.invoke(persisted, invokeArg);
            /**
             * TODO - We have to specialcase the comparisons for now.
             * That is because we are ignoring TimeZone information. We will have to add that in.
             */
            if (expected == actual) {
                // Nothing to do here
            } else if (expected == null) {
                // These are read only fields and fields set by server
                if ("lastModifiedDate".equals(fieldName)) {
                    Assert.assertTrue(actual instanceof Calendar, "LastModifiedDate is not a Calendar: " + actual);
                } else {
                    Assert.fail("Unexpected readonly field detected: " + fieldName);
                }
            } else if (expected instanceof Calendar && actual instanceof Calendar) {
                Assert.assertEquals(((Calendar) actual).getTime(), ((Calendar) expected).getTime(),
                        "Difference at field (type Clendar) " + fieldName + ": ");
            } else if (expected instanceof ParentTestEntity && actual instanceof ParentTestEntity) {
                Assert.assertEquals(((ParentTestEntity) actual).getId(), ((ParentTestEntity) expected).getId(),
                        "Parent objects are different: ");
                Assert.assertEquals(((ParentTestEntity) actual).getName(), ((ParentTestEntity) expected).getName(),
                        "Parent object's name are different: ");
            } else if (expected.getClass().isArray()) {
                Assert.assertTrue(Arrays.equals((Object[]) expected, (Object[]) actual), "Array objects are different");
            } else if (expected instanceof EmbeddedTestEntity && actual instanceof EmbeddedTestEntity) {
                Assert.assertEquals(((EmbeddedTestEntity) actual).getEmbedded(), ((EmbeddedTestEntity) expected).getEmbedded(),
                        "Embedded object values are different: ");
            } else {
                Assert.assertEquals(actual, expected, "Difference at field " + fieldName + ": ");
            }
        }
        Assert.assertEquals(fieldCount, AnnotatedEntity.TOTAL_FIELDS, "Missing fields in the test ");
        return true;
    }

    /**
     * Verify that PhoneEntity object has name and type starting with the prefix.
     * @param o: The phoneEntity.
     * @param prefix:  
     */
    public static void verifyPhoneEntity(PhoneEntity o, String prefix) {
        Assert.assertTrue(o.getName().contains(prefix), "Phone name does not match.");
        Assert.assertTrue(o.getType().contains(prefix), "Phone type does not match.");
    }

    /**
     * Create a PhoneEntity object.
     * @param prefix: the PhoneEntity object name and type will start with this prefix.
     * @return phoneEntity object.
     */
    public static PhoneEntity createPhoneEntity(String prefix) {
        PhoneEntity newPhone = new PhoneEntity();
        newPhone.setName(prefix + " phone");
        newPhone.setType(prefix + "type");
        return newPhone;
    }

   /**
    * Verify that list there is one to one mapping between the PhoneEntity objects (in list) and prefixes.
    * 
    * @param list: list of PhoneEntity objects
    * @param prefixes: list of prefixes.
    */
    public static void verifyContains(List<PhoneEntity> list, String [] prefixes) {
        Assert.assertEquals(list.size(), prefixes.length, "PhoneEntity list and prefixes should have same size.");
        for (String prefix : prefixes) {
            boolean found = false;
            for (PhoneEntity phone : list) {
                if (phone.getName().contains(prefix)) {
                    found = true;
                }
            }
            
            if (!found) {
                Assert.assertTrue(found, "Prefix was not found:" + prefix);
            }
        }
    }

    /**
     * Assert that detached object exception occurs when a lazy field (which has not been loaded)
     * is accessed after detaching an object.
     *  
     * @param obj: The entity whose property is accessed.
     * @param property: The property name, which has not been loaded before detaching the entity. 
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static void assertDetachedFieldException(Object obj, String property)
    throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException {
        try {
            Class c = Class.forName(PersonEntity.class.getName());
            Class[] partypes = null;
            Object[] args = null;
            Method m = c.getMethod(property, partypes);
            m.invoke(obj, args);
            Assert.fail(property + " is lazy attribute and an exception was expected.");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getTargetException().toString().contains(LAZY_ATTR_ACCESS_ERROR));
        }
    }
}
