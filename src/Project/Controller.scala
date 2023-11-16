package Project

import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, RadioButton, TextField}
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.{Modality, Stage}
import Project.ControllerUtils
import Project.Container
import Project.Container.{getFirstRandomMove, undo}
import Project.Difficulty
import Project.FirstPlayer
import Project.FirstPlayer.{Computer, User}

import scala.annotation.{nowarn, tailrec}

class Controller {

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

  @FXML
  private var startNewGameButton, loadGamePopupButton, returnToMainDisplayButton, undoLastMoveButton: Button = _

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

  /* Game Menu */

  // Handle mouse click event.
  def onMouseClicked(mouseEvent: MouseEvent): Unit = {
    val polygon: Polygon = mouseEvent.getTarget.asInstanceOf[Polygon]
    val position: String = ControllerUtils.getPosition(polygon.getId)
    val (row, column): (Int, Int) = ControllerUtils.getBoardPosition(position)
    if (FxApp.container.state.isEmpty(row, column)) {
      FxApp.container = Container.play(position)(FxApp.container)
      polygon.setFill(Color.BLUE)
      val scene: Scene = polygon.getScene
      if (FxApp.container.state.hasContiguousLine(Cells.Blue)) {launchGameWinnerPopup("User"); return}
      val nextPosition: (Int, Int) = FxApp.container.positions.head
      val nextPolygon: Polygon = scene.lookup(s"#${ControllerUtils.getButtonId(nextPosition)}").asInstanceOf[Polygon]
      nextPolygon.setFill(Color.RED)
      if (FxApp.container.state.hasContiguousLine(Cells.Red)) {launchGameWinnerPopup("Computer"); return}
    }
  }

  // Launch a popup if there is a winner.
  private def launchGameWinnerPopup(winner: String): Unit = {
    val scene: Scene = loadWindow("Winner", "GameWinnerPopup.fxml", saveGamePopupButton, false, false)
    scene.lookup("#winnerLabel").asInstanceOf[Label].setText(s"The $winner wins the game!")
  }

  // Revert the last move and restores the game state to the previous state.
  def onMouseClickedUndo(): Unit = {
    val scene: Scene = undoLastMoveButton.getScene
    val (removedPositions, remainingPositions): (List[(Int, Int)], List[(Int, Int)]) = FxApp.container.positions.splitAt(2)
    if (removedPositions.size.equals(2)) {
      removedPositions.map(position => scene.lookup(s"#${ControllerUtils.getButtonId(position)}").asInstanceOf[Polygon].setFill(Color.WHITE)): @nowarn
      FxApp.container = Container.undo()(FxApp.container)
    }
  }

  // Save the current game.
  def onMouseClickedSave(): Unit = {
    val scene: Scene = loadWindow("Save Game", "SaveGamePopup.fxml", saveGamePopupButton, false, false)
  }

  // Save the current game.
  def onMouseClickedConfirmSave(): Unit = {
    HexUtils.save(FxApp.container, saveGameTextField.getText)
    saveGameButton.getScene.getWindow.hide()
  }

  def onMouseClickedCancelSave(): Unit = {
    cancelSaveGameButton.getScene.getWindow.hide()
  }

  // Return to the Main Menu.
  def onMouseClickedReturn(): Unit = {
    val scene: Scene = loadWindow("Return: Main Menu", "ReturnPopup.fxml", returnToMainDisplayButton, false, false)
  }

  // Return to the Main Menu.
  def onMouseClickedConfirmReturn(): Unit = {
    val stage: Stage = yesReturnButton.getScene.getWindow.asInstanceOf[Stage].getOwner.asInstanceOf[Stage]
    val fxmlLoader = new FXMLLoader(getClass.getResource("MainWindow.fxml"))
    val mainViewRoot: Parent = fxmlLoader.load()
    val scene = new Scene(mainViewRoot)
    applyDefaultSettingsTo(scene); stage.setScene(scene); stage.show()
    yesReturnButton.getScene.getWindow.hide()
    HexUtils.saveMyRandom(FxApp.container.random)
  }

  def onMouseClickedCancelReturn(): Unit = {
    noReturnButton.getScene.getWindow.hide()
  }

  /* Main Menu */

