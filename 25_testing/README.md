# Chapter 25: Testing -- Java

Automated tests are code that checks your code: they encode your expectations as executable assertions, run in seconds, and fail loudly the moment a change breaks behavior. They are what lets you refactor fearlessly, document intent precisely, and catch regressions before users do. Java's testing ecosystem is mature and standardized around **JUnit 5** (the *Jupiter* generation) for the test framework, **Mockito** for test doubles, and **AssertJ** for fluent assertions, all driven by Maven (Surefire) or Gradle. C++ has analogous frameworks (GoogleTest, Catch2, doctest), but no single de-facto standard the way JUnit owns the JVM — and nothing as deeply integrated into the build tools.

This chapter covers why and how to test: the testing pyramid and a word on TDD; the JUnit 5 essentials (`@Test`, the lifecycle annotations, the assertion family including `assertThrows` and `assertAll`, `@DisplayName`, `@Disabled`); data-driven testing with `@ParameterizedTest` and its sources; assumptions, `@Nested` grouping, tagging, and ordering; brief tours of Mockito and AssertJ; how to run tests under Maven and Gradle; and the practices that separate tests that help from tests that rot. The companion `code_examples/chapter25_testing.java` is a *self-contained, dependency-free* program — a tiny hand-rolled harness — because JUnit is not on this book's bare classpath; it also contains, in comments, the equivalent JUnit 5 test so you can see the real thing.

> **C++ testing → Java/JUnit — at a glance**
> - GoogleTest `TEST(Suite, Name)` / Catch2 `TEST_CASE` → `@Test` method
> - `EXPECT_EQ` / `ASSERT_EQ` / `REQUIRE` → `assertEquals`, `assertTrue`, ...
> - `EXPECT_THROW(expr, Ex)` → `assertThrows(Ex.class, () -> expr)`
> - Fixtures (`SetUp`/`TearDown`) → `@BeforeEach` / `@AfterEach` (+ `@BeforeAll`/`@AfterAll`)
> - `TEST_P` / `INSTANTIATE_TEST_SUITE_P` → `@ParameterizedTest` + `@ValueSource`/`@CsvSource`/`@MethodSource`
> - GoogleMock `MOCK_METHOD` / `EXPECT_CALL` → Mockito `mock` / `when().thenReturn()` / `verify()`
> - `ctest` / build-integrated runner → Maven Surefire / Gradle `test`

## 25.1 Why Test, the Testing Pyramid, and TDD

Tests pay for themselves by catching regressions, pinning down behavior so it can be safely changed, and serving as living documentation of how code is meant to be used. The **testing pyramid** is the guiding shape: a broad base of fast, isolated **unit tests** (one class/method, no I/O), a thinner middle of **integration tests** (several components together — a real database, a DAO against H2, an HTTP call), and a narrow top of slow **end-to-end / system tests** (the whole application through its public interface). The pyramid says: write *many* unit tests and *few* e2e tests, because unit tests are fast, stable, and pinpoint failures, while e2e tests are slow and flaky and tell you only that *something* broke.

| Level | Scope | Speed | How many |
|-------|-------|-------|----------|
| **Unit** | One class/method, dependencies mocked | Milliseconds | Many (the base) |
| **Integration** | Several real components (DB, HTTP, files) | Seconds | Some (the middle) |
| **End-to-end** | Whole system through its UI/API | Seconds–minutes | Few (the top) |

**Test-Driven Development (TDD)** inverts the usual order into a tight loop: **Red** (write a failing test for the next small behavior), **Green** (write the minimum code to pass), **Refactor** (clean up with the test as a safety net), then repeat. Even if you do not practice strict TDD, writing the test first clarifies the API you actually want and guarantees the test can fail.

---

## 25.2 JUnit 5 Basics — @Test and the Lifecycle

