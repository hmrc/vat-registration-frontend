/*
 * Copyright 2020 HM Revenue & Customs
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

package models.view.test

import models.api.Country
import play.api.libs.json.Json

case class VatContactTestSetup( email: Option[String],
                                daytimePhone: Option[String],
                                mobile: Option[String],
                                website: Option[String],
                                line1: Option[String],
                                line2: Option[String],
                                line3: Option[String],
                                line4: Option[String],
                                postcode: Option[String] = None,
                                country: Option[Country] = None
                              )

object VatContactTestSetup {
  implicit val format = Json.format[VatContactTestSetup]
}
