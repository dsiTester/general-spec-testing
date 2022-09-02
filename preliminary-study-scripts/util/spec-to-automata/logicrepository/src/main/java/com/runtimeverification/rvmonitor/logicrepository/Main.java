/**
 * Main package for logic repository.
 *
 * author: Dongyun Jin
 */
package com.runtimeverification.rvmonitor.logicrepository;

import com.runtimeverification.rvmonitor.logicrepository.outputfsmparsing.parserast.FSMAlias;
import com.runtimeverification.rvmonitor.logicrepository.outputfsmparsing.parserast.FSMItem;
import com.runtimeverification.rvmonitor.logicrepository.outputfsmparsing.parserast.FSMTransition;
import com.runtimeverification.rvmonitor.logicrepository.outputfsmparsing.parservisitor.HasDefaultVisitor;
import com.runtimeverification.rvmonitor.logicrepository.parser.logicrepositorysyntax.LogicRepositoryType;

import com.runtimeverification.rvmonitor.logicrepository.parser.logicrepositorysyntax.PropertyType;
import com.runtimeverification.rvmonitor.logicrepository.plugins.LogicPluginFactory;
import com.runtimeverification.rvmonitor.logicrepository.outputfsmparsing.parser.FSMParser;
import com.runtimeverification.rvmonitor.logicrepository.outputfsmparsing.parserast.FSMInput;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Main class for a program that takes in through Std input an LTL/FSM spec, and outputs a dot file with the
 * corresponding monitor generated through JavaMOP monitor generation code.
 */
public class Main {

    /**
     * The path to the base repository this project resides in.
     */
    public static String basePath = null;

    /*
     * public static String readInput() { Scanner scanner = new
     * Scanner(System.in, "UTF-8"); String input = new String();
     *
     * while (scanner.hasNextLine()) { input += scanner.nextLine() + "\n"; }
     * return input; }
     */

    /**
     * Read Logic Plugin Directory.
     */
    public static String readLogicPluginDir(String basePath) {
        String logicPluginDirPath = System.getenv("LOGICPLUGINPATH");
        if (logicPluginDirPath == null || logicPluginDirPath.length() == 0) {
            if (basePath.charAt(basePath.length() - 1) == '/') {
                logicPluginDirPath = basePath + "plugins";
            } else {
                logicPluginDirPath = basePath + "/plugins";
            }
        }
        return logicPluginDirPath;
    }

    /**
     * Polishing directory path for windows.
     */
    public static String polishPath(String path) {
        if (path.indexOf("%20") > 0) {
            path = path.replaceAll("%20", " ");
        }
        return path;
    }

    /**
     * code from JavaFSM class (rv-monitor).
     */
    private static ArrayList<String> getEvents(String eventStr) {
        ArrayList<String> events = new ArrayList<String>();

        for (String event : eventStr.trim().split(" ")) {
            if (event.trim().length() != 0) {
                events.add(event.trim());
            }
        }

        return events;
    }

    private static String readFile(String filePath, String delimiter) throws IOException {
        List<String> eventsList = Files.readAllLines(Paths.get(filePath));
        String acc = "";
        // assuming that the file contains newline separated event names (+ space separated on same line works)
        for (String newEvent : eventsList) {
            if (!newEvent.startsWith("#")) {
                acc = acc + newEvent + delimiter;
            }
        }
        return acc;
    }

