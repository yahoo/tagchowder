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
import java.util.List;

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
     * Parse an sample AMP html.txt and verify tag's attributes with AMP validation enabled.
     *
     * @throws IOException IOException
     * @throws SAXException SAXException
     */
    @Test
    public void testSampleHtmlWithAMPValidationFeatureEnable() throws IOException, SAXException {
        final String html = getSampleHtml("html.txt");
        final Parser parser = new Parser();
        final CustomHandler customHandler = new CustomHandler();
        parser.setContentHandler(customHandler);
        parser.setErrorHandler(customHandler);
        parser.setFeature(Parser.AMP_VALIDATION_FEATURE, true);
        parser.setDefaultBufferSize(html.length());
        final InputSource inSource = new InputSource(new StringReader(html));
        parser.parse(inSource);

        List<ParsedHtmlTag> parsedHtmlTagList = customHandler.getParsedHtmlTags();
        Assert.assertEquals(parsedHtmlTagList.size(), 31);

        //Assert true for tags having attributes in 'html.txt'
        ParsedHtmlTag parsedHtmlTag = parsedHtmlTagList.get(3);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "body");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("class"));

        parsedHtmlTag = parsedHtmlTagList.get(4);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "div");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("dir"));

        parsedHtmlTag = parsedHtmlTagList.get(10);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "div");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("id"));

        parsedHtmlTag = parsedHtmlTagList.get(11);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "hr");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("id"));

        parsedHtmlTag = parsedHtmlTagList.get(17);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "div");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("style"));
    }

    /**
     * Parse an sample AMP amphtml.txt and verify tag's attributes with AMP validation enabled.
     *
     * @throws IOException IOException
     * @throws SAXException SAXException
     */
    @Test
    public void testSampleAMPHtml() throws IOException, SAXException {
        final String html = getSampleHtml("amphtml.txt");
        final Parser parser = new Parser();
        final CustomHandler customHandler = new CustomHandler();
        parser.setContentHandler(customHandler);
        parser.setErrorHandler(customHandler);
        parser.setFeature(Parser.AMP_VALIDATION_FEATURE, true);
        parser.setDefaultBufferSize(html.length());
        final InputSource inSource = new InputSource(new StringReader(html));
        parser.parse(inSource);

        List<ParsedHtmlTag> parsedHtmlTagList = customHandler.getParsedHtmlTags();
        Assert.assertEquals(parsedHtmlTagList.size(), 21);

        //Assert true for these cases
        ParsedHtmlTag parsedHtmlTag = parsedHtmlTagList.get(0);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "html");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("⚡4email"));

        parsedHtmlTag = parsedHtmlTagList.get(6);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("[text]"));

        parsedHtmlTag = parsedHtmlTagList.get(7);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("[[text]]"));

        parsedHtmlTag = parsedHtmlTagList.get(8);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("[{text}]"));

        parsedHtmlTag = parsedHtmlTagList.get(9);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("{text}"));

        parsedHtmlTag = parsedHtmlTagList.get(10);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("{{text}}"));

        parsedHtmlTag = parsedHtmlTagList.get(11);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("{[text]}"));

        //Assert false for these cases
        parsedHtmlTag = parsedHtmlTagList.get(12);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("{[text}"));

        parsedHtmlTag = parsedHtmlTagList.get(13);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("{text]"));

        parsedHtmlTag = parsedHtmlTagList.get(14);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("{text"));

        parsedHtmlTag = parsedHtmlTagList.get(15);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("text}"));

        parsedHtmlTag = parsedHtmlTagList.get(16);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("[text"));

        parsedHtmlTag = parsedHtmlTagList.get(17);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("text]"));

        parsedHtmlTag = parsedHtmlTagList.get(18);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("⚡text]"));

        parsedHtmlTag = parsedHtmlTagList.get(19);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("⚡text}"));

        parsedHtmlTag = parsedHtmlTagList.get(20);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "p");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("⚡text"));
    }

    /**
     * Test AMP feature.
     *
     * @throws IOException IOException
     * @throws SAXException SAXException
     */
    @Test
    public void testAMPFeature() throws IOException, SAXException {
        final Parser parser = new Parser();
        Assert.assertEquals(parser.getFeature(Parser.AMP_VALIDATION_FEATURE), false);

        parser.setFeature(Parser.AMP_VALIDATION_FEATURE, true);
        Assert.assertEquals(parser.getFeature(Parser.AMP_VALIDATION_FEATURE), true);
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
    public void testSampleHtml5() throws IOException, SAXException {
        final String html = getSampleHtml("html5.txt");
        final Parser parser = new Parser();
        final InputSource inSource = new InputSource(new StringReader(html));
        parser.parse(inSource);
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

    /**
     * Parse a sample AMP ampVoidTagHtml.txt and verify tag's attributes with void tags.
     *
     * @throws IOException IOException
     * @throws SAXException SAXException
     */
    @Test
    public void testAmpVoidTagHtml() throws IOException, SAXException {
        final String html = getSampleHtml("ampVoidTagHtml.txt");
        final Parser parser = new Parser();
        final CustomHandler customHandler = new CustomHandler();
        parser.setContentHandler(customHandler);
        parser.setErrorHandler(customHandler);
        parser.setFeature(Parser.AMP_VALIDATION_FEATURE, true);
        parser.setDefaultBufferSize(html.length());
        final InputSource inSource = new InputSource(new StringReader(html));
        parser.parse(inSource);

        List<ParsedHtmlTag> parsedHtmlTagList = customHandler.getParsedHtmlTags();
        Assert.assertEquals(parsedHtmlTagList.size(), 12);

        //Assert true for these cases
        ParsedHtmlTag parsedHtmlTag = parsedHtmlTagList.get(0);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "html");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("⚡4email"));

        parsedHtmlTag = parsedHtmlTagList.get(6);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "input");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("required"));
        Assert.assertFalse(parsedHtmlTag.hasAttribute("_"));
        Assert.assertTrue(parsedHtmlTag.attrs().getValue("name").equals("required>"));

        parsedHtmlTag = parsedHtmlTagList.get(7);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "input");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("required"));
        Assert.assertFalse(parsedHtmlTag.hasAttribute("_"));
        Assert.assertTrue(parsedHtmlTag.attrs().getValue("name").equals("required >"));

        parsedHtmlTag = parsedHtmlTagList.get(8);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "input");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("required"));
        Assert.assertFalse(parsedHtmlTag.hasAttribute("_"));
        Assert.assertTrue(parsedHtmlTag.attrs().getValue("name").equals("required/>"));

        parsedHtmlTag = parsedHtmlTagList.get(9);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "input");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("required"));
        Assert.assertFalse(parsedHtmlTag.hasAttribute("_"));
        Assert.assertTrue(parsedHtmlTag.attrs().getValue("name").equals("required />"));

        parsedHtmlTag = parsedHtmlTagList.get(10);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "input");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("required"));
        Assert.assertFalse(parsedHtmlTag.hasAttribute("_"));
        Assert.assertTrue(parsedHtmlTag.attrs().getValue("name").equals("required / >"));

        parsedHtmlTag = parsedHtmlTagList.get(11);
        Assert.assertEquals(parsedHtmlTag.lowerName(), "input");
        Assert.assertTrue(parsedHtmlTag.hasAttribute("required"));
        Assert.assertFalse(parsedHtmlTag.hasAttribute("_"));
        Assert.assertTrue(parsedHtmlTag.attrs().getValue("name").equals("required //>"));
    }
}
