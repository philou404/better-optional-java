```markdown
# Opt - A Functional Option Type for Java

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)  
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)  
[![Stars](https://img.shields.io/github/stars/philou404/better-optional-java)](https://github.com/philou404/better-optional-java/stargazers)

`Opt<T>` is a powerful functional alternative to `Optional<T>` in Java, inspired by Rust and leveraging a *sealed* hierarchy to enforce strict type control.

---

## Features

- **Sealed Hierarchy**: Prevents uncontrolled subclassing with `Some<T>`, `None<T>`, and `Lazy<T>`.
- **Pattern Matching with Visitor**: Type-safe and extensible functional pattern matching.
- **Monadic API**: Includes `map`, `flatMap`, `zip`, `fold`, `ap`, `filter`, and more.
- **Functional Utilities**: Handy methods like `ifPresentOrElse`, `orElseThrow`, `toOptional`, `stream`, `flatten`, etc.
- **Interoperability**: Converts to and from `Optional<T>`, integrates with Java Streams and Iterators.
- **Optimized Performance**: Eliminates `null` checks and `try-catch` blocks, making code cleaner and safer. 

---

## Usage

### Creating Opt Instances

```java
Opt<String> someValue = Opt.of("Hello, World!");
Opt<String> noneValue = Opt.none();
Opt<Integer> emptyValue = Opt.ofNullable(null); // Returns Opt.None<Integer>
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
transformed.ifPresent(System.out::println); // Output: "Number: 42"

Opt<Integer> filtered = number.filter(n -> n > 50); // Returns None since 42 is not > 50
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

### Pattern Matching with `switch`

```java
Opt<Integer> i = Opt.none();

switch (i) {
    case Opt.Some<Integer> x -> System.out.println("Value: " + x.get());
    case Opt.None<Integer> x -> System.out.println("None"); // Output: "None"
}
```

---

## Performance Considerations

- **Optimized Execution**: `Opt<T>` avoids `null` checks and exceptions, ensuring efficient and safe value handling.
- **Seamless Stream Integration**: Methods like `flatten()` and `sequence()` enable smooth stream processing.

---

## License

This project is licensed under the MIT License – see the [LICENSE](LICENSE) file for details.

---

## Contributing

Feel free to fork and submit pull requests! Contributions, bug reports, and feature requests are always welcome. 😊

---

## Show Your Support

If you find this project useful, consider giving it a star to show your appreciation! ⭐

---

## FAQ

**Why use a sealed class?**  
- The sealed hierarchy ensures that only predefined implementations (`Some`, `None`) can exist, enhancing safety and predictability.

**Why implement so many methods?**  
- The goal is to provide a rich and expressive API that simplifies functional value handling, drawing inspiration from Rust while leveraging modern Java features.

---

Explore the code and contribute to improving this functional `Optional` monad for Java! 
