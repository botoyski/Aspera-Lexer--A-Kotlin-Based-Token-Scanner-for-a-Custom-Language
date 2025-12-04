# Medieval Fantasy Character & Battle DSL
### Creator [Eryl Joseph Aspera, Antonio Gabriel Salmon]

## Language Overview

This interpreter implements a domain-specific language (DSL) for tabletop RPG character creation and scripted simulations, primarily focused on generating fantasy characters and running automated battles. The language supports two main sections: declarative character sheets starting with `CHARACTER` keywords and an optional imperative script section for general-purpose programming. It combines structured RPG data parsing with a Lox-like expression language for logic and control flow.

**Category:** Declarative, Functional, Interpreted  
**Typing:** Weak & Dynamic

---

## Built-in Value Types

From scanning literals, scanner rules, and interpreter behavior, the concrete data types are:

- **Number**  
  Parsed from tokens of type `NUMBER`, stored and operated on as `Double`.

- **String**  
  Produced by string literals, concatenation, native `concat`, and string indexing/assignment; used heavily in the character DSL (race, class, skills, etc.).

- **Boolean**  
  The keywords `true` and `false` create booleans; logic and conditionals treat any non-null, non-boolean value as truthy, with `nil` as falsey.

- **Nil**  
  The keyword `nil` denotes the absence of a value; also used as default for uninitialized variables and failed native calls (e.g., `stepBattle`).

- **Function / Callable**  
  User functions and built-in natives (`len`, `concat`, `stepBattle`) implement a `Callable` interface; they can be stored in variables and called with `()` syntax.

- **Character / DSL Records (Host Side)**  
  Character sheets and battle logic use structured Kotlin types (`Stmt.Character` plus maps for attributes and lists for skills) on the host side, but from the script’s viewpoint these are just printed output and not first-class script values yet.

---

## Keyword Groups

The language has two groups of keywords:

1. **Character-DSL keywords**
2. **Scripting keywords**

Each keyword has a fixed syntactic role and cannot be used as an identifier.

---

## Character DSL Keywords

These shape the tabletop character sheets and related data:

- **Character** – Starts a new character block.
- **Race** – Introduces the race field (e.g., Elf, Orc).
- **Class** – Introduces the class field (e.g., Warrior, Mage).
- **Background** – Introduces the background field (e.g., Noble, Outcast).
- **Attributes** – Starts the attribute section (`STR`, `DEX`, `INT`, `WIS`, `CHA`, `END`).
- **Skills** – Starts a comma-separated list of skills.
- **Equipment** – Starts the gear section (weapon, armor, accessory).
- **Magic Affinity** – Introduces the magic affinity field (Fire, Water, None, etc.).
- **Alignment** – Introduces the alignment field; the following text on the line is treated as the full alignment string.

There are also fixed tokens for:
- Attribute labels (`STR`, `DEX`, `INT`, `WIS`, `CHA`, `END`)
- Equipment labels (`Weapon`, `Armor`, `Accessory`)
- Race, class, background, skill, and magic options

These act as enumerated values rather than general control-flow keywords.


## Scripting Language Keywords

These implement the imperative, Lox-style scripting language.

- **var** – Declares a mutable variable, optionally with an initializer.
- **val** – (Reserved) Intended for immutable bindings; currently tokenized but not specially handled.
- **if** – Starts a conditional; used with `(condition)` and a following statement/block.
- **elif** – Else-if branch; parsed into chained `if` statements.
- **else** – Final fallback branch of an `if`/`elif` chain.
- **while** – Starts a while loop with `(condition)` and a body.
- **for** – Starts a C-style `for` loop, which desugars into an initializer plus a `while` loop with optional increment.
- **fun** – Declares a function with parameters and a block body.
- **return** – Exits the current function, optionally yielding a value.
- **print** – Statement keyword to evaluate an expression and print its string form.
- **true / false** – Boolean literals.
- **nil** – The “no value” literal.
- **and / or** – Logical operators for short-circuiting boolean expressions.

