private void loadFromClasspath() {
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
            List<String> lines = loadRenameFile(url);//method a
            parseRenameFile(lines, url);//null pointer due to null replacement
        }
    } catch (Exception ex) {//we catch and log this exception
        if (LOG) {
            ex.printStackTrace(System.err);
        }
        throw new IllegalStateException("Unable to load Renamed.ini: " + url + ": " + ex.getMessage(), ex);
    }
}

private void parseRenameFile(List<String> lines, URL url) {
    // format allows multiple [types] and [enums] so file can be merged
    boolean types = false;
    boolean enums = false;
    for (String line : lines) {
        try {
            if (line.equals("[types]")) {
                types = true;
                enums = false;
            } else if (line.equals("[enums]")) {
                types = false;
                enums = true;
            } else if (types) {
                int equalsPos = line.indexOf('=');
                if (equalsPos < 0) {
                    throw new IllegalArgumentException(
                            "Renamed.ini type line must be formatted as 'oldClassName = newClassName'");
                }
                String oldName = line.substring(0, equalsPos).trim();
                String newName = line.substring(equalsPos + 1).trim();
                Class<?> newClass = null;
                try {
                    newClass = StringConvert.loadType(newName);
                } catch (Throwable ex) {
                    if (LOG) {
                        ex.printStackTrace(System.err);
                    }
                    throw new IllegalArgumentException(
                            "Class.forName(" + newName + ") failed: " + ex.getMessage());
                }
                renamedType(oldName, newClass);
            } else if (enums) {
                int equalsPos = line.indexOf('=');
                int lastDotPos = line.lastIndexOf('.');
                if (equalsPos < 0 || lastDotPos < 0 || lastDotPos < equalsPos) {
                    throw new IllegalArgumentException(
                            "Renamed.ini enum line must be formatted as 'oldEnumConstantName = enumClassName.newEnumConstantName'");
                }
                String oldName = line.substring(0, equalsPos).trim();
                String enumClassName = line.substring(equalsPos + 1, lastDotPos).trim();
                String enumConstantName = line.substring(lastDotPos + 1).trim();
                @SuppressWarnings("rawtypes")
                Class<? extends Enum> enumClass = Class.forName(enumClassName).asSubclass(Enum.class);
                @SuppressWarnings("unchecked")
                Enum<?> newEnum = Enum.valueOf(enumClass, enumConstantName);
                renamedEnum(oldName, newEnum);
            } else {
                throw new IllegalArgumentException("Renamed.ini must start with [types] or [enums]");
            }
        } catch (Exception ex) {
            // always print message, and then continue
            System.err.println("ERROR: Invalid Renamed.ini: " + url + ": " + ex.getMessage());
        }
    }
}


private List<String> loadRenameFile(URL url) throws IOException {//method a, stateless 
    List<String> lines = new ArrayList<String>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), Charset.forName("UTF-8")));
    try {
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                lines.add(trimmed);
            }
        }
    } finally {
        reader.close();
    }
    return lines;
}

public <T extends Enum<T>> T lookupEnum(Class<T> type, String name) {//method b
    if (type == null) {
        throw new IllegalArgumentException("type must not be null");
    }
    if (name == null) {
        throw new IllegalArgumentException("name must not be null");
    }
    Map<String, Enum<?>> map = getEnumRenames(type);//depends on renames
    Enum<?> value = map.get(name);
    if (value != null) {
        return type.cast(value);
    }
    return Enum.valueOf(type, name);
}

@Test(expected=RuntimeException.class)//lv
public void test_Enum_invalidConstant() {//we expect method-b to fail
    TypedStringConverter<RoundingMode> test = StringConvert.create().findTypedConverter(RoundingMode.class);
    test.convertFromString(RoundingMode.class, "RUBBISH");//no enums are renamed in this test
}

@Test
public void test_convertFromString_inherit() {//ls
    assertEquals(RoundingMode.CEILING, StringConvert.INSTANCE.convertFromString(RoundingMode.class, "CEILING"));//no enums are renamed in this test
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

public static RenameHandler create(boolean loadFromClasspath) {//used to create a new rename handler
    RenameHandler handler = new RenameHandler();
    if (loadFromClasspath) {//explicit boolean condition to skip method a
        handler.loadFromClasspath();
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


