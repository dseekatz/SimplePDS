package ds.simplepds;

import ds.simplepds.automata.PAutomaton;
import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.EndConfiguration;
import ds.simplepds.interfaces.PushdownSystem;
import ds.simplepds.interfaces.Rule;
import ds.simplepds.interfaces.StackSymbol;
import ds.simplepds.interfaces.StartConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class TestUtils {

    public static ControlLocation<Integer> createControlLocation(int number) {
        return new TestControlLocation(number);
    }

    public static StackSymbol<String> createStackSymbol(String s) {
        return new TestStackSymbol(s);
    }

    public static StartConfiguration<Integer, String> createStartConfiguration(
            ControlLocation<Integer> cl,
            StackSymbol<String> ss
    ) {
        return new StartConfiguration<>() {
            @Override
            public StackSymbol<String> getStackSymbol() {
                return ss;
            }

            @Override
            public ControlLocation<Integer> getControlLocation() {
                return cl;
            }
        };
    }

    public static EndConfiguration<Integer, String> createNormalEndConfiguration(
            ControlLocation<Integer> cl,
            StackSymbol<String> ss
    ) {
        return new EndConfiguration<>() {
            private final List<StackSymbol<String>> word = new LinkedList<>();
            {
                word.add(ss);
            }

            @Override
            public List<StackSymbol<String>> getWord() {
                return word;
            }

            @Override
            public ControlLocation<Integer> getControlLocation() {
                return cl;
            }
        };
    }

    public static EndConfiguration<Integer, String> createPopEndConfiguration(ControlLocation<Integer> cl) {
        return new EndConfiguration<>() {

            @Override
            public List<StackSymbol<String>> getWord() {
                return Collections.emptyList();
            }

            @Override
            public ControlLocation<Integer> getControlLocation() {
                return cl;
            }
        };
    }

    public static EndConfiguration<Integer, String> createPushEndConfiguration(
            ControlLocation<Integer> cl,
            StackSymbol<String> ss1,
            StackSymbol<String> ss2
    ) {
        return new EndConfiguration<>() {
            private final List<StackSymbol<String>> word = new LinkedList<>();
            {
                word.add(ss1);
                word.add(ss2);
            }

            @Override
            public List<StackSymbol<String>> getWord() {
                return word;
            }

            @Override
            public ControlLocation<Integer> getControlLocation() {
                return cl;
            }
        };
    }

    public static Rule<Integer, String> createRule(
            StartConfiguration<Integer, String> startConfiguration,
            EndConfiguration<Integer, String> endConfiguration
    ) {
        return new Rule<>() {
            @Override
            public StartConfiguration<Integer, String> getStartConfiguration() {
                return startConfiguration;
            }

            @Override
            public EndConfiguration<Integer, String> getEndConfiguration() {
                return endConfiguration;
            }
        };
    }

    public static PushdownSystem<Integer, String> createPDS(Collection<Rule<Integer, String>> rules) {
        return () -> rules;
    }

    public static PAutomaton.Transition<Integer, String> createTransition(Integer start, Integer end, String label) {
        return new PAutomaton.Transition<>(
                createControlLocation(start),
                createControlLocation(end),
                createStackSymbol(label)
        );
    }

    public static class TestControlLocation implements ControlLocation<Integer> {

        private final Integer value;

        private TestControlLocation(int number) {
            this.value = number;
        }

        @Override
        public Integer unwrap() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestControlLocation that = (TestControlLocation) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    public static class TestStackSymbol implements StackSymbol<String> {

        private final String s;

        private TestStackSymbol(String s) {
            this.s = s;
        }

        @Override
        public String unwrap() {
            return s;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestStackSymbol that = (TestStackSymbol) o;
            return Objects.equals(s, that.s);
        }

        @Override
        public int hashCode() {
            return Objects.hash(s);
        }
    }
}
