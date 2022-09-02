mtype = { a, b, c, d };
mtype = { start, one, two, three, end };

mtype state = start;
mtype event = a;

active proctype P() {
  printf("The state is now %e and event is %e\n", state, event)
  do
  :: if
     :: state == start && event == a -> state = one;
					           if
						      :: event = b
						      :: event = c
						   fi
     :: state == one && event == b -> state = two; event = d
     :: state == one && event == c -> state = three; event = d 
     :: state == two && event == d -> state = end
     :: state == three && event == d -> state = end     
     :: else -> state = end
     fi
     printf("The state is now %e and event is %e\n", state, event)
  od

}

/* Constraint 1: a is always followed by d */
/* ltl p1 { [] ((event == a) -> X <> (event == d)) } */

/* Constraint 2: b is never followed by a */
/*ltl p2 { [] ((event == b) -> X [] (event != a)) }*/

/* Constraint 3: d is always preceded by a */
/* ltl p3 { ((event != d) U (event == a)) || [](event != d) } */

/* !!Constraint 4: b is always immediately followed by d */
/* ltl p4 { [] ((event == b) -> X (event == d)) } */

/* Constraint 5: a is never immediately followed by d */
/* ltl p5 { [] ((event == a) -> X (event != d)) }*/

/* Constraint 6: b is always immediately preceded by a */
/* ltl p6 { <>(event == b) -> ((event != b) U ((event == a) && X(event == b))) } */

