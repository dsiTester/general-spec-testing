@Override
public Object convertFromString(Class<?> cls, String str) {//method b
    return java.util.UUID.fromString(str);
}


public String convertToString(Object object) {//method a
    return object.toString();
}

@Test
public void test_UUID() {
    JDKStringConverter test = JDKStringConverter.UUID;
    UUID uuid = UUID.randomUUID();
    doTest(test, UUID.class, uuid, uuid.toString());
}

public void doTest(JDKStringConverter test, Class<?> cls, Object obj, String str, Object objFromStr) {//ls
    assertEquals(cls, test.getType());
    assertEquals(str, test.convertToString(obj));//REPLACE_RETURN_WITH_EXPECTED_OUTPUT
    assertEquals(objFromStr, test.convertFromString(cls, str));
}

public void test_AtomicBoolean_false() {//u
    JDKStringConverter test = JDKStringConverter.ATOMIC_BOOLEAN;
    AtomicBoolean obj = new AtomicBoolean(false);
    assertEquals(AtomicBoolean.class, test.getType());
    assertEquals("false", test.convertToString(obj));// call to a; we delay method-a here and fail this assertion
    AtomicBoolean back = (AtomicBoolean) test.convertFromString(AtomicBoolean.class, "false"); // call to b
    assertEquals(false, back.get());
}
