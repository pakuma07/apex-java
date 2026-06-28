# Convex Hull

## Concept

The convex hull of a set of points is the smallest convex polygon that encloses all of them, like a rubber band snapped around a set of nails. Andrew's monotone chain algorithm first sorts the points by x (then y), then sweeps left-to-right building the lower hull and right-to-left building the upper hull. While extending a chain, it pops the previous vertex whenever the last three points make a non-left (clockwise or collinear) turn, tested with the 2D cross product; this keeps only convex corners. Concatenating the two chains (dropping the duplicated endpoints) yields the hull in counter-clockwise order. Sorting dominates the cost at O(n log n); it underpins computational-geometry tasks such as collision bounds, farthest-pair, and shape simplification.

## Mermaid

```mermaid
flowchart TD
    A[Sort points by x then y] --> B[Build lower hull left to right]
    B --> C{Last 3 points turn clockwise or collinear?}
    C -- Yes --> D[Pop middle point]
    D --> C
    C -- No --> E[Push current point]
    E --> F[Build upper hull right to left same rule]
    F --> G[Concatenate lower + upper, drop duplicate endpoints]
    G --> H[CCW convex hull]
```

## Complexity

- Time: O(n log n), dominated by the initial sort (the two sweeps are O(n))
- Space: O(n) for the output hull

## Java Code

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class ConvexHull {

    // Coordinates are long (64-bit) so that the cross product below does not
    // overflow on large inputs; Java longs wrap silently, unlike Python integers.
    public record Point(long x, long y) {}

    // Cross product of OA x OB; >0 left turn (CCW), <0 right turn, =0 collinear.
    private static long cross(Point o, Point a, Point b) {
        return (a.x() - o.x()) * (b.y() - o.y()) - (a.y() - o.y()) * (b.x() - o.x());
    }

    // Returns hull vertices in counter-clockwise order (no repeated endpoint).
    public static List<Point> convexHull(List<Point> input) {
        int n = input.size();
        if (n < 3) return new ArrayList<>(input);   // degenerate: line or point

        Point[] pts = input.toArray(new Point[0]);
        Arrays.sort(pts, Comparator.comparingLong(Point::x).thenComparingLong(Point::y));

        Point[] hull = new Point[2 * n];
        int k = 0;

        // Lower hull.
        for (int i = 0; i < n; i++) {
            while (k >= 2 && cross(hull[k - 2], hull[k - 1], pts[i]) <= 0) k--;
            hull[k++] = pts[i];
        }

        // Upper hull (start at second-last point, end before lower's start).
        int lower = k + 1;
        for (int i = n - 2; i >= 0; i--) {
            while (k >= lower && cross(hull[k - 2], hull[k - 1], pts[i]) <= 0) k--;
            hull[k++] = pts[i];
        }

        // Drop last point (== first point).
        List<Point> result = new ArrayList<>(k - 1);
        for (int i = 0; i < k - 1; i++) result.add(hull[i]);
        return result;
    }
}
```

## Mini Usage Example

```java
// A square with two interior points; hull should be the 4 corners.
List<ConvexHull.Point> pts = List.of(
    new ConvexHull.Point(0, 0), new ConvexHull.Point(4, 0),
    new ConvexHull.Point(4, 4), new ConvexHull.Point(0, 4),
    new ConvexHull.Point(1, 1), new ConvexHull.Point(2, 2)
);
List<ConvexHull.Point> hull = ConvexHull.convexHull(pts);   // 4 corner points in CCW order
```

## Code Snippet Flow

```mermaid
flowchart LR
    A[Sort points lexicographically] --> B[Sweep up: build lower hull]
    B --> C["Pop while cross(<=0)"]
    C --> D[Append point]
    D --> E[Sweep down: build upper hull]
    E --> F["Pop while cross(<=0)"]
    F --> G[Append point]
    G --> H[Resize to drop duplicate endpoint]
    H --> I[Return CCW hull]
```
