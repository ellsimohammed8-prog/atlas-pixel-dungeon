/*
 * Atlas Pixel Dungeon — Arabic text support
 *
 * The game's renderer draws glyphs strictly left-to-right and cannot use
 * OpenType shaping, so Arabic strings are converted here into Unicode
 * Presentation Forms-B codepoints (which the bundled Arabic font contains)
 * and reordered into visual order with a simplified bidi pass.
 * Non-Arabic text is returned untouched.
 */
package com.watabou.utils;

public class ArabicSupport {

	public static boolean containsArabic(String s) {
		if (s == null) return false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 0x0600 && c <= 0x06FF) return true;
		}
		return false;
	}

	// letter tables: base char -> [isolated, final, initial, medial] presentation forms (0 = none)
	private static int[] forms(char c) {
		switch (c) {
			case 0x0621: return new int[]{0xFE80, 0, 0, 0};                 // hamza
			case 0x0622: return new int[]{0xFE81, 0xFE82, 0, 0};           // alef madda
			case 0x0623: return new int[]{0xFE83, 0xFE84, 0, 0};           // alef hamza above
			case 0x0624: return new int[]{0xFE85, 0xFE86, 0, 0};           // waw hamza
			case 0x0625: return new int[]{0xFE87, 0xFE88, 0, 0};           // alef hamza below
			case 0x0626: return new int[]{0xFE89, 0xFE8A, 0xFE8B, 0xFE8C}; // yeh hamza
			case 0x0627: return new int[]{0xFE8D, 0xFE8E, 0, 0};           // alef
			case 0x0628: return new int[]{0xFE8F, 0xFE90, 0xFE91, 0xFE92}; // beh
			case 0x0629: return new int[]{0xFE93, 0xFE94, 0, 0};           // teh marbuta
			case 0x062A: return new int[]{0xFE95, 0xFE96, 0xFE97, 0xFE98}; // teh
			case 0x062B: return new int[]{0xFE99, 0xFE9A, 0xFE9B, 0xFE9C}; // theh
			case 0x062C: return new int[]{0xFE9D, 0xFE9E, 0xFE9F, 0xFEA0}; // jeem
			case 0x062D: return new int[]{0xFEA1, 0xFEA2, 0xFEA3, 0xFEA4}; // hah
			case 0x062E: return new int[]{0xFEA5, 0xFEA6, 0xFEA7, 0xFEA8}; // khah
			case 0x062F: return new int[]{0xFEA9, 0xFEAA, 0, 0};           // dal
			case 0x0630: return new int[]{0xFEAB, 0xFEAC, 0, 0};           // thal
			case 0x0631: return new int[]{0xFEAD, 0xFEAE, 0, 0};           // reh
			case 0x0632: return new int[]{0xFEAF, 0xFEB0, 0, 0};           // zain
			case 0x0633: return new int[]{0xFEB1, 0xFEB2, 0xFEB3, 0xFEB4}; // seen
			case 0x0634: return new int[]{0xFEB5, 0xFEB6, 0xFEB7, 0xFEB8}; // sheen
			case 0x0635: return new int[]{0xFEB9, 0xFEBA, 0xFEBB, 0xFEBC}; // sad
			case 0x0636: return new int[]{0xFEBD, 0xFEBE, 0xFEBF, 0xFEC0}; // dad
			case 0x0637: return new int[]{0xFEC1, 0xFEC2, 0xFEC3, 0xFEC4}; // tah
			case 0x0638: return new int[]{0xFEC5, 0xFEC6, 0xFEC7, 0xFEC8}; // zah
			case 0x0639: return new int[]{0xFEC9, 0xFECA, 0xFECB, 0xFECC}; // ain
			case 0x063A: return new int[]{0xFECD, 0xFECE, 0xFECF, 0xFED0}; // ghain
			case 0x0641: return new int[]{0xFED1, 0xFED2, 0xFED3, 0xFED4}; // feh
			case 0x0642: return new int[]{0xFED5, 0xFED6, 0xFED7, 0xFED8}; // qaf
			case 0x0643: return new int[]{0xFED9, 0xFEDA, 0xFEDB, 0xFEDC}; // kaf
			case 0x0644: return new int[]{0xFEDD, 0xFEDE, 0xFEDF, 0xFEE0}; // lam
			case 0x0645: return new int[]{0xFEE1, 0xFEE2, 0xFEE3, 0xFEE4}; // meem
			case 0x0646: return new int[]{0xFEE5, 0xFEE6, 0xFEE7, 0xFEE8}; // noon
			case 0x0647: return new int[]{0xFEE9, 0xFEEA, 0xFEEB, 0xFEEC}; // heh
			case 0x0648: return new int[]{0xFEED, 0xFEEE, 0, 0};           // waw
			case 0x0649: return new int[]{0xFEEF, 0xFEF0, 0, 0};           // alef maksura
			case 0x064A: return new int[]{0xFEF1, 0xFEF2, 0xFEF3, 0xFEF4}; // yeh
			default: return null;
		}
	}

	private static boolean isArabicLetter(char c) { return forms(c) != null; }

	private static boolean isArabicDigit(char c) { // Arabic-Indic digits ٠-٩ read left-to-right
		return c >= 0x0660 && c <= 0x0669;
	}

	private static boolean isBracket(char c) { // pairs are re-mirrored by the final pass
		return c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}';
	}
	private static boolean isMark(char c) { // harakat / tatweel: transparent for joining
		return (c >= 0x064B && c <= 0x0652) || c == 0x0670 || c == 0x0640;
	}

	/** Shapes one logical-order line and returns it in visual order. */
	public static String shapeLine(String line) {
		if (!containsArabic(line)) return line;
		int n = line.length();

		// 1) lam-alef ligatures + per-letter form selection (logical order)
		StringBuilder shaped = new StringBuilder();
		for (int i = 0; i < n; i++) {
			char c = line.charAt(i);
			// lam-alef ligature
			if (c == 0x0644 && i + 1 < n) {
				char nx = line.charAt(i + 1);
				int lig = 0;
				if (nx == 0x0622) lig = joinsPrev(line, i) ? 0xFEF6 : 0xFEF5;
				else if (nx == 0x0623) lig = joinsPrev(line, i) ? 0xFEF8 : 0xFEF7;
				else if (nx == 0x0625) lig = joinsPrev(line, i) ? 0xFEFA : 0xFEF9;
				else if (nx == 0x0627) lig = joinsPrev(line, i) ? 0xFEFC : 0xFEFB;
				if (lig != 0) { shaped.append((char) lig); i++; continue; }
			}
			if (isArabicLetter(c)) {
				boolean prevJoins = joinsPrev(line, i);
				boolean nextJoins = false;
				for (int j = i + 1; j < n; j++) {
					char nc = line.charAt(j);
					if (isMark(nc)) continue;
					nextJoins = isArabicLetter(nc) && forms(c)[2] != 0;
					break;
				}
				int[] f = forms(c);
				int form;
				if (prevJoins && nextJoins && f[3] != 0) form = f[3];       // medial
				else if (prevJoins && f[1] != 0) form = f[1];               // final
				else if (nextJoins && f[2] != 0) form = f[2];               // initial
				else form = f[0];                                           // isolated
				shaped.append((char) form);
			} else {
				shaped.append(c);
			}
		}

		// 2) simplified bidi: reverse the ORDER of chunks. A chunk is either one
		//    Arabic word (reversed again at unit level, so Arabic reads right-to-
		//    left), or a maximal run of CONSECUTIVE non-Arabic words which reads
		//    left-to-right and keeps its word order (Latin phrases and multi-word
		//    numbers would read backwards word-by-word otherwise). A unit inside
		//    an Arabic word is one Arabic char, one ASCII bracket pair member, an
		//    Arabic-Indic digit run, or a maximal non-Arabic character run (latin
		//    words, numbers, %1$d placeholders).
		String s = shaped.toString();
		String[] words = s.split(" ", -1);
		java.util.ArrayList<java.util.ArrayList<String>> chunks = new java.util.ArrayList<>();
		java.util.ArrayList<String> latinRun = null;
		for (String word : words) {
			boolean arabicChunk = false;
			for (int k = 0; k < word.length() && !arabicChunk; k++) arabicChunk = isArabicUnit(word.charAt(k));
			if (arabicChunk) {
				latinRun = null;
				java.util.ArrayList<String> one = new java.util.ArrayList<>();
				one.add(word);
				chunks.add(one);
			} else {
				if (latinRun == null) {
					latinRun = new java.util.ArrayList<>();
					chunks.add(latinRun);
				}
				latinRun.add(word);
			}
		}
		StringBuilder out = new StringBuilder();
		for (int w = chunks.size() - 1; w >= 0; w--) {
			if (w < chunks.size() - 1) out.append(' ');
			java.util.ArrayList<String> chunk = chunks.get(w);
			if (chunk.size() > 1) { // latin run: word order and inner spacing preserved
				for (int k = 0; k < chunk.size(); k++) {
					if (k > 0) out.append(' ');
					out.append(chunk.get(k));
				}
			} else {
				String word = chunk.get(0);
				java.util.ArrayList<String> units = new java.util.ArrayList<>();
				int i = 0;
				while (i < word.length()) {
					//Atlas: ASCII brackets resolve to the RTL side in real bidi (a bracket
					//group round Arabic content displays as "( content )" with the
					//brackets at the group's visual edges). Making each bracket its own
					//unit lets the reversal move it there; the final pass re-mirrors the
					//glyph. Character runs like "+10%" / "%1$d" are deliberately kept as
					//single LTR islands so placeholder syntax never gets reordered.
					if (isBracket(word.charAt(i))) {
						units.add(word.substring(i, i + 1));
						i++;
					}
					//Atlas: Arabic-Indic digit runs read left-to-right within RTL text,
					//so a maximal run of ٠-٩ is one unit, like a latin word
					else if (isArabicDigit(word.charAt(i))) {
						int j = i;
						while (j < word.length() && isArabicDigit(word.charAt(j))) j++;
						units.add(word.substring(i, j));
						i = j;
					} else if (isArabicUnit(word.charAt(i))) {
						units.add(word.substring(i, i + 1));
						i++;
					} else {
						int j = i;
						while (j < word.length() && !isArabicUnit(word.charAt(j)) && !isBracket(word.charAt(j))) j++;
						units.add(word.substring(i, j));
						i = j;
					}
				}
				for (int u = units.size() - 1; u >= 0; u--) out.append(units.get(u));
			}
		}

		// 3) mirror brackets: in RTL visual order bracket glyphs face the other way
		String result = out.toString();
		StringBuilder mirrored = new StringBuilder(result.length());
		for (int i = 0; i < result.length(); i++) {
			char c = result.charAt(i);
			switch (c) {
				case '(': c = ')'; break;
				case ')': c = '('; break;
				case '[': c = ']'; break;
				case ']': c = '['; break;
				case '{': c = '}'; break;
				case '}': c = '{'; break;
			}
			mirrored.append(c);
		}
		return mirrored.toString();
	}

	/**
	 * Atlas: true when s contains Arabic either in logical codepoints
	 * (U+0600–U+06FF) or already shaped (presentation forms A/B and the
	 * supplementary Arabic range). RenderedTextBlock uses this to decide
	 * which lines to right-align, including tokens produced by shape().
	 */
	public static boolean containsArabicText(String s) {
		if (s == null) return false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if ((c >= 0x0600 && c <= 0x06FF) || (c >= 0xFB50 && c <= 0xFDFF) || (c >= 0xFE70 && c <= 0xFEFF)) return true;
		}
		return false;
	}

	private static boolean joinsPrev(String s, int i) {
		for (int j = i - 1; j >= 0; j--) {
			char pc = s.charAt(j);
			if (isMark(pc)) continue;
			return isArabicLetter(pc) && forms(pc)[2] != 0;
		}
		return false;
	}

	private static boolean isArabicUnit(char c) {
		return (c >= 0xFE80 && c <= 0xFEFF) || (c >= 0xFB50 && c <= 0xFDFF)
				|| (c >= 0x0600 && c <= 0x06FF);
	}

	/** Shapes every line of a (possibly multi-line) string. */
	public static String shape(String text) {
		if (!containsArabic(text)) return text;
		//Atlas: presentation-form input means the string has ALREADY been shaped
		//(RenderedTextBlock pre-shapes whole strings before tokenizing, then each
		//token goes through RenderedText.shape() again). Shaping must be
		//idempotent, or the bidi pass would re-reorder the visual glyphs.
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c >= 0xFE70 && c <= 0xFEFF) return text;
		}
		String[] lines = text.split("\n", -1);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lines.length; i++) {
			if (i > 0) sb.append('\n');
			sb.append(shapeLine(lines[i]));
		}
		return sb.toString();
	}
}
