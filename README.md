# MythWeaver Character & Battle DSL

## Creator
Eryl Joseph Aspera, Antonio Gabriel Salmon

---

## Language Overview

MythWeaver is a small, interpreter-based language project extended with a domain-specific layer for creating fantasy RPG-style characters and simulating battles between them. It combines:

- A core expression/statement language (variables, control flow, functions) used in course labs.
- A mini DSL for describing characters (race, class, attributes, skills, equipment, alignment, magic affinity).
- A Kotlin evaluator that computes derived stats and runs a turn-based combat loop using while-loops and recursive abilities.

The goal is to make control flow, functions, and recursion tangible by driving a small game-like system instead of just printing numbers.

---

## Category & Typing

- **Category:** Imperative, interpreted, expression-based
- **Typing:** Dynamic in the core language; character attributes and enumerations are validated by the evaluator.

---

## Data Model

### Core Language Values

- **Number** – Integer or floating-point numeric values.
- **String** – Double-quoted text used for messages, names, and descriptions.
- **Boolean** – `true` or `false` for conditions and control flow.
- **Nil** – Represents “no value”.

### Character DSL Types (Conceptual)

Represented as Kotlin data structures and AST nodes:

- **Character**
    - `race` (e.g., Elf, Human)
    - `clazz` (e.g., Ranger, Wizard)
    - `background` (e.g., Outcast, Scholar)
    - `attributes` map (`STR`, `DEX`, `INT`, `WIS`, `CHA`, `END`)
    - `skills` list (e.g., Tracking, Archery, ElementalMagic)
    - `weapon`, `armor`, `accessory`
    - `alignment` (e.g., Chaotic Good, Neutral Good)
    - `magicAffinity` (e.g., Nature, Fire, None)

- **Derived Stats**
    - **Armor Class (AC)** – based on `DEX` and armor.
    - **Hit Points (HP)** – based on `END`.
    - **Carry Capacity** – based on `STR`.
    - **Power Score** – aggregate of attributes, skills, and magic affinity.

- **Roles & Tags**
    - High-level classification such as `Tank` or `Ranged`, inferred from stats and equipment.

---

## Core Language Keywords

Reserved words that cannot be used as identifiers:

- `var` – Declare a mutable variable
- `val` – Declare an immutable variable
- `if` – Conditional branching
- `else` – Alternative branch for `if`
- `true` – Boolean literal for truth
- `false` – Boolean literal for falsehood
- `nil` – Null/empty value literal
- `for` – Looping construct
- `while` – Looping construct
- `fun` – Declare a function
- `return` – Return from a function
- `print` – Output to console

---

## Character DSL Keywords & Labels

The character description format uses fixed labels recognized by the scanner and parser:

### Section Headers

- `Character:` – Start of a new character block
- `Race:` – Race value
- `Class:` – Class value
- `Background:` – Background value
- `Attributes:` – Attribute assignments
- `Skills:` – Skill list
- `Equipment:` – Weapon/Armor/Accessory in one line
- `Alignment:` – Full alignment string (can contain spaces)
- `Magic Affinity:` – Magic affinity value

### Attribute Keys

Used inside the `Attributes:` line:

- `STR`, `DEX`, `INT`, `WIS`, `CHA`, `END`

### Equipment Labels

- `Weapon`
- `Armor`
- `Accessory`

### Enumerated Values (Examples)

These are tokenized as specific types in the scanner:

- **Races:** `Orc`, `Human`, `Elf`, `Fairy`, `Spirit`, `Demihuman`, `Angel`, `Demon`, `Dwarf`
- **Classes:** `Warrior`, `Knight`, `Mage`, `Thief`, `Ranger`, `Paladin`, `Barbarian`, `Monk`, `Druid`, `Sorcerer`, `Warlock`, `Wizard`
- **Backgrounds:** `Noble`, `Commoner`, `Outcast`, `Mercenary`, `Acolyte`, `Hermit`, `Scholar`, `Hunter`, `Nomad`
- **Skills:** `Tracking`, `Alchemy`, `Blacksmithing`, `Stealth`, `Healing`, `TwoHanded`, `Archery`, `ElementalMagic`, `Cooking`
- **Weapons:** `Sword`, `Axe`, `Dagger`, `Bow`, `Spear`, `Staff`
- **Armor:** `Leather`, `Plate`, `Robe`, `Chainmail`
- **Accessories:** `Ring`, `Amulet`, `Charm`, `Rune`
- **Magic Affinity:** `Fire`, `Water`, `Earth`, `Air`, `Light`, `Dark`, `Nature`, `Arcane`, `None`

---

## Operators

### Arithmetic

- `+` – Addition
- `-` – Subtraction / unary negation
- `*` – Multiplication
- `/` – Division

### Comparison

- `<`, `<=`, `>`, `>=` – Relational comparisons
- `==`, `!=` – Equality / inequality

### Logical

- `!` – Logical NOT
- `and` – Logical AND (short-circuit)
- `or` – Logical OR (short-circuit)

### Assignment & Punctuation

- `=` – Assignment in expressions and attribute specifications (e.g., `STR=15`)
- `,` – Separator (skills list, attributes list)
- `:` – Used in section labels (e.g., `Race:`)

