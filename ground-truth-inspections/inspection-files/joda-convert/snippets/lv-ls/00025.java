private void loadFromClasspath() {//method a
    URL url = null;
    try {
        // this is the new location of the file, working on Java 8, Java 9 class path and Java 9 module path
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = RenameHandler.class.getClassLoader();
        }
        if (LOG) {
            System.err.println("Loading from classpath: " + loader);
        }
        Enumeration<URL> en = loader.getResources("META-INF/org/joda/convert/Renamed.ini");
        while (en.hasMoreElements()) {
            url = en.nextElement();
            if (LOG) {
                System.err.println("Loading file: " + url);
            }
            List<String> lines = loadRenameFile(url);
            parseRenameFile(lines, url);//this adds renamed enums
        }
    } catch (Exception ex) {
        if (LOG) {
            ex.printStackTrace(System.err);
        }
        throw new IllegalStateException("Unable to load Renamed.ini: " + url + ": " + ex.getMessage(), ex);
    }
}

public <T extends Enum<T>> T lookupEnum(Class<T> type, String name) {//method b
    if (type == null) {
        throw new IllegalArgumentException("type must not be null");
    }
    if (name == null) {
        throw new IllegalArgumentException("name must not be null");
    }
    Map<String, Enum<?>> map = getEnumRenames(type);//depends on the renamed enums
    Enum<?> value = map.get(name);
    if (value != null) {
        return type.cast(value);
    }
    return Enum.valueOf(type, name);
}

public static RenameHandler create(boolean loadFromClasspath) {
    RenameHandler handler = new RenameHandler();
    if (loadFromClasspath) {
        handler.loadFromClasspath();//we may not need to call method-a at all, and even if we do the renamed enums may not be relevant, or there may be none
    }
    return handler;
}

public void renamedEnum(String oldName, Enum<?> currentValue) {//public method to register a renamed enum
    if (oldName == null) {
        throw new IllegalArgumentException("oldName must not be null");
    }
    if (currentValue == null) {
        throw new IllegalArgumentException("currentValue must not be null");
    }
    checkNotLocked();
    Class<?> enumType = currentValue.getDeclaringClass();
    Map<String, Enum<?>> perClass = enumRenames.get(enumType);
    if (perClass == null) {
        enumRenames.putIfAbsent(enumType, new ConcurrentHashMap<String, Enum<?>>(16, 0.75f, 2));
        perClass = enumRenames.get(enumType);
    }
    perClass.put(oldName, currentValue);
}

@Test(expected=RuntimeException.class)//lv
public void test_Enum_invalidConstant() {//we expect an exception
    TypedStringConverter<RoundingMode> test = StringConvert.create().findTypedConverter(RoundingMode.class);
    test.convertFromString(RoundingMode.class, "RUBBISH");//No enums are renamed in this test
}

@Test
public void test_convertToString_inherit() {//ls
    assertEquals("CEILING", StringConvert.INSTANCE.convertToString(RoundingMode.CEILING));//No enums are renamed in this test
}

@Test
public void test_Enum_withRename() {//this test did not mine the spec, but it actually relies on renamed enums
    TypedStringConverter<Status> test = StringConvert.create().findTypedConverter(Status.class);
    assertEquals("VALID", test.convertToString(Status.VALID));
    assertEquals("INVALID", test.convertToString(Status.INVALID));
    assertEquals(Status.VALID, test.convertFromString(Status.class, "VALID"));//we call convertFromString() before instanciating RenameHandler because we haven't made any renames yet, so we call method-b before method-a
    assertEquals(Status.INVALID, test.convertFromString(Status.class, "INVALID"));
    try {
        test.convertFromString(Status.class, "OK");
        fail();
    } catch (RuntimeException ex) {
        // expected
    }
    RenameHandler.INSTANCE.renamedEnum("OK", Status.VALID);
    assertEquals(Status.VALID, test.convertFromString(Status.class, "OK"));
    assertEquals(Status.VALID, test.convertFromString(Status.class, "VALID"));
    assertEquals(Status.INVALID, test.convertFromString(Status.class, "INVALID"));
}
