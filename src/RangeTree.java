import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/*
 *  A RangeTree is a binary search tree that
 *  (a) every node represent a range
 *  (b) nodes are ordered by the start of the range
 *  (c) every node is augmented by the maximum end of range in the sub-tree rooted by this node
 *  Reference: https://en.wikipedia.org/wiki/Interval_tree#Augmented_tree
 *
 *  A better implementation would have been a self-balancing tree like red-black tree
 *  However due to time constraints I am going to skip this improvement
 *  Reference: https://en.wikipedia.org/wiki/Red–black_tree
 */
public class RangeTree<T extends Comparable<T>> {

    private Node<T> root;

    public static class Node<T extends Comparable<T>> {
        // root of the left sub-tree (@Nullable)
        private Node<T> left;
        // root of the right sub-tree (@Nullable)
        private Node<T> right;
        // the range represented by the node (@NotNull)
        private Range<T> range;
        // the maximum range end in the sub-tree rooted by this node (@NotNull)
        private T max;

        public Node(Range<T> range) {
            this.range = range;
            max = range.getEnd();
        }

        public Node<T> getLeft() {
            return left;
        }

        public void setLeft(Node<T> left) {
            this.left = left;
        }

        public Node<T> getRight() {
            return right;
        }

        public void setRight(Node<T> right) {
            this.right = right;
        }

        public Range<T> getRange() {
            return range;
        }

        public T getMax() {
            return max;
        }

        public void setMax(T max) {
            this.max = max;
        }
    }

    public static class Range<T extends Comparable<T>> implements Comparable<Range<T>> {
        private T start;
        private T end;

        public Range(T start, T end) {
            this.start = start;
            this.end = end;
        }

        public T getStart() {
            return start;
        }

        public T getEnd() {
            return end;
        }

        @Override
        public int compareTo(Range<T> other) {
            // the range that starts first is ordered higher
            if (start.compareTo(other.getStart()) < 0) return -1;
            else if (start.compareTo(other.getStart()) > 0) return 1;
            // if two ranges have the same start then the one that ends first is ordered higher
            else if (end.compareTo(other.getEnd()) < 0) return -1;
            else if (end.compareTo(other.getEnd()) > 0) return 1;
            // if two ranges have similar starts and ends then they are equal
            else return 0;
        }

        public boolean contains(Range<T> other) {
            return start.compareTo(other.getStart()) <= 0 && end.compareTo(other.getEnd()) >= 0;
        }

        public boolean overlaps(Range<T> other) {
            // overlapping ranges:
            // (1) ---this.start---other.start---other.end---this.end---
            // (2) ---this.start---other.start---this.end---other.end---
            // (3) ---other.start---this.start---this.end---other-end---
            // (4) ---other.start---this.start---other.end---this.end---
            // non-overlapping ranges:
            // (1) ---this.start---this.end---other.start---other.end---
            // (2) ---other.start---other.end---this.start---this.end---
            return start.compareTo(other.getEnd()) <= 0 && end.compareTo(other.getStart()) >= 0;
        }

        @Override
        public String toString() {
            return "(" + start + ", " + end + ")";
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;

            Range<?> range = (Range<?>) other;
            // two ranges (start1, end1) and (start2, end2) are equal if start1 equals start2 and end1 equals end2
            if (!start.equals(range.start)) return false;
            return end.equals(range.end);
        }

        @Override
        public int hashCode() {
            // needed to override equals(), so had to override hashCode() as well
            int result = start.hashCode();
            result = 31 * result + end.hashCode();
            return result;
        }
    }

    public Node<T> getRoot() {
        return root;
    }

    /*
     * Adds range to the tree (does not add duplicate ranges)
     * Returns true if a new node with this range is added, false otherwise
     * Throws IllegalArgumentException if range is invalid
     */
    public boolean addRange(Range<T> range) {
        verifyIsValidRange(range);

        if (root == null) {
            root = new Node<>(range);
            return true;
        }

        return addRangeRecursive(root, range);
    }

    void verifyIsValidRange(Range<T> range) {
        if (range == null) {
            throw new IllegalArgumentException("Range cannot be null.");
        }
        if (range.getStart() == null || range.getEnd() == null) {
            throw new IllegalArgumentException("Range start and end cannot be null.");
        }
        if (range.getStart().compareTo(range.getEnd()) > 0) {
            throw new IllegalArgumentException("Invalid range: " + range);
        }
    }

