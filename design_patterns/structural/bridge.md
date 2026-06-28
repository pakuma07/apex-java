# Bridge Pattern

## Overview

The Bridge pattern decouples an abstraction from its implementation so that the two can vary independently.

## Intent

- Decouple abstraction from implementation
- Allow independent variation of both
- Avoid permanent binding to implementation
- Reduce class hierarchies
- Share implementations across multiple abstractions

## Problem

You have an abstraction with multiple implementations:
- Window abstraction on Windows, Linux, Mac
- Drawing API with different renderers
- Data storage with multiple backends
- Shape with multiple rendering engines

Creating all combinations creates a class explosion:
```
Shape (abstraction)
├── CircleWindows, CircleLinux, CircleMac
├── RectangleWindows, RectangleLinux, RectangleMac
└── PolygonWindows, PolygonLinux, PolygonMac
```

## Solution

Create two separate hierarchies:
1. **Abstraction** - High-level interface
2. **Implementation** - Low-level, platform-specific

The abstraction delegates to implementation, which can be swapped.

## Structure

```
┌──────────────────┐         ┌─────────────────┐
│  Abstraction     │         │ Implementation  │
├──────────────────┤         ├─────────────────┤
│+ operation()     │────────→│+ operationImpl() │
└──────────────────┘         └─────────────────┘
        △                            △
        │                            │
   ┌────┴────┐              ┌───────┴────────┐
   │Refined   │              │ConcreteImpl A   │
   │Abstract  │              │ConcreteImpl B   │
   └──────────┘              └────────────────┘
```

## Java 21 Implementation

### Basic Bridge

```java
// Implementation interface (platform-specific)
interface Renderer {
    void renderCircle(float x, float y, float radius);
    void renderSquare(float x, float y, float size);
}

// Concrete implementations
class RasterRenderer implements Renderer {
    @Override
    public void renderCircle(float x, float y, float radius) {
        System.out.println("Raster: Drawing circle at (" + x + ", " + y
                + ") with radius " + radius);
    }

    @Override
    public void renderSquare(float x, float y, float size) {
        System.out.println("Raster: Drawing square at (" + x + ", " + y
                + ") with size " + size);
    }
}

class VectorRenderer implements Renderer {
    @Override
    public void renderCircle(float x, float y, float radius) {
        System.out.println("Vector: Drawing circle at (" + x + ", " + y
                + ") with radius " + radius);
    }

    @Override
    public void renderSquare(float x, float y, float size) {
        System.out.println("Vector: Drawing square at (" + x + ", " + y
                + ") with size " + size);
    }
}

// Abstraction (high-level)
abstract class Shape {
    protected final Renderer renderer;

    protected Shape(Renderer renderer) {
        this.renderer = renderer;
    }

    public abstract void draw();
}

// Refined abstractions
class Circle extends Shape {
    private final float x, y, radius;

    public Circle(Renderer renderer, float x, float y, float radius) {
        super(renderer);
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    @Override
    public void draw() {
        renderer.renderCircle(x, y, radius);
    }
}

class Square extends Shape {
    private final float x, y, size;

    public Square(Renderer renderer, float x, float y, float size) {
        super(renderer);
        this.x = x;
        this.y = y;
        this.size = size;
    }

    @Override
    public void draw() {
        renderer.renderSquare(x, y, size);
    }
}

// Usage
public class BridgeDemo {
    public static void main(String[] args) {
        // Create raster renderer
        var rasterRenderer = new RasterRenderer();

        Circle rasterCircle = new Circle(rasterRenderer, 0, 0, 5);
        Square rasterSquare = new Square(rasterRenderer, 10, 10, 8);

        rasterCircle.draw();
        rasterSquare.draw();

        System.out.println();

        // Switch to vector renderer
        var vectorRenderer = new VectorRenderer();

        Circle vectorCircle = new Circle(vectorRenderer, 0, 0, 5);
        Square vectorSquare = new Square(vectorRenderer, 10, 10, 8);

        vectorCircle.draw();
        vectorSquare.draw();
    }
}
```

## Advantages

✅ **Decouple abstraction from implementation** - Independent variation
✅ **Reduce class explosion** - No combinatorial class growth
✅ **Runtime switching** - Change implementation at runtime
✅ **Single Responsibility** - Each class has focused responsibility
✅ **Better encapsulation** - Implementation details hidden
✅ **Flexibility** - Add new abstractions and implementations independently

## Disadvantages

❌ **Complexity** - Extra layer of indirection
❌ **Performance** - Overhead from virtual calls
❌ **Learning curve** - Less intuitive than inheritance
❌ **Overkill for simple cases** - May overcomplicate code

## When to Use

✅ Decouple abstraction from implementation required
✅ Multiple implementations of abstraction
✅ Both abstraction and implementation vary
✅ Want to avoid permanent binding
✅ Reduce class hierarchies
✅ Platform-specific implementations needed
✅ Multiple rendering/storage backends
✅ Want to switch implementations at runtime

## When NOT to Use

❌ Single implementation only
❌ Abstraction and implementation don't vary independently
❌ Simple cases (inheritance sufficient)
❌ Performance critical and overhead unacceptable
❌ No need to hide implementation details

## Related Patterns

- **Adapter** - Makes incompatible interfaces work
- **Abstract Factory** - Creates families of related objects
- **Decorator** - Adds functionality dynamically

---

Bridge pattern is powerful for managing complexity when both abstraction and implementation require independent variation.
