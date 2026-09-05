package Project

import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, RadioButton, TextField}
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.input.MouseEvent
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.{Modality, Stage}
import Project.FirstPlayer.{Computer, User}

class Controller {

  // The GUI board is a fixed 5x5 grid of Polygon cells (see GameWindow.fxml).
  private val BOARD_SIZE: Int = 5
  // Board colours, matching the Polygon fills in GameWindow.fxml and the palette in hex.css.
  private val EMPTY_CELL_COLOR: Color = Color.web("#F4F1EA")
  private val BLUE_PIECE_COLOR: Color = Color.web("#2F6FEB")
  private val RED_PIECE_COLOR: Color = Color.web("#E0433A")

  @FXML
  private var P00, P01, P02, P03, P04: Polygon = _
  @FXML
  private var P10, P11, P12, P13, P14: Polygon = _
  @FXML
  private var P20, P21, P22, P23, P24: Polygon = _
  @FXML
  private var P30, P31, P32, P33, P34: Polygon = _
  @FXML
  private var P40, P41, P42, P43, P44: Polygon = _

  // The cells above as a matrix, so a cell can be addressed by row and column instead of by name.
  // Lazy because the FXMLLoader injects the fields after the controller has been constructed.
  private lazy val cells: Vector[Vector[Polygon]] = Vector(
    Vector(P00, P01, P02, P03, P04),
    Vector(P10, P11, P12, P13, P14),
    Vector(P20, P21, P22, P23, P24),
    Vector(P30, P31, P32, P33, P34),
    Vector(P40, P41, P42, P43, P44))

  // Return the board cell of this window at the given position.
  private def cellAt(position: (Int, Int)): Polygon = cells(position._1)(position._2)

  @FXML
  private var startNewGameButton, resumeGameButton, loadGamePopupButton, returnToMainDisplayButton, undoLastMoveButton: Button = _

  @FXML
  private var saveGamePopupButton: Button = _

  @FXML
  private var gameDisplayTitle: Label = _

  @FXML
  private var easy, normal, user, computer: RadioButton = _

  @FXML
  private var cancelLoadGameButton, loadGameButton: Button = _

  @FXML
  private var yesReturnButton, noReturnButton: Button = _

  @FXML
  private var saveGameButton, cancelSaveGameButton: Button = _

  @FXML
  private var saveGameTextField: TextField = _

  @FXML
  private var loadGameTextField: TextField = _

  @FXML
  private var acknowledgeWinnerButton: Button = _

  private def loadWindow(title: String, fxmlWindow: String, controls: Button, modal: Boolean, resizable: Boolean): Scene = {
    val stage: Stage = new Stage()
    stage.setTitle(title)
    if modal then stage.initModality(Modality.APPLICATION_MODAL)
    stage.initOwner(controls.getScene.getWindow)
    val fxmlLoader: FXMLLoader = new FXMLLoader(getClass.getResource(fxmlWindow))
    val mainViewRoot: Parent = fxmlLoader.load()
    val scene: Scene = new Scene(mainViewRoot)
    stage.setScene(scene); stage.setResizable(resizable); stage.show(); scene
  }

  // Called by the FXMLLoader once the fields of the loaded window have been injected.
  @FXML
  def initialize(): Unit = {
    // Main menu only: reflect the game in progress and the settings of the current container.
    if (resumeGameButton != null) {
      resumeGameButton.setDisable(FxApp.container.positions.isEmpty)
      val (difficulty, firstPlayer, _): Settings = FxApp.container.settings
      normal.setSelected(difficulty.equals(Difficulty.Normal))
      easy.setSelected(difficulty.equals(Difficulty.Easy))
      user.setSelected(firstPlayer.equals(FirstPlayer.User))
      computer.setSelected(firstPlayer.equals(FirstPlayer.Computer))
    }
  }

  // Replace the content of the given stage with the main menu.
  private def showMainMenu(stage: Stage): Unit = {
    val fxmlLoader = new FXMLLoader(getClass.getResource("MainWindow.fxml"))
    val mainViewRoot: Parent = fxmlLoader.load()
    stage.setScene(new Scene(mainViewRoot)); stage.show()
  }

