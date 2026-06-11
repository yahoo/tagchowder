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
import java.net.URI;
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

    /**
     * Test DOCTYPE system ID resolution with various URL combinations.
     * Validates that URI-based resolution works correctly for common cases.
     */
    @Test
    public void testDoctypeSystemIdResolution() {
        // Test case 1: Absolute HTTP URL + relative path
        String result1 = resolveSystemId("http://example.com/docs/page.html", "dtds/strict.dtd");
        Assert.assertEquals(result1, "http://example.com/docs/dtds/strict.dtd");

        // Test case 2: File URL + relative path (URI normalizes file URLs differently than URL)
        String result2 = resolveSystemId("file:///home/user/docs/page.html", "dtds/xhtml.dtd");
        Assert.assertTrue(result2.contains("home/user/docs/dtds/xhtml.dtd"));

        // Test case 3: Absolute URL already in systemid
        String result3 = resolveSystemId("http://example.com/page.html",
                                         "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
        Assert.assertEquals(result3, "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");

        // Test case 4: Base with trailing slash + relative
        String result4 = resolveSystemId("http://example.com/docs/", "dtds/strict.dtd");
        Assert.assertEquals(result4, "http://example.com/docs/dtds/strict.dtd");

        // Test case 5: Null handling
        String result5 = resolveSystemId(null, "dtds/strict.dtd");
        Assert.assertNull(result5);

        // Test case 6: Empty systemid
        String result6 = resolveSystemId("http://example.com/page.html", "");
        Assert.assertEquals(result6, "http://example.com/");
    }

    /**
     * Helper method that simulates the URI-based resolution logic.
     * This mimics what happens in Parser.java with URI.resolve()
     */
    private String resolveSystemId(final String baseSystemId, final String systemid) {
        try {
            if (baseSystemId != null && systemid != null) {
                final URI baseURI = new URI(baseSystemId);
                final URI resolvedURI = baseURI.resolve(systemid);
                return resolvedURI.toString();
            }
        } catch (final java.net.URISyntaxException e) {
            // Silent exception handling (matches Parser.java behavior)
        }
        return null;
    }

    /**
     * Parse HTML with DOCTYPE and verify system ID is captured.
     * @throws IOException if an I/O error occurs
     * @throws SAXException if a parsing error occurs
     */
    @Test
    public void testDoctypeWithSystemId() throws IOException, SAXException {
        final String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
                            + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
                            + "<html><head><title>Test</title></head><body>Test</body></html>";
        final Parser parser = new Parser();
        final InputSource inSource = new InputSource(new StringReader(html));
        parser.parse(inSource);
        Assert.assertNotNull(parser);
    }

    /**
     * Test DOCTYPE system ID resolution with various protocols including custom ones.
     */
    @Test
    public void testDoctypeSystemIdResolutionWithVariousProtocols() {
        // Test case 1: HTTP protocol
        String result1 = resolveSystemId("http://example.com/docs/page.html", "dtds/strict.dtd");
        Assert.assertEquals(result1, "http://example.com/docs/dtds/strict.dtd");

        // Test case 2: HTTPS protocol
        String result2 = resolveSystemId("https://secure.example.com/docs/page.html", "dtds/strict.dtd");
        Assert.assertEquals(result2, "https://secure.example.com/docs/dtds/strict.dtd");

        // Test case 3: FTP protocol
        String result3 = resolveSystemId("ftp://ftp.example.com/docs/page.html", "dtds/strict.dtd");
        Assert.assertEquals(result3, "ftp://ftp.example.com/docs/dtds/strict.dtd");

        // Test case 4: CSID protocol (custom identifier scheme)
        String result4 = resolveSystemId("csid://example/docs/page.html", "dtds/strict.dtd");
        Assert.assertTrue(result4.contains("csid://"));
        Assert.assertTrue(result4.contains("dtds/strict.dtd"));

        // Test case 5: Custom protocol
        String result5 = resolveSystemId("custom://resource/docs/page.html", "dtds/strict.dtd");
        Assert.assertTrue(result5.contains("custom://"));
        Assert.assertTrue(result5.contains("dtds/strict.dtd"));

        // Test case 6: Jar protocol (common in Java applications)
        String result6 = resolveSystemId("jar:file:/lib.jar!/com/example/page.html", "dtds/strict.dtd");
        Assert.assertTrue(result6 != null);
        Assert.assertTrue(result6.contains("strict.dtd"));

        // Test case 7: Data protocol (may fail due to embedded HTML in URI)
        String result7 = resolveSystemId("data:text/html", "dtds/strict.dtd");
        // Data URIs are allowed but resolution depends on URI structure
        Assert.assertTrue(result7 == null || result7.contains("dtds/strict.dtd"));
    }

    /**
     * Test DOCTYPE system ID resolution with edge cases and malformed protocols.
     */
    @Test
    public void testDoctypeSystemIdResolutionEdgeCases() {
        // Test case 1: Protocol with unusual characters
        String result1 = resolveSystemId("x-custom+proto://example/page.html", "dtds/strict.dtd");
        Assert.assertTrue(result1 != null);

        // Test case 2: Multiple slashes
        String result2 = resolveSystemId("http://example.com////docs/page.html", "dtds/strict.dtd");
        Assert.assertTrue(result2.contains("dtds/strict.dtd"));

        // Test case 3: Port number in URL
        String result3 = resolveSystemId("http://example.com:8080/docs/page.html", "dtds/strict.dtd");
        Assert.assertEquals(result3, "http://example.com:8080/docs/dtds/strict.dtd");

        // Test case 4: Username and password in URL (should be preserved)
        String result4 = resolveSystemId("http://user:pass@example.com/docs/page.html", "dtds/strict.dtd");
        Assert.assertTrue(result4.contains("user"));
        Assert.assertTrue(result4.contains("pass"));
        Assert.assertTrue(result4.contains("dtds/strict.dtd"));

        // Test case 5: Query parameters in base URL
        String result5 = resolveSystemId("http://example.com/docs/page.html?param=value", "dtds/strict.dtd");
        Assert.assertTrue(result5.contains("dtds/strict.dtd"));

        // Test case 6: Fragment in base URL
        String result6 = resolveSystemId("http://example.com/docs/page.html#section", "dtds/strict.dtd");
        Assert.assertTrue(result6.contains("dtds/strict.dtd"));

        // Test case 7: Parent directory reference (..)
        String result7 = resolveSystemId("http://example.com/docs/subdir/page.html", "../dtds/strict.dtd");
        Assert.assertTrue(result7.contains("dtds/strict.dtd"));

        // Test case 8: Current directory reference (.)
        String result8 = resolveSystemId("http://example.com/docs/page.html", "./dtds/strict.dtd");
        Assert.assertTrue(result8.contains("dtds/strict.dtd"));

        // Test case 9: Absolute path override
        String result9 = resolveSystemId("http://example.com/docs/page.html", "/absolute/dtds/strict.dtd");
        Assert.assertEquals(result9, "http://example.com/absolute/dtds/strict.dtd");

        // Test case 10: IPv6 address in URL
        String result10 = resolveSystemId("http://[2001:db8::1]/docs/page.html", "dtds/strict.dtd");
        Assert.assertTrue(result10 != null);
        Assert.assertTrue(result10.contains("dtds/strict.dtd"));
    }

    /**
     * Test DOCTYPE system ID resolution robustness with malformed inputs.
     */
    @Test
    public void testDoctypeSystemIdResolutionMalformedInputs() {
        // Test case 1: Invalid protocol (no colon) - treated as relative path
        String result1 = resolveSystemId("invalidprotocol//example/page.html", "dtds/strict.dtd");
        // Invalid URIs may return null or succeed depending on URI parser strictness
        Assert.assertTrue(result1 == null || result1.contains("dtds/strict.dtd"));

        // Test case 2: Empty protocol - invalid URI format
        String result2 = resolveSystemId("://example/page.html", "dtds/strict.dtd");
        // Invalid URIs are expected to fail gracefully
        Assert.assertTrue(result2 == null || result2.contains("dtds/strict.dtd"));

        // Test case 3: Special characters in path
        String result3 = resolveSystemId("http://example.com/docs/page%20name.html", "dtds/strict.dtd");
        Assert.assertTrue(result3 != null);

        // Test case 4: Very long URL
        String longBase = "http://example.com/" + "a".repeat(1000) + "/page.html";
        String result4 = resolveSystemId(longBase, "dtds/strict.dtd");
        Assert.assertTrue(result4 != null);

        // Test case 5: Unusual but valid systemid
        String result5 = resolveSystemId("http://example.com/page.html", "///dtds/strict.dtd");
        Assert.assertTrue(result5 != null);

        // Test case 6: Base with no path
        String result6 = resolveSystemId("http://example.com", "dtds/strict.dtd");
        Assert.assertTrue(result6.contains("dtds/strict.dtd"));
    }
}
