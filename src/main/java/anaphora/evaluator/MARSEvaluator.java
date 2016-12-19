package anaphora.evaluator;

import static anaphora.helper.MARSHelper.getBaseVBForm;
import static anaphora.helper.MARSHelper.getNPs;
import static anaphora.helper.MARSHelper.isComplexSentence;
import static anaphora.helper.MARSHelper.isImperative;
import static anaphora.helper.MARSHelper.isVerb;
import static anaphora.helper.MARSHelper.labelOf;
import static anaphora.helper.MARSHelper.matchedTrees;
import static anaphora.helper.MARSHelper.wordOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.trees.Tree;

public class MARSEvaluator {
    private static final int INDICATOR_WORDS_MARK = 1;
    private static final int DEFINITENESS_MARK = -1;
    private static final int GIVENESS_MARK = 1;
    private static final int COLLOCATION_PATTERN_MARK = 2;
    private static final int IMMEDIATE_REFERENCE_MARK = 2;
    private static final int NON_PREPOSITIONAL_NP_MARK = -1;

    private static Set<String> INDICATOR_WORDS = new HashSet<>(Arrays.asList(
            "discuss", "present", "illustrate", "identify", "summarize", "examine",
            "describe", "define", "show", "check", "develop", "review", "report",
            "outline", "consider", "investigate", "explore", "assess", "analyse",
            "synthesize", "study", "survey", "deal", "cover"
    ));

    private static Set<String> POSSESSIVE_WORDS = new HashSet<>(Arrays.asList(
            "my", "your", "his", "her", "its", "our", "their"
    ));

    private static Set<String> DEMONSTRATIVE_WORDS = new HashSet<>(Arrays.asList(
            "this", "that", "these", "those", "the"
    ));

    private int[] immediateReferenceScores;
    private int[] collocationalPatternScores;
    private int[] indicatorWordsScores;

    private int[] generalScores;

    public int[] evaluate(List<Tree> listSent, List<Tree> listNP, Tree anaphor, Tree anaphorTree) {
        initScores(listNP.size());

        evalDefiniteness(listNP);
        evalIndicatorWords(listSent, listNP);
        evalGiveness(listSent, listNP, anaphor);
        evalCollocationPattern(listSent, listNP, anaphor, anaphorTree);
        evalImmediateReference(listNP, anaphor, anaphorTree);
        evalReferentialDistance(listSent, listNP, anaphor, anaphorTree);
        evalNonPrepositionalNP(listSent, listNP);

        return generalScores;
    }

    private void evalIndicatorWords(List<Tree> listSent, List<Tree> listNP) {
        List<Tree> goodNPs = new ArrayList<>();
        for (Tree tree : listSent) {
            // The verb should be converted to the base form
            for (Tree vp : matchedTrees(tree, "VP < (NP [$- VBZ | $- VB | $- VBD])")) {
                String verb = getBaseVBForm(vp.firstChild().firstChild().label().value());
                if (INDICATOR_WORDS.contains(verb)) {
                    goodNPs.add(vp.getChild(1));
                }
            }
        }

        indicatorWordsScores = giveMarks(listNP, goodNPs, generalScores, INDICATOR_WORDS_MARK);
    }

    // TODO: add checking is it NN or NNS in NP
    // TODO: ignore rule if there are no definite articles, possessive or demonstrative words in paragraph
    private void evalDefiniteness(List<Tree> listNP) {
        List<Tree> badNPs = new ArrayList<>();

        for (Tree tree : listNP) {
            // Find all trees with root NP and sons NN right to DT or NN right to PRP$, leave definite
            for (Tree t : matchedTrees(tree, "NP < (NN [$-- /PRP./ | $-- DT])")) {
                String artWordLabel = labelOf(t.firstChild());
                String artWord = wordOf(t.firstChild());
                if ((artWordLabel.equals("PRP$") && POSSESSIVE_WORDS.contains(artWord.toLowerCase()))
                        || (artWordLabel.equals("DT") && DEMONSTRATIVE_WORDS.contains(artWord.toLowerCase()))) {
                    badNPs.add(t);
                }
            }
        }

        // Definite score 0, indefinite are penalized -1
        giveMarks(listNP, badNPs, generalScores, DEFINITENESS_MARK);
    }

