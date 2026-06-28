# Flyweight Pattern

## Overview

The Flyweight pattern uses sharing to support large numbers of fine-grained objects efficiently by sharing common state.

## Intent

- Share objects to use less memory
- Support large numbers of similar objects
- Extract intrinsic (shared) state
- Separate from extrinsic (unique) state
- Reduce memory footprint
- Improve performance

## Problem

You need many similar objects:
- Text editor with millions of character objects
- Game with thousands of particle objects
- Drawing application with thousands of shapes
- Each object consumes significant memory

Creating one object per entity is memory-intensive:
```
Text: "Hello World" = 11 Character objects
Each Character object: font, style, color, etc.
Total memory: 11 * (font + style + color + ...) bytes
```

## Solution

Share intrinsic state (font, style) across objects while keeping extrinsic state (position, value) separate:

```
Intrinsic State (shared):
- Font
- Style
- Color

Extrinsic State (unique):
- Character value
- Position
- Index
```

## Structure

```
┌──────────────────┐      ┌──────────────────┐
│   FlyweightFactory       │   Flyweight      │
├──────────────────┤      ├──────────────────┤
│+getFlyweight()   │      │+operation()      │
└──────────────────┘      └──────────────────┘
         │                        △
         │                        │
         └────────┬───────────────┘
                  │
            ┌─────▼──────┐
            │ConcreteFly │
            │weight      │
            └────────────┘
```

## Java 21 Implementation

### Character Flyweight

```java
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

// Intrinsic state (shared) - immutable, so a record fits naturally
record Character(char value, String font, int size) {
    void render(int x, int y) {
        System.out.println("Char '" + value + "' at (" + x + "," + y
                + ") font:" + font + " size:" + size);
    }
}

// Factory for managing flyweights
class CharacterFactory {
    private final Map<java.lang.Character, Character> flyweights = new HashMap<>();
    private final String defaultFont = "Arial";
    private final int defaultSize = 12;

    public Character getCharacter(char c) {
        return flyweights.computeIfAbsent(c,
                key -> new Character(key, defaultFont, defaultSize));
    }

    public int getPoolSize() {
        return flyweights.size();
    }
}

// Extrinsic state (unique position, etc)
class CharacterPosition {
    int x, y;
    Character character;
}

// Client: Text document
class TextDocument {
    private final List<CharacterPosition> characters = new ArrayList<>();
    private final CharacterFactory factory = new CharacterFactory();

    public void addCharacter(char c, int x, int y) {
        CharacterPosition cp = new CharacterPosition();
        cp.character = factory.getCharacter(c);
        cp.x = x;
        cp.y = y;
        characters.add(cp);
    }

    public void render() {
        for (var cp : characters) {
            cp.character.render(cp.x, cp.y);
        }
    }

    public int getCharacterPoolSize() {
        return factory.getPoolSize();
    }
}

// Usage
public class FlyweightDemo {
    public static void main(String[] args) {
        TextDocument doc = new TextDocument();

        // Add many characters
        String text = "Hello World";
        int x = 0;
        for (char c : text.toCharArray()) {
            doc.addCharacter(c, x++, 0);
        }

        System.out.println("Total characters: " + text.length());
        System.out.println("Unique characters (pool size): " + doc.getCharacterPoolSize());
        System.out.println("Memory saved: " + (text.length() - doc.getCharacterPoolSize())
                + " character objects");
        System.out.println();

        doc.render();
    }
}
```

### Game Particle Flyweight

```java
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Intrinsic state - immutable, so a record fits naturally
record ParticleType(String name, String texture, int color, float mass) {
    void render(float x, float y) {
        System.out.println("Rendering " + name + " at (" + x + "," + y + ")");
    }
}

// Factory
class ParticleFactory {
    private final Map<String, ParticleType> types = new HashMap<>();

    public ParticleType getParticleType(
            String name, String texture, int color, float mass) {
        return types.computeIfAbsent(name,
                key -> new ParticleType(key, texture, color, mass));
    }

    public int getTypeCount() {
        return types.size();
    }
}

// Extrinsic state
class Particle {
    float x, y;
    float velocityX, velocityY;
    ParticleType type;
}

// Game world
class ParticleSystem {
    private final List<Particle> particles = new ArrayList<>();
    private final ParticleFactory factory = new ParticleFactory();
    private final Random rand = new Random();

    public void emitParticles(String typeName, int count, float x, float y) {
        var type = factory.getParticleType(
                typeName, "particle.png", 0xFF0000, 1.0f);

        for (int i = 0; i < count; ++i) {
            Particle p = new Particle();
            p.type = type;
            p.x = x + rand.nextInt(10);
            p.y = y + rand.nextInt(10);
            p.velocityX = rand.nextInt(5) - 2.5f;
            p.velocityY = rand.nextInt(5) - 2.5f;
            particles.add(p);
        }
    }

    public void render() {
        for (var p : particles) {
            p.type.render(p.x, p.y);
        }
    }

    public int getParticleCount() {
        return particles.size();
    }

    public int getTypeCount() {
        return factory.getTypeCount();
    }
}
```

