package edu.cornell.parser;

import java.util.List;

public class SpecParts {
    private List<String> arguments;
    private String receiver;
    private String name;
    private String returnType;

    public SpecParts(List<String> arguments, String receiver, String name, String returnType) {
        setArguments(arguments);
        setReceiver(receiver);
        setName(name);
        setReturnType(returnType);
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return "SpecParts{"
                + "arguments=" + arguments
                + ", receiver='" + receiver + '\''
                + ", name='" + name + '\''
                + ", returnType='" + returnType + '\''
                + '}';
    }
}
