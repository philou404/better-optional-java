# Opt - A Functional Option Type for Java

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Stars](https://img.shields.io/github/stars/philou404/better-optional-java)](https://github.com/philou404/better-optional-java/stargazers)

`Opt<T>` is an advanced functional alternative to `Optional<T>` in Java.

## Features

- **Sealed Hierarchy**: `Some<T>`, `None<T>`.
- **Visitor Pattern**: Type-safe and extensible functional pattern matching.
- **Monadic API**: `map`, `flatMap`, `zip`, `fold`, `ap`, `filter`, etc.
- **Functional Utilities**: `ifPresentOrElse`, `orElseThrow`, `toOptional`, `stream`, `flatten`, etc.
- **Interoperability**: Converts to and from `Optional<T>`, supports Java Streams.

---

## Usage

### Creating Opt Instances

```java
Opt<String> someValue = Opt.of("Hello, World!");
Opt<String> noneValue = Opt.none();
Opt<Integer> emptyValue = Opt.ofNullable(null); // Opt.None<Integer>
```

### Basic Operations

```java
String result = someValue.orElse("Default Value");  // "Hello, World!"
String result2 = noneValue.orElse("Default Value"); // "Default Value"
```

### Functional Composition

```java
Opt<Integer> number = Opt.of(42);
Opt<String> transformed = number.map(n -> "Number: " + n);
transformed.

ifPresent(System.out::println); // Output: "Number: 42"

Opt<Integer> filtered = number.filter(n -> n > 50); // None, since 42 is not > 50
```

### Pattern Matching with Visitor

```java
OptVisitor<Integer, String> visitor = new OptVisitor<>() {
    @Override
    public String visit(Opt.Some<Integer> some) {
        return "Got a value: " + some.get();
    }

    @Override
    public String visit(Opt.None<Integer> none) {
        return "No value present";
    }
};

Opt<Integer> value = Opt.of(10);
String message = value.accept(visitor);
System.out.println(message); // Output: "Got a value: 10"
```

## Performance Considerations

- `Opt<T>` avoids `null` checks and `try-catch` blocks, making code cleaner and safer.
- Supports **stream processing** with `flatten()` and `sequence()`.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Contributing

Feel free to fork and submit pull requests! Contributions, issues, and feature requests are welcome.

---

## Star the Repository ⭐

If you find this project useful, consider giving it a star to show your support!

## F.A.Q.

- Why do I code with a sealed class ?
- I think I've implemented the maximum number of methods possible and the two values present in an optional monad. I've
  taken my inspiration mainly from rust for the base, but as we don't have the same enumeration as in rust. I've used a
  sealed abstract class to limit the possible types.
