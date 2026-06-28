# 09 — Computational Geometry

Use `long` (64-bit) for integer coordinates whenever possible to avoid floating-point errors. For products that can exceed 64 bits (e.g. cross products of coordinates up to 10^9), promote to `Math.multiplyHigh`/`BigInteger` or keep coordinates bounded; a cross product of two values up to ~3×10^9 stays within `long`.

---

## 9.1 Point and Vector

Java has no operator overloading, so vector operations become methods. A small immutable class works well.

```java
static final class Point {
    final long x, y;
    Point(long x, long y) { this.x = x; this.y = y; }

    Point add(Point o) { return new Point(x + o.x, y + o.y); }
    Point sub(Point o) { return new Point(x - o.x, y - o.y); }
    Point mul(long t)  { return new Point(x * t, y * t); }

    long dot(Point o)   { return x * o.x + y * o.y; }   // dot product
    long cross(Point o) { return x * o.y - y * o.x; }   // cross product

    long norm2()  { return x * x + y * y; }             // squared magnitude
    double norm() { return Math.sqrt((double) norm2()); }

    // ordering: by x, then y (replaces operator<)
    static final java.util.Comparator<Point> BY_XY =
        java.util.Comparator.comparingLong((Point p) -> p.x).thenComparingLong(p -> p.y);

    @Override public boolean equals(Object o) {
        if (!(o instanceof Point)) return false;
        Point p = (Point) o; return x == p.x && y == p.y;
    }
    @Override public int hashCode() { return Long.hashCode(x) * 31 + Long.hashCode(y); }
}
```

A Java 21 `record Point(long x, long y)` gives you `equals`/`hashCode` for free; add the helper methods in the record body.

---

## 9.2 Cross Product Uses

```java
// cross(B-A, C-A):
// > 0  → C is to the LEFT  of line A→B  (counter-clockwise turn)
// = 0  → C is COLLINEAR with A and B
// < 0  → C is to the RIGHT of line A→B  (clockwise turn)

static long cross(Point o, Point a, Point b) {
    return a.sub(o).cross(b.sub(o));
}

// Check if three points make a counter-clockwise turn
static boolean ccw(Point a, Point b, Point c) { return cross(a, b, c) > 0; }
```

---

## 9.3 Line Segment Intersection

```java
// Do segments AB and CD intersect?
static boolean onSegment(Point p, Point a, Point b) {
    return Math.min(a.x, b.x) <= p.x && p.x <= Math.max(a.x, b.x) &&
           Math.min(a.y, b.y) <= p.y && p.y <= Math.max(a.y, b.y);
}

static boolean segmentsIntersect(Point a, Point b, Point c, Point d) {
    long d1 = cross(c, d, a), d2 = cross(c, d, b);
    long d3 = cross(a, b, c), d4 = cross(a, b, d);
    if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
        ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) return true;
    if (d1 == 0 && onSegment(a, c, d)) return true;
    if (d2 == 0 && onSegment(b, c, d)) return true;
    if (d3 == 0 && onSegment(c, a, b)) return true;
    if (d4 == 0 && onSegment(d, a, b)) return true;
    return false;
}
```

---

## 9.4 Polygon Area (Shoelace Formula)

```java
// Signed area × 2 (avoid division for integer coordinates)
static long signedArea2(Point[] poly) {
    long area = 0;
    int n = poly.length;
    for (int i = 0; i < n; ++i) {
        int j = (i + 1) % n;
        area += poly[i].cross(poly[j]);
    }
    return area;  // positive = CCW, negative = CW
}

static long area2(Point[] poly) { return Math.abs(signedArea2(poly)); }
// True area = area2(poly) / 2.0

// Check if polygon is CCW
static boolean isCCW(Point[] poly) { return signedArea2(poly) > 0; }
```

---

## 9.5 Convex Hull — O(N log N)

