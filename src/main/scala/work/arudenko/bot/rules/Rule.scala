package work.arudenko.bot.rules
import scala.util.matching.Regex
import cats.syntax.functor._
import io.circe.{ Decoder, Encoder }, io.circe.generic.auto._
import io.circe.syntax._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import work.arudenko.markov.discrete.DiscreteMarkovChain
import work.arudenko.markov.discrete.DiscreteMarkovChain._


sealed trait Rule {
  def matches(text:String): Boolean
  def getAnswer:Answer
  def getMatch(text:String):Option[String] = if(matches(text)) Some(getAnswer.get(text)) else None
}




case class RegexRule(matchPatterns:Seq[Regex], answer: Answer) extends Rule {
  override def matches(text: String): Boolean = matchPatterns.exists(regex=>regex.findFirstIn(text).isDefined)
  override def getAnswer: Answer = answer
}


case class SubstringRule(matchStrings:Seq[String], answer: Answer,ignoreCase: Boolean = true) extends Rule {
  override def matches(text: String): Boolean =
    if(ignoreCase)
      matchStrings.exists(str=>text.toLowerCase.contains(str.toLowerCase))
    else
      matchStrings.exists(str=>text.contains(str))
  override def getAnswer: Answer = answer
}

case class TextRule(matchTextExact:Seq[String], answer: Answer,ignoreCase: Boolean = true) extends Rule {
  lazy val matchTextExactLower: Seq[String] = matchTextExact.map(_.toLowerCase)

  override def matches(text: String): Boolean =
    if(ignoreCase)
      matchTextExactLower.contains(text.toLowerCase)
    else
      matchTextExact.contains(text)
  override def getAnswer: Answer = answer
}
