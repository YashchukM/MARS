package anaphora.domain;

import edu.stanford.nlp.trees.Tree;

import java.util.List;

public class AnaphorContext {
    private Tree anaphor;
    private List<Tree> candidateSentences;
    private List<Tree> candidates;

    public AnaphorContext(Tree anaphor, List<Tree> candidateSentences, List<Tree> candidates) {
        this.anaphor = anaphor;
        this.candidateSentences = candidateSentences;
        this.candidates = candidates;
    }

    public Tree getAnaphor() {
        return anaphor;
    }

    public void setAnaphor(Tree anaphor) {
        this.anaphor = anaphor;
    }

    public List<Tree> getCandidateSentences() {
        return candidateSentences;
    }

    public void setCandidateSentences(List<Tree> candidateSentences) {
        this.candidateSentences = candidateSentences;
    }

    public List<Tree> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Tree> candidates) {
        this.candidates = candidates;
    }
}
