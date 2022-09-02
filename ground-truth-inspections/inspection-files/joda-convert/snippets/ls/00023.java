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
            parseRenameFile(lines, url);
        }
    } catch (Exception ex) {
        if (LOG) {
            ex.printStackTrace(System.err);
        }
        throw new IllegalStateException("Unable to load Renamed.ini: " + url + ": " + ex.getMessage(), ex);
    }
}


public Map<String, Enum<?>> getEnumRenames(Class<?> type) {//method b
    if (type == null) {
        throw new IllegalArgumentException("type must not be null");
    }
    Map<String, Enum<?>> map = enumRenames.get(type);//depends on rename file we loaded
    if (map == null) {
        return new HashMap<String, Enum<?>>();
    }
    return new HashMap<String, Enum<?>>(map);
}

private void parseRenameFile(List<String> lines, URL url) {//called in method-a
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
                renamedEnum(oldName, newEnum);//this affects enumRenames
            } else {
                throw new IllegalArgumentException("Renamed.ini must start with [types] or [enums]");
            }
        } catch (Exception ex) {
            // always print message, and then continue
            System.err.println("ERROR: Invalid Renamed.ini: " + url + ": " + ex.getMessage());
        }
    }
}

public void renamedEnum(String oldName, Enum<?> currentValue) {//we can populate our enumRenames without the class path
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



@Test
public void test_convertToString_inherit() {
    assertEquals("CEILING", StringConvert.INSTANCE.convertToString(RoundingMode.CEILING));
}

public void renamedEnum(String oldName, Enum<?> currentValue) {
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
