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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import com.yahoo.tagchowder.templates.HTMLSchema;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Unit test for {@link Parser}.
 *
 */
public class ParserTest {

    /**
     * Parse an sample html.
     *
     * @throws IOException IOException
     * @throws SAXException SAXException
     */
    @Test
    public void testSampleHtml() throws IOException, SAXException {
        final String html = getSampleHtml("html.txt");
        final Parser parser = new Parser();
        final InputSource inSource = new InputSource(new StringReader(html));
        parser.parse(inSource);
    }

    /**
     * Parse an sample html with only double quote in public id inside !DOCTYPE.
     *
     * @throws IOException IOException
     * @throws SAXException SAXException
     */
    @Test
    public void testSampleHtmlWithDoubleQuotePublicId() throws IOException, SAXException {
        final String html = getSampleHtml("htmlDoubleQuoteOnlyPublicId.txt");
        final Parser parser = new Parser();
        final InputSource inSource = new InputSource(new StringReader(html));
        parser.parse(inSource);
    }

    /**
     * Parse an sample html with only double quote in public id inside without string intern !DOCTYPE.
     *
     * @throws IOException IOException when error
     * @throws SAXException SAXException when error
     */
    @Test
    public void testSampleHtmlWithDoubleQuotePublicIdWithoutIntern() throws IOException, SAXException {
        final String html = getSampleHtml("html5_badtag.txt");
        final Parser parser = new Parser();
        parser.setFeature(Parser.STRING_INTERNING_FEATURE, false);
        // Create a test schema
        // Set the intern to false with parser context object
        parser.setProperty(Parser.SCHEMA_PROPERTY, HTMLSchema.class);
        // Below feature will set the intern to true in parser context object
        parser.setFeature(Parser.STRING_INTERNING_FEATURE, true);
        final InputSource inSource = new InputSource(new StringReader(html));
        parser.parse(inSource);
        Assert.assertNull(parser.getTheParserContext(), "Parser context should be null");
        Assert.assertNull(parser.getTheSchema(), "Parser schema object should be null");
    }

    /**
     * Test for checking if the elements "a", "ul" and "ol" have correct model and memberships for html version 5.
     *
     */
    @Test
    public void testSchemaModelAndMembershipsForHTML5Support() {
        // Create a test schema
        final Schema testSchema = new HTMLSchema(new Parser());

        int currentModelForA = testSchema.getElementType("a").model();
        int expectedModelForA = 1073774596;
        Assert.assertEquals(currentModelForA, expectedModelForA, "Unexpected model found for \"a\"");

        int currentModelForUl = testSchema.getElementType("ul").model();
        int expectedModelForUl = 16384;
        Assert.assertEquals(currentModelForUl, expectedModelForUl, "Unexpected model found for \"ul\"");

        int currentModelForOl = testSchema.getElementType("ol").model();
        int expectedModelForOl = 16384;
        Assert.assertEquals(currentModelForOl, expectedModelForOl, "Unexpected model found for \"ol\"");

        int currentMembershipForUl = testSchema.getElementType("ul").memberOf();
        int expectedMembershipForUl = 16388;
        Assert.assertEquals(currentMembershipForUl, expectedMembershipForUl, "Unexpected membership found for \"ul\"");

        int currentMembershipForOl = testSchema.getElementType("ol").memberOf();
        int expectedMembershipForOl = 16388;
        Assert.assertEquals(currentMembershipForOl, expectedMembershipForOl, "Unexpected membership found for \"ol\"");
    }

    /**
     * Parse a sample html with only single quote in public id inside !DOCTYPE.
     *
     * @throws IOException IOException
     * @throws SAXException SAXException
     */
    @Test
    public void testSampleHtmlWithSingleQuotePublicId() throws IOException, SAXException {
        final String html = getSampleHtml("htmlSingleQuoteOnlyPublicId.txt");
        final Parser parser = new Parser();
        final InputSource inSource = new InputSource(new StringReader(html));
        parser.parse(inSource);
    }

    /**
     * Parse an sample html5.
     *
     * @throws IOException IOException
     * @throws SAXException SAXException
     */
    @Test
    public void test1SampleHtml5() throws IOException, SAXException {
        final String html = getSampleHtml("html5.txt");
        final Parser parser = new Parser();
        parser.setFeature(Parser.STRING_INTERNING_FEATURE, false);
        final InputSource inSource = new InputSource(new StringReader(html));
        parser.parse(inSource);
    }

    /**
     * Parse an sample html5.
     * Verify that the same parser object cannot be used to parse twice.
     *
     * @throws IOException IOException when error
     * @throws SAXException SAXException when error
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void test2SampleHtml5() throws IOException, SAXException {
        final String html = getSampleHtml("html5.txt");
        final Parser parser = new Parser();
        final InputSource inSource = new InputSource(new StringReader(html));
        parser.parse(inSource);
        final String htmlAgain = getSampleHtml("html.txt");
        final InputSource inSourceAgain = new InputSource(new StringReader(htmlAgain));
        parser.parse(inSourceAgain);
    }

    /**
     * Read HTML from file and returns string.
     *
     * @param filename file name.
     * @return contents of file as string.
     * @throws IOException error in reading file.
     */
    private String getSampleHtml(final String filename) throws IOException {
        final StringBuilder retString = new StringBuilder();
        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(
                ParserTest.class.getClassLoader().getResourceAsStream("html/" + filename), StandardCharsets.UTF_8))) {
            final char[] buffer = new char[8192];
            int read;
            while ((read = bReader.read(buffer, 0, buffer.length)) > 0) {
                retString.append(buffer, 0, read);
            }
        }
        return retString.toString();
    }
}