  // Replace the content of the given stage with the game window and return its scene.
  private def showGameWindow(stage: Stage): Scene = {
    val fxmlLoader = new FXMLLoader(getClass.getResource("GameWindow.fxml"))
    val mainViewRoot: Parent = fxmlLoader.load()
    val scene: Scene = new Scene(mainViewRoot)
    stage.setScene(scene); stage.show(); scene
  }

  // Draw the pieces of the current container on the given game window scene.
  private def drawBoard(scene: Scene): Unit = {
    fillPositions(FxApp.container.state.getOccupiedBy(Cells.Red), RED_PIECE_COLOR, scene)
    fillPositions(FxApp.container.state.getOccupiedBy(Cells.Blue), BLUE_PIECE_COLOR, scene)
  }

  /* Game Menu */

  // Handle mouse click event.
  def onMouseClicked(mouseEvent: MouseEvent): Unit = {
    // Ignore clicks once the game has been decided (the board stays visible behind the winner popup).
    if (FxApp.container.state.hasWinner) then return
    val polygon: Polygon = mouseEvent.getTarget.asInstanceOf[Polygon]
    val position: String = ControllerUtils.getPosition(polygon.getId)
    val (row, column): (Int, Int) = ControllerUtils.getBoardPosition(position)
    if (FxApp.container.state.isEmpty(row, column)) {
      FxApp.container = Container.play(position)(FxApp.container)
      polygon.setFill(BLUE_PIECE_COLOR)
      if (FxApp.container.state.hasContiguousLine(Cells.Blue)) {launchGameWinnerPopup("User"); return}
      cellAt(FxApp.container.positions.head).setFill(RED_PIECE_COLOR)
      if (FxApp.container.state.hasContiguousLine(Cells.Red)) {launchGameWinnerPopup("Computer"); return}
    }
  }

  // Launch a popup if there is a winner.
  private def launchGameWinnerPopup(winner: String): Unit = {
    val scene: Scene = loadWindow("Winner", "GameWinnerPopup.fxml", saveGamePopupButton, true, false)
    scene.lookup("#winnerLabel").asInstanceOf[Label].setText(s"The $winner wins the game!")
  }

  // Revert the last move and restores the game state to the previous state.
  def onMouseClickedUndo(): Unit = {
    val (removedPositions, remainingPositions): (List[(Int, Int)], List[(Int, Int)]) = FxApp.container.positions.splitAt(2)
    if (removedPositions.size.equals(2)) {
      removedPositions.foreach(position => cellAt(position).setFill(EMPTY_CELL_COLOR))
      FxApp.container = Container.undo()(FxApp.container)
    }
  }

  // Save the current game.
  def onMouseClickedSave(): Unit = {
    val scene: Scene = loadWindow("Save Game", "SaveGamePopup.fxml", saveGamePopupButton, true, false)
  }

  // Save the current game.
  def onMouseClickedConfirmSave(): Unit = {
    val filename: String = saveGameTextField.getText.trim
    if (filename.nonEmpty) {
      HexUtils.save(FxApp.container, filename)
      saveGameButton.getScene.getWindow.hide()
    } else {
      // Keep the popup open and use the text field's prompt to ask for a name.
      saveGameTextField.setPromptText("Please enter a name for the game.")
    }
  }

  def onMouseClickedCancelSave(): Unit = {
    cancelSaveGameButton.getScene.getWindow.hide()
  }

  // Return to the Main Menu.
  def onMouseClickedReturn(): Unit = {
    val scene: Scene = loadWindow("Return: Main Menu", "ReturnPopup.fxml", returnToMainDisplayButton, true, false)
  }

  // Return to the Main Menu.
  def onMouseClickedConfirmReturn(): Unit = {
    val stage: Stage = yesReturnButton.getScene.getWindow.asInstanceOf[Stage].getOwner.asInstanceOf[Stage]
    showMainMenu(stage)
    yesReturnButton.getScene.getWindow.hide()
    HexUtils.saveMyRandom(FxApp.container.random)
  }

