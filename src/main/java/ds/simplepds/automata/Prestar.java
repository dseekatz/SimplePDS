package ds.simplepds.automata;

import ds.simplepds.interfaces.Configuration;
import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.EndConfiguration;
import ds.simplepds.interfaces.PushdownSystem;
import ds.simplepds.interfaces.Rule;

import java.util.LinkedList;
import java.util.Queue;

/**
 * An instance of the prestar algorithm for a given initial configuration and pushdown system
 * @param <L>
 * @param <S>
 */
public class Prestar<L,S> {

    private EndConfiguration<L,S> initial;
    private PAutomaton<L,S> saturatedAut;
    private PushdownSystem<L,S> pushdownSystem;

    /**
     * Creates and runs an instance of the prestar algorithm.
     *
     * @param initialConfiguration to construct the initial PAutomaton
     * @param pushdownSystem to be used to saturate the initial PAutomaton
     * @throws InvalidInstanceException if the provided initial configuration is not present in the pushdown system
     */
    public Prestar(EndConfiguration<L,S> initialConfiguration, PushdownSystem<L,S> pushdownSystem)
            throws InvalidInstanceException {
        // Check that the initial configuration occurs in the pushdown system as the end configuration
        // of at least one rule
        if (pushdownSystem.getRules().stream()
                .map(Rule::getEndConfiguration)
                .noneMatch(config -> config.equals(initialConfiguration))
        ) {
            throw new InvalidInstanceException("Could not create Prestar instance: provided initial configuration" +
                    "is not valid for the provided pushdown system");
        }
        this.initial = initialConfiguration;
        this.pushdownSystem = pushdownSystem;
        this.saturatedAut = createInitialAutomaton();
        this.apply();
    }

    /**
     * Create an initial automaton A. Initially, the automaton contains two states.
     * The first state is the control location specified by the initial configuration.
     * The second is a dummy accepting (final) state.
     * We add a transition from the first to the second, labelled with the epsilon (empty) label.
     * @return
     */
    private PAutomaton<L,S> createInitialAutomaton() {
        PAutomaton<L,S> initialAutomaton = new PAutomaton<>();
        initialAutomaton.addFinalState(initialAutomaton.getDummyAcceptingState());
        initialAutomaton.addTransition(
                initial.getControlLocation(),
                initialAutomaton.getDummyAcceptingState(),
                initialAutomaton.getEpsilonStackSymbol()
        );
        return initialAutomaton;
    }

    /**
     * Implementation of pre-* as a worklist algorithm
     */
    private void apply() {
        Queue<ControlLocation<L>> worklist = new LinkedList<>();
        worklist.add(initial.getControlLocation());
        while (!worklist.isEmpty()) {
            pushdownSystem.getRules().forEach(rule -> {
                ControlLocation<L> endLocation = rule.getEndConfiguration().getControlLocation();
                if (saturatedAut.getAllStates().contains(endLocation)) {
                    ControlLocation<L> startLocation = rule.getStartConfiguration().getControlLocation();
                    worklist.add(startLocation);
                    saturatedAut.addTransition(
                            startLocation,
                            saturatedAut.getDummyAcceptingState(),
                            rule.getStartConfiguration().getStackSymbol()
                    );
                }
            });
            worklist.remove();
        }
    }

    public Configuration<L, S> getInitial() {
        return initial;
    }

    public PAutomaton<L, S> getSaturatedAut() {
        return saturatedAut;
    }
}
