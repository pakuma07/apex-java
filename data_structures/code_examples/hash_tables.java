// hash_tables.java
// Demonstrates a hash table with separate chaining (one linked list per bucket)
// and contrasts it with the JDK's HashMap.

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class hash_tables {

    // A (key,value) pair stored in a bucket chain.
    static class Entry {
        int key, value;
        Entry(int k, int v) { key = k; value = v; }
    }

    // Hash table using separate chaining: a fixed array of buckets, each a list of
    // (key,value) pairs. Collisions land in the same bucket's chain.
    // Average insert/find O(1); worst case O(n) if all keys hash to one bucket.
    static class HashTable {
        private static final int SIZE = 11;                 // prime bucket count reduces clustering
        private final List<LinkedList<Entry>> table;        // table[i] = chain of entries for bucket i

        // Maps a key to a bucket index via modulo. O(1).
        private int hashFunc(int key) {
            return key % SIZE;
        }

        HashTable() {
            table = new ArrayList<>(SIZE);
            for (int i = 0; i < SIZE; ++i) table.add(new LinkedList<>());  // allocate SIZE empty buckets
        }

        // Insert or update. Hash to a bucket, scan its chain: if the key exists
        // overwrite the value, otherwise append a new pair. O(1) average.
        void insert(int key, int value) {
            int idx = hashFunc(key);
            for (Entry kv : table.get(idx)) {     // walk the collision chain
                if (kv.key == key) {
                    kv.value = value;             // key present: update in place
                    return;
                }
            }
            table.get(idx).add(new Entry(key, value));  // key absent: add to chain
        }

        // Look up by key; on hit, return the value boxed in a result, else null.
        // O(1) average (proportional to chain length).
        Integer find(int key) {
            int idx = hashFunc(key);
            for (Entry kv : table.get(idx)) {  // scan only the relevant bucket
                if (kv.key == key) {
                    return kv.value;
                }
            }
            return null;
        }

        // Dump every bucket and its chain, for visualizing collisions. O(SIZE + entries).
        void printBuckets() {
            for (int i = 0; i < SIZE; ++i) {
                System.out.print("bucket[" + i + "] : ");
                for (Entry kv : table.get(i)) {
                    System.out.print("(" + kv.key + "," + kv.value + ") ");
                }
                System.out.println();
            }
        }
    }

    public static void main(String[] a) {
        System.out.println("== Custom Hash Table (Chaining) ==");
        HashTable ht = new HashTable();
        ht.insert(1, 100);
        ht.insert(12, 1200); // collision with key 1 for SIZE=11
        ht.insert(23, 2300); // collision chain

        Integer value = ht.find(12);
        if (value != null) {
            System.out.println("Found key 12: " + value);
        }
        ht.printBuckets();

        System.out.println("\n== JDK HashMap ==");
        // HashMap is a production hash table; here used as a frequency counter.
        // getOrDefault lets us count occurrences without a separate "contains" check.
        // (A LinkedHashMap is used so iteration order is deterministic for the demo.)
        Map<String, Integer> freq = new LinkedHashMap<>();
        freq.put("apple", freq.getOrDefault("apple", 0) + 1);
        freq.put("banana", freq.getOrDefault("banana", 0) + 2);
        freq.put("apple", freq.getOrDefault("apple", 0) + 1);

        for (Map.Entry<String, Integer> kv : freq.entrySet()) {
            System.out.println(kv.getKey() + " -> " + kv.getValue());
        }
    }
}
