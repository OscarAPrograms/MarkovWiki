///////////////////////////////////////////////////////////////////////////////
//
// Title:    Markov Model
//
// Author:   Oscar Afraymovich
//
//////////////////////// ASSISTANCE/HELP CITATIONS ////////////////////////////
//
//								   							  N/A
//
///////////////////////////////////////////////////////////////////////////////
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import io.github.fastily.jwiki.core.*;

/** 
 * Class representing the Markov Model that is used for random text generation.
 * This model utilizes a HashMap to connect String keys (with a "pastState" number of words)
 * taken from a given text to ArrayList values containing all Strings (with a
 * "futureState" number of words) that follow said keys in the text. Duplicate elements in the
 * ArrayList values allow for some future states to be more probable than others when
 * generating text.
 */
public class MarkovModel {
	/** 
	 * HashMap connecting past state to an ArrayList of potential future states.
	 */
	private HashMap<String, ArrayList<String>> modelHashMap;
	
	/** 
	 * The number of words that constitute a previous state.
	 */
	private int pastState;
	
	/** 
	 * The number of words that constitute a future state.
	 */
	private int futureState;
	
	/** 
	 * MarkovModel object constructor. Initializes "modelHashMap", "pastState" and "futureState".
	 * 
	 * @param numWordsPast - the number of words intended to constitute a past state.
	 */
	public MarkovModel(int numWordsPast) {
		/** Note if tweaking this program: Setting futureState > 1 can lead to errors. With this 
		 * tweak the model may generate text with no corresponding past state in modelHashMap.
		 *
		 * This occurs when the text generated is a future state that is followed by less than
		 * "futureState" words (but more than 0 words) in the paragraph it was taken from. When
		 * this text is generated, the last "pastState" words of that future state will be reused
		 * to continue generation (per the definition of a Markov Model) but no past states follow
		 * less than "futureState" words in modelHashMap.
		 */
		modelHashMap = new HashMap<>();
		pastState = numWordsPast;
		futureState = 1;
	}
	
	/** 
	 * Makes the "text" argument readable and checks if it has at least one sentence*. If "text"
	 * has no sentences it is impossible for the Markov chain model to generate novel sentences
	 * and an IllegalArgumentException is thrown. 
	 * 
	 * Otherwise this method splits "text" into paragraphs and for each paragraph:
	 * 
	 * 1. Adds all "pastState"-grams (strings with "pastState" contiguous words) in the paragraph
	 * as keys in "modelHashMap". 
	 * 
	 * 2. Adds the 1 word strings ("futureState"-grams) that follow the keys to the keys'
	 * corresponding ArrayList values. 
	 * 
	 * 3. Adds "\n\n" to the ArrayList value corresponding to the key containing the last
	 * "pastState" words in each paragraph*.
	 * 
	 * 4. Adds the first "pastState" words in each paragraph to the ArrayList value corresponding
	 * to the key "\n\n" (the Markov Model needs at least "pastState" words to generate text)*.
	 * 	 * 
	 * *Note: Splitting the text into paragraphs and adding "\n\n" keys/values allows the Markov
	 * chain model to generate text that is split into paragraphs.
	 * 
	 * @param text - the String that is parsed to build modelHashMap.
	 * @throws IllegalArgumentException - when "text" has no sentences.
	 */
	public void parseText(String text) throws IllegalArgumentException {
		// Make "text" readable by removing extraneous information embedded by jwiki.
		text = jwikiHTMLParser(text);
		
		// Throws an illegal argument exception if text doesn't have a sentence.
		containsSentence(text);
		
		// Split the text into paragraphs (using any number of contiguous "\n"'s as a delimiter).
		String[] textParagraphs = text.split("\n+");
		
		// For every paragraph in the text:
		for (String paragraph : textParagraphs) {
			// Split the paragraph into words (delimited by any amount of whitespace)
			String[] paragraphWords = paragraph.split("\\s+");
			
			// If statement gets rid of headers that may be mistaken as full paragraphs
			// (these headers would cause an Index Out of Bounds Exception in steps 3&4)
			if(paragraphWords.length >= pastState) {
				// For every word in the text followed by at least ("futureState"+"pastState"-1) words:
				// (1&2&5):
			for (int i = 0; i<paragraphWords.length-(futureState+pastState)+1; ++i) {
				String key = ""; 
				String value = "";
				int j; 
				
				// 1. Set "key" equal to the "pastState"-gram that starts at the "i"th word in "text".
				for (j = i; j < i+pastState; ++j) {
					key += paragraphWords[j] + " ";
				}
				// Now that the loop above completed, "j" stores the index of the word following "key".

				// 2. Set "value" equal to the word ("futureState"-gram) following the "key".
				value += (paragraphWords[j]) + " ";

				// Add the key-value pair to "modelHashMap". 
				this.addMapping(key, value);
			}

			// 3. Add "\n\n" and the first "pastState"-gram in the paragraph as a key-value pair 
			String firstState = "";
			for (int l = 0; l<pastState; ++l) {
				firstState += (paragraphWords[l]) + " ";
			}
			this.addMapping("\n\n", firstState);
			
			// 4. Add the last "pastState"-gram in the paragraph and "\n\n" as a key-value pair.
			String lastPastState = "";
			for (int l = paragraphWords.length-pastState; l<paragraphWords.length; ++l) {
				lastPastState += paragraphWords[l] + " ";
			}
			this.addMapping(lastPastState, "\n\n");
			}
			}
		}
	
