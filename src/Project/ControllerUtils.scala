package Project

import Project.GameOptions.{MainMenu, GameMenu, Settings}
import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage
import scala.annotation.tailrec
import scala.util.matching.Regex

object ControllerUtils {

  // Extracts the position from a button ID string.
  def getPosition(buttonId: String): String = {
    val pattern: Regex = """P(\d+)(\d+)""".r
    buttonId.replaceAll(pattern.toString, "$1,$2")
  }

  // Generates the button ID string based on the given position coordinates.
  def getButtonId(position: (Int, Int)): String = s"P${position._1}${position._2}"

  // Extracts the position tuple from a string in the format "a,b".
  def getBoardPosition(position: String): (Int, Int) = {
    val pattern: Regex = """(\d+),(\d+)""".r
    position match
      case pattern(a, b) => (a.toInt, b.toInt)
      case _ => throw new IllegalArgumentException("Invalid position!")
  }

}
