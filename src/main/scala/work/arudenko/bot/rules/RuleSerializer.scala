package work.arudenko.bot.rules

import cats.syntax.functor._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import cats.kernel.Semigroup
import cats.syntax.functor._
import scala.util.matching.Regex


object RuleSerializer {

  implicit val regexEncoder:Encoder[Regex] = Encoder[String].contramap[Regex](regex=>regex.regex)
  implicit val regexDecoder:Decoder[Regex] = Decoder[String].map(s=>new Regex(s))


  implicit val encodeAnswer: Encoder[Answer] = Encoder.instance {
    case a: RandText => a.asJson
    case a: AnswerCombination => a.asJson
    case a: AnswerFromExternalFile => a.asJson
    case a: SimpleMarkovChainAnswer => a.asJson
    case a: MarkovChainAnswer => a.asJson
    case a: AnswerFromAppCall => a.asJson
    case a: AnswerFromAppWithText => a.asJson
  }

  implicit val decodeAnswer: Decoder[Answer] =
    Seq[Decoder[Answer]](
      Decoder[RandText].widen,
      Decoder[AnswerCombination].widen,
      Decoder[AnswerFromExternalFile].widen,
      Decoder[SimpleMarkovChainAnswer].widen,
      Decoder[MarkovChainAnswer].widen,
      Decoder[AnswerFromAppCall].widen,
      Decoder[AnswerFromAppWithText].widen
    ).reduceLeft(_ or _)

  implicit val encodeRule: Encoder[Rule] = Encoder.instance {
    case regexRule: RegexRule => regexRule.asJson
    case substringRule: SubstringRule => substringRule.asJson
    case textRule: TextRule => textRule.asJson
  }


  implicit val decoderSub: Decoder[SubstringRule] = (c: HCursor) => {
    for {
      patterns <- c.downField("matchStrings").as[Seq[String]]
      answer <- c.downField("answer").as[Answer]
      ignoreCase <- c.downField("ignoreCase").as[Option[Boolean]]
    } yield SubstringRule(patterns,answer,ignoreCase.getOrElse(true))
  }

  implicit val decoderText: Decoder[TextRule] = (c: HCursor) => {
    for {
      patterns <- c.downField("matchTextExact").as[Seq[String]]
      answer <- c.downField("answer").as[Answer]
      ignoreCase <- c.downField("ignoreCase").as[Option[Boolean]]
    } yield TextRule(patterns,answer,ignoreCase.getOrElse(true))
  }


  implicit val decodeRule: Decoder[Rule] =
    List[Decoder[Rule]](
      Decoder[RegexRule].widen,
      Decoder[SubstringRule].widen,
      Decoder[TextRule].widen
    ).reduceLeft(_ or _)



}