	/** 
	 * Throws an IllegalArgumentException when "text" does not contain a sentence.
	 * 
	 * Note: This method looks at the capitalization of words following a '.', '!', '?', or 
	 * these characters followed by a '"' (because acronyms [U.S.] and abbreviations [etc.]
	 * ending in '.' add ambiguity to whether or not a '.' ended a sentence) and if none of these
	 * words are capitalized an exception is thrown. This means that, while in reality sentences
	 * can start with a number/symbol (instead of a capitalized letter), this program will not
	 * consider these to be sentences (because its hard to distinguish from acronyms followed by 
	 * numbers/symbols). Also sentences like the "U.S. Virgin Islands ..." will be interpreted as
	 * separate sentences (because "Virgin" is capitalized).  There is no way around these edge
	 * cases without natural language processing. 
	 * 
	 * @param text - the String that is checked for sentences.
	 * @throws IllegalArgumentException - when "text" has no sentences.
	 */
	public void containsSentence(String text) throws IllegalArgumentException {
		/* Set "noPeriod" equal to "text" after removing:
		 * Words (contiguous non-whitespace character[s]) ending in a '.', '!', '?', or these 
		 * characters and a '"', followed by whitespace and a capital letter.
		 */
		// Regex for a word ending in '.', '!', '?', or these characters and a '"'.
		String regexSyntax = "[^\\s]+[\\.\\!\\?](\"|)"; 
		// Regex for words followed by whitespace and a capital letter.
		String noPeriod  = text.replaceAll(regexSyntax + "\\s+[A-Z]", "");
		
		String [] noPeriodWords = noPeriod.split("\\s+"); // Array of all the words in noPeriod.
		String lastWord = noPeriodWords[noPeriodWords.length-1]; // Last word in noPeriod.
		/* Remove the last word in "noPeriod" if it ends in a '.', '!', '?', or these characters
		 * and a '"' (it marks the end of a sentence but was not removed by the previous code).
		 */
		if (lastWord.endsWith(".") || lastWord.endsWith("!") || lastWord.endsWith("?") ||
				lastWord.endsWith(".\"") || lastWord.endsWith("!\"") || lastWord.endsWith("?\"")) {
			// "noPeriod.lastIndexOf(lastWord)-1" is the index right before "lastWord".
			noPeriod = noPeriod.substring(0, noPeriod.lastIndexOf(lastWord)-1);
		}
		
		// If "noPeriod" equals "text" that means "text" has no sentences.
		if (noPeriod.equals(text)) {
			// Throw an IllegalArgumentException, "text" is invalid for the Markov chain model.
			throw new IllegalArgumentException();
		} 
	}

