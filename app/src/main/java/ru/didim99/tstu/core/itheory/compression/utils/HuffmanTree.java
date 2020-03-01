package ru.didim99.tstu.core.itheory.compression.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Created by didim99 on 29.02.20.
 */
class HuffmanTree {
  private ArrayList<HuffmanTreeEntry> leafs;
  private HuffmanTreeEntry root;

  HuffmanTree(Map<Character, Integer> frequencyMap) {
    this.root = buildTree(frequencyMap);
  }

  ArrayList<HuffmanTreeEntry> getLeafs() {
    return leafs;
  }

  HuffmanTreeEntry getRoot() {
    return root;
  }

  /**
   * Get symbol code represented by this tree.
   */
  ArrayList<Boolean> getCharCode(char c) {
    if (!root.contains(c)) return null;
    ArrayList<Boolean> code = new ArrayList<>();
    if (root.isLeaf()) code.add(false);
    HuffmanTreeEntry entry = root;

    while (true) {
      if (entry.isLeaf()) return code;
      HuffmanTreeEntry left = entry.getLeftChild();
      if (left.contains(c)) {
        code.add(false);
        entry = left;
      } else {
        code.add(true);
        entry = entry.getRightChild();
      }
    }
  }

  /**
   * Builds a Huffman tree and returns their root.
   */
  private HuffmanTreeEntry buildTree(Map<Character, Integer> frequencyMap) {
    ArrayList<HuffmanTreeEntry> tree = new ArrayList<>(frequencyMap.size());
    for (Map.Entry<Character, Integer> e : frequencyMap.entrySet())
      tree.add(new HuffmanTreeEntry(e.getKey(), e.getValue()));
    this.leafs = new ArrayList<>(tree);
    if (tree.size() < 2) return tree.get(0);

    while (true) {
      HuffmanTreeEntry e1 = null, e2 = null;
      Collections.sort(tree, HuffmanTreeEntry::weightCompare);
      for (HuffmanTreeEntry e : tree) {
        if (!e.hasParent()) {
          if (e1 == null) {
            e1 = e;
          } else {
            e2 = e;
            break;
          }
        }
      }

      if (e2 == null) return e1;
      tree.add(new HuffmanTreeEntry(e1, e2));
    }
  }
}
