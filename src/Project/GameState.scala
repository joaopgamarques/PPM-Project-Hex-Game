package Project

import Project.Board
import Project.Cells.Cell
import scala.Console
import scala.annotation.tailrec

// Implement all instance methods.
case class GameState(board: Board, move: Option[(Int, Int)]) {
  def randomMove(random: MyRandom): ((Int, Int), MyRandom) = GameState.randomMove(this.board, random)
  def randomMoveAlternative(random: MyRandom): ((Int, Int), MyRandom) = GameState.randomMoveAlternative(this.board, random)
  def play(player: Cells.Cell, row:Int, column:Int): Board = GameState.play(this.board, player, row, column)
  def remove(positions: List[(Int, Int)]): Board = GameState.remove(this.board, positions)
  def isEmpty(row: Int, column: Int): Boolean = GameState.isEmpty(this.board, row, column)
  def isOccupied(row: Int, column: Int): Boolean = GameState.isOccupied(this.board, row, column)
  def isBoardEmpty: Boolean = GameState.isBoardEmpty(this.board)
  def isBoardFull: Boolean = GameState.isBoardFull(this.board)
  def getNumberOfPieces: Int = GameState.getNumberOfPieces(this.board)
  def hasEvenNumberOfPieces: Boolean = GameState.hasEvenNumberOfPieces(this.board)
  def getPiece(row: Int, column: Int): Cells.Cell = GameState.getPiece(this.board, row, column)
  def getOccupiedBy(player: Cells.Cell): List[(Int, Int)] = GameState.getOccupiedBy(this.board, player)
  def getNeighbors(row: Int, column: Int): List[(Int, Int)] = GameState.getNeighbors(this.board, row, column)
  def hasContiguousLine(player: Cells.Cell): Boolean = GameState.hasContiguousLine(this.board, player)
  def hasWinner: Boolean = GameState.hasWinner(this.board)
}

object GameState {

  private val NEIGHBORS: List[(Int, Int)] = List((1, 0), (1, -1), (0, 1), (0, -1), (-1, 1), (-1, 0))

  // Return true whether a given cell exists within the board's boundaries.
  private def exists(board: Board, row: Int, column: Int): Boolean = row >= 0 && row < board.size && column >= 0 && column < board.size

  // Return true whether a given cell is empty.
  def isEmpty(board: Board, row: Int, column: Int): Boolean = (row, column) match {
    case (row, column): (Int, Int) if exists(board, row, column) =>
      if (board.apply(row).apply(column).equals(Cells.Empty)) then true else false
    case _ => false
  }

  // Return true if a given position is already occupied.
  private def isOccupied(board: Board, row: Int, column: Int): Boolean = !isEmpty(board, row, column) && exists(board, row, column)

  // Return the piece located at a given position.
  private def getPiece(board: Board, row: Int, column: Int): Cells.Cell = {
    getFlattenedListOfCells(board).apply(row * board.size + column)._1
  }

  // Return all the positions where the player has placed a piece.
  private def getOccupiedBy(board: Board, player: Cells.Cell): List[(Int, Int)] = {
    getFlattenedListOfCells(board).filter(cell => cell._1.equals(player)).map(cell => cell._2)
  }
  
  // Return all adjacent positions.
  private def getNeighbors(board: Board, row: Int, column: Int): List[(Int, Int)] = {
    getFlattenedListOfCells(board).filter(cell => NEIGHBORS.contains((row - cell._2._1, column - cell._2._2))).map(cell => cell._2)
  }

  // Return true whether all positions are already occupied.
  private def isBoardFull(board: Board): Boolean = getOccupiedBy(board, Cells.Empty).isEmpty
  
  // Return the number of positions already occupied.
  private def getNumberOfPieces(board: Board): Int = getOccupiedBy(board, Cells.Red).size + getOccupiedBy(board, Cells.Blue).size
  
  // Return true whether all positions are empty.
  private def isBoardEmpty(board: Board): Boolean = getNumberOfPieces(board).equals(0)

  // Return true whether the board has an even number of pieces.
  private def hasEvenNumberOfPieces(board: Board): Boolean = (getNumberOfPieces(board) % 2).equals(0)

  // T1: Implementar o método randomMove(board: Board, random:MyRandom):((Int, Int), MyRandom) responsável
  // por gerar uma coordenada aleatória válida para a próxima jogada (recebe e devolve MyRandom para poder ser puro).
  private def randomMove(board: Board, random: MyRandom): ((Int, Int), MyRandom) = {
    // Get a flattened list of cells with their positions and filter out the cells that are empty.
    val listOfEmptyCells: List[(Cells.Cell, (Int, Int))] = getFlattenedListOfCells(board).filter(cell => cell._1.equals(Cells.Empty))
    val (index, nextRandom): (Int, MyRandom) = random.nextInt(listOfEmptyCells.size - 1)
    // Determine the position based on the selected index from the list of empty cells.
    val position: (Int, Int) = listOfEmptyCells.apply(index)._2
    (position, nextRandom)
  }

