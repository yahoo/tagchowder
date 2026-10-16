# AGENTS.md

Guidance for AI coding agents (and humans) working in this repository. This file holds the rules for every change. Area-specific
detail lives in `.claude/rules/`. Claude Code loads each page automatically when matching files are touched; other agents should read
the matching page before changing that area:

| Changing | Read |
|---|---|
| `core/definitions/`, `core/templates/`, `core/tssl/`, `core/stml/` (schema, scanner table, generated code) | `.claude/rules/definitions.md` |
| a class in `core/src/main/java/` (`Parser`, `AttributesImpl`, writers, `jaxp/`) | `.claude/rules/parser-internals.md` |
| any test | `.claude/rules/testing.md` |

## What this is

`tagchowder` is an open-source Java library (Apache 2.0, published to Maven Central as `com.yahoo.tagchowder:tagchowder.core`) that
parses HTML as found in the wild and reports it through the SAX `XMLReader` interface. It is a fork of John Cowan's TagSoup 1.2.1.
Callers create a `Parser`, set a `ContentHandler` (and optionally features and properties), and call `parse(InputSource)`. The
parser always produces a well-nested element stream: it supplies missing parents, closes and restarts overlapping tags, and never
reports a syntax error.

This is a public repository. Everything committed here (code, comments, commit messages, docs, PR text) must make sense to an outside
contributor: no links to private systems, internal ticket ids, internal hostnames, or customer data.

## Build and verify

The bytecode target is Java 8 (`jdk.version` 1.8). CI builds on **JDK 17**; JDK 21 also builds locally.

```bash
# Full build: code generation, checkstyle, compile, tests, SpotBugs, PMD, javadoc and source jars (~15s, offline once deps are cached)
mvn clean install -Dgpg.skip=true

# Same without tests: proves it generates, compiles and is clean, not that it works
mvn clean install -DskipTests -Dgpg.skip=true

# One test class / one method (checkstyle and code generation still run first)
mvn -pl core test -Dtest=ParserTest
mvn -pl core test -Dtest='ParserTest#testAMPFeature'

# Try the parser on a snippet (the jar has no Main-Class, so `java -jar` does not work)
echo '<p>a <b>b<i>c</b>d' | java -cp core/target/tagchowder.core.jar:$HOME/.m2/repository/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar \
    com.yahoo.tagchowder.CommandLine --html
```

A change is done only when `mvn clean install -Dgpg.skip=true` passes. Don't guess at style: run the build and fix what the tools
report.

| Gate | Where | What fails the build |
|---|---|---|
| Checkstyle (`checkstyle.xml`, `java.header`) | `validate`, main **and** test sources | more than 45 violations (`core/pom.xml`). There are exactly 45 today, so **any new violation fails the build** |
| SpotBugs (`spotbugs-include.xml`) | `verify` | medium+ findings from the include list, main and test code |
| PMD (`pmd-ruleset.xml`) | `verify` | `StringInstantiation`, unguarded log statements |
| Javadoc | `package` | broken Javadoc in main code |

There is no coverage gate (see Known gotchas), so nothing but review makes you add tests. Add them anyway: every behavior change
needs a test that fails without it.

## Layout

Single Maven module `core/` under the parent `pom.xml`. The parent pom says "DO NOT ADD DEPENDENCIES TO THIS FILE": declare versions
in the parent's `<dependencyManagement>` and the dependency itself in `core/pom.xml`. The only runtime dependency is `slf4j-api`
(plus `jsr305` annotations); keep it that way.

