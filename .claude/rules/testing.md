---
paths:
  - "core/src/test/**"
---

# Testing conventions (`core/src/test/`)

- **Framework:** TestNG 7.7 (`org.testng.annotations.Test`, `org.testng.Assert`). Argument order is
  `Assert.assertEquals(actual, expected, message)`, the reverse of JUnit. Pass a message in new asserts, and prefer `assertEquals`
  over `assertTrue(a.equals(b))` so a failure shows both values.
- **No mocking library** is on the classpath, and none is needed: parse real input and record what the parser reports.
- **How to drive the parser:** `new Parser()`, set a handler (`CustomHandler` records start tags as `ParsedHtmlTag`; extend
  `DefaultHandler` for other events), set features, then `parse(new InputSource(new StringReader(html)))`. For longer documents,
  add a file under `resources/html/` and load it the way `ParserTest.getSampleHtml` does, as UTF-8.
- **Assert on the output, not only that parsing finished.** A test that just calls `parse` proves only that nothing threw. Check
  the element sequence, attribute names and values, or the serialized result (`XMLWriter` into a `StringWriter`).
- **Charset:** surefire runs with `-Dfile.encoding=ANSI_X3.4-1968` (ASCII). Feed characters through a `Reader`, or bytes with an
  explicit encoding. Never rely on the platform default.
- **Malformed and hostile input** is the point of this library. A parser change needs cases for: missing end tags, end tags in the
  wrong order, an end tag for an element that isn't open, misplaced block elements (`<p><div>`), unclosed attribute quotes,
  `<` and `&` in odd places, references with and without `;`, numeric references to surrogates, C1 controls and astral code points,
  and non-ASCII text (accented Latin, Cyrillic, CJK, right-to-left, emoji). Assert that the result is well nested.
- **Test the change, not the stack.** Add unit tests only for the code the change adds or alters, in the `<ClassName>Test` of the
  class you touched. Don't add tests for unchanged code and don't reorganize existing tests. A parser change is tested by parsing a
  small input and asserting on the events; don't route it through `CommandLine` or JAXP when a `Parser` test covers it.
- **No threads.** Don't start a `Thread` or executor in a unit test. Parsing is synchronous; call it from the test thread. No
  existing test creates a thread.
- **Independent tests.** Every `@Test` builds its own `Parser` and handler. No shared mutable fields, no static state, no order
  dependence. Checkstyle rejects `dependsOnMethods` / `dependsOnGroups`.
- **Asserts only in `@Test` methods.** Helpers return values (a list of tags, a string) and the test asserts on them. Helpers are
  instance methods, never `static` (constants may be `private static final`).
- **Checkstyle applies to tests:** every test method needs a Javadoc with `@throws` for each declared exception, and parameters are
  `final`. `MagicNumber`, `JavadocPackage`, `JavadocStyle` and `JavadocVariable` are suppressed under `test/`.
- **Layout:** name a test class `<ClassName>Test` and put it in the same package as the class it tests (only `Parser` and
  `AttributesImpl` have one today). `CustomHandler` and `ParsedHtmlTag` are shared test fixtures.
- **No network and no files outside `resources/`.** Don't call `parse(String systemid)` with a URL.
- TestNG groups `notIsolate` and `EventListenersRegression` are excluded by surefire. Don't tag normal tests with them.
- There is no working coverage report (see `AGENTS.md`, Known gotchas). Check by reading that each new branch has a test that fails
  without the change.
