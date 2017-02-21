/*
 * Copyright 2017 HM Revenue & Customs
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

package models.api

import play.api.libs.functional.syntax._
import play.api.libs.json._
import org.joda.time.DateTime

case class VatChoice(
                      startDate: DateTime,
                      necessity: String = "" // "obligatory" or "voluntary"
                    )

object VatChoice {

  val NECESSITY_OBLIGATORY = "obligatory"
  val NECESSITY_VOLUNTARY = "voluntary"

  implicit val format: OFormat[VatChoice] = (
    (__ \ "start-date").format[DateTime] and
      (__ \ "necessity").format[String]) (VatChoice.apply, unlift(VatChoice.unapply))

  def empty: VatChoice = VatChoice(DateTime.now)
}
