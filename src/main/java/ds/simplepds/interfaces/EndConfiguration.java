package ds.simplepds.interfaces;

import java.util.List;

public interface EndConfiguration<L,S> extends Configuration<L,S> {
    List<StackSymbol<S>> getWord();
}
