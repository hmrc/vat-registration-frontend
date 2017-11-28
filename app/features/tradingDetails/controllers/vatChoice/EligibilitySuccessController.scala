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

package controllers.vatTradingDetails.vatChoice

import javax.inject.{Inject, Singleton}

import cats.data.OptionT
import connectors.KeystoreConnect
import controllers.{CommonPlayDependencies, VatRegistrationController}
import models.ModelKeys._
import models.external.IncorporationInfo
import org.apache.commons.lang3.StringUtils
import play.api.mvc.{Action, AnyContent}
import services.SessionProfile
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

// TODO re-check why this is in trading details rather than vatEligibility
@Singleton
class EligibilitySuccessController @Inject()(ds: CommonPlayDependencies,
                                             val authConnector: AuthConnector,
                                             val keystoreConnector: KeystoreConnect) extends VatRegistrationController(ds) with SessionProfile {

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          Future.successful(Ok(views.html.pages.vatEligibility.eligible()))
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { _ =>
          fetchIncorporationInfo.subflatMap(_.statusEvent.crn).filter(StringUtils.isNotBlank)
            .fold(controllers.vatTradingDetails.vatChoice.routes.TaxableTurnoverController.show())(
              crn =>
                controllers.vatTradingDetails.vatChoice.routes.OverThresholdController.show()).map(Redirect)
        }
  }

  private def fetchIncorporationInfo()(implicit headerCarrier: HeaderCarrier) =
    OptionT(keystoreConnector.fetchAndGet[IncorporationInfo](INCORPORATION_STATUS))
}

