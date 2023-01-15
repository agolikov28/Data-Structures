import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HeightBalancedkdTree<LPoint extends LabeledPoint2D> {
	private class KDNode { // node in a kd-tree
		private LPoint point; // splitting point
		private int cutDim; // cutting dimension
		private float cutVal; //
		private int height; // height of tree
		private KDNode left; // left child
		private KDNode right; // right child

		KDNode(LPoint point, float cutVal, int cutDim) { // constructor
			this.point = point;
			this.cutDim = cutDim;
			this.cutVal = cutVal;
			this.height = -1;
			left = right = null;
		}

		boolean inLeftSubtree(LPoint pt) { // is pt in left subtree?
			return pt.get(cutDim) < point.get(cutDim);
		}

		LPoint getPoint() {
			return this.point;
		}

		void setPoint(LPoint newPT) {
			this.point = newPT;
		}

		void setNewVals(int newDim, float newCut) {
			this.cutDim = newDim;
			this.cutVal = newCut;
		}

		@Override
		public String toString() {
			if (cutDim == 0) {
				return "(x=" + cutVal + " ht=" + height + ") " + point.toString();
			} else {
				return "(y=" + cutVal + " ht=" + height + ") " + point.toString();
			}
		}

		LPoint find(Point2D pt) {
			if (pt.equals(this.point.getPoint2D())) {
				return this.point;
			} else if (cutDim == 0) {
				if (pt.getX() < cutVal && left != null) {
					return left.find(pt);
				} else if (right != null) {
					return right.find(pt);
				}
			} else {
				if (pt.getY() < cutVal && left != null) {
					return left.find(pt);
				} else if (right != null) {
					return right.find(pt);
				}
			}
			return null;
		}
	}

	private Rectangle2D bbox;
	private int maxHeightD;
	private int size;
	private KDNode root;
	public int DUPLICATE = 28;

	public HeightBalancedkdTree(int maxHeightDifference, Rectangle2D bbox) {
		this.bbox = bbox;
		this.maxHeightD = maxHeightDifference;
		this.size = 0;
	}

	public void setBBox(Rectangle2D bbox) {
		this.bbox = bbox;
	}

	public LPoint find(Point2D pt) {
		if (root == null) {
			return null;
		} else {
			return root.find(pt);
		}
	}

	public void insert(LPoint pt) throws Exception {
		Point2D currPt = pt.getPoint2D();

		if (!(bbox.contains(currPt))) {
			throw new Exception("Attempt to insert a point outside bounding box");
		} else {
			root = insert(pt, root, bbox);
			this.size++;
		}
	}

	private KDNode insert(LPoint pt, KDNode p, Rectangle2D cell) throws Exception {
		float midPoint = 0;
		double height = cell.getWidth(1);
		double width = cell.getWidth(0);
		int cutDim = 0;
		if (height > width) {
			cutDim = 1;
		}

		if (cutDim == 0) {
			midPoint = (float) ((pt.getX() + pt.getX()) / 2);
		} else {
			midPoint = (float) ((pt.getY() + pt.getY()) / 2);
		}

		// fell out of tree
		if (p == null) {
			// create new leaf
			p = new KDNode(pt, midPoint, cutDim);
		}
		// duplicate pt
		else if (p.point.getX() == pt.getX() && p.point.getY() == pt.getY()) {
			// throw exception
			throw new Exception("Attempt to insert a duplicate point");
		}

		// get left part and right part using utility functions
		else if (p.inLeftSubtree(pt)) { // insert into left subtree
			p.left = insert(pt, p.left, cell.leftPart(p.cutDim, p.point.get(cutDim)));
		} else { // insert into right subtree
			p.right = insert(pt, p.right, cell.rightPart(p.cutDim, p.point.get(cutDim)));
		}
		return rebalance(p, cell);
	}

	int height(KDNode p) {
		return p == null ? -1 : p.height;
	}

	void updateHeight(KDNode p) {
		p.height = 1 + Math.max(height(p.left), height(p.right));
	}

	int balanceFactor(KDNode p) {
		return height(p.right) - height(p.left);
	}

	public class ByX implements Comparator<KDNode> {
		public int compare(KDNode u, KDNode v) {
			double uX = u.getPoint().getX();
			double vX = v.getPoint().getX();
			if (uX < vX)
				return -1;
			else if (uX == vX) {
				double uY = u.getPoint().getY();
				double vY = v.getPoint().getY();
				if (uY < vY)
					return -1;
				else if (uY == vY)
					return DUPLICATE;
				else
					return +1;
			} else
				return +1;
		}
	}

	public class ByY implements Comparator<KDNode> {
		public int compare(KDNode u, KDNode v) {
			double uY = u.getPoint().getY();
			double vY = v.getPoint().getY();
			if (uY < vY)
				return -1;
			else if (uY == vY) {
				double uX = u.getPoint().getX();
				double vX = v.getPoint().getX();
				if (uX < vX)
					return -1;
				else if (uX == vX)
					return DUPLICATE;
				else
					return +1;

			} else
				return +1;
		}
	}

	KDNode buildSubtree(List<KDNode> A, Rectangle2D cell) {
		int k = A.size();
		float midPoint = 0;
		double height = cell.getWidth(1);
		double width = cell.getWidth(0);

		int cutDim = 0;
		if (height > width) {
			cutDim = 1;
		}

		if (k == 0) {
			return null;
		} else {

			// vertical (sort by x)
			if (cutDim == 0) {
				Collections.sort(A, new ByX());
			}
			// horizontal (sort by y)
			else {
				Collections.sort(A, new ByY());
			}
			double convert = Math.floor(k / 2); // median of array
			int j = (int) convert;
			KDNode p = new KDNode(null, j, j);
			p = A.get(j); // root node

			if (cutDim == 0) {
				midPoint = (float) ((p.getPoint().getX() + p.getPoint().getX()) / 2);
				p.setNewVals(cutDim, midPoint);
			} else {
				midPoint = (float) ((p.getPoint().getY() + p.getPoint().getY()) / 2);
				p.setNewVals(cutDim, midPoint);
			}
			p.left = buildSubtree(A.subList(0, j), cell.leftPart(p.cutDim, p.point.get(cutDim)));
			p.right = buildSubtree(A.subList(j + 1, k), cell.rightPart(p.cutDim, p.point.get(cutDim)));
			updateHeight(p);
			return p; // return root of the subtree
		}
	}

	public ArrayList<KDNode> makeNodeList(KDNode curr) {
		ArrayList<KDNode> list = new ArrayList<KDNode>();
		if (curr == null) {
			return list;
		} else {
			list.add(curr);
			list.addAll(makeNodeList(curr.left));
			list.addAll(makeNodeList(curr.right));
		}
		return list;
	}

	KDNode rebalance(KDNode p, Rectangle2D cell) {
		updateHeight(p);
		int bf = balanceFactor(p);
		if (p == null) {
			return p; // null - nothing to do
		}

		if (bf > maxHeightD || bf < (-maxHeightD)) {
			List<KDNode> cutArray = makeNodeList(p);
			return buildSubtree(cutArray, cell);
		}
		return p;
	}

	LPoint findMin(KDNode p, int i) throws Exception { // get min point along dim i
		if (p == null) {
			return null; // fell out of tree?
		}
		if (p.cutDim == i) { // cutting dimension matches i?
			if (p.left == null) { // no left child?
				return p.point; // use this point
			} else {
				return findMin(p.left, i); // get min from left subtree
			}
		} else { // it may be in either side
			return minNode(p.point, findMin(p.left, i), findMin(p.right, i), i);
		}
	}

	private LPoint minNode(LPoint x, LPoint y, LPoint z, int cd) {
		LPoint res = x;
		if (cd == 0) {
			if (y != null && y.getX() < res.getX())
				res = y;
			if (z != null && z.getX() < res.getX())
				res = z;
		} else {
			if (y != null && y.getY() < res.getY())
				res = y;
			if (z != null && z.getY() < res.getY())
				res = z;
		}
		return res;
	}

	public void delete(Point2D pt) throws Exception {
		if (root == null) {
			throw new Exception("Attempt to delete a nonexistent point");
		} else {
			if (size == 1) {
				clear();
			} else {
				LPoint found = find(pt);
				root = delete(found, root, bbox);
				rebalance(root, bbox);
				this.size--;
			}
		}
	}

	KDNode delete(LPoint pt, KDNode p, Rectangle2D cell) throws Exception {
		double height = cell.getWidth(1);
		double width = cell.getWidth(0);

		int cutDim = 0;
		if (height > width) {
			cutDim = 1;
		}

		// fell out of tree
		if (p == null || pt == null) {
			throw new Exception("Attempt to delete a nonexistent point");
		}
		// found it
		else if (p.getPoint().equals(pt)) {
			if (p.right != null) { // can replace from right
				p.setPoint(findMin(p.right, p.cutDim));// find and copy replacement
				p.setNewVals(p.cutDim, (p.cutDim == 0) ? (float) p.point.getX() : (float) p.point.getY());

				p.right = delete(p.point, p.right, cell.rightPart(p.cutDim, p.point.get(cutDim))); // delete from right

			}
			// can replace from left
			else if (p.left != null) {
				// find and copy replacement
				p.setPoint(findMin(p.left, p.cutDim));
				p.setNewVals(p.cutDim, (p.cutDim == 0) ? (float) p.point.getX() : (float) p.point.getY());
				// delete left but move to right!!
				p.right = delete(p.point, p.left, cell.leftPart(p.cutDim, p.point.get(cutDim)));
				// left subtree is now empty
				p.left = null;

			}
			// deleted point in leaf
			else {
				return null;
			}
			return rebalance(p, cell);

		}
		// delete from left subtree
		else if (p.inLeftSubtree(pt)) {
			p.left = delete(pt, p.left, cell.leftPart(p.cutDim, p.point.get(cutDim)));
		}
		// delete from right subtree
		else {
			p.right = delete(pt, p.right, cell.rightPart(p.cutDim, p.point.get(cutDim)));
		}

		return rebalance(p, cell);

	}

	public ArrayList<String> listHelper(KDNode curr) {
		ArrayList<String> list = new ArrayList<String>();
		if (curr == null) {
			list.add("[]");
		} else {
			list.add(curr.toString());
			list.addAll(listHelper(curr.left));
			list.addAll(listHelper(curr.right));
		}
		return list;
	}

	public ArrayList<String> getPreorderList() {
		ArrayList<String> list = listHelper(root);
		return list;
	}

	public ArrayList<LPoint> orthogRangeReport(Rectangle2D query) {
		ArrayList<LPoint> orthArr = new ArrayList<LPoint>();
		return orthHelper(query, root, bbox, orthArr);
	}

	ArrayList<LPoint> orthHelper(Rectangle2D R, KDNode p, Rectangle2D cell, ArrayList<LPoint> arr) {
		double height = cell.getWidth(1);
		double width = cell.getWidth(0);

		int cutDim = 0;
		if (height > width) {
			cutDim = 1;
		}

		// empty subtree
		if (p == null) {
			return new ArrayList<LPoint>(0);
		}
		// the range contains entire cell
		else if (R.contains(cell)) {
			ArrayList<KDNode> temp = makeNodeList(p);
			for (int i = 0; i < temp.size(); i++) {
				arr.add(temp.get(i).point);
			}
			return arr;
		}
		// the range stabs this cell
		else {
			if (R.contains(p.point.getPoint2D())) {
				arr.add(p.point);
			}
			// apply recursively to children
			arr.addAll(orthHelper(R, p.left, cell.leftPart(p.cutDim, p.point.get(cutDim)), new ArrayList<LPoint>()));
			arr.addAll(orthHelper(R, p.right, cell.leftPart(p.cutDim, p.point.get(cutDim)), new ArrayList<LPoint>()));
			return arr;
		}
	}

	public int size() {
		return size;
	}

	public void clear() {
		this.root = null;
		this.size = 0;
	}
}
