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

package forms

import models.api.returns._
import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

object AnnualStaggerForm {

  val annualStagger: String = "value"
  val annualStaggerError: String = "last-month-of-accounting-year.error.required"

  val januaryKey = "january"
  val februaryKey = "february"
  val marchKey = "march"
  val aprilKey = "april"
  val mayKey = "may"
  val juneKey = "june"
  val julyKey = "july"
  val augustKey = "august"
  val septemberKey = "september"
  val octoberKey = "october"
  val novemberKey = "november"
  val decemberKey = "december"

  def formatter: Formatter[AnnualStagger] = new Formatter[AnnualStagger] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], AnnualStagger] = {
      data.get(key) match {
        case Some(`januaryKey`) => Right(FebJanStagger)
        case Some(`februaryKey`) => Right(MarFebStagger)
        case Some(`marchKey`) => Right(AprMarStagger)
        case Some(`aprilKey`) => Right(MayAprStagger)
        case Some(`mayKey`) => Right(JunMayStagger)
        case Some(`juneKey`) => Right(JulJunStagger)
        case Some(`julyKey`) => Right(AugJulStagger)
        case Some(`augustKey`) => Right(SepAugStagger)
        case Some(`septemberKey`) => Right(OctSepStagger)
        case Some(`octoberKey`) => Right(NovOctStagger)
        case Some(`novemberKey`) => Right(DecNovStagger)
        case Some(`decemberKey`) => Right(JanDecStagger)
        case _ => Left(Seq(FormError(key, annualStaggerError)))
      }
    }

    override def unbind(key: String, value: AnnualStagger): Map[String, String] = {
      val stringValue = value match {
        case FebJanStagger => januaryKey
        case MarFebStagger => februaryKey
        case AprMarStagger => marchKey
        case MayAprStagger => aprilKey
        case JunMayStagger => mayKey
        case JulJunStagger => juneKey
        case AugJulStagger => julyKey
        case SepAugStagger => augustKey
        case OctSepStagger => septemberKey
        case NovOctStagger => octoberKey
        case DecNovStagger => novemberKey
        case JanDecStagger => decemberKey
      }
      Map(key -> stringValue)
    }
  }

  def form: Form[AnnualStagger] = Form(
    single(
      annualStagger -> of(formatter)
    )
  )

}