A JUnit 5 test is a plain method annotated **`@Test`** (no `public` required, unlike JUnit 4). The class needs no special base class or naming. JUnit runs each `@Test` method, and the test **passes** unless an assertion fails or an unexpected exception escapes. The **lifecycle** annotations create and tear down fixtures: `@BeforeEach`/`@AfterEach` run before/after *every* test method (a fresh fixture per test — the default for isolation), while `@BeforeAll`/`@AfterAll` run *once* for the whole class and must be `static` (use them only for expensive, immutable shared setup like starting a test container).

```java
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    Calculator calc;   // a fresh instance per test, set up below

    @BeforeAll
    static void initAll() {
        System.out.println("Runs ONCE before all tests in this class");
    }

    @BeforeEach
    void setUp() {
        calc = new Calculator();   // re-created before EVERY test → isolation
    }

    @Test
    void addsTwoNumbers() {
        assertEquals(5, calc.add(2, 3));
    }

    @AfterEach
    void tearDown() {
        // release per-test resources here (close files, etc.)
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("Runs ONCE after all tests");
    }
}
```

> **Contrast with C++:** GoogleTest's `TEST_F` fixture class with `SetUp()`/`TearDown()` maps directly onto `@BeforeEach`/`@AfterEach`. JUnit constructs a **new instance of the test class for each test method** by default, which is why a plain field assigned in `@BeforeEach` is automatically isolated — there is no shared mutable state leaking between tests unless you deliberately use `static`/`@BeforeAll`.

---

## 25.3 Assertions

Assertions are the checks that decide pass or fail; a failed assertion throws and marks the test failed. JUnit 5's assertions live as static methods on `org.junit.jupiter.api.Assertions` (statically imported above). The core family:

| Assertion | Checks |
|-----------|--------|
| `assertEquals(expected, actual)` | equal (note the **expected-first** order) |
| `assertNotEquals(a, b)` | not equal |
| `assertTrue(cond)` / `assertFalse(cond)` | a boolean condition |
| `assertNull(x)` / `assertNotNull(x)` | null / non-null |
| `assertSame(a, b)` / `assertNotSame` | same reference (`==`) |
| `assertArrayEquals(e, a)` | arrays element-wise equal |
| `assertThrows(Ex.class, executable)` | the code throws that exception (returns it) |
| `assertDoesNotThrow(executable)` | the code throws nothing |
| `assertAll(...)` | groups checks; reports **all** failures, not just the first |
| `assertTimeout(dur, exec)` | completes within a duration |

```java
@Test
void demonstratesAssertions() {
    assertEquals(4, 2 + 2);
    assertTrue("hello".startsWith("he"));
    assertNotNull(new Object());

    // assertThrows captures and RETURNS the exception so you can inspect it.
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> Integer.parseInt("not a number"));   // any throwing lambda
    assertTrue(ex.getMessage().contains("not a number"));

    // assertAll runs every check and reports ALL failures together,
    // rather than stopping at the first — great for validating an object.
    var p = new Person("Alice", 30);
    assertAll("person",
        () -> assertEquals("Alice", p.name()),
        () -> assertEquals(30, p.age()),
        () -> assertTrue(p.age() >= 18));

    // The last argument can be a message (use a Supplier to build it lazily).
    assertEquals(5, 2 + 3, () -> "math should still work");
}
```

> **Contrast with C++:** GoogleTest distinguishes `EXPECT_*` (non-fatal — continue) from `ASSERT_*` (fatal — abort the test). JUnit assertions all abort the current test on failure; `assertAll` is how you get the "report every failure" behavior of grouped `EXPECT_*`. JUnit also puts **expected before actual** — the reverse of some C++ frameworks — so a swapped order produces a misleading failure message.

---

## 25.4 @DisplayName and @Disabled

Two annotations improve test readability and let you skip tests. **`@DisplayName`** gives a test (or class) a human-readable name shown in reports, free of method-name camelCase. **`@Disabled`** skips a test (optionally with a reason) — far better than commenting it out, because the runner reports it as *skipped* so it is not silently forgotten.

