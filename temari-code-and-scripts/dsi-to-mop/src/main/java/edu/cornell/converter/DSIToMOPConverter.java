package edu.cornell.converter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;

import edu.cornell.components.Event;
import edu.cornell.parser.DSISpecParser;
import edu.cornell.parser.SpecParts;

public class DSIToMOPConverter {
    public static String preamble = "package mop;\n" +
            "\n" +
            "import com.runtimeverification.rvmonitor.java.rt.RVMLogging;\n" +
            "import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;\n\n";

    public static final Set<String> primitiveTypes = new HashSet<>(
            Arrays.asList("boolean", "character", "byte", "short", "integer", "long", "float", "double", "void",
                    "boolean[]", "character[]", "byte[]", "short[]", "integer[]", "long[]", "float[]", "double[]"));

    public static final String fsmTemplate = "\tfsm :\n\n" +
            "    initial [\n" +
            "       aEvent -> midstate\n" +
            "       bEvent -> unsafe\n" +
            "    ]\n" +
            "\n" +
            "    midstate [\n" +
            "       aEvent -> unsafe\n" +
            "       bEvent -> bstate\n" +
            "    ]\n" +
            "\n" +
            "    bstate [\n" +
            "       aEvent -> unsafe\n" +
            "       bEvent -> unsafe\n" +
            "    ]\n" +
            "\n" +
            "    unsafe [\n" +
            "       aEvent -> unsafe\n" +
            "       bEvent -> unsafe\n" +
            "    ]\n";

    public static final String handlerTemplate = "\t@unsafe {\n" +
            "        RVMLogging.out.println(Level.CRITICAL, __DEFAULT_MESSAGE);\n" +
            "        RVMLogging.out.println(Level.CRITICAL, \"Specification SPECCNAME went into an error state.\");\n" +
            "\t}\n";

    public static void main(String[] args) {
        String projectName = args[0]; //"FileUpload";
        String specFileName = args[1];
        String outDirName = args[2];
        File specFile = new File(specFileName);
        File outDir = new File(outDirName);
        try (BufferedReader reader = new BufferedReader(new FileReader(specFile))) {
            if (!outDir.exists()) {
                if (!outDir.mkdirs()) {
                    throw new IOException("Error creating: " + outDirName);
                }
            }
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                String specID = parts[0];
                String methodA = parts[1].contains("=") ? parts[1].split("=")[1] : parts[1];
                String methodB = parts[2].contains("=") ? parts[2].split("=")[1] : parts[2];
                String spec = convertToMOPSpec(projectName, specID, methodA, methodB);
                try (FileWriter writer = new FileWriter(new File(outDir, projectName + specID + ".mop"))) {
                    writer.write(spec);
                }
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static String convertToMOPSpec(String projectName, String specID, String methodA, String methodB) {
        DSISpecParser parser = new DSISpecParser();
        SpecParts methodAParts = parser.parse(methodA);
        SpecParts methodBParts = parser.parse(methodB);
        List<String> mopSpecParams = computeSpecParams(methodAParts, methodBParts);
        Map<String, String> specParamNameMap = mapNamesToParams(mopSpecParams);
        List<Event> events = createEvents(methodAParts, methodBParts, specParamNameMap);
        String spec = buildSpecFromParts(projectName + specID, specParamNameMap, events, methodAParts, methodBParts);
        return spec;
    }

    private static List<Event> createEvents(SpecParts methodAParts, SpecParts methodBParts,
                                            Map<String, String> specParamNameMap) {
        List<Event> events = new ArrayList<>();
        events.add(new Event(methodAParts, specParamNameMap));
        events.add(new Event(methodBParts, specParamNameMap));
        return events;
    }

    private static String buildSpecFromParts(String specName, Map<String, String> specParamNameMap, List<Event> events,
                                             SpecParts methodAParts, SpecParts methodBParts) {
        StringBuilder spec = new StringBuilder(preamble);
        spec.append(buildSpecHeader(specName, specParamNameMap));
        spec.append(" {\n\n");
        for (Event event : events) {
            spec.append(event);
            spec.append("\n\n");
        }
        spec.append(fsmTemplate.replaceAll("aEvent",
                methodAParts.getName()).replaceAll("bEvent", methodBParts.getName()));
        spec.append("\n\n");
        spec.append(handlerTemplate.replaceAll("SPECCNAME", specName));
        spec.append("}");
        return spec.toString();
    }

    private static String buildSpecHeader(String specName, Map<String, String> specParamNameMap) {
        StringBuilder specHeader = new StringBuilder(specName);
        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        for (Map.Entry entry : specParamNameMap.entrySet()) {
            joiner.add(entry.getValue() + " " + entry.getKey());
        }
        specHeader.append(joiner.toString());
        return specHeader.toString();
    }

    private static Map<String, String> mapNamesToParams(List<String> mopSpecParams) {
        Map<String, String> map = new HashMap<>();
        int count = 1;
        for (String paramType : mopSpecParams) {
            map.put(getShortName(paramType, count++), paramType);
        }
        return map;
    }

    private static String getShortName(String paramType, int suffix) {
        return paramType.substring(paramType.lastIndexOf('.') + 1).toLowerCase().replace("[]", "") + suffix;
    }

    private static List<String> computeSpecParams(SpecParts methodAParts, SpecParts methodBParts) {
        //receivers and return types become parameters
        // TODO: do we need to add non-primitive arguments as params?
        List<String> paramTypes = new ArrayList<>();
        // TODO: should we consider arrays of primitives as primitives?
        paramTypes.addAll(Arrays.asList(methodAParts.getReturnType(), methodBParts.getReturnType()));
        paramTypes.addAll(new HashSet<>(Arrays.asList(methodAParts.getReceiver(), methodBParts.getReceiver())));
        paramTypes.removeAll(primitiveTypes);
        return paramTypes;
    }
}
