package ru.didim99.tstu.core.itheory.compression.utils;

/**
 * Created by didim99 on 29.02.20.
 */
public class HuffmanTreeEntry {
  // relations
  private HuffmanTreeEntry parent;
  private HuffmanTreeEntry leftChild;
  private HuffmanTreeEntry rightChild;
  // tree data
  private boolean isLeaf;
  private String charSet;
  private char character;
  private int weight;

  HuffmanTreeEntry(char c, int count) {
    this.character = c;
    this.weight = count;
    this.isLeaf = true;
    this.charSet = computeCharSet();
  }

  HuffmanTreeEntry(HuffmanTreeEntry leftChild, HuffmanTreeEntry rightChild) {
    this.leftChild = leftChild;
    this.rightChild = rightChild;
    leftChild.parent = this;
    rightChild.parent = this;
    this.isLeaf = false;
    this.charSet = computeCharSet();
    this.weight = computeWeight();
  }

  HuffmanTreeEntry getLeftChild() {
    return leftChild;
  }

  HuffmanTreeEntry getRightChild() {
    return rightChild;
  }

  public HuffmanTreeEntry getChild(boolean bit) {
    return bit ? rightChild : leftChild;
  }

  public boolean isLeaf() {
    return isLeaf;
  }

  public char getCharacter() {
    return character;
  }

  int getWeight() {
    return weight;
  }

  boolean hasParent() {
    return parent != null;
  }

  boolean contains(char c) {
    return charSet.indexOf(c) > -1;
  }

  private int computeWeight() {
    if (isLeaf) return weight;
    else return leftChild.computeWeight() + rightChild.computeWeight();
  }

  private String computeCharSet() {
    if (isLeaf) return String.valueOf(character);
    else return leftChild.computeCharSet().concat(rightChild.computeCharSet());
  }

  @Override
  public String toString() {
    return "TreeEntry [" + charSet + ":" + weight + "]";
  }

  static int weightCompare(HuffmanTreeEntry e1, HuffmanTreeEntry e2) {
    int c = Integer.compare(e1.computeWeight(), e2.computeWeight());
    if (c == 0) c = Integer.compare(e1.charSet.length(), e2.charSet.length());
    if (c == 0) c = e1.charSet.compareTo(e2.charSet);
    return c;
  }
}
