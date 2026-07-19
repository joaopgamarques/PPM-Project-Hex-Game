# Hex Board Game

A functional implementation of the [Hex board game](https://en.wikipedia.org/wiki/Hex_(board_game)) in Scala 3, featuring both a text-based (TUI) and graphical (GUI) interface.

Developed for the Multiparadigm Programming (PPM) course at ISCTE — see the [assignment specification](Projeto_PPM_22_23_v1.pdf) (in Portuguese).

## About the Game

Hex is a two-player strategy game played on a rhombus-shaped board of hexagonal cells. Players take turns placing pieces — Red aims to connect the left and right edges, Blue aims to connect the top and bottom edges. The first player to complete a contiguous path wins.

## Features

- **Dual Interface** — Play via terminal (TUI) or JavaFX graphical interface (GUI)
- **Computer Opponent** — Two difficulty levels:
  - *Easy* — Random move selection
  - *Normal* — Weighted strategy that prefers moves adjacent to existing pieces
- **Configurable Settings** — Difficulty and first player selection; board size (3–21) in the TUI (the GUI board is fixed at 5×5)
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
├── ControllerUtils.scala    # Maps FXML polygon ids to board coordinates
├── HexUtils.scala           # I/O utilities (rendering, input, file operations)
├── Board.scala              # Type alias: List[List[Cells.Cell]]
├── Cells.scala              # Enum: Red, Blue, Empty
├── Settings.scala           # Tuple: (Difficulty, FirstPlayer, UserInterface)
├── Difficulty.scala         # Enum: Easy, Normal
├── FirstPlayer.scala        # Enum: User, Computer
├── UserInterface.scala      # Enum: TUI, GUI
├── GameOptions.scala        # Menu state enum (MainMenu, GameMenu, Settings)
├── Options.scala            # Menu definitions (main menu, game menu, settings)
├── CommandLineOption.scala  # Menu entry: name + action
├── MyRandom.scala           # Functional random number generator
├── RandomWithState.scala    # RNG state threading
└── *.fxml                   # JavaFX layout files
```

### Key Design Decisions

- **Immutable state** — `Container` and `GameState` are case classes; every action returns a new instance
- **Pure/impure separation** — Game logic is pure; all I/O is isolated in `HexUtils`, `HexTUI`, and `Controller`
- **Functional RNG** — Random state is threaded explicitly through `MyRandom` to avoid side effects
- **Win detection** — Path-finding with hex neighbor offsets `(1,0), (1,-1), (0,1), (0,-1), (-1,1), (-1,0)`

## Requirements

- **JDK 20**
- **Scala 3.2.2**
- **JavaFX (OpenJFX) SDK 20.0.1** — [download from Gluon](https://gluonhq.com/products/javafx/); the IntelliJ project expects it at `%USERPROFILE%\Documents\openjfx-20.0.1_windows-x64_bin-sdk\javafx-sdk-20.0.1\lib`
- **IntelliJ IDEA** with the Scala plugin (project files are included in the repository)

JavaFX is required to *compile* the project (the GUI sources import it) even if you only intend to play the TUI.

## Running

Open the project in IntelliJ IDEA and make sure the JDK, Scala SDK, and JavaFX library above are configured (IntelliJ will flag anything missing when the project opens).

### TUI Mode

Run the `HexTUI` object. No VM options are required.

### GUI Mode

Run the `FxApp` object (in `HexGUI.scala`) with VM options pointing at your JavaFX SDK:

```text
--module-path "C:\Users\<you>\Documents\openjfx-20.0.1_windows-x64_bin-sdk\javafx-sdk-20.0.1\lib" --add-modules javafx.controls,javafx.fxml
```

## Authors

- Joao Marques
- Ricardo Carvalho
- Ruben Gaspar
