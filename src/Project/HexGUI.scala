package Project

import Project.Board
import Project.Cells.Cell
import Project.Options
import Project.GameOptions
import Project.GameOptions.{MainMenu, GameMenu, Settings}
import Project.Controller
import Project.Container
import Project.ControllerUtils
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.control.RadioButton
import javafx.scene.{Parent, Scene}
import javafx.stage.{Modality, Stage}

class HexGUI extends Application {

  override def start(stage: Stage): Unit = {
    stage.setTitle("Hex Board Game")
    // Load the FXML file.
    val fxmlLoader = new FXMLLoader(getClass.getResource("MainWindow.fxml"))
    val mainViewRoot: Parent = fxmlLoader.load()
    // Create a scene with the loaded FXML content.
    val scene: Scene = new Scene(mainViewRoot)
    // Set the scene on the stage.
    stage.setScene(scene)
    // Show the stage.
    stage.show()
    // Access the RadioButtons in the scene by their IDs and set them as selected or not selected.
    scene.lookup("#normal").asInstanceOf[RadioButton].setSelected(true)
    scene.lookup("#easy").asInstanceOf[RadioButton].setSelected(false)
    scene.lookup("#user").asInstanceOf[RadioButton].setSelected(true)
    scene.lookup("#computer").asInstanceOf[RadioButton].setSelected(false)
  }
  
}

object FxApp {
  // Create a container instance.
  var container: Container = Container.create();
  // Launch the GUI application.
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[HexGUI], args: _*)
  }
}