    boolean addRangeRecursive(Node<T> node, Range<T> range) {
        // adjust current node's max (if necessary)
        adjustMax(node, range);

        if (range.equals(node.getRange())) {
            // if range is currently in the tree, do not add it again
            return false;
        } else if (range.getStart().compareTo(node.getRange().getStart()) < 0) {
            // if range starts before current node's range then add it to left sub-tree
            if (node.getLeft() == null) {
                node.setLeft(new Node<>(range));
                return true;
            }
            return addRangeRecursive(node.getLeft(), range);
        } else {
            // if the range's start is the same or greater than current node's
            // then add it to the right sub-tree
            if (node.getRight() == null) {
                node.setRight(new Node<>(range));
                return true;
            }
            return addRangeRecursive(node.getRight(), range);
        }
    }

    void adjustMax(Node<T> node, Range<T> range) {
        // no need to adjust max if it is less than or equal to current max
        if (range.getEnd().compareTo(node.getMax()) > 0) {
            node.setMax(range.getEnd());
        }
    }

    /*
     * Delete range from the tree (only removes if a node with the same range exists)
     * Returns true if a node with the same range is found and deleted, false otherwise
     * Throws IllegalArgumentException if range is invalid
     */
    public boolean deleteRange(Range<T> range) {
        verifyIsValidRange(range);

        if (root == null) return false;
        return deleteRangeRecursive(range, root, null);
    }

    boolean deleteRangeRecursive(Range<T> range, Node<T> node, Node<T> parent) {
        // The delete logic works based on the number of children of the node to be deleted:
        //  If no children then simply delete node
        //  If just one child then (a) replace node with this child and (b) delete node
        //  If two children then (a) replace node with the smallest node in the right sub-tree and (b) delete node
        // When returning from recursive calls adjust node max values if necessary
        return false;
    }

    /*
     * Verifies if the query range is covered by the ranges in the tree
     * Returns true if the range is contained, false otherwise
     * Throws IllegalArgumentException if range is invalid
     */
    public boolean queryRange(Range<T> query) {
        verifyIsValidRange(query);

        // get all ranges that overlap query sorted by their range start
        List<Range<T>> sortedRanges = queryRangeRecursive(root, query);
        // merge overlapping ranges and see if any resulting range matches query
        return match(query, sortedRanges);
    }

    List<Range<T>> queryRangeRecursive(Node<T> node, Range<T> query) {
        if (node == null || node.getMax().compareTo(query.getStart()) < 0) {
            // if query range starts after the current maximum in the sub-tree, then it cannot be in the sub-tree
            return Collections.emptyList();
        }

        List<Range<T>> overlapping = new LinkedList<>();
        if (node.getLeft() != null && node.getMax().compareTo(query.getStart()) < 0) {
            // recurse left sub-tree
            overlapping.addAll(queryRangeRecursive(node.getLeft(), query));
        }
        if (node.getRange().overlaps(query)) {
            // if range overlaps this node's range select it
            overlapping.add(node.getRange());
        }
        if (node.getRight() != null && query.getEnd().compareTo(node.getRange().getStart()) > 0) {
            // recurse right sub-tree only if there will be any ranges that overlap it
            overlapping.addAll(queryRangeRecursive(node.getRight(), query));
        }

        return overlapping;
    }

    boolean match(Range<T> query, List<Range<T>> ranges) {
        // we need access to the last element in the list
        Range<T> lastRange = null;

        // merge sorted ranges into lastRange, one at a time
        while (!ranges.isEmpty()) {
            Range<T> newRange = ranges.remove(0);

            if (lastRange != null) {
                if (lastRange.overlaps(newRange)) {
                    // two ranges (start1, ene1) and (start2, end2) that overlap can be combined to form
                    // the range (min(start1, start2), max(end1, end2))
                    T start = lastRange.getStart().compareTo(newRange.getStart()) < 0 ?
                            lastRange.getStart() :
                            newRange.getStart();
                    T end = lastRange.getEnd().compareTo(newRange.getEnd()) < 0 ?
                            newRange.getEnd() :
                            lastRange.getEnd();
                    // replace lastRange with merged range
                    lastRange = new Range<>(start, end);
                } else {
                    // discard previous lastRange (if it does not overlap newRange then
                    // it cannot also overlap any ranges that start after newRange)
                    lastRange = newRange;
                }
            } else {
                lastRange = newRange;
            }

            if (lastRange.getStart().compareTo(query.getEnd()) > 0) {
                // give up, query cannot be contained in ranges that start after it
                break;
            } else if (lastRange.contains(query)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return inOrderToString(root);
    }

    String inOrderToString(Node<T> node) {
        if (node == null) return "";

        StringBuilder builder = new StringBuilder();
        builder.append(inOrderToString(node.getLeft()));
        builder.append("[");
        builder.append(node.getRange());
        builder.append(", ");
        builder.append(node.getMax());
        builder.append("]");
        builder.append(inOrderToString(node.getRight()));

        return builder.toString();
    }

}
