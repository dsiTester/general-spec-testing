mtype = { ValidatorResourcesgetParent, FormSetgetType, FormSetmerge, Formmerge, FormgetFields, FormSetgetForms };
mtype = { s0, s1, s2, s3 };

mtype state = s0;
mtype event = ValidatorResourcesgetParent;

active proctype P() {
  printf("The state is now %e and event is %e\n", state, event)
  do
  :: if
     :: state == s0 && event == ValidatorResourcesgetParent -> /* outgoing edges from s1:
                                                                  s1 s1 org.apache.commons.validator.FormSet.getType()I
                                                                  s1 s2 org.apache.commons.validator.FormSet.merge(Lorg/apache/commons/validator/FormSet;)V
                                                               */
                                                               if
                                                               :: state = s1; event = FormSetgetType
                                                               :: state = s1; event = FormSetmerge
                                                               fi
     :: state == s1 && event == FormSetgetType -> /* outgoing edges from s1:
                                                     s1 s1 org.apache.commons.validator.FormSet.getType()I
                                                     s1 s2 org.apache.commons.validator.FormSet.merge(Lorg/apache/commons/validator/FormSet;)V
                                                     */
                                                  if
                                                  :: state = s1; event = FormSetgetType
                                                  :: state = s1; event = FormSetmerge
                                                  fi
     :: state == s1 && event == FormSetmerge -> /* outgoing edges from s2:
                                                   s2 s2 org.apache.commons.validator.FormSet.getForms()Ljava/util/Map;
                                                   s2 s3 org.apache.commons.validator.Form.merge(Lorg/apache/commons/validator/Form;)V
                                                */
                                                if
                                                :: state = s2; event = Formmerge
                                                :: state = s2; event = FormSetgetForms
                                                fi
     :: state == s2 && event == FormSetgetForms ->  /* outgoing edges from s2:
                                                   s2 s2 org.apache.commons.validator.FormSet.getForms()Ljava/util/Map;
                                                   s2 s3 org.apache.commons.validator.Form.merge(Lorg/apache/commons/validator/Form;)V
                                                */
                                                if
                                                :: state = s2; event = FormSetgetForms
                                                :: state = s2; event = Formmerge
                                                fi
     :: state == s2 && event == Formmerge -> state = s3; event = FormgetFields
     :: state == s3 && event == FormgetFields -> state = s0
     :: state == s0 -> break /* FIXME: check; this might be wrong? */
     fi
     printf("The state is now %e and event is %e\n", state, event)
  od
}
