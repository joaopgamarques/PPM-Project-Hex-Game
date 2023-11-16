package Project

import Project.Board
import Project.Cells.Cell
import Project.Options
import Project.GameOptions.{MainMenu, GameMenu, Settings}
import Project.Difficulty.{Normal, Easy}
import Project.FirstPlayer.{User, Computer}
import Project.UserInterface.{TUI, GUI}
import scala.annotation.tailrec
import scala.collection.SortedMap

object HexTUI extends App {

  HexUtils.showWelcomeMessage()
  private val DEFAULT_SETTINGS: Settings = (Normal, User, TUI)
  mainLoop(Container.create(settings = DEFAULT_SETTINGS), GameOptions.MainMenu)

  @tailrec
  private def mainLoop(container: Container, gameOptions: GameOptions): Unit = {
    gameOptions match {
      case MainMenu =>
        HexUtils.userOptionsPrompt(Options.mainMenu) match {
          case Some(option) => option.name match {
            case "Start" | "Load" | "Resume" => val nextContainer = option.execute(container); mainLoop(nextContainer, GameOptions.GameMenu)
            case "Settings" => val nextContainer = option.execute(container); mainLoop(nextContainer, GameOptions.Settings)
            case "Exit" => HexUtils.saveMyRandom(container.random); val nextContainer = option.execute(container); mainLoop(nextContainer, GameOptions.MainMenu)
          }
          case _ => println(Console.RED + "The option you just entered is not valid." + Console.RESET); mainLoop(container, GameOptions.MainMenu)
        }
      case GameMenu =>
        HexUtils.userOptionsPrompt(Options.gameMenu) match {
          case Some(option) => option.name match {
            case "Play" | "Save" | "Undo" => val nextContainer = option.execute(container); mainLoop(nextContainer, GameOptions.GameMenu)
            case "Return" => HexUtils.saveMyRandom(container.random); val nextContainer = option.execute(container); mainLoop(nextContainer, GameOptions.MainMenu)
          }
          case _ => println(Console.RED + "The option you just entered is not valid." + Console.RESET); mainLoop(container, GameOptions.GameMenu)
        }
      case Settings =>
        HexUtils.userOptionsPrompt(Options.settings) match {
          case Some(option) => option.name match {
            case "Board's size" | "Difficulty" | "First player" => val nextContainer = option.execute(container); mainLoop(nextContainer, GameOptions.MainMenu)
            case "Return" => val nextContainer = option.execute(container); mainLoop(nextContainer, GameOptions.MainMenu)
          }
          case _ => println(Console.RED + "The option you just entered is not valid." + Console.RESET); mainLoop(container, GameOptions.Settings)
        }
    }
  }
}