	/** 
	 * Adds the inputed key-value pair to modelHashMap.
	 * 
	 * @param key - the String key that is mapped to an ArrayList value.
	 * @param value - the String that is added to the ArrayList value mapped to the key.
	 */
	public void addMapping(String key, String value) {
		// If modelHashMap does not contain "key", map "key" to an empty ArrayList in modelHashMap.
		if(!(modelHashMap.containsKey(key))) {
			modelHashMap.put(key, new ArrayList<String>());
		} 
		// Add "value" to the ArrayList value corresponding to "key"
			modelHashMap.get(key).add(value);
	}
	
	/** 
	 * Generates random text based on modelHashMap. The first words are randomly selected based
	 * off the ArrayList value corresponding to the key "\n\n".
	 * 
	 * @param numSentences - the number of sentences generated.
	 */
	public String generateText(int numSentences) {
		// Stores the value of the generated text.
		String textGenerated = "";
		// Stores the number of sentences.
		int sentences = 0;
		// Stores the value of the pastState that will be used for futureState generation.
		String tempPastState = "\n\n";
		// Creates a pseudorandom number generator.
		Random randomIndex = new Random();
		
		/* 
		 * True if in the previous while loop iteration "toAdd" ended in a '.', '!', '?', or these
		 * characters and a '"'.
		 */
		Boolean checkNext = false; 
		
		while (sentences != numSentences) {
			// Stores the number of potential future states inside.
			int valueLength = modelHashMap.get(tempPastState).size();
			
			String toAdd = modelHashMap.get(tempPastState).get(randomIndex.nextInt(valueLength));
			
			// If the previously added word ended in a '.', '!', '?', or these characters and a '"':
			if(checkNext) {
				// If "toAdd" is capitalized  or "\n\n" (previous word may be an acronym/abbreviation):
				if(Character.isUpperCase(toAdd.charAt(0)) || toAdd.equals("\n\n")) {
					++sentences; // Increment "sentences".
				}
			}
			
			// Checks that "sentences" does not equal "numSentences":
			if (sentences != numSentences) {
				// Adds a pseudorandom future state to "textGenerated".
				textGenerated += toAdd;		
			} else {
				break; // If "sentences" equals "numSentences", end the loop.
			}
			
		checkNext	= false;
		// If the previous future state was "\n\n", reuse it as a past state.
		if (toAdd == "\n\n") {
			tempPastState = "\n\n";
		} else {
			// Otherwise, stores last "pastState" words in "textGenerated" to use as new past state.
			tempPastState = "";
			// Stores already generated words in an ArrayList (delimited by any amount of whitespace)
			String[] generatedWords = textGenerated.split("\\s+");
			for (int i = generatedWords.length-pastState; i < generatedWords.length; ++i) {
				tempPastState += generatedWords[i] + " ";
				}
			
			// If "toAdd" ends in '.', '!', '?', or these characters and '"' set "checkNext" to true.
			if((toAdd.endsWith(". ")) || (toAdd.endsWith("! ")) || (toAdd.endsWith("? ")) ||
					(toAdd.endsWith(".\" ")) || (toAdd.endsWith("!\" ")) || (toAdd.endsWith("?\" "))) {
				checkNext = true; 
				}
			}
		}
		
		return textGenerated;
	}

/** 
 * Removes extraneous HTML information (like descriptions of references, images, or links)
 * embedded in "text" by jwiki's "getPageText()" method, producing a near perfect conversion of
 * "text" back to the Wikipedia article it was taken from (not perfectly converted because
 * headings are intentionally removed, "\n"s are dealt with in the "parseText()" method, and
 * MediaWiki formats some info like dates and HTML tags in a way that is hard to revert back to
 * readable text).
 * 
 * Note: In regex syntax:
 * 1. ".*?" means any number of characters until the latter part of the pattern is matched 
 * (non-greedy/lazy).
 * 2. ".*" means any number of characters until the last occurrence (on a given line) of the
 * latter part of the pattern is matched (greedy).
 * 3. ".+" means one or more characters until the last occurrence (on a given line) of the
 * latter part of the pattern is matched (greedy).
 * 4. ("[^" + char + "]") means any character but char.
 * 5. "[", "]", "{", "{", "|", "/" have special regex meanings and are thus prefaced by "\\".
 * 
 * @param text - Wikipedia article with extraneous information.
 * @return readable Wikipedia article.
 */
public static String jwikiHTMLParser(String text) {
	/*
	 *  jwiki's "getPageText()" includes HTML for three different types of embeded references:
	 *  1. "<ref>...</ref>" 
	 *  2. "<ref name="...">...</ref>" 
	 *  3. "<ref name=.../>...</ref name=... />" 
	 *  The first two types should be deleted (they provide reference descriptions). But the
	 *  third type contains text from the article in between the "ref" tags, thus:
	 */
	
	/*
	 * Remove strings enclosed by "<ref" and "</ref>", without "<" in between the tags (type 1
	 * and 2 references) in case of "<ref...<ref name=... />...</ref>" (type 3 reference
	 * surrounded by type 1 or 2 references on the same line).
	 */
	String regexSyntax = "[^<]"; // Regex for any char but "<"
	text = text.replaceAll("<ref" + regexSyntax + "*</ref>", ""); 
	
	/*
	 * Remove HTML tags but not text in between them (strings starting with "<" or "</" followed
	 * by a lowercase letter or symbol and ending with ">", without "<" in between the brackets).
	 * 
	 * Ex: Type 3 "<ref" tags, "<!--" HTML comments, "<code" tags, "<span" tags, etc.
	 * 
	 * Note: This regex is supposed to avoid deleting Wikipedia text enclosed by "<" and ">"
	 * that is intentionally in an article (not an HTML tag) although it can fail.
	 */
	regexSyntax = "[^<]"; // Regex for any char but "<"
	text = text.replaceAll("<[^A-Z]" + regexSyntax + "*>", "");
	
	// Remove "&nbsp;" or "{{nbsp}}" (indicates a "non-breaking" space)
	text = text.replaceAll("&nbsp;", " ");
	text = text.replaceAll("\\{\\{nbsp\\}\\}", " ");
	
	// Remove "&ndash;" with a hyphen (indicates an en dash).
	text = text.replaceAll("&ndash;", "-");
	
	// Remove "{{!}}" (this can throw off the next line of code if not removed).
	text = text.replaceAll("\\{\\{!\\}\\}", "");
	
	// Remove strings enclosed by "{{" and "}}" (other info)
	text = text.replaceAll("\\{\\{.*?\\}\\}", "");
	
	// Remove strings that start with "[[File:" and end with "]]" (info about images)
	text = text.replaceAll("\\[\\[File:.*\\]\\]", "");
	
	// Remove any text following "Categories" section. 
	if (text.indexOf("[[Category:") != -1) {
		text = text.substring(0, text.indexOf("[[Category:"));
	}
	
	// Remove strings enclosed by "[[" and "|", without "[" or "\n" in between (titles of linked
	// articles, different from actual text) in case of "[[...]]...[[...|" 
	regexSyntax = "[^\\[\n]"; // Regex for any char but "[" or "\n"
	text = text.replaceAll("\\[\\[" + regexSyntax + "*?\\|", "");
	
	// Remove "[[" and "]]" (titles of linked articles, same as actual text)
	text = text.replaceAll("\\[\\[", "");
	text = text.replaceAll("\\]\\]", "");
	
	// Remove "'''" or  "''"(indicates the text is bolded or italicized)
	text = text.replaceAll("('''|'')", "");
	
	// Remove lines that start with "===", "==", or "* " (headings, subheadings, bullet points)
	text = text.replaceAll("(===|==|\\*).*", "");

	
	// Remove remaining text starting with "|", "*", "{", or "!" and ending with "}" (info tables)
	regexSyntax = "[^\\}]"; // Regex for any char but "}"
	text = text.replaceAll("(\\||\\*|\\{|!)" + regexSyntax + "*\\}.*", "");
	
	// Remove Behavior switch "magic words".
	text = text.replaceAll("__[A-Z]+__", "");
	
	// Remove any remaining brackets
	text = text.replaceAll("}}", "");
	
	return text;
	}
}


