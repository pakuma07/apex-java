# Builder Pattern

## Overview

The Builder pattern separates construction of a complex object from its representation, allowing the same construction process to create different representations.

## Intent

- Separate object construction from representation
- Build complex objects step by step
- Create different representations with same process
- Hide internal implementation details
- Make immutable objects
- Improve readability of construction code

## Problem

Creating complex objects with many parameters:
- Large number of constructor parameters
- Some parameters optional
- Parameter combinations invalid
- Object representation may vary
- Construction logic is complex

Result: Unmaintainable, hard-to-read constructors.

## Solution

Define a separate Builder class that:
1. Holds parameter values
2. Provides fluent interface (method chaining)
3. Builds the final object
4. Validates before building

## Structure

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌──────────────────┐      ┌────────────────┐
│Builder (abstract)│──────│ConcreteBuilder │
├──────────────────┤      ├────────────────┤
│+buildPart()      │      │-result         │
│+getProduct()     │      │+buildPart()    │
└──────────────────┘      │+getProduct()   │
                          └────────────────┘
                                 │
                                 ▼
                          ┌─────────────┐
                          │  Product    │
                          ├─────────────┤
                          │ data fields │
                          └─────────────┘
```

## Java 21 Implementation

### Basic Builder

```java
class House {
    private String windows;
    private String doors;
    private String roof;
    private String garage;

    // Constructor is package-private; created via the builder
    House() {
        this.windows = "none";
        this.doors = "none";
        this.roof = "none";
        this.garage = "none";
    }

    // Setters used by HouseBuilder
    void setWindows(String windows) { this.windows = windows; }
    void setDoors(String doors) { this.doors = doors; }
    void setRoof(String roof) { this.roof = roof; }
    void setGarage(String garage) { this.garage = garage; }

    public void show() {
        System.out.println("House: " + windows + ", " + doors
                + ", " + roof + ", " + garage);
    }
}

class HouseBuilder {
    private final House house = new House();

    public HouseBuilder buildWindows(String type) {
        house.setWindows(type);
        return this;  // For chaining
    }

    public HouseBuilder buildDoors(String type) {
        house.setDoors(type);
        return this;
    }

    public HouseBuilder buildRoof(String type) {
        house.setRoof(type);
        return this;
    }

    public HouseBuilder buildGarage(String type) {
        house.setGarage(type);
        return this;
    }

    public House build() {
        return house;
    }
}

// Usage
House house = new HouseBuilder()
    .buildWindows("glass")
    .buildDoors("wooden")
    .buildRoof("tiles")
    .buildGarage("concrete")
    .build();

house.show();
```

### Pizza Builder

```java
import java.util.ArrayList;
import java.util.List;

class Pizza {
    public String dough;
    public String sauce;
    public List<String> toppings = new ArrayList<>();
    public boolean cheese = false;

    public void display() {
        System.out.print("Pizza: " + dough + ", " + sauce);
        System.out.print(", cheese: " + (cheese ? "yes" : "no"));
        System.out.print(", toppings: ");
        for (String t : toppings) System.out.print(t + " ");
        System.out.println();
    }
}

class PizzaBuilder {
    private final Pizza pizza = new Pizza();

    public PizzaBuilder withDough(String d) {
        pizza.dough = d;
        return this;
    }

    public PizzaBuilder withSauce(String s) {
        pizza.sauce = s;
        return this;
    }

    public PizzaBuilder addTopping(String t) {
        pizza.toppings.add(t);
        return this;
    }

    public PizzaBuilder addCheese() {
        pizza.cheese = true;
        return this;
    }

    public Pizza build() {
        return pizza;
    }
}

// Usage
Pizza margherita = new PizzaBuilder()
    .withDough("thin")
    .withSauce("tomato")
    .addCheese()
    .addTopping("basil")
    .build();

margherita.display();
```

### Database Connection Builder

```java
class DatabaseConnection {
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private int poolSize;
    private boolean ssl;

    DatabaseConnection() {
        this.port = 0;
        this.poolSize = 10;
        this.ssl = false;
    }

    // Setters used by DatabaseBuilder
    void setHost(String host) { this.host = host; }
    void setPort(int port) { this.port = port; }
    void setDatabase(String database) { this.database = database; }
    void setUsername(String username) { this.username = username; }
    void setPassword(String password) { this.password = password; }
    void setPoolSize(int poolSize) { this.poolSize = poolSize; }
    void setSsl(boolean ssl) { this.ssl = ssl; }

    String getHost() { return host; }
    String getDatabase() { return database; }

    public void showConfig() {
        System.out.println("DB Config: " + host + ":" + port
                + " db=" + database + " user=" + username
                + " pool=" + poolSize + " ssl=" + ssl);
    }
}

class DatabaseBuilder {
    private final DatabaseConnection db = new DatabaseConnection();

    public DatabaseBuilder setHost(String h) {
        db.setHost(h);
        return this;
    }

    public DatabaseBuilder setPort(int p) {
        db.setPort(p);
        return this;
    }

