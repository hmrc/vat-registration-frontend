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

import com.google.inject.ImplementedBy
import models.api.{VatDetails, VatRegistration => VatRegistrationAPI}
import models.view.{Summary, SummaryRow, SummarySection}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[VatRegistrationService])
trait RegistrationService {

  def getRegistrationSummary()(implicit executionContext: ExecutionContext): Future[Option[Summary]]

}

class VatRegistrationService extends RegistrationService {

  override def getRegistrationSummary()(implicit ec: ExecutionContext): Future[Option[Summary]] = {
    Future.successful(
      Option(
        registrationToSummary(
          new VatRegistrationAPI("VAT123456", "2017-01-11T15:10:12",
            new VatDetails(Option("No"), Option("Yes"), Option("1 February 2017")))
        )
      )
    )
  }

  def registrationToSummary(apiModel: VatRegistrationAPI): Summary = {
    Summary(
      Seq(SummarySection(
        id = "vatDetails",
        Seq(SummaryRow(
          id = "vatDetails.taxableTurnover",
          answer = apiModel.vatDetails.taxableTurnover match {
            case Some(name) => Right(name)
          },
          changeLink = Some(controllers.userJourney.routes.VoluntaryRegistrationController.show())
        ),
          SummaryRow(
            id = "vatDetails.registerVoluntarily",
            answer = apiModel.vatDetails.registerVoluntarily match {
              case Some(name) => Right(name)
            },
            changeLink = Some(controllers.userJourney.routes.SummaryController.show())
          ),
          SummaryRow(
            id = "vatDetails.startDate",
            answer = apiModel.vatDetails.startDate match {
              case Some(name) => Right(name)
            },
            changeLink = Some(controllers.userJourney.routes.StartDateController.show())
          )
        )
      ))
    )
  }
}


