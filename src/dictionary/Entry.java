package dictionary;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Entry {
	private String language1, language2;
	private String type;
	private String classification;
	private UUID id;
	private int wordHit = 0;
	private Set<String> words = new HashSet<String>();

	public Entry(String language1, String language2) {
		this.language1 = language1;
		this.language2 = language2;
		this.setId(UUID.randomUUID());

		String all = language1 + " " + language2;

		all = all.replaceAll("\\[.+?\\]", "");
		all = all.replaceAll("\\{.+?\\}", "");
		for (String word : all.split("[\\s-]")) {
			word = word.toLowerCase().replaceAll("[^A-Za-zÄÖÜäöüáÁàÀéÉèÈúÚùÙíÍìÌóÓòÒýÝß]", "");
			if (word.length() > 1) {
				words.add(word);
			}
		}
	}

	public int getRelevance() {
		int t = 64 * wordHit + 8 * (8 - Math.min(8, getLength()));

		if (type == null) {
			t += 2;
		} else {
			switch (type.toLowerCase()) {
			case "noun":
				t += 7;
				break;
			case "verb":
				t += 6;
				break;
			case "adv":
			case "adj":
				t += 5;
				break;
			case "idiom":
				t += 3;
				break;
			case "[none]":
				t += 2;
				break;
			default:
				t += 4;
			}
		}
		return t;

	}

	public Set<String> getAllWords() {
		return words;
	}

	public String getLanguage1() {
		return language1;
	}

	public void setLanguage1(String language1) {
		this.language1 = language1;
	}

	public String getLanguage2() {
		return language2;
	}

	public void setLanguage2(String language2) {
		this.language2 = language2;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String toString() {
		return language1 + "\t" + language2 + "\t" + type + "\t" + classification;
	}

	public int getLength() {
		return words.size();
	}

	public int getWordHit() {
		return wordHit;
	}

	public void setWordHit(int wordHit) {
		this.wordHit = wordHit;
	}
}
