package ds.simplepds.automata;

import ds.simplepds.interfaces.Configuration;
import ds.simplepds.interfaces.PushdownSystem;
import ds.simplepds.interfaces.Rule;

/**
 * An instance of the prestar algorithm for a given initial configuration and pushdown system
 * @param <L>
 * @param <S>
 */
public class Prestar<L,S> {

    private Configuration<L,S> initial;
    private PAutomaton<L,S> saturatedAut;

    /**
     * Creates and runs an instance of the prestar algorithm.
     *
     * @param initialConfiguration to construct the initial PAutomaton
     * @param pushdownSystem to be used to saturate the initial PAutomaton
     * @throws InvalidInstanceException if the provided initial configuration is not present in the pushdown system
     */
    public Prestar(Configuration<L,S> initialConfiguration, PushdownSystem<L,S> pushdownSystem)
            throws InvalidInstanceException {
        // Check that the initial configuration occurs in the pushdown system as the start con
        if (pushdownSystem.getRules().stream()
                .map(Rule::getStartConfiguration)
                .noneMatch(config -> config.equals(initialConfiguration))
        ) {
            throw new InvalidInstanceException("Could not create Prestar instance: provided initial configuration" +
                    "is not valid for the provided pushdown system");
        }
        this.saturatedAut = createInitialAutomaton();
        this.apply();
    }

    /**
     * TODO
     * @return
     */
    private PAutomaton<L,S> createInitialAutomaton() {
        return null;
    }

    /**
     * TODO
     */
    private void apply() {

    }

    public Configuration<L, S> getInitial() {
        return initial;
    }

    public PAutomaton<L, S> getSaturatedAut() {
        return saturatedAut;
    }
}
