package ds.simplepds.automata;

import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.EndConfiguration;
import ds.simplepds.interfaces.PushdownSystem;
import ds.simplepds.interfaces.Rule;
import ds.simplepds.interfaces.StackSymbol;
import ds.simplepds.interfaces.StartConfiguration;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @param <L>
 * @param <S>
 */
public class Poststar<L,S> {

    private final StartConfiguration<L,S> initial;
    private final PAutomaton<L,S> saturatedAut;
    private final PushdownSystem<L,S> pushdownSystem;

    public Poststar(
            StartConfiguration<L,S> initialConfiguration,
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

    public Poststar(
            StartConfiguration<L,S> initialConfiguration,
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

    private void checkValidity(StartConfiguration<L,S> initialConfiguration, PushdownSystem<L,S> pds)
            throws InvalidInstanceException {
        // Check that the initial configuration occurs in the pushdown system as the start configuration
        // of at least one rule
        if (pds.getRules().stream()
                .map(Rule::getStartConfiguration)
                .noneMatch(config -> config.equals(initialConfiguration))
        ) {
            throw new InvalidInstanceException("Could not create Poststar instance: provided initial configuration" +
                    "is not valid for the provided pushdown system");
        }
    }

    /**
     * Implementation of post-*.  See Esparza, et al. (CAV00).
     */
    private void apply() {
        // Step 1
        pushdownSystem.getRules().stream()
                .filter(rule -> rule.getStartConfiguration().getControlLocation().equals(initial.getControlLocation()))
                .filter(rule -> rule.getEndConfiguration().getWord().size() == 2)
                .forEach(rule -> {
                    PAutomaton<L, S>.CreatedState createdState = saturatedAut.generateNewState(rule);
                    ControlLocation<L> transitionStart = rule.getEndConfiguration().getControlLocation();
                    StackSymbol<S> transitionSymbol = rule.getEndConfiguration().getWord().get(0);
                    saturatedAut.addTransition(
                            transitionStart,
                            createdState,
                            transitionSymbol
                    );
                });

        // Step 2
        Queue<ControlLocation<L>> worklist = new LinkedList<>();
        worklist.add(initial.getControlLocation());
        while (!worklist.isEmpty()) {
            ControlLocation<L> current = worklist.remove();
            pushdownSystem.getRules().forEach(rule -> {
                ControlLocation<L> startLocation = rule.getStartConfiguration().getControlLocation();
                if (startLocation.equals(current)) {
                    worklist.add(startLocation);
                    EndConfiguration<L,S> endConfiguration = rule.getEndConfiguration();
                    if (endConfiguration.getWord().size() == 2) {
                        saturatedAut.addTransition(
                                saturatedAut.getGeneratedStateFromRule(rule),
                                saturatedAut.getDummyAcceptingState(),
                                endConfiguration.getWord().get(1)
                        );
                    } else {
                        saturatedAut.addTransition(
                                endConfiguration.getControlLocation(),
                                saturatedAut.getDummyAcceptingState(),
                                endConfiguration.getWord().get(0)
                        );
                    }
                }
            });
        }
    }

    /**
     * Create an initial automaton A. Initially, the automaton contains two states.
     * The first state is the control location specified by the initial configuration.
     * The second is a dummy accepting (final) state.
     * We add a transition from the first to the second, labelled with the symbol of the initial configuration.
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
                initial.getStackSymbol()
        );
        return initialAutomaton;
    }
}
