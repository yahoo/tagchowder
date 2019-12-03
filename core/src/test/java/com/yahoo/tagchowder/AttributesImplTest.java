/*
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  ====================================================================
 */

/*
 * Changes to the original project are Copyright 2019 Oath Inc.
 */

package com.yahoo.tagchowder;

import java.util.Iterator;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AttributesImpl}.
 *
 */
public class AttributesImplTest {

    /**
     * Test constructor.
     */
    @Test
    public void testConstructor() {
        AttributesImpl attributes = new AttributesImpl();
        Assert.assertEquals(attributes.getLength(), 0, "attrs length mismatched");
        AttributesImpl oldAttributes = new AttributesImpl();
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String type1 = "type1";
        String value = "value";
        oldAttributes.addAttribute(uri, localName, qname, type, value);
        // Try creating new attributes with a old copy.
        AttributesImpl newAttributes = new AttributesImpl(oldAttributes);
        Assert.assertEquals(newAttributes.getLength(), 1, "attrs length mismatched");
    }

    /**
     * Test getURI.
     */
    @Test
    public void testGetUriWithIndex() {
        // Add 2 attributes with same qualified name and verify uri returned is from first attribute.
        AttributesImpl attributes = new AttributesImpl();
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String uri1 = "xmlns=\"http://www.w3.org/1999/xhtml1\"";
        attributes.addAttribute(uri, "localName", "qname", "type", "value");
        attributes.addAttribute(uri1, "localName1", "qname", "type", "value");
        int index = attributes.getIndex("qname");
        Assert.assertEquals(attributes.getURI(index), uri, "uri mismatched");

        // Verify invalid index returns null.
        Assert.assertNull(attributes.getURI(10), "URI must be null");
    }

    /**
     * Test getLocalName.
     */
    @Test
    public void testGetLocalNameWithIndex() {
        // Add 2 attributes with same qualified name and verify local name returned is from first attribute.
        AttributesImpl attributes = new AttributesImpl();
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName0 = "localname0";
        String localName1 = "localname1";
        attributes.addAttribute(uri, localName0, "qname", "type", "value");
        attributes.addAttribute(uri, localName1, "qname", "type", "value");
        int index = attributes.getIndex("qname");
        Assert.assertEquals(attributes.getLocalName(index), localName0, "local name mismatched");

        // Verify invalid index returns null.
        Assert.assertNull(attributes.getLocalName(10), "Local name must be null");
    }

    /**
     * Test getQName.
     */
    @Test
    public void testGetQNameWithIndex() {
        // Add 2 attributes with same qualified name and verify qname returned is from its respective attribute.
        AttributesImpl attributes = new AttributesImpl();
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname0 = "qname0";
        String qname1 = "qname1";
        attributes.addAttribute(uri, localName, qname0, "type", "value");
        attributes.addAttribute(uri, localName, qname1, "type", "value");
        int index = attributes.getIndex(qname0);
        Assert.assertEquals(attributes.getQName(index), qname0, "qualified name mismatched");
        index = attributes.getIndex(qname1);
        Assert.assertEquals(attributes.getQName(index), qname1, "qualified name mismatched");

        // Verify invalid index returns null.
        Assert.assertNull(attributes.getQName(10), "qualified name must be null");
    }

    /**
     * Test getType.
     */
    @Test
    public void testGetTypeWithIndex() {
        // Add 2 attributes with same qualified name and verify type returned is from first attribute.
        AttributesImpl attributes = new AttributesImpl();
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type0 = "type0";
        String type1 = "type1";
        attributes.addAttribute(uri, localName, qname, type0, "value");
        attributes.addAttribute(uri, localName, qname, type1, "value");
        int index = attributes.getIndex(qname);
        Assert.assertEquals(attributes.getType(index), type0, "type mismatched");

        // Verify invalid index returns null.
        Assert.assertNull(attributes.getType(10), "type must be null");
    }

    /**
     * Test getType with qualified name.
     */
    @Test
    public void testGetTypeWithQName() {
        // Add 2 attributes with same qualified name but different type and verify type returned is from first attribute.
        AttributesImpl attributes = new AttributesImpl();
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String type1 = "type1";
        String value = "value";
        attributes.addAttribute(uri, localName, qname, type, value);
        attributes.addAttribute(uri, localName, qname, type1, value);
        Assert.assertEquals(attributes.getType(uri, localName), type, "type mismatched");
    }