```
core/definitions/   html.tssl (elements, content models, entities) and html.stml (scanner state table): the HTML knowledge
core/tssl/, stml/   XSLT that turns the definitions into Java fragments, plus the RELAX NG schemas (.rnc) for the definition files
core/templates/     HTMLSchema / HTMLModels / HTMLScanner Java templates with @@TOKEN@@ placeholders
                    -> generated into core/target/generated-sources/com/yahoo/tagchowder/templates/ (never edit the output)
core/src/main/java/com/yahoo/tagchowder/
  Parser            XMLReader + ScanHandler: features/properties, the element stack, entity expansion, DOCTYPE handling
  Scanner, ScanHandler  The lexer contract: Scanner reads characters and fires ScanHandler events (gi, aname, aval, etag, ...)
  Schema, ElementType, Element  Content models as bitmasks, default attributes, natural parents; Element is a stack entry
  AttributesImpl    SAX Attributes with an index map for O(1) lookup by name
  AutoDetector      Hook to pick a Reader for a byte stream (default: platform charset)
  XMLWriter, PYXWriter, CommandLine  Output side and the command-line tool (README lists its options)
  jaxp/             JAXP SAXParserFactory / SAXParser adapters; JAXPTest is a manual demo with a main(), not a unit test
core/src/test/      TestNG tests, sample documents in resources/html/
build.xml           Legacy TagSoup Ant file. Unused and broken (it references a missing etc/build); use Maven
```

## How a parse flows

1. `parse(InputSource)` calls `setup()`: creates `HTMLSchema` and `HTMLScanner` if none were set, and resets the element stack to
   `<root>`. It then picks a `Reader`: the character stream if set; otherwise the byte stream (opened from the system id when
   absent), decoded with the given encoding or, when none is given, by the `AutoDetector`.
2. `HTMLScanner.scan` walks the generated state table one character at a time. It maps C1 controls (0x80-0x9F) through the Windows-1252
   table, folds CR/LF, drops other control characters, and fires `ScanHandler` events on `Parser` with offsets into its own buffer.
3. `gi` creates a pending `Element` (unknown names become *bogons* and are added to the schema). Attribute events fill it. `stagc` /
   `stage` call `rectify`, which finds the nearest stack entry that can contain the element, inventing natural parents
   (`ElementType.parent()`) when nothing can, and pops the elements in between restartably.
4. `etag` pops to the matching element. Elements with `F_RESTART` that were closed by force are saved and pushed again afterwards
   (`<b>x<i>y</b>z` becomes `<b>x<i>y</i></b><i>z</i>`). Inside an `F_NOFORCE` element the match is only *preclosed*.
5. `push` / `pop` are the only places that call `ContentHandler.startElement` / `endElement`. `eof` pops everything and ends the
   document.

## Invariants: do not break these

- **Never throw on bad markup.** Malformed or hostile input must produce *some* well-formed event stream, never an exception. A
  `SAXException` comes only from the caller's handlers. An `Error` from `Schema` or `HTMLScanner` means the definitions in
  `core/definitions/` are inconsistent; fix the definition rather than catching it.
