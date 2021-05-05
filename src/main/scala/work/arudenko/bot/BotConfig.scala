package work.arudenko.bot

import better.files._
import java.io.{File => JFile}

sealed trait RuleSource{
  def getRules:String
}

case class RuleResource(path: String) extends RuleSource {
  override def getRules: String = Resource.getAsString(path)
}
case class RuleFile(path: String) extends RuleSource {
  override def getRules: String = File(path).contentAsString
}

case class BotConfig(token:String, rules:RuleSource)
