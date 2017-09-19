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

import scala.concurrent.ExecutionContext.Implicits.global

package services {

  import common.ErrorUtil.fail
  import models.{CurrentProfile, S4LVatFinancials}
  import models.api.{VatFinancials, VatScheme}
  import uk.gov.hmrc.play.http.HeaderCarrier

  import scala.concurrent.Future

  trait FinancialsService extends CommonService {

    self: RegistrationService =>

    def submitVatFinancials()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[VatFinancials] = {
      def merge(fresh: Option[S4LVatFinancials], vs: VatScheme): VatFinancials =
        fresh.fold(
          vs.financials.getOrElse(throw fail("VatFinancials"))
        ) (s4l => S4LVatFinancials.apiT.toApi(s4l))

      for {
        vs <- getVatScheme()
        vf <- s4l[S4LVatFinancials]
        response <- vatRegConnector.upsertVatFinancials(profile.registrationId, merge(vf, vs))
      } yield response
    }

  }
}

package connectors {

  import cats.instances.FutureInstances
  import models.api.VatFinancials
  import uk.gov.hmrc.play.http.{HeaderCarrier, HttpReads}

  import scala.concurrent.Future

  trait FinancialsConnector extends FutureInstances {
    self: RegistrationConnector =>

    def upsertVatFinancials(regId: String, vatFinancials: VatFinancials)
                           (implicit hc: HeaderCarrier, rds: HttpReads[VatFinancials]): Future[VatFinancials] =
      http.PATCH[VatFinancials, VatFinancials](s"$vatRegUrl/vatreg/$regId/vat-financials", vatFinancials).recover{
        case e: Exception => throw logResponse(e, className, "upsertVatFinancials")
      }
  }
}
