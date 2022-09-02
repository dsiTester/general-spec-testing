mtype = { DoubleMetaphoneDoubleMetaphoneResultappendAlternate, DoubleMetaphoneDoubleMetaphoneResultappend, DoubleMetaphoneDoubleMetaphoneResultappendPrimary, DoubleMetaphoneDoubleMetaphoneResultgetPrimary, DoubleMetaphoneconditionC0, DoubleMetaphonegetMaxCodeLen, DoubleMetaphonehandleAEIOUY, DoubleMetaphonehandleC, DoubleMetaphoneisSilentStart, DoubleMetaphoneisSlavoGermanic };
mtype = { s0, s1, s2, s3, s4 };

mtype state = s0;
mtype event = DoubleMetaphoneisSlavoGermanic;

active proctype P() {
  printf("The state is now %e and event is %e\n", state, event)
  do
  :: if
     :: state == s0 && event == DoubleMetaphoneisSlavoGermanic -> /* s0 -> s1. outgoing edges from s1:
                                                                  s1 s2 org.apache.commons.codec.language.DoubleMetaphone.isSilentStart(Ljava/lang/String;)Z
                                                               */
                                                               state = s1 ; event = DoubleMetaphoneisSilentStart
     :: state == s1 && event == DoubleMetaphoneisSilentStart -> /* s1 -> s2. outgoing edges from s2:
                                                                s2 s2 org.apache.commons.codec.language.DoubleMetaphone.getMaxCodeLen()I
                                                                s2 s3 org.apache.commons.codec.language.DoubleMetaphone.handleC(Ljava/lang/String;Lorg/apache/commons/codec/language/DoubleMetaphone$DoubleMetaphoneResult;I)I
                                                                */
                                                                if
                                                                :: state = s2; event = DoubleMetaphonegetMaxCodeLen
                                                                :: state = s2; event = DoubleMetaphonehandleC
                                                                fi
     :: state == s2 && event == DoubleMetaphonegetMaxCodeLen -> /* s2 -> s2. outgoing edges from s2:
                                                                s2 s2 org.apache.commons.codec.language.DoubleMetaphone.getMaxCodeLen()I
                                                                s2 s3 org.apache.commons.codec.language.DoubleMetaphone.handleC(Ljava/lang/String;Lorg/apache/commons/codec/language/DoubleMetaphone$DoubleMetaphoneResult;I)I
                                                                */
                                                                if
                                                                :: state = s2; event = DoubleMetaphonegetMaxCodeLen
                                                                :: state = s2; event = DoubleMetaphonehandleC
                                                                fi
     :: state == s2 && event == DoubleMetaphonehandleC -> /* s2 -> s3. outgoing edges from s3:
                                                          s3 s5_s4 org.apache.commons.codec.language.DoubleMetaphone.conditionC0(Ljava/lang/String;I)Z
                                                          */
                                                          state = s3 ; event = DoubleMetaphoneconditionC0
     :: state == s3 && event == DoubleMetaphoneconditionC0 -> /* s3 -> s4. outgoing edges from s4 (s5_s4):
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone.handleAEIOUY(Lorg/apache/commons/codec/language/DoubleMetaphone$DoubleMetaphoneResult;I)I
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.appendPrimary(C)V
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.appendAlternate(C)V
                                                              s5_s4 s0 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.getPrimary()Ljava/lang/String;
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.append(C)V
                                                              */
                                                              if
                                                              :: state = s4; event = DoubleMetaphonehandleAEIOUY
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappendPrimary
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappendAlternate
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultgetPrimary
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappend
                                                              fi
     :: state == s4 && event == DoubleMetaphonehandleAEIOUY -> /* s4 -> s4. outgoing edges from s4 (s5_s4):
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone.handleAEIOUY(Lorg/apache/commons/codec/language/DoubleMetaphone$DoubleMetaphoneResult;I)I
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.appendPrimary(C)V
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.appendAlternate(C)V
                                                              s5_s4 s0 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.getPrimary()Ljava/lang/String;
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.append(C)V
                                                              */
                                                              if
                                                              :: state = s4; event = DoubleMetaphonehandleAEIOUY
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappendPrimary
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappendAlternate
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultgetPrimary
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappend
                                                              fi
     :: state == s4 && event == DoubleMetaphoneDoubleMetaphoneResultappendPrimary -> /* s4 -> s4. outgoing edges from s4 (s5_s4):
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone.handleAEIOUY(Lorg/apache/commons/codec/language/DoubleMetaphone$DoubleMetaphoneResult;I)I
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.appendPrimary(C)V
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.appendAlternate(C)V
                                                              s5_s4 s0 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.getPrimary()Ljava/lang/String;
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.append(C)V
                                                              */
                                                              if
                                                              :: state = s4; event = DoubleMetaphonehandleAEIOUY
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappendPrimary
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappendAlternate
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultgetPrimary
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappend
                                                              fi
     :: state == s4 && event == DoubleMetaphoneDoubleMetaphoneResultappendAlternate -> /* s4 -> s4. outgoing edges from s4 (s5_s4):
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone.handleAEIOUY(Lorg/apache/commons/codec/language/DoubleMetaphone$DoubleMetaphoneResult;I)I
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.appendPrimary(C)V
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.appendAlternate(C)V
                                                              s5_s4 s0 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.getPrimary()Ljava/lang/String;
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.append(C)V
                                                              */
                                                              if
                                                              :: state = s4; event = DoubleMetaphonehandleAEIOUY
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappendPrimary
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappendAlternate
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultgetPrimary
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappend
                                                              fi
     :: state == s4 && event == DoubleMetaphoneDoubleMetaphoneResultgetPrimary -> /* s4 -> s0. outgoing edges from s0:
                                                                                  s0 s1 org.apache.commons.codec.language.DoubleMetaphone.isSlavoGermanic(Ljava/lang/String;)Z
                                                                                  */
                                                                                  state = s0; event = DoubleMetaphoneisSlavoGermanic
     :: state == s4 && event == DoubleMetaphoneDoubleMetaphoneResultappend -> /* s4 -> s4. outgoing edges from s4 (s5_s4):
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone.handleAEIOUY(Lorg/apache/commons/codec/language/DoubleMetaphone$DoubleMetaphoneResult;I)I
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.appendPrimary(C)V
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.appendAlternate(C)V
                                                              s5_s4 s0 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.getPrimary()Ljava/lang/String;
                                                              s5_s4 s5_s4 org.apache.commons.codec.language.DoubleMetaphone$DoubleMetaphoneResult.append(C)V
                                                              */
                                                              if
                                                              :: state = s4; event = DoubleMetaphonehandleAEIOUY
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappendPrimary
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappendAlternate
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultgetPrimary
                                                              :: state = s4; event = DoubleMetaphoneDoubleMetaphoneResultappend
                                                              fi
     :: state == s0 -> break /* FIXME: check; this might be wrong? */
     fi
     printf("The state is now %e and event is %e\n", state, event)
  od
}
