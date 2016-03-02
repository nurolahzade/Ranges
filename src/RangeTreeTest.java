import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class RangeTreeTest {
    @Test
    public void range_creation() {
        final Integer start = 1;
        final Integer end = 10;

        final RangeTree.Range<Integer> range = new RangeTree.Range<>(start, end);

        assertEquals(start, range.getStart());
        assertEquals(end, range.getEnd());
    }

    @Test
    public void range_toString() {
        final RangeTree.Range<Integer> range = new RangeTree.Range<>(5, 15);

        assertEquals("(5, 15)", range.toString());
    }

    @Test
    public void range_equals() {
        final RangeTree.Range<Integer> range1 = new RangeTree.Range<>(5, 15);
        final RangeTree.Range<Integer> range2 = new RangeTree.Range<>(5, 15);
        final RangeTree.Range<String> range3 = new RangeTree.Range<>("5", "15");

        assertTrue(range1.equals(range1));
        assertTrue(range1.equals(range2));
        assertTrue(range2.equals(range1));

        assertFalse(range1.equals(null));
        assertFalse(range1.equals(range3));
    }

    @Test
    public void range_hashCode() {
        final RangeTree.Range<Integer> range1 = new RangeTree.Range<>(10, 15);
        final RangeTree.Range<Integer> range2 = new RangeTree.Range<>(10, 15);

        assertEquals(range1, range2);
        assertEquals(range1.hashCode(), range2.hashCode());
    }

    @Test
    public void range_compareTo() {
        final RangeTree.Range<Integer> range1 = new RangeTree.Range<>(10, 25);
        final RangeTree.Range<Integer> range2 = new RangeTree.Range<>(15, 25);
        final RangeTree.Range<Integer> range3 = new RangeTree.Range<>(15, 30);
        final RangeTree.Range<Integer> range4 = new RangeTree.Range<>(35, 40);

        assertTrue(range1.compareTo(range2) < 0);
        assertTrue(range2.compareTo(range1) > 0);

        assertTrue(range1.compareTo(range3) < 0);
        assertTrue(range3.compareTo(range1) > 0);

        assertTrue(range2.compareTo(range3) < 0);
        assertTrue(range3.compareTo(range2) > 0);

        assertTrue(range1.compareTo(range4) < 0);
        assertTrue(range4.compareTo(range1) > 0);

        assertTrue(range1.compareTo(range1) == 0);
    }

    @Test
    public void range_contains() {
        final RangeTree.Range<Integer> range1 = new RangeTree.Range<>(20, 25);
        final RangeTree.Range<Integer> range2 = new RangeTree.Range<>(15, 25);
        final RangeTree.Range<Integer> range3 = new RangeTree.Range<>(20, 30);
        final RangeTree.Range<Integer> range4 = new RangeTree.Range<>(15, 30);
        final RangeTree.Range<Integer> range5 = new RangeTree.Range<>(30, 40);

        assertTrue(range1.contains(range1));
        assertTrue(range2.contains(range1));
        assertTrue(range3.contains(range1));
        assertTrue(range4.contains(range1));

        assertFalse(range1.contains(range2));
        assertFalse(range1.contains(range3));
        assertFalse(range1.contains(range4));
        assertFalse(range2.contains(range3));
        assertFalse(range1.contains(range5));
        assertFalse(range5.contains(range1));
    }

    @Test
    public void range_overlaps() {
        final RangeTree.Range<Integer> range1 = new RangeTree.Range<>(20, 25);
        final RangeTree.Range<Integer> range2 = new RangeTree.Range<>(15, 25);
        final RangeTree.Range<Integer> range3 = new RangeTree.Range<>(20, 30);
        final RangeTree.Range<Integer> range4 = new RangeTree.Range<>(15, 30);
        final RangeTree.Range<Integer> range5 = new RangeTree.Range<>(30, 40);

        assertTrue(range1.overlaps(range1));
        assertTrue(range1.overlaps(range2));
        assertTrue(range2.overlaps(range1));
        assertTrue(range1.overlaps(range3));
        assertTrue(range3.overlaps(range1));
        assertTrue(range1.overlaps(range4));
        assertTrue(range4.overlaps(range1));
        assertTrue(range2.overlaps(range3));

        assertFalse(range1.overlaps(range5));
        assertFalse(range5.overlaps(range1));
    }

    @Test
    public void node_creation() {
        final RangeTree.Range<Integer> range = new RangeTree.Range<>(1, 10);

        RangeTree.Node<Integer> node = new RangeTree.Node<>(range);

        assertEquals(range, node.getRange());
        assertEquals(range.getEnd(), node.getMax());
        assertNull(node.getLeft());
        assertNull(node.getRight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeTree_addRange_nullRange() {
        RangeTree<Integer> tree = new RangeTree<>();
        tree.addRange(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeTree_addRange_nullRangeStart() {
        RangeTree<Integer> tree = new RangeTree<>();
        tree.addRange(new RangeTree.Range<>(null, 10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeTree_addRange_nullRangeEnd() {
        RangeTree<Integer> tree = new RangeTree<>();
        tree.addRange(new RangeTree.Range<>(10, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeTree_addRange_invalidRange() {
        RangeTree<Integer> tree = new RangeTree<>();
        tree.addRange(new RangeTree.Range<>(20, 10));
    }

    @Test
    public void rangeTree_addRange_duplicateRange() {
        final RangeTree.Range<Integer> range = new RangeTree.Range<>(5, 10);
        RangeTree<Integer> tree = new RangeTree<>();

        assertTrue(tree.addRange(range));
        assertFalse(tree.addRange(range));
    }

    @Test
    public void rangeTree_addRange_leftSubTree() {
        final RangeTree.Range<Integer> range1 = new RangeTree.Range<>(5, 10);
        final RangeTree.Range<Integer> range2 = new RangeTree.Range<>(3, 15);
        final RangeTree.Range<Integer> range3 = new RangeTree.Range<>(1, 5);

        RangeTree<Integer> tree = new RangeTree<>();

        assertTrue(tree.addRange(range1));
        assertTrue(tree.addRange(range2));
        assertTrue(tree.addRange(range3));

        assertTrue(tree.getRoot().getRange().equals(range1));
        assertTrue(tree.getRoot().getLeft().getRange().equals(range2));
        assertNull(tree.getRoot().getRight());
        assertTrue(tree.getRoot().getLeft().getLeft().getRange().equals(range3));
        assertNull(tree.getRoot().getLeft().getRight());
        assertNull(tree.getRoot().getLeft().getLeft().getLeft());
        assertNull(tree.getRoot().getLeft().getLeft().getRight());
    }

    @Test
    public void rangeTree_addRange_rightSubTree() {
        final RangeTree.Range<Integer> range1 = new RangeTree.Range<>(5, 10);
        final RangeTree.Range<Integer> range2 = new RangeTree.Range<>(10, 15);
        final RangeTree.Range<Integer> range3 = new RangeTree.Range<>(12, 14);

        RangeTree<Integer> tree = new RangeTree<>();

        assertTrue(tree.addRange(range1));
        assertTrue(tree.addRange(range2));
        assertTrue(tree.addRange(range3));

        assertTrue(tree.getRoot().getRange().equals(range1));
        assertNull(tree.getRoot().getLeft());
        assertTrue(tree.getRoot().getRight().getRange().equals(range2));
        assertNull(tree.getRoot().getRight().getLeft());
        assertTrue(tree.getRoot().getRight().getRight().getRange().equals(range3));
        assertNull(tree.getRoot().getRight().getRight().getLeft());
        assertNull(tree.getRoot().getRight().getRight().getRight());
    }

    @Test
    public void rangeTree_addRange_updatesMax() {
        final RangeTree.Range<Integer> range1 = new RangeTree.Range<>(5, 10);
        final RangeTree.Range<Integer> range2 = new RangeTree.Range<>(7, 15);

        RangeTree<Integer> tree = new RangeTree<>();

        assertTrue(tree.addRange(range1));
        assertTrue(tree.addRange(range2));
        assertEquals(range2.getEnd(), tree.getRoot().getMax());
    }

    @Test
    public void rangeTree_queryRange_emptyTree() {
        RangeTree<Integer> tree = new RangeTree<>();
        assertFalse(tree.queryRange(new RangeTree.Range<>(1, 5)));
    }

    @Test
    public void rangeTree_queryRange_oneNode() {
        final RangeTree.Range<Integer> range = new RangeTree.Range<>(5, 10);
        RangeTree<Integer> tree = new RangeTree<>();

        assertTrue(tree.addRange(range));

        assertTrue(tree.queryRange(range));
        assertTrue(tree.queryRange(new RangeTree.Range<>(5, 9)));
        assertTrue(tree.queryRange(new RangeTree.Range<>(6, 10)));
        assertTrue(tree.queryRange(new RangeTree.Range<>(6, 9)));

        assertFalse(tree.queryRange(new RangeTree.Range<>(1, 4)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(11, 15)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(1, 6)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(9, 12)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(1, 12)));
    }

    @Test
    public void rangeTree_queryRange_twoOverlappingNodes() {
        final RangeTree.Range<Integer> range1 = new RangeTree.Range<>(5, 10);
        final RangeTree.Range<Integer> range2 = new RangeTree.Range<>(8, 15);
        RangeTree<Integer> tree = new RangeTree<>();

        assertTrue(tree.addRange(range1));
        assertTrue(tree.addRange(range2));

        assertTrue(tree.queryRange(range1));
        assertTrue(tree.queryRange(range2));

        assertTrue(tree.queryRange(new RangeTree.Range<>(5, 15)));
        assertTrue(tree.queryRange(new RangeTree.Range<>(6, 15)));
        assertTrue(tree.queryRange(new RangeTree.Range<>(5, 14)));

        assertFalse(tree.queryRange(new RangeTree.Range<>(1, 4)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(20, 25)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(1, 6)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(12, 20)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(1, 20)));
    }

    @Test
    public void rangeTree_queryRange_threeOverlappingNodes() {
        final RangeTree.Range<Integer> range1 = new RangeTree.Range<>(5, 10);
        final RangeTree.Range<Integer> range2 = new RangeTree.Range<>(1, 6);
        final RangeTree.Range<Integer> range3 = new RangeTree.Range<>(8, 15);

        RangeTree<Integer> tree = new RangeTree<>();

        assertTrue(tree.addRange(range1));
        assertTrue(tree.addRange(range2));
        assertTrue(tree.addRange(range3));

        assertTrue(tree.queryRange(range1));
        assertTrue(tree.queryRange(range2));
        assertTrue(tree.queryRange(range3));

        assertTrue(tree.queryRange(new RangeTree.Range<>(1, 15)));
        assertTrue(tree.queryRange(new RangeTree.Range<>(2, 14)));
        assertTrue(tree.queryRange(new RangeTree.Range<>(1, 14)));
        assertTrue(tree.queryRange(new RangeTree.Range<>(2, 15)));
        assertTrue(tree.queryRange(new RangeTree.Range<>(1, 10)));
        assertTrue(tree.queryRange(new RangeTree.Range<>(5, 15)));

        assertFalse(tree.queryRange(new RangeTree.Range<>(1, 16)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(0, 15)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(0, 16)));
    }

    @Test
    public void rangeTree_queryRange_twoNonOverlappingNodes() {
        final RangeTree.Range<Integer> range1 = new RangeTree.Range<>(16, 20);
        final RangeTree.Range<Integer> range2 = new RangeTree.Range<>(10, 14);
        RangeTree<Integer> tree = new RangeTree<>();

        assertTrue(tree.addRange(range1));
        assertTrue(tree.addRange(range2));

        assertTrue(tree.queryRange(range1));
        assertTrue(tree.queryRange(range2));

        assertTrue(tree.queryRange(new RangeTree.Range<>(16, 19)));
        assertTrue(tree.queryRange(new RangeTree.Range<>(17, 20)));
        assertTrue(tree.queryRange(new RangeTree.Range<>(11, 14)));
        assertTrue(tree.queryRange(new RangeTree.Range<>(10, 13)));

        assertFalse(tree.queryRange(new RangeTree.Range<>(10, 20)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(25, 30)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(1, 5)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(15, 15)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(10, 15)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(15, 20)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(10, 18)));
        assertFalse(tree.queryRange(new RangeTree.Range<>(12, 20)));
    }

    @Test
    public void rangeTree_deleteRange() {
        RangeTree<Integer> tree = new RangeTree<>();
        assertFalse(tree.deleteRange(new RangeTree.Range<>(1, 5)));
    }

    @Test
    public void rangeTree_toString() {
        RangeTree<Integer> tree = new RangeTree<>();
        assertEquals("", tree.toString());

        tree.addRange(new RangeTree.Range<>(5, 10));
        assertEquals("[(5, 10), 10]", tree.toString());

        tree.addRange(new RangeTree.Range<>(1, 7));
        assertEquals("[(1, 7), 7][(5, 10), 10]", tree.toString());

        tree.addRange(new RangeTree.Range<>(9, 12));
        assertEquals("[(1, 7), 7][(5, 10), 12][(9, 12), 12]", tree.toString());
    }

    @Test(timeout = 10000)
    public void rangeTree_scalability() {
        final int sampleSize = 100000;
        final int rangeStart = 0;
        final int rangeEnd = 1000000;
        final int rangeLength = rangeEnd - rangeStart;
        final int maxSubRangeLength = 1000;

        RangeTree<Integer> tree = new RangeTree<>();
        Set<RangeTree.Range<Integer>> randomRanges = new HashSet<>(sampleSize);

        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < sampleSize; ++i) {
            int start = rangeStart + random.nextInt(rangeLength);
            int length = random.nextInt(maxSubRangeLength);
            RangeTree.Range<Integer> range = new RangeTree.Range<>(start, start + length);
            randomRanges.add(range);
            tree.addRange(range);
        }

        Iterator<RangeTree.Range<Integer>> iterator = randomRanges.iterator();
        while (iterator.hasNext()) {
            assertTrue(tree.queryRange(iterator.next()));
        }
    }

}
