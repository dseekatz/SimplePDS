package ds.simplepds.automata.demand;

import ds.simplepds.automata.PAutomaton;
import ds.simplepds.interfaces.Rule;
import ds.simplepds.interfaces.StackSymbol;

public class WildcardPreStar<L,S> extends DemandPreStar<L,S> {
    public WildcardPreStar(BackwardFlowFunctions<L, S> flowFunction, PAutomaton<L, S> initialAutomaton) {
        super(flowFunction, initialAutomaton);
    }

    @Override
    protected void handleNormalRule(Rule<L, S> rule, PAutomaton.Transition<L, S> current) {
        if (rule.getEndConfiguration().getWord().get(0).equals(current.getLabel())) {
            StackSymbol<S> label;
            if (rule.getStartConfiguration().getStackSymbol() instanceof Wildcard<S>) {
                label = current.getLabel();
            } else {
                label = rule.getStartConfiguration().getStackSymbol();
            }
            worklist.add(new PAutomaton.Transition<>(
                    rule.getStartConfiguration().getControlLocation(),
                    current.getEndState(),
                    label
            ));
        }
    }


}
