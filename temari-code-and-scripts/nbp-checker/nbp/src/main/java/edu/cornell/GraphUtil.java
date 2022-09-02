package edu.cornell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.yasgl.DirectedGraph;
import edu.illinois.yasgl.DirectedGraphBuilder;
import edu.illinois.yasgl.GraphUtils;

public class GraphUtil {

    private static String artifactsDir;
    private static Map<String, Set<String>> parentMap;
    private static Map<String, Set<String>> childrenMap;
    private static Set<Spec> manualNBPs;

    public static void main(String[] args) {
        String tcFilePath = args[0];
        String specsFilePath = args[1];
        artifactsDir = args[2];
        String parentsFile = args[3];
        String childrenFile = args[4];
        if (args.length == 6) {
            String manualNBPFile = args[5];
            manualNBPs = loadSpecs(manualNBPFile);
        } else {
            manualNBPs = new HashSet<Spec>();
        }
        parentMap = loadHierarchy(parentsFile);
        childrenMap = loadHierarchy(childrenFile);

        Map<String, Set<String>> tc = loadTCFromFile(tcFilePath);
        Set<Spec> specs = loadSpecs(specsFilePath);
        Set<Spec> nbpSpecs = findNBP(tc, specs);
        writeSpecs(nbpSpecs, artifactsDir, true, "nbp-specs.txt");
        writeSpecs(specs, artifactsDir, true, "specs.txt");
        writeTC(tc, artifactsDir, true, "tc.txt");
    }

