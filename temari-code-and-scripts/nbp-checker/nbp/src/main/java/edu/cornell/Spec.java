package edu.cornell;

import java.util.ArrayList;
import java.util.Objects;

public class Spec {
    private String specID;
    private String methodA;
    private String methodB;
    private ArrayList<String> transformedAParts = new ArrayList<>();
    private ArrayList<String> transformedBParts = new ArrayList<>();

    public Spec(String specID, String methodA, String methodB) {
        this.specID = specID;
        this.methodA = process(methodA);
        this.methodB = process(methodB);
    }

    private String process(String method) {
        if (method.contains("=")) {
            return method.split("=")[1];
        }
        return method;
    }

    public Spec(String[] specParts) {
        this(specParts[0], specParts[1], specParts[2]);
    }

    public String getSpecID() {
        return specID;
    }

    public String getMethodA() {
        return methodA;
    }

    public String getTransformedMethodA() {
        if (transformedAParts.isEmpty()) {
            transformedAParts = transform(methodA);
        }
        return String.join("", transformedAParts);
    }

    private ArrayList<String> transform(String method) {
        ArrayList<String> transformedParts = new ArrayList<>();
        String[] parts = method.split("[()]");
        String clas = parts[0].substring(0, parts[0].lastIndexOf(".")).replaceAll("\\.", "/");
        String methodPart = parts[0].substring(parts[0].lastIndexOf(".")).replace(".", "#");
        String args = "(" + parts[1] + ")";
        transformedParts.add(clas);
        transformedParts.add(methodPart);
        transformedParts.add(args);
        return transformedParts;
    }

    public String getMethodB() {
        return methodB;
    }

    public String getTransformedMethodB() {
        if (transformedBParts.isEmpty()) {
            transformedBParts = transform(methodB);
        }
        return String.join("", transformedBParts);
    }

    public ArrayList<String> getTransformedAParts() {
        return transformedAParts;
    }

    public ArrayList<String> getTransformedBParts() {
        return transformedBParts;
    }

    @Override
    public String toString() {
        return String.join(" ", getSpecID(), getMethodA(), getMethodB());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Spec spec = (Spec) o;
        return Objects.equals(getSpecID(), spec.getSpecID()) &&
               Objects.equals(getMethodA(), spec.getMethodA()) &&
               Objects.equals(getMethodB(), spec.getMethodB());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSpecID(), getMethodA(), getMethodB());
    }
}
