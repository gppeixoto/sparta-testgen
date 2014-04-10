package replayer;

import java.util.HashSet;
import java.util.Set;

public class Heap {

	/**
	 * Fields
	 */
	private Set<HeapCell> object = new HashSet<HeapCell>();

	public HeapCell newCell() {
		HeapCell res = new HeapCell();
		object.add(res);
		return res;
	}
		
}
