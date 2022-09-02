mtype = { StringConvertfindFromStringMethod, StringConvertfindToStringMethod, StringConvertregisterMethods, StringConverttryRegisterGuava, StringConverttryRegisterJava8Optionals, StringConverttryRegisterJava8, StringConverttryRegisterThreeTenBackport, StringConverttryRegisterThreeTenOld, StringConverttryRegisterTimeZone, TypedStringConvertergetEffectiveType };
mtype = { s0, s1, s2, s3, s4, s6 }; /* s5_s4 in txt is s4 here */

mtype state = s0;
mtype event = StringConverttryRegisterGuava;

active proctype P() {
  printf("The state is now %e and event is %e\n", state, event)
  do
  :: if
     :: state == s0 && event == StringConverttryRegisterGuava -> /* s0 -> s1. outgoing edges from s1
                                                                 s1 s2 org.joda.convert.StringConvert.tryRegisterJava8Optionals()V
                                                                 */
                                                                 state = s1; event -> StringConverttryRegisterJava8Optionals
     :: state == s1 && event == StringConverttryRegisterGuava -> /* s1 -> s2. outgoing edges from s2
                                                                 s2 s2 org.joda.convert.TypedStringConverter.getEffectiveType()Ljava/lang/Class;
                                                                 s2 s3 org.joda.convert.StringConvert.tryRegisterTimeZone()V
                                                                 */
                                                                 if
                                                                 :: state = s2; event = TypedStringConvertergetEffectiveType
                                                                 :: state = s2; event = StringConverttryRegisterTimeZone
                                                                 fi
     :: state == s2 && event == TypedStringConvertergetEffectiveType -> /* s2 -> s2. outgoing edges from s2
                                                                        s2 s2 org.joda.convert.TypedStringConverter.getEffectiveType()Ljava/lang/Class;
                                                                        s2 s3 org.joda.convert.StringConvert.tryRegisterTimeZone()V
                                                                        */
                                                                        if
                                                                        :: state = s2; event = TypedStringConvertergetEffectiveType
                                                                        :: state = s2; event = StringConverttryRegisterTimeZone
                                                                        fi
     :: state == s2 && event == StringConverttryRegisterTimeZone -> /* s2 -> s3. outgoing edges from s3
                                                                    s3 s5_s4 org.joda.convert.StringConvert.tryRegisterJava8()V
                                                                    */
                                                                    state = s3; event = StringConverttryRegisterJava8
     :: state == s3 && event == StringConverttryRegisterJava8 -> /* s3 -> s4. outgoing edges from s4
                                                                 s5_s4 s5_s4 org.joda.convert.StringConvert.findToStringMethod(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;
                                                                 s5_s4 s5_s4 org.joda.convert.StringConvert.findFromStringMethod(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;
                                                                 s5_s4 s5_s4 org.joda.convert.StringConvert.registerMethods(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)V
                                                                 s5_s4 s6 org.joda.convert.StringConvert.tryRegisterThreeTenBackport()V
                                                                 */
                                                                 if
                                                                 :: state = s4 ; event = StringConvertfindToStringMethod
                                                                 :: state = s4 ; event = StringConvertfindFromStringMethod
                                                                 :: state = s4 ; event = StringConvertregisterMethods
                                                                 :: state = s4 ; event = StringConverttryRegisterThreeTenBackport
                                                                 fi
     :: state == s4 && event == StringConvertfindToStringMethod -> /* s4 -> s4. outgoing edges from s4
                                                                 s5_s4 s5_s4 org.joda.convert.StringConvert.findToStringMethod(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;
                                                                 s5_s4 s5_s4 org.joda.convert.StringConvert.findFromStringMethod(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;
                                                                 s5_s4 s5_s4 org.joda.convert.StringConvert.registerMethods(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)V
                                                                 s5_s4 s6 org.joda.convert.StringConvert.tryRegisterThreeTenBackport()V
                                                                 */
                                                                 if
                                                                 :: state = s4 ; event = StringConvertfindToStringMethod
                                                                 :: state = s4 ; event = StringConvertfindFromStringMethod
                                                                 :: state = s4 ; event = StringConvertregisterMethods
                                                                 :: state = s4 ; event = StringConverttryRegisterThreeTenBackport
                                                                 fi
     :: state == s4 && event == StringConvertfindFromStringMethod -> /* s4 -> s4. outgoing edges from s4
                                                                 s5_s4 s5_s4 org.joda.convert.StringConvert.findToStringMethod(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;
                                                                 s5_s4 s5_s4 org.joda.convert.StringConvert.findFromStringMethod(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;
                                                                 s5_s4 s5_s4 org.joda.convert.StringConvert.registerMethods(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)V
                                                                 s5_s4 s6 org.joda.convert.StringConvert.tryRegisterThreeTenBackport()V
                                                                 */
                                                                 if
                                                                 :: state = s4 ; event = StringConvertfindToStringMethod
                                                                 :: state = s4 ; event = StringConvertfindFromStringMethod
                                                                 :: state = s4 ; event = StringConvertregisterMethods
                                                                 :: state = s4 ; event = StringConverttryRegisterThreeTenBackport
                                                                 fi
     :: state == s4 && event == StringConvertregisterMethods -> /* s4 -> s4. outgoing edges from s4
                                                                 s5_s4 s5_s4 org.joda.convert.StringConvert.findToStringMethod(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;
                                                                 s5_s4 s5_s4 org.joda.convert.StringConvert.findFromStringMethod(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;
                                                                 s5_s4 s5_s4 org.joda.convert.StringConvert.registerMethods(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)V
                                                                 s5_s4 s6 org.joda.convert.StringConvert.tryRegisterThreeTenBackport()V
                                                                 */
                                                                 if
                                                                 :: state = s4 ; event = StringConvertfindToStringMethod
                                                                 :: state = s4 ; event = StringConvertfindFromStringMethod
                                                                 :: state = s4 ; event = StringConvertregisterMethods
                                                                 :: state = s4 ; event = StringConverttryRegisterThreeTenBackport
                                                                 fi
     :: state == s4 ; event == StringConverttryRegisterThreeTenBackport -> /* s4 -> s6. outgoing edges from s6
                                                                           s6 s0 org.joda.convert.StringConvert.tryRegisterThreeTenOld()V
                                                                           */
                                                                           state = s6; event = StringConverttryRegisterThreeTenOld
     :: state == s6 ; event == StringConverttryRegisterThreeTenOld -> /* s6 -> s0. outgoing edges from s0
                                                                      s0 s1 org.joda.convert.StringConvert.tryRegisterGuava()V
                                                                      */
                                                                      state = s0; event = StringConverttryRegisterGuava
     :: state == s0 -> break
     fi
     printf("The state is now %e and event is %e\n", state, event)
  od
}
