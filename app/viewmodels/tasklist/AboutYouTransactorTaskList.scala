/*
 * Copyright 2024 HM Revenue & Customs
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

package viewmodels.tasklist


import config.FrontendAppConfig
import models.CurrentProfile
import models.api.{NETP, NonUkNonEstablished, VatScheme}
import play.api.i18n.Messages
import uk.gov.hmrc.http.InternalServerException


object AboutYouTransactorTaskList {

  def build(vatScheme: VatScheme)
           (implicit profile: CurrentProfile,
            messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection = {

    val isTransactor = vatScheme.eligibilitySubmissionData.exists(_.isTransactor)
    val isAgent = isTransactor && profile.agentReferenceNumber.nonEmpty

    TaskListSection(
      heading = messages("tasklist.aboutYou.heading"),
      rows = Seq(
        Some(transactorPersonalDetailsRow.build(vatScheme)),
        if (isAgent) None else Some(transactorAddressDetailsRow.build(vatScheme)),
        Some(transactorContactDetailsRow.build(vatScheme))
      ).flatten
    )
  }

  def transactorPersonalDetailsRow(implicit profile: CurrentProfile, appConfig: FrontendAppConfig): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.aboutYou.personalDetails",
    url = _ => _ => {
      if (profile.agentReferenceNumber.isDefined) {
        controllers.transactor.routes.AgentNameController.show.url
      } else {
        controllers.transactor.routes.PartOfOrganisationController.show.url
      }
    },
    tagId = "transactorPersonalDetailsRow",
    checks = scheme => {
      if (profile.agentReferenceNumber.isDefined) {
        Seq(
          scheme.transactorDetails.exists(_.personalDetails.isDefined),
          scheme.transactorDetails.exists(_.declarationCapacity.isDefined)
        )
      }
      else {
        Seq(
          scheme.transactorDetails.exists(_.personalDetails.isDefined),
          scheme.transactorDetails.exists(_.isPartOfOrganisation.exists(answer =>
            if (answer) {
              scheme.transactorDetails.exists(_.organisationName.isDefined)
            } else {
              true
            }
          )),
          scheme.transactorDetails.exists(_.declarationCapacity.isDefined)
        )
      }
    },
    prerequisites = scheme => Seq(RegistrationReasonTaskList.registrationReasonRow(scheme.registrationId))
  )

  def transactorAddressDetailsRow(implicit profile: CurrentProfile, appConfig: FrontendAppConfig): TaskListRowBuilder = {
    TaskListRowBuilder(
      messageKey = _ => "tasklist.aboutYou.addressDetails",
      url = vatScheme => _ => resolveAddressRowUrl(vatScheme),
      tagId = "addressDetailsRow",
      checks = scheme => {
        if (scheme.transactorDetails.flatMap(_.personalDetails).exists(_.arn.isDefined)) {
          Seq(true)
        } else {
          Seq(scheme.transactorDetails.exists(_.address.isDefined))
        }
      },
      prerequisites = _ => Seq(transactorPersonalDetailsRow)
    )
  }

  def transactorContactDetailsRow(implicit profile: CurrentProfile, appConfig: FrontendAppConfig): TaskListRowBuilder = {
    TaskListRowBuilder(
      messageKey = _ => "tasklist.aboutYou.contactDetails",
      url = _ => _ => controllers.transactor.routes.TelephoneNumberController.show.url,
      tagId = "contactDetailsRow",
      checks = scheme => {
        Seq(
          scheme.transactorDetails.exists(_.email.isDefined),
          scheme.transactorDetails.exists(_.emailVerified.getOrElse(false)),
          scheme.transactorDetails.exists(_.telephone.isDefined)
        )
      },
      prerequisites = _ => Seq(transactorAddressDetailsRow)
    )
  }

  def resolveAddressRowUrl(vatScheme: VatScheme): String = {
    vatScheme.partyType match {
      case Some(NETP) | Some(NonUkNonEstablished) if vatScheme.eligibilitySubmissionData.exists(!_.fixedEstablishmentInManOrUk) =>
        controllers.transactor.routes.TransactorInternationalAddressController.show.url
      case Some(_) =>
        controllers.transactor.routes.TransactorHomeAddressController.redirectToAlf.url
      case None =>
        throw new InternalServerException("[AboutYouTaskList][resolveAddressRowUrl] Failed to initiate address details task list due to missing party type")
    }
  }
}