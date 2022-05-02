package ds.simplepds.interfaces;

public interface StartConfiguration<L,S> extends Configuration<L,S> {
    StackSymbol<S> getStackSymbol();
}
