package ds.simplepds.automata;

import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.Rule;
import ds.simplepds.interfaces.StackSymbol;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PAutomaton<L,S> {

    private final Set<ControlLocation<L>> initialStates = new HashSet<>();
    private final Set<ControlLocation<L>> finalStates = new HashSet<>();
    private final Set<ControlLocation<L>> states = new HashSet<>();
    private final Set<Transition<L,S>> transitionRelation = new HashSet<>();


    public void addInitialState(ControlLocation<L> initialState) {
        initialStates.add(initialState);
        states.add(initialState);
    }

    public void addFinalState(ControlLocation<L> finalState) {
        finalStates.add(finalState);
        states.add(finalState);
    }

    public void addTransition(ControlLocation<L> start, ControlLocation<L> end, StackSymbol<S> label) {
        transitionRelation.add(
                new Transition<>(
                        start,
                        end,
                        label
                )
        );
        states.add(start);
        states.add(end);
    }

    public void addTransition(Transition<L, S> t) {
        transitionRelation.add(t);
        states.add(t.startState);
        states.add(t.endState);
    }

    public Set<ControlLocation<L>> getInitialStates() {
        return initialStates;
    }

    public Set<ControlLocation<L>> getFinalStates() {
        return finalStates;
    }

    public Set<Transition<L, S>> getTransitionRelation() {
        return transitionRelation;
    }

    public Set<ControlLocation<L>> getAllStates() {
        return states;
    }

    public void addState(ControlLocation<L> state) {
        states.add(state);
    }

    public String toDotString() {
        StringBuilder out = new StringBuilder();
        out.append("digraph {\n");
        for (Transition<L,S> t : transitionRelation) {
            out.append("\t\"")
                    .append(t.startState)
                    .append("\"")
                    .append(" -> ")
                    .append("\"")
                    .append(t.endState)
                    .append("\"")
                    .append("[label=\"")
                    .append(t.label)
                    .append("\"];\n");
        }
        out.append("}");
        return out.toString();
    }

    public static class Transition<L,S> {

        private final ControlLocation<L> startState;
        private final ControlLocation<L> endState;
        private final StackSymbol<S> label;

        public Transition(ControlLocation<L> startState, ControlLocation<L> endState, StackSymbol<S> label) {
            this.startState = startState;
            this.endState = endState;
            this.label = label;
        }

        public ControlLocation<L> getStartState() {
            return startState;
        }

        public ControlLocation<L> getEndState() {
            return endState;
        }

        public StackSymbol<S> getLabel() {
            return label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Transition<?, ?> that = (Transition<?, ?>) o;
            return Objects.equals(startState, that.startState) && Objects.equals(endState, that.endState) && Objects.equals(label, that.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(startState, endState, label);
        }
    }
}