All other words (that do not match one of these) are treated as **identifiers**: variable names, function names, or DSL free-text (like specific race names already covered by the enumerated tokens).



## Arithmetic and Numeric Operators

- **Addition:** `+`
- **Subtraction:** `-`
- **Multiplication:** `*`
- **Division:** `/`

These expect **numeric operands** (numbers are stored as doubles), and arithmetic is **left-associative**.

---

## Comparison and Equality Operators

- **Less than:** `<`
- **Less than or equal:** `<=`
- **Greater than:** `>`
- **Greater than or equal:** `>=`
- **Equal to:** `==`
- **Not equal to:** `!=`

Used in `if`, `elif`, `while`, `for` conditions and anywhere a **boolean expression** is needed.

---

## Logical and Unary Operators

- **Logical AND:** `and`
- **Logical OR:** `or`
- **Logical NOT:** `!`
- **Unary minus (numeric negation):** `-` in front of a single expression

- `and` and `or` **short-circuit**
- `!` **flips truthiness**
- Unary `-` **negates a number**

---

## Assignment and Indexing Operators

- **Simple assignment:** `=`
    - Examples:
      ```
      x = 3
      x = x + 1
      ```

- **Indexing:** `[]`
    - Example:
      ```
      s[0]
      ```

- **Indexed assignment:** `=` after `[]`
    - Example:
      ```
      s[0] = "a"
      ```

Indexing currently only works on **strings**, and indexed assignment replaces a **single character position** in a string.

---

## Literal Categories

The language has **four literal categories**: numbers, strings, booleans, and `nil`. Each appears both in the scanner and in the parser’s primary expression rules.

---

## Number Literals

- **Form:** One or more digits, optionally with a decimal point and more digits
- **Stored as:** `Double`

**Examples:**
```
0
42
3.14
100.0
```
## String Literals

- **Form:** Text wrapped in double quotes `"..."`
- Newlines inside strings are allowed; the scanner tracks them until the closing `"`.

**Examples:**
```
"hello"
"Elf Ranger"
"Round 1: Battle starts!"
```

## Boolean literals
- Keywords: true and false.​

Examples:
true,
false

**Typical use:**
```
if (true) print "Always runs";

var flag = false;
```

## Nil literal
Keyword: nil.​
```

var result = nil;
return nil;
```

Represents “no value” / absence.


## What counts as an identifier
Any word that is not tokenized as a keyword, literal, or special DSL label is treated as an identifier.

These are used for:

Variable names 
```
(var hp = 10;)

Function names (fun attack(target) { ... })

Parameters (fun heal(amount) { ... })
```
From the scanner logic, a “word” token becomes an identifier when it does not match any of the special cases (like Character:, Race:, if, while, true, Fire, Sword, etc.), and is then given the generic IDENTIFIER token type.​

## Identifier syntax

Starts with a letter (or underscore, depending on your implementation) and can contain letters and digits after that (e.g., player1, battleResult, sum_str).

## Reserved names

Cannot be equal to:

- Scripting keywords: var, val, if, elif, else, while, for, fun, return, print, true, false, nil, and, or.
- Character DSL keywords/labels like Character:, Race:, Class:, Attributes:, etc., or enumerated values like Elf, Warrior, Fire when they are matched as special token types.​

## Examples of identifiers

- Variable names: hp, rounds, winnerName, message.
- Function names: battle, stepBattle, computeDamage.
- Parameters: attacker, defender, amount.

All of those are legal identifiers because they are not reserved words and they match the general “word” pattern the scanner uses for non‑keyword names.

## Comments

In this language, comments are single‑line and start with `//`.

Anything after `//` on the same line is ignored by the scanner and not turned into tokens.​

## Example in a script section:
```
// Initialize hit points
var hp = 10; // player starting HP
```
There is no support in the scanner for block comments like /* ... */, so only // style comments are valid.