```java
@DisplayName("Order pricing rules")
class OrderPricingTest {

    @Test
    @DisplayName("applies 10% discount for orders over $100")
    void appliesBulkDiscount() {
        assertEquals(90.0, new Order(100.0).discountedTotal());
    }

    @Test
    @Disabled("flaky until the rounding bug TICKET-1234 is fixed")
    void roundsToTwoDecimals() {
        // temporarily skipped — still listed in the report as skipped
    }
}
```

---

## 25.5 Parameterized Tests

When the *same* test logic should run against many inputs, a **`@ParameterizedTest`** replaces copy-pasted `@Test` methods. Instead of `@Test`, annotate with `@ParameterizedTest` and add a **source** that supplies the arguments; JUnit runs the method once per argument set. The common sources:

- **`@ValueSource`** — a single array of literals (`ints`, `strings`, ...).
- **`@CsvSource`** — inline comma-separated rows, mapping to multiple parameters.
- **`@MethodSource`** — a `static` method returning a `Stream<Arguments>`, for complex or computed inputs.
- (`@EnumSource`, `@NullSource`, `@EmptySource` round out the set.)

```java
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    // One parameter, several literal values → runs 4 times.
    @ParameterizedTest
    @ValueSource(strings = {"racecar", "level", "noon", "civic"})
    void detectsPalindromes(String word) {
        assertTrue(StringUtil.isPalindrome(word));
    }

    // Multiple parameters per row via CSV.
    @ParameterizedTest
    @CsvSource({
        "abc, cba",
        "hello, olleh",
        "'', ''"            // quotes denote an empty string
    })
    void reversesStrings(String input, String expected) {
        assertEquals(expected, StringUtil.reverse(input));
    }

    // A method supplying typed argument tuples for richer cases.
    @ParameterizedTest
    @MethodSource("additionCases")
    void adds(int a, int b, int sum) {
        assertEquals(sum, a + b);
    }

    static Stream<Arguments> additionCases() {
        return Stream.of(
            Arguments.of(1, 1, 2),
            Arguments.of(-1, 1, 0),
            Arguments.of(Integer.MAX_VALUE, 0, Integer.MAX_VALUE));
    }
}
```

> **Contrast with C++:** This is JUnit's answer to GoogleTest's value- and type-parameterized tests (`TEST_P` + `INSTANTIATE_TEST_SUITE_P`), but far less ceremonious — a single annotation with an inline data source instead of a separate fixture and instantiation macro.

---

## 25.6 Assumptions, Nested Tests, Tagging, Ordering

**Assumptions** (`Assumptions.assumeTrue`/`assumeFalse`) abort a test as *skipped* — not failed — when a precondition does not hold, e.g. an environment-specific test that should only run on CI or a particular OS. **`@Nested`** groups related tests in inner classes, producing a readable tree in reports and letting each group share its own `@BeforeEach` context (ideal for "given an empty stack / given a non-empty stack" structures). **Tagging** (`@Tag("slow")`) labels tests so the build can include or exclude categories. **Ordering** is *off by default on purpose* — tests should be independent — but `@TestMethodOrder` with `@Order` enforces a sequence when you genuinely need one.

```java
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@DisplayName("A stack")
class StackTest {

    @Test
    void runsOnlyOnCi() {
        assumeTrue("true".equals(System.getenv("CI")));  // else: skipped, not failed
        // ... CI-only logic
    }

    @Nested
    @DisplayName("when empty")
    class WhenEmpty {
        Deque<Integer> stack = new ArrayDeque<>();

        @Test
        @Tag("fast")
        void isEmpty() {
            assertTrue(stack.isEmpty());
        }

        @Test
        void poppingThrows() {
            assertThrows(NoSuchElementException.class, stack::pop);
        }
    }
}

// Ordering: opt in explicitly when a sequence truly matters.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderedTest {
    @Test @Order(1) void first()  { /* ... */ }
    @Test @Order(2) void second() { /* ... */ }
}
```

