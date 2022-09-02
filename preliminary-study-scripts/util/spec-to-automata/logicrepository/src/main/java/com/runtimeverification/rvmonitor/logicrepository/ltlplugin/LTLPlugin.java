package com.runtimeverification.rvmonitor.logicrepository.ltlplugin;

import com.runtimeverification.rvmonitor.logicrepository.Log;
import com.runtimeverification.rvmonitor.logicrepository.LogicException;
import com.runtimeverification.rvmonitor.logicrepository.LogicRepositoryData;
import com.runtimeverification.rvmonitor.logicrepository.parser.logicrepositorysyntax.LogicRepositoryType;
import com.runtimeverification.rvmonitor.logicrepository.plugins.LogicPlugin;
import com.runtimeverification.rvmonitor.logicrepository.ltlplugin.parser.LTLParser;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;

public class LTLPlugin extends LogicPlugin {
    
    public LogicRepositoryType process(LogicRepositoryType logicInputXML) throws LogicException {
        String logicStr = logicInputXML.getProperty().getFormula();
        String eventsStr = logicInputXML.getEvents();
        eventsStr.replaceAll("\\s+", " ");
        String[] eventStrings = eventsStr.split(" ");
        
        HashSet<Atom> events = new HashSet();
        for (int i = 0; i < eventStrings.length; ++i) {
            events.add(Atom.get(eventStrings[i]));
        }
        
        LTLParser ltlParser = LTLParser.parse(logicStr);
        LTLFormula ltl = ltlParser.getFormula();
        
        ltl = ltl.simplify();
        
        for(Atom a : ltl.atoms()){
            if(!events.contains(a)){
                throw new LogicException("event " + a + " is used but never defined");
            }
        }

        Log.write("going to go through transformations", "");
        Log.write("LTL", ltl.toString());
        AAutomaton aa = new AAutomaton(ltl);
        Log.write("AAutomaton ", aa.toString());
        GBA gba = new GBA(aa);
        Log.write("GBA", gba.toString());
        BA ba = new BA(gba);
        Log.write("BA", ba.toString());
        NFA nfa = new NFA(ba);
        Log.write("NFA", nfa.toString());
        DFA dfa = new DFA(nfa);
        Log.write("DFA", dfa.toString());

        String logic = "fsm";
        String formula = dfa.toString();
        
        LogicRepositoryType logicOutputXML = logicInputXML;
        logicOutputXML.getProperty().setLogic(logic);
        logicOutputXML.getProperty().setFormula(formula);

        ByteArrayOutputStream logicOutput = new LogicRepositoryData(logicOutputXML).getOutputStream();
        Log.write("LTL logicOutput", logicOutput.toString());

        return logicOutputXML;
    }
    
    static protected LTLPlugin plugin = new LTLPlugin();
    
    public static void main(String[] args) {
        
        try {
            // Parse Input
            LogicRepositoryData logicInputData = new LogicRepositoryData(System.in);

            Log.write("LTL Plugin Main System.in", logicInputData.getOutputStream().toString());

            // use plugin main function
            if(plugin == null){
                throw new LogicException("Each plugin should initiate plugin field.");
            }
            LogicRepositoryType logicOutputXML = plugin.process(logicInputData.getXML());
            
            if (logicOutputXML == null) {
                throw new LogicException("no output from the plugin.");
            }
            System.setOut(System.out);
            ByteArrayOutputStream logicOutput = new LogicRepositoryData(logicOutputXML).getOutputStream();
            System.out.println(logicOutput);
        } catch (LogicException e) {
            System.out.println(e);
        }
        
    }
}
