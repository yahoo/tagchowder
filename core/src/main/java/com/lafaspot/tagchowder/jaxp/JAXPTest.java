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

package com.lafaspot.tagchowder.jaxp;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;

/**
 * Trivial non-robust test class, to show that TagChowder can be accessed using JAXP interface.
 */
public class JAXPTest {
    /**
     * Main program.
     *
     * @param args arguments
     * @throws Exception exception
     */
    public static void main(final String[] args) throws Exception {
        new JAXPTest().test(args);
    }

    /**
     * Test case.
     *
     * @param args arguments
     * @throws Exception exception
     */
    private void test(final String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java " + getClass() + " [input-file]");
            System.exit(1);
        }
        File f = new File(args[0]);
        // System.setProperty("javax.xml.parsers.SAXParserFactory", SAXFactoryImpl.class.toString());
        System.setProperty("javax.xml.parsers.SAXParserFactory", "com.lafaspot.tagchowder..jaxp.SAXFactoryImpl");

        SAXParserFactory spf = SAXParserFactory.newInstance();
        System.out.println("Ok, SAX factory JAXP creates is: " + spf);
        System.out.println("Let's parse...");
        spf.newSAXParser().parse(f, new org.xml.sax.helpers.DefaultHandler());
        System.out.println("Done. And then DOM build:");

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);

        System.out.println("Succesfully built DOM tree from '" + f + "', -> " + doc);
    }
}
