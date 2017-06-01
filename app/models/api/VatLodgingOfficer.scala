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

import play.api.libs.json.{Json, OFormat}

case class VatLodgingOfficer(
                              currentAddress: ScrsAddress,
                              dob: DateOfBirth,
                              nino: String,
                              role: String,
                              name: Name,
                              formerName: FormerName,
                              contact: OfficerContactDetails
                            )

object VatLodgingOfficer {
  implicit val format: OFormat[VatLodgingOfficer] = Json.format[VatLodgingOfficer]

  // TODO remove once no longer required
  val empty = VatLodgingOfficer(
    currentAddress = ScrsAddress(line1 = "", line2 = ""),
    dob = DateOfBirth(1, 1, 1980),
    nino = "NB686868C",
    role = "",
    name = Name.empty,
    formerName = FormerName(selection = false, formerName = None),
    contact = OfficerContactDetails.empty)
}
