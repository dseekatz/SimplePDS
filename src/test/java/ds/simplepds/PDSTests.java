package ds.simplepds;

import ds.simplepds.automata.FastLookupRuleMap;
import ds.simplepds.automata.HashBasedPostStar;
import ds.simplepds.automata.HashBasedPreStar;
import ds.simplepds.automata.PAutomaton;
import ds.simplepds.automata.Poststar;
import ds.simplepds.automata.Prestar;
import ds.simplepds.automata.demand.BackwardFlowFunctions;
import ds.simplepds.automata.demand.DemandPostStar;
import ds.simplepds.automata.demand.DemandPreStar;
import ds.simplepds.automata.demand.ForwardFlowFunctions;
import ds.simplepds.automata.demand.WildcardPostStar;
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
import java.util.stream.Collectors;

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

        EndConfiguration<String, String> p1g1g0End = TestUtils.createPushEndConfiguration(p1, g0, g1);
        EndConfiguration<String, String> p2g2g0End = TestUtils.createPushEndConfiguration(p2, g0, g2);
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
        prestar.apply();
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
    public void testHashBasedPrestar() {
        FastLookupRuleMap<String, String> fastLookupRuleMap = new FastLookupRuleMap<>(pushAndPopPDS);
        Prestar<String, String> prestar = new HashBasedPreStar<>(pushAndPopPDS, initialAut, fastLookupRuleMap);
        prestar.apply();
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
    public void testDemandPrestar() {
        BackwardFlowFunctions<String,String> flowFunctions =
                currentLocation -> pushAndPopPDS.getRules().stream()
                    .filter(rule ->
                            rule.getEndConfiguration().getControlLocation().unwrap().equals(currentLocation))
                    .collect(Collectors.toSet());
        DemandPreStar<String, String> prestar = new DemandPreStar<>(flowFunctions, initialAut);
        prestar.apply();
        System.out.println(prestar.getSaturatedAut().toDotString());
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
        FastLookupRuleMap<String, String> fastLookupRuleMap = new FastLookupRuleMap<>(pushAndPopPDS);
        Poststar<String, String> poststar = new HashBasedPostStar<>(
                pushAndPopPDS,
                initialAut,
                rule -> {
                    int index = generatedStateIndexMap.computeIfAbsent(rule, r -> generatedStateIndexMap.size() + 1);
                    return "m" + index;
                }, // silly but effective way to get a simple unique identifier for generated states
                fastLookupRuleMap
        );
        poststar.apply();
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

    @Test
    public void testHashBasedPoststar() {
        Map<Rule<String, String>, Integer> generatedStateIndexMap = new HashMap<>();

        Poststar<String, String> poststar = new Poststar<>(
                pushAndPopPDS,
                initialAut,
                rule -> {
                    int index = generatedStateIndexMap.computeIfAbsent(rule, r -> generatedStateIndexMap.size() + 1);
                    return "m" + index;
                } // silly but effective way to get a simple unique identifier for generated states
        );
        poststar.apply();
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

    @Test
    public void testDemandPoststar() {
        Map<Rule<String, String>, Integer> generatedStateIndexMap = new HashMap<>();
        ForwardFlowFunctions<String, String> flowFunctions =
                currentLocation -> pushAndPopPDS.getRules().stream()
                    .filter(rule ->
                            rule.getStartConfiguration().getControlLocation().unwrap().equals(currentLocation))
                    .collect(Collectors.toSet());

        DemandPostStar<String, String> poststar = new DemandPostStar<>(
                flowFunctions,
                initialAut,
                rule -> {
                    int index = generatedStateIndexMap.computeIfAbsent(rule, r -> generatedStateIndexMap.size() + 1);
                    return "m" + index;
                } // silly but effective way to get a simple unique identifier for generated states
        );
        poststar.apply();
        System.out.println(poststar.getSaturatedAut().toDotString());
        Set<PAutomaton.Transition<String, String>> relation = poststar.getSaturatedAut().getTransitionRelation();
        DemandPostStar<String, String>.GeneratedState m1 = poststar.createGeneratedStateFromRule(stateGeneratingRuleM1);
        DemandPostStar<String, String>.GeneratedState m2 = poststar.createGeneratedStateFromRule(stateGeneratingRuleM2);
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

    @Test
    public void testDemandPoststarSimplePop() {
        ControlLocation<String> f = TestUtils.createControlLocation("f");
        ControlLocation<String> push1 = TestUtils.createControlLocation("push1");
        ControlLocation<String> push2 = TestUtils.createControlLocation("push2");
        ControlLocation<String> pop1 = TestUtils.createControlLocation("pop1");
        ControlLocation<String> pop2 = TestUtils.createControlLocation("pop2");
        StackSymbol<String> c1 = TestUtils.createStackSymbol("c1");
        StackSymbol<String> c2 = TestUtils.createStackSymbol("c2");
        StackSymbol<String> w = TestUtils.createStackSymbol("w");

        StartConfiguration<String, String> push1Start = TestUtils.createStartConfiguration(f, w);
        StartConfiguration<String, String> push2Start = TestUtils.createStartConfiguration(push1, c1);
        StartConfiguration<String, String> pop1Start = TestUtils.createStartConfiguration(push2, c2);
        StartConfiguration<String, String> pop2Start = TestUtils.createStartConfiguration(pop1, c1);

        EndConfiguration<String, String> push1End = TestUtils.createPushEndConfiguration(push1, c1, w);
        EndConfiguration<String, String> push2End = TestUtils.createPushEndConfiguration(push2, c2, c1);
        EndConfiguration<String, String> pop1End = TestUtils.createPopEndConfiguration(pop1);
        EndConfiguration<String, String> pop2End = TestUtils.createPopEndConfiguration(pop2);

        Set<Rule<String, String>> rules = new HashSet<>();
        rules.add(TestUtils.createRule(push1Start, push1End));
        rules.add(TestUtils.createRule(push2Start, push2End));
        rules.add(TestUtils.createRule(pop1Start, pop1End));
        rules.add(TestUtils.createRule(pop2Start, pop2End));

        PushdownSystem<String, String> pds = TestUtils.createPDS(rules);

        PAutomaton<String, String> aut = new PAutomaton<>();
        ControlLocation<String> s = TestUtils.createControlLocation("s");
        aut.addFinalState(s);
        aut.addTransition(TestUtils.createTransition(f, s, "w"));
        aut.addInitialState(f);

        Map<Rule<String, String>, Integer> generatedStateIndexMap = new HashMap<>();
        ForwardFlowFunctions<String, String> flowFunctions =
                currentLocation -> pds.getRules().stream()
                        .filter(rule ->
                                rule.getStartConfiguration().getControlLocation().unwrap().equals(currentLocation))
                        .collect(Collectors.toSet());

        DemandPostStar<String, String> poststar = new DemandPostStar<>(
                flowFunctions,
                aut,
                rule -> {
                    int index = generatedStateIndexMap.computeIfAbsent(rule, r -> generatedStateIndexMap.size() + 1);
                    return "m" + index;
                } // silly but effective way to get a simple unique identifier for generated states
        );
        poststar.apply();
        System.out.println(poststar.getSaturatedAut().toDotString());
    }

    @Test
    public void testWildcardPoststarSimplePop() {
        ControlLocation<String> f = TestUtils.createControlLocation("f");
        ControlLocation<String> push1 = TestUtils.createControlLocation("push1");
        ControlLocation<String> push2 = TestUtils.createControlLocation("push2");
        ControlLocation<String> pop1 = TestUtils.createControlLocation("pop1");
        ControlLocation<String> pop2 = TestUtils.createControlLocation("pop2");
        ControlLocation<String> normal = TestUtils.createControlLocation("normal");
        StackSymbol<String> c1 = TestUtils.createStackSymbol("c1");
        StackSymbol<String> c2 = TestUtils.createStackSymbol("c2");
        StackSymbol<String> w = TestUtils.getWildcardStackSymbol();

        StartConfiguration<String, String> push1Start = TestUtils.createStartConfiguration(f, w);
        StartConfiguration<String, String> push2Start = TestUtils.createStartConfiguration(push1, c1);
        StartConfiguration<String, String> pop1Start = TestUtils.createStartConfiguration(normal, c2);
        StartConfiguration<String, String> pop2Start = TestUtils.createStartConfiguration(pop1, c1);
        StartConfiguration<String, String> normalStart = TestUtils.createStartConfiguration(push2, w);

        EndConfiguration<String, String> push1End = TestUtils.createPushEndConfiguration(push1, c1, w);
        EndConfiguration<String, String> push2End = TestUtils.createPushEndConfiguration(push2, c2, c1);
        EndConfiguration<String, String> pop1End = TestUtils.createPopEndConfiguration(pop1);
        EndConfiguration<String, String> pop2End = TestUtils.createPopEndConfiguration(pop2);
        EndConfiguration<String, String> normalEnd = TestUtils.createNormalEndConfiguration(normal, w);

        Set<Rule<String, String>> rules = new HashSet<>();
        rules.add(TestUtils.createRule(push1Start, push1End));
        rules.add(TestUtils.createRule(push2Start, push2End));
        rules.add(TestUtils.createRule(pop1Start, pop1End));
        rules.add(TestUtils.createRule(pop2Start, pop2End));
        rules.add(TestUtils.createRule(normalStart, normalEnd));

        PushdownSystem<String, String> pds = TestUtils.createPDS(rules);

        PAutomaton<String, String> aut = new PAutomaton<>();
        ControlLocation<String> s = TestUtils.createControlLocation("s");
        aut.addFinalState(s);
        aut.addTransition(TestUtils.createTransition(f, s, "w"));
        aut.addInitialState(f);

        Map<Rule<String, String>, Integer> generatedStateIndexMap = new HashMap<>();
        ForwardFlowFunctions<String, String> flowFunctions =
                currentLocation -> pds.getRules().stream()
                        .filter(rule ->
                                rule.getStartConfiguration().getControlLocation().unwrap().equals(currentLocation))
                        .collect(Collectors.toSet());

        WildcardPostStar<String, String> poststar = new WildcardPostStar<>(
                flowFunctions,
                aut,
                rule -> {
                    int index = generatedStateIndexMap.computeIfAbsent(rule, r -> generatedStateIndexMap.size() + 1);
                    return "m" + index;
                } // silly but effective way to get a simple unique identifier for generated states
        );
        poststar.apply();
        System.out.println(poststar.getSaturatedAut().toDotString());
        assert poststar.getSaturatedAut().getTransitionRelation().contains(
                TestUtils.createTransition(pop2, s, w)
        );
    }
}
