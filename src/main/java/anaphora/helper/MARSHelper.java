package anaphora.helper;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MARSHelper {
    public static boolean useGoogle = false;

    // Get NP from up to 3 sentences before sentence index
    public static List<Tree> getNPs(int index, List<Tree> treeList, Tree anaphor) {
        List<Tree> nounPhrasesList = new ArrayList<>();

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
            // NP's not dominating other NP's
            List<Tree> trees = matchedTrees(treeList.get(index), "NP !<< NP");
            for (Tree t : trees) {
                if (labelOf(t.firstChild()).equals("PRP") && t.firstChild().equals(anaphor)) {
                    break;
                } else if (!labelOf(t.firstChild()).equals("PRP")) {
                    nounPhrasesList.add(t);
                }
            }
        }

        return nounPhrasesList;
    }

    // Get NP subtrees from this tree
    public static List<Tree> getNPs(Tree tree, Tree anaphor) {
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
     *
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
     *
     * @param root    parent tree root
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
    public static List<Tree> getParsedSentences(LexicalizedParser lexParser, String filename) {
        List<Tree> treeList = new ArrayList<>();

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
     * @param index    index of sentence in <code>treeList</code>
     * @param treeList list of sentences, each represented in <code>Tree</code>
     * @return list of up to 3 sentences before the one on position <code>index</code>
     */
    public static List<Tree> getPrevThreeSentences(int index, List<Tree> treeList) {
        List<Tree> sentences = new ArrayList<>();
        for (int i = 0; i <= 3; i++) {
            if (index - i >= 0) {
                sentences.add(treeList.get(index - i));
            }
        }
        return sentences;
    }

    public static String labelOf(Tree tree) {
        return tree.value();
    }

    public static String wordOf(Tree tree) {
        return tree.firstChild().toString();
    }

    public static String stringFormOf(Tree tree) {
        return tree.getLeaves().stream().map(Tree::value).reduce((s, s2) -> s.concat(" ").concat(s2)).get();
    }

    /**
     * Checks whether the sentence is imperative
     */
    public static boolean isImperative(Tree sentence) {
        // First word in sentence is verb (in infinitive)
        return sentence.getLeaves().get(0).parent(sentence).value().equals("VB");
    }

    public static boolean isVerb(Tree element) {
        switch (labelOf(element)) {
            case "VB":
            case "VBD":
            case "VBZ":
                return true;
            default:
                return false;
        }
    }

    public static boolean isComplexSentence(Tree sentence) {
        return !matchedTrees(sentence, "SBAR").isEmpty();
    }
}
