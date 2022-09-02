mtype = { STN, HMTT, HMTF, NT };
mtype = { s0, s1, s11, s12, s2, s3 };

mtype state = s0;
mtype event = STN;

active proctype P() {
  printf("The state is now %e and event is %e\n", state, event)
  do
  :: if
     :: state == s0 && event == STN ->
			           if
	                           :: state = s1; event = HMTT 
				   :: state = s11; event = NT
				   :: state = s12; event = HMTF                                   
				   fi
     :: state == s1 && event == HMTT ->
                                   if
                                   :: state = s2; event = NT
                                   :: state = s11; event = NT                                   
                                   fi
     :: state == s11 && event == NT -> state = s12; event = HMTF
     :: state == s12 && event == HMTF -> state = s3
     :: state == s2 && event == NT -> state = s3     
     :: state == s3 -> skip
     fi
     printf("The state is now %e and event is %e\n", state, event)
  od
}
ltl STN_NT_6 { <>(event == STN) -> ((event != STN) U ((event == NT) && X(event == STN))) }
