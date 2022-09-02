package edu.cornell.parser;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class DSISpecParserTest {

    @Test
    public void testOneObjectArgOneObjectReturn() {
        String method = "org.apache.commons.collections4.AbstractObjectTest."
                + "readExternalFormFromStream(Ljava/io/InputStream;)Ljava/lang/Object;";
        DSISpecParser parser = new DSISpecParser();
        SpecParts parts = parser.parse(method);
        assertEquals("OneObjectArgOneObjectReturn receiver",
                "org.apache.commons.collections4.AbstractObjectTest",
                    parts.getReceiver());
        assertEquals("OneObjectArgOneObjectReturn name", "readExternalFormFromStream", parts.getName());
        assertEquals("OneObjectArgOneObjectReturn returnType", "java.lang.Object", parts.getReturnType());
        assertEquals("OneObjectArgOneObjectReturn args",
                Arrays.asList(new String[]{"java.io.InputStream"}),
                parts.getArguments());
    }
}