- **Never throw an unchecked exception in new code, main or test.** That means `RuntimeException` and every subclass
  (`IllegalArgumentException`, `IllegalStateException`, `NullPointerException`, `UnsupportedOperationException`,
  `ArrayIndexOutOfBoundsException`, ...) and `Error`. Report failures with the checked SAX exceptions (`SAXException`, and
  `SAXNotRecognizedException` / `SAXNotSupportedException` for features and properties) or `IOException` for input. Where a JDK
  call can throw one (`Integer.parseInt`, `new URI`, `substring`), catch it at that call and keep parsing, as `lookupEntity` does with
  `NumberFormatException`. Test helpers return values or declare a specific checked exception.
  The existing unchecked throws are fixed by contracts inherited from TagSoup, SAX and JAXP. Don't copy them, and don't change them
  in an unrelated change: `AttributesImpl.badIndex` (`ArrayIndexOutOfBoundsException`, as SAX's own `AttributesImpl` does), the
  `Error`s in `Schema` and `HTMLScanner` for broken definitions, and `RuntimeException` in `SAXParserImpl.isNamespaceAware` /
  `isValidating`, which cannot declare a checked exception.
- **Balanced events.** Every `startElement` gets an `endElement` in LIFO order, and every `startPrefixMapping` an
  `endPrefixMapping`. Change the stack only through `push`, `pop`, `restartablyPop` and `rectify`.
- **No external fetches during a parse.** DOCTYPE system ids are resolved as strings (`URI.resolve`) and passed to the
  `EntityResolver`, whose result is ignored. Don't add code that loads a DTD, an entity or any other URL. `parse(String systemid)`
  opens a URL by design; document it, don't extend it.
- **Feature state stays in sync.** A feature is a `public static final String` name, a `DEFAULT_*` constant, a field, an entry in
  `theFeatures`, and a branch in `setFeature`. Adding one means touching all five (see `.claude/rules/parser-internals.md`).
- **Content models fit in an `int`.** Groups in `html.tssl` become bits 1..n, and bits 30 and 31 are `M_PCDATA` and `M_ROOT`, so
  there can be at most 29 groups (22 today).
- **Java 8 runtime.** The compiler uses `-source/-target 1.8` without `--release`, so a Java 9+ library call compiles on JDK 17
  and then fails on a Java 8 runtime. Use only classes and methods that exist in Java 8.
- **SAX contract.** `Parser` implements `XMLReader`: unknown feature or property names throw `SAXNotRecognizedException`, and a
  wrong property type throws `SAXNotSupportedException`. Keep public signatures and feature names stable; callers depend on them.

## Code style

Checkstyle enforces the mechanical part (150-column lines, `final` parameters, Javadoc on public members, no `System.out/err`, no
star imports, the license header from `java.header`). On top of that:

- New files start with the Apache header from `java.header`. Files changed from TagSoup keep their header and the
  "Changes to the original project are Copyright ... Oath Inc." line.
- Log with `{}` placeholders and plain arguments, as `Parser.parse(String)` does. PMD (`GuardLogStatement`) fails an unguarded
  call that builds its message, such as `logger.debug("x" + id)`; wrap that in `if (logger.isDebugEnabled())`.
- Use `StringBuilder` in new code. Existing `StringBuffer` and raw `HashMap`/`ArrayList` are inherited from TagSoup: don't rewrite
  them in a change that is about something else.
- Annotate nullability in new public methods with `javax.annotation.Nonnull` / `@Nullable`. `jsr305` is already a dependency;
  the test fixture `ParsedHtmlTag` shows the usage.

## Design rules (judgment the tools cannot enforce)

- **The hot path stays cheap.** `HTMLScanner.scan` and the `ScanHandler` callbacks run per character or per token on large
  documents. No per-character allocation, regex, logging, or boxing there. Work on the `char[]` buffer with offsets, and build a
  `String` only when a name or value is complete.
- **Browser semantics over strictness.** When deciding how to repair markup, do what browsers do, as the README describes, and
  say so in the test or commit message.
- **New behavior goes behind a feature, default off,** unless it fixes output that was clearly wrong. Callers rely on today's output,
  and a release reaches them on the next merge.
- **Fix the definition, not the code.** Element nesting, empty elements, default attributes and entities belong in `html.tssl`.
  Special-casing an element name in `Parser` is a last resort.
- **No mutable static state** in the parser. `Parser`, `Schema` and `HTMLScanner` are per-instance. They are not thread-safe,
  and don't make them pretend to be. `CommandLine` keeps its options in static fields; it is a program, not something to copy.
- **No lambdas, streams or method references in main code or templates.** Use loops and anonymous classes (as `Parser.setup`
  does for the default `AutoDetector`), matching the rest of the library.
- **An enum over a `boolean` flag** for a new option on a public method. SAX features stay booleans, since `setFeature` takes one.
- **Size buffers and collections up front** when the size is known, as `cleanPublicid` and `makeName` do for their buffers.
- **No throwaway temporaries.** Don't name a value that is used once on the next line unless the name adds real clarity.
- **No new dependencies** for the core parser.

## Comments

- Default to no inline comment. Write one only when a competent reader would otherwise make a wrong change. One line, two at most.
- Explain *why*, not *what*: name the browser behavior, the spec section, or the hazard.
- No narration of the next line and no history ("used to", "previously", "replaced"). Git and the PR hold that.
- Don't add new commented-out code (`// System.err.println(...)`); the existing lines are TagSoup leftovers.
- A `TODO` / `FIXME` names a GitHub issue. Otherwise fix it now or leave it out.

## Tests

See `.claude/rules/testing.md`. Short version: TestNG with `Assert.assertEquals(actual, expected, message)`, parse from a
`StringReader` or a resource read as UTF-8, capture events with a handler, and assert on the result. No new threads, no static
helpers, asserts only inside `@Test` methods.
For each change, add unit tests that cover only the new or changed code, not the entire stack.

## Commits and PRs

- Commit subject: imperative, ≤ 72 chars, describes the behavior change. The body explains what was wrong and why the fix is right,
  wrapped at ~72 columns. Don't restate the diff.
- One logical change per commit and per PR. Don't fold unrelated cleanups in.
- Stage files by name, never with `git add -A` / `git add .`. That keeps scratch files and local credentials out of commits.
- Never use `--no-verify`. Never push directly to `yahoo/tagchowder`; open PRs from a fork. Force-push only your own PR branch, and only
  with `--force-with-lease`.
- Update `README.md` when public behavior or a command-line option changes, and `CHANGES` for user-visible fixes.
- Before opening a PR, run the `review-change` skill (`.claude/skills/review-change/`) or walk its checklist by hand.

## Release and secrets

- CI is Travis (`.travis.yml`, JDK 17). Every push to `master` runs `cd/deploy.sh`, which deploys to Maven Central. Treat merging to
  `master` as releasing.
- A release needs a new version in three places: `<version>` in `pom.xml`, `<parent><version>` in `core/pom.xml`, and the dependency
  snippet in `README.md` (which has lagged before). Bump it only when asked.
- `cd/mvnsettings.xml` must read credentials from `${env.OSSRH_USER}` / `${env.OSSRH_TOKEN}`. **Never commit a literal user name,
  token or password there**, even if your working copy has one. Check `git diff --cached cd/` before every commit.
- Never commit `target/`, IDE files, `cd/deploy/` (decrypted signing keys) or any key material. Don't edit `cd/*.enc`,
  `cd/deploy.sh` or the `ossrh` profile unless the task is about release automation.

## Known gotchas

- **Coverage is not measured.** The surefire `<argLine>` in `pom.xml` replaces the `argLine` that the JaCoCo agent sets, so no
  `jacoco.exec` is written and the report step prints "Skipping JaCoCo execution due to missing execution data file". Don't cite
  coverage numbers from this build.
- **Tests run with an ASCII default charset** (`-Dfile.encoding=ANSI_X3.4-1968`). Anything that relies on the platform default,
  including the default `AutoDetector`, reads non-ASCII bytes as garbage in tests. Pass characters (a `Reader`) or an explicit encoding.
- **Generated code skips checkstyle.** The templates under `core/templates/` are compiled from `target/generated-sources` after the
  `validate` phase, so checkstyle never sees them. SpotBugs and the compiler do. Keep them tidy by hand.
- **Reusing a `Parser`** keeps its schema, so bogons from one document stay defined for the next. `theDoctypeIsPresent` is never
  reset either, so a later document's DOCTYPE is not reported. `CommandLine` sets a fresh `HTMLSchema` for every file, even with
  `--reuse`.
- **`STRING_INTERNING_FEATURE`** is read when the schema is created, which happens on the first parse. Setting it afterwards does
  not change the schema's names.
- `README.md` documents `java -jar tagchowder.jar`, but the jar has no `Main-Class`; use `java -cp ... com.yahoo.tagchowder.CommandLine`.
- `testTimeLimitInSeconds` in the poms is not wired to anything.
