package com.runtimeverification.rvmonitor.logicrepository;

public class TransitionTriple {

    private final String src;
    private final String dest;
    private final String event;

    public TransitionTriple(String src, String dest, String event) {
        this.src = src;
        this.dest = dest;
        this.event = event;
    }

    @Override
    public String toString() {
        return src + " " + dest + " " + event;
    }

    public String getSrc() {
        return src;
    }

    public String getDest() {
        return dest;
    }

    public String getEvent() {
        return event;
    }
}
