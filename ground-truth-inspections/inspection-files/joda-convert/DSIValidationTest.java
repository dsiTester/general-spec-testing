package org.joda.convert;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DSIValidationTest {

    @Test
    public void testDSI00091() {
        StringConvert test = new StringConvert();
        //test.registerFactory(new Factory1());
        assertEquals(DistanceMethodMethod.class, test.findTypedConverter(DistanceMethodMethod.class).getEffectiveType());
    }

}