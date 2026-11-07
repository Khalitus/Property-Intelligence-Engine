package edu.shu.propertyintel.dsa;

import java.util.Objects;

public final class CustomHashTable<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private static final double MAX_LOAD_FACTOR = 0.75;

    private CustomLinkedList<Entry<K, V>>[] buckets;
    private int size;

    private static final class Entry<K, V> {
        private final K key;
        private V value;

        private Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public CustomHashTable() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public CustomHashTable(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        buckets = (CustomLinkedList<Entry<K, V>>[]) new CustomLinkedList<?>[capacity];
    }

    public void put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Entry<K, V> existing = findEntry(key);
        if (existing != null) {
            existing.value = value;
            return;
        }
        if ((size + 1) > buckets.length * MAX_LOAD_FACTOR) {
            resize();
        }
        addEntry(new Entry<>(key, value));
    }

    public V get(K key) {
        Objects.requireNonNull(key, "key");
        Entry<K, V> entry = findEntry(key);
        return entry == null ? null : entry.value;
    }

    public boolean containsKey(K key) {
        Objects.requireNonNull(key, "key");
        return findEntry(key) != null;
    }

    public int size() {
        return size;
    }

    private Entry<K, V> findEntry(K key) {
        CustomLinkedList<Entry<K, V>> bucket = buckets[indexFor(key)];
        if (bucket != null) {
            for (Entry<K, V> entry : bucket) {
                if (entry.key.equals(key)) {
                    return entry;
                }
            }
        }
        return null;
    }

    private int indexFor(K key) {
        return (key.hashCode() & 0x7fffffff) % buckets.length;
    }

    private void addEntry(Entry<K, V> entry) {
        int index = indexFor(entry.key);
        if (buckets[index] == null) {
            buckets[index] = new CustomLinkedList<>();
        }
        buckets[index].addLast(entry);
        size++;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        CustomLinkedList<Entry<K, V>>[] oldBuckets = buckets;
        buckets = (CustomLinkedList<Entry<K, V>>[]) new CustomLinkedList<?>[oldBuckets.length * 2];
        size = 0;
        for (CustomLinkedList<Entry<K, V>> bucket : oldBuckets) {
            if (bucket != null) {
                for (Entry<K, V> entry : bucket) {
                    addEntry(entry);
                }
            }
        }
    }
}