    private void evalGiveness(List<Tree> listSent, List<Tree> listNP, Tree anaphor) {
        List<Tree> goodNPs = new ArrayList<>();

        for (Tree sentence : listSent) {
            if (!isImperative(sentence)) {
                List<Tree> sentenceNPs = getNPs(sentence, anaphor);

                // Get first NP from sentence
                if (!sentenceNPs.isEmpty()) {
                    goodNPs.add(sentenceNPs.get(0));
                }
            }
        }

        giveMarks(listNP, goodNPs, generalScores, GIVENESS_MARK);
    }

    // TODO: logic seems to be incorrect
    private void evalCollocationPattern(List<Tree> listSent, List<Tree> listNP, Tree anaphor, Tree parent) {
        List<Tree> goodNPs = new ArrayList<>();

        // One level higher
        Tree[] parents = anaphor.parent(parent).parent(parent).children();
        if (parents.length >= 2) {
            for (int i = 0; i < parents.length - 1; i++) {
                if (labelOf(parents[i]).equals("VB") && labelOf(parents[i + 1]).equals("NP")) {
                    for (Tree sent : listSent) {
                        goodNPs.addAll(matchedTrees(sent, "NP $-- VB"));
                    }
                } else if (labelOf(parents[i]).equals("NP") && labelOf(parents[i + 1]).equals("VP")
                        && isVerb(parents[i + 1])) {
                    for (Tree sent : listSent) {
                        goodNPs.addAll(matchedTrees(sent, "NP $++ (VP < /VB./)"));
                    }
                }
            }
        } else {
            return;
        }

        collocationalPatternScores = giveMarks(listNP, goodNPs, generalScores, COLLOCATION_PATTERN_MARK);
    }

    protected void evalImmediateReference(List<Tree> listNP, Tree anaphor, Tree parent) {
        String name = "anaphor";
        String anaphoraVP = String.format("(VP < (VB [$. (NP < PRP=%s) | $. (S < (NP < PRP=%s))]))", name, name);
        String pattern = String.format("NP !< PRP > (VP [$. (CC $. %s) | $. (/,/ $. %s)])", anaphoraVP, anaphoraVP);
        List<Tree> patternMatches = matchedTrees(parent, pattern, name, anaphor);

        if (patternMatches.isEmpty()) return;

        immediateReferenceScores = giveMarks(listNP, patternMatches, generalScores, IMMEDIATE_REFERENCE_MARK);
    }

    private void evalReferentialDistance(List<Tree> listSent, List<Tree> listNP, Tree anaphor, Tree parent) {
        List<Tree> goodNPs = new ArrayList<>();

        if (isComplexSentence(parent)) {
            List<Tree> trees = matchedTrees(parent, "NP $+ VP");

            // NPs before anaphor, that don't contain PRP
            for (Tree t : trees) {
                if (t.equals(anaphor)) break;
                if (matchedTrees(t, "PRP").isEmpty()) {
                    goodNPs.add(t);
                }
            }
        }

        int est = 1;
        if (listSent.size() > 1) {
            for (int i = listSent.size() - 2; i >= 0; i--) {
                for (int j = 0; j < listNP.size(); j++) {
                    if (matchedTrees(listSent.get(i), "NP !< PRP").contains(listNP.get(j))) {
                        generalScores[j] += est;
                    }
                    if (!goodNPs.isEmpty() && goodNPs.contains(listNP.get(j))) {
                        generalScores[j] += 2;
                    }
                }
                est--;
            }
        }
    }

    private void evalNonPrepositionalNP(List<Tree> listSent, List<Tree> listNP) {
        List<Tree> badNPs = new ArrayList<>();

        for (Tree sentence : listSent) {
            badNPs.addAll(matchedTrees(sentence, "NP [>> PP & [!< PRP & !< /PRP./]]"));
        }

        immediateReferenceScores = giveMarks(listNP, badNPs, generalScores, NON_PREPOSITIONAL_NP_MARK);
    }

    protected void initScores(int size) {
        generalScores = new int[size];
        immediateReferenceScores = new int[size];
        collocationalPatternScores = new int[size];
        indicatorWordsScores = new int[size];
    }

    private int[] giveMarks(List<Tree> allNPs, List<Tree> markedNPs, int[] generalScores, int mark) {
        int[] currentMarks = new int[generalScores.length];

        if (!markedNPs.isEmpty()) {
            for (int i = 0; i < allNPs.size(); i++) {
                if (markedNPs.contains(allNPs.get(i))) {
                    generalScores[i] += mark;
                    currentMarks[i] += mark;
                }
            }
        }

        return currentMarks;
    }

    public int[] getGeneralScores() {
        return generalScores;
    }
}