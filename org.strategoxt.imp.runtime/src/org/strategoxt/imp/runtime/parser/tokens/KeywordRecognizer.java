package org.strategoxt.imp.runtime.parser.tokens;

import static java.util.Collections.synchronizedMap;
import static org.spoofax.jsglr.Term.termAt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.spoofax.jsglr.Label;
import org.spoofax.jsglr.ParseTable;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;

import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;

/**
 * Recognizes keywords in a language without considering their context.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class KeywordRecognizer {

	private static final Map<Descriptor, KeywordRecognizer> cache =
		synchronizedMap(new WeakHashMap<Descriptor, KeywordRecognizer>());
	
	private static final AFun litFun = Environment.getATermFactory().makeAFun("lit", 1, false);
	
	private final Set<String> keywords = new HashSet<String>();
	
	private KeywordRecognizer(ParseTable table) {
		if (table != null) {
			for (Label l : table.getLabels()) {
				if (l != null) {
					ATerm rhs = termAt(l.getProduction(), 1);
					if (rhs instanceof ATermAppl && ((ATermAppl) rhs).getAFun() == litFun) {
						ATermAppl lit = termAt(rhs, 0);
						String litString = lit.getName();
						if (TokenKindManager.isKeyword(litString))
							keywords.add(litString);
					}
				}
			}
		}
	}
	
	public static KeywordRecognizer create(Descriptor d) {
		KeywordRecognizer result = cache.get(d);
		if (result == null) {
			try {
				Debug.startTimer();
				ParseTable table = Environment.getParseTable(d.getLanguage());
				Debug.stopTimer("Keyword recognizer loaded for " + d.getLanguage().getName());
				result = new KeywordRecognizer(table);
				cache.put(d, result);
			} catch (Exception e) {
				Environment.logException("Unexpected exception initializing the keyword recognizer", e);
				return new KeywordRecognizer(null);
			}
		}
		return result;
	}
	
	public boolean isKeyword(String literal) {
		return keywords.contains(literal);
	}
}