  private def randomMoveAlternative(board: Board, random: MyRandom): ((Int, Int), MyRandom) = {
    // Get a flattened list of cells with their positions.
    val flattenedBoard: List[(Cells.Cell, (Int, Int))] = getFlattenedListOfCells(board)
    // Filter out the cells that are empty.
    val listOfEmptyCells: List[(Cells.Cell, (Int, Int))] = flattenedBoard.filter(cell => cell._1.equals(Cells.Empty))
    // Filter out the empty cells that have at least one adjacent cell with the value Cells.Red.
    val listOfAdjacentEmptyCells: List[(Cells.Cell, (Int, Int))] =
      listOfEmptyCells.filter(cell => getNeighbors(board, cell._2._1, cell._2._2).exists(neighbor => getPiece(board, neighbor._1, neighbor._2).equals(Cells.Red)))
    val (index, nextRandom): (Int, MyRandom) = listOfAdjacentEmptyCells match
      case Nil => random.nextInt(listOfEmptyCells.size - 1)
      case List(value) => (0, random)
      case head :: tail => random.nextInt(listOfAdjacentEmptyCells.size - 1)
    // Determine the position based on the index and the list of adjacent empty cells.
    val position: (Int, Int) = listOfAdjacentEmptyCells match
      case Nil => listOfEmptyCells.apply(index)._2
      case head :: tail => listOfAdjacentEmptyCells.apply(index)._2
    (position, nextRandom)
  }

  // Return an indexed nested list of cells.
  private def getIndexedNestedListOfCells(board: Board): List[List[(Cells.Cell, (Int, Int))]] = board.zipWithIndex.map {
    case (innerListOfCells, outerIndex): (List[Cells.Cell], Int) => innerListOfCells.zipWithIndex.map {
      case (innerElement, innerIndex): (Cells.Cell, Int) => (innerElement, (outerIndex, innerIndex))
    }
  }

  // Return a flattened indexed list of cells.
  private def getFlattenedListOfCells(board: Board): List[(Cells.Cell, (Int, Int))] = getIndexedNestedListOfCells(board).flatten

  // T2: Implementar o método play(board:Board, player: Cells.Cell, row:Int, column:Int): Board responsável por efetuar
  // uma dada jogada (i.e., do computador ou do jogador).
  private def play(board: Board, player: Cells.Cell, row:Int, column:Int): Board =
    if(isEmpty(board, row, column)) then board.updated(row, board.apply(row).updated(column, player)) else board

  // Remove a list of pieces from the board.
  private def remove(board: Board, positions: List[(Int, Int)]): Board = {
    // Iterate over the positions using foldRight.
    positions.foldRight(board){(position, updatedBoard) =>
      val (row, column): (Int, Int) = position
      // Update the cell at the current position to Cells.Empty.
      updatedBoard.updated(row, updatedBoard(row).updated(column, Cells.Empty))
    }
  }

  // T4: Implementar o método hasContiguousLine responsável por verificar se o computador ou o jogador ganhou o jogo.
  private def hasContiguousLine(board: Board, player: Cells.Cell): Boolean = {
    val flattenedBoard: List[(Cell, (Int, Int))] = getFlattenedListOfCells(board)
    // First, we need to find all the positions where the player has placed a piece.
    val positions: List[(Int, Int)] = flattenedBoard.filter(cell => cell._1.equals(player)).map(cell => cell._2)
    // We'll keep track of the positions that we've already visited to avoid infinite recursion.
    val visited: List[(Int, Int)] = List[(Int, Int)]()

    // Define a recursive inner function that will traverse the board from a given position and check if the player has won.
    def traverse(position: (Int, Int), visited: List[(Int, Int)]): Boolean = {
      val (row, column): (Int, Int) = position
      // Mark the current position as visited.
      val newVisited: List[(Int, Int)] = position :: visited
      if(player.equals(Cells.Red) && position._2.equals(board.size-1)) then return true
      if(player.equals(Cells.Blue) && position._1.equals(board.size-1)) then return true
      // Otherwise, we need to check the adjacent positions.
      val neighbors: List[(Int, Int)] =
        getNeighbors(board, row, column).filter(position => flattenedBoard.contains((player, position)) && !newVisited.contains(position))
      // Recursively traverse each adjacent position.
      neighbors.exists(position => traverse(position, newVisited))
    }

    player match
      // Call the recursive function on each position on the leftmost column of the board.
      case Project.Cells.Red => positions.filter(position => position._2.equals(0)).exists(position => traverse(position, visited))
      // Call the recursive function on each position on the top row of the board.
      case Project.Cells.Blue => positions.filter(position => position._1.equals(0)).exists(position => traverse(position, visited))
      case Project.Cells.Empty => false
  }

  // Return true whether the user or the computer have won the game.
  private def hasWinner(board: Board): Boolean = hasContiguousLine(board, Cells.Blue) || hasContiguousLine(board, Cells.Red)
  
}