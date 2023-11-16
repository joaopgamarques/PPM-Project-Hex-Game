package Project

import Project.Board
import Project.Cells.Cell
import Project.FirstPlayer.{Computer, User}
import Project.Difficulty.{Easy, Normal}
import Project.UserInterface.{GUI, TUI}
import Project.GameState
import scala.annotation.{tailrec, targetName}

case class Container(state: GameState, positions: List[(Int, Int)], random: MyRandom, settings: Settings) {
}

object Container {

  private val DEFAULT_SEED: Long = 453665644
  private val DEFAULT_SIZE: Int = 5
  private val DEFAULT_SETTINGS: Settings = (Normal, User, GUI)

  // Create a new container.
  def create(size: Int = DEFAULT_SIZE, seed: Long = DEFAULT_SEED, settings: Settings = DEFAULT_SETTINGS): Container = {
    val state: GameState = GameState(List.fill(size)(List.fill(size)(Cells.Empty)), None)
    val positions: List[(Int, Int)] = List[(Int, Int)]()
    val random: MyRandom = MyRandom(seed)
    Container(state, positions, random, settings)
  }

  // Return the current container.
  def getContainer()(container: Container): Container = container

  // Return a new container with different settings: board's size.
  def setBoardSize(size: => Int)(container: Container): Container = Container.create(size, DEFAULT_SEED, container.settings)

  // Return a new container with different settings: game's difficulty.
  def setGameDifficulty(difficulty: => Difficulty)(container: Container): Container =
    Container.create(container.state.board.size, DEFAULT_SEED, (difficulty, container.settings._2, container.settings._3))

  // Return a new container with different settings: first player.
  def setFirstPlayer(firstPlayer: => FirstPlayer)(container: Container): Container =
    Container.create(container.state.board.size, DEFAULT_SEED, (container.settings._1, firstPlayer, container.settings._3))

  // Start a new game.
  def start()(container: Container): Container = Container.create(container.state.board.size, HexUtils.loadMyRandom().get.seed, container.settings)

  // Prompt the user to enter his/her next move and the computer will respond accordingly.
  def play(move: => String)(container: Container): Container = {
    val (difficulty, firstPlayer, userInterface): Settings = container.settings
    if(container.state.hasWinner) then return container

    if(container.state.isBoardFull) then
      HexUtils.showHexBoardWarning(); return container

    val lastContainer: Container = container.settings._2 match
      case FirstPlayer.User => if (container.state.isBoardEmpty) {HexUtils.printContainer(container); container} else {container}
      case FirstPlayer.Computer => getFirstRandomMove(container)

    move match {
      case s"$a,$b" if (GameState.isEmpty(lastContainer.state.board, a.toInt, b.toInt)) =>
        val state: GameState = GameState(lastContainer.state.play(Cells.Blue, a.toInt, b.toInt), Some((a.toInt, b.toInt)))
        if (state.hasContiguousLine(Cells.Blue)) then HexUtils.showUserWinsMessage()
        val container: Container = Container(state, Some((a.toInt, b.toInt)).get :: lastContainer.positions, lastContainer.random, lastContainer.settings)
        if (!state.isBoardFull && !state.hasWinner) {
          val randomMove: ((Int, Int), MyRandom) = getRandomMove(container)
          val nextState: GameState = GameState(state.play(Cells.Red, randomMove._1._1, randomMove._1._2), Some(randomMove._1))
          if (nextState.hasContiguousLine(Cells.Red)) then HexUtils.showComputerWinsMessage()
          val nextContainer: Container = Container(nextState, Some(randomMove._1).get :: container.positions, randomMove._2, lastContainer.settings)
          HexUtils.printContainer(nextContainer); nextContainer
        } else {
          HexUtils.printContainer(container); container
        }
      case _ => HexUtils.showUserNextMoveWarning(); container
    }
  }

  // Return the computer first move.
  private def getFirstRandomMove(container: Container): Container = {
    if (!container.state.isBoardEmpty) then return container
    val randomMove: ((Int, Int), MyRandom) = getRandomMove(container)
    val state: GameState = GameState(container.state.play(Cells.Red, randomMove._1._1, randomMove._1._2), Some(randomMove._1))
    val nextContainer: Container = Container(state, Some(randomMove._1).get :: container.positions, randomMove._2, container.settings)
    HexUtils.printContainer(nextContainer); nextContainer
  }

  // Return the computer next move.
  private def getRandomMove(container: Container): ((Int, Int), MyRandom) = container.settings._1 match {
    case Easy => container.state.randomMove(container.random)
    case Normal => container.state.randomMoveAlternative(container.random)
  }

  // Revert the last move and restores the game state to the previous state.
  def undo()(container: Container): Container = {
    container.positions match
      case Nil => HexUtils.printContainer(container); container
      case List(value) =>
        container.settings._2 match
          case FirstPlayer.User =>
            val lastContainer: Container = Container.create(container.state.board.size)
            HexUtils.printContainer(lastContainer); lastContainer
          case FirstPlayer.Computer => HexUtils.printContainer(container); container
      case head :: tail =>
        val (removedPositions, remainingPositions): (List[(Int, Int)], List[(Int, Int)]) = container.positions.splitAt(2)
        if (remainingPositions.nonEmpty) {
          val state: GameState = GameState(container.state.remove(removedPositions), Some(remainingPositions.head))
          val lastContainer: Container = Container(state, remainingPositions, container.random, container.settings)
          HexUtils.printContainer(lastContainer); lastContainer
        } else {
          val lastContainer: Container = Container.create(container.state.board.size, container.random.seed, container.settings)
          HexUtils.printContainer(lastContainer); lastContainer
        }
  }

  // Load a previous saved container.
  def load(filename: => String)(container: Container): Container = {
    HexUtils.load(filename) match
      case Some(value) => HexUtils.printContainer(value); value
      case None => container
  }

  // Save the current container.
  def save(filename: => String)(container: Container): Container = {
    HexUtils.save(container, HexUtils.showSaveGamePrompt()); container
  }

  // Return to the main menu.
  def returnToMainMenu()(container: Container): Container = container

  // Resume the game.
  def resume()(container: Container): Container = {
    HexUtils.printContainer(container); container
  }
  
}