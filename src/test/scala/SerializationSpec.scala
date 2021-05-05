import better.files.Resource
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.log4s._
import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers._
import pureconfig.ConfigSource
import work.arudenko.bot.rules._
import work.arudenko.bot.BotConfig

import scala.collection.immutable.ArraySeq
import scala.util.matching.Regex

class SerializationSpec  extends AnyFlatSpec with should.Matchers {
  private[this] val logger = getLogger
  val regexMatch = new Regex(""".*""")
  val answer1: Answer = RandText(ArraySeq("this", "that"))
  val answer2: Answer = RandText(ArraySeq("this3", "that3"))
  val rule:Rule = RegexRule(regexMatch::Nil,answer1+answer2)
  val rule2:Rule = SubstringRule("t1"::"t2"::Nil,answer1)
  val answerList = List(rule,rule2)

  val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)
  val exampleStr = "[{\"matchPatterns\":[\".*\"],\"answer\":{\"variants\":[{\"options\":[\"this\",\"that\"]},{\"options\":[\"this3\",\"that3\"]}]}},{\"matchStrings\":[\"t1\",\"t2\"],\"answer\":{\"options\":[\"this\",\"that\"]},\"ignoreCase\":true}]"


  import work.arudenko.bot.rules.RuleSerializer._

  "Rule list" should "serialize into json string" in {
    val serialized = answerList.asJson
    printer.print(serialized) should be (exampleStr)
  }

  "rule deserializer" should "be able to parse example json string" in {
    val decoded = decode[List[Rule]](exampleStr).toTry.get
    decoded.toString() should be  (answerList.toString())
  }

  it  should "be able to deserialize json file with simple rules" in {
    val decoded = decode[List[Rule]](Resource.getAsString("answers.json")).toTry.get
    logger.info(decoded.toString())
  }

  it should "be able to deserialize json file with external rule source" in {
    val decoded = decode[List[Rule]](Resource.getAsString("answers_resource.json")).toTry.get
    logger.info(decoded.toString())
  }

  it should "be able to deserialize file with markov chain rule" in {
    val decoded = decode[List[Rule]](Resource.getAsString("answers_markov.json")).toTry.get
    logger.info(decoded.toString())
  }

  it should "be able to deserialize file with call to external program" in {
    val decoded = decode[List[Rule]](Resource.getAsString("answers_external.json")).toTry.get
    logger.info(decoded.toString())
  }

  "bot configuration" should "load without errors" in{
    import pureconfig.generic.auto._
    ConfigSource.default.loadOrThrow[BotConfig]
  }

}
