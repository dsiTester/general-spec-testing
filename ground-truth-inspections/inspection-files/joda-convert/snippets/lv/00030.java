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
            parseRenameFile(lines, url);
        }
    } catch (Exception ex) {
        if (LOG) {
            ex.printStackTrace(System.err);
        }
        throw new IllegalStateException("Unable to load Renamed.ini: " + url + ": " + ex.getMessage(), ex);//exception here, since we delayed the loading of the file
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

@Override
public Object convertFromString(Class<?> cls, String str) {
    try {
        return RenameHandler.INSTANCE.lookupType(str);//method b
    } catch (ClassNotFoundException ex) {
        throw new RuntimeException("Unable to create type: " + str, ex);
    }
}

public Class<?> lookupType(String name) throws ClassNotFoundException {//method b
    if (name == null) {
        throw new IllegalArgumentException("name must not be null");
    }
    Class<?> type = typeRenames.get(name);//there will not be a rename for two reasons: 1. We did not create one in the test 2. We didn't properly load the renames because we delayed method-a so none of the renames in the classpath were loaded
    if (type == null) {
        type = StringConvert.loadType(name);//we will need to call loadType
    }
    return type;
}

static Class<?> loadType(String fullName) throws ClassNotFoundException {
    try {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader != null && !fullName.startsWith("[") ? loader.loadClass(fullName) : Class.forName(fullName); //our full name is RUBBISH so this will not work (there is no class called RUBBISH)
    } catch (ClassNotFoundException ex) {
        return loadPrimitiveType(fullName, ex);//we will attempt to load a primitive type
    }
}

private static Class<?> loadPrimitiveType(String fullName, ClassNotFoundException ex) throws ClassNotFoundException {
    if (fullName.equals("int")) {
        return int.class;
    } else if (fullName.equals("long")) {
        return long.class;
    } else if (fullName.equals("double")) {
        return double.class;
    } else if (fullName.equals("boolean")) {
        return boolean.class;
    } else if (fullName.equals("short")) {
        return short.class;
    } else if (fullName.equals("byte")) {
        return byte.class;
    } else if (fullName.equals("char")) {
        return char.class;
    } else if (fullName.equals("float")) {
        return float.class;
    } else if (fullName.equals("void")) {
        return void.class;
    }
    throw ex;// our name is RUBBISH, so we throw here
}

@Test(expected=RuntimeException.class)//we expect an exception
public void test_Class_fail() {
    JDKStringConverter.CLASS.convertFromString(Class.class, "RUBBISH");
}

public static RenameHandler create(boolean loadFromClasspath) {//used to create a new rename handler
    RenameHandler handler = new RenameHandler();
    if (loadFromClasspath) {//explicit boolean condition to skip method a
        handler.loadFromClasspath();
    }
    return handler;
}

public void renamedType(String oldName, Class<?> currentValue) {//public method to rename a type
    if (oldName == null) {
        throw new IllegalArgumentException("oldName must not be null");
    }
    if (currentValue == null) {
        throw new IllegalArgumentException("currentValue must not be null");
    }
    if (oldName.startsWith("java.") || oldName.startsWith("javax.") || oldName.startsWith("org.joda.")) {
        throw new IllegalArgumentException("oldName must not be a java.*, javax.* or org.joda.* type");
    }
    checkNotLocked();
    typeRenames.put(oldName, currentValue);
}
