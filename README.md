Design Decisions & Trade-offs - Step 1: Core Domain Models

Immutability for Attributes: I chose to model Attributes as an immutable record. This is a deliberate trade-off. It prevents accidental state mutation when multiple cells share the same attributes, and it allows for memory optimization (Flyweight pattern can be applied later to reuse attribute instances).

Mutable Cell: The Cell class is kept mutable. Recreating a new Cell object every time a character changes on the screen during rapid terminal output could cause excessive Garbage Collection pressure. Reusing and mutating existing Cell instances in the grid is more performant.

Type Safety: Used an enum for TerminalColor to enforce the 16-color standard strictly and prevent invalid states at compile time.

Design Decisions & Trade-offs - Step 2: Cursor & State Management

Zero-based indexing: Used 0-based indexing for cursor coordinates (0 to width-1) to align naturally with array/list structures that will back the grid, preventing constant +1/-1 offset calculations.

Clamping vs. Exceptions for Cursor: Decided to silently clamp cursor coordinates to screen boundaries instead of throwing IndexOutOfBoundsException. This mimics real terminal emulator behavior (e.g., VT100), where sending a command to move 999 columns left is a standard way to ensure the cursor is at column 0.

DRY Principle in Movement: Relative cursor movements (moveCursorUp, etc.) delegate to setCursorPosition to centralize the bounding logic.

Design Decisions & Trade-offs - Step 3: Screen Grid & Writing

2D Array for Screen: I used a fixed-size 2D array (Cell[][]) to represent the visible screen. Since the screen dimensions are fixed, a primitive array offers the best read/write performance and memory layout predictability compared to nested collections like ArrayList.

Object Reuse: In methods like clearScreen(), instead of allocating new Cell objects, I mutate the existing cells back to their default state. This significantly reduces Garbage Collection pressure during heavy terminal clearing/redrawing operations.

Writing and Wrapping: The write operation implements basic line wrapping. When the cursor hits the right edge (width - 1), it automatically wraps to column 0 of the next row, mimicking standard TTY behavior.

Design Decisions & Trade-offs - Step 4: Advanced Editing & ScrollbackScrollback Data Structure: 

Used ArrayDeque for the scrollback buffer. A double-ended queue provides $O(1)$ operations for adding to the tail and removing from the head, which perfectly models a fixed-capacity FIFO history buffer without the array-shifting overhead of an ArrayList.

Insert Operation via 1D Projection: To implement text insertion with line wrapping, I projected the 2D grid coordinates into a 1D flat index (r * width + c), added the shift offset (text length), and projected it back. This elegantly handles multi-line text shifting natively without complex nested conditional loops.

Deep Copying for History: When rows roll off the screen into the scrollback, a deep copy of the Cell objects is created. This severs the reference to the active screen grid, ensuring that subsequent mutations to the screen cells do not corrupt the historical data.

Design Decisions & Trade-offs - Step 5: Content Access & Data Presentation

Virtual Indexing System: 
To simplify accessing coordinates that span both the scrollback and the active screen, I implemented a unified "Virtual Row" index. Rows 0 to scrollbackSize - 1 map to history, while scrollbackSize to totalRows - 1 map to the active screen grid. This abstracts the underlying split data structures away from the caller.

Trailing Space Trimming (RTrim): When extracting a row as a String, I implemented logic to strip trailing spaces. Because the terminal buffer eagerly fills empty cells with spaces, raw string extraction would result in padded strings (e.g., 80 characters wide even for short text). Trimming provides a much better experience for typical operations like copy-pasting terminal output.

StringBuilder Performance: Used StringBuilder for all multi-line string aggregations to ensure $O(n)$ concatenation performance, avoiding the heavy memory allocation overhead of repeated String immutability creation.

Design Decisions & Trade-offs - Step 6: Testing Strategy

JUnit 5 framework: Used standard JUnit 5 for unit testing.

Miniature Buffer for Tests: In the @BeforeEach setup, I initialize a small terminal buffer (10x3 with a scrollback of 2). Testing logic like line wrapping and scrollback eviction is much easier to reason about and assert on a small grid rather than a standard 80x24 one.

Edge Case Coverage: Tests explicitly cover negative boundary conditions (constructor validation), cursor out-of-bounds clamping, and buffer eviction policies when scrollback exceeds its configured limit.

Bonus Implementation: Resize Strategy

I implemented the Truncation Strategy for resizing the terminal buffer.

When the buffer is shrunk, data outside the new bounds is permanently discarded (both on the screen and in the scrollback history). When expanded, the new space is filled with empty default cells.

Trade-off: This is simpler and faster $O(N \times M)$ than a complete text "reflow" strategy (like standard modern emulators that push text to new lines). However, it means resizing a window smaller and then larger will result in data loss on the right edge. I chose this approach to guarantee system stability and memory safety within the given time constraints, rather than risking coordinate corruption with a complex wrapping algorithm.
