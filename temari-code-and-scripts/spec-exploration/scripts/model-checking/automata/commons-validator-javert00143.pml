mtype = { InetAddressValidatorisValidInet4Address, InetAddressValidatorisValid, RegexValidatormatch, UrlValidatorcountToken, UrlValidatorisValidAuthority, UrlValidatorisValidFragment, UrlValidatorisValid, UrlValidatorisValidPath, UrlValidatorisValidQuery };
mtype = { s0, s1, s2, s3, s4, s5 };

mtype state = s0;
mtype event = UrlValidatorisValid;

active proctype P() {
  printf("The state is now %e and event is %e\n", state, event)
  do
  :: if
     :: state == s0 && event == UrlValidatorisValid -> /* s0 -> s1. outgoing edges from s1
                                                       s1 s2 org.apache.commons.validator.UrlValidator.isValidAuthority(Ljava/lang/String;)Z
                                                       */
                                                       state = s1; event -> UrlValidatorisValidAuthority
     :: state == s1 && event == UrlValidatorisValidAuthority -> /* s1 -> s2. outgoing edges from s2
                                                       s2 s3 org.apache.commons.validator.routines.InetAddressValidator.isValid(Ljava/lang/String;)Z
                                                       */
                                                       state = s2; event = InetAddressValidatorisValid
     :: state == s2 && event == InetAddressValidatorisValid -> /* s2 -> s3. outgoing edges from s3
                                                               s3 s3 org.apache.commons.validator.routines.RegexValidator.match(Ljava/lang/String;)[Ljava/lang/String;
                                                               s3 s3 org.apache.commons.validator.routines.InetAddressValidator.isValidInet4Address(Ljava/lang/String;)Z
                                                               s3 s4 org.apache.commons.validator.UrlValidator.isValidPath(Ljava/lang/String;)Z
                                                               */
                                                               if
                                                               :: state = s3; event = RegexValidatormatch
                                                               :: state = s3; event = InetAddressValidatorisValidInet4Address
                                                               :: state = s3; event = UrlValidatorisValidPath
                                                               fi
     :: state == s3 && event == RegexValidatormatch -> /* s3 -> s3. outgoing edges from s3
                                                               s3 s3 org.apache.commons.validator.routines.RegexValidator.match(Ljava/lang/String;)[Ljava/lang/String;
                                                               s3 s3 org.apache.commons.validator.routines.InetAddressValidator.isValidInet4Address(Ljava/lang/String;)Z
                                                               s3 s4 org.apache.commons.validator.UrlValidator.isValidPath(Ljava/lang/String;)Z
                                                               */
                                                               if
                                                               :: state = s3; event = RegexValidatormatch
                                                               :: state = s3; event = InetAddressValidatorisValidInet4Address
                                                               :: state = s3; event = UrlValidatorisValidPath
                                                               fi
     :: state == s3 && event == InetAddressValidatorisValidInet4Address -> /* s3 -> s3. outgoing edges from s3
                                                               s3 s3 org.apache.commons.validator.routines.RegexValidator.match(Ljava/lang/String;)[Ljava/lang/String;
                                                               s3 s3 org.apache.commons.validator.routines.InetAddressValidator.isValidInet4Address(Ljava/lang/String;)Z
                                                               s3 s4 org.apache.commons.validator.UrlValidator.isValidPath(Ljava/lang/String;)Z
                                                               */
                                                               if
                                                               :: state = s3; event = RegexValidatormatch
                                                               :: state = s3; event = InetAddressValidatorisValidInet4Address
                                                               :: state = s3; event = UrlValidatorisValidPath
                                                               fi
     :: state == s3 && event == UrlValidatorisValidPath -> /* s3 -> s4. outgoing edges from s4
                                                           s4 s4 org.apache.commons.validator.UrlValidator.countToken(Ljava/lang/String;Ljava/lang/String;)I
                                                           s4 s5 org.apache.commons.validator.UrlValidator.isValidQuery(Ljava/lang/String;)Z
                                                           */
                                                           if
                                                           :: state = s4; event = UrlValidatorcountToken
                                                           :: state = s4; event = UrlValidatorisValidQuery
                                                           fi
     :: state == s4 && event == UrlValidatorcountToken -> /* s4 -> s4. outgoing edges from s4
                                                           s4 s4 org.apache.commons.validator.UrlValidator.countToken(Ljava/lang/String;Ljava/lang/String;)I
                                                           s4 s5 org.apache.commons.validator.UrlValidator.isValidQuery(Ljava/lang/String;)Z
                                                           */
                                                           if
                                                           :: state = s4; event = UrlValidatorcountToken
                                                           :: state = s4; event = UrlValidatorisValidQuery
                                                           fi
     :: state == s4 && event == UrlValidatorisValidQuery -> /* s4 -> s5. outgoing edges from s5
                                                            s5 s0 org.apache.commons.validator.UrlValidator.isValidFragment(Ljava/lang/String;)Z
                                                            */
                                                            state = s5; event = UrlValidatorisValidFragment
     :: state == s5 && event == UrlValidatorisValidFragment -> /* s5 -> s0. outgoing edges from s0
                                                               s0 s1 org.apache.commons.validator.UrlValidator.isValid(Ljava/lang/String;)Z
                                                               */
                                                               state = s0 ; event = UrlValidatorisValid
     :: state == s0 -> break
     fi
     printf("The state is now %e and event is %e\n", state, event)
  od
}
