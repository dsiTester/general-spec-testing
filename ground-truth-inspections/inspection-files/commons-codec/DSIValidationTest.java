/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.codec.binary;

import org.apache.commons.codec.language.MatchRatingApproachEncoder;
import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.net.BCodec;
import org.apache.commons.codec.net.PercentCodec;
import org.apache.commons.codec.net.QCodec;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.codec.net.URLCodec;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.codec.binary.BaseNCodec.EOF;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class DSIValidationTest {

    private static final String STRING_FIXTURE = "Hello World";
    private static final String ENCODED_B16 = "CAFEBABEFFFF";
    @Test
    public void testDSI00064() throws Exception {
        // Hello World test.
        byte[] encoded = StringUtils.getBytesUtf8("48656C6C6F20576F726C64");
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        final Base16OutputStream out = new Base16OutputStream(byteOut, true, false);
        out.close();
        out.write(decoded);
        final byte[] output = byteOut.toByteArray();
        assertArrayEquals("Streaming chunked base16 encode", encoded, output);
        byteOut.close();
    }
    @Test
    public void testDSI00065() throws Exception {
        // Hello World test.
        byte[] encoded = StringUtils.getBytesUtf8("48656C6C6F20576F726C64");
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        final Base16OutputStream out = new Base16OutputStream(byteOut, true, false);
        out.eof();
        out.write(decoded);
        final byte[] output = byteOut.toByteArray();
        assertArrayEquals("Streaming chunked base16 encode", encoded, output);
        byteOut.close();
    }

    @Test
    public void testDSI00066() throws Exception {
        // Hello World test.
        byte[] encoded = StringUtils.getBytesUtf8("48656C6C6F20576F726C64");
        byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        final Base16OutputStream out = new Base16OutputStream(byteOut, true, false);
        out.flush();
        out.write(decoded);
        final byte[] output = byteOut.toByteArray();
        assertArrayEquals("Streaming chunked base16 encode", encoded, output);
        byteOut.close();
    }

    private static final String ENCODED_FOO = "MZXW6===";

    @Test
    public void testDSI00092() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        final Base32InputStream b32stream = new Base32InputStream(ins);
        final byte[] actualBytes = new byte[6];
        assertEquals(0, b32stream.skip(0));
        b32stream.read(actualBytes, 0, actualBytes.length);
        assertArrayEquals(actualBytes, new byte[] { 102, 111, 111, 0, 0, 0 });
        // End of stream reached
        b32stream.close();
        assertEquals(-1, b32stream.read());
    }

    @Test
    public void testDSI00097() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        final Base32InputStream b32stream = new Base32InputStream(ins);
        assertEquals(1, b32stream.available());
        b32stream.close();
        assertEquals(3, b32stream.skip(10));
        // End of stream reached
        assertEquals(0, b32stream.available());
        assertEquals(-1, b32stream.read());
        assertEquals(-1, b32stream.read());
        assertEquals(0, b32stream.available());
    }

    @Test
    public void testDSI00108() throws Exception {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            final Base32OutputStream out = new Base32OutputStream(bout);
            out.close();
            out.write(null, 0, 0);
            fail("Expcted Base32OutputStream.write(null) to throw a NullPointerException");
        } catch (final NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testDSI00154() throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Base32 codec = new Base32();
        BaseNCodec.Context context = new BaseNCodec.Context();
        int i = 10;
        final byte unencoded[] = new byte[i];
        System.out.println(codec.available(context));
        codec.encode(unencoded, 5, 1, context);
        System.out.println(codec.available(context));
    }

    @Test
    public void testDSI00169() throws IOException {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B16));
        final Base16InputStream b16Stream = new Base16InputStream(ins);
        final byte[] actualBytes = new byte[6];
        assertEquals(0, b16Stream.skip(0));
        b16Stream.read(actualBytes, 0, actualBytes.length);
        assertArrayEquals(actualBytes, new byte[] {(byte)202, (byte)254, (byte)186, (byte)190, (byte)255, (byte)255});
        // End of stream reached
        b16Stream.close(); // call to method-b
        assertEquals(-1, b16Stream.read()); // delayed call to method-a
    }

    @Test
    public void testDSI00170() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        final Base32InputStream b32stream = new Base32InputStream(ins);
        final byte[] actualBytes = new byte[6];
        assertEquals(0, b32stream.skip(0));
        b32stream.read(actualBytes, 0, actualBytes.length);
        assertArrayEquals(actualBytes, new byte[] { 102, 111, 111, 0, 0, 0 });
        // End of stream reached
        b32stream.close();
        assertEquals(-1, b32stream.read());
    }

    @Test
    public final void testDSI00257() {
        MatchRatingApproachEncoder m = new MatchRatingApproachEncoder();
        assertEquals("SMTBCD", m.encode("SSmithhabcd"));
    }

    @Test
    public void testDSI00262() {
        Metaphone m = new Metaphone();
        assertEquals( "PX", m.metaphone("PISH") );
    }

    @Test
    public void testDSI00297() throws Exception {
        final BCodec bcodec = new BCodec();
        final String plain = "Hello there";
        final String e = "=?UTF-8?B?SGVsbG8gdGhlcmU=?=";
        final String decoded = bcodec.decode(e);
        assertEquals("Basic B decoding test", plain, decoded);
        final String encoded = bcodec.encode(decoded);
        assertEquals("Basic B encoding test", e, encoded);
    }

    @Test
    public void testDSI00320() throws Exception {
        final BCodec bcodec = new BCodec();
        final String plain = "Hello there";
        assertEquals("Basic B decoding test", plain, bcodec.decode("=?UTF-8?B?SGVsbG8gdGhlcmU=?="));
        System.out.println(bcodec.getCharset());
    }

    @Test
    public void testDSI00345() throws Exception {
        final PercentCodec percentCodec = new PercentCodec(null, true);
        final String input = "abc123_-.*";
        final Object decoded = percentCodec.decode((Object) input.getBytes(StandardCharsets.UTF_8));
        final String decodedS = new String((byte[]) decoded, "UTF-8");
        final Object encoded = percentCodec.encode(decoded);
        final String encodedS = new String((byte[]) encoded, "UTF-8");
        assertEquals("Basic PercentCodec safe char encoding test", input, encodedS);
        assertEquals("Basic PercentCodec safe char decoding test", input, decodedS);
    }

    @Test
    public void testDSI00353() throws Exception {
        final PercentCodec percentCodec = new PercentCodec();
        final String input = "\u03B1\u03B2\u03B3\u03B4\u03B5\u03B6% ";
        final byte[] encoded = (byte[]) percentCodec.encode((Object) input.getBytes(StandardCharsets.UTF_8));
        final String encodedS = new String(encoded, "UTF-8");
        final byte[] decoded = percentCodec.decode(encoded);
        final String decodedS = new String(decoded, "UTF-8");
        assertEquals("Basic PercentCodec unsafe char encoding test", "%CE%B1%CE%B2%CE%B3%CE%B4%CE%B5%CE%B6%25 ", encodedS);
        assertEquals("Basic PercentCodec unsafe char decoding test", input, decodedS);
    }

    @Test
    public void testDSI00361() throws Exception {
        final String input = "abc123_-.*\u03B1\u03B2";
        final PercentCodec percentCodec = new PercentCodec("abcdef".getBytes("UTF-8"), false);
        final byte[] encoded = (byte[]) percentCodec.encode((Object) input.getBytes(StandardCharsets.UTF_8));
        final String encodedS = new String(encoded, "UTF-8");
        assertEquals("Configurable PercentCodec encoding test", "%61%62%63123_-.*%CE%B1%CE%B2", encodedS);
        final byte[] decoded = percentCodec.decode(encoded);
        assertEquals("Configurable PercentCodec decoding test", new String(decoded, "UTF-8"), input);
    }

    @Test
    public void testDSI00372() throws Exception {
        final QCodec qcodec = new QCodec();
        final String plain = "= Hello there =\r\n";
        final String e = "=?UTF-8?Q?=3D Hello there =3D=0D=0A?=";
        final String decoded = qcodec.decode(e);
        final String encoded = qcodec.encode(decoded);
        assertEquals("Basic Q decoding test",
                plain, decoded);
        assertEquals("Basic Q encoding test",
                e, encoded);
    }

    @Test
    public void testDSI00402() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "= Hello there =\r\n";
        final String e = "=3D Hello there =3D=0D=0A";
        final String decoded = qpcodec.decode(e);
        assertEquals("Basic quoted-printable decoding test",
                plain, decoded);
        assertEquals("Basic quoted-printable encoding test",
                e, qpcodec.encode(decoded));
    }

    @Test
    public void testDSI00416() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "1+1 =3D 2";
        String decoded = new String(qpcodec.decode(plain.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Basic quoted-printable decoding test",
                "1+1 = 2", decoded);
        System.out.println(qpcodec.getCharset());
    }

    @Test
    public void testDSI00419() throws Exception {
        final BCodec bcodec = new BCodec();
        final String plain = "Hello there";
        String e = "=?UTF-8?B?SGVsbG8gdGhlcmU=?=";
        final String decoded = bcodec.decode(e);
        assertEquals("Basic B decoding test", plain, decoded);
        assertEquals("Basic B encoding test", e, bcodec.encode(decoded));
    }

    @Test
    public void testDSI00430() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String plain = "Hello there!";
        final String e = "Hello+there%21";
        String decoded = urlCodec.decode(e);
        assertEquals("Basic URL decoding test",
                plain, decoded);
        assertEquals("Basic URL encoding test",
                e, urlCodec.encode(plain));
    }

    @Test
    public void testDSI00444() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String plain = "Hello there!";
        final String encoded = new String( URLCodec.encodeUrl(null, plain.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Basic URL decoding test",
                plain, new String(urlCodec.decode(encoded.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        assertEquals("UTF-8", urlCodec.getDefaultCharset());
    }
}
