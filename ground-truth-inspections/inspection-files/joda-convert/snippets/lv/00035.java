private void parseRenameFile(List<String> lines, URL url) {//method a
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
                renamedEnum(oldName, newEnum);//adds renamed enums
            } else {
                throw new IllegalArgumentException("Renamed.ini must start with [types] or [enums]");
            }
        } catch (Exception ex) {
            // always print message, and then continue
            System.err.println("ERROR: Invalid Renamed.ini: " + url + ": " + ex.getMessage());
        }
    }
}

public Class<?> lookupType(String name) throws ClassNotFoundException {//method b
    if (name == null) {
        throw new IllegalArgumentException("name must not be null");
    }
    Class<?> type = typeRenames.get(name);//there will not be a rename 1. None exists 2. We didn't properly load the renames because we delayed method-a
    if (type == null) {
        type = StringConvert.loadType(name);
    }
    return type;
}

static Class<?> loadType(String fullName) throws ClassNotFoundException {
    try {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader != null && !fullName.startsWith("[") ? loader.loadClass(fullName) : Class.forName(fullName);
    } catch (ClassNotFoundException ex) {
        return loadPrimitiveType(fullName, ex);
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

@Test(expected=RuntimeException.class)//we expect a failure
public void test_Enum_invalidConstant() {
    TypedStringConverter<RoundingMode> test = StringConvert.create().findTypedConverter(RoundingMode.class);
    test.convertFromString(RoundingMode.class, "RUBBISH");
}

public static RenameHandler create(boolean loadFromClasspath) {
    RenameHandler handler = new RenameHandler();
    if (loadFromClasspath) {
        handler.loadFromClasspath();//we can explicitly ignore method-a
    }
    return handler;
}