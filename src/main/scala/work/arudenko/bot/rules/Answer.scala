package work.arudenko.bot.rules

import better.files.{File, Resource}
import cats.kernel.Semigroup
import io.circe.parser._
import work.arudenko.bot.rules.Answer.answerSemi
import work.arudenko.markov.discrete.DiscreteMarkovChain
import work.arudenko.markov.MarkovSerializer._
import io.circe.Decoder
import work.arudenko.markov.TextBasedMarkovChain.TextBasedMarkovChain

import scala.collection.immutable.ArraySeq
import scala.sys.process._
import scala.util.Random

sealed trait Answer{
  def get(text:String): String
  def + (other:Answer): Answer = answerSemi.combine(this,other)
}

object Answer {
  implicit val answerSemi: Semigroup[Answer] = (x: Answer, y: Answer) => AnswerCombination(ArraySeq(x, y))
  implicit def toAnswer(s: Seq[Answer]): Answer = AnswerCombination(ArraySeq.from(s))
}

case class SimpleMarkovChainAnswer(chain:DiscreteMarkovChain[String], targetLength:Option[Int]) extends Answer {
  override def get(text:String): String = chain.walkTxt(targetLength)
}

case class MarkovChainAnswer(pathToMarkovChain:String, targetLength:Option[Int],isResource:Option[Boolean])extends Answer {
  private def chain ={
    val res = isResource.getOrElse(false)
    if(res)
      decode[DiscreteMarkovChain[String]](Resource.getAsString(pathToMarkovChain)).toTry.get
    else
      decode[DiscreteMarkovChain[String]](File(pathToMarkovChain).contentAsString).toTry.get
  }
  private val answer = SimpleMarkovChainAnswer(chain,targetLength)
  override def get(text:String): String = answer.get(text)
}

case class RandText(options: ArraySeq[String],prefix:Option[String] = None,postfix:Option[String] = None) extends Answer {
  def get(text:String): String = prefix.getOrElse("")+options(Random.nextInt(options.length))+postfix.getOrElse("")
}

case class AnswerCombination(variants: ArraySeq[Answer]) extends Answer {
  override def get(text:String): String = variants(Random.nextInt(variants.length)).get(text)

  override def equals(obj: Any): Boolean = obj match {
    case AnswerCombination(variantsOther) => variants.equals(variantsOther)
    case _ => false
  }
  override def hashCode(): Int = variants.hashCode()
}

case class AnswerFromExternalFile(path: String,isResource:Option[Boolean])(implicit dec:Decoder[Answer])  extends Answer {
  val answer: Answer = {
    if(isResource.getOrElse(false))
      decode[Answer](Resource.getAsString(path))(dec).toTry.get
    else
      decode[Answer](File(path).contentAsString)(dec).toTry.get
  }
  override def get(text:String): String = answer.get(text)
}

case class AnswerFromAppCall(command:Seq[String]) extends Answer{
  override def get(text:String): String = command.!!
}

case class AnswerFromAppWithText(commandQuery:Seq[String]) extends Answer{
  override def get(text:String): String = commandQuery.appended(text).!!
}