    public DatabaseBuilder setDatabase(String d) {
        db.setDatabase(d);
        return this;
    }

    public DatabaseBuilder setUsername(String u) {
        db.setUsername(u);
        return this;
    }

    public DatabaseBuilder setPassword(String p) {
        db.setPassword(p);
        return this;
    }

    public DatabaseBuilder setPoolSize(int size) {
        db.setPoolSize(size);
        return this;
    }

    public DatabaseBuilder enableSSL() {
        return enableSSL(true);
    }

    public DatabaseBuilder enableSSL(boolean enable) {
        db.setSsl(enable);
        return this;
    }

    public DatabaseConnection build() {
        if (db.getHost() == null || db.getHost().isEmpty()
                || db.getDatabase() == null || db.getDatabase().isEmpty()) {
            throw new RuntimeException("Host and database required");
        }
        return db;
    }
}

// Usage
var dbConfig = new DatabaseBuilder()
    .setHost("localhost")
    .setPort(5432)
    .setDatabase("myapp")
    .setUsername("admin")
    .setPassword("secret")
    .setPoolSize(20)
    .enableSSL()
    .build();

dbConfig.showConfig();
```

### HTTP Request Builder

```java
import java.util.HashMap;
import java.util.Map;

class HttpRequest {
    public String method;
    public String url;
    public Map<String, String> headers = new HashMap<>();
    public String body;
    public int timeout;
    public boolean followRedirects;

    public void send() {
        System.out.println(method + " " + url);
        System.out.print("Headers: ");
        for (var entry : headers.entrySet()) {
            System.out.print(entry.getKey() + "=" + entry.getValue() + " ");
        }
        System.out.println();
        if (body != null && !body.isEmpty()) System.out.println("Body: " + body);
    }
}

class HttpRequestBuilder {
    private final HttpRequest request = new HttpRequest();

    public HttpRequestBuilder() {
        request.method = "GET";
        request.timeout = 30;
        request.followRedirects = true;
    }

    public HttpRequestBuilder url(String u) {
        request.url = u;
        return this;
    }

    public HttpRequestBuilder method(String m) {
        request.method = m;
        return this;
    }

    public HttpRequestBuilder header(String key, String value) {
        request.headers.put(key, value);
        return this;
    }

    public HttpRequestBuilder body(String b) {
        request.body = b;
        request.method = "POST";
        return this;
    }

    public HttpRequestBuilder timeout(int seconds) {
        request.timeout = seconds;
        return this;
    }

    public HttpRequestBuilder followRedirects(boolean follow) {
        request.followRedirects = follow;
        return this;
    }

    public HttpRequest build() {
        if (request.url == null || request.url.isEmpty()) {
            throw new RuntimeException("URL is required");
        }
        return request;
    }
}

// Usage
var request = new HttpRequestBuilder()
    .url("https://api.example.com/users")
    .method("POST")
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer token123")
    .body("{\"name\": \"John\"}")
    .timeout(60)
    .build();

request.send();
```

## Advantages

✅ **Readable code** - Method chaining makes construction clear
✅ **Optional parameters** - Add only needed parameters
✅ **Immutability** - Build then freeze object
✅ **Validation** - Validate in build() before returning
✅ **Flexibility** - Different builders for different needs
✅ **Separation** - Construction logic separate from business logic

## Disadvantages

❌ **Extra classes** - More code to write and maintain
❌ **Overhead** - Extra object allocation for builder
❌ **Complexity** - Overkill for simple objects
❌ **Learning curve** - Developers must understand pattern

## When to Use

✅ **Complex objects** - Many parameters or configuration
✅ **Optional parameters** - Some parameters optional
✅ **Different representations** - Same construction, different products
✅ **Immutable objects** - Build then freeze
✅ **Fluent API** - Method chaining for readability
✅ **Parameter validation** - Complex validation logic

## When NOT to Use

❌ Simple objects with few parameters
❌ All parameters required
❌ No optional parameters
❌ Single fixed representation

## Related Patterns

- **Composite** - Often uses builder to construct
- **Prototype** - Similar goal, different approach
- **Strategy** - Can use strategies in builder
- **Factory Method** - Can be used with builder

## Best Practices

1. **Use object references** - Rely on garbage collection to manage builder internal state
2. **Method chaining** - Return this from setters
3. **Validate in build()** - Check invariants before returning
4. **Provide defaults** - Sensible default values
5. **Immutable product** - Mark fields final if possible
6. **Descriptive names** - with, set, add prefixes
7. **Reset method** - Option to create multiple objects
8. **Thread safety** - If shared across threads

## Implementation Checklist

- [ ] Create complex object class
- [ ] Create builder class with all parameters
- [ ] Provide setter methods returning this
- [ ] Implement build() method
- [ ] Validate in build() before returning
- [ ] Provide sensible defaults
- [ ] Rely on garbage collection for state management
- [ ] Document all setter methods
- [ ] Test all parameter combinations
- [ ] Test validation logic
