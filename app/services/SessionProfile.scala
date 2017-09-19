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

package services

import connectors.KeystoreConnector
import models.CurrentProfile
import play.api.mvc.{Request, Result}
import play.api.mvc.Results.{Redirect, Conflict}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait SessionProfile {

  val keystoreConnector: KeystoreConnector

  private val CURRENT_PROFILE_KEY = "CurrentProfile"

  def withCurrentProfile(f: CurrentProfile => Future[Result])(implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {
    keystoreConnector.fetchAndGet[CurrentProfile](CURRENT_PROFILE_KEY) flatMap {
      case Some(profile) => f(profile)
      case None          => Future.successful(Conflict)
    }
  }
}
