package edu.cornell;

import edu.cornell.components.Event;
import edu.cornell.parser.SpecParts;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class AspectEvent extends Event {

    String eventHeader;
    String callText;
    String targetText;
    String argsText;

    String adviceHeader;

    String varNames;

    public static int count = 1;
    String objName;

    // methodMode = A or B
    public AspectEvent(SpecParts parts, Map<String, String> specParamNameMap, String methodMode) {
        String pointCutParams = getPointCutParams(parts, specParamNameMap);
        this.eventHeader = "    pointcut callMethod" + methodMode + " " + pointCutParams;
        this.callText = super.computeCallText(parts, specParamNameMap);
        String callMethodArgs;
        if (this.varNames.equals("")) {
            this.argsText = "";
            callMethodArgs = this.objName;
        } else {
            this.argsText = "args(" + this.varNames + ")";
            callMethodArgs = this.objName + ", " + this.varNames;
        }
        this.targetText = super.computeTargetText(parts, specParamNameMap);

        this.adviceHeader = pointCutParams + " : " + "callMethod" + methodMode + "(" + callMethodArgs + ") { \n";
    }

    private String computeHeaderArgsText(SpecParts parts, Map<String, String> specParamNameMap) {
        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        String entryFromMap = super.getEntryFromMap(parts.getReceiver(), specParamNameMap);
        joiner.add(entryFromMap.split(" ")[1]);
        int count=1;
        for (String argType : parts.getArguments()) {
            if (specParamNameMap.containsValue(argType)) {
                joiner.add(super.getEntryFromMap(argType, specParamNameMap).split(" ")[1]);
            } else {
                String newArg = argType.substring(argType.lastIndexOf(".") + 1).substring(0, 3).toLowerCase() + count++;
                joiner.add(newArg);
                specParamNameMap.put(newArg, argType);
            }
        }
        return joiner.toString();
    }

    public String getAdviceHeader() {
        return adviceHeader;
    }

    @Override
    protected String getPointCutParams(SpecParts parts, Map<String, String> specParamNameMap) {
        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        String entryFromMap = super.getEntryFromMap(parts.getReceiver(), specParamNameMap);
        joiner.add(entryFromMap);
        this.objName = entryFromMap.split(" ")[1];
        Set<String> seenArgTypes = new HashSet<>();
        StringJoiner varNameJoiner = new StringJoiner(", ");
        for (String argType : parts.getArguments()) {
            if (seenArgTypes.contains(argType)) {
                String newArg = argType.substring(argType.lastIndexOf(".") + 1).substring(0, 3).toLowerCase() + count++;
                joiner.add(argType + " " + newArg);
                varNameJoiner.add(newArg);
            } else if (specParamNameMap.containsValue(argType)) {
                String entry = super.getEntryFromMap(argType, specParamNameMap);
                joiner.add(entry); //
                varNameJoiner.add(entry.split(" ")[1]);
            } else {
                String newArg = argType.substring(argType.lastIndexOf(".") + 1).substring(0, 3).toLowerCase() + count++;
                joiner.add(argType + " " + newArg);
                specParamNameMap.put(newArg, argType);
                varNameJoiner.add(newArg);
            }
            seenArgTypes.add(argType);
        }
        this.varNames = varNameJoiner.toString();
//        if (!DSIToMOPConverter.primitiveTypes.contains(parts.getReturnType())
//                && specParamNameMap.values().contains(parts.getReturnType())) {
//            joiner.add(getEntryFromMap(parts.getReturnType(), specParamNameMap));
//        }
        return joiner.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.eventHeader);
        builder.append(":\n");
        StringJoiner joiner = new StringJoiner(" && ", "", " ;");
        joiner.add(this.callText);
        if (!this.argsText.isEmpty()) {
            joiner.add(this.argsText);
        }

        joiner.add(this.targetText);
        builder.append(joiner.toString());
        return builder.toString();
    }
}
