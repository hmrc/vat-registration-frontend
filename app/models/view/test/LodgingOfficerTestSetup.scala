/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.libs.json.Json

case class LodgingOfficerTestSetup(
                                  dobDay: Option[String] = None,
                                  dobMonth: Option[String] = None,
                                  dobYear: Option[String] = None,
                                  email: Option[String] = None,
                                  mobile: Option[String] = None,
                                  phone: Option[String] = None,
                                  formernameChoice: Option[String] = None,
                                  formername: Option[String] = None,
                                  formernameChangeDay: Option[String] = None,
                                  formernameChangeMonth: Option[String] = None,
                                  formernameChangeYear: Option[String] = None)

object LodgingOfficerTestSetup {
  implicit val format = Json.format[LodgingOfficerTestSetup]
}
