package edu.shu.propertyintel.dsa;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AVLTree<K extends Comparable<? super K>, V> {
    private Node<K, V> root;
    private int valueCount;
    private int keyCount;

    private static final class Node<K, V> {
        private final K key;
        private final CustomLinkedList<V> values = new CustomLinkedList<>();
        private Node<K, V> left;
        private Node<K, V> right;
        private int height = 1;

        private Node(K key, V value) {
            this.key = key;
            values.addLast(value);
        }
    }

    public void insert(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        root = insert(root, key, value);
        valueCount++;
    }

    private Node<K, V> insert(Node<K, V> node, K key, V value) {
        if (node == null) {
            keyCount++;
            return new Node<>(key, value);
        }
        int comparison = key.compareTo(node.key);
        if (comparison < 0) {
            node.left = insert(node.left, key, value);
        } else if (comparison > 0) {
            node.right = insert(node.right, key, value);
        } else {
            node.values.addLast(value);
            return node;
        }

        updateHeight(node);
        int balance = balance(node);
        if (balance > 1) {
            if (key.compareTo(node.left.key) > 0) {
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        }
        if (balance < -1) {
            if (key.compareTo(node.right.key) < 0) {
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }
        return node;
    }

    public List<V> find(K key) {
        Objects.requireNonNull(key, "key");
        Node<K, V> current = root;
        while (current != null) {
            int comparison = key.compareTo(current.key);
            if (comparison == 0) {
                return copyValues(current);
            }
            current = comparison < 0 ? current.left : current.right;
        }
        return List.of();
    }

    public List<V> rangeSearch(K minimum, K maximum) {
        Objects.requireNonNull(minimum, "minimum");
        Objects.requireNonNull(maximum, "maximum");
        if (minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("minimum must not exceed maximum");
        }
        List<V> matches = new ArrayList<>();
        collectRange(root, minimum, maximum, matches);
        return matches;
    }

    private void collectRange(Node<K, V> node, K minimum, K maximum, List<V> matches) {
        if (node == null) {
            return;
        }
        int fromMin = minimum.compareTo(node.key);
        int toMax = maximum.compareTo(node.key);
        if (fromMin < 0) {
            collectRange(node.left, minimum, maximum, matches);
        }
        if (fromMin <= 0 && toMax >= 0) {
            for (V value : node.values) {
                matches.add(value);
            }
        }
        if (toMax > 0) {
            collectRange(node.right, minimum, maximum, matches);
        }
    }

    private static <K, V> List<V> copyValues(Node<K, V> node) {
        List<V> values = new ArrayList<>();
        for (V value : node.values) {
            values.add(value);
        }
        return values;
    }

    public int size() {
        return valueCount;
    }

    public int distinctKeys() {
        return keyCount;
    }

    public int height() {
        return height(root);
    }

    public K rootKey() {
        return root == null ? null : root.key;
    }

    private static <K, V> int height(Node<K, V> node) {
        return node == null ? 0 : node.height;
    }

    private static <K, V> void updateHeight(Node<K, V> node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    private static <K, V> int balance(Node<K, V> node) {
        return height(node.left) - height(node.right);
    }

    private Node<K, V> rotateRight(Node<K, V> top) {
        Node<K, V> newTop = top.left;
        top.left = newTop.right;
        newTop.right = top;
        updateHeight(top);
        updateHeight(newTop);
        return newTop;
    }

    private Node<K, V> rotateLeft(Node<K, V> top) {
        Node<K, V> newTop = top.right;
        top.right = newTop.left;
        newTop.left = top;
        updateHeight(top);
        updateHeight(newTop);
        return newTop;
    }
}
