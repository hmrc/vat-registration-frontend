/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.api.returns

import play.api.libs.json._

sealed trait Stagger

case object MonthlyStagger extends Stagger

sealed trait QuarterlyStagger extends Stagger
case object JanuaryStagger extends QuarterlyStagger
case object FebruaryStagger extends QuarterlyStagger
case object MarchStagger extends QuarterlyStagger

sealed trait AnnualStagger extends Stagger
case object JanDecStagger extends AnnualStagger
case object FebJanStagger extends AnnualStagger
case object MarFebStagger extends AnnualStagger
case object AprMarStagger extends AnnualStagger
case object MayAprStagger extends AnnualStagger
case object JunMayStagger extends AnnualStagger
case object JulJunStagger extends AnnualStagger
case object AugJulStagger extends AnnualStagger
case object SepAugStagger extends AnnualStagger
case object OctSepStagger extends AnnualStagger
case object NovOctStagger extends AnnualStagger
case object DecNovStagger extends AnnualStagger

object Stagger {

  val monthlyStagger: String = "MM"

  val janStagger: String = "MA"
  val febStagger: String = "MB"
  val marStagger: String = "MC"

  val janDecStagger: String = "YA"
  val febJanStagger: String = "YB"
  val marFebStagger: String = "YC"
  val aprMarStagger: String = "YD"
  val mayAprStagger: String = "YE"
  val junMayStagger: String = "YF"
  val julJunStagger: String = "YG"
  val augJulStagger: String = "YH"
  val sepAugStagger: String = "YI"
  val octSepStagger: String = "YJ"
  val novOctStagger: String = "YK"
  val decNovStagger: String = "YL"

  val reads: Reads[Stagger] = Reads[Stagger] {
    case JsString(`monthlyStagger`) => JsSuccess(MonthlyStagger)
    case JsString(`janStagger`) => JsSuccess(JanuaryStagger)
    case JsString(`febStagger`) => JsSuccess(FebruaryStagger)
    case JsString(`marStagger`) => JsSuccess(MarchStagger)
    case JsString(`janDecStagger`) => JsSuccess(JanDecStagger)
    case JsString(`febJanStagger`) => JsSuccess(FebJanStagger)
    case JsString(`marFebStagger`) => JsSuccess(MarFebStagger)
    case JsString(`aprMarStagger`) => JsSuccess(AprMarStagger)
    case JsString(`mayAprStagger`) => JsSuccess(MayAprStagger)
    case JsString(`junMayStagger`) => JsSuccess(JunMayStagger)
    case JsString(`julJunStagger`) => JsSuccess(JulJunStagger)
    case JsString(`augJulStagger`) => JsSuccess(AugJulStagger)
    case JsString(`sepAugStagger`) => JsSuccess(SepAugStagger)
    case JsString(`octSepStagger`) => JsSuccess(OctSepStagger)
    case JsString(`novOctStagger`) => JsSuccess(NovOctStagger)
    case JsString(`decNovStagger`) => JsSuccess(DecNovStagger)
    case _ => JsError("Could not parse Stagger")
  }

  val writes: Writes[Stagger] = Writes[Stagger] {
    case MonthlyStagger => JsString(monthlyStagger)
    case JanuaryStagger => JsString(janStagger)
    case FebruaryStagger => JsString(febStagger)
    case MarchStagger => JsString(marStagger)
    case JanDecStagger => JsString(janDecStagger)
    case FebJanStagger => JsString(febJanStagger)
    case MarFebStagger => JsString(marFebStagger)
    case AprMarStagger => JsString(aprMarStagger)
    case MayAprStagger => JsString(mayAprStagger)
    case JunMayStagger => JsString(junMayStagger)
    case JulJunStagger => JsString(julJunStagger)
    case AugJulStagger => JsString(augJulStagger)
    case SepAugStagger => JsString(sepAugStagger)
    case OctSepStagger => JsString(octSepStagger)
    case NovOctStagger => JsString(novOctStagger)
    case DecNovStagger => JsString(decNovStagger)
  }

  implicit val format: Format[Stagger] = Format(reads, writes)
}
