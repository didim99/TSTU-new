package ru.didim99.tstu.core.translator.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by didim99 on 05.11.18.
 */
class HashTable<K, V> {
  private static final int HASH_TABLE_SIZE = 16;
  private static final int EMPTY_HASH = -1;
  private static final int EMPTY_CHAIN = 0;

  private int[] hashTable;
  private ArrayList<Entry<K, V>> entries;

  HashTable() {
    hashTable = new int[HASH_TABLE_SIZE];
    Arrays.fill(hashTable, EMPTY_HASH);
    entries = new ArrayList<>();
  }

  void add(K key, V value) {
    if (containsKey(key)) return;
    int hash = hashOf(key);

    if (hashTable[hash] == EMPTY_HASH) {
      hashTable[hash] = entries.size();
    } else {
      Entry<K, V> e = entries.get(hashTable[hash]);
      while (e.chain != EMPTY_CHAIN)
        e = entries.get(e.chain);
      e.chain = entries.size();
    }

    entries.add(new Entry<>(key, value));
  }

  public V get(int index) {
    return entries.get(index).value;
  }

  int indexOfKey(K key) {
    int hash = hashOf(key);
    if (hashTable[hash] != EMPTY_HASH) {
      int index = hashTable[hash];
      Entry<K, V> e;

      do {
        e = entries.get(index);
        if (e.getKey().equals(key))
          return index;
        index = e.chain;
      } while (e.chain != EMPTY_CHAIN);
    }

    return EMPTY_HASH;
  }

  ArrayList<Entry<K, V>> getEntries() {
    return entries;
  }

  private boolean containsKey(K key) {
    return indexOfKey(key) >= 0;
  }

  private int hashOf(K key) {
    return Math.abs(key.hashCode() % HASH_TABLE_SIZE);
  }

  @Override
  public String toString() {
    if (entries.isEmpty())
      return "Empty HashTable";
    StringBuilder sb = new StringBuilder();
    sb.append(String.format(Locale.US, "HashTable (size: %d)\n", entries.size()));
    sb.append("\nHash table:\n");
    sb.append("hash  index\n");
    for (int i = 0; i < hashTable.length; i++) {
      if (hashTable[i] != EMPTY_HASH) {
        sb.append(String.format(Locale.US,
          "%4d: %d\n", i, hashTable[i]));
      }
    }

    sb.append("\nEntries table:\n");
    sb.append("index  chain  value\n");
    for (int i = 0; i < entries.size(); i++) {
      Entry e = entries.get(i);
      sb.append(String.format(Locale.US, "%5d  %5d: %s\n",
        i, e.chain, e.value == this ? "This table" : e.value.toString()));
    }

    return sb.toString();
  }

  static class Entry<K, V> {
    private K key;
    private V value;
    private int chain;

    Entry(K key, V value) {
      this.key = key;
      this.value = value;
      this.chain = EMPTY_CHAIN;
    }

    K getKey() { return key; }
    V getValue() { return value; }
  }
}
