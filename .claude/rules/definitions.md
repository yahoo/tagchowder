---
paths:
  - "core/definitions/**"
  - "core/templates/**"
  - "core/tssl/**"
  - "core/stml/**"
---

# HTML schema and scanner definitions (`core/definitions/`)

These two files are the parser's knowledge of HTML. The build turns them into Java on every run (`generate-sources` phase,
`core/pom.xml`):

```
html.tssl --tssl/tssl-model.xslt--> @@MODEL_DEFINITIONS@@ in templates/HTMLModels.java  (group bit constants)
html.tssl --tssl/tssl-schema.xslt-> @@SCHEMA_CALLS@@      in templates/HTMLSchema.java  (elementType/parent/attribute/entity calls)
html.stml --stml/stml-scanner.xslt> @@STATE_TABLE@@       in templates/HTMLScanner.java (state, action and table constants)
                                    -> core/target/generated-sources/com/yahoo/tagchowder/templates/*.java
```

Read the generated file in `core/target/generated-sources/...` to see what a definition change did. Never edit the generated files:
the next build overwrites them. The `.rnc` files under `core/tssl/` and `core/stml/` describe the definition formats.

## html.tssl (schema)

- `<group id='M_X'/>` declares a content-model group. Groups are sorted by id and numbered from bit 1. Bits 30 and 31 are
  `M_PCDATA` and `M_ROOT`, so **there can be at most 29 groups** (22 today). A 30th group silently overlaps `M_PCDATA`.
- `<element name type closeMode? text-parent?>`:
  - `type`: `element` (children only, from `<contains>`), `mixed` (text plus `<contains>`), `empty`, `string` / `cdata` (text only),
    `any`. `cdata` also sets `F_CDATA`: the scanner then treats the content as raw text until the matching end tag (`script`, `style`).
  - `closeMode='restartable'` sets `F_RESTART` (reopened after a forced close: `b`, `i`, `abbr`, ...). `closeMode='unclosable'`
    sets `F_NOFORCE` (`form`, `table`): an end tag for an element outside it does not force it closed. The outer element is only
    *preclosed* and ends after the unclosable one does. `cdata` wins over `closeMode`.
  - `<contains group>` sets what the element may hold. `<memberOf group>` sets which groups it belongs to. `<memberOfAny/>` means
    any group except root.
  - **Nesting an `<element>` inside another makes the outer one its natural parent.** The parser invents that parent when the
    element appears where nothing on the stack can contain it (`<li>` alone gets a `<ul>`). `text-parent='true'` marks the element
    invented for bare text (`body`).
  - `<attribute name type? default?>` inside an element declares an attribute. A `default` is reported when the attribute is
    missing and the `default-attributes` feature is on. A `type` other than `CDATA` makes the parser whitespace-normalize the value,
    so an attribute without a default is still worth declaring when it has a type. Top-level `<attribute>` entries apply to every
    element.
- `<entity name codepoint>` adds a named character reference (hex code point, astral values allowed).
- Element names must be unique and lowercase. A second definition with the same name replaces the first.
- The schema is HTML 4 era. HTML5 elements such as `section`, `article`, `nav`, `header`, `footer`, `main`, `template`, `video`
  and `audio` are not defined, so they are parsed as bogons (content model `ANY` unless `bogons-empty` is set).

## html.stml (scanner state table)

- `<state id>` holds `<tr char|symbol action newstate/>` rows. `symbol` is `EOF`, `LF`, `S` (space, LF or tab) or `default`. A row
  for the exact character wins over `default`, whatever the order in the file.
- An action is a constant that `HTMLScanner.scan` switches on. **Adding an `<action>` needs a matching `case` in
  `core/templates/HTMLScanner.java`**: the switch's `default` throws `Error("Can't process state ...")`, which breaks the
  never-throw rule on real input.
- The table is indexed in a static initializer by state and character. Characters above the highest one in the table fall back to
  `default`, so a `char=` row for a non-ASCII character makes the index table larger for every state.

## Templates (`core/templates/`)

- Plain Java with a `@@TOKEN@@` placeholder. They are not under `src/`, so an IDE won't compile them, and checkstyle never checks
  them because they are added to the build after `validate`. The compiler and SpotBugs do see them. Keep them Java 8, with the
  license header, and match the surrounding code.
- `HTMLScanner` is the hot loop of the whole library: no allocation, logging or boxing per character.

## Changing a definition

1. Edit `html.tssl` / `html.stml`, plus the template if a new action needs code.
2. `mvn -pl core test -Dtest=ParserTest` regenerates and runs the parser tests. Inspect the generated Java.
3. Add a test that parses a document using the changed element, entity or state and asserts on the event stream. Cover the repair
   case (element in an unexpected place, missing end tag, an end tag for an outer element), not only well-formed input.
4. A content-model change moves other documents' output too. Run every sample in `core/src/test/resources/html/` through
   `CommandLine --html` (see `AGENTS.md`, Build and verify) before and after the change, diff the results, and explain each
   difference in the PR or turn it into a test.
5. Note the behavior change in `CHANGES`; callers see different output.
