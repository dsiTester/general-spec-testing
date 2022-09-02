package edu.cornell.components;

import java.util.Map;
import java.util.StringJoiner;

import edu.cornell.converter.DSIToMOPConverter;
import edu.cornell.parser.SpecParts;

public class Event {
    String eventHeader;
    String returningText;
    String callText;
    String targetText;
    String argsText;

    // default constructor so that I can inherit in spec-to-aspect for state-comparison
    public Event() {}

    public Event(SpecParts parts, Map<String, String> specParamNameMap) {
        this.eventHeader = computeEventHeader(parts, specParamNameMap);
        this.returningText = computeReturningText(parts, specParamNameMap);
        this.callText = computeCallText(parts, specParamNameMap);
        this.targetText = computeTargetText(parts, specParamNameMap);
        this.argsText = computeArgsText(parts, specParamNameMap);
    }

    private String computeEventHeader(SpecParts parts, Map<String, String> specParamNameMap) {
        return "\tevent " + parts.getName() + " after" + getPointCutParams(parts, specParamNameMap);
    }

    protected String getPointCutParams(SpecParts parts, Map<String, String> specParamNameMap) {
        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        String entryFromMap = getEntryFromMap(parts.getReceiver(), specParamNameMap);
        joiner.add(entryFromMap);
//        if (!DSIToMOPConverter.primitiveTypes.contains(parts.getReturnType())
//                && specParamNameMap.values().contains(parts.getReturnType())) {
//            joiner.add(getEntryFromMap(parts.getReturnType(), specParamNameMap));
//        }
        return joiner.toString();
    }

    protected String getEntryFromMap(String type, Map<String, String> specParamNameMap) {
        for (Map.Entry entry : specParamNameMap.entrySet()) {
            if (entry.getValue().equals(type)) {
                return entry.getValue() + " " + entry.getKey();
            }
        }
        return "";
    }

    private String computeReturningText(SpecParts parts, Map<String, String> specParamNameMap) {
        StringBuilder text = new StringBuilder();
        String entryFromMap = getEntryFromMap(parts.getReturnType(), specParamNameMap);
        if (!entryFromMap.isEmpty()) {
            text.append("returning(");
            text.append(entryFromMap);
            text.append(")");
        }
        return text.toString();
    }

    protected String computeCallText(SpecParts parts, Map<String, String> specParamNameMap) {
        StringBuilder text = new StringBuilder("\t\tcall(" + parts.getReturnType() + " ");
        text.append(parts.getReceiver());
        text.append(".");
        text.append(parts.getName());
        text.append(getArgTypes(parts));
        text.append(")");
        return text.toString();
    }

    private String getArgTypes(SpecParts parts) {
        StringJoiner joiner =  new StringJoiner(",", "(", ")");
        for (String arg : parts.getArguments()) {
            joiner.add(arg);
        }
        return joiner.toString();
    }

    protected String computeArgsText(SpecParts parts, Map<String, String> specParamNameMap) {
        StringBuilder text = new StringBuilder("");
        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        int count = 1;
        if (!parts.getArguments().isEmpty()) {
            for (String argType : parts.getArguments()) {
                if (specParamNameMap.containsValue(argType)) {
                    joiner.add(getEntryFromMap(argType, specParamNameMap).split(" ")[1]);
                } else {
                    joiner.add(argType.substring(argType.lastIndexOf(".") + 1).substring(0, 3).toLowerCase() + count++);
                }
            }
            text.append("args");
            text.append(joiner.toString());
        }
        return text.toString();
    }

    protected String computeTargetText(SpecParts parts, Map<String, String> specParamNameMap) {
        StringBuilder text = new StringBuilder("target(");
        text.append(getEntryFromMap(parts.getReceiver(), specParamNameMap).split(" ")[1]);
        text.append(")");
        return text.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(eventHeader);
        builder.append(" ");
        builder.append(returningText);
        builder.append(":\n");
        StringJoiner joiner = new StringJoiner(" && ", "", " {}");
        joiner.add(callText);
        if (!argsText.isEmpty()) {
            joiner.add(argsText);
        }
        joiner.add(targetText);
        builder.append(joiner.toString());
        return builder.toString();
    }
}