    public static LogicRepositoryType runLogicRepo(String logic, String eventNames, String formula) throws LogicException {

        // Read Logic Plugin Directory
        String logicPluginDirPath = polishPath(readLogicPluginDir(basePath));

        PropertyType propertyType = new PropertyType();
        propertyType.setLogic(logic);
        propertyType.setFormula(formula);

        LogicRepositoryType logicXML = new LogicRepositoryType();
        logicXML.setClient("RVMonitor");
        logicXML.setEvents(eventNames);
        logicXML.setProperty(propertyType);
        logicXML.setCategories("violation");
        LogicRepositoryData logicRepositoryData = new LogicRepositoryData(logicXML);

        // Get Logic Name and Client Name
        String logicName = null;
        if (logicXML.getProperty() != null) {
            logicName = logicXML.getProperty().getLogic().toLowerCase();
        }
        if (logicName == null || logicName.length() == 0) {
            Log.setStatus(Log.ERROR);
            Log.setErrorMsg("No logic names");
            Log.flush();
            throw new LogicException("no logic names");
        }
        String clientName = logicXML.getClient();
        if (clientName == null || clientName.length() == 0) {
            clientName = "Anonymous Client";
        }

        // Find a logic plugin and apply
        ByteArrayOutputStream logicPluginResultStream = LogicPluginFactory.process(logicPluginDirPath, logicName,
                logicRepositoryData);

        // Error check
        if (logicPluginResultStream == null || logicPluginResultStream.size() == 0) {
            Log.setStatus(Log.ERROR);
            Log.setErrorMsg("Unknown Error from Logic Plugins");
            Log.flush();
            throw new LogicException("Unknown Error from Logic Plugins");
        }

        LogicRepositoryData logicOutputData = new LogicRepositoryData(logicPluginResultStream);
        LogicRepositoryType logicOutputXML = logicOutputData.getXML();

        // Outputting
        String logicRepositoryOutput = logicOutputData.getOutputStream().toString();
        Log.write("Logic Repository Output", logicRepositoryOutput);

        return logicOutputXML;
    }

