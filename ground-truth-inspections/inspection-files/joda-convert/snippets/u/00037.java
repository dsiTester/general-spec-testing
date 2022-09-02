@Test(expected = ClassCastException.class)
public void test_convert_annotationSuperFactorySubViaSub2() {
    StringConvert test = new StringConvert();
    test.convertFromString(SuperFactorySub.class, "25m");//method a
}

public <T> T convertFromString(Class<T> cls, String str) {//method a
    if (str == null) {
        return null;
    }
    StringConverter<T> conv = findConverter(cls);//method b
    return conv.convertFromString(cls, str);
}

public <T> StringConverter<T> findConverter(final Class<T> cls) {//method b
    return findTypedConverter(cls);
}