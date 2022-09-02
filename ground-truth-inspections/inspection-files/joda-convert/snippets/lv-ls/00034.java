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

public Map<String, Enum<?>> getEnumRenames(Class<?> type) {
    if (type == null) {
        throw new IllegalArgumentException("type must not be null");
    }
    Map<String, Enum<?>> map = enumRenames.get(type);
    if (map == null) {
        return new HashMap<String, Enum<?>>();
    }
    return new HashMap<String, Enum<?>>(map);
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
    return Enum.valueOf(type, name);//we throw an illegal argument exception here, because we are passing in "RUBBISH", which isn't in the enum
}

public <T extends Enum<T>> T lookupEnum(Class<T> type, String name) {
    if (type == null) {
        throw new IllegalArgumentException("type must not be null");
    }
    if (name == null) {
        throw new IllegalArgumentException("name must not be null");
    }
    Map<String, Enum<?>> map = getEnumRenames(type);
    Enum<?> value = map.get(name);
    if (value != null) {
        return type.cast(value);
    }
    return Enum.valueOf(type, name);
}


@Test(expected=RuntimeException.class)//we expect an exception, lv
public void test_Enum_invalidConstant() {
    TypedStringConverter<RoundingMode> test = StringConvert.create().findTypedConverter(RoundingMode.class);
    test.convertFromString(RoundingMode.class, "RUBBISH");//it is not possible to create an enum with the given string
}

public static RenameHandler create(boolean loadFromClasspath) {
    RenameHandler handler = new RenameHandler();
    if (loadFromClasspath) {
        handler.loadFromClasspath();//we can explicitly ignore method-a
    }
    return handler;
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
