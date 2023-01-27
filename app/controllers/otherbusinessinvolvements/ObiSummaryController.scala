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

package controllers.otherbusinessinvolvements

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.otherbusinessinvolvements.ObiSummaryForm
import models.OtherBusinessInvolvement
import play.api.mvc.{Action, AnyContent}
import services.{OtherBusinessInvolvementsService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import viewmodels.ObiSummaryRow
import views.html.otherbusinessinvolvements.ObiSummary

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ObiSummaryController @Inject()(val authConnector: AuthConnector,
                                     val sessionService: SessionService,
                                     otherBusinessInvolvementsService: OtherBusinessInvolvementsService,
                                     view: ObiSummary)
                                    (implicit appConfig: FrontendAppConfig,
                                     val executionContext: ExecutionContext,
                                     baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        otherBusinessInvolvementsService.getOtherBusinessInvolvements.flatMap {
          case otherBusinessInvolvements if otherBusinessInvolvements.exists(_.isModelComplete) =>
            val clearedObiList = otherBusinessInvolvements.filter(_.isModelComplete)
            val page = Ok(view(ObiSummaryForm(), buildRows(clearedObiList), clearedObiList.size))

            if (clearedObiList != otherBusinessInvolvements) {
              otherBusinessInvolvementsService.upsertObiList(clearedObiList).map(_ => page)
            } else {
              Future.successful(page)
            }
          case _ =>
            Future.successful(Redirect(routes.OtherBusinessInvolvementController.show))
        }
  }


  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        ObiSummaryForm().bindFromRequest.fold(
          errors =>
            otherBusinessInvolvementsService.getOtherBusinessInvolvements.map {
              case Nil =>
                Redirect(routes.OtherBusinessInvolvementController.show)
              case otherBusinessInvolvements =>
                BadRequest(view(errors, buildRows(otherBusinessInvolvements), otherBusinessInvolvements.size))
            },
          addMore =>
            if (addMore) {
              otherBusinessInvolvementsService.getHighestValidIndex.map { nextIndex =>
                Redirect(routes.OtherBusinessNameController.show(nextIndex))
              }
            } else {
              Future.successful(Redirect(controllers.routes.TaskListController.show))
            }
        )
  }

  def continue: Action[AnyContent] = isAuthenticatedWithProfile { _ =>
    _ =>
      Future.successful(Redirect(controllers.routes.TaskListController.show))
  }

  private def buildRows(otherBusinessInvolvements: List[OtherBusinessInvolvement]): List[ObiSummaryRow] =
    otherBusinessInvolvements.zipWithIndex.map { case (obi, idx) =>
      val businessName = obi.businessName.getOrElse(throw new InternalServerException(s"Couldn't render OBI summary due to missing business name for index: $idx"))
      val indexFrom1 = idx + 1

      ObiSummaryRow(
        businessName = businessName,
        changeAction = routes.OtherBusinessNameController.show(indexFrom1),
        deleteAction = routes.RemoveOtherBusinessController.show(indexFrom1)
      )
    }

}
