/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.partners

import config.FrontendAppConfig
import controllers.partners.PartnerIndexValidation.minPartnerIndex
import models.{CurrentProfile, Entity}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Result}
import services.EntityService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait PartnerIndexValidation {
  val entityService: EntityService

  def validateIndex(index: Int, fallBack: Int => Call, minIndex: Int = minPartnerIndex)(function: Option[Entity] => Future[Result])
                   (implicit appConfig: FrontendAppConfig, hc: HeaderCarrier, profile: CurrentProfile, executionContext: ExecutionContext): Future[Result] = {
    if (index > appConfig.maxPartnerCount) {
      Future.successful(Redirect(fallBack(appConfig.maxPartnerCount)))
    } else if (index < minIndex) {
      Future.successful(Redirect(fallBack(minIndex)))
    } else {
      entityService.getAllEntities(profile.registrationId).flatMap {
        case Nil =>
          Future.successful(Redirect(controllers.routes.TaskListController.show))
        case list if index > list.length + 1 =>
          Future.successful(Redirect(fallBack(list.length + 1)))
        case list =>
          function(list.lift(index - 1))
      }
    }
  }

  def validateIndexSubmit(index: Int, fallBack: Int => Call, minIndex: Int = minPartnerIndex)(function: Future[Result])
                         (implicit appConfig: FrontendAppConfig): Future[Result] = {
    if (index > appConfig.maxPartnerCount) {
      Future.successful(Redirect(fallBack(appConfig.maxPartnerCount)))
    } else if (index < minIndex) {
      Future.successful(Redirect(fallBack(minIndex)))
    } else {
      function
    }
  }

}

object PartnerIndexValidation {
  val minPartnerIndex = 2
}