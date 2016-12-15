package anaphora.evaluator;

import static anaphora.helper.MARSHelper.getBaseVBForm;
import static anaphora.helper.MARSHelper.getNPs;
import static anaphora.helper.MARSHelper.matchedTrees;
import static anaphora.helper.MARSHelper.isComplexSentence;
import static anaphora.helper.MARSHelper.isImperative;
import static anaphora.helper.MARSHelper.isVerb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.trees.Tree;

public class MARSEvaluator {
    public static int[] evaluate(List<Tree> listSent, List<Tree> listNP, Tree anaphor, Tree anaphorTree) {
        int[] scores = new int[listNP.size()];

        scores = evalDefiniteness(listNP, anaphor, scores);
        scores = evalIndicatorWords(listSent, listNP, anaphor, scores);
        scores = evalGiveness(listSent, listNP, anaphor, scores);
        scores = evalCollocationPattern(listSent, listNP, anaphor, anaphorTree, scores);
        scores = evalImmediateReference(listNP, anaphor, anaphorTree, scores);
        scores = evalReferentialDistance(listSent, listNP, anaphor, anaphorTree, scores);
        scores = evalNonPrepositionalNP(listSent, listNP, anaphor, scores);

        return scores;
    }

    public static int[] evalIndicatorWords(List<Tree> listSent, List<Tree> listNP, Tree anaphor, int[] scores) {
        Set<String> verbSet = new HashSet<String>(Arrays.asList(
                "discuss", "present", "illustrate", "identify", "summarize", "examine",
                "describe", "define", "show", "check", "develop", "review", "report",
                "outline", "consider", "investigate", "explore", "assess", "analyse",
                "synthesize", "study", "survey", "deal", "cover"));

        List<Tree> goodNPs = new ArrayList<Tree>();
        for (Tree tree : listSent) {
            // The verb should be converted to the base form
            for (Tree vp : matchedTrees(tree, "VP < (NP [$- VBZ | $- VB | $- VBD])")) {
                // TODO: may return some trash
                String verb = getBaseVBForm(vp.firstChild().firstChild().label().value());
                if (verbSet.contains(verb)) {
                    goodNPs.add(vp.getChild(1));
                }
            }
        }

        if (goodNPs.size() != 0) {
            for (int i = 0; i < listNP.size(); i++) {
                if (goodNPs.contains(listNP.get(i))) {
                    scores[i]++;
                }
            }
        }

        return scores;
    }

    // TODO: add checking is it NN or NNS in NP
    public static int[] evalDefiniteness(List<Tree> listNP, Tree anaphor, int[] scores) {
        List<Tree> goodNPs = new ArrayList<Tree>();
        Set<String> possessives = new HashSet<String>(Arrays.asList(
                "my", "your", "his", "her", "its", "our", "their"));
        // Demonstrative words + "the"
        Set<String> demonstratives = new HashSet<String>(Arrays.asList(
                "this", "that", "these", "those", "the"));

        for (Tree tree : listNP) {
            // Find all trees with root NP and sons NN right to DT or NN right to PRP$, leave definite
            for (Tree t : matchedTrees(tree, "NP < (NN [$-- /PRP./ | $-- DT])")) {
                String artWordLabel = t.firstChild().label().value();
                String artWord = t.firstChild().firstChild().label().value();
                if ((artWordLabel.equals("PRP$")
                        && possessives.contains(artWord.toLowerCase()))
                        || (artWordLabel.equals("DT")
                        && demonstratives.contains(artWord.toLowerCase()))) {
                    goodNPs.add(t);
                }
            }
        }

        // Definite score 0, indefinite are penalized -1
        if (goodNPs.size() != 0) {
            for (int i = 0; i < listNP.size(); i++) {
                if (!goodNPs.contains(listNP.get(i))) {
                    scores[i]--;
                }
            }
        }

        return scores;
    }

    public static int[] evalGiveness(List<Tree> listSent, List<Tree> listNP, Tree anaphor, int[] scores) {
        List<Tree> goodNPs = new ArrayList<Tree>();

        for (Tree sentence : listSent) {
            if (!isImperative(sentence)) {
                List<Tree> sentenceNPs = getNPs(sentence, anaphor);

                // Get first NP from sentence
                if (sentenceNPs.size() != 0) {
                    goodNPs.add(sentenceNPs.get(0));
                }
            }
        }

        if (goodNPs.size() != 0) {
            for (int i = 0; i < listNP.size(); i++) {
                if (goodNPs.contains(listNP.get(i))) {
                    scores[i]++;
                }
            }
        }

        return scores;
    }

