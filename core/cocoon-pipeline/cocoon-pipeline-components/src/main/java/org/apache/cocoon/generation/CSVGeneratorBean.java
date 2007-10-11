/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.generation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.Source;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @cocoon.sitemap.component.documentation
 * A simple parser converting a Comma Separated Values (CSV) file into XML.
 * 
 * <p>This parser is controlled by the following sitemap parameters:</p>
 * 
 * <ul>
 *   <li>
 *     <b>process-headers</b>: whether the first line in the CSV is considered
 *     to be the header defining column names (the resulting output will be
 *     different if this is <i>true</i> or <i>false</i> (default: <i>false</i>).
 *   </li>
 *   <li>
 *     <b>max-records</b>: the maximum number of records to read
 *     (default: <i>-1</i> read all records).
 *   </li>
 *   <li>
 *     <b>encoding</b>: the character encoding (UTF-8, ISO8859-1, ...) used to
 *     interpret the input CSV source file (default: <i>system default</i>).
 *   </li>
 *   <li>
 *     <b>separator</b>: the field-separator character in the CSV file (comma,
 *     tab, ...) (default: <i>,</i> <small>comma</small>).
 *   </li>
 *   <li>
 *     <b>escape</b>: the character used to escape fields, or part of them, in
 *     the CSV file (default: <i>"</i> <small>quote</small>).
 *   </li>
 *   <li>
 *     <b>buffer-size</b>: the size of the buffer used for reading the source
 *     CSV file (default: <i>4096 bytes</i>).
 *   </li>
 * </ul>
 *
 * <p>The generated output will look something like the following:</p>
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="ISO-8859-1"?&gt;
 * &lt;csv:document xmlns:csv="http://apache.org/cocoon/csv/1.0"&gt;
 *   &lt;csv:header&gt;
 *     &lt;csv:column number="1"&gt;Column A&lt;/csv:column&gt;
 *     &lt;csv:column number="2"&gt;Column B&lt;/csv:column&gt;
 *     &lt;csv:column number="3"&gt;Column C&lt;/csv:column&gt;
 *   &lt;/csv:header&gt;
 *   &lt;csv:record number="1"&gt;
 *     &lt;csv:field number="1" column="Column A"&gt;Field A1&lt;/csv:field&gt;
 *     &lt;csv:field number="2" column="Column B"&gt;Field B1&lt;/csv:field&gt;
 *     &lt;csv:field number="3" column="Column C"&gt;Field C1&lt;/csv:field&gt;
 *   &lt;/csv:record&gt;
 *   &lt;csv:record number="2"&gt;
 *     &lt;csv:field number="1" column="Column A"&gt;Field A2&lt;/csv:field&gt;
 *     &lt;csv:field number="2" column="Column B"&gt;Field B2&lt;/csv:field&gt;
 *     &lt;csv:field number="3" column="Column C"&gt;Field C2&lt;/csv:field&gt;
 *   &lt;/csv:record&gt;
 * &lt;/csv:document&gt;
 * </pre>
 *
 * <p>Note that this generator has been thoroughly tested with CSV files generated
 * by <a href="http://office.microsoft.com/" target="_new">Microsoft Excel</a>.
 * Unfortunately no official CSV specification has ever been published by
 * any standard body, so the interpretation of the format might be slightly
 * different in cases.</p>
 */
public class CSVGeneratorBean extends FileGeneratorBean {

    /** <p>The namespace URI of XML generated by this instance.</p> */
    public static final String NAMESPACE_URI = "http://apache.org/cocoon/csv/1.0";
    /** <p>The namespace prefix of XML generated by this instance.</p> */
    public static final String NAMESPACE_PREFIX = "csv";

    /** <p>The default encoding configured in the Java VM.</p> */
    private static final String DEFAULT_ENCODING = 
        new InputStreamReader(new ByteArrayInputStream(new byte[0])).getEncoding();
    /** <p>The default field separator character.</p> */
    private static final String DEFAULT_SEPARATOR = ",";
    /** <p>The default field separator character.</p> */
    private static final String DEFAULT_ESCAPE = "\"";
    /** <p>The default field separator character.</p> */
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final int UNLIMITED_MAXRECORDS = -1;
    /** <p>A string used for indenting.</p> */
    private static final char INDENT_STRING[] = "\n          ".toCharArray();

    /** <p>The encoding used to read the CSV resource from a stream.</p> */
    private String encoding = DEFAULT_ENCODING;
    /** <p>The character used to separate fields.</p> */
    private char separator = DEFAULT_SEPARATOR.charAt(0);
    /** <p>The character used to initiate and terminate esacaped sequences.</p> */
    private char escape = DEFAULT_ESCAPE.charAt(0);
    /** <p>The size of the buffer used to read the input.</p> */
    private int buffersize = DEFAULT_BUFFER_SIZE;
    /** <p>The current field (column) number in the current record.</p> */
    private int fieldnumber = 1;
    /** <p>The current record (line) number in the current CSV.</p> */
    private int recordnumber = 1;
    /** <p>The maximum number of records to read (-1 = read all records)</p> */
    private int maxrecords;
    /** <p>A flag indicating whether the &lt;record&gt; tag was opened.</p> */
    private boolean openrecord = false;
    /** <p>The character buffer for the current field.</p> */
    private CharArrayWriter buffer = null;
    /** <p>A map of all known columns or null if no headers are processed.</p> */
    private Map columns = null;

    /**
     * <p>Create a new {@link CSVGeneratorBean} instance.</p>
     */
    public CSVGeneratorBean() {
        super();
    }

    /**
     * @see org.apache.cocoon.generation.FileGenerator#dispose()
     */
    public void dispose() {
        super.dispose();

        this.encoding = DEFAULT_ENCODING;
        this.separator = DEFAULT_SEPARATOR.charAt(0);
        this.escape = DEFAULT_ESCAPE.charAt(0);
        this.buffersize = DEFAULT_BUFFER_SIZE;
        this.buffer = null;
        this.columns = null;
        this.recordnumber = 1;
        this.fieldnumber = 1;
        this.openrecord = false;
    }

    /**
     * <p>Setup this {@link CSVGeneratorBean} instance.</p>
     */
    public void setup(SourceResolver resolver, Map object_model, String source,
                      Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, object_model, source, parameters);

        boolean header = parameters.getParameterAsBoolean("process-headers", false);

        this.encoding = parameters.getParameter("encoding", DEFAULT_ENCODING);
        this.separator = parameters.getParameter("separator", DEFAULT_SEPARATOR).charAt(0);
        this.escape = parameters.getParameter("escape", DEFAULT_ESCAPE).charAt(0);
        this.buffersize = parameters.getParameterAsInteger("buffer-size", DEFAULT_BUFFER_SIZE);
        this.maxrecords = parameters.getParameterAsInteger("max-records", UNLIMITED_MAXRECORDS);
        this.buffer = new CharArrayWriter();
        this.columns =  (header ? new HashMap() : null);
        this.recordnumber = (header ? 0 : 1);
        this.fieldnumber = 1;
        this.openrecord = false;
    }

    /**
     * <p>Generate the unique key.</p>
     */
    public Serializable getKey() {
        StringBuffer key = new StringBuffer(this.inputSource.getURI());
        if (this.columns != null) key.append("headers");
        key.append(separator);
        key.append(maxrecords);
        key.append(escape);
        return key;
    }

    /**
     * <p>Generate XML data from a Comma Separated Value resource.</p>.
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {

        /* Create a new Reader correctly decoding the source stream */
        CSVReader csv = new CSVReader(this.inputSource, this.encoding, this.buffersize);

        try {
            /* Start the document */
            this.consumer.setDocumentLocator(csv);
            this.consumer.startDocument();
            this.consumer.startPrefixMapping(NAMESPACE_PREFIX, NAMESPACE_URI);
            this.indent(0);
            this.startElement("document");

            /* Allocate buffer and status for parsing */
            boolean unescaped = true;
            int prev = -1;
            int curr = -1;

            /* Parse the file reading characters one-by-one */
            while ((curr = csv.read()) >= 0 && (this.maxrecords == UNLIMITED_MAXRECORDS || recordnumber <= this.maxrecords)) {

                /* Process any occurrence of the escape character */
                if (curr == this.escape) {
                    if ((unescaped) && (prev == this.escape)) {
                        this.buffer.write(this.escape);
                    }
                    unescaped = ! unescaped;
                    prev = curr;
                    continue;
                }

                /* Process any occurrence of the field separator */
                if ((unescaped) && (curr == this.separator)) {
                    this.dumpField();
                    prev = curr;
                    continue;
                }

                /* Process newline characters */
                if ((unescaped) && ((curr == '\r') || (curr == '\n'))) {
                    this.dumpField();
                    this.dumpRecord();

                    /* Record numbering */
                    if (((curr == '\n') && (prev != '\r')) || (curr == '\r')) {
                        this.recordnumber ++;
                    }
                    
                    /* Nothing else to do */
                    prev = curr;
                    continue;
                }

                /* Any other character simply gets added to the buffer */
                this.buffer.write(curr);
                prev = curr;
            }

            /* Terminate any hanging open record element (just in case) */
            this.dumpField();
            this.dumpRecord();

            /* Terminate the document */
            this.indent(0);
            this.endElement("document");
            this.consumer.endPrefixMapping(NAMESPACE_PREFIX);
            this.consumer.endDocument();

        } finally {
            csv.close();
        }
    }

    
    private void dumpField()
    throws SAXException {
        if (this.buffer.size() < 1) {
            this.fieldnumber ++;
            return;
        }

        if (! this.openrecord) {
            this.indent(4);

            if (this.recordnumber > 0) {
                AttributesImpl attributes = new AttributesImpl();
                String value = Integer.toString(this.recordnumber);
                attributes.addCDATAAttribute("number", value);
                this.startElement("record", attributes);
            } else {
                this.startElement("header");
            }
            this.openrecord = true;
        }

        /* Enclode the field in the proper element */
        String element = "field";
        char array[] = this.buffer.toCharArray();
        this.indent(8);

        AttributesImpl attributes = new AttributesImpl();
        String value = Integer.toString(this.fieldnumber);
        attributes.addCDATAAttribute("number", value);

        if (this.recordnumber < 1) {
            this.columns.put(new Integer(this.fieldnumber), new String(array));
            element = "column";
        } else if (this.columns != null) {
            String header = (String) this.columns.get(new Integer(this.fieldnumber));
            if (header != null) {
                attributes.addCDATAAttribute("column", header);
            }
        }

        this.startElement(element, attributes);
        this.consumer.characters(array, 0, array.length);
        this.endElement(element);
        this.buffer.reset();

        this.fieldnumber ++;
    }

    private void dumpRecord()
    throws SAXException {
        if (this.openrecord) {
            this.indent(4);
            if (this.recordnumber > 0) {
                this.endElement("record");
            } else {
                this.endElement("header");
            }
            this.openrecord = false;
        }
        this.fieldnumber = 1;
    }

    private void indent(int level)
    throws SAXException {
        this.consumer.characters(INDENT_STRING, 0, level + 1);
    }

    private void startElement(String name)
    throws SAXException {
        this.startElement(name, XMLUtils.EMPTY_ATTRIBUTES);
    }

    private void startElement(String name, Attributes atts)
    throws SAXException {
        if (name == null) throw new NullPointerException("Null name");
        String qual = NAMESPACE_PREFIX + ':' + name;
        this.consumer.startElement(NAMESPACE_URI, name, qual, (atts == null ? XMLUtils.EMPTY_ATTRIBUTES : atts));
    }

    private void endElement(String name)
    throws SAXException {
        String qual = NAMESPACE_PREFIX + ':' + name;
        this.consumer.endElement(NAMESPACE_URI, name, qual);
    }

    private static final class CSVReader extends Reader implements Locator {
        
        private String uri = null;
        private Reader input = null;
        private int column = 1;
        private int line = 1;
        private int last = -1;

        protected CSVReader(Source source, String encoding, int buffer)
        throws IOException {
            InputStream stream = source.getInputStream();
            Reader reader = new InputStreamReader(stream, encoding);
            this.input = new BufferedReader(reader, buffer);
            this.uri = source.getURI();
        }

        public String getPublicId() {
            return null;
        }

        public String getSystemId() {
            return this.uri;
        }

        public int getLineNumber() {
            return this.line;
        }

        public int getColumnNumber() {
            return this.column;
        }

        public void close()
        throws IOException {
            this.input.close();
        }
        
        public int read()
        throws IOException {
            int c = this.input.read();
            if (c < 0) return c;

            if (((c == '\n') && (this.last != '\r')) || (c == '\r')) {
                this.column = 1;
                this.line ++;
            }

            this.last = c;
            return c;
        }

        public int read(char b[], int o, int l)
        throws IOException {
            if (b == null) throw new NullPointerException();
            if ((o<0)||(o>b.length)||(l<0)||((o+l)>b.length)||((o+l)<0)) {
                throw new IndexOutOfBoundsException();
            }
            if (l == 0) return 0;

            int c = read();
            if (c == -1) return -1;
            b[o] = (char)c;

            int i = 1;
            try {
                for (i = 1; i < l ; i++) {
                    c = read();
                    if (c == -1) break;
                    b[o + i] = (char)c;
                }
            } catch (IOException ee) {
                return i;
            }
            return i;
        }
    }
}
