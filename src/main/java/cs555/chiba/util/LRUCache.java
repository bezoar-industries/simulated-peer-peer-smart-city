package cs555.chiba.util;

import cs555.chiba.service.Identity;

import java.util.HashMap;
import java.util.UUID;

public class LRUCache {
	
	public class Entry {
		public Identity value;
		public HashMap<Identity, Integer> valueList;
		public String keyName;
		UUID key;
		int distance;
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
	
	@SuppressWarnings("unchecked")
	public HashMap<UUID, Entry> getHashmap(){
		return (HashMap<UUID, Entry>) hashmap.clone();
	}
	
	public synchronized boolean putEntryAppend(UUID key, String keyName, int distance, Identity value) {
		boolean updated = false;
		if (hashmap.containsKey(key)){
			Entry entry = hashmap.get(key);
			if(!entry.valueList.containsKey(value) || entry.distance > distance)
				updated = true;
			entry.valueList.put(value, Math.min(distance, entry.distance));
			removeNode(entry);
			addAtTop(entry);
		} else {
			updated = true;
			Entry newnode = new Entry();
			newnode.left = null;
			newnode.right = null;
			newnode.valueList = new HashMap<Identity, Integer>();
			newnode.valueList.put(value, distance);
			newnode.keyName = keyName;
			newnode.key = key;
			newnode.distance = distance;
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

	public synchronized boolean putEntry(UUID key, Identity value) {
		boolean newEntry = false;
		if (hashmap.containsKey(key)){
			Entry entry = hashmap.get(key);
			entry.value = value;
			removeNode(entry);
			addAtTop(entry);
		} else {
			newEntry = true;
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
		return newEntry;
	}
	
	public synchronized boolean putEntryWithProbability(UUID key, Identity value, String device, double probability) {
		boolean newEntry = false;
		if (hashmap.containsKey(key)){
			Entry entry = hashmap.get(key);
			entry.value = value;
			entry.keyName = device;
			removeNode(entry);
			addAtTop(entry);
		} else {
			newEntry = true;
			Entry newnode = new Entry();
			newnode.left = null;
			newnode.right = null;
			newnode.value = value;
			newnode.key = key;
			newnode.keyName = device;
			if (hashmap.size() > LRU_SIZE){
				if(Math.random() < probability)
					return false;
				hashmap.remove(end.key);
				removeNode(end);				
				addAtTop(newnode);
			} else {
				addAtTop(newnode);
			}

			hashmap.put(key, newnode);
		}
		return newEntry;
	}
	
	public HashMap<String,Integer> getValueLists(){
		HashMap<String,Integer> keyNames = new HashMap<String,Integer>();
		for(Entry e : hashmap.values()) {
			keyNames.put(e.keyName, e.distance);
		}
		return keyNames;
	}
	
	public HashMap<Identity,String> getLocations(){
		HashMap<Identity,String> locations = new HashMap<>();
		for(Entry e : this.hashmap.values()) {
			locations.put(e.value, e.keyName);
		}
		return locations;
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