## Overall style
The language has two faces: a declarative character section that looks like a structured form, and an imperative script section that looks like a small curly‑brace scripting language. In the script part you use var/fun, if (...) { ... }, while (...) { ... }, and for (...) { ... } with semicolons between statements, much like JavaScript or Kotlin.​

## Statements and blocks
A statement normally ends with ;, unless it is a block or control‑flow header. Blocks are wrapped in { ... } and contain zero or more statements, for example:​
```
var x = 10;
if (x > 0) {
print x;
}
```
Functions follow the same style: fun name(params) { ... } with a parameter list in () and a brace‑delimited body.​

## Expressions and precedence
Expressions are infix and use familiar operators: 
```
a + b * c
x < y, a == b
c != nil
```
Parentheses (...) group expressions, literals are 123, "text", true, false, nil, and you can call functions with name(arg1, arg2) or index into strings with s[0].​

## Character section style
Before the script, character definitions use a more “sheet‑like” syntax instead of code: lines such as Character:, Race: Elf, Class: Ranger, Attributes:, Skills: Archery, Stealth, Equipment:, Weapon = Bow. This section is keyword‑driven and line‑oriented, so it feels like filling in a form rather than writing code, but under the hood it is parsed by the same token/AST machinery as the script.​    

Sample Code
```
// Character sheet
CHARACTER
Race: Elf
Class: Ranger
Background: Outcast
Attributes:
STR = 10
DEX = 16
INT = 12
WIS = 14
CHA = 8
END = 20
Skills: Archery, Stealth
Equipment:
Weapon = Bow
Armor  = Leather
Accessory = Cloak
Alignment: Chaotic Good
Magic Affinity: Nature

// Script section
SCRIPT

fun greet(name) {
print "Welcome, " + name;
}

var rounds = 3;
for (var i = 0; i < rounds; i = i + 1) {
print "Round " + i;
}

greet("Ranger");
```

The language is designed to make two things easy: writing tabletop‑style character sheets and then scripting simple battle logic around those characters. Everything in the design serves those goals.

## Design Rationale Summary

- **Separation of Data and Logic**  
  Character blocks use a friendly, label-driven syntax (`Character:`, `Race: Elf`, `Skills: Archery, Stealth`) for easy reading and editing by non-programmers. Scripts use a traditional curly-brace language for loops, conditionals, and functions, keeping player content and designer rules separate.

- **Simple, Lox-Style Scripting Core**  
  The scripting language is small and familiar: `var`, `if`, `while`, `for`, `fun`, `return`, `print`, plus a few native functions like `len` and `concat`. This keeps the learning curve low while supporting non-trivial logic like battle simulations.

- **Minimal Dynamic Type System**  
  Runtime values are numbers, strings, booleans, `nil`, and callables. Types are dynamic, so errors appear only when a value is misused. This simplifies the interpreter and suits quick iteration rather than mission-critical code.

- **RPG-Specific Semantics**  
  The language encodes common RPG patterns: attributes sum to fixed totals, battle resolution is turn-based, and character roles emerge from stats, skills, and equipment. Users can prototype characters and combat systems without re-implementing core mechanics.

- **Host Integration and Extensibility**  
  Functions like `stepBattle` and the `Callable` abstraction allow the host program (Kotlin) to provide native behavior. Scripts remain small and declarative while performance-sensitive or complex logic resides in the host engine.

