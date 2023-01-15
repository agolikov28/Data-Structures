import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class QuakeHeap<Key extends Comparable<Key>, Value> {

	// references to nodes at level lev
	private List<LinkedList<Node>> roots;

	// number of nodes at level lev
	private List<Integer> nodeCt;

	private int nLevels;
	private double currRatio;

	class Node {
		protected Key key;
		protected Value value;
		protected int level;
		protected Node parent;
		protected Node leftChild;
		protected Node rightChild;
		protected Locator r;

		// default constructor
		public Node() {

		}

		public Node(Key key, Value value) {
			this.key = key;
			this.value = value;
			this.level = 0;
		}

		public Node(Key key, Value value, Node parent, int level, Node lc, Node rc) {
			this.key = key;
			this.value = value;
			this.parent = parent;
			this.leftChild = lc;
			this.rightChild = rc;
			this.level = level;
		}

		public int getLevel() {
			return level;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		public Key getKey() {
			return key;
		}

		public void setKey(Key key) {
			this.key = key;
		}

		public Value getVal() {
			return value;
		}

		public Node getParent() {
			return parent;
		}

		public void setParent(Node parent) {
			this.parent = parent;
		}

		public Node getLeftChild() {
			return leftChild;
		}

		public void setLeftChild(Node leftChild) {
			this.leftChild = leftChild;
		}

		public Node getRightChild() {
			return rightChild;
		}

		public void setRightChild(Node rightChild) {
			this.rightChild = rightChild;
		}

		public Locator getLocator() {
			return r;
		}

		public void setLocator(Locator r) {
			this.r = r;
		}

		@Override
		public String toString() {
			String finStr = " Level: " + this.level;
			finStr += " Key: " + this.key;
			finStr += " Value: " + this.value;
			finStr += " Parent: " + this.parent;
			return finStr;
		}

	}

	public class Locator {
		private Node u;

		private Locator(Node u) {
			this.u = u;
			u.setLocator(this);
		}

		private Node get() {
			return u;
		}
	}

	public QuakeHeap(int nLevels) {
		this.roots = new ArrayList<>(nLevels);
		this.nodeCt = new ArrayList<>(nLevels);
		this.nLevels = nLevels;
		this.currRatio = 0.75;
		for (int i = 0; i < nLevels; i++) {
			this.roots.add(new LinkedList<>());
			this.nodeCt.add(0);
		}
	}

	public void clear() {

		for (int i = 0; i < nodeCt.size(); i++) {
			this.roots.set(i, new LinkedList<>());
			this.nodeCt.set(i, 0);
		}

	}

	public Locator insert(Key x, Value v) {
		Node u = trivialTree(x, v);
		return new Locator(u);
	}

	public int getMaxLevel(Locator r) {
		Node u = r.get();
		Key x = r.get().key;
		int finLevel = 0;
		Node uChild = null;
		do {
			u.setKey(x);
			uChild = u;
			u = u.parent;
			if (u != null && u.key == x && u.level > finLevel) {
				finLevel = u.level;
			}

		} while (u != null && uChild == u.leftChild);

		return finLevel;
	}

	public Key getMinKey() throws Exception {
		Node u = findRootWithSmallestKey(); // find the min root
		if (u == null) { // heap is empty
			throw new Exception("Empty heap");
		}
		mergeTrees(); // merge trees
		return u.key;
	}

	Node findRootWithSmallestKey() throws Exception {
		if (!isArrEmpty()) {
			Node min = null;
			for (int lev = 0; lev < nLevels; lev++) { // process all levels
				for (Node u : roots.get(lev)) {
					if (min == null) {
						min = u;
					} else if (u.key.compareTo(min.key) < 0) {
						min = u;
					}
				}
			}
			return min;
		} else {
			throw new Exception("Empty heap");
		}
	}

	public ArrayList<String> listHeap() {
		ArrayList<String> list = new ArrayList<String>();
		for (int lev = 0; lev < nLevels; lev++) {
			if (nodeCt.get(lev) > 0) {
				list.add("{lev: " + lev + " nodeCt: " + nodeCt.get(lev) + "}");
			}
			if (roots.get(lev).size() > 0) { // has at least one root?
				Collections.sort(roots.get(lev), new nodeComparator()); // sort roots by key
				for (Node u : roots.get(lev)) {
					list.addAll(internalNode(u));
				}
			}
		}
		return list;
	}

	private ArrayList<String> internalNode(Node u) {
		ArrayList<String> list = new ArrayList<String>();
		if (u == null) {
			list.add("[null]");
		} else if (u.level > 0) {
			list.add("(" + u.key + ")");
			list.addAll(internalNode(u.leftChild));
			list.addAll(internalNode(u.rightChild));
		} else {
			list.add("[" + u.key + " " + u.value + "]");
		}
		return list;
	}

	private void makeRoot(Node u) {
		u.setParent(null);
		roots.get(u.level).add(u);
	}

	private Node trivialTree(Key x, Value v) {
		Node u = new Node(x, v);
		nodeCt.set(0, nodeCt.get(0) + 1);
		makeRoot(u);

		return u;
	}

	private void deleteLeftPath(Node u) {
		while (u != null) {
			cut(u);
			nodeCt.set(u.level, nodeCt.get(u.level) - 1);
			u = u.leftChild;
		}

	}

	private void cut(Node w) {
		Node v = w.rightChild;
		if (v != null) {
			w.rightChild = null;
			makeRoot(v);
		}
	}

	private void cut2(Node n) {
		Node parent = n.parent;
		if (parent != null) {
			parent.rightChild = null;
			n.parent = null;
			this.roots.get(n.getLevel()).add(n);
		}
	}

	private void clearHelp(int minLevel, Node n) {
		if (n == null) {
			return;
		}
		Node left = n.leftChild;
		Node right = n.rightChild;
		if (n.level == minLevel) {
			makeRoot(n);
		} else {

			clearHelp(minLevel, left);
			clearHelp(minLevel, right);
			n.parent = null;
			n.leftChild = null;
			n.rightChild = null;
		}

	}

	private void clearAllAbove(int lev) {
		for (int i = nLevels - 1; i >= lev + 1; i--) {
			Collections.sort(roots.get(i), new nodeComparator()); // sort roots by key
			for (Node u : roots.get(i)) {
				clearHelp(lev, u);
			}
			nodeCt.set(i, 0);
			roots.set(i, new LinkedList<Node>());
		}
	}

	private void quake() {
		for (int i = 0; i <= nLevels - 2; i++) {
			if (nodeCt.get(i + 1) > (this.currRatio * nodeCt.get(i))) {
				clearAllAbove(i);
			}
		}
	}

	private Node link(Node u, Node v) {
		assert (u.level == v.level);
		Node w;
		int lev = u.level + 1;
		if (u.key.compareTo(v.key) <= 0) {
			w = new Node(u.key, u.value, null, lev, u, v);
		} else {
			w = new Node(v.key, v.value, null, lev, v, u);
		}

		nodeCt.set(lev, nodeCt.get(lev) + 1);
		u.parent = v.parent = w;
		return w;
	}

	private void mergeTrees() {
		for (int lev = 0; lev < nLevels - 1; lev++) {
			// sort nodes
			Collections.sort(roots.get(lev), new nodeComparator());
			// while roots[k] has at least 2 roots
			while (roots.get(lev).size() >= 2) {
				Node u = roots.get(lev).remove();
				Node v = roots.get(lev).remove();
				Node w = link(u, v);
				makeRoot(w);
			}
		}
	}

	public void decreaseKey(Locator r, Key newKey) throws Exception {
		Node u = r.get();
		Node uChild = null;
		// setting u to root node
		if (newKey.compareTo(u.getKey()) <= 0) {
			do {
				u.key = newKey;
				u.setKey(newKey);
				uChild = u;
				u = u.parent;
			} while (u != null && uChild == u.leftChild);
			// if not root, we cut
			if (u != null) {
				cut(u);
			}
		} else {
			throw new Exception("Invalid key for decrease-key");
		}
	}

	public Value extractMin() throws Exception {
		if (!(isArrEmpty())) {
			Node u = findRootWithSmallestKey();
			Value result = u.value;
			deleteLeftPath(u);
			roots.get(u.level).remove(u);
			mergeTrees();
			quake();
			return result;
		} else {
			throw new Exception("Empty heap");
		}
	}

	public int size() {
		return nodeCt.get(0);
	}

	public void setQuakeRatio(double newRatio) throws Exception {
		if (newRatio < 0.5 || newRatio > 1.0) {
			throw new Exception("Quake ratio is outside valid bounds");
		}
		this.currRatio = newRatio;
	}

	public void setNLevels(int nl) throws Exception {
		if (nl < 1) {
			throw new Exception("Attempt to set an invalid number of levels");
		}
		if (nl > this.nLevels) {
			List<LinkedList<Node>> newRoots = new ArrayList<>(nl);
			List<Integer> newNodeCt = new ArrayList<>(nl);

			for (int i = 0; i <= nl - nLevels; i++) {
				newRoots.add(roots.get(i));
				newNodeCt.add(nodeCt.get(i));
			}

			for (int j = (nl - nLevels) + 1; j < nl; j++) {
				newRoots.add(new LinkedList<>());
				newNodeCt.add(0);
			}

			this.roots = newRoots;
			this.nodeCt = newNodeCt;

		} else if (nl < this.nLevels) {
			clearAllAbove(nl - 1);

			for (int i = nLevels - 1; i <= nl; i--) {
				this.roots.remove(i);
				this.nodeCt.remove(i);
			}
		}
		this.nLevels = nl;

	}

	private boolean isArrEmpty() {
		return nodeCt.get(0) == 0;
	}

	// comparator class for sorting
	private class nodeComparator implements Comparator<Node> {
		public int compare(Node u, Node v) {
			if (u == null) {
				return (v == null) ? 0 : -1;
			}
			if (v == null) {
				return (u == null) ? 0 : 1;
			}
			return u.key.compareTo(v.key);

		}
	}

	public ArrayList<String> printWholeTree() {
		ArrayList<String> list = new ArrayList<String>();
		for (int lev = 0; lev < nLevels; lev++) {
			if (nodeCt.get(lev) > 0) {
				list.add("\n{lev: " + lev + " nodeCt: " + nodeCt.get(lev) + "}");
			}
			if (roots.get(lev).size() > 0) { // has at least one root?
				Collections.sort(roots.get(lev), new nodeComparator()); // sort roots by key
				for (Node u : roots.get(lev)) {
					list.addAll(printHelp(u));
				}
			}
		}
		return list;
	}

	private ArrayList<String> printHelp(Node u) {
		ArrayList<String> list = new ArrayList<String>();

		if (u == null) {
			list.add("[null]");
		} else if (u.level > 0) {
			list.add("level: " + u.level + " ");

			list.add("(" + u.key + ")");
			list.addAll(printHelp(u.leftChild));
			list.addAll(printHelp(u.rightChild));
		} else {
			list.add("level: " + u.level + " ");

			list.add("[" + u.key + " " + u.value + "]");
		}
		return list;
	}
}
