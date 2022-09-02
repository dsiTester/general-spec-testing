mtype = { InetAddressValidatorisValidInet4Address, InetAddressValidatorisValid, RegexValidatormatch, UrlValidatorcountToken, UrlValidatorisValidAuthority, UrlValidatorisValidFragment, UrlValidatorisValid, UrlValidatorisValidPath, UrlValidatorisValidQuery, UrlValidatorisValidScheme };
mtype = { s0, s1, s2, s3, s4, s5, s6 };

mtype state = s0;
mtype event = UrlValidatorisValid;

active proctype P() {
  printf("The state is now %e and event is %e\n", state, event)
  do
  :: if
     :: state == s0 && event == UrlValidatorisValid -> state = s1; event = UrlValidatorisValidScheme
     :: state == s1 && event == UrlValidatorisValidScheme -> state = s2; event = UrlValidatorisValidAuthority
     :: state == s2 && event == UrlValidatorisValidAuthority -> state = s3; event = InetAddressValidatorisValid
     :: state == s3 && event == InetAddressValidatorisValid -> /* outgoing edges from s4:
                                                                  s4 s4 org.apache.commons.validator.routines.InetAddressValidator.isValidInet4Address(Ljava/lang/String;)Z
                                                                  s4 s5 org.apache.commons.validator.UrlValidator.isValidPath(Ljava/lang/String;)Z
                                                               */
                                                               if
                                                               :: state = s4; event = InetAddressValidatorisValidInet4Address
                                                               :: state = s4; event = UrlValidatorisValidPath
                                                               fi
     :: state == s4 && event == InetAddressValidatorisValidInet4Address -> /* outgoing edges from s4:
                                                                           s4 s4 org.apache.commons.validator.routines.InetAddressValidator.isValidInet4Address(Ljava/lang/String;)Z
                                                                           s4 s5 org.apache.commons.validator.UrlValidator.isValidPath(Ljava/lang/String;)Z
                                                                           */
                                                                           if
                                                                           :: state = s4; event = InetAddressValidatorisValidInet4Address
                                                                           :: state = s4; event = UrlValidatorisValidPath
                                                                           fi
     :: state == s4 && event == UrlValidatorisValidPath -> /* outgoing edges from s5
                                                           s5 s5 org.apache.commons.validator.UrlValidator.countToken(Ljava/lang/String;Ljava/lang/String;)I
                                                           s5 s6 org.apache.commons.validator.UrlValidator.isValidQuery(Ljava/lang/String;)Z
                                                           */
                                                           if
                                                           :: state = s5; event = UrlValidatorcountToken
                                                           :: state = s6; event = UrlValidatorisValidQuery
                                                           fi
     :: state == s5 && event == UrlValidatorcountToken -> /* outgoing edges from s5
                                                           s5 s5 org.apache.commons.validator.UrlValidator.countToken(Ljava/lang/String;Ljava/lang/String;)I
                                                           s5 s6 org.apache.commons.validator.UrlValidator.isValidQuery(Ljava/lang/String;)Z
                                                           */
                                                           if
                                                           :: state = s5; event = UrlValidatorcountToken
                                                           :: state = s6; event = UrlValidatorisValidQuery
                                                           fi
     :: state == s5 && event == UrlValidatorisValidQuery -> state = s6 ; event = UrlValidatorisValidFragment
     :: state == s6 && event == UrlValidatorisValidFragment -> state = s0; event UrlValidatorisValid
     :: state == s0 -> break
     fi
     printf("The state is now %e and event is %e\n", state, event)
  od
}
