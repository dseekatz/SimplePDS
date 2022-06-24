package ds.simplepds;

import ds.simplepds.automata.PAutomaton;
import ds.simplepds.automata.Poststar;
import ds.simplepds.automata.demand.Wildcard;
import ds.simplepds.interfaces.ControlLocation;
import ds.simplepds.interfaces.EndConfiguration;
import ds.simplepds.interfaces.PushdownSystem;
import ds.simplepds.interfaces.Rule;
import ds.simplepds.interfaces.StackSymbol;
import ds.simplepds.interfaces.StartConfiguration;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TestUtils {

    private static final Wildcard<String> wildcard = new Wildcard<>() {
        @Override
        public String unwrap() {
            return "*";
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof StackSymbol<?>;
        }

        @Override
        public String toString() {
            return "w";
        }
    };

    public static ControlLocation<String> createControlLocation(String s) {
        return new TestControlLocation(s);
    }

    public static StackSymbol<String> createStackSymbol(String s) {
        return new TestStackSymbol(s);
    }

    public static StackSymbol<String> getWildcardStackSymbol() {
        return wildcard;
    }

    public static StartConfiguration<String, String> createStartConfiguration(
            ControlLocation<String> cl,
            StackSymbol<String> ss
    ) {
        return new StartConfiguration<>() {
            @Override
            public StackSymbol<String> getStackSymbol() {
                return ss;
            }

            @Override
            public ControlLocation<String> getControlLocation() {
                return cl;
            }
        };
    }

    public static EndConfiguration<String, String> createNormalEndConfiguration(
            ControlLocation<String> cl,
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
            public ControlLocation<String> getControlLocation() {
                return cl;
            }
        };
    }

    public static EndConfiguration<String, String> createPopEndConfiguration(ControlLocation<String> cl) {
        return new EndConfiguration<>() {

            @Override
            public List<StackSymbol<String>> getWord() {
                return Collections.emptyList();
            }

            @Override
            public ControlLocation<String> getControlLocation() {
                return cl;
            }
        };
    }

    public static EndConfiguration<String, String> createPushEndConfiguration(
            ControlLocation<String> cl,
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
            public ControlLocation<String> getControlLocation() {
                return cl;
            }
        };
    }

    public static Rule<String, String> createRule(
            StartConfiguration<String, String> startConfiguration,
            EndConfiguration<String, String> endConfiguration
    ) {
        return new Rule<>() {
            @Override
            public StartConfiguration<String, String> getStartConfiguration() {
                return startConfiguration;
            }

            @Override
            public EndConfiguration<String, String> getEndConfiguration() {
                return endConfiguration;
            }
        };
    }

    public static PushdownSystem<String, String> createPDS(Set<Rule<String, String>> rules) {
        return () -> rules;
    }

    public static PAutomaton.Transition<String, String> createTransition(String start, String end, String label) {
        return new PAutomaton.Transition<>(
                createControlLocation(start),
                createControlLocation(end),
                createStackSymbol(label)
        );
    }

    public static PAutomaton.Transition<String, String> createTransition(ControlLocation<String> start, String end, String label) {
        return new PAutomaton.Transition<>(
                start,
                createControlLocation(end),
                createStackSymbol(label)
        );
    }

    public static PAutomaton.Transition<String, String> createTransition(String start, ControlLocation<String> end, String label) {
        return new PAutomaton.Transition<>(
                createControlLocation(start),
                end,
                createStackSymbol(label)
        );
    }

    public static PAutomaton.Transition<String, String> createTransition(ControlLocation<String> start, ControlLocation<String> end, String label) {
        return new PAutomaton.Transition<>(
                start,
                end,
                createStackSymbol(label)
        );
    }

    public static Poststar<String, String>.GeneratedState createGeneratedState(
            Rule<String, String> rule,
            Poststar<String, String> instance
    ) {
        return instance.createGeneratedStateFromRule(rule);
    }

    public static PAutomaton.Transition<String, String> createTransition(
            ControlLocation<String> s1,
            ControlLocation<String> s2,
            StackSymbol<String> label
    ) {
        return new PAutomaton.Transition<>(s1, s2, label);
    }

    public static class TestControlLocation implements ControlLocation<String> {

        private final String value;

        private TestControlLocation(String s) {
            this.value = s;
        }

        @Override
        public String unwrap() {
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

        @Override
        public String toString() {
            return value;
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
            if (o instanceof Wildcard<?>) return true;
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestStackSymbol that = (TestStackSymbol) o;
            return Objects.equals(s, that.s);
        }

        @Override
        public int hashCode() {
            return Objects.hash(s);
        }


        @Override
        public String toString() {
            return s;
        }
    }
}
