/**
    * Tries to register the subclasses of TimeZone.
    * Try various things, doesn't matter if the map entry gets overwritten.
    */
private void tryRegisterTimeZone() {
    try {
        registered.put(SimpleTimeZone.class, JDKStringConverter.TIME_ZONE);

    } catch (Throwable ex) {
        if (LOG) {
            System.err.println("tryRegisterTimeZone1: " + ex);
        }
    }
    try {
        TimeZone zone = TimeZone.getDefault();
        registered.put(zone.getClass(), JDKStringConverter.TIME_ZONE);

    } catch (Throwable ex) {
        if (LOG) {
            System.err.println("tryRegisterTimeZone2: " + ex);
        }
    }
    try {
        TimeZone zone = TimeZone.getTimeZone("Europe/London");
        registered.put(zone.getClass(), JDKStringConverter.TIME_ZONE);

    } catch (Throwable ex) {
        if (LOG) {
            System.err.println("tryRegisterTimeZone3: " + ex);
        }
    }
}

public String convertToString(Class<?> cls, Object object) {//method b
    if (object == null) {
        return null;
    }
    StringConverter<Object> conv = findConverterNoGenerics(cls);//relies on registered converters
    return conv.convertToString(object);
}

public StringConvert(boolean includeJdkConverters, StringConverterFactory... factories) {
    if (factories == null) {
        throw new IllegalArgumentException("StringConverterFactory array must not be null");
    }
    for (int i = 0; i < factories.length; i++) {
        if (factories[i] == null) {
            throw new IllegalArgumentException("StringConverterFactory array must not contain a null element");
        }
    }
    if (includeJdkConverters) {
        for (JDKStringConverter conv : JDKStringConverter.values()) {
            registered.put(conv.getType(), conv);
        }
        registered.put(Boolean.TYPE, JDKStringConverter.BOOLEAN);
        registered.put(Byte.TYPE, JDKStringConverter.BYTE);
        registered.put(Short.TYPE, JDKStringConverter.SHORT);
        registered.put(Integer.TYPE, JDKStringConverter.INTEGER);
        registered.put(Long.TYPE, JDKStringConverter.LONG);
        registered.put(Float.TYPE, JDKStringConverter.FLOAT);
        registered.put(Double.TYPE, JDKStringConverter.DOUBLE);
        registered.put(Character.TYPE, JDKStringConverter.CHARACTER);
        tryRegisterGuava();
        tryRegisterJava8Optionals();
        tryRegisterTimeZone();//method a, note all the other converters
        tryRegisterJava8();
        tryRegisterThreeTenBackport();
        tryRegisterThreeTenOld();
    }
    if (factories.length > 0) {
        this.factories.addAll(Arrays.asList(factories));
    }
    this.factories.add(AnnotationStringConverterFactory.INSTANCE);
    if (includeJdkConverters) {
        this.factories.add(EnumStringConverterFactory.INSTANCE);
        this.factories.add(TypeStringConverterFactory.INSTANCE);
    }
}

@Test(expected = IllegalArgumentException.class)//we expect to not find a converter
public void test_convertToString_nullClass() {//lv
    assertNull(StringConvert.INSTANCE.convertToString(null, "6"));
}

@Test
public void test_convertToString_withType_primitive2() {//ls
    int i = 6;
    assertEquals("6", StringConvert.INSTANCE.convertToString(Integer.TYPE, i));//an integer can be converted by other converters 
}
