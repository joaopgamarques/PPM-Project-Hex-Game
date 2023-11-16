package Project

import Project.Board
import Project.Cells.Cell
import Project.Difficulty.{Easy, Normal}
import Project.FirstPlayer.{Computer, User}
import scala.io.StdIn.readLine
import scala.io.StdIn.readInt
import scala.Console
import scala.annotation.{nowarn, tailrec}
import scala.util.matching.Regex
import java.io.{FileInputStream, FileOutputStream, IOException, ObjectInputStream, ObjectOutputStream}
import scala.collection.SortedMap
import scala.util.{Failure, Success, Try}

// Implement all user interface functions (impure).
object HexUtils {

  def showWelcomeMessage(): Unit = println("Welcome!")

  @tailrec
  def userOptionsPrompt(options: SortedMap[Int, CommandLineOption]): Option[CommandLineOption] = {
    options.match {
      case Options.mainMenu => println("-- Main menu --")
      case Options.gameMenu => println("-- Game menu --")
      case Options.settings => println("-- Settings --")
    }
    options.toList.map((option: (Int, CommandLineOption)) => println(option._1 + ") " + option._2.name)): @nowarn
    getUserInput("Select an option") match {
      case Success(i) => if(options.toList.unzip._1.contains(i)) then options.get(i) else userOptionsPrompt(options)
      case Failure(_) => println("The option you have selected is not valid."); userOptionsPrompt(options)
    }
  }

  private def getUserInput(message: String): Try[Int] = {
    print(message + ": ")
    Try(readLine.trim.toUpperCase.toInt)
  }

  private def prompt(message: String): String = {
    print(message + ": ")
    readLine()
  }

  // T3: Representar, visualmente, as jogadas no tabuleiro.
  def printContainer(container: Container): Unit = {

    if (container.settings._3.equals(UserInterface.GUI)) then return

    // Map which associates a cell value with its corresponding string representation.
    val marker = Map(Cells.Blue -> (Console.BLUE + "O" + Console.RESET),
      Cells.Red -> (Console.RED + "X" + Console.RESET),
      Cells.Empty -> ".")

    // Print the header with the current player and move information.
    def printHeader(container: Container): Unit = Some(container.state.move).get match {
      case Some(value) =>
        container.settings._2 match
          case FirstPlayer.User =>
            val player: String = if (container.state.hasEvenNumberOfPieces) then "Computer" else "User"
            println(s"$player Moves: " + value._1.toString + " " + value._2.toString)
          case FirstPlayer.Computer =>
            val player: String = if (container.state.hasEvenNumberOfPieces) then "User" else "Computer"
            println(s"$player Moves: " + value._1.toString + " " + value._2.toString)
      case None => println("Computer Moves:")
    }

    // Print the game board.
    @tailrec
    def printBoard(board: List[(List[Cells.Cell], Int)], size: Int): Unit = board match {
      case Nil => print("")
      case head :: tail =>
        if (head._2 < 10) then print(" " * 2 * head._2 + head._2 + Console.RED + " * " + Console.RESET)
        else print(" " * (2 * head._2 - 2) + " " + head._2 + Console.RED + " * " + Console.RESET)
        printRow(head._1)
        println(Console.RED + " * " + Console.RESET + head._2.toString)
        if (tail.nonEmpty) then println(" " * (2 * head._2 + 5) + "\\ / " * (size - 1) + "\\")
        printBoard(tail, size)
    }

    // Print a row of cells separated by "-".
    @tailrec
    def printRow(row: List[Cells.Cell]): Unit = row match {
      case Nil => print("")
      case List(cell) => print(marker(cell))
      case head :: tail =>
        print(marker(head) + " - ")
        printRow(tail)
    }

    printHeader(container)
    println(" " * 3 + List.range(0, container.state.board.size).map(index => index.toString).foldRight("")((index, string) =>
      if (index.toInt < 10) then " " + index + "  " + string else " " + index + " " + string))
    println(" " * 3 + Console.BLUE + " *  " * container.state.board.size + Console.RESET)
    printBoard(container.state.board.zipWithIndex, container.state.board.size)
    println(Console.BLUE + " " * (2 * container.state.board.size + 1) + " *  " * container.state.board.size + Console.RESET)
    println(" " * (2 * container.state.board.size + 1) + List.range(0, container.state.board.size).map(index => index.toString).foldRight("")((index, string) =>
      if (index.toInt < 10) then " " + index + "  " + string else " " + index + " " + string))
  }

  // Prompt the user to enter the board's size.
  private def showGetBoardSizePrompt(): Unit = print("Please enter the board's size: ")

  @tailrec
  def getBoardSize: Int = {
    showGetBoardSizePrompt()
    val size: Try[Int] = Try(readInt())
    size match
      case Success(value) => if(value >= 3 && value <= 21) then value else getBoardSize
      case Failure(exception) => getBoardSize
  }

