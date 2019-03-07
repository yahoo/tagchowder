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

package com.lafaspot.tagchowder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

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
}
