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

import models.S4LKey
import play.api.libs.json._
import utils.JsonUtilities

import java.time.LocalDate

case class Returns(zeroRatedSupplies: Option[BigDecimal] = None,
                   reclaimVatOnMostReturns: Option[Boolean] = None,
                   returnsFrequency: Option[ReturnsFrequency] = None,
                   staggerStart: Option[Stagger] = None,
                   startDate: Option[LocalDate] = None,
                   annualAccountingDetails: Option[AASDetails] = None)

object Returns extends JsonUtilities {
  implicit val s4lKey: S4LKey[Returns] = S4LKey("returns")
  implicit val format: Format[Returns] = Json.format[Returns]
}

case class AASDetails(paymentFrequency: Option[PaymentFrequency] = None,
                      paymentMethod: Option[PaymentMethod] = None)

object AASDetails {
  implicit val format: Format[AASDetails] = Json.format[AASDetails]
}