    /**
     * Test getType with uri and localname.
     */
    @Test
    public void testGetTypeWithUriLocalName() {
        // Add 2 attributes with different localname and verify both present.
        AttributesImpl attributes = new AttributesImpl();
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String localName1 = "localname1";
        String qname = "qname";
        String type = "type";
        String type1 = "type1";
        String value = "value";
        attributes.addAttribute(uri, localName, qname, type, value);
        attributes.addAttribute(uri, localName1, qname, type1, value);
        Assert.assertEquals(attributes.getType(uri, localName), type, "type mismatched");
        Assert.assertEquals(attributes.getType(uri, localName1), type1, "type mismatched");
        Assert.assertNull(attributes.getType(uri, "invalid localname"), "type for given localname should not be present");
    }

    /**
     * Test getValue with index.
     */
    @Test
    public void testGetValueWithIndex() {
        // Add 2 attributes with same qualified name and verify value returned is from first attribute.
        AttributesImpl attributes = new AttributesImpl();
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value0 = "value0";
        String value1 = "value1";
        attributes.addAttribute(uri, localName, qname, type, value0);
        attributes.addAttribute(uri, localName, qname, type, value1);
        int index = attributes.getIndex(qname);
        Assert.assertEquals(attributes.getValue(index), value0, "value mismatched");

        // Verify invalid index returns null.
        Assert.assertNull(attributes.getValue(10), "value must be null");
    }

    /**
     * Test getValue with qualified name.
     */
    @Test
    public void testGetValueWithQName() {
        // Add 2 attributes with same qualified name but different value and verify value returned is from first attribute.
        AttributesImpl attributes = new AttributesImpl();
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value0 = "value0";
        String value1 = "value1";
        attributes.addAttribute(uri, localName, qname, type, value0);
        attributes.addAttribute(uri, localName, qname, type, value1);
        Assert.assertEquals(attributes.getValue(qname), value0, "value mismatched");

        // Verify getValue with non existent qname returns null.
        Assert.assertNull(attributes.getValue("invalidqname"), "value for nonexistent qname should return null");
    }

    /**
     * Test getValue with uri and localname.
     */
    @Test
    public void testGetValueWithUriLocalName() {
        // Add 2 attributes with different localname and verify both present.
        AttributesImpl attributes = new AttributesImpl();
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String localName1 = "localname1";
        String qname = "qname";
        String type = "type";
        String value = "value";
        String value1 = "value1";
        attributes.addAttribute(uri, localName, qname, type, value);
        attributes.addAttribute(uri, localName1, qname, type, value1);
        Assert.assertEquals(attributes.getValue(uri, localName), value, "value mismatched");
        Assert.assertEquals(attributes.getValue(uri, localName1), value1, "value mismatched");
        Assert.assertNull(attributes.getValue(uri, "invalid localname"), "value for given localname should not be present");
    }

    /**
     * Test clear.
     */
    @Test
    public void testClear() {
        AttributesImpl attributes = new AttributesImpl();
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        for (int i = 0; i < 10; i++) {
            attributes.addAttribute(uri, localName, qname + String.valueOf(i), type, value);
        }
        Assert.assertNotNull(attributes.getType(qname + "0"), "qname should not be empty before clear");
        Assert.assertEquals(attributes.getLength(), 10, "Length mismatched before clear operation");
        attributes.clear();
        Assert.assertEquals(attributes.getLength(), 0, "Length mismatched after clear operation");
        Assert.assertNull(attributes.getType(qname), "qname should be empty after clear");
    }

