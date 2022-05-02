package ds.simplepds.automata;

import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.StackSymbol;

import java.util.Set;

public class PAutomaton<L,S> {

    private Set<ControlLocation<L>> initialStates;
    private Set<ControlLocation<L>> finalStates;
    private Set<Transition<L,S>> transitionRelation;

    /**
     * Merge nondistinguishable states using Hopcroft's algorithm
     */
    public void minimize() {
        // TODO
    }


    private static class Transition<L,S> {
        private ControlLocation<L> startState;
        private ControlLocation<L> endState;
        private StackSymbol<S> label;
    }
}
