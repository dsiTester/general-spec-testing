mtype = { ValidatorActioninit, formatJavascriptFileName, javascriptAlreadyLoaded, loadJavascriptFunction, generateJsFunction, readJavascriptFile, getName };
mtype = { s0, s1, s2, s3, s4, s5, s6 };

mtype state = s0;
mtype event = ValidatorActioninit;

active proctype P() {
  printf("The state is now %e and event is %e\n", state, event)
  do
  :: if
     :: state == s0 && event == ValidatorActioninit -> state = s1; event = loadJavascriptFunction
     :: state == s1 && event == loadJavascriptFunction -> state = s2; event = javascriptAlreadyLoaded
     :: state == s2 && event == javascriptAlreadyLoaded -> state = s3; event = generateJsFunction
     :: state == s3 && event == generateJsFunction -> state = s4; event = formatJavascriptFileName
     :: state == s4 && event == formatJavascriptFileName -> state = s5; event = readJavascriptFile
     :: state == s5 && event == readJavascriptFile -> state = s6; event = getName
     :: state == s6 && event == getName -> state = s0; event = ValidatorActioninit
     /* :: state ==  -> skip */
     fi
     printf("The state is now %e and event is %e\n", state, event)
  od
}
