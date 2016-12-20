package anaphora.evaluator;

import edu.stanford.nlp.trees.Tree;

import java.util.List;
import java.util.Set;

public abstract class BasicEvaluator implements Evaluator {
    protected static final String STANDARD_MATCH_NAME = "name";

    protected int[] lastScores;

    protected int[] giveScores(List<Tree> candidates, Set<Tree> marked, int mark) {
        lastScores = new int[candidates.size()];

        if (!marked.isEmpty()) {
            for (int i = 0; i < candidates.size(); i++) {
                if (marked.contains(candidates.get(i))) {
                    lastScores[i] += mark;
                }
            }
        }

        return lastScores;
    }

    public int[] getLastScores() {
        return lastScores;
    }
}
