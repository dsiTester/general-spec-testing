/**
    * Tries to register ThreeTen ThreeTen/JSR-310 classes v0.6.3 and beyond.
    */
private void tryRegisterThreeTenOld() {//method a
    try {
        tryRegister("javax.time.Instant", "parse");
        tryRegister("javax.time.Duration", "parse");
        tryRegister("javax.time.calendar.LocalDate", "parse");
        tryRegister("javax.time.calendar.LocalTime", "parse");
        tryRegister("javax.time.calendar.LocalDateTime", "parse");
        tryRegister("javax.time.calendar.OffsetDate", "parse");
        tryRegister("javax.time.calendar.OffsetTime", "parse");
        tryRegister("javax.time.calendar.OffsetDateTime", "parse");
        tryRegister("javax.time.calendar.ZonedDateTime", "parse");
        tryRegister("javax.time.calendar.Year", "parse");
        tryRegister("javax.time.calendar.YearMonth", "parse");
        tryRegister("javax.time.calendar.MonthDay", "parse");
        tryRegister("javax.time.calendar.Period", "parse");
        tryRegister("javax.time.calendar.ZoneOffset", "of");
        tryRegister("javax.time.calendar.ZoneId", "of");
        tryRegister("javax.time.calendar.TimeZone", "of");

    } catch (Throwable ex) {
        if (LOG) {
            System.err.println("tryRegisterThreeTenOld: " + ex);
        }
    }
}

public <T> void register(final Class<T> cls, final ToStringConverter<T> toString, final FromStringConverter<T> fromString) {//method b
    if (fromString == null || toString == null) {
        throw new IllegalArgumentException("Converters must not be null");
    }
    register(cls, new TypedStringConverter<T>() {
        @Override
        public String convertToString(T object) {
            return toString.convertToString(object);
        }
        @Override
        public T convertFromString(Class<? extends T> cls, String str) {
            return fromString.convertFromString(cls, str);
        }
        @Override
        public Class<?> getEffectiveType() {
            return cls;
        }
    });
}

@Test(expected = IllegalStateException.class)//we expect to fail
public void test_registerFactory_cannotChangeSingleton() {
    //the creation of this instance calls method-a
    StringConvert.INSTANCE.register(//method b
        DistanceNoAnnotations.class, DISTANCE_TO_STRING_CONVERTER, DISTANCE_FROM_STRING_CONVERTER);
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
        tryRegisterTimeZone();
        tryRegisterJava8();
        tryRegisterThreeTenBackport();
        tryRegisterThreeTenOld();//method a
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
