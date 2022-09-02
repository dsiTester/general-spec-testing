package com.runtimeverification.rvmonitor.logicrepository.outputfsmparsing.parserast;

import java.util.List;

import com.runtimeverification.rvmonitor.logicrepository.outputfsmparsing.parservisitor.GenericVisitor;
import com.runtimeverification.rvmonitor.logicrepository.outputfsmparsing.parservisitor.VoidVisitor;

public class FSMItem extends Node {

    String state;
    List<FSMTransition> Transitions;

    public FSMItem(int line, int column, String state,
            List<FSMTransition> Transitions) {
        super(line, column);
        this.state = state;
        this.Transitions = Transitions;
    }

    public String getState() {
        return state;
    }

    public List<FSMTransition> getTransitions() {
        return Transitions;
    }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) {
        v.visit(this, arg);
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

}