    /**
     * Handles all of the logic from getting the formula to outputting to a dot file.
     */
    public static void main(String[] args) {

        if (args.length != 4) {
            System.out.println("USAGE: LOGICREPOSITORY_PATH EVENTS_FILE FORMULA_FILE LANGUAGE");
            return;
        }

        int i = 0;

        while (i < args.length) {
            if (args[i].compareTo("-install") == 0) {
                i++;
                Statistics.install = true;
            }
            ++i;
        }

        try {

            long startTime = System.currentTimeMillis();

            // Initialize Logging
            Date date = Log.init(polishPath(basePath + "/log"));
            Log.setStatus(Log.SUCCESS);

            byte[] formulaBytes;
            basePath = args[0];
            String allEvents = readFile(args[1], " ");
            LogicRepositoryType logicOutputXML;
            String formulaString = readFile(args[2], "\n");

            String logicOption = args[3];
            if (logicOption.equals("fsm")) { // Not running Logic Plugin
                formulaBytes = formulaString.getBytes();
            } else if (logicOption.equals("ere")) { // converting an ERE
                logicOutputXML = runLogicRepo("ere", allEvents, formulaString);
                allEvents = logicOutputXML.getEvents();
                formulaBytes = logicOutputXML.getProperty().getFormula().getBytes();
            } else { // we run the logic repo and get a LogicRepositoryType object
                logicOutputXML = runLogicRepo("ltl", allEvents, formulaString); // args[1] should be formatted by the script
                allEvents = logicOutputXML.getEvents(); // this might be unnecessary
                formulaBytes = logicOutputXML.getProperty().getFormula().getBytes();
            }

            // Read xml through parsing to get some representation of FSM
            FSMInput fsmInput = null;
            try {
                fsmInput = FSMParser.parse(new ByteArrayInputStream(formulaBytes));
            } catch (Exception e) {
                throw new LogicException("could not parse output FSM formula");
            }

            if (fsmInput == null) {
                throw new LogicException("could not parse output FSM formula");
            }

            // I think this determines whether the formula actually has a default transition?
            HasDefaultVisitor hasDefaultVisitor = new HasDefaultVisitor();
            fsmInput.accept(hasDefaultVisitor, null);

            // this is technically the alphabet
            ArrayList<String> events = getEvents(allEvents);

            // create states
            Map<State, String> stateToName = new HashMap<State, String>();
            Map<String, State> nameToState = new HashMap<String, State>();
            Map<FSMItem, State> fsmItemStateMap = new HashMap<FSMItem, State>();
            int countState = 0;

            Automaton a = new Automaton();
            if (fsmInput.getItems() != null) {
                for (FSMItem item : fsmInput.getItems()) {
                    String stateName = item.getState();
                    State newState = new State();
                    stateToName.put(newState, stateName);
                    nameToState.put(stateName, newState);
                    fsmItemStateMap.put(item, newState);
                    if (countState == 0) {
                        a.setInitialState(newState);
                    }
                    newState.setAccept(computeAcceptStatus(stateName, logicOption, fsmInput));
                    countState++;
                }
            }

            // need to make another state for sink transition - by design I think violation goes to sink regardless
            // of event.
            State sink = new State();
            if (!logicOption.contains("ere")) {
                // FIXME: debatable whether sink is actually an accept state, but since going over it doesn't get a
                //  violation I'm going to say that it's an accept state for now
                sink.setAccept(true);
            }

            // Transitions have a char identifier. Therefore we need to create a mapping btwn chars and event names
            int alphabetCounter = 0;
            String alphabet = "abcdefghijklmnopqrstuvwxyz"; // this gives us 26 chars, hopefully that's enough

            Map<String, Character> eventToTransCharMap = new HashMap<String, Character>();
            Map<Character, String> transCharToEventMap = new HashMap<Character, String>();
            Set<TransitionTriple> transitionTripleSet = new HashSet<>();

            // assign event names to chars for transition
            for (String event : events) {
                char c = alphabet.charAt(alphabetCounter);
                eventToTransCharMap.put(event, c);
                transCharToEventMap.put(c, event);
                alphabetCounter++;
            }

            // iterate over each state and process its transitions
            for (FSMItem item : fsmInput.getItems()) {
                State currState = fsmItemStateMap.get(item);

                List<FSMTransition> transitionList = item.getTransitions();
                for (FSMTransition t : transitionList) {
                    if (t.isDefaultFlag()) {
                        if (nameToState.get(t.getStateName()) == null) {
                            throw new LogicException("Incorrect Monitor");
                        }
                        continue;
                    }
                    String tEvent = t.getEventName();
                    Transition transition = new Transition(eventToTransCharMap.get(tEvent), nameToState.get(t.getStateName()));
                    currState.addTransition(transition);
                    transitionTripleSet.add(new TransitionTriple(item.getState(), t.getStateName(), tEvent));
                }

                // violation states need to go to the sink for every event
                if (item.getState().equals("violation") && item.getTransitions().size() == 0) {
                    for (String event : events) {
                        Transition transition = new Transition(eventToTransCharMap.get(event), sink);
                        currState.addTransition(transition);
                    }
                }
            }

            // Sink needs to loop back.
            for (String event : events) {
                Transition transition = new Transition(eventToTransCharMap.get(event), sink);
                sink.addTransition(transition);
            }

            // need to print out transition key, since a dot file can't include this info.
            System.out.println();
            System.out.println("===== TRANSITION CHAR KEY =====");
            String transitionCharKey = "";
            for (char transChar : transCharToEventMap.keySet()) {
                transitionCharKey += transChar + ": " + transCharToEventMap.get(transChar) + "\n";
            }
            System.out.println(transitionCharKey);
            System.out.println();

            Log.write("===== TRANSITION CHAR KEY =====", transitionCharKey);

            System.out.println("Writing to dotFile...");
            String dotFileName;
            String transitionKeyFileName;
            String txtRepresentationFileName;

            if (date != null) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                dotFileName = "dotFile-" + dateFormat.format(date) + ".dot";
                transitionKeyFileName = "transitionKey-" + dateFormat.format(date) + ".txt";
                txtRepresentationFileName = "txtRepresentation-" + dateFormat.format(date) + ".txt";
            } else {
                dotFileName = "dotFile.dot";
                transitionKeyFileName = "transitionKey.txt";
                txtRepresentationFileName = "txtRepresentation.txt";
            }

//            String dotString = a.toDot();
            String dotString = generateDotRepresentation(a, stateToName, transitionTripleSet);
            FileWriter fileWriter = new FileWriter(dotFileName);
            fileWriter.write(dotString);
            fileWriter.close();
            System.out.println("Output written in " + dotFileName);

            FileWriter fileWriter0 = new FileWriter(transitionKeyFileName);
            fileWriter0.write(transitionCharKey);
            fileWriter0.close();

            Log.write("AUTOMATON: ", a.toString());
            System.out.println("done!");

            FileWriter txtRepresentationWriter = new FileWriter(txtRepresentationFileName);
            txtRepresentationWriter.write(generateTxtRepresentation(a, stateToName, transitionTripleSet));
            txtRepresentationWriter.close();

            long endTime = System.currentTimeMillis();
            long runTime = endTime - startTime;

            // Logging
            Log.setExecTime(runTime);
            Log.flush();
        } catch (LogicException e) {
            System.out.println(e);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static boolean computeAcceptStatus(String stateName, String logicOption, FSMInput input) {
        if (logicOption.equals("ere")) {
            for (FSMAlias alias : input.getAliases()) {
                if (alias.getGroupName().equals("match")) { // matches the regular expression; i.e. is accept
                    return alias.getStates().contains(stateName); // is our current state in "match"?
                }
            }
            return false;
        } else {
            return !(stateName.equals("violation")); // non-violations are going to be accept states
        }
    }

    public static String generateTxtRepresentation(Automaton automaton, Map<State, String> state2String, Set<TransitionTriple> transitionTripleSet) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("1\n"); // these FSMs will only have one initial state
        stringBuilder.append(state2String.get(automaton.getInitialState()) + "\n");
        stringBuilder.append(automaton.getAcceptStates().size() + "\n");
        for (State acceptState : automaton.getAcceptStates()) {
            stringBuilder.append(state2String.get(acceptState) + "\n");
        }
        // write the number of transitions
        stringBuilder.append(transitionTripleSet.size() + "\n");
        for (TransitionTriple transition : transitionTripleSet) {
            stringBuilder.append(transition.toString() + "\n");
        }

        return stringBuilder.toString();
    }

