# Terminal Buffer

## Storage model

The buffer uses a single unified history (`LineHistory`) to represent both the visible screen and scrollback.  
The visible screen is simply the last `height` lines of this history.

This avoids duplicating state between screen and scrollback and makes scrolling and resizing easier to reason about.

---

## Virtual blank rows

When the history contains fewer lines than the screen height, the missing rows are treated as virtual blank rows instead of being materialized.

They behave like real empty rows for reads but become real lines only when modified (e.g. via `writeText`, `fillLine`, or `insertText`).  
This keeps the history minimal and matches terminal behavior.

---

## writeText semantics

`writeText` writes characters at the cursor position and:

- advances normally
- wraps at the last column
- scrolls at the bottom

Wrapping creates SOFT breaks, while explicit `\n` creates HARD breaks.  
This distinction is needed later for resizing.

---

## insertText semantics

`insertText` shifts existing content to the right starting from the cursor:

- cascading across lines if needed  
- truncating overflow past the last visible row  

This matches typical terminal insert-mode behavior and keeps the implementation deterministic.

---

## HARD vs SOFT line breaks

Each stored line records how it ended:

- HARD break → explicit newline  
- SOFT break → wrap

This allows reconstructing logical lines correctly during resize and avoids incorrect merges.

---

## Resizing strategy

Resizing uses full reflow.

The algorithm:

1. rebuild logical lines using HARD breaks  
2. flatten each logical line  
3. wrap again using the new width  

The visible screen is then the last `height` lines.

This produces consistent behavior for both shrinking and expanding width.

---

## Scrollback model

Scrollback is implicit — everything above the visible window.

When history exceeds `scrollbackMaxSize`, the oldest lines are dropped.  
No separate structure is needed.

---

## Testing approach

Tests focus on observable behavior (screen, cursor, scrollback, resize).  
Some tests use a small test-only helper to construct history directly instead of simulating writes.