## Concrete Syntax Example
```
Character:
Race: Elf
Class: Ranger
Background: Outcast
Attributes:
STR = 10
DEX = 16
INT = 12
WIS = 14
CHA = 8
END = 20
Skills: Archery, Stealth
Equipment:
Weapon = Bow
Armor = Leather
Accessory = Cloak
Alignment: Chaotic Good
Magic Affinity: Nature

SCRIPT

print "Character demo running.";

What this does:

Parses one character, validates attributes, and prints a formatted character sheet.

Runs the script section and prints Character demo running. on the cons

Larger example: multiple characters + battles + script
Character:
Race: Human
Class: Warrior
Background: Mercenary
Attributes:
STR = 18
DEX = 12
INT = 8
WIS = 10
CHA = 10
END = 22
Skills: TwoHanded, Blacksmithing
Equipment:
Weapon = Sword
Armor = Plate
Accessory = Ring
Alignment: Lawful Neutral
Magic Affinity: None

Character:
Race: Elf
Class: Mage
Background: Scholar
Attributes:
STR = 8
DEX = 14
INT = 20
WIS = 16
CHA = 12
END = 10
Skills: ElementalMagic, Alchemy
Equipment:
Weapon = Staff
Armor = Robe
Accessory = Amulet
Alignment: Neutral Good
Magic Affinity: Fire

Character:
Race: Dwarf
Class: Ranger
Background: Hunter
Attributes:
STR = 14
DEX = 18
INT = 10
WIS = 12
CHA = 8
END = 18
Skills: Archery, Tracking
Equipment:
Weapon = Bow
Armor = Chainmail
Accessory = Charm
Alignment: Chaotic Neutral
Magic Affinity: Nature

SCRIPT

print "Custom script section starting.";

fun banner(title) {
print "=== " + title + " ===";
}

banner("Battle report");

// Example general-purpose script logic
var rounds = 3;
for (var i = 1; i <= rounds; i = i + 1) {
print "Round " + i;
}
```
### What this does

Builds three characters and prints their sheets.

Runs an automatic battle:

If there are 2 characters, a single 1‑v‑1 battle.

If there are more than 2 characters, a round‑robin where each pair fights once.

After battles, runs the script and prints the custom lines.

## Execution model and CLI usage
### Running programs
The entry point is main, which accepts either a file path or reads from standard input.

### From a file
```
kotlin MainKt myscript.txt
```
### From stdin
```
kotlin MainKt
```
Paste your program, then send an empty line to finish input.

## Splitting character vs script sections
The source is split into two logical parts inside run(source: String):

### Character section

Everything before the first line that contains the word SCRIPT (case‑insensitive) is treated as character input.​

At least one Character: block must appear here to trigger character parsing and evaluation.​

### Script section

Everything after the SCRIPT marker line is treated as the script program.​

This section is optional; if it is empty or whitespace, no script is run.​

What gets printed and in what order
When you run a program:

Character evaluation (if a character section exists):

### For each character:

The full character sheet is printed: race, optional class/background, attributes, validation messages (if any), skills, equipment, alignment, magic affinity, derived power score, story hook, and inferred roles (e.g., Tank, Ranged).​

Then, battles:

### If exactly 2 characters:

A “Battle Demo” header is printed.

A 1‑v‑1 battle runs until one character’s HP drops to 0, logging each round, damage, and the winner.​

### If more than 2 characters:

A “Round Robin Battles” header is printed.

Each pair fights once, printing per‑match logs and winners, followed by a “Round Robin Complete” line.​

Script execution (if the script section is non‑blank):

The script is scanned, parsed into statements, and interpreted in order.​

print statements in the script write directly to stdout, interleaved with any other output from functions you call.​

If there is no Character: block at all, the character evaluator is skipped and only the script part runs.

## Grammar / language reference (semi‑formal)
Character section grammar (informal EBNF)

