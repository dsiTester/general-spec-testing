mtype = { STN, HMTT, HMTF, NT, EPSILON };
mtype = { start, s0, s1, s2, s3, s5, s8, s15, s17, end }; /* we may not need start or end */

mtype state = start;
mtype event = EPSILON;

active proctype P() {
  printf("The state is now %e and event is %e\n", state, event)
  do
  :: if
     :: state == start && event == EPSILON -> state = s0; event = STN
     :: state == s0 && event == STN -> /* edges out of S0 */
                                   if
                                   /* S0 -> S1 [ label = STN, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s1 have transitions NT and HMTT */
                                   :: state = s1; event = NT
                                   :: state = s1; event = HMTT
                                   /* S0 -> S2 [ label = STN, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s2 have transitions HMTF */
                                   :: state = s2; event = HMTF
                                   fi
     :: state == s1 && event == NT -> /* transitions from S1 with NT */
                                   if
                                   /* S1 -> S1 [ label = NT, fontcolor="#ff0000"]; */
                                   /* self-loop: outgoing edges from s1 have transitions NT and HMTT */
                                   :: state = s1; event = NT
                                   :: state = s1; event = HMTT
                                   /* S1 -> S2 [ label = NT, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s2 have transitions HMTF */
                                   :: state = s2; event = HMTF
                                   fi
     :: state == s1 && event == HMTT -> /* transitions from S1 with HMTT */
                                   if
                                   /* S1 -> S3 [ label = HMTT, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s3 have transition NT */
                                   :: state = s3; event = NT
                                   /* S1 -> S8 [ label = HMTT, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s8 have transition NT */
                                   :: state = s8; event = NT
                                   /* S1 -> S15 [ label = HMTT, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s15 have transition NT */
                                   :: state = s15; event = NT
                                   fi
     :: state == s2 && event == HMTF -> /* transitions from S2 */
                                   if
                                   /* S2 -> S5 [ label = HMTF, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s5 have transition HMTF */
                                   :: state = s5; event = HMTF
                                   /* S2 -> S17 [ label = HMTF, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s17 have transition EPSILON */
                                   :: state = s17; event = EPSILON
                                   fi
     :: state == s3 && event == NT -> /* transitions from S3 */
                                   if
                                   /* S3 -> S1 [ label = NT, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s1 have transitions NT and HMTT */
                                   :: state = s1; event = NT
                                   :: state = s1; event = HMTT
                                   /* S3 -> S2 [ label = NT, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s2 have transitions HMTF */
                                   :: state = s2; event = HMTF
                                   fi
     :: state == s5 && event == HMTF -> /* transitions from S5 */
                                   if
                                   /* S5 -> S5 [ label = HMTF, fontcolor="#ff0000"]; */
                                   /* self-loop: outgoing edges from s5 have transition HMTF */
                                   :: state = s5; event = HMTF
                                   fi
     :: state == s8 && event == NT -> /* transitions from S8 */
                                   if
                                   /* S8 -> S1 [ label = NT, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s1 have transitions NT and HMTT */
                                   :: state = s1; event = NT
                                   :: state = s1; event = HMTT
                                   /* S8 -> S2 [ label = NT, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s2 have transitions HMTF */
                                   :: state = s2; event = HMTF
                                   fi
     :: state == s15 && event == NT -> /* transitions from S15 */
                                   if
                                   /* S15 -> S1 [ label = NT, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s1 have transitions NT and HMTT */
                                   :: state = s1; event = NT
                                   :: state = s1; event = HMTT
                                   /* S15 -> S2 [ label = NT, fontcolor="#ff0000"]; */
                                   /* outgoing edges from s2 have transitions HMTF */
                                   :: state = s2; event = HMTF
                                   fi
     :: state == s17 && event == EPSILON -> state = end /* S17 -> end [ label = EPSILON, fontcolor="#ff0000"]; */
     :: state == end -> skip
     fi
     printf("The state is now %e and event is %e\n", state, event)
  od
}
