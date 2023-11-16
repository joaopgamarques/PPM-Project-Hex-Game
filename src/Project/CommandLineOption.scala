package Project

case class CommandLineOption(name: String, execute: Container => Container)