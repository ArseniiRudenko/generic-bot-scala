package work.arudenko.bot

import better.files._


object ConsoleRunner {

  sealed trait Mode
  case object Bot extends Mode
  case object Usage extends Mode
  case class MarkovGen(in:File,out:File,names:Option[Seq[String]]) extends Mode
  case class Config(mode:Mode=Usage)


  implicit val FileRead: scopt.Read[File] =
    scopt.Read.reads(File(_))



  def main(args: Array[String]): Unit = {
    import scopt.OParser
    val builder = OParser.builder[Config]
    val parser1 = {
      import builder._
      OParser.sequence(
        programName("nikita-bot"),
        head("nikita-bot", "0.1"),
        cmd("bot")
          .action((_,c)=>c.copy(mode = Bot))
          .text("runs bot in foreground"),
        cmd("chain-gen")
          .action((_, c) =>c.copy(mode = MarkovGen(File(""),File(""),None)))
          .text("chain-gen is used to generate markov chain from tg chat export file")
          .children(
            opt[File]("source")
              .required()
              .validate(f=>if(f.exists) success else failure(s"file ${f.path.toString} not found"))
              .abbr("s")
              .action((s, c) => c.copy(mode =c.mode.asInstanceOf[MarkovGen].copy(in = s)))
              .text("soruce file with exported telegram chat"),
            opt[File]("target")
               .required()
               .abbr("t")
              .action((s, c) => c.copy(mode =c.mode.asInstanceOf[MarkovGen].copy(out = s)))
               .text("target file with generated model"),
            opt[Seq[String]]("users")
              .valueName("<user1>,<user2>...")
              .abbr("u")
              .action((n, c) => c.copy(mode=c.mode.asInstanceOf[MarkovGen].copy(names = Some(n))))
              .text("users to extract text for"),
          )
      )
    }

    // OParser.parse returns Option[Config]
    OParser.parse(parser1, args, Config()) match {
      case Some(config) =>
        config.mode match {
          case Usage => println(OParser.usage(parser1))
          case Bot => BotRunner.run()
          case a:MarkovGen =>TgChatMarkov.runGen(a)
        }
      // do something
      case _ =>
      // arguments are bad, error message will have been displayed
    }
  }


}
