package work.arudenko.bot


import pureconfig._
import pureconfig.generic.auto._
import better.files.Resource
import io.circe.parser.decode
import pureconfig.ConfigSource
import work.arudenko.bot.rules.Rule
import work.arudenko.bot.rules.RuleSerializer._

object BotRunner {

  def run(): Unit = {
    val config = ConfigSource.default.loadOrThrow[BotConfig]
    val textProcessingRules:List[Rule] = decode[List[Rule]](config.rules.getRules).toTry.get
    val bot = new Bot(config.token,textProcessingRules)
    val eol = bot.run(Nil)
    val cancel = eol.unsafeRunCancelable(result=>println(result))
    println("Press [ENTER] to shutdown the bot, it may take a few seconds...")
    scala.io.StdIn.readLine()
    println("shutting down...")
    // initiate shutdown
    // Wait for the bot end-of-life
    cancel.unsafeRunSync()
  }





}
