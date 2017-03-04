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

package models.view

import enums.CacheKeys
import models.api.VatChoice.{NECESSITY_OBLIGATORY, NECESSITY_VOLUNTARY}
import models.api.{VatChoice, VatScheme}
import models.{ApiModelTransformer, CacheKey, ViewModelTransformer}
import play.api.libs.json.Json

case class VoluntaryRegistration(yesNo: String = "")

object VoluntaryRegistration {

  val REGISTER_YES = "REGISTER_YES"
  val REGISTER_NO = "REGISTER_NO"

  implicit val format = Json.format[VoluntaryRegistration]

  implicit val modelTransformer = ApiModelTransformer { vs: VatScheme =>
    vs.vatChoice.map(_.necessity).collect {
      case NECESSITY_VOLUNTARY => VoluntaryRegistration(REGISTER_YES)
    }.getOrElse(VoluntaryRegistration())
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: VoluntaryRegistration, g: VatChoice) =>
    g.copy(necessity = if (REGISTER_YES == c.yesNo) NECESSITY_VOLUNTARY else NECESSITY_OBLIGATORY)
  }

  implicit val cacheKeyProvider = CacheKey[VoluntaryRegistration](CacheKeys.VoluntaryRegistration.toString)

}