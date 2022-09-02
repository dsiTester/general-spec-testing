mtype = { getMinLength, getMaxLength, isValid, getRegexValidator, getCheckDigit, validate, EPSILON };
mtype = { start, C0, C4, C5, C6, C8 };

mtype state = start;
mtype event = EPSILON;

active proctype P() {
  printf("The state is now %e and event is %e\n", state, event)
  do
  :: if
     :: state == start && event == EPSILON ->
                                           if
                                           /* outgoing edges from C4 
                                              C4	C0	getMinLength()I
                                              C4	C0	getMaxLength()I
                                              C4	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                              C4	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                              C4	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                           */
                                           :: state = C4 ; event = getMinLength
                                           :: state = C4 ; event = getMaxLength
                                           :: state = C4 ; event = getRegexValidator
                                           :: state = C4 ; event = getCheckDigit
                                           :: state = C4 ; event = validate
                                           /* outgoing edges from C6
                                              C6	C5	isValid(Ljava/lang/String;)Z
                                              C6	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                              C6	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                              C6	C0	getMaxLength()I
                                              C6	C0	getMinLength()I
                                              C6	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                           */
                                           :: state = C6 ; event = isValid
                                           :: state = C6 ; event = validate
                                           :: state = C6 ; event = getRegexValidator
                                           :: state = C6 ; event = getMaxLength
                                           :: state = C6 ; event = getMinLength
                                           :: state = C6 ; event = getCheckDigit
                                           fi
     :: state == C4 ; event == getMinLength -> /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C4 ; event == getMaxLength -> /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C4 ; event == getRegexValidator -> /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C4 ; event == getCheckDigit -> /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C4 ; event == validate -> /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C6 ; event == isValid -> /* outgoing edges from C5:
                                             C5	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                          */
                                          state = C5 ; event = validate
     :: state == C6 ; event == validate ->  /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C6 ; event == getRegexValidator ->  /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C6 ; event == getMaxLength ->  /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C6 ; event == getMinLength ->  /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C6 ; event == getCheckDigit ->  /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C0 ; event == EPSILON -> state = C8; event = EPSILON
     :: state == C0 ; event == getMinLength ->  /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C0 ; event == getRegexValidator ->  /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C0 ; event == isValid ->  /* outgoing edges from C5:
                                              C5	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                           */
                                           state = C5 ; event = validate
     :: state == C0 ; event == getCheckDigit ->  /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C0 ; event == validate ->  /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C0 ; event == getMaxLength ->  /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C5 && event == validate -> /* outgoing edges from C0:
                                               C0	C8	<END>
                                               C0	C0	getMinLength()I
                                               C0	C0	getRegexValidator()Lorg/apache/commons/validator/routines/RegexValidator;
                                               C0	C5	isValid(Ljava/lang/String;)Z
                                               C0	C0	getCheckDigit()Lorg/apache/commons/validator/routines/checkdigit/CheckDigit;
                                               C0	C0	validate(Ljava/lang/String;)Ljava/lang/Object;
                                               C0	C0	getMaxLength()I
                                               */
                                               if
                                               :: state = C0 ; event = EPSILON
                                               :: state = C0 ; event = getMinLength
                                               :: state = C0 ; event = getRegexValidator
                                               :: state = C0 ; event = isValid
                                               :: state = C0 ; event = getCheckDigit
                                               :: state = C0 ; event = validate
                                               :: state = C0 ; event = getMaxLength
                                               fi
     :: state == C8 -> break
     fi
     printf("The state is now %e and event is %e\n", state, event)
  od
}
