package dictionary;

import java.util.Comparator;

public class EntrySorter implements Comparator<Entry> {
	@Override
	public int compare(Entry o1, Entry o2) {

		Integer c1 = o1.getRelevance();
		Integer c2 = o2.getRelevance();

		return c2.compareTo(c1);
	}

}