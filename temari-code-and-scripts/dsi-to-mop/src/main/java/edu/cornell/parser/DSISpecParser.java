package edu.cornell.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.objectweb.asm.Type;

/**
 * This class parses methods from DSI output into its parts.
 */
public class DSISpecParser {

    public SpecParts parse(String dsiMethod) {
        return new SpecParts(parseArguments(dsiMethod),
                parseReceiver(dsiMethod),
                parseMethodName(dsiMethod),
                parseReturnType(dsiMethod));
    }

    public String parseReturnType(String dsiMethod) {
        String signature = dsiMethod.substring(dsiMethod.indexOf('('));
        return Type.getReturnType(signature).getClassName();
    }

    private List<String> parseArguments(String dsiMethod) {
        List<String> args = new ArrayList<>();
        String desc = dsiMethod.substring(dsiMethod.indexOf('('));
        for (Type type : Type.getArgumentTypes(desc)) {
            args.add(type.getClassName());
        }
        return args;
    }

    private String parseMethodName(String dsiMethod) {
        return dsiMethod.substring(dsiMethod.lastIndexOf('.') + 1, dsiMethod.indexOf('('));
    }

    private String parseReceiver(String dsiMethod) {
        return dsiMethod.substring(0, dsiMethod.lastIndexOf('.')).replaceAll(Matcher.quoteReplacement("$"), ".");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            return;
        }
        DSISpecParser parser = new DSISpecParser();
    }
}