```java
// Andrew's monotone chain — returns points in CCW order
static Point[] convexHull(Point[] in) {
    Point[] pts = in.clone();
    java.util.Arrays.sort(pts, Point.BY_XY);
    // remove duplicates
    int u = 0;
    for (int i = 0; i < pts.length; ++i)
        if (u == 0 || !pts[u - 1].equals(pts[i])) pts[u++] = pts[i];
    pts = java.util.Arrays.copyOf(pts, u);
    int n = pts.length;
    if (n < 3) return pts;

    Point[] hull = new Point[2 * n];
    int k = 0;
    // Lower hull
    for (int i = 0; i < n; ++i) {
        while (k >= 2 && cross(hull[k - 2], hull[k - 1], pts[i]) <= 0) k--;
        hull[k++] = pts[i];
    }
    // Upper hull
    int lower = k + 1;
    for (int i = n - 2; i >= 0; --i) {
        while (k >= lower && cross(hull[k - 2], hull[k - 1], pts[i]) <= 0) k--;
        hull[k++] = pts[i];
    }
    return java.util.Arrays.copyOf(hull, k - 1); // last point == first
}
// Change <= 0 to < 0 to include collinear points on the hull boundary
```

---

## 9.6 Point in Polygon

```java
// Ray casting — O(N) — works for any simple polygon
static boolean pointInPolygon(Point p, Point[] poly) {
    int n = poly.length, crosses = 0;
    for (int i = 0; i < n; ++i) {
        Point a = poly[i], b = poly[(i + 1) % n];
        if (a.y > b.y) { Point t = a; a = b; b = t; }
        if (p.y <= a.y || p.y > b.y) continue;
        long cp = b.sub(a).cross(p.sub(a));
        if (cp > 0) ++crosses;
    }
    return crosses % 2 == 1;
}

// Returns: 0 = outside, 1 = on boundary, 2 = inside (winding number version)
```

---

## 9.7 Distance Formulas

```java
// Squared Euclidean distance (exact, integer)
static long dist2(Point a, Point b) { return a.sub(b).norm2(); }

// Euclidean distance (floating point)
static double dist(Point a, Point b) { return a.sub(b).norm(); }

// Point to line distance (signed, integer cross product)
// Line through A and B; returns signed distance × |AB|
static long pointToLineDist2x(Point p, Point a, Point b) {
    return cross(a, b, p);  // = (b-a).cross(p-a)
}

// Closest pair of points — O(N log N) divide and conquer
// (Standard algorithm — see reference implementations)
```

---

## 9.8 Circle

```java
static final class Circle {
    final Point c; final double r;
    Circle(Point c, double r) { this.c = c; this.r = r; }
    boolean contains(Point p) {
        return dist2(c, p) <= (long) (r * r + 1e-9);
    }
}

// Two circles intersect?
static boolean circlesIntersect(Circle a, Circle b) {
    double d = dist(a.c, b.c);
    return d <= a.r + b.r + 1e-9 && d >= Math.abs(a.r - b.r) - 1e-9;
}
```

---

## 9.9 Common Geometry Pitfalls

```
✗ Using double for coordinates when integer arithmetic would do
✗ Comparing doubles with ==  (use Math.abs(a-b) < EPS)
✗ Forgetting collinear edge cases in segment intersection
✗ Using floating-point area when the answer must be exact
✗ Sorting points without a stable tie-breaking rule
✗ Convex hull with fewer than 3 points
✗ Ray casting failing on polygon edges (boundary is a special case)
✗ long overflow in cross products when coordinates exceed ~3×10^9 (use BigInteger / Math.multiplyHigh)
```

```java
// Epsilon for floating-point comparisons
static final double EPS = 1e-9;
static boolean eq(double a, double b) { return Math.abs(a - b) < EPS; }
static boolean lt(double a, double b) { return a < b - EPS; }
```

---

## 9.10 Summary

| Task | Algorithm | Complexity |
|------|-----------|------------|
| Polygon area | Shoelace formula | O(N) |
| Convex hull | Andrew's monotone chain | O(N log N) |
| Point in polygon | Ray casting | O(N) |
| Segment intersection | Cross product test | O(1) |
| Closest pair | Divide and conquer | O(N log N) |
| CCW/CW turn | Cross product sign | O(1) |

---

**Next**: [10 — Game Theory](10_game_theory.md)
