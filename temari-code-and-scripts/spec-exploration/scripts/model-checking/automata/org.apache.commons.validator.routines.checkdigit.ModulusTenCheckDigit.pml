mtype = { calculate, getModulus, isValid, EPSILON };
mtype = { start, C0, C1, C2, C3, C4 }; /* we may not need start or end */

mtype state = start;
mtype event = EPSILON;

active proctype P() {
  printf("The state is now %e and event is %e\n", state, event)
  do
  :: if
     :: state == start && event == EPSILON -> /* real start state is C3. outgoing edges from C3
                                              C3	C1	calculate(Ljava/lang/String;)Ljava/lang/String;
                                              C3	C2	isValid(Ljava/lang/String;)Z
                                              C3	C0	getModulus()I
                                              */
                                              if
                                              :: state = C3 ; event = calculate
                                              :: state = C3 ; event = isValid
                                              :: state = C3 ; event = getModulus
                                              fi
     :: state == C3 && event == calculate -> /* C3 -> C1. outgoing edges from C1
                                             C1	C4	<END>
                                             */
                                             state = C1 ; event = EPSILON
     :: state == C3 && event == isValid -> /* C3 -> C2. outgoing edges from C2
                                           C2	C0	getModulus()I
                                           C2	C2	isValid(Ljava/lang/String;)Z
                                           C2	C2	calculate(Ljava/lang/String;)Ljava/lang/String;
                                           C2	C4	<END>
                                           */
                                           if
                                           :: state = C2 ; event = getModulus
                                           :: state = C2 ; event = isValid
                                           :: state = C2 ; event = calculate
                                           :: state = C2 ; event = EPSILON
                                           fi
     :: state == C3 && event == getModulus -> /* C3 -> C0. outgoing edges from C0
                                              C0	C1	calculate(Ljava/lang/String;)Ljava/lang/String;
                                              C0	C0	getModulus()I
                                              C0	C0	isValid(Ljava/lang/String;)Z
                                              C0	C4	<END>
                                              */
                                              if
                                              :: state = C0 ; event = calculate
                                              :: state = C0 ; event = getModulus
                                              :: state = C0 ; event = isValid
                                              :: state = C0 ; event = EPSILON
                                              fi
     :: state == C1 && event == EPSILON -> /* C1 -> C4. C4 does not have any outgoing edges. */
                                           state = C4
     :: state == C2 && event == getModulus -> /* C2 -> C0. outgoing edges from C0
                                              C0	C1	calculate(Ljava/lang/String;)Ljava/lang/String;
                                              C0	C0	getModulus()I
                                              C0	C0	isValid(Ljava/lang/String;)Z
                                              C0	C4	<END>
                                              */
                                              if
                                              :: state = C0 ; event = calculate
                                              :: state = C0 ; event = getModulus
                                              :: state = C0 ; event = isValid
                                              :: state = C0 ; event = EPSILON
                                              fi
     :: state == C2 && event == isValid -> /* C2 -> C2. outgoing edges from C2
                                           C2	C0	getModulus()I
                                           C2	C2	isValid(Ljava/lang/String;)Z
                                           C2	C2	calculate(Ljava/lang/String;)Ljava/lang/String;
                                           C2	C4	<END>
                                           */
                                           if
                                           :: state = C2 ; event = getModulus
                                           :: state = C2 ; event = isValid
                                           :: state = C2 ; event = calculate
                                           :: state = C2 ; event = EPSILON
                                           fi
     :: state == C2 && event == calculate -> /* C2 -> C2. outgoing edges from C2
                                             C2	C0	getModulus()I
                                             C2	C2	isValid(Ljava/lang/String;)Z
                                             C2	C2	calculate(Ljava/lang/String;)Ljava/lang/String;
                                             C2	C4	<END>
                                             */
                                           if
                                           :: state = C2 ; event = getModulus
                                           :: state = C2 ; event = isValid
                                           :: state = C2 ; event = calculate
                                           :: state = C2 ; event = EPSILON
                                           fi
     :: state == C0 && event == calculate -> /* C0 -> C1. outgoing edges from C1
                                             C1	C4	<END>
                                             */
                                             state = C1 ; event = EPSILON
     :: state == C0 && event == getModulus -> /* C0 -> C0. outgoing edges from C0
                                              C0	C1	calculate(Ljava/lang/String;)Ljava/lang/String;
                                              C0	C0	getModulus()I
                                              C0	C0	isValid(Ljava/lang/String;)Z
                                              C0	C4	<END>
                                              */
                                              if
                                              :: state = C0 ; event = calculate
                                              :: state = C0 ; event = getModulus
                                              :: state = C0 ; event = isValid
                                              :: state = C0 ; event = EPSILON
                                              fi
     :: state == C0 && event == isValid -> /* C0 -> C0. outgoing edges from C0
                                              C0	C1	calculate(Ljava/lang/String;)Ljava/lang/String;
                                              C0	C0	getModulus()I
                                              C0	C0	isValid(Ljava/lang/String;)Z
                                              C0	C4	<END>
                                              */
                                              if
                                              :: state = C0 ; event = calculate
                                              :: state = C0 ; event = getModulus
                                              :: state = C0 ; event = isValid
                                              :: state = C0 ; event = EPSILON
                                              fi
     :: state == C0 && event == EPSILON -> /* C0 -> C4. C4 does not have any outgoing edges. */
                                           state = C4
     :: state == C4 -> skip
     fi
     printf("The state is now %e and event is %e\n", state, event)
  od
}
