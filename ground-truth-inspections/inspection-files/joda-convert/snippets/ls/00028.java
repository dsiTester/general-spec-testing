private List<String> loadRenameFile(URL url) throws IOException {//method a
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

public Map<String, Enum<?>> getEnumRenames(Class<?> type) {//method b
    if (type == null) {
        throw new IllegalArgumentException("type must not be null");
    }
    Map<String, Enum<?>> map = enumRenames.get(type);
    if (map == null) {
        return new HashMap<String, Enum<?>>();
    }
    return new HashMap<String, Enum<?>>(map);
}

@SuppressWarnings({"unchecked", "rawtypes"})
public Enum<?> convertFromString(Class<? extends Enum<?>> cls, String str) {
    return RenameHandler.INSTANCE.lookupEnum((Class) cls, str);//we need to instantiate the RenameHandler to call convertFromString for enums
}

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
            parseRenameFile(lines, url);//NPE
        }
    } catch (Exception ex) {
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
    for (String line : lines) {//lines is null due to null replacement, NPE
        //etc
    }
}

@Test
public void test_convertFromString_inherit() {
    assertEquals(RoundingMode.CEILING, StringConvert.INSTANCE.convertFromString(RoundingMode.class, "CEILING"));//calling convertFromString for an enum, the enum was not renamed
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