---

## 25.7 Mocking with Mockito

A **unit** test should exercise one class in isolation, but real classes have collaborators (a `PersonDao`, an HTTP client, a clock). A **mock** is a stand-in for a collaborator whose behavior you script and whose interactions you can verify — so you test your class's logic without a real database or network. **Mockito** is the standard mocking library: `mock(Type.class)` creates a fake, `when(fake.method(args)).thenReturn(value)` stubs its responses, and `verify(fake).method(args)` asserts the fake was called as expected. Mock at the *seams* — the interfaces your code depends on — not the class under test itself.

```java
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    @Test
    void returnsGreetingForKnownUser() {
        // 1. Create a mock of the collaborator (no real DB needed).
        UserRepository repo = mock(UserRepository.class);

        // 2. Stub: define what the mock returns for a given call.
        when(repo.findName(42)).thenReturn("Alice");

        // 3. Exercise the class under test, injecting the mock.
        UserService service = new UserService(repo);
        String greeting = service.greet(42);

        // 4. Assert on the result...
        assertEquals("Hello, Alice", greeting);

        // 5. ...and verify the collaborator was used as expected.
        verify(repo).findName(42);
        verify(repo, never()).deleteAll();   // and NOT called in unexpected ways
    }
}
```

> **Why mock?** Speed and isolation: a mocked repository returns instantly and deterministically, so the test is fast and stable and fails only when *your* logic is wrong — not because a database was down. The DAO interface from Chapter 24 is exactly the kind of seam you mock. **Don't over-mock**, though: mocking value objects or the class under test couples tests to implementation detail and is a common smell.

> **Contrast with C++:** This mirrors GoogleMock (`MOCK_METHOD` to declare, `EXPECT_CALL(...).WillOnce(Return(...))` to stub and verify). Mockito needs no code generation or macros because it builds the mock at runtime via dynamic proxies/bytecode.

---

## 25.8 AssertJ — Fluent Assertions

**AssertJ** is an optional assertion library offering a fluent, chainable, discoverable style: every assertion starts with `assertThat(actual)` and you chain readable conditions. The chaining reads like a sentence, IDE autocompletion guides you to the right check, and failure messages are richer than the plain JUnit assertions — especially for collections and strings.

```java
import static org.assertj.core.api.Assertions.*;

@Test
void fluentAssertions() {
    String name = "Alice";
    assertThat(name).isNotEmpty()
                    .startsWith("Al")
                    .endsWith("ce")
                    .hasSize(5);

    var nums = java.util.List.of(1, 2, 3, 4);
    assertThat(nums).hasSize(4)
                    .contains(2, 3)
                    .doesNotContain(5)
                    .allMatch(n -> n > 0);

    assertThatThrownBy(() -> Integer.parseInt("x"))
        .isInstanceOf(NumberFormatException.class);
}
```

JUnit's built-in assertions and AssertJ are interchangeable; many teams adopt AssertJ for its readability on collections and custom objects. Use one consistently within a project.

---

## 25.9 Running Tests — Maven and Gradle

Both major build tools run JUnit 5 automatically: place tests under `src/test/java`, declare the dependency, and the build's test phase discovers and runs everything. Maven uses the **Surefire** plugin (bound to the `test` phase); Gradle has a built-in `test` task. Test source and `src/main/java` production code are compiled and packaged separately, so test code and test-only dependencies never ship in your artifact.

**Maven** (`pom.xml`) — the `junit-jupiter` aggregator pulls in the API and engine:

