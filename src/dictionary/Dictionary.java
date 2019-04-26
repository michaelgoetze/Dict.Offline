package dictionary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import GUI.MessageWindow;

public class Dictionary {
	private Map<String, Set<UUID>> wordMap = new HashMap<String, Set<UUID>>();
	private Map<UUID, Entry> entryMap = new HashMap<UUID, Entry>();
	private String language1, language2;

	public Dictionary(String file) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

			String line = br.readLine();
			while (line != null) {
				if (!line.startsWith("#")) {
					while (line.matches(".*&#.+?;.*")) {
						char c = (char) Integer.parseInt(line.replaceAll(".*&#(.+?);.*", "$1"));
						line = line.replaceAll("&#.+?;", String.valueOf(c));
					}
					String[] dataArray = line.split("\t");
					try {
						Entry entry = new Entry(dataArray[0], dataArray[1]);
						try {
							entry.setType(dataArray[2]);
							entry.setClassification(dataArray[3]);
						} catch (ArrayIndexOutOfBoundsException e) {
						}
						entryMap.put(entry.getId(), entry);
						for (String word : entry.getAllWords()) {
							if (!wordMap.containsKey(word)) {
								wordMap.put(word, new HashSet<UUID>());
							}
							wordMap.get(word).add(entry.getId());
						}
					} catch (ArrayIndexOutOfBoundsException e1) {
					}
				} else {
					if (line.matches(".*\\s\\w+?-\\w+?\\s.*")) {
						setLanguage1(line.replaceAll(".*\\s(\\w+?)-(\\w+?)\\s.*", "$1"));
						setLanguage2(line.replaceAll(".*\\s(\\w+?)-(\\w+?)\\s.*", "$2"));
					}
				}

				line = br.readLine();
			}

			br.close();

		} catch (Exception e) {
			MessageWindow.showErrorDialog("Error Loading dictionary from " + file, e);
		}

	}

	public ArrayList<String> getAllWords() {
		return new ArrayList<String>(wordMap.keySet());
	}

	public ArrayList<Entry> getExactEntries(String query) {
		Map<UUID, Integer> ids = new HashMap<UUID, Integer>();
		ArrayList<Entry> entries = new ArrayList<Entry>();
		for (String word : query.split("[\\s-]")) {
			Set<UUID> idList = wordMap.get(word.toLowerCase());
			if (idList != null) {
				for (UUID id : idList) {
					if (!ids.containsKey(id)) {
						ids.put(id, 0);
					}
					ids.put(id, ids.get(id) + 1);
				}
			}
		}

		for (UUID id : ids.keySet()) {
			Entry entry = entryMap.get(id);
			entry.setWordHit(ids.get(id));
			entries.add(entry);
		}

		Collections.sort(entries, new EntrySorter());

		return entries;
	}

	public ArrayList<Entry> getPartialMatchedEntries(String query) {
		Map<UUID, Integer> ids = new HashMap<UUID, Integer>();
		ArrayList<Entry> entries = new ArrayList<Entry>();

		Set<String> matchingWords = new HashSet<String>();
		for (String word : query.toLowerCase().split("[\\s-]")) {
			for (String key : wordMap.keySet()) {
				if (!key.equals(word)) {
					if (key.matches(".*" + word.toLowerCase()) || key.matches(word.toLowerCase() + ".*")) {
						matchingWords.add(key);
					}
				}
			}
		}

		for (String word : matchingWords) {
			Set<UUID> idList = wordMap.get(word.toLowerCase());
			if (idList != null) {
				for (UUID id : idList) {
					if (!ids.containsKey(id)) {
						ids.put(id, 0);
					}
					ids.put(id, ids.get(id) + 1);
				}
			}
		}

		for (UUID id : ids.keySet()) {
			Entry entry = entryMap.get(id);
			entry.setWordHit(ids.get(id));
			entries.add(entry);
		}

		Collections.sort(entries, new EntrySorter());

		return entries;
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
}
