package anaphora.finder;

import static anaphora.helper.MARSHelper.*;
import static anaphora.evaluator.MARSEvaluator.*;

import java.util.List;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

public class AnaphoraResolver {
    private static final String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
    private static final LexicalizedParser PARSER = LexicalizedParser.loadModel(PCG_MODEL);

    public static void resolve(String fileName) {
        List<Tree> treeList = getTreeList(PARSER, "input.txt");
        for (int i = 0; i < treeList.size(); i++) {
            Tree t = treeList.get(i);
            List<Tree> pronouns = getPronounPhrases(t);

            System.out.println("Sentence: " + (i + 1));
            List<Tree> listSent = getSentences(i, treeList);
            for (Tree anaphor : pronouns) {
                List<Tree> listNP = getNPs(i, treeList, anaphor);
                int[] scores = evaluate(listSent, listNP, anaphor, t);

                String antecedent = chooseAntecedent(listSent, listNP, scores, anaphor, t);
                System.out.println(anaphor + " - " + antecedent);
            }
        }
    }

    public static void main(String[] args) {
        resolve("input.txt");
//
//		List<Tree> treeList = getTreeList(PARSER, "input.txt");
//		List<Tree> pronouns;
//
//		for (int i = 0; i < treeList.size(); i++) {
//			Tree t = treeList.get(i);
//			pronouns = getPronounPhrases(t);
//			t.pennPrint();
//
//
//			//List<Tree> listNP = getNPs(i, treeList);
////			List<Tree> listNP = matchedTrees(t, "S [<< PRP | << NP]");
//////			Set<Tree> listNP = matchedTrees(t, "VP < ((VP < (VB .. (NP !< PRP))) "
//////					+ "[$+ (CC $+ (VP < (VB .. (NP < PRP)))) | $+ (/,/ $+ (VP < (VB .. (NP < PRP))))])");
////			for (Tree anaphor : listNP) {
//////				/*anaphor.parent(t).parent(t).pennPrint();  $+ (VP < (/VB./ .. (NP < PRP)))
//////				for (Tree tr : anaphor.parent(t).parent(t).children()) {
//////					tr.pennPrint();
//////				}
//////				System.out.println();
//////				break;*/
////				anaphor.pennPrint();
////			}
//			System.out.println("-------------------------");
//		}
    }
}