### Tree Node Flyweight

```java
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

// Intrinsic state: rendering properties - immutable, so a record fits naturally
record TreeNodeType(String name, String iconFile, int color) {
    void displayIcon() {
        System.out.println("Icon: " + iconFile);
    }
}

// Factory
class TreeNodeTypeFactory {
    private final Map<String, TreeNodeType> types = new HashMap<>();

    public TreeNodeType getNodeType(String name) {
        return types.computeIfAbsent(name,
                key -> new TreeNodeType(key, key + ".png", 0x000000));
    }
}

// Extrinsic state: tree structure
class TreeNode {
    private final String value;
    private final TreeNodeType type;
    private final List<TreeNode> children = new ArrayList<>();

    public TreeNode(String value, TreeNodeType type) {
        this.value = value;
        this.type = type;
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }

    public void display(int depth) {
        System.out.print(" ".repeat(depth * 2) + value + " - ");
        type.displayIcon();
        for (var child : children) {
            child.display(depth + 1);
        }
    }

    public void display() {
        display(0);
    }
}

// Usage
public class TreeNodeFlyweightDemo {
    public static void main(String[] args) {
        TreeNodeTypeFactory typeFactory = new TreeNodeTypeFactory();
        var folderType = typeFactory.getNodeType("Folder");
        var fileType = typeFactory.getNodeType("File");

        var root = new TreeNode("root", folderType);
        var subdir = new TreeNode("subdir", folderType);
        subdir.addChild(new TreeNode("file.txt", fileType));
        root.addChild(subdir);
    }
}
```

## Advantages

✅ **Reduce memory** - Dramatically reduce memory consumption
✅ **Improve performance** - Fewer objects to manage
✅ **Share state** - Common state shared across many objects
✅ **Scalability** - Handle thousands/millions of objects
✅ **Transparency** - Works transparently to clients
✅ **Thread safety** - Immutable flyweights are naturally thread-safe

## Disadvantages

❌ **Complexity** - Complex code with intrinsic/extrinsic state
❌ **Thread safety** - Shared state requires synchronization
❌ **CPU overhead** - Lookup in factory map has cost
❌ **Learning curve** - Understand intrinsic vs extrinsic
❌ **Premature optimization** - Only use when memory is issue

## When to Use

✅ Many similar objects consuming memory
✅ Can extract shared intrinsic state
✅ Object identity not critical
✅ Memory usage is bottleneck
✅ Thousands of objects needed
✅ Objects are mostly read-only
✅ Fine-grained objects (characters, particles)

## When NOT to Use

❌ Few objects needed
❌ Cannot extract intrinsic state
❌ Memory not a concern
❌ Objects need unique identity
❌ Mutable shared state required
❌ Simple solution sufficient

## Memory Analysis

### Without Flyweight
```
Object: 100 bytes
Objects needed: 10,000
Total: 100 * 10,000 = 1,000,000 bytes = 1 MB

Without unique positions: 976,562 bytes saved!
```

### With Flyweight
```
Shared type: 100 bytes (1 copy)
Position info: 8 bytes (10,000 copies)
Total: 100 + (8 * 10,000) = 80,100 bytes = 0.08 MB
```

## Related Patterns

- **Factory** - Creates and manages flyweights
- **Composite** - Flyweights often used in composite structures
- **Singleton** - Flyweight factory is often singleton

## Best Practices

1. **Identify intrinsic state** - What's truly shared?
2. **Immutable flyweights** - Don't modify shared state
3. **Use factory** - Centralize flyweight creation
4. **Thread safety** - Synchronize factory access
5. **Measure first** - Profile before optimizing
6. **Document clearly** - Explain intrinsic vs extrinsic
7. **Separate concerns** - Keep state separation clean

## Implementation Checklist

- [ ] Identify intrinsic state (shared)
- [ ] Identify extrinsic state (unique)
- [ ] Create Flyweight class (intrinsic only)
- [ ] Create Flyweight factory
- [ ] Factory caches flyweights
- [ ] Client holds references to flyweight
- [ ] Client holds extrinsic state
- [ ] Flyweights are immutable
- [ ] Test memory usage
- [ ] Verify correctness with/without
- [ ] Document intrinsic vs extrinsic
- [ ] Measure performance improvement

---

Flyweight pattern is essential when dealing with large numbers of similar objects where memory is a constraint. Use it when you can clearly separate shared and unique state.
