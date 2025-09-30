**YoungStunna**

### Creator
Eryl Joseph Aspera, Antonio Gabriel Salmon 

---

### Language Overview

YoungStunna is a small, experimental programming language designed for learning how compilers and interpreters work. It uses a hand-written scanner (lexer) to break source code into tokens. The language includes variables, control flow statements, literals, and basic operators. Its syntax is similar to languages like Kotlin and JavaScript but is deliberately simplified for educational purposes.

---

### Keywords

Reserved words that cannot be used as identifiers:

* `var` – Declare a mutable variable
* `val` – Declare an immutable variable
* `if` – Conditional branching
* `else` – Alternative branch for `if`
* `true` – Boolean literal for truth
* `false` – Boolean literal for falsehood
* `nil` – Null/empty value literal
* `for` – Looping construct
* `while` – Looping construct
* `print` – Output to console
* `return` – Return from a function

---

### Operators

**Arithmetic Operators**

* `+` (addition)
* `-` (subtraction/negation)
* `*` (multiplication)
* `/` (division)

**Comparison Operators**

* `==` (equal to)
* `!=` (not equal to)
* `<` (less than)
* `<=` (less than or equal)
* `>` (greater than)
* `>=` (greater than or equal)

**Logical Operators**

* `!` (logical NOT)

**Grouping & Delimiters**

* `(`, `)` – Parentheses for grouping and calls
* `{`, `}` – Braces for blocks
* `,` – Separator (e.g., in argument lists)
* `;` – Statement terminator
* `.` – Member access

---

### Literals

* **Numbers** – Integers and floating-point numbers (e.g., `123`, `3.14`)
* **Strings** – Double-quoted (`"Hello world"`)
* **Booleans** – `true` and `false`
* **Nil** – `nil` represents absence of a value

---

### Identifiers

* Must start with a letter (`a–z`, `A–Z`) or underscore `_`
* May contain letters, digits (`0–9`), or underscores after the first character
* Are **case-sensitive** (`var count` and `var Count` are different)

---

### Comments

* **Single-line comment**: `// This is a comment`
* **Block comment**: `/* This is a block comment */`
* Nested block comments are **not supported**

---

### Syntax Style

* Whitespace (` `, `\t`, `\r`, `\n`) is ignored except as a separator
* Statements are terminated by a semicolon `;`
* Blocks are enclosed in braces `{ ... }`
* Newlines increment the line count but are otherwise not significant

---

### Sample Code

```sahh
var x = 10;
val y = 20;

if (x < y) {
    print "x is less than y";
} else {
    print "x is greater or equal to y";
}

for (var i = 0; i < 5; i = i + 1) {
    print "Loop iteration " + i;
}
```

---

### Design Rationale

The goal of YoungStunna is clarity and simplicity of the real ones. The token set is minimal but expressive enough to demonstrate how real-world languages are scanned and parsed.

* **Kotlin-style keywords** (`val`, `var`) are included to show mutability rules.
* **JavaScript-like syntax** (`;`, `{}`) makes it easy to read for beginners.
* **Nil literal** demonstrates null-handling.
* Comments support both single-line and block styles for flexibility.
* Identifiers and operators are chosen to be familiar but kept minimal.

This design balances educational value with familiarity, so learners can focus on compiler construction without being overwhelmed by complexity.
