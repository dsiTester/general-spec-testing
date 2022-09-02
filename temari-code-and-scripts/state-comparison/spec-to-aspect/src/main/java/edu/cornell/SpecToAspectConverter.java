package edu.cornell;

import edu.cornell.components.Event;
import edu.cornell.parser.DSISpecParser;
import edu.cornell.parser.SpecParts;

import java.io.BufferedReader;
import java.io.File;
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

import static edu.cornell.converter.DSIToMOPConverter.primitiveTypes;

public class SpecToAspectConverter {

    // package name should come before this
    public static String preamble = "import com.sun.jdi.VirtualMachine;\n" +
            "import edu.cornell.*;";

    public static String beforeMethodA = "        if (methodADiff.beforeCall == null) {\n" +
            "        System.out.println(\"before calling method-a\");\n" +
            "                try {\n" +
            "                    methodADiff.collectStateBeforeMethod(StateComparisonManager.getVm());\n" +
            "                } catch (Exception e) {\n" +
            "                    System.out.println(\"[ERROR] Unable to obtain the VM\");\n" +
            "                    e.printStackTrace();\n" +
            "                }\n" +
            "           }\n" +
            "        }\n";

    public static String afterMethodA = "        if (methodADiff.afterCall == null) {\n" +
            "                System.out.println(\"after calling method-a\");\n" +
            "                try {\n" +
            "                    methodADiff.collectStateAfterMethod(StateComparisonManager.getVm());\n" +
            "                    methodADiff.compareState();\n" +
            "                } catch (Exception e) {\n" +
            "                    System.out.println(\"[ERROR] Unable to obtain the VM\");\n" +
            "                    e.printStackTrace();\n" +
            "                }\n" +
            "           }\n" +
            "        }\n";

    public static String beforeMethodB = "        System.out.println(\"before calling method-b\");\n" +
            "        try {\n" +
            "            methodBDiff.collectStateBeforeMethod(StateComparisonManager.getVm());\n" +
            "        } catch (Exception e) {\n" +
            "            System.out.println(\"[ERROR] Unable to obtain the VM\");\n" +
            "            e.printStackTrace();\n" +
            "\n" +
            "        }\n" +
            "    }\n";

    public static String afterMethodB = "            System.out.println(\"after calling method-b\");\n" +
            "            try {\n" +
            "                methodBDiff.collectStateAfterMethod(StateComparisonManager.getVm());\n" +
            "                methodBDiff.compareState();\n" +
            "                if (methodADiff.findOverlaps(methodBDiff)) {\n" +
            "                    System.out.println(\"method-a and method-b share state!\");\n" +
            "                } else {\n" +
            "                    System.out.println(\"method-a and method-b do NOT share state!\");\n" +
            "                }\n" +
            "                System.exit(0);\n" +
            "            } catch (Exception e) {\n" +
            "                System.out.println(\"[ERROR] Unable to obtain the VM\");\n" +
            "            e.printStackTrace();\n" +
            "            }\n" +
            "    }\n";

