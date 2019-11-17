package cs555.chiba.util;

import cs555.chiba.service.Identity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class LRUCache {
	
	public class Entry {
		Identity value;
		public HashSet<Identity> valueList;
		String keyName;
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

	public Entry getEntry(UUID key) {
		if (hashmap.containsKey(key)) {
			Entry entry = hashmap.get(key);
			removeNode(entry);
			addAtTop(entry);
			return entry;
		}
		return new Entry();
	}
	
	public boolean containsEntry(UUID key) {
		return hashmap.containsKey(key);
	}
	
	public boolean putEntryAppend(UUID key, String keyName, Identity value) {
		boolean updated = false;
		if (hashmap.containsKey(key)){
			Entry entry = hashmap.get(key);
			if(!entry.valueList.contains(value))
				updated = true;
			entry.valueList.add(value);
			removeNode(entry);
			addAtTop(entry);
		} else {
			updated = true;
			Entry newnode = new Entry();
			newnode.left = null;
			newnode.right = null;
			newnode.valueList = new HashSet<Identity>();
			newnode.valueList.add(value);
			newnode.keyName = keyName;
			newnode.key = key;
			if (hashmap.size() > LRU_SIZE) {
				hashmap.remove(end.key);
				removeNode(end);				
				addAtTop(newnode);

			} else {
				addAtTop(newnode);
			}

			hashmap.put(key, newnode);
		}
		return updated;
	}

	public void putEntry(UUID key, Identity value) {
		if (hashmap.containsKey(key)){
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
			if (hashmap.size() > LRU_SIZE){
				hashmap.remove(end.key);
				removeNode(end);				
				addAtTop(newnode);

			} else {
				addAtTop(newnode);
			}

			hashmap.put(key, newnode);
		}
	}
	
	public String[] getValueLists(){
		String[] keyNames = new String[hashmap.size()];
		int i = 0;
		for(Entry e : hashmap.values()) {
			keyNames[i] = e.keyName;
			i++;
		}
		return keyNames;
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
