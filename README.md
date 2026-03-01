# Hex Board Game

A functional implementation of the [Hex board game](https://en.wikipedia.org/wiki/Hex_(board_game)) in Scala 3, featuring both a text-based (TUI) and graphical (GUI) interface.

Developed for the Multiparadigm Programming (PPM) course at ISCTE.

## About the Game

Hex is a two-player strategy game played on a rhombus-shaped board of hexagonal cells. Players take turns placing pieces — Red aims to connect the left and right edges, Blue aims to connect the top and bottom edges. The first player to complete a contiguous path wins.

## Features

- **Dual Interface** — Play via terminal (TUI) or JavaFX graphical interface (GUI)
- **Computer Opponent** — Two difficulty levels:
  - *Easy* — Random move selection
  - *Normal* — Weighted strategy that prefers moves adjacent to existing pieces
- **Configurable Settings** — Board size (3–21), difficulty, and first player selection
- **Save / Load** — Persist and resume games via binary serialization
- **Undo** — Revert the last move pair (player + computer)

## Architecture

The project follows functional programming principles — immutable data structures, pure/impure separation, tail recursion, and explicit state threading.

```text
src/Project/
├── HexTUI.scala             # TUI entry point (recursive state machine)
├── HexGUI.scala             # GUI entry point (JavaFX Application)
├── GameState.scala          # Core game logic (move placement, win detection)
├── Container.scala          # Game orchestration (play, undo, save/load, AI)
├── Controller.scala         # JavaFX FXML controller
├── HexUtils.scala           # I/O utilities (rendering, input, file operations)
├── Board.scala              # Type alias: List[List[Cells.Cell]]
├── Cells.scala              # Enum: Red, Blue, Empty
├── Settings.scala           # Tuple: (Difficulty, FirstPlayer, UserInterface)
├── Difficulty.scala         # Enum: Easy, Normal
├── FirstPlayer.scala        # Enum: User, Computer
├── UserInterface.scala      # Enum: TUI, GUI
├── GameOptions.scala        # Menu state enum (MainMenu, GameMenu, Settings)
├── Options.scala            # Sub-menu options
├── MyRandom.scala           # Functional random number generator
├── RandomWithState.scala    # RNG state threading
└── *.fxml                   # JavaFX layout files
```

### Key Design Decisions

- **Immutable state** — `Container` and `GameState` are case classes; every action returns a new instance
- **Pure/impure separation** — Game logic is pure; all I/O is isolated in `HexUtils`, `HexTUI`, and `Controller`
- **Functional RNG** — Random state is threaded explicitly through `MyRandom` to avoid side effects
- **Win detection** — Path-finding with hex neighbor offsets `(1,0), (1,-1), (0,1), (0,-1), (-1,1), (-1,0)`

## Prerequisites

- Scala 3
- JavaFX SDK (for GUI mode)
- IntelliJ IDEA (project files included) or any Scala-compatible IDE

## Running

### TUI Mode

Run the `HexTUI` object:

```bash
scala src/Project/HexTUI.scala
```

### GUI Mode

Run the `FxApp` object in `HexGUI.scala` (requires JavaFX on the classpath).

## Authors

- Joao Marques
- Ricardo Carvalho
- Ruben Gaspar