    public static int[] evalCollocationPattern(List<Tree> listSent, List<Tree> listNP, Tree anaphor, Tree parent, int[] scores) {
        List<Tree> goodNPs = new ArrayList<Tree>();

        Tree[] parents = anaphor.parent(parent).parent(parent).children();
        if (parents.length >= 2) {
            for (int i = 0; i < parents.length - 1; i++) {
                if (parents[i].value().equals("VB") && parents[i + 1].value().equals("NP")) {
                    for (Tree sent : listSent) {
                        goodNPs.addAll(matchedTrees(sent, "NP $-- VB"));
                    }
                } else if (parents[i].value().equals("NP")
                        && parents[i + 1].value().equals("VP") && isVerb(parents[i + 1])) {
                    for (Tree sent : listSent) {
                        goodNPs.addAll(matchedTrees(sent, "NP $++ (VP < /VB./)"));
                    }
                }
            }
        } else {
            return scores;
        }

        if (goodNPs.size() != 0) {
            for (int i = 0; i < listNP.size(); i++) {
                if (goodNPs.contains(listNP.get(i))) {
                    scores[i] += 2;
                }
            }
        }

        return scores;
    }

    public static int[] evalImmediateReference(List<Tree> listNP, Tree anaphor, Tree parent, int[] scores) {
        List<Tree> goodNPs = new ArrayList<Tree>();

        List<Tree> patternMatches = matchedTrees(parent, "VP < ((VP < (VB .. (NP !< PRP))) "
                + "[$+ (CC $+ (VP < (VB .. (NP < PRP)))) | $+ (/,/ $+ (VP < (VB .. (NP < PRP))))])");
        if (patternMatches.size() != 0) {
            Tree[] children;
            for (Tree tree : patternMatches) {
                children = tree.children();
                for (int i = 0; i < children.length - 2; i++) {
                    Tree par = anaphor.parent(parent).parent(parent);
                    if (children[i].value().equals("VP")
                            && children[i].getChild(1).value().equals("NP")
                            && !children[i].getChild(1).firstChild().value().equals("PRP")
                            && (children[i + 1].value().equals("CC") || children[i + 1].value().equals(","))
                            && children[i + 2].equals(par)) {
                        goodNPs.add(children[i].getChild(1));
                    } else if (i + 4 < children.length
                            && children[i].value().equals("VP")
                            && children[i].getChild(1).value().equals("NP")
                            && !children[i].getChild(1).firstChild().value().equals("PRP")
                            && (children[i + 1].value().equals("CC") || children[i + 1].value().equals(","))
                            && !children[i + 2].equals(par)
                            && children[i + 4].equals(par)) {
                        goodNPs.add(children[i].getChild(1));
                    }
                }

            }
        } else {
            return scores;
        }

        if (goodNPs.size() != 0) {
            for (int i = 0; i < listNP.size(); i++) {
                if (goodNPs.contains(listNP.get(i))) {
                    scores[i] += 2;
                }
            }
        }

        return scores;
    }

    public static int[] evalReferentialDistance(List<Tree> listSent, List<Tree> listNP, Tree anaphor, Tree parent, int[] scores) {
        List<Tree> goodNPs = new ArrayList<Tree>();

        if (isComplexSentence(parent)) {
            List<Tree> trees = matchedTrees(parent, "NP $+ VP");

            // NPs before anaphor, that don't contain PRP
            for (Tree t : trees) {
                if (!t.equals(anaphor)) {
                    if (matchedTrees(t, "PRP").isEmpty()) {
                        goodNPs.add(t);
                    }
                } else {
                    break;
                }
            }
        }

        int est = 1;
        if (listSent.size() > 1) {
            for (int i = listSent.size() - 2; i >= 0; i--) {
                for (int j = 0; j < listNP.size(); j++) {
                    if (matchedTrees(listSent.get(i), "NP !< PRP").contains(listNP.get(j))) {
                        scores[j] += est;
                    }
                    if (goodNPs.size() != 0 && goodNPs.contains(listNP.get(j))) {
                        scores[j] += 2;
                    }
                }
                est--;
            }
        }

        return scores;
    }

    public static int[] evalNonPrepositionalNP(List<Tree> listSent, List<Tree> listNP, Tree anaphor, int[] scores) {
        List<Tree> badNPs = new ArrayList<Tree>();

        for (Tree sentence : listSent) {
            badNPs.addAll(matchedTrees(sentence, "NP [>> PP & [!< PRP & !< /PRP./]]"));
        }

        if (badNPs.size() != 0) {
            for (int i = 0; i < listNP.size(); i++) {
                if (badNPs.contains(listNP.get(i))) {
                    scores[i]--;
                }
            }
        }

        return scores;
    }
}