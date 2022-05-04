package ds.simplepds.automata;

import ds.simplepds.interfaces.Configuration;
import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.EndConfiguration;
import ds.simplepds.interfaces.PushdownSystem;
import ds.simplepds.interfaces.Rule;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * An instance of the prestar algorithm for a given initial configuration and pushdown system
 * @param <L>
 * @param <S>
 */
public class Prestar<L,S> {

    private final EndConfiguration<L,S> initial;
    private final PAutomaton<L,S> saturatedAut;
    private final PushdownSystem<L,S> pushdownSystem;

    public Prestar(
            EndConfiguration<L,S> initialConfiguration,
            PushdownSystem<L,S> pushdownSystem,
            L acceptingStateUnwrapValue,
            S epsilonSymbolUnwrapValue
    )
            throws InvalidInstanceException {
        checkValidity(initialConfiguration, pushdownSystem);
        this.initial = initialConfiguration;
        this.pushdownSystem = pushdownSystem;
        this.saturatedAut = createInitialAutomaton(acceptingStateUnwrapValue, epsilonSymbolUnwrapValue);
        this.apply();
    }

    public Prestar(
            EndConfiguration<L,S> initialConfiguration,
            PushdownSystem<L,S> pushdownSystem,
            PAutomaton<L,S> initialAutomaton
    )
            throws InvalidInstanceException {
        checkValidity(initialConfiguration, pushdownSystem);
        this.initial = initialConfiguration;
        this.pushdownSystem = pushdownSystem;
        this.saturatedAut = initialAutomaton;
        this.apply();
    }

    private void checkValidity(EndConfiguration<L,S> initialConfiguration, PushdownSystem<L,S> pushdownSystem)
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
    }

    /**
     * Create an initial automaton A. Initially, the automaton contains two states.
     * The first state is the control location specified by the initial configuration.
     * The second is a dummy accepting (final) state.
     * We add a transition from the first to the second, labelled with the epsilon (empty) label.
     * @return
     * @param acceptingStateUnwrapValue
     * @param epsilonSymbolUnwrapValue
     */
    private PAutomaton<L,S> createInitialAutomaton(L acceptingStateUnwrapValue, S epsilonSymbolUnwrapValue) {
        PAutomaton<L,S> initialAutomaton = new PAutomaton<>(acceptingStateUnwrapValue, epsilonSymbolUnwrapValue);
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
        Set<ControlLocation<L>> visited = new HashSet<>();
        worklist.add(initial.getControlLocation());
        while (!worklist.isEmpty()) {
            ControlLocation<L> current = worklist.remove();
            visited.add(current);
            pushdownSystem.getRules().forEach(rule -> {
                ControlLocation<L> endLocation = rule.getEndConfiguration().getControlLocation();
                if (endLocation.equals(current)) {
                    ControlLocation<L> startLocation = rule.getStartConfiguration().getControlLocation();
                    if (!visited.contains(startLocation)) {
                        worklist.add(startLocation);
                    }
                    saturatedAut.addTransition(
                            startLocation,
                            saturatedAut.getDummyAcceptingState(),
                            rule.getStartConfiguration().getStackSymbol()
                    );
                }
            });
        }
    }

    public Configuration<L, S> getInitial() {
        return initial;
    }

    public PAutomaton<L, S> getSaturatedAut() {
        return saturatedAut;
    }
}
