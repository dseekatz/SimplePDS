package ds.simplepds;

import ds.simplepds.automata.PAutomaton;
import ds.simplepds.automata.Poststar;
import ds.simplepds.automata.Prestar;
import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.EndConfiguration;
import ds.simplepds.interfaces.PushdownSystem;
import ds.simplepds.interfaces.Rule;
import ds.simplepds.interfaces.StackSymbol;
import ds.simplepds.interfaces.StartConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PDSTests {

    private static PushdownSystem<String, String> pushAndPopPDS;
    private static PAutomaton<String, String> initialAut;
    private static Rule<String, String> stateGeneratingRuleM1;
    private static Rule<String, String> stateGeneratingRuleM2;

    @BeforeEach
    public void build() {
        // See example in Esparza, et al. (CAV00)
        ControlLocation<String> p0 = TestUtils.createControlLocation("p0");
        ControlLocation<String> p1 = TestUtils.createControlLocation("p1");
        ControlLocation<String> p2 = TestUtils.createControlLocation("p2");
        StackSymbol<String> g0 = TestUtils.createStackSymbol("g0");
        StackSymbol<String> g1 = TestUtils.createStackSymbol("g1");
        StackSymbol<String> g2 = TestUtils.createStackSymbol("g2");

        StartConfiguration<String, String> p0g0Start = TestUtils.createStartConfiguration(p0, g0);
        StartConfiguration<String, String> p1g1Start = TestUtils.createStartConfiguration(p1, g1);
        StartConfiguration<String, String> p2g2Start = TestUtils.createStartConfiguration(p2, g2);
        StartConfiguration<String, String> p0g1Start = TestUtils.createStartConfiguration(p0, g1);

        EndConfiguration<String, String> p1g1g0End = TestUtils.createPushEndConfiguration(p1, g1, g0);
        EndConfiguration<String, String> p2g2g0End = TestUtils.createPushEndConfiguration(p2, g2, g0);
        EndConfiguration<String, String> p0g1End = TestUtils.createNormalEndConfiguration(p0, g1);
        EndConfiguration<String, String> p0End = TestUtils.createPopEndConfiguration(p0);

        Set<Rule<String, String>> rules = new HashSet<>();
        Rule<String, String> r1 = TestUtils.createRule(p0g0Start, p1g1g0End);
        rules.add(r1);
        Rule<String, String> r2 = TestUtils.createRule(p1g1Start, p2g2g0End);
        rules.add(r2);
        rules.add(TestUtils.createRule(p2g2Start, p0g1End));
        rules.add(TestUtils.createRule(p0g1Start, p0End));

        pushAndPopPDS = TestUtils.createPDS(rules);
        initialAut = new PAutomaton<>();
        initialAut.addFinalState(TestUtils.createControlLocation("s2"));
        initialAut.addTransition(TestUtils.createTransition("s1", "s2", "g0"));
        initialAut.addTransition(TestUtils.createTransition("p0", "s1", "g0"));
        initialAut.addInitialState(p0);
        initialAut.addInitialState(p1);
        initialAut.addInitialState(p2);

        stateGeneratingRuleM1 = r1;
        stateGeneratingRuleM2 = r2;
    }

    @Test
    public void testPrestar() {
        Prestar<String, String> prestar = new Prestar<>(pushAndPopPDS, initialAut);
        //System.out.println(prestar.getSaturatedAut().toDotString());
        Set<PAutomaton.Transition<String, String>> relation = prestar.getSaturatedAut().getTransitionRelation();
        assert relation.size() == 7;
        assert relation.contains(TestUtils.createTransition("p2", "p0", "g2"));
        assert relation.contains(TestUtils.createTransition("p0", "p0", "g1"));
        assert relation.contains(TestUtils.createTransition("p0", "s1", "g0"));
        assert relation.contains(TestUtils.createTransition("p0", "s2", "g0"));
        assert relation.contains(TestUtils.createTransition("p1", "s1", "g1"));
        assert relation.contains(TestUtils.createTransition("p1", "s2", "g1"));
        assert relation.contains(TestUtils.createTransition("s1", "s2", "g0"));
    }

    @Test
    public void testPoststar() {
        Map<Rule<String, String>, Integer> generatedStateIndexMap = new HashMap<>();
        Poststar<String, String> poststar = new Poststar<>(
                pushAndPopPDS,
                initialAut,
                rule -> {
                    int index = generatedStateIndexMap.computeIfAbsent(rule, r -> generatedStateIndexMap.size() + 1);
                    return "m" + index;
                } // silly but effective way to get a simple unique identifier for generated states
        );
        //System.out.println(poststar.getSaturatedAut().toDotString());
        Set<PAutomaton.Transition<String, String>> relation = poststar.getSaturatedAut().getTransitionRelation();
        Poststar<String, String>.GeneratedState m1 = poststar.createGeneratedStateFromRule(stateGeneratingRuleM1);
        Poststar<String, String>.GeneratedState m2 = poststar.createGeneratedStateFromRule(stateGeneratingRuleM2);
        assert relation.size() == 9;
        assert relation.contains(TestUtils.createTransition("s1", "s2", "g0"));
        assert relation.contains(TestUtils.createTransition("p0", "s1", "g0"));
        assert relation.contains(TestUtils.createTransition("p0", m1, "g0"));
        assert relation.contains(TestUtils.createTransition(m1, "s1", "g0"));
        assert relation.contains(TestUtils.createTransition("p1", m1, "g1"));
        assert relation.contains(TestUtils.createTransition(m1, m1, "g0"));
        assert relation.contains(TestUtils.createTransition(m2, m1, "g0"));
        assert relation.contains(TestUtils.createTransition("p2", m2, "g2"));
        assert relation.contains(TestUtils.createTransition("p0", m2, "g1"));
    }
}
