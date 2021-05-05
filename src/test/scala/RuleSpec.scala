import better.files.Resource
import io.circe.parser._
import org.scalatest.flatspec._
import org.scalatest.matchers._
import work.arudenko.bot.rules._
import work.arudenko.bot.rules.RuleSerializer._

class RuleSpec  extends AnyFlatSpec with should.Matchers {

  val decoded: List[Rule] = decode[List[Rule]](Resource.getAsString("answers.json")).toTry.get


  "substring rule" should "match text containing specified substring" in{
    val rule= decoded.head
    rule.matches("this text has 'one' in it") shouldBe true
    rule.matches("this text does not") shouldBe false
  }

  "regex rule" should "match text matching regex" in{
    val rule = decoded.tail.head
    rule.matches("I am a robot") shouldBe true
    rule.matches("androids will rule the earth") shouldBe true
    rule.matches("this bot is cool") shouldBe true
    rule.matches("word chatbot shouldn't match and") shouldBe false
    rule.matches("but chat-bot") shouldBe true
  }

  "exact text rule" should "match text fully" in{
    val rule = decoded.last
    rule.matches("yes") shouldBe true
    rule.matches("anything that is not the exact match with yes") shouldBe false
    rule.matches("yesyesyes") shouldBe false
    rule.matches("Yes") shouldBe false //because we have set ignoreCase to false
  }

}
