package anaphora.helper;

import static anaphora.helper.MARSHelper.isYouPRP;
import static anaphora.helper.MARSHelper.matchedTrees;
import static anaphora.helper.MARSHelper.wordOf;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import anaphora.domain.AnaphorInfo;
import anaphora.resolver.MARSResolver;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

/**
 * Created by Mykhailo Yashchuk on 24.01.2017.
 */
public class AnaphorIterator implements Iterator<AnaphorInfo> {
    private static final String LOWEST_NP_PATTERN = "NP [!<< NP]";
    private static final String CANDIDATES_PATTERN = "NP [!<< NP & !<< PRP]";

    private Iterator<List<HasWord>> documentPreprocessor;
    private LexicalizedParser parser;

    private List<Tree> currentSentenceNPs;
    private Queue<Tree> previousSentencesTrees;

    private Queue<AnaphorInfo> anaphorInfos;

    public AnaphorIterator(Iterator<List<HasWord>> documentPreprocessor) {
        this.documentPreprocessor = documentPreprocessor;
        this.parser = MARSResolver.PARSER;

        this.previousSentencesTrees = new ArrayDeque<>();
        this.anaphorInfos = new ArrayDeque<>();
        this.currentSentenceNPs = new ArrayList<>();
    }

    @Override
    public boolean hasNext() {
        if (!anaphorInfos.isEmpty()) {
            return true;
        } else {
            prepare();
            return !anaphorInfos.isEmpty();
        }
    }

    @Override
    public AnaphorInfo next() {
        prepare();  // For next() without hasNext() to work
        return anaphorInfos.remove();
    }

    private void prepare() {
        while (anaphorInfos.isEmpty()) {
            if (documentPreprocessor.hasNext()) {
                if (previousSentencesTrees.size() > 3) {
                    previousSentencesTrees.poll();
                }
                Tree currentSentenceTree = parser.parse(documentPreprocessor.next());
                currentSentenceNPs.clear();

                List<Tree> lowestNPs = matchedTrees(currentSentenceTree, LOWEST_NP_PATTERN);
                for (Tree lowestNP : lowestNPs) {
                    if (MARSHelper.isAnaphorPRP(lowestNP.firstChild())) {
                        List<Tree> candidateSentences = new ArrayList<>(previousSentencesTrees);
                        candidateSentences.add(currentSentenceTree);

                        List<Tree> candidates = new ArrayList<>();
                        previousSentencesTrees.forEach(t -> candidates.addAll(matchedTrees(t, CANDIDATES_PATTERN)));
                        candidates.addAll(currentSentenceNPs);

                        anaphorInfos.offer(new AnaphorInfo(lowestNP.firstChild(), currentSentenceTree,
                                candidateSentences, candidates));
                    } else {
                        currentSentenceNPs.add(lowestNP);
                    }
                }

                previousSentencesTrees.add(currentSentenceTree);
            } else {
                break;
            }
        }

        filterInfo();
    }

    private void filterInfo() {
        for (AnaphorInfo anaphorInfo : anaphorInfos) {
            anaphorInfo.getCandidates().removeIf(t -> isYouPRP(t));
        }
    }
}