    public static void main(String[] args) {
//        String projectName = "FileUpload";
//        String line = "00153 a=org.apache.commons.fileupload2.FileUploadBase.getItemIterator(Lorg/apache/commons/fileupload2/RequestContext;)Lorg/apache/commons/fileupload2/FileItemIterator; b=org.apache.commons.fileupload2.FileUploadBase.getFileItemFactory()Lorg/apache/commons/fileupload2/FileItemFactory;";
//        String[] parts = line.split(" ");
//        String specID = parts[0];
//        String methodA = parts[1].contains("=") ? parts[1].split("=")[1] : parts[1];
//        String methodB = parts[2].contains("=") ? parts[2].split("=")[1] : parts[2];
//        String packageName = "org.apache.commons.fileupload2";
//        String spec = convertToStateAspect(projectName, specID, methodA, methodB, packageName);
//        System.out.println(spec);
        String mode = args[0];
        if (mode.equals("DUMP")) {
            String methods = args[1];
            String methodA = methods.split(",")[0];
            String methodB = methods.split(",")[1];
            System.out.println(dumpMethod(methodA) + "," + dumpMethod(methodB));
            return;
        }
        if (args.length < 4) {
            System.out.println("ARGS: projectName specFileName outDirName packageName");
            return;
        }
        String projectName = args[0]; //"FileUpload";
        String specFileName = args[1];
        String outDirName = args[2];
        String packageName = args[3];
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
                AspectEvent.count = 1;
                String[] parts = line.split(" ");
                String specID = parts[0];
                System.out.println("creating spec " + specID);
                String methodA = parts[1].contains("=") ? parts[1].split("=")[1] : parts[1];
                String methodB = parts[2].contains("=") ? parts[2].split("=")[1] : parts[2];
                String spec = convertToStateAspect(projectName, specID, methodA, methodB, packageName);
                try (FileWriter writer = new FileWriter(new File(outDir, projectName + specID + ".aj"))) {
                    writer.write(spec);
                }
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    private static String dumpMethod(String methodA) {
        DSISpecParser dsiSpecParser = new DSISpecParser();
        SpecParts parts = dsiSpecParser.parse(methodA);
        return parts.getReturnType() + " " + parts.getReceiver() + "." + parts.getName() + "("
                + String.join(";", parts.getArguments())  + ")";
    }

    /**
     * method adopted from dsi-to-mop
     * @param projectName
     * @param specID
     * @param methodA
     * @param methodB
     * @return
     */
    private static String convertToStateAspect(String projectName, String specID, String methodA, String methodB, String packageName) {
        DSISpecParser parser = new DSISpecParser();
        SpecParts methodAParts = parser.parse(methodA);
        SpecParts methodBParts = parser.parse(methodB);
        List<String> mopSpecParams = computeSpecParams(methodAParts, methodBParts);
        Map<String, String> specParamNameMap = mapNamesToParams(mopSpecParams);
        List<AspectEvent> events = createEvents(methodAParts, methodBParts, specParamNameMap);
        String spec = buildAspectFileFromParts(projectName + specID, specParamNameMap, events, packageName);
        return spec;
    }

    private static String buildAspectFileFromParts(String specName, Map<String, String> specParamNameMap, List<AspectEvent> events,
                                             String packageName) {
        StringBuilder spec = new StringBuilder();
        spec.append(buildSpecHeader(specName, specParamNameMap, packageName));

        spec.append("    StateDiff methodADiff = new StateDiff();\n" +
                "    StateDiff methodBDiff = new StateDiff();\n\n");

        for (Event event : events) {
            spec.append(event);
            spec.append("\n\n");
        }

        spec.append("   before" + events.get(0).getAdviceHeader());
        spec.append(    beforeMethodA);
        spec.append("\n\n");
        spec.append("   after" + events.get(0).getAdviceHeader());
        spec.append(    afterMethodA);
        spec.append("\n\n");

        spec.append("   before" + events.get(1).getAdviceHeader());
        spec.append(beforeMethodB);
        spec.append("\n\n");
        spec.append("   after" + events.get(1).getAdviceHeader());
        spec.append(afterMethodB);
        spec.append("\n\n");

//        spec.append(fsmTemplate.replaceAll("aEvent",
//                methodAParts.getName()).replaceAll("bEvent", methodBParts.getName()));
//        spec.append("\n\n");
//        spec.append(handlerTemplate.replaceAll("SPECCNAME", specName));
        spec.append("}");
        return spec.toString();
    }

    private static String buildSpecHeader(String specName, Map<String, String> specParamNameMap, String packageName) {
        StringBuilder header = new StringBuilder();
        header.append("package " + packageName + ";\n");
        header.append(preamble + "\n\n");
        header.append("public aspect " + specName + " {\n\n");
        return header.toString();
    }


    /**
     * Mostly stolen from dsi-to-mop
     * @param methodAParts
     * @param methodBParts
     * @param specParamNameMap
     * @return
     */
    private static List<AspectEvent> createEvents(SpecParts methodAParts, SpecParts methodBParts,
                                                  Map<String, String> specParamNameMap) {
        List<AspectEvent> events = new ArrayList<>();
        events.add(new AspectEvent(methodAParts, specParamNameMap, "A"));
        events.add(new AspectEvent(methodBParts, specParamNameMap, "B"));
        return events;
    }


    /**
     * method stolen from dsi-to-mop
     * @param methodAParts
     * @param methodBParts
     * @return
     */
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

    /**
     * stolen
     * @param paramType
     * @param suffix
     * @return
     */
    private static String getShortName(String paramType, int suffix) {
        return paramType.substring(paramType.lastIndexOf('.') + 1).toLowerCase().replace("[]", "") + suffix;
    }

    /**
     * method stolen from dsi-to-mop
     * @param mopSpecParams
     * @return
     */
    private static Map<String, String> mapNamesToParams(List<String> mopSpecParams) {
        Map<String, String> map = new HashMap<>();
        int count = 1;
        for (String paramType : mopSpecParams) {
            map.put(getShortName(paramType, count++), paramType);
        }
        return map;
    }
}
