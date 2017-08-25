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

import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package services {

  import common.ErrorUtil.fail
  import models.S4LFlatRateScheme
  import models.api.{VatFlatRateScheme, VatScheme}

  trait FlatRateService extends CommonService {

    self: RegistrationService =>

    import cats.syntax.all._

    def submitVatFlatRateScheme()(implicit hc: HeaderCarrier): Future[VatFlatRateScheme] = {
      def merge(fresh: Option[S4LFlatRateScheme], vs: VatScheme): VatFlatRateScheme =
        fresh.fold(
          vs.vatFlatRateScheme.getOrElse(throw fail("VatFlatRateScheme"))
        )(s4l => S4LFlatRateScheme.apiT.toApi(s4l))

      for {
        (vs, frs) <- (getVatScheme() |@| s4l[S4LFlatRateScheme]()).tupled
        response <- vatRegConnector.upsertVatFlatRateScheme(vs.id, merge(frs, vs))
      } yield response
    }

  }
}

package connectors {

  import cats.instances.FutureInstances
  import models.api.VatFlatRateScheme
  import uk.gov.hmrc.play.http.HttpReads

  trait FlatRateConnector extends FutureInstances {
    self: RegistrationConnector =>

    // TODO - check why this is here twice? update vs upsert?
    def updateVatFlatRateScheme(regId: String, vatFlatRateScheme: VatFlatRateScheme)
                               (implicit hc: HeaderCarrier, rds: HttpReads[VatFlatRateScheme]): Future[VatFlatRateScheme] =
      http.PATCH[VatFlatRateScheme, VatFlatRateScheme](s"$vatRegUrl/vatreg/$regId/flat-rate-scheme", vatFlatRateScheme).recover{
        case e: Exception => throw logResponse(e, className, "vatFlatRateScheme")
      }

    def upsertVatFlatRateScheme(regId: String, vatFrs: VatFlatRateScheme)
                               (implicit hc: HeaderCarrier, rds: HttpReads[VatFlatRateScheme]): Future[VatFlatRateScheme] =
      http.PATCH[VatFlatRateScheme, VatFlatRateScheme](s"$vatRegUrl/vatreg/$regId/flat-rate-scheme", vatFrs).recover{
        case e: Exception => throw logResponse(e, className, "upsertVatFrsAnswers")
      }
  }
}