  // Start a new game.
  def onMouseClickedStart(): Unit = {
    FxApp.container = Container.create(settings = getUserSettings);
    val stage: Stage = startNewGameButton.getScene.getWindow.asInstanceOf[Stage]
    val fxmlLoader = new FXMLLoader(getClass.getResource("GameWindow.fxml"))
    val mainViewRoot: Parent = fxmlLoader.load()
    val scene: Scene = new Scene(mainViewRoot)
    stage.setScene(scene); stage.show()
    FxApp.container.settings._2 match {
      case User => FxApp.container = Container(FxApp.container.state, FxApp.container.positions, HexUtils.loadMyRandom().get, FxApp.container.settings)
      case Computer =>
        val (position, random): ((Int, Int), MyRandom) = FxApp.container.state.randomMove(HexUtils.loadMyRandom().get)
        val state: GameState = GameState(FxApp.container.state.play(Cells.Red, position._1, position._2), Option(position))
        FxApp.container = Container(state, position :: FxApp.container.positions, random, FxApp.container.settings)
        val polygon: Polygon = scene.lookup(s"#${ControllerUtils.getButtonId(position)}").asInstanceOf[Polygon]
        polygon.setFill(Color.RED)
    }
  }

  // Retrieve user settings.
  private def getUserSettings: Settings = {
    val difficulty: Difficulty = if (normal.isSelected) then Difficulty.Normal else Difficulty.Easy
    val firstPlayer: FirstPlayer = if (user.isSelected) then FirstPlayer.User else FirstPlayer.Computer
    (difficulty, firstPlayer, UserInterface.GUI)
  }

  // Applies the default settings to the specified scene.
  private def applyDefaultSettingsTo(scene: Scene): Unit = {
    scene.lookup("#normal").asInstanceOf[RadioButton].setSelected(true)
    scene.lookup("#easy").asInstanceOf[RadioButton].setSelected(false)
    scene.lookup("#user").asInstanceOf[RadioButton].setSelected(true)
    scene.lookup("#computer").asInstanceOf[RadioButton].setSelected(false)
  }

  // Load a previous saved game.
  def onMouseClickedLoad(): Unit = {
    val scene: Scene = loadWindow("Load Game", "LoadGamePopup.fxml", loadGamePopupButton, false, false)
  }

  // Load a previous saved game.
  def onMouseClickedConfirmLoad(): Unit = {
    val stage: Stage = loadGameButton.getScene.getWindow.asInstanceOf[Stage].getOwner.asInstanceOf[Stage]
    val fxmlLoader = new FXMLLoader(getClass.getResource("GameWindow.fxml"))
    val mainViewRoot: Parent = fxmlLoader.load()
    val scene: Scene = new Scene(mainViewRoot)
    stage.setScene(scene); stage.show()
    loadGameButton.getScene.getWindow.hide()
    FxApp.container = HexUtils.load(loadGameTextField.getText).get
    fillPositions(FxApp.container.state.getOccupiedBy(Cells.Red), Color.RED, scene)
    fillPositions(FxApp.container.state.getOccupiedBy(Cells.Blue), Color.BLUE, scene)
  }

  // Draw the board by filling the corresponding Polygon elements with the specified color.
  private def fillPositions(positions: List[(Int, Int)], color: Color, scene: Scene): Unit = {
    positions.map(position => scene.lookup(s"#${ControllerUtils.getButtonId(position)}").asInstanceOf[Polygon].setFill(color)): @nowarn
  }

  def onMouseClickedCancelLoad(): Unit = {
    cancelLoadGameButton.getScene.getWindow.hide()
  }

  def onMouseClickedAcknowledgeWinner(): Unit = {
    val stage: Stage = acknowledgeWinnerButton.getScene.getWindow.asInstanceOf[Stage].getOwner.asInstanceOf[Stage]
    val fxmlLoader = new FXMLLoader(getClass.getResource("MainWindow.fxml"))
    val mainViewRoot: Parent = fxmlLoader.load()
    val scene = new Scene(mainViewRoot)
    applyDefaultSettingsTo(scene); stage.setScene(scene); stage.show()
    acknowledgeWinnerButton.getScene.getWindow.hide()
    HexUtils.saveMyRandom(FxApp.container.random)
  }

}