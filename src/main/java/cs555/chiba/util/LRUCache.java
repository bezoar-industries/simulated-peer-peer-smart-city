package cs555.chiba.util;

import java.util.HashMap;
import java.util.UUID;

public class LRUCache {
	
	class Entry {
		int value;
		UUID key;
		Entry left;
		Entry right;
	}
	
	HashMap<UUID, Entry> hashmap;
	Entry start, end;
	int LRU_SIZE; 

	public LRUCache(int size) {
		hashmap = new HashMap<UUID, Entry>();
		LRU_SIZE = size;
	}

	public int getEntry(UUID key) {
		if (hashmap.containsKey(key)) 
		{
			Entry entry = hashmap.get(key);
			removeNode(entry);
			addAtTop(entry);
			return entry.value;
		}
		return -1;
	}
	
	public boolean containsEntry(UUID key) {
		return hashmap.containsKey(key);
	}

	public void putEntry(UUID key, int value) {
		if (hashmap.containsKey(key))
		{
			Entry entry = hashmap.get(key);
			entry.value = value;
			removeNode(entry);
			addAtTop(entry);
		} else {
			Entry newnode = new Entry();
			newnode.left = null;
			newnode.right = null;
			newnode.value = value;
			newnode.key = key;
			if (hashmap.size() > LRU_SIZE)
			{
				hashmap.remove(end.key);
				removeNode(end);				
				addAtTop(newnode);

			} else {
				addAtTop(newnode);
			}

			hashmap.put(key, newnode);
		}
	}
	
	private void addAtTop(Entry node) {
		node.right = start;
		node.left = null;
		if (start != null)
			start.left = node;
		start = node;
		if (end == null)
			end = start;
	}

	private void removeNode(Entry node) {

		if (node.left != null) {
			node.left.right = node.right;
		} else {
			start = node.right;
		}

		if (node.right != null) {
			node.right.left = node.left;
		} else {
			end = node.left;
		}
	}
}
