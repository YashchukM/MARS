import java.io.IOException;

import anaphora.finder.MARSResolver;


public class Manager {
    private static final String DEFAULT_INPUT_PATH = "input.txt";

    public static void main(String[] args) throws IOException {
        MARSResolver.resolve(DEFAULT_INPUT_PATH);
    }
}