Whitespace and newlines are mostly insignificant except where noted.
```
program        ::= { character_block } [ "SCRIPT" script_program ]
character_block ::= "Character:" newline
"Race:" race_type newline
[ "Class:" class_type newline ]
[ "Background:" background_type newline ]
"Attributes:" newline
attribute_line { attribute_line }
"Skills:" skill_list newline
"Equipment:" newline
equipment_block
"Alignment:" alignment_text newline
"Magic Affinity:" magic_type newline

attribute_line ::= IDENT "=" NUMBER newline
(* IDENT must be one of STR, DEX, INT, WIS, CHA, END *)

skill_list     ::= skill { "," skill }

equipment_block ::= "Weapon" "=" weapon_value newline
"Armor" "=" armor_value newline
"Accessory" "=" accessory_value newline

race_type      ::= one of: Orc | Human | Elf | Fairy | Spirit | Demihuman | Angel | Demon | Dwarf
class_type     ::= one of: Warrior | Knight | Mage | Thief | Ranger | Paladin
| Barbarian | Monk | Druid | Sorcerer | Warlock
background_type ::= one of: Noble | Commoner | Outcast | Mercenary
| Acolyte | Hermit | Scholar | Hunter | Nomad
skill          ::= one of: Tracking | Alchemy | Blacksmithing | Stealth | Healing
| TwoHanded | Archery | ElementalMagic | Cooking
weapon_value   ::= one of: Sword | Axe | Dagger | Bow | Spear | Staff
armor_value    ::= one of: Leather | Plate | Robe | Chainmail
accessory_value ::= one of: Ring | Amulet | Charm | Rune
magic_type     ::= one of: Fire | Water | Earth | Air | Light | Dark | Nature | Arcane | None

alignment_text ::= any non‑empty text up to end of line
```
Notes:

Attribute lines are parsed but currently ignored in favor of random, class‑biased generation; they are kept for possible manual mode later.​

Multiple Character: blocks in a row are allowed.

## Script section grammar (expressions and statements)
This is essentially a Lox‑style grammar.
```
script_program ::= { declaration }

declaration   ::= fun_decl
| var_decl
| statement

fun_decl      ::= "fun" IDENT "(" [ parameters ] ")" block
parameters    ::= IDENT { "," IDENT }

var_decl      ::= "var" IDENT [ "=" expression ] ";"

statement     ::= expr_stmt
| print_stmt
| if_stmt
| while_stmt
| for_stmt
| return_stmt
| block

expr_stmt     ::= expression ";"
print_stmt    ::= "print" expression ";"

if_stmt       ::= "if" "(" expression ")" statement
{ "elif" "(" expression ")" statement }
[ "else" statement ]

while_stmt    ::= "while" "(" expression ")" statement

for_stmt      ::= "for" "("
( var_decl | expr_stmt | ";" )
expression? ";"
expression?
")" statement

return_stmt   ::= "return" expression? ";"

block         ::= "{" { declaration } "}"

Expression grammar (with precedence from lowest to highest):
expression    ::= assignment

assignment    ::= ( index | IDENT ) "=" assignment
| logic_or

logic_or      ::= logic_and { "or" logic_and }
logic_and     ::= equality { "and" equality }

equality      ::= comparison { ( "==" | "!=" ) comparison }
comparison    ::= term { ( ">" | ">=" | "<" | "<=" ) term }
term          ::= factor { ( "+" | "-" ) factor }
factor        ::= unary { ( "*" | "/" ) unary }

unary         ::= ( "!" | "-" ) unary
| call

call          ::= primary { "(" arguments? ")" | "[" expression "]" }

arguments     ::= expression { "," expression }

primary       ::= NUMBER
| STRING
| "true"
| "false"
| "nil"
| IDENT
| "(" expression ")"
```
Indexing target[index] and index assignment target[index] = value are supported only when target is a string.​

All numbers are parsed as Double.

Runtime behavior details (semantics)
Truthiness
false and nil are falsey.

Everything else (numbers, strings, functions, objects) is truthy.​

This affects if, elif, while, for loop conditions, and logical and / or.

Numeric model
All numeric literals are stored as Double (Kotlin Double).​

Arithmetic operators (+, -, *, /, <, <=, >, >=) assume both operands are numbers and cast them to Double internally.​

Division is floating‑point division; integer division is not special‑cased.​

## Errors and failure behavior
### Runtime errors include:

Using non‑numeric values with arithmetic operators.

Calling something that is not a function.

Wrong number of arguments in a function call.

Indexing with a non‑integer index or indexing a non‑string value.

Indexing outside the string bounds.

Assigning to invalid targets (e.g., left side of = that is not a variable or supported index).​

### On a runtime error:

A RuntimeException is thrown with a message; main catches it and prints the error message to the console.​

There is no static type checking; all checks occur at runtime.

Character system specifics
Attribute generation
For each character:

Attributes are stored in a map with keys STR, DEX, INT, WIS, CHA, END and values between 1 and 20.​

Total sum of all attributes is 80.​

Generation is class‑biased random:

Start with each attribute at a minimum value (1).

### Distribute the remaining points according to class‑specific weights:

- Melee classes (warrior / knight / barbarian / paladin): heavier weight on STR and END, moderate DEX, lower INT/WIS/CHA.

- Casters (sorcerer / mage): heavier INT and CHA, moderate WIS, lighter STR/END.

- Rangers: heavier DEX and WIS, decent END, moderate STR.

- Other or unknown classes: roughly even weights across all attributes.​

Sampling stops when the total reaches 80 while respecting the 1–20 bounds.​

## Derived stats 
Given a character c:

### Hit points (HP):
```
HP = END(c)×2
```
where END(c) is the ENDURANCE attribute or 0 if missing.​

### Armor class (AC):
```
AC=10+DEX(c)/2
```
using integer arithmetic / rounding based on implementation.​

### Carry capacity:
```
CarryCapacity=STR(c)×5
```
### Power score:

PowerScore
=
```
PowerScore=∑(all attributes)+(skills count×2)+(magicBonus)
```
where magicBonus = 5 if magic affinity is not None, otherwise 0.​

## Battle algorithm
Battles are 1‑v‑1 between characters a and b.​

### Initialization:

```
hpA = hitPoints(a) and hpB = hitPoints(b).
```
A log line prints nameOf(a) vs nameOf(b) with their starting HP.​

### Rounds:
```
While both hpA > 0 and hpB > 0:
```
### A attacks B:

If a is a “wizard/fire user” (class is Sorcerer or Mage and magic affinity is Fire), there is a 30% chance to use a fire flurry:
```
fireFlurryDamage(attacker, defender, hitsLeft):
```
Uses INT plus a magic bonus (3 if affinity is Fire) as base damage, with small random variation and reduced armor effect.​

Recursively applies multiple small hits and sums the total.

### Otherwise, use physical damage:
```
computePhysicalDamage(attacker, defender):
```
- Base damage derived mainly from STR(attacker) plus a random bonus minus armorClass(defender) scaled.​

- Minimum damage of 1 per hit.

- Subtract damage from hpB and clamp at 0; print damage and remaining HP.

- If hpB reached 0, A wins the battle.​

### B attacks A (same logic mirrored):

- Use fire flurry if B qualifies and the chance roll succeeds; otherwise physical damage.

- Subtract damage from hpA, print, and end if hpA hits 0.​

- When one HP reaches 0, the winner is whichever character still has HP > 0.​

## Roles, tags, and story hooks
### Tank role:

A character is tagged as “Tank” if:
```
END >= 16 
```
and
Armor is one of Plate or Chainmail.​

### Ranged role:

A character is tagged as “Ranged” if:

Has Archery skill and Weapon is Bow or Spear.​

The printed “Roles” line may include both Tank and Ranged for hybrid characters.

## Story hook:

For each character, a short flavor text is generated based on race, background, alignment, class, and role.

There is a small set of hook templates (e.g., “An [alignment] [race] [background] who wanders the land…”, etc.), and one is chosen at random and filled with the character’s data.​

## Standard library / built‑ins
### Here are the built‑in functions available in the script environment:


- len(s)	s: string	Returns the length of the string as a number.
- concat(a,b)	a: string, b: string	Returns the concatenation of the two strings.
- stepBattle()	Calls an optional host‑provided callback for one “battle step”; by default does nothing and returns nil.

## Details:

### len(x):

If x is not a string, raises a runtime error: “len argument must be a string”.​

### concat(a, b):

Both a and b must be strings; otherwise a runtime error is raised.​

### stepBattle():

Exposed as a Callable that, when called, invokes a Kotlin function if one was provided to the interpreter; otherwise returns nil.​

You can also define your own functions in the script using fun name(params) { ... }, and they behave like any other Callable.
























