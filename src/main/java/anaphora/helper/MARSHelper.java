package anaphora.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import anaphora.evaluator.MARSEvaluator;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class MARSHelper {

    public static boolean useGoogle = false;

    // Get NP from up to 3 sentences before sentence index
    public static List<Tree> getNPs(int index, List<Tree> treeList, Tree anaphor) {
        List<Tree> nounPhrasesList = new ArrayList<Tree>();

        if (index - 3 >= 0) {
            nounPhrasesList.addAll(getNPs(treeList.get(index - 3), anaphor));
        }
        if (index - 2 >= 0) {
            nounPhrasesList.addAll(getNPs(treeList.get(index - 2), anaphor));
        }
        if (index - 1 >= 0) {
            nounPhrasesList.addAll(getNPs(treeList.get(index - 1), anaphor));
        }
        if (index >= 0) {
            List<Tree> trees = matchedTrees(treeList.get(index), "NP !<< NP");
            for (Tree t : trees) {
                if (!t.firstChild().label().equals("PRP")) {
                    if (!t.firstChild().equals(anaphor)) {
                        nounPhrasesList.add(t);
                    } else {
                        break;
                    }
                }
            }

            //nounPhrasesList.addAll(getNPs(treeList.get(index), anaphor));
        }

        // TODO: add search in this sentence, before the anaphora
        return nounPhrasesList;
    }

    // Get NP subtrees from this tree
    public static List<Tree> getNPs(Tree tree, Tree anaphor) {
        // TODO: add filter to have same gender/number
        String number = getNumber(anaphor.getChild(0).value());

        TregexPattern tpattern = TregexPattern.compile("NP [!<< PRP & !<< NP]");
        TregexMatcher tmatcher = tpattern.matcher(tree);

        List<Tree> listNP = new ArrayList<>();
        while (tmatcher.find()) {
            Tree subtree = tmatcher.getMatch();
            try {
                if (!matchedTrees(subtree, "NP [" + number + "]").isEmpty() &&
                        (!useGoogle || genderMatches(anaphor.getChild(0).value(), subtree))) {
                    listNP.add(subtree);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return listNP;
    }

    public static String getNumber(String prp) {
        prp = prp.toLowerCase();
        String singular = "(i|me|she|her|he|him|it)";
        String plural = "(we|us|they|them)";
        String both = "(you)";
        if (prp.matches(singular)) {
            return " << NN | << NNP";
        } else if (prp.matches(plural)) {
            return " << NNS | << NNP";
        } else if (prp.matches(both)) {
            return " << NN | << NNS";
        }
        return " << NN | << NNS | << NNP";
    }

    /**
     * Checks whether genders of preposition and noun match
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean genderMatches(String prp, Tree nounPhrase) throws IOException, InterruptedException {
        prp = prp.toLowerCase();
        String noun = matchedTrees(nounPhrase, "(NN >> NP) | (NNS >> NP) | (NNP >> NP)").get(0).getChild(0).value();
        String masculine = "(he|him)";
        String feminine = "(she|her)";
        String notAlive = "(it)";
        String neutral = "(i|me|we|us|they|them|you)";
        int occur = 0;
        if (prp.matches(masculine)) {
            occur = GoogleHelper.occurNumber(noun, "himself");
            return (occur >= GoogleHelper.occurNumber(noun, "herself") &&
                    occur >= GoogleHelper.occurNumber(noun, "itself"));
        } else if (prp.matches(feminine)) {
            occur = GoogleHelper.occurNumber(noun, "herself");
            return (occur >= GoogleHelper.occurNumber(noun, "himself") &&
                    occur >= GoogleHelper.occurNumber(noun, "itself"));
        } else if (prp.matches(notAlive)) {
            occur = GoogleHelper.occurNumber(noun, "itself");
            return (occur >= GoogleHelper.occurNumber(noun, "himself") &&
                    occur >= GoogleHelper.occurNumber(noun, "herself"));
        } else if (prp.matches(neutral)) {
            return true;
        }
        return true;
    }

    /**
     * Get trees by pattern
     * @param root parent tree root
     * @param pattern pattern to match
     * @return list of subtrees of tree with root <code>root</code>
     */
    public static List<Tree> matchedTrees(Tree root, String pattern) {
        TregexPattern tpattern = TregexPattern.compile(pattern);
        TregexMatcher tmatcher = tpattern.matcher(root);

        List<Tree> setTrees = new ArrayList<>();
        while (tmatcher.find()) {
            Tree subtree = tmatcher.getMatch();
            setTrees.add(subtree);
        }
        return setTrees;
    }

    /**
     * @param verb verb to find basic form
     * @return basic (infinitive) form of verb
     */
    public static String getBaseVBForm(String verb) {
        return Morphology.lemmaStatic(verb.toLowerCase(), "VB", true);
    }

    /**
     * Get list of trees, each representing one sentence from file
     */
    public static List<Tree> getTreeList(LexicalizedParser lexParser, String filename) {
        List<Tree> treeList = new ArrayList<Tree>();

        for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
            Tree parse = lexParser.apply(sentence);
            treeList.add(parse);
        }

        return treeList;
    }

    /**
     * Get all pronouns in tree (that represents one sentence)
     */
    public static List<Tree> getPronounPhrases(Tree sentenceTree) {
        TregexPattern tpattern = TregexPattern.compile("PRP [!< you & !< You]");
        TregexMatcher tmatcher = tpattern.matcher(sentenceTree);

        List<Tree> listPRP = new ArrayList<>();
        while (tmatcher.find()) {
            Tree subtree = tmatcher.getMatch();
            listPRP.add(subtree);
        }
        return listPRP;
    }

    /**
     *
     * @param index index of sentence in <code>treeList</code>
     * @param treeList list of sentences, each represented in <code>Tree</code>
     * @return list of up to 3 sentences before the one on position <code>index</code>
     */
    public static List<Tree> getSentences(int index, List<Tree> treeList) {
        List<Tree> sentences = new ArrayList<>();
        for (int i = 0; i <= 3; i++) {
            if (index - i >= 0) {
                sentences.add(treeList.get(index - i));
            }
        }
        return sentences;
    }

    /**
     * Checks whether the sentence is imperative
     */
    public static boolean isImperative(Tree sentence) {
        // First word in sentence is verb (in infinitive)
        return sentence.getLeaves().get(0).parent(sentence).value().equals("VB");
    }

    public static String chooseAntecedent(List<Tree> listSent, List<Tree> listNP, int[] scores, Tree anaphor, Tree parent) {
        int occur = 1;
        int max = -100;
        int maxInd = 0;
        List<Tree> trees = new ArrayList<Tree>();
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > max) {
                trees.clear();
                max = scores[i];
                trees.add(listNP.get(i));
                maxInd = i;
                occur = 1;
            } else if (scores[i] == max) {
                occur++;
                trees.add(listNP.get(i));
            }
        }

        if (occur == 1) {
            return listNP.get(maxInd).toString();
        }

        ///////////////////////////////////////////////////////////////////////////////////
        int[] newScores = new int[trees.size()];
        newScores = MARSEvaluator.evalImmediateReference(trees, anaphor, parent, newScores);

        occur = 1;
        max = -100;
        maxInd = 0;
        List<Tree> trees1 = new ArrayList<Tree>();
        //trees.clear();
        for (int i = 0; i < newScores.length; i++) {
            if (newScores[i] > max) {
                trees1.clear();
                max = scores[i];
                trees1.add(trees.get(i));
                maxInd = i;
                occur = 1;
            } else if (newScores[i] == max) {
                occur++;
                trees1.add(trees.get(i));
            }
        }

        if (occur == 1) {
            return trees.get(maxInd).toString();
        }
        //////////////////////////////////////////////////////////////////////////////////
        newScores = new int[trees1.size()];
        newScores = MARSEvaluator.evalCollocationPattern(listSent, trees, anaphor, parent, newScores);

        occur = 1;
        max = -100;
        maxInd = 0;
        trees.clear();
        for (int i = 0; i < newScores.length; i++) {
            if (newScores[i] > max) {
                trees.clear();
                max = scores[i];
                trees.add(trees1.get(i));
                maxInd = i;
                occur = 1;
            } else if (newScores[i] == max) {
                occur++;
                trees.add(trees1.get(i));
            }
        }

        if (occur == 1) {
            return trees1.get(maxInd).toString();
        }
        ///////////////////////////////////////////////////////////////////////////////

        newScores = new int[trees.size()];
        newScores = MARSEvaluator.evalIndicatorWords(listSent, trees, anaphor, newScores);

        occur = 1;
        max = -100;
        maxInd = 0;
        trees1.clear();
        for (int i = 0; i < newScores.length; i++) {
            if (newScores[i] > max) {
                trees1.clear();
                max = scores[i];
                trees1.add(trees.get(i));
                maxInd = i;
                occur = 1;
            } else if (newScores[i] == max) {
                occur++;
                trees1.add(trees.get(i));
            }
        }

        if (occur == 1) {
            return trees.get(maxInd).toString();
        }
        /////////////////////////////////////////////////////////////////////////////////

//		for (int i = 0; i < scores.length; i++) {
//			System.out.println(listNP.get(i).toString() + " - " + scores[i]);
//		}

//		System.out.println("Occurs: " + occur);
//		System.out.println();
        return trees.get(trees.size() - 1).toString();
    }

    public static boolean isVerb(Tree element) {
        switch (element.value()) {
            case "VB":
            case "VBD":
            case "VBZ":
                return true;
            default:
                return false;
        }
    }

    public static boolean isComplexSentence(Tree sentence) {
        if (matchedTrees(sentence, "SBAR").isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
}