```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.11.3</version>
        <scope>test</scope>          <!-- test classpath only -->
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.14.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

```bash
mvn test                  # compile + run all tests via Surefire
mvn -Dtest=CalculatorTest test    # run a single test class
```

**Gradle** (`build.gradle`) — call `useJUnitPlatform()` so the `test` task runs Jupiter:

```groovy
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.11.3'
    testImplementation 'org.mockito:mockito-core:5.14.2'
}
test {
    useJUnitPlatform()        // run JUnit 5 (the JUnit Platform)
}
```

```bash
gradle test               # run all tests
gradle test --tests CalculatorTest    # run a single class
```

> **Contrast with C++:** C++ has no universal build-integrated test runner; you typically register tests with CMake/CTest and link the framework manually. On the JVM, `mvn test` / `gradle test` is the one-liner everyone uses, and CI tooling understands the standardized XML reports both produce.

---

## 25.10 Best Practices

```text
✅ Arrange-Act-Assert: set up inputs, invoke the behavior, then assert — in that order.
✅ One concept per test: a test should fail for exactly one reason. Many small focused
   tests beat one giant test (use assertAll for related checks on one result).
✅ Fast, Independent, Repeatable: no test depends on another's order or leftover state,
   no real clock/network/sleep, same result every run. (The "F.I.R.S.T." principles.)
✅ Name tests for behavior, not implementation: `withdraw_failsWhenBalanceTooLow`,
   not `test1`. @DisplayName for readable reports.
✅ Test behavior through the public API, not private internals — so you can refactor
   the implementation without rewriting the tests.
✅ Cover the edges: empty, null, boundary, and error cases — not just the happy path.
✅ Keep tests deterministic: inject a fixed Clock/Random/seed instead of using now()/random.
✅ Mock at the seams (interfaces/collaborators); don't mock value objects or the SUT.

❌ Don't write tests that only restate the implementation (assert a getter returns the
   field) — they pass trivially and catch nothing.
❌ Don't share mutable state between tests; prefer a fresh fixture in @BeforeEach.
❌ Don't ignore a flaky test by disabling it forever — fix the nondeterminism.
```

A good test suite is **fast** (so you run it constantly), **trustworthy** (a green run means the code works; a red run means a real bug), and **maintainable** (it tests *behavior*, so refactoring the implementation does not cascade into rewriting tests). Tests that are slow, flaky, or coupled to internals get ignored — and an ignored test suite protects nothing.

---

## Summary

| Concept | JUnit 5 / tooling |
|---------|-------------------|
| **A test** | `@Test` method (no `public` needed) |
| **Per-test setup/teardown** | `@BeforeEach` / `@AfterEach` |
| **Once-per-class setup/teardown** | `@BeforeAll` / `@AfterAll` (`static`) |
| **Core assertions** | `assertEquals`, `assertTrue`, `assertNull`, `assertSame` |
| **Exception testing** | `assertThrows(Ex.class, executable)` |
| **Group checks** | `assertAll(...)` — reports all failures |
| **Readable names / skipping** | `@DisplayName`, `@Disabled` |
| **Data-driven tests** | `@ParameterizedTest` + `@ValueSource`/`@CsvSource`/`@MethodSource` |
| **Conditional skip** | `Assumptions.assumeTrue` |
| **Grouping / labels / order** | `@Nested`, `@Tag`, `@TestMethodOrder` + `@Order` |
| **Mocking** | Mockito: `mock`, `when().thenReturn()`, `verify()` |
| **Fluent assertions** | AssertJ: `assertThat(x)...` |
| **Run tests** | `mvn test` (Surefire) / `gradle test` (`useJUnitPlatform()`) |

---

## Next Steps
- Read and run `code_examples/chapter25_testing.java` (no dependencies needed) to see assert/pass-fail mechanics, then read its commented JUnit 5 equivalent
- Add JUnit 5 to a project and convert that harness into real `@Test` methods
- Write parameterized tests for a function with many input/output pairs
- Introduce Mockito to unit-test a class that depends on the Chapter 24 DAO
- Wire `mvn test` / `gradle test` into a CI pipeline
- Move to [Chapter 26: Build Tools](../26_build_tools/README.md)