  def onMouseClickedCancelReturn(): Unit = {
    noReturnButton.getScene.getWindow.hide()
  }

  /* Main Menu */

  // Start a new game.
  def onMouseClickedStart(): Unit = {
    FxApp.container = Container.create(settings = getUserSettings)
    val stage: Stage = startNewGameButton.getScene.getWindow.asInstanceOf[Stage]
    val scene: Scene = showGameWindow(stage)
    FxApp.container.settings._2 match {
      case User => FxApp.container = Container(FxApp.container.state, FxApp.container.positions, HexUtils.loadMyRandom().get, FxApp.container.settings)
      case Computer =>
        val (position, random): ((Int, Int), MyRandom) = FxApp.container.state.randomMove(HexUtils.loadMyRandom().get)
        val state: GameState = GameState(FxApp.container.state.play(Cells.Red, position._1, position._2), Option(position))
        FxApp.container = Container(state, position :: FxApp.container.positions, random, FxApp.container.settings)
        val polygon: Polygon = scene.lookup(s"#${ControllerUtils.getButtonId(position)}").asInstanceOf[Polygon]
        polygon.setFill(RED_PIECE_COLOR)
    }
  }

  // Resume the game in progress.
  def onMouseClickedResume(): Unit = {
    val stage: Stage = resumeGameButton.getScene.getWindow.asInstanceOf[Stage]
    drawBoard(showGameWindow(stage))
  }

  // Retrieve user settings.
  private def getUserSettings: Settings = {
    val difficulty: Difficulty = if (normal.isSelected) then Difficulty.Normal else Difficulty.Easy
    val firstPlayer: FirstPlayer = if (user.isSelected) then FirstPlayer.User else FirstPlayer.Computer
    (difficulty, firstPlayer, UserInterface.GUI)
  }

  // Load a previous saved game.
  def onMouseClickedLoad(): Unit = {
    val scene: Scene = loadWindow("Load Game", "LoadGamePopup.fxml", loadGamePopupButton, true, false)
  }

  // Load a previous saved game.
  def onMouseClickedConfirmLoad(): Unit = {
    val filename: String = loadGameTextField.getText.trim
    val loaded: Option[Container] = if (filename.nonEmpty) then HexUtils.load(filename) else None
    loaded match {
      // Only a save whose board matches the fixed 5x5 GUI grid can be drawn; force the GUI user interface on its settings.
      case Some(value) if value.state.board.size.equals(BOARD_SIZE) =>
        val stage: Stage = loadGameButton.getScene.getWindow.asInstanceOf[Stage].getOwner.asInstanceOf[Stage]
        val scene: Scene = showGameWindow(stage)
        loadGameButton.getScene.getWindow.hide()
        FxApp.container = Container(value.state, value.positions, value.random, (value.settings._1, value.settings._2, UserInterface.GUI))
        drawBoard(scene)
      // On failure keep the popup open and use the text field's prompt to tell the user what went wrong.
      case Some(_) => loadGameTextField.clear(); loadGameTextField.setPromptText("Only 5x5 games can be loaded here.")
      case None => loadGameTextField.clear(); loadGameTextField.setPromptText("Game not found.")
    }
  }

  // Draw the board by filling the corresponding Polygon elements with the specified color.
  private def fillPositions(positions: List[(Int, Int)], color: Color, scene: Scene): Unit = {
    positions.foreach(position => scene.lookup(s"#${ControllerUtils.getButtonId(position)}").asInstanceOf[Polygon].setFill(color))
  }

  def onMouseClickedCancelLoad(): Unit = {
    cancelLoadGameButton.getScene.getWindow.hide()
  }

  def onMouseClickedAcknowledgeWinner(): Unit = {
    val stage: Stage = acknowledgeWinnerButton.getScene.getWindow.asInstanceOf[Stage].getOwner.asInstanceOf[Stage]
    showMainMenu(stage)
    acknowledgeWinnerButton.getScene.getWindow.hide()
    HexUtils.saveMyRandom(FxApp.container.random)
  }

}