package work.arudenko.bot

import canoe.api._
import canoe.models.messages.TextMessage
import canoe.syntax._
import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import work.arudenko.bot.rules.Rule

class Bot(token: String, rules:List[Rule]) extends IOApp {

  val partialAnswer:PartialFunction[TextMessage,(TextMessage,String)] =
    m => rules.map(_.getMatch(m.text)).fold(None)((a,b)=>a.orElse(b)).map(str=>(m,str)) match {
      case Some(value)=>value
    }

  val messageThatHasAnswer:Expect[(TextMessage,String)] = textMessage.andThen(partialAnswer)

  def answers[F[_]: TelegramClient]: Scenario[F, Unit] =
    for {
      msg <- Scenario.expect(messageThatHasAnswer)
      _   <- Scenario.eval(msg._1.chat.send(msg._2,replyToMessageId = Some(msg._1.messageId)))
    } yield ()


  def run(args: List[String]): IO[ExitCode] =
    Stream
      .resource(TelegramClient.global[IO](token))
      .flatMap { implicit client =>
        Bot.polling[IO].follow(answers)
      }
      .compile
      .drain
      .as(ExitCode.Success)
}