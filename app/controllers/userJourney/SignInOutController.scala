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

package controllers.userJourney

import javax.inject.Inject

import auth.VATRegime
import config.FrontendAuthConnector
import controllers.{CommonPlayDependencies, VatRegistrationController}
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.Future

class SignInOutController @Inject()(ds: CommonPlayDependencies) extends VatRegistrationController(ds) {
  //$COVERAGE-OFF$
  override val authConnector = FrontendAuthConnector
  //$COVERAGE-ON$

  def postSignIn: Action[AnyContent] = AuthorisedFor(taxRegime = new VATRegime, pageVisibility = GGConfidence).async {
    implicit user =>

      implicit request =>
        Future.successful(Redirect(controllers.userJourney.routes.WelcomeController.show()))
  }

}

//trait SignInOutController extends FrontendController with Actions {


//  def postSignIn: Action[AnyContent] = AuthorisedFor(taxRegime = new VATRegime, pageVisibility = GGConfidence).async {
//    implicit user =>
//      implicit request =>
//        checkAndStoreCurrentProfile {
//          checkAndStoreCompanyDetails {
//            fetchAndStorePAYERegistration {
//              Redirect(controllers.userJourney.routes.WelcomeController.show())
//            }
//          }
//        }
//  }
//
//  private def checkAndStoreCurrentProfile(f: => Future[Result])(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[Result] = {
//    currentProfileService.fetchAndStoreCurrentProfile flatMap {
//      case DownstreamOutcome.Success => f
//      case DownstreamOutcome.Failure => Future.successful(InternalServerError(views.html.pages.error.restart()))
//    }
//  }
//
//  private def checkAndStoreCompanyDetails(f: => Future[Result])(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[Result] = {
//    coHoAPIService.fetchAndStoreCoHoCompanyDetails flatMap {
//      case DownstreamOutcome.Success => f
//      case DownstreamOutcome.Failure => Future.successful(InternalServerError(views.html.pages.error.restart()))
//    }
//  }
//
//  private def fetchAndStorePAYERegistration(f: => Result)(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[Result] = {
//    payeRegistrationService.fetchAndStoreCurrentRegistration() flatMap {
//      case regOpt => regOpt match {
//        case Some(reg) => Future.successful(f)
//        case _ => payeRegistrationService.createNewRegistration() map {
//          case DownstreamOutcome.Success => f
//          case DownstreamOutcome.Failure => InternalServerError(views.html.pages.error.restart())
//        }
//      }
//    } recover {
//      case e =>
//        Logger.warn(s"[SignInOutController] [fetchAndStorePAYERegistration] Unable to fetch/store current registration. Error: ${e.getMessage}")
//        InternalServerError(views.html.pages.error.restart())
//    }
//  }
//}