    /**
     * Test setAttributes.
     */
    @Test
    public void testSetAttributes() {
        AttributesImpl attributes = new AttributesImpl();
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        // Create attributes with same qualified name for even numbers.
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                attributes.addAttribute(uri, localName, qname + String.valueOf(i), type, value);
            } else {
                attributes.addAttribute(uri, localName, qname, type, value);
            }
        }
        Assert.assertEquals(attributes.getLength(), 10, "Expect length to be 0 before setAttributes");
        AttributesImpl newAttributes = new AttributesImpl();
        newAttributes.setAttributes(attributes);
        Assert.assertEquals(newAttributes.getLength(), attributes.getLength(), "After setAttribute both length should be equal");

        // Verify the index
        // Even number will have their corresponding index but odd numbers will always point to first attribute since qname is same
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                Assert.assertEquals(newAttributes.getIndex(qname + String.valueOf(i)), i);
            } else {
                Assert.assertEquals(newAttributes.getIndex(qname), 1,
                        "Odd number attributes will always point for first attributes since qname is same");
            }
        }
    }

    /**
     * Test setAttribute with invalid index throws exception.
     */
    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void testSetAttributeWithInvalidIndex() {
        AttributesImpl attributes = new AttributesImpl();
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        attributes.setAttribute(1, uri, localName, qname, type, value);
    }

    /**
     * Test setURI
     */
    @Test
    public void testSetURIWithIndex() {
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(uri, localName, qname, type, value);
        int index = attributes.getIndex(qname);
        Assert.assertEquals(attributes.getURI(index), uri, "URI should match");
        String newUri = "xmlns=\"http://www.w3.org/1999/xhtml/NEW\"";
        attributes.setURI(index, newUri);
        Assert.assertEquals(attributes.getURI(index), newUri, "set uri should have overwritten old url");
    }

    /**
     * Test setURI throws ArrayIndexOutOfBoundsException.
     */
    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void testSetURIWithInvalidIndex() {
        AttributesImpl attributes = new AttributesImpl();
        String newUri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        attributes.setURI(1, newUri);
    }

    /**
     * Test setLocalName.
     */
    @Test
    public void testSetLocalNameWithIndex() {
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(uri, localName, qname, type, value);
        int index = attributes.getIndex(qname);
        Assert.assertEquals(attributes.getLocalName(index), localName, "Local name should match");
        String newLocalName = "localname1";
        attributes.setLocalName(index, newLocalName);
        Assert.assertEquals(attributes.getLocalName(index), newLocalName, "set localname should have overwritten old localname");
    }

    /**
     * Test setLocalName throws ArrayIndexOutOfBoundsException.
     */
    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void testSetLocalNameWithInvalidIndex() {
        AttributesImpl attributes = new AttributesImpl();
        String localName = "localname";
        attributes.setLocalName(1, localName);
    }

    /**
     * Test setQName.
     */
    @Test
    public void testSetQNameWithIndex() {
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(uri, localName, qname, type, value);
        int index = attributes.getIndex(qname);
        Assert.assertEquals(attributes.getQName(index), qname, "qname name should match");
        String newQName = "qname1";
        attributes.setQName(index, newQName);
        Assert.assertEquals(attributes.getQName(index), newQName, "set qname should have overwritten old qname");
    }

    /**
     * Test setQName throws ArrayIndexOutOfBoundsException.
     */
    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void testSetQNameWithInvalidIndex() {
        AttributesImpl attributes = new AttributesImpl();
        String qname = "qname";
        attributes.setQName(1, qname);
    }

    /**
     * Test setType.
     */
    @Test
    public void testSetTypeWithIndex() {
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(uri, localName, qname, type, value);
        int index = attributes.getIndex(qname);
        Assert.assertEquals(attributes.getType(index), type, "type should match");
        String newType = "type1";
        attributes.setType(index, newType);
        Assert.assertEquals(attributes.getType(index), newType, "set type should have overwritten old type");
    }

    /**
     * Test setType throws ArrayIndexOutOfBoundsException.
     */
    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void testSetTypeWithInvalidIndex() {
        AttributesImpl attributes = new AttributesImpl();
        String type = "type";
        attributes.setType(1, type);
    }

    /**
     * Test setValue.
     */
    @Test
    public void testSetValueWithIndex() {
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(uri, localName, qname, type, value);
        int index = attributes.getIndex(qname);
        Assert.assertEquals(attributes.getValue(index), value, "value should match");
        String newValue = "value1";
        attributes.setValue(index, newValue);
        Assert.assertEquals(attributes.getValue(index), newValue, "set value should have overwritten old value");
    }

    /**
     * Test setValue throws ArrayIndexOutOfBoundsException.
     */
    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void testSetValueWithInvalidIndex() {
        AttributesImpl attributes = new AttributesImpl();
        String value = "value";
        attributes.setValue(1, value);
    }

    /**
     * Test getLength.
     */
    @Test
    public void testGetLength() {
        AttributesImpl attributes = new AttributesImpl();
        Assert.assertEquals(attributes.getLength(), 0);
    }

    /**
     * Test removeAttribute.
     */
    @Test
    public void testRemoveAttribute() {
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(uri, localName, qname, type, value);
        int index = attributes.getIndex(qname);
        Assert.assertEquals(attributes.getQName(index), qname, "qname should be same");
        attributes.removeAttribute(index);
        Assert.assertNull(attributes.getQName(index), "After removing attribute qname should be null");
        // Add 2 attributes and try removing it should shrink the array.
        attributes.addAttribute(uri, localName, qname, type, value);
        attributes.addAttribute(uri, localName, "qname1", type, value);
        Assert.assertEquals(attributes.getLength(), 2, "should have 2 attributes");
        Assert.assertEquals(attributes.getIndex(qname), 1, "Index should be 1");
        Assert.assertEquals(attributes.getIndex("qname1"), 2, "Index should be 2");
        index = attributes.getIndex(qname);
        attributes.removeAttribute(index);
        Assert.assertEquals(attributes.getLength(), 1, "should have 1 attribute");
        Assert.assertEquals(attributes.getIndex(qname), -1, "Index should be -1");
        Assert.assertEquals(attributes.getIndex("qname1"), 2, "Index should be 2");

        // Add 2 attributes with same qname and 1 with different qname, verify after remove it returns proper index.
        attributes.clear();
        attributes.addAttribute(uri, localName, qname, type, value);
        attributes.addAttribute(uri, localName, qname, type, value);
        attributes.addAttribute(uri, localName, "qname1", type, value);
        Assert.assertEquals(attributes.getLength(), 3, "should have 3 attributes");
        Assert.assertEquals(attributes.getIndex(qname), 0, "Index should be 0");
        Assert.assertEquals(attributes.getIndex("qname1"), 2, "Index should be 2");

        index = attributes.getIndex(qname);
        attributes.removeAttribute(index);
        Assert.assertEquals(attributes.getLength(), 2, "should have 1 attributes");
        Assert.assertEquals(attributes.getIndex(qname), 1, "Index should be 1");
        Assert.assertEquals(attributes.getIndex("qname1"), 2, "Index should be 2");

        // Add 2 attributes with same qname and verify after remove it returns proper index.
        attributes.clear();
        attributes.addAttribute(uri, localName, qname, type, value);
        attributes.addAttribute(uri, localName, qname, type, value);
        Assert.assertEquals(attributes.getLength(), 2, "should have 2 attributes");
        Assert.assertEquals(attributes.getIndex(qname), 0, "Index should be 0");

        index = attributes.getIndex(qname);
        attributes.removeAttribute(index);
        Assert.assertEquals(attributes.getLength(), 1, "should have 1 attributes");
        Assert.assertEquals(attributes.getIndex(qname), 1, "Index should be 1");

        // Add 2 attributes with different qname and verify after remove it returns proper index.
        attributes.clear();
        attributes.addAttribute(uri, localName, qname, type, value);
        attributes.addAttribute(uri, localName, "qname1", type, value);
        Assert.assertEquals(attributes.getLength(), 2, "should have 2 attributes");
        Assert.assertEquals(attributes.getIndex(qname), 0, "Index should be 0");
        Assert.assertEquals(attributes.getIndex("qname1"), 1, "Index should be 1");

        index = attributes.getIndex("qname1");
        attributes.removeAttribute(index);
        Assert.assertEquals(attributes.getLength(), 1, "should have 1 attributes");
        Assert.assertEquals(attributes.getIndex(qname), 0, "Index should be 0");
        Assert.assertEquals(attributes.getIndex("qname1"), -1, "Index should be -1");

        // Add 2 attributes with same qname and 1 with different qname,
        // Remove middle attribute and verify after first attribute returns same index.
        attributes.clear();
        attributes.addAttribute(uri, localName, qname, type, value);
        attributes.addAttribute(uri, localName, "qname1", type, value);
        attributes.addAttribute(uri, localName, qname, type, value);
        Assert.assertEquals(attributes.getLength(), 3, "should have 3 attributes");
        Assert.assertEquals(attributes.getIndex(qname), 0, "Index should be 0");
        Assert.assertEquals(attributes.getIndex("qname1"), 1, "Index should be 1");

        index = attributes.getIndex("qname1");
        attributes.removeAttribute(index);
        Assert.assertEquals(attributes.getLength(), 2, "should have 2 attributes");
        Assert.assertEquals(attributes.getIndex(qname), 0, "Index should be 0");
        Assert.assertEquals(attributes.getIndex("qname1"), -1, "Index should be -1");
    }

    /**
     * Test removeAttribute with invalid index.
     */
    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void testRemoveAttributeWithInvalidIndex() {
        AttributesImpl attributes = new AttributesImpl();
        attributes.removeAttribute(0);
    }

    /**
     * Test getIndex.
     */
    @Test
    public void testGetIndexWithQName() {
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String uri1 = "xmlns=\"http://www.w3.org/1999/xhtml/NEW\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        AttributesImpl attributes = new AttributesImpl();
        // After adding first attribute it should return 0.
        attributes.addAttribute(uri, localName, qname, type, value);
        Assert.assertEquals(attributes.getIndex(qname), 0, "Index should be 0");

        // Adding same qname with different value should return first attribute index.
        attributes.addAttribute(uri1, "localname1", qname, type, value);
        Assert.assertEquals(attributes.getURI(1), uri1, "new uri should exist in next item");
        Assert.assertEquals(attributes.getLocalName(1), "localname1", "new localname should exist in next item");
        int index = attributes.getIndex(qname);
        Assert.assertEquals(index, 0, "Index should be 0 after adding new attribute with same qname");

        // Adding different qname should return different index.
        attributes.addAttribute(uri1, "localname1", "qname1", type, value);
        Assert.assertEquals(attributes.getIndex("qname1"), 2, "This should be 3rd attribute with index 2");

        attributes.clear();
        // Verify index after calling setAttribute(index, ...) with same qname.
        attributes.addAttribute(uri, localName, qname, type, value);
        Assert.assertEquals(attributes.getQName(0), qname, "qname should be same");
        index = attributes.getIndex(qname);
        attributes.setAttribute(0, uri1, localName, qname, type, value);
        Assert.assertEquals(attributes.getQName(0), qname, "qname should be same");
        Assert.assertEquals(attributes.getURI(0), uri1, "uri should match with new value");
        Assert.assertEquals(attributes.getIndex(qname), index, "old index and new should be same for same qname");

        attributes.clear();
        // Verify index after calling setAttribute(index, ...) with different qname.
        attributes.addAttribute(uri, localName, qname, type, value);
        Assert.assertEquals(attributes.getQName(0), qname, "qname should be same");
        index = attributes.getIndex(qname);
        attributes.setAttribute(0, uri1, localName, "qname1", type, value);
        Assert.assertEquals(attributes.getURI(0), uri1, "uri should match with new value");
        Assert.assertEquals(attributes.getQName(0), "qname1", "qname should be the last qname");
        Assert.assertEquals(attributes.getIndex(qname), -1, "old value should be -1");
        Assert.assertEquals(attributes.getIndex("qname1"), 0, "new index should be 0");

        attributes.clear();
        // Verify doing multiple setAttribute on same index removes old entry.
        attributes.addAttribute(uri, localName, qname, type, value);
        attributes.addAttribute(uri, localName, "qname1", type, value);
        attributes.setAttribute(0, uri, localName, "qname2", type, value);
        attributes.setAttribute(0, uri, localName, "qname3", type, value);
        Assert.assertEquals(attributes.getIndex(qname), -1);
        Assert.assertEquals(attributes.getIndex("qname1"), 1);
        Assert.assertEquals(attributes.getIndex("qname2"), -1);
        Assert.assertEquals(attributes.getIndex("qname3"), 0);

        // Verify setAttributes() clears and keeps track of new index.
        AttributesImpl attributes1 = new AttributesImpl();
        attributes1.addAttribute(uri, localName, qname, type, value);
        attributes1.addAttribute(uri, localName, "qname1", type, value);
        Assert.assertEquals(attributes1.getIndex(qname), 0, "index should be 0");
        Assert.assertEquals(attributes1.getIndex("qname1"), 1, "index should be 1");

        AttributesImpl newAttributes = new AttributesImpl();
        newAttributes.addAttribute(uri, localName, "qname2", type, value);
        newAttributes.addAttribute(uri, localName, "qname3", type, value);
        attributes1.setAttributes(newAttributes);
        Assert.assertEquals(newAttributes.getIndex(qname), -1, "index for old entry should be -1");
        Assert.assertEquals(newAttributes.getIndex("qname1"), -1, "index for old entry should be -1");
        Assert.assertEquals(newAttributes.getIndex("qname2"), 0, "index for first entry should be 0");
        Assert.assertEquals(newAttributes.getIndex("qname3"), 1, "index for second entry should be 1");

        // Verify setAttribute() for same qname does not replace higher index with lower.
        attributes.clear();
        attributes.addAttribute(uri, localName, "qname1", type, value);
        attributes.addAttribute(uri, localName, qname, type, value);
        attributes.addAttribute(uri, localName, "qname2", type, value);
        attributes.addAttribute(uri, localName, "qname3", type, value);
        attributes.addAttribute(uri, localName, qname, type, value);
        Assert.assertEquals(attributes.getLength(), 5, "total length should be 5");
        Assert.assertEquals(attributes.getIndex("qname3"), 3, "index for qname3 should be 3");
        Assert.assertEquals(attributes.getIndex(qname), 1, "index for qname should be 1");
        attributes.setAttribute(3, uri, localName, qname, type, value);
        Assert.assertEquals(attributes.getIndex("qname3"), -1, "index for qname3 should be -1");
        Assert.assertEquals(attributes.getIndex(qname), 1, "index for qname should be 1");

        // Verify setAttribute() for same qname with lower index updates the lookup table.
        attributes.clear();
        attributes.addAttribute(uri, localName, "qname1", type, value);
        attributes.addAttribute(uri, localName, qname, type, value);
        Assert.assertEquals(attributes.getLength(), 2, "total length should be 2");
        Assert.assertEquals(attributes.getIndex("qname1"), 0, "index for qname1 should be 1");
        Assert.assertEquals(attributes.getIndex(qname), 1, "index for qname should be 1");
        attributes.setAttribute(0, uri, localName, qname, type, value);
        Assert.assertEquals(attributes.getIndex("qname1"), -1, "index for qname1 should be -1");
        Assert.assertEquals(attributes.getIndex(qname), 0, "index for qname should be 0");
    }

    /**
     * Test getIndex with uri and localname.
     */
    @Test
    public void testGetIndexWithURIAndLocalName() {
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(uri, localName, qname, type, value);
        Assert.assertEquals(attributes.getIndex(uri, localName), 0, "index should be 0");
        Assert.assertEquals(attributes.getIndex("uri1", localName), -1, "non existent uri, localname should return -1");
    }

    /**
     * Test getType with uri and localname.
     */
    @Test
    public void testGetTypeWithURIAndLocalName() {
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(uri, localName, qname, type, value);
        Assert.assertEquals(attributes.getType(uri, localName), type, "type should match");
        Assert.assertNull(attributes.getType("uri1", localName), "non existent uri, getType should return null");
    }

    /**
     * Test getValue with uri and localname.
     */
    @Test
    public void testGetValueWithURIAndLocalName() {
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(uri, localName, qname, type, value);
        Assert.assertEquals(attributes.getValue(uri, localName), value, "value should match");
        Assert.assertNull(attributes.getType("uri1", localName), "non existent uri, getValue should return null");
    }

    /**
     * Test getIndexes returns the proper index.
     */
    @Test
    public void testGetIndexes() {
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        AttributesImpl attributes = new AttributesImpl();
        for (int i = 0; i < 10; i++) {
            attributes.addAttribute(uri, localName, qname, type, value);
        }
        int index = 0;
        Iterator<Integer> iterator = attributes.getIndexes().iterator();
        while (iterator.hasNext()) {
            Assert.assertEquals(iterator.next().intValue(), index++, "Index should match");
        }
        for (int i = 0; i < 10; i++) {
            // Remove odd index elements.
            if (i % 2 != 0) {
                attributes.removeAttribute(i);
            }
        }
        // Verify only even index elements present.
        index = 0;
        Iterator<Integer> iterator1 = attributes.getIndexes().iterator();
        while (iterator1.hasNext()) {
            Assert.assertEquals(iterator1.next().intValue(), index, "Index should match");
            index += 2;
        }
    }

    /**
     * Test getReverseIndexes returns the proper index.
     */
    @Test
    public void testGetReverseIndexes() {
        String uri = "xmlns=\"http://www.w3.org/1999/xhtml\"";
        String localName = "localname";
        String qname = "qname";
        String type = "type";
        String value = "value";
        AttributesImpl attributes = new AttributesImpl();
        for (int i = 0; i < 10; i++) {
            attributes.addAttribute(uri, localName, qname, type, value);
        }
        int index = 9;
        Iterator<Integer> iterator = attributes.getReverseIndexes().iterator();
        while (iterator.hasNext()) {
            Assert.assertEquals(iterator.next().intValue(), index--, "Index should match");
        }
        for (int i = 0; i < 10; i++) {
            // Remove odd index elements.
            if (i % 2 != 0) {
                attributes.removeAttribute(i);
            }
        }
        // Verify only even index elements present.
        index = 8;
        Iterator<Integer> iterator1 = attributes.getReverseIndexes().iterator();
        while (iterator1.hasNext()) {
            Assert.assertEquals(iterator1.next().intValue(), index, "Index should match");
            index -= 2;
        }
    }
}