    // NOTE: most of this code is borrowed and adapted from Automaton's toDot method.
    // We just need to add labels to the states...
    public static String generateDotRepresentation(Automaton automaton,
                                                   Map<State, String> state2String,
                                                   Set<TransitionTriple> transitionTripleSet) {
        Map<String, Integer> stateLabelToId = new HashMap<>();
        Map<Integer, String> idToStateLabel = new HashMap<>();
        StringBuilder b = new StringBuilder("digraph Automaton {\n");
        b.append("  rankdir = LR;\n");
        Set<State> states = automaton.getStates();
        int acc = 0;
        for (State s : states) {
            String stateLabel = state2String.get(s);
            stateLabelToId.put(stateLabel, acc);
            idToStateLabel.put(acc, stateLabel);
            b.append("  ").append(acc);
            if (s.isAccept()) {
                b.append(" [shape=doublecircle,label=\"" + stateLabel + "\"];\n");
            }
            else {
                b.append(" [shape=circle,label=\"" + stateLabel + "\"];\n");
            }
            if (s.equals(automaton.getInitialState())) {
                b.append("  initial [shape=plaintext,label=\"\"];\n");
                b.append("  initial -> ").append(acc).append("\n");
            }
            acc++;
        }
        for (TransitionTriple transitionTriple : transitionTripleSet) {
            String srcId = stateLabelToId.get(transitionTriple.getSrc()).toString();
            String destId = stateLabelToId.get(transitionTriple.getDest()).toString();
            b.append("  " + srcId + " -> " + destId + " [label=\"" + transitionTriple.getEvent() + "\"];\n");
        }
        return b.append("}\n").toString();


    }


}
