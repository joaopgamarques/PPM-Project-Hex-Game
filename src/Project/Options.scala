package Project

import scala.collection.SortedMap

object Options {
    val mainMenu: SortedMap[Int, CommandLineOption] = SortedMap[Int, CommandLineOption](
        1 -> CommandLineOption("Start", Container.start()),
        2 -> CommandLineOption("Load", Container.load(HexUtils.showLoadGamePrompt())),
        3 -> CommandLineOption("Settings", Container.getContainer()),
        4 -> CommandLineOption("Resume", Container.resume()),
        0 -> CommandLineOption("Exit", _ => sys.exit))

    val gameMenu: SortedMap[Int, CommandLineOption] = SortedMap[Int, CommandLineOption](
        1 -> CommandLineOption("Play", Container.play(HexUtils.getUserNextMove)),
        2 -> CommandLineOption("Save", Container.save(HexUtils.showSaveGamePrompt())),
        3 -> CommandLineOption("Undo", Container.undo()),
        0 -> CommandLineOption("Return", Container.returnToMainMenu()))

    val settings: SortedMap[Int, CommandLineOption] = SortedMap[Int, CommandLineOption](
        1 -> CommandLineOption("Board's size", Container.setBoardSize(HexUtils.getBoardSize)),
        2 -> CommandLineOption("Difficulty", Container.setGameDifficulty(HexUtils.getGameDifficulty)),
        3 -> CommandLineOption("First player", Container.setFirstPlayer(HexUtils.getFirstPlayer)),
        0 -> CommandLineOption("Return", Container.returnToMainMenu()))
}