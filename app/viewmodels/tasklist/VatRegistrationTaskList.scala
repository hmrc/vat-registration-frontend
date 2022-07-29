/*
 * Copyright 2022 HM Revenue & Customs
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
import models.api.vatapplication.{OverseasCompliance, StoringWithinUk}
import models.api.{NETP, NonUkNonEstablished, VatScheme}
import models.{CurrentProfile, NonUk}
import play.api.i18n.Messages
import play.api.mvc.Request

import javax.inject.{Inject, Singleton}

@Singleton
class VatRegistrationTaskList @Inject()(aboutTheBusinessTaskList: AboutTheBusinessTaskList) {

  def buildGoodsAndServicesRow(implicit profile: CurrentProfile): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.vatRegistration.goodsAndServices",
    url = scheme => {
      scheme.partyType match {
        case Some(NETP) | Some(NonUkNonEstablished) => controllers.vatapplication.routes.TurnoverEstimateController.show.url
        case _ => controllers.vatapplication.routes.ImportsOrExportsController.show.url
      }
    },
    tagId = "goodsAndServicesRow",
    checks = scheme => {
      checkImportsAndExports(scheme).++ {
        Seq(
          scheme.vatApplication.exists(_.turnoverEstimate.isDefined),
          scheme.vatApplication.exists(_.zeroRatedSupplies.isDefined),
          scheme.vatApplication.exists(_.northernIrelandProtocol.exists(_.goodsToEU.isDefined)),
          scheme.vatApplication.exists(_.northernIrelandProtocol.exists(_.goodsFromEU.isDefined)),
          scheme.vatApplication.exists(_.claimVatRefunds.isDefined)
        )
      }.++ {
        scheme.eligibilitySubmissionData.map(_.registrationReason) match {
          case Some(NonUk) => scheme.vatApplication.flatMap(_.overseasCompliance).map(checkOverseasCompliance).getOrElse(Nil)
          case _ => Nil
        }
      }
    },
    prerequisites = _ => Seq(aboutTheBusinessTaskList.otherBusinessInvolvementsRow)
  )

  def bankAccountDetailsRow(implicit profile: CurrentProfile): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.vatRegistration.bankAccountDetails",
    url = _ => controllers.bankdetails.routes.HasBankAccountController.show.url,
    tagId = "bankAccountDetailsRow",
    checks = scheme => {
      Seq(scheme.bankAccount.isDefined)
        .++ {
          if (scheme.bankAccount.exists(_.isProvided)) {
            Seq(scheme.bankAccount.exists(_.details.isDefined))
          } else {
            Seq(scheme.bankAccount.exists(_.reason.isDefined))
          }
        }
    },
    prerequisites = _ => Seq(buildGoodsAndServicesRow)
  )

  def build(vatScheme: VatScheme)
           (implicit request: Request[_],
            profile: CurrentProfile,
            messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection = {

    TaskListSection(
      heading = messages("tasklist.vatRegistration.heading"),
      rows = Seq(
        Some(buildGoodsAndServicesRow.build(vatScheme)),
        if (Seq(NETP, NonUkNonEstablished).exists(vatScheme.partyType.contains)) None else Some(bankAccountDetailsRow.build(vatScheme))
      ).flatten
    )
  }

  private def checkImportsAndExports(vatScheme: VatScheme) = {
    vatScheme.partyType match {
      case Some(NETP) | Some(NonUkNonEstablished) => Nil
      case _ => {
        Seq(vatScheme.vatApplication.exists(_.tradeVatGoodsOutsideUk.isDefined))
      }.++ {
        if (vatScheme.vatApplication.exists(_.tradeVatGoodsOutsideUk.contains(true))) {
          Seq(vatScheme.vatApplication.exists(_.eoriRequested.isDefined))
        } else {
          Nil
        }
      }
    }
  }

  private def checkOverseasCompliance(overseasCompliance: OverseasCompliance) = {
    Seq(
      overseasCompliance.goodsToOverseas.isDefined,
      overseasCompliance.storingGoodsForDispatch.isDefined
    ). ++ {
      if (overseasCompliance.goodsToOverseas.contains(true)) Seq(overseasCompliance.goodsToEu.isDefined) else Nil
    }. ++ {
      if (overseasCompliance.storingGoodsForDispatch.contains(StoringWithinUk)) {
        Seq(overseasCompliance.usingWarehouse.isDefined).++ {
          if (overseasCompliance.usingWarehouse.contains(true)) {
            Seq(
              overseasCompliance.fulfilmentWarehouseNumber.isDefined,
              overseasCompliance.fulfilmentWarehouseName.isDefined
            )
          } else {
            Nil
          }
        }
      } else {
        Nil
      }
    }
  }
}