    private static Map<String, Set<String>> loadHierarchy(String parentsFile) {
        Map<String, Set<String>> map = new HashMap<>();
        DirectedGraphBuilder<String> builder = new DirectedGraphBuilder<>();
        try(BufferedReader specFileReader = new BufferedReader(new FileReader(new File(parentsFile)))) {
            String line;
            while ((line = specFileReader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 1) {
                    builder.addVertex(parts[0]);
                } else {
                    for (String relative : Arrays.asList(parts[1].split(","))) {
                        builder.addEdge(parts[0], relative);
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        DirectedGraph<String> graph = builder.build();
        return GraphUtils.computeTransitiveClosure(graph);
    }

    private static void writeTC(Map<String, Set<String>> tc, String artifactsDir, boolean isPrint, String fileName) {
        if (isPrint) {
            String outFilename = artifactsDir + File.separator + fileName;
            try (BufferedWriter writer = getWriter(outFilename)) {
                for (Map.Entry<String, Set<String>> entry : tc.entrySet()) {
                    writer.write(entry.getKey() + " " + String.join(",", entry.getValue()) + System.lineSeparator());
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private static Map<String, Set<String>> loadTCFromFile(String tcFilePath) {
        Map<String, Set<String>> tc = new HashMap<>();
        try(BufferedReader specFileReader = new BufferedReader(new FileReader(new File(tcFilePath)))) {
            String line;
            while ((line = specFileReader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 1) {
                    throw new IOException("lines in the transitive closure has only one element: " + line);
                }
                if (parts.length > 2) {
                    throw new IOException("lines in the transitive closure has more than two elements: " + parts[0]);
                }
                tc.put(parts[0], removeFields(parts[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tc;
    }

    private static Set<String> removeFields(String part) {
        Set<String> reachable = new HashSet<>();
        for (String reached : part.split(",")) {
            if (isMethod(reached)) {
                reachable.add(reached);
            }
        }
        return reachable;
    }

    private static void writeSpecs(Set<Spec> nbpSpecs, String artifactsDir, boolean isPrint, String nbpSpecFile) {
        if (isPrint) {
            String outFilename = artifactsDir + File.separator + nbpSpecFile;
            try (BufferedWriter writer = getWriter(outFilename)) {
                for (Spec spec : nbpSpecs) {
                    writer.write(spec.toString() + System.lineSeparator());
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }

    private static boolean checkAndAddNBP(Map<String, Set<String>> tc, String methodA, String methodB, Set<Spec> nbpSpecs, Spec spec) {
        boolean ret = false;
        if (tc.keySet().contains(methodA)) {
            if (tc.get(methodA).contains(methodB)) {
                nbpSpecs.add(spec);
                ret = true;
            }
        }
        return ret;
    }

    private static Set<Spec> findNBP(Map<String, Set<String>> tc, Set<Spec> specs) {
        Set<Spec> nbpSpecs = new HashSet<>();
        for (Spec spec : specs) {
            String methodA = spec.getTransformedMethodA();
            String methodB = spec.getTransformedMethodB();
            if (checkAndAddNBP(tc, methodA, methodB, nbpSpecs, spec)) {
                continue;
            } else {
                // check if any of the parents of the class in method-a is the implementer of the method
                if (checkAHierarchy(tc, nbpSpecs, spec, parentMap, "UPWARDS(A)")){
                    continue;
                }
                // check if any of the children of the class in method-a is the implementer of the method
                if (checkAHierarchy(tc, nbpSpecs, spec, childrenMap, "DOWNWARDS(A)")) {
                    continue;
                }
                // check if any of the parents of the class in method-b is the implementer of the method
                if (checkBHierarchy(tc, nbpSpecs, spec, parentMap, "UPWARDS(B)")) {
                    continue;
                }

                // check if any of the parents of the class in method-b is the implementer of the method
                if (checkBHierarchy(tc, nbpSpecs, spec, childrenMap, "DOWNWARDS(B)")) {
                    continue;
                }

                // do a two-way check to see if any of the parents/children of the class in method-a or method-b
                // is the implementer of the method
                if (checkABHierarchy(tc, nbpSpecs, spec, parentMap, childrenMap)) {
                    continue;
                }

                if (checkABHierarchy(tc, nbpSpecs, spec, childrenMap, parentMap)) {
                    continue;
                }


                // if it is, then it should also be a DYNAMIC_DISPATCH case
                // if not we should print it for debugging
            }
        }
        Set<Spec> manualMissed = new HashSet<>(nbpSpecs);
        Set<Spec> scriptMissed = new HashSet<>(manualNBPs);
        manualMissed.removeAll(manualNBPs);
        scriptMissed.removeAll(nbpSpecs);
        System.out.println("Manual: " + manualNBPs.size());
        System.out.println("Script: " + nbpSpecs.size());
        System.out.println("ManualMissed: " + manualMissed.size());
        System.out.println("Script Missed: " + scriptMissed.size());
        writeSpecs(manualMissed, artifactsDir, true, "manual-missed-specs.txt");
        writeSpecs(scriptMissed, artifactsDir, true, "script-missed-specs.txt");

        return nbpSpecs;
    }

    private static boolean checkABHierarchy(Map<String, Set<String>> tc, Set<Spec> nbpSpecs, Spec spec,
                                            Map<String, Set<String>> parentMap, Map<String, Set<String>> childrenMap) {
        boolean ret = false;
        String methodA = spec.getTransformedMethodA();
        String methodB = spec.getTransformedMethodB();
        ArrayList<String> aParts = spec.getTransformedAParts();
        ArrayList<String> bParts = spec.getTransformedBParts();
        Set<String> ancestors = parentMap.get(aParts.get(0));
        Set<String> descendants = childrenMap.get(bParts.get(0));
        String candidateB;
        if (ancestors != null && !ancestors.isEmpty()) {
            for (String ancestor : ancestors) {
                String candidateA = String.join("", ancestor, aParts.get(1), aParts.get(2));
                // check if the ancestor implements method-a and that implementation forms a spec with method-b
                if (checkAndAddNBP(tc, candidateA, methodB, nbpSpecs, spec)) {
                    System.out.println("FOUND UPWARDS(AB): " + spec.getSpecID());
                    ret = true;
                    break;
                }
                // check if the ancestor implements method-a and that implementation forms a spec with a method-b that
                // is implemented in any of the ancestors
                for (String ancestorB : ancestors) {
                    candidateB = String.join("", ancestorB, bParts.get(1), bParts.get(2));
                    if (checkAndAddNBP(tc, candidateA, candidateB, nbpSpecs, spec)) {
                        System.out.println("FOUND UPWARDS(AB): " + spec.getSpecID());
                        ret = true;
                        break;
                    }
                }
                // check if the ancestor implements method-a and that implementation forms a spec with a method-b that
                // is implemented in any of the descendants
                if (descendants != null && !descendants.isEmpty()) {
                    for (String descendant : descendants) {
                        candidateB = String.join("", descendant, bParts.get(1), bParts.get(2));
                        if (checkAndAddNBP(tc, candidateA, candidateB, nbpSpecs, spec)) {
                            System.out.println("FOUND UPWARDS-DOWNWARDS(AB): " + spec.getSpecID());
                            ret = true;
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    private static boolean checkAHierarchy(Map<String, Set<String>> tc, Set<Spec> nbpSpecs, Spec spec,
                                           Map<String, Set<String>> hierarchy, String direction) {
        boolean ret = false;
        String methodB = spec.getTransformedMethodB();
        ArrayList<String> aParts = spec.getTransformedAParts();
        Set<String> relatives = hierarchy.get(aParts.get(0));
        if (relatives != null && !relatives.isEmpty()) {
            for (String relative : relatives) {
                String candidate = String.join("", relative, aParts.get(1), aParts.get(2));
                if (checkAndAddNBP(tc, candidate, methodB, nbpSpecs, spec)) {
                    System.out.println("FOUND " + direction + ": " + spec.getSpecID());
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    private static boolean checkBHierarchy(Map<String, Set<String>> tc, Set<Spec> nbpSpecs, Spec spec,
                                           Map<String, Set<String>> hierarchy, String direction) {
        boolean ret = false;
        String methodA = spec.getTransformedMethodA();
        ArrayList<String> bParts = spec.getTransformedBParts();
        Set<String> relatives = hierarchy.get(bParts.get(0));
        if (relatives != null && !relatives.isEmpty()) {
            for (String relative : relatives) {
                String candidate = String.join("", relative, bParts.get(1), bParts.get(2));
                if (checkAndAddNBP(tc, methodA, candidate, nbpSpecs, spec)) {
                    System.out.println("FOUND " + direction + ": " + spec.getSpecID());
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    private static Set<Spec> loadSpecs(String specsFilePath) {
        Set<Spec> loadedSpecs = new HashSet<>();
        try(BufferedReader specFileReader = new BufferedReader(new FileReader(new File(specsFilePath)))) {
            String line;
            while((line = specFileReader.readLine()) != null) {
                loadedSpecs.add(new Spec(line.split(" ")));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadedSpecs;
    }

    private static boolean isMethod(String fromNode) {
        return fromNode.endsWith(")");
    }

    public static BufferedWriter getWriter(String filePath) {
        Path path = Paths.get(filePath);
        BufferedWriter writer = null;
        try {
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return writer;
    }
}
