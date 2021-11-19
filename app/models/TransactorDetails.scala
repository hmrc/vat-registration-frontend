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

package models

import models.api.Address
import play.api.libs.json.{Format, Json}

case class TransactorDetails(personalDetails: Option[PersonalDetails] = None,
                             isPartOfOrganisation: Option[Boolean] = None,
                             organisationName: Option[String] = None,
                             telephone: Option[String] = None,
                             email: Option[String] = None,
                             address: Option[Address] = None,
                             declarationCapacity: Option[DeclarationCapacityAnswer] = None)

object TransactorDetails {
  implicit val s4lKey: S4LKey[TransactorDetails] = S4LKey("transactor")
  implicit val apiKey: ApiKey[TransactorDetails] = ApiKey("transactor")

  implicit val format: Format[TransactorDetails] = Json.format[TransactorDetails]
}

case class DeclarationCapacityAnswer(role: DeclarationCapacity,
                                     otherRole: Option[String] = None)

object DeclarationCapacityAnswer {
  implicit val format: Format[DeclarationCapacityAnswer] = Json.format[DeclarationCapacityAnswer]
}