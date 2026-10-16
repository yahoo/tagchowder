---
paths:
  - "core/src/main/java/**"
---

# Parser internals (`com.yahoo.tagchowder`, `jaxp`)

Read `AGENTS.md` first. This page covers the hand-written classes under `core/src/main/java/`. The generated `templates` package is
covered by `.claude/rules/definitions.md`.

## ScanHandler events

- The scanner passes `(char[] buff, int offset, int length)` slices of **its own reused output buffer**. Copy what you need
  (`new String(buff, offset, length)`, `makeName`) before returning; never keep a reference to `buff`.
- Events arrive in this order for a start tag: `gi`, then `aname` / `aval` / `adup` per attribute, then `stagc` (`>`) or `stage`
  (`/>`). `theNewElement` is the pending element between `gi` and the close. Handlers return early when it is `null`, and the
  input can make it `null` at any point (an ignored bogon, a stray attribute), so keep those guards.
- `entity` only records the value; the scanner reads it back with `getEntity()` and inserts the character. Return 0 for "not an
  entity" so the scanner emits the raw text.
- `PYXWriter` implements the same interface, so a change to `ScanHandler` touches `Parser`, `PYXWriter` and every external
  implementation. Treat the interface as frozen.

## The element stack

- `theStack` is a linked list of `Element` through `next()`, topped by the current element and ending at `<root>`. `theSaved` holds
  restartable elements waiting to be reopened, and `thePCDATA` is the stand-in used to ask "can text go here?".
- `rectify(e)` is the only way to place a new element: it walks up for a container, adds natural parents, pops restartably, pushes,
  and calls `restart`. Don't call `push` directly from an event handler.
- `etagBasic` never pops `<root>` or the document element: the `sp.next() == null || sp.next().next() == null` check stops a stray
  `</html>` from ending the document early. Keep it.
- `Element.anonymize()` strips `ID`-typed and `name` attributes before an element is restarted, so a restarted copy never repeats an id.

## Features and properties

Adding a feature:

1. `public static final String X_FEATURE` with a Javadoc that says what `true` means. Custom features use a short name
   (`"cdata-elements"`), not a URL, unless they belong to an outside spec (as `AMP_VALIDATION_FEATURE` does).
2. `private static final boolean DEFAULT_X`, a field initialized from it, and a `theFeatures.put(X_FEATURE, truthValue(DEFAULT_X))`
   line. The comment above `theFeatures` explains why both exist: keep them in sync.
3. A branch in `setFeature` that sets the field.
4. If the command line should expose it: an option in `CommandLine` (`options` and `process`) and a line in the README option list.
5. Tests for both values, and for the default.

Properties (`scanner`, `schema`, `auto-detector`, `lexical-handler`) type-check their value and throw `SAXNotSupportedException`
otherwise. A `null` lexical handler resets to the parser's own no-op implementation; the other properties do not accept `null`.

## Text and names

- `makeName` builds element and attribute names: letters and `_` pass, a leading digit, `-` or `.` gets a `_` prefix, only the first
  `:` survives (or becomes `_` under `translate-colons`), and other characters are dropped. With `AMP_VALIDATION_FEATURE` on,
  `⚡ [ ] { }` are also kept for AMP attributes such as `⚡4email` and `[text]`. Names are interned.
- `expandEntities` expands a reference in an attribute value only when it ends with `;`. Text content expands references in the
  scanner and accepts a missing `;`, as browsers do.
- Code points above U+FFFF become surrogate pairs. Lone surrogates from `&#xD800;`-style references are dropped by the scanner.

## AttributesImpl

- Each attribute is five consecutive `String`s in `data` (uri, localName, qName, type, value). `qNameIndex` maps a qName to its
  **lowest** index so `getIndex(qName)` is O(1). Every method that adds, removes, moves or renames an entry must keep that map
  correct. `setQName` does not update it today: if you touch it, fix that and add a test.
- `getIndex(uri, localName)` is still a linear scan.

## Writers and tools

- `XMLWriter` is a SAX filter that serializes events. Its `setOutputProperty` keys are the `XMLWriter` constants (`METHOD`,
  `OMIT_XML_DECLARATION`, `DOCTYPE_PUBLIC`, ...). `INDENT` and `MEDIA_TYPE` are ignored. `STANDALONE` is used, although
  its comment says "currently ignored". Escaping lives in `writeEsc`; any change there needs tests with `& < > "` and non-ASCII
  text under a non-UTF-8 output encoding.
- `System.out/err/exit` are used only by `CommandLine`, `jaxp/JAXPTest` and `XMLWriter` (stdout as its default output).
  Commented-out debug lines in `Parser`, `Schema` and `XMLWriter` also match the checkstyle regexp. All of these are in the
  45-violation baseline. Don't add any.
