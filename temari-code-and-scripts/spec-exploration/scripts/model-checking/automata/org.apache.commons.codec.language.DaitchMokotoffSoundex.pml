mtype = { cleanup, encodeObject, encodeString, soundexString, soundexStringBoolean, EPSILON };
mtype = { C0, C1, C2, C6, C8 }; /* we may not need start or end */

mtype state = C1;
mtype event = soundexStringBoolean;

active proctype P() {
  printf("The state is now %e and event is %e\n", state, event)
  do
  :: if
     :: state == C1 && event == soundexStringBoolean -> /* C1 -> C0. outgoing edges from C0
                                                        C0	C2	cleanup(Ljava/lang/String;)Ljava/lang/String;
                                                        */
                                                        state = C0; event = cleanup
     :: state == C0 && event == cleanup -> /* C0 -> C2. outgoing edges from C2
                                           C2	C1	soundex(Ljava/lang/String;)Ljava/lang/String;
                                           C2	C1	encode(Ljava/lang/String;)Ljava/lang/String;
                                           C2	C6	encode(Ljava/lang/Object;)Ljava/lang/Object;
                                           C2	C8	<END>
                                           */
                                           if
                                           :: state = C2; event = soundexString
                                           :: state = C2; event = encodeString
                                           :: state = C2; event = encodeObject
                                           :: state = C2; event = EPSILON
                                           fi
     :: state == C2 && event == soundexString -> /* C2 -> C1. outgoing edges from C1
                                                C1	C0	soundex(Ljava/lang/String;Z)[Ljava/lang/String;
                                                */
                                                state = C1; event = soundexStringBoolean
     :: state == C2 && event == encodeString -> /* C2 -> C1. outgoing edges from C1
                                                C1	C0	soundex(Ljava/lang/String;Z)[Ljava/lang/String;
                                                */
                                                state = C1; event = soundexStringBoolean
     :: state == C2 && event == encodeObject -> /* C2 -> C6. outgoing edges from C6
                                                C6	C1	encode(Ljava/lang/String;)Ljava/lang/String;
                                                C6	C8	<END>
                                                */
                                                if
                                                :: state = C6 ; event = encodeString
                                                :: state = C6 ; event = EPSILON
                                                fi
     :: state == C2 && event == EPSILON -> /* C2 -> C8. no outgoing edges from C8 */
                                           state = C8
     :: state == C6 && event == encodeString -> /* C6 -> C1. outgoing edges from C1
                                                C1	C0	soundex(Ljava/lang/String;Z)[Ljava/lang/String;
                                                */
                                                state = C1 ; event = soundexStringBoolean
     :: state == C6 && event == EPSILON -> /* C6 -> C8. no outgoing edges from C8 */
                                           state = C8
     :: state == C8 -> skip
     fi
     printf("The state is now %e and event is %e\n", state, event)
  od
}