  // Prompt the user to select the game's difficulty.
  private def showGetGameDifficultyPrompt(): Unit = print("Please enter the game's difficulty: Easy(1), Normal(2) ")

  @tailrec
  def getGameDifficulty: Difficulty = {
    showGetGameDifficultyPrompt()
    val difficulty: Try[Int] = Try(readInt())
    difficulty match
      case Success(value) => value match {
        case 1 => Difficulty.Easy
        case 2 => Difficulty.Normal
        case _ => getGameDifficulty
      }
      case Failure(exception) => getGameDifficulty
  }

  // Prompt the user to select the first player.
  private def showGetFirstPlayerPrompt(): Unit = print("Please enter the first player: User(1), Computer(2) ")

  @tailrec
  def getFirstPlayer: FirstPlayer = {
    showGetFirstPlayerPrompt()
    val firstPlayer: Try[Int] = Try(readInt())
    firstPlayer match
      case Success(value) => value match {
        case 1 => FirstPlayer.User
        case 2 => FirstPlayer.Computer
        case _ => getFirstPlayer
      }
      case Failure(exception) => getFirstPlayer
  }

  // Prompt the user to enter his/her the next move.
  private def showUserNextMovePrompt(): Unit = print("Please enter your next move (e.g. 1,2): ")

  @tailrec
  def getUserNextMove: String = {
    showUserNextMovePrompt()
    val pattern: Regex = "[0-9],[0-9]".r
    val move: String = readLine().trim().toUpperCase()
    pattern.findFirstMatchIn(move) match
      case Some(_) => move
      case None => getUserNextMove
  }

  def showUserNextMoveWarning(): Unit = println(Console.RED + "The cell you just entered is not available!" + Console.RESET)

  def showHexBoardWarning(): Unit = println(Console.RED + "There are no empty cells available!" + Console.RESET)

  def showUserWinsMessage(): Unit = println(Console.BLUE + "The user wins the game!" + Console.RESET)

  def showComputerWinsMessage(): Unit = println(Console.RED + "The computer wins the game!" + Console.RESET)

  // Save the game to a binary file.
  def showSaveGamePrompt(): String = readLine("Please enter the name of the file to save the game to: ")

  def save(container: Container, filename: String): Unit = {
    var fileOut: Option[FileOutputStream] = None
    var objectOut: Option[ObjectOutputStream] = None
    try {
      fileOut = Some(new FileOutputStream(filename + ".bin"))
      objectOut = Some(new ObjectOutputStream(fileOut.get))
      objectOut.get.writeObject(container)
    } catch {
      case exception: IOException => println(Console.RED + "An error occurred while saving the file." + Console.RESET)
    } finally {
      if(objectOut.isDefined) then objectOut.get.close()
      if(fileOut.isDefined) then fileOut.get.close()
    }
  }

  // Load a game from a binary file.
  def showLoadGamePrompt(): String = readLine("Please enter the name of the file to load the game from: ")

  def load(filename: String): Option[Container] = {
    var fileIn: Option[FileInputStream] = None
    var objectIn: Option[ObjectInputStream] = None
    var container: Option[Container] = None
    try {
      fileIn = Some(new FileInputStream(filename + ".bin"))
      objectIn = Some(new ObjectInputStream(fileIn.get))
      container = Some(objectIn.get.readObject().asInstanceOf[Container])
    } catch {
      case exception: IOException => println(Console.RED + "An error occurred while loading the file." + Console.RESET)
    } finally {
      if(objectIn.isDefined) then objectIn.get.close()
      if(fileIn.isDefined) then fileIn.get.close()
    }
    container
  }

  // Save the current MyRandom to a binary file.
  def saveMyRandom(random: MyRandom): Unit = {
    var fileOut: Option[FileOutputStream] = None
    var objectOut: Option[ObjectOutputStream] = None
    try {
      fileOut = Some(new FileOutputStream( "MyRandom.bin"))
      objectOut = Some(new ObjectOutputStream(fileOut.get))
      objectOut.get.writeObject(random)
    } catch {
      case exception: IOException => println(Console.RED + "An error occurred while saving the file." + Console.RESET)
    } finally {
      if (objectOut.isDefined) then objectOut.get.close()
      if (fileOut.isDefined) then fileOut.get.close()
    }
  }

  // Load a MyRandom from a binary file.
  def loadMyRandom(): Option[MyRandom] = {
    var fileIn: Option[FileInputStream] = None
    var objectIn: Option[ObjectInputStream] = None
    var random: Option[MyRandom] = None
    try {
      fileIn = Some(new FileInputStream("MyRandom.bin"))
      objectIn = Some(new ObjectInputStream(fileIn.get))
      random = Some(objectIn.get.readObject().asInstanceOf[MyRandom])
    } catch {
      case exception: IOException => saveMyRandom(Container.create().random)
    } finally {
      if (objectIn.isDefined) then objectIn.get.close()
      if (fileIn.isDefined) then fileIn.get.close()
    }
    if (random.isDefined) then random else Some(Container.create().random)
  }
  
}