---

## Literals

### Core Literals

- **Numbers**
    - Integers: `0`, `1`, `25`, `300`
    - (Optional) Floats: `0.5`, `3.14`
- **Strings**
    - Double-quoted, e.g. `"Elf"`, `"Chaotic Good"`, `"Battle start!"`
- **Booleans**
    - `true`, `false`
- **Nil**
    - `nil` for “no value”

### Character DSL Literals

- Integer values for attributes, e.g. `STR=15`, `DEX=18`.
- Alignment values scanned as one token, e.g. `Alignment: Chaotic Good`.

---

## Identifiers

- Start with a letter or `_`.
- May contain letters, digits, and underscores after the first character.
- Case-sensitive (`powerScore` vs `PowerScore`).
- In the character DSL, many “values” are fixed enumerations, not arbitrary identifiers.

---

## Comments

- Single-line: `// This is a comment`
- Block: `/* This is a block comment */`
- Nested block comments are not supported.
- Character files typically avoid comments inside character blocks, but the core language supports them.

---

## Syntax Style

- Whitespace is ignored except as a separator.
- Core language:
    - Statements end with `;`
    - Blocks use `{ ... }`
- Character DSL:
    - One logical field per line.
    - Attributes and skills are comma-separated.
    - Equipment is encoded as: `Equipment: Weapon=... Armor=... Accessory=...`

---

## Sample Character File



```sahh
Character:
Race: Elf
Class: Ranger
Background: Outcast
Attributes: STR=15, DEX=18, INT=12, WIS=10, CHA=8, END=14
Skills: Tracking, Archery, Stealth
Equipment: Weapon=Bow Armor=Leather Accessory=Amulet
Alignment: Chaotic Good
Magic Affinity: Nature

Character:
Race: Human
Class: Wizard
Background: Scholar
Attributes: STR=8, DEX=14, INT=18, WIS=12, CHA=10, END=12
Skills: Alchemy, ElementalMagic, Cooking
Equipment: Weapon=Staff Armor=Robe Accessory=Rune
Alignment: Neutral Good
Magic Affinity: Fire
```

---


The parser reads multiple `Character:` blocks, producing a list of `Character` nodes for the evaluator.

---

## Battle Behavior

The battle system in the evaluator uses character data to simulate a simple turn-based fight.

### Initialization

For each character:

- **HP** = function of `END` (e.g., `hitPoints = END * 2`).
- **Armor Class** = function of `DEX` and armor.
- **Power Score** = function of attributes, skill count, and magic affinity.
- **Roles** (e.g., Tank, Ranged) are derived from attributes and equipment.

### Turn Loop

- A `while` loop runs as long as both characters have HP > 0.
- Each round:
    - Character A attacks Character B.
    - If B is still alive, B attacks A.
    - Round counter increments.
- When one HP reaches 0 or below, the loop ends and a winner is printed.

### Damage Models

- **Physical Damage**
    - Based on attacker `STR` plus a small random component.
    - Reduced by defender’s Armor Class (DEX + armor).

- **Magic Flurry (Recursive)**
    - Available to certain casters (e.g., Fire-aligned Wizards / Mages).
    - Triggered by a random chance per attack.
    - Implemented as a recursive function:
        - Deals one INT-based hit.
        - Calls itself with `hitsLeft - 1` until no hits remain.
    - Armor reduces this magical damage less than physical damage.

This uses both a `while` loop and recursion, aligning with the control-flow and function requirements from the lab.

---

## High-Level Character DSL Grammar (Informal)
```sahh
program = { character_block } ;

character_block
= "Character:" newline
race_line
class_line?
background_line?
attributes_line
skills_line
equipment_line
alignment_line
magic_affinity_line ;

race_line = "Race:" RACE_TYPE newline ;
class_line = "Class:" CLASS_TYPE newline ;
background_line = "Background:" BACKGROUND_TYPE newline ;
attributes_line = "Attributes:" attribute_list newline ;
skills_line = "Skills:" skill_list newline ;
equipment_line = "Equipment:" equipment_spec newline ;
alignment_line = "Alignment:" ALIGNMENT_TYPE newline ;
magic_affinity_line= "Magic Affinity:" MAGIC_TYPE newline ;

attribute_list = attribute { "," attribute } ;
attribute = ATTR_KEY "=" NUMBER ;

skill_list = SKILL { "," SKILL } ;

equipment_spec = "Weapon=" WEAPON_VALUE
"Armor=" ARMOR_VALUE
"Accessory=" ACCESSORY_VALUE ;
```
---

(Exact token names follow the `TokenType` enum used in the scanner and parser.)

---

## Design Rationale

- Use a **readable, domain-specific format** so character authors don’t need to think in raw ASTs.
- Keep the **core language** minimal but expressive enough to support labs on scanning, parsing, evaluation, control flow, and functions.
- Showcase **loops** and **recursion** via a battle demo instead of purely abstract examples.
- Enforce **simple semantic rules** (attribute caps, totals) to mirror real-world language semantic checking.
- Demonstrate how a **general interpreter** can back a specific domain (fantasy characters and combat) with only a thin DSL layer on top.

