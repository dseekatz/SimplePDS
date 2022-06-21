package ds.simplepds.automata.demand;

import ds.simplepds.automata.PAutomaton;
import ds.simplepds.interfaces.Rule;
import ds.simplepds.interfaces.StackSymbol;

import java.util.function.Function;

public class WildcardPostStar<L,S> extends DemandPostStar<L,S>{
    public WildcardPostStar(
            ForwardFlowFunctions<L, S> flowFunction,
            PAutomaton<L, S> initialAutomaton,
            Function<Rule<L, S>, L> generatedStateIdentifierFunction
    ) {
        super(flowFunction, initialAutomaton, generatedStateIdentifierFunction);
    }

    @Override
    protected void handleNormalRule(Rule<L, S> rule, PAutomaton.Transition<L, S> current) {
        if (rule.getStartConfiguration().getStackSymbol().equals(current.getLabel())) {
            StackSymbol<S> label;
            if (rule.getEndConfiguration().getWord().get(0) instanceof Wildcard<S>) {
                label = current.getLabel();
            } else {
                label = rule.getEndConfiguration().getWord().get(0);
            }
            worklist.add(new PAutomaton.Transition<>(
                    rule.getEndConfiguration().getControlLocation(),
                    current.getEndState(),
                    label
            ));
        }
    }

}
