package ds.simplepds;

import ds.simplepds.automata.InvalidInstanceException;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PDSTests {

    private static PushdownSystem<Integer, String> pushAndPopPDS;
    private static StartConfiguration<Integer, String> poststarInitial;
    private static EndConfiguration<Integer, String> prestarInitial;
    private static StartConfiguration<Integer, String> invalidPoststarInitial;
    private static EndConfiguration<Integer, String> invalidPrestarInitial;
    private static PAutomaton<Integer, String> initialAut;

    @BeforeEach
    public void build() {
        ControlLocation<Integer> cl1 = TestUtils.createControlLocation(1);
        ControlLocation<Integer> cl2 = TestUtils.createControlLocation(2);
        StackSymbol<String> ssa = TestUtils.createStackSymbol("a");
        StackSymbol<String> ssb = TestUtils.createStackSymbol("b");
        StackSymbol<String> ssc = TestUtils.createStackSymbol("c");
        StackSymbol<String> ssd = TestUtils.createStackSymbol("d");

        StartConfiguration<Integer, String> paStart = TestUtils.createStartConfiguration(cl1, ssa);
        StartConfiguration<Integer, String> qbStart = TestUtils.createStartConfiguration(cl2, ssb);
        StartConfiguration<Integer, String> pcStart = TestUtils.createStartConfiguration(cl1, ssc);
        StartConfiguration<Integer, String> pdStart = TestUtils.createStartConfiguration(cl1, ssd);

        EndConfiguration<Integer, String> qbEnd = TestUtils.createNormalEndConfiguration(cl2, ssb);
        EndConfiguration<Integer, String> pcEnd = TestUtils.createNormalEndConfiguration(cl1, ssc);
        EndConfiguration<Integer, String> pdEnd = TestUtils.createNormalEndConfiguration(cl1, ssd);
        EndConfiguration<Integer, String> padEnd = TestUtils.createPushEndConfiguration(cl1, ssa, ssd);
        EndConfiguration<Integer, String> pEnd = TestUtils.createPopEndConfiguration(cl1);

        Collection<Rule<Integer, String>> rules = new HashSet<>();
        rules.add(TestUtils.createRule(paStart, qbEnd));
        rules.add(TestUtils.createRule(paStart, pcEnd));
        rules.add(TestUtils.createRule(qbStart, pdEnd));
        rules.add(TestUtils.createRule(pcStart, padEnd));
        rules.add(TestUtils.createRule(pdStart, pEnd));

        pushAndPopPDS = TestUtils.createPDS(rules);
        poststarInitial = pcStart;
        prestarInitial = qbEnd;
        invalidPoststarInitial = TestUtils.createStartConfiguration(cl2, ssd);
        invalidPrestarInitial = TestUtils.createNormalEndConfiguration(cl2, ssd);

        // TODO: initialize the arbitrary starting automaton

    }

    @Test
    public void prestarException() {
        try {
            Prestar<Integer, String> prestar = new Prestar<>(
                    invalidPrestarInitial,
                    pushAndPopPDS,
                    0,
                    "eps"
            );
            assert false; // Should throw exception before this line
        } catch (InvalidInstanceException ignored) {}
    }

    @Test
    public void poststarException() {
        try {
            Poststar<Integer, String> poststar = new Poststar<>(
                    invalidPoststarInitial,
                    pushAndPopPDS,
                    0,
                    "eps"
            );
            assert false; // Should throw exception before this line
        } catch (InvalidInstanceException ignored) {}
    }

    @Test
    public void prestarTest() {
        try {
            Prestar<Integer, String> prestar = new Prestar<>(
                    prestarInitial,
                    pushAndPopPDS,
                    0,
                    "eps"
            );
            System.out.println(prestar.getSaturatedAut().toDotString());
            Set<PAutomaton.Transition<Integer,String>> relation = prestar.getSaturatedAut().getTransitionRelation();
            assert relation.contains(
                    TestUtils.createTransition(2, 0, "b")
            );
            assert relation.contains(
                    TestUtils.createTransition(1, 0, "c")
            );
            assert relation.contains(
                    TestUtils.createTransition(1, 0, "a")
            );
            assert relation.contains(
                    TestUtils.createTransition(1, 1, "d")
            );
        } catch (InvalidInstanceException e) {
            assert false;
        }
    }

    @Test
    public void poststarTest() {

    }

    @Test
    public void prestarWithInitialAut() {

    }

    @Test
    public void poststarWithInitialAut() {

    }
}
