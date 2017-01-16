package anaphora.domain;

import edu.stanford.nlp.trees.Tree;

import java.util.List;

public class AnaphorInfo {
    private Tree anaphor;
    private Tree anaphorSentence;

    private List<Tree> candidateSentences;
    private List<Tree> candidates;

    public AnaphorInfo(Tree anaphor, List<Tree> candidateSentences, List<Tree> candidates) {
        this.anaphor = anaphor;
        this.candidateSentences = candidateSentences;
        this.candidates = candidates;
    }

    public AnaphorInfo(Tree anaphor, Tree anaphorSentence, List<Tree> candidateSentences, List<Tree> candidates) {
        this.anaphor = anaphor;
        this.anaphorSentence = anaphorSentence;
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

    public Tree getAnaphorSentence() {
        return anaphorSentence;
    }

    public void setAnaphorSentence(Tree anaphorSentence) {
        this.anaphorSentence = anaphorSentence;
    }
}
