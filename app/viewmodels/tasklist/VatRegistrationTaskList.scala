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

package viewmodels.tasklist

import models._
import models.api.vatapplication.{AnnualStagger, OverseasCompliance, StoringWithinUk, VatApplication}
import models.api.{NETP, NonUkNonEstablished, PartyType, VatScheme}
import play.api.i18n.Messages

import javax.inject.{Inject, Singleton}

@Singleton
class VatRegistrationTaskList @Inject()(aboutTheBusinessTaskList: AboutTheBusinessTaskList) {

  def goodsAndServicesRow(implicit profile: CurrentProfile): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.vatRegistration.goodsAndServices",
    url = _ => controllers.vatapplication.routes.ImportsOrExportsController.show.url,
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
        scheme.partyType match {
          case Some(NETP) | Some(NonUkNonEstablished) =>
            scheme.vatApplication.flatMap(_.overseasCompliance).map(checkOverseasCompliance).getOrElse(Nil)
          case _ => Nil
        }
      }
    },
    prerequisites = _ =>
      Seq(aboutTheBusinessTaskList.otherBusinessInvolvementsRow)
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
    prerequisites = _ => Seq(goodsAndServicesRow)
  )

  def registrationDateRow(implicit profile: CurrentProfile): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.vatRegistration.registrationDate",
    url = _ => controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url,
    tagId = "vatRegistrationDateRow",
    checks = scheme => {
      Seq(scheme.vatApplication.exists(_.startDate.isDefined))
        .++ {
          if (scheme.eligibilitySubmissionData.exists(_.registrationReason.isVoluntary)) {
            Seq(scheme.vatApplication.exists(_.currentlyTrading.isDefined))
          } else {
            Nil
          }
        }
    },
    prerequisites = _ => Seq(bankAccountDetailsRow)
  )

  def vatReturnsRow(implicit profile: CurrentProfile): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.vatRegistration.vatReturns",
    url = _ => controllers.vatapplication.routes.ReturnsFrequencyController.show.url,
    tagId = "vatReturnsRow",
    checks = scheme => scheme.vatApplication.fold(Seq(false))(vatAppl => checkVatReturns(scheme.partyType, vatAppl)),
    prerequisites = scheme => Seq(
      List(
        resolveVATRegistrationDateRow(scheme),
        resolveBankDetailsRow(scheme)
      ).flatten.headOption.getOrElse(goodsAndServicesRow)
    )
  )

  def flatRateSchemeRow(implicit profile: CurrentProfile): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.vatRegistration.flatRateScheme",
    url = _ => controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url,
    tagId = "flatRateScheme",
    checks = scheme => {
      scheme.flatRateScheme match {
        case Some(FlatRateScheme(Some(true), Some(true), Some(_), Some(true), Some(true), Some(_), Some(_), Some(_), Some(false))) => List(true)
        case Some(FlatRateScheme(Some(true), Some(true), Some(_), Some(false), Some(true), Some(_), Some(_), Some(_), Some(true))) => List(true)
        case Some(FlatRateScheme(Some(true), Some(false), _, _, Some(true), Some(_), Some(_), Some(_), Some(true))) => List(true)
        case Some(FlatRateScheme(Some(true), Some(false), _, _, Some(false), _, _, _, _)) => List(true)
        case Some(FlatRateScheme(Some(true), _, _, _, _, _, _, _, _)) => List(true, false)
        case Some(FlatRateScheme(Some(false), _, _, _, _, _, _, _, _)) => List(true)
        case _ => List(false)
      }
    },
    prerequisites = _ => Seq(vatReturnsRow)
  )

  def build(vatScheme: VatScheme)(implicit profile: CurrentProfile, messages: Messages): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.vatRegistration.heading"),
      rows = Seq(
        Some(goodsAndServicesRow.build(vatScheme)),
        resolveBankDetailsRow(vatScheme).map(_.build(vatScheme)),
        resolveVATRegistrationDateRow(vatScheme).map(_.build(vatScheme)),
        Some(vatReturnsRow.build(vatScheme)),
        resolveFlatRateSchemeRow(vatScheme).map(_.build(vatScheme))
      ).flatten
    )

  private def checkImportsAndExports(vatScheme: VatScheme) = {
    {
      Seq(vatScheme.vatApplication.exists(_.tradeVatGoodsOutsideUk.isDefined))
    }.++ {
      if (vatScheme.vatApplication.exists(_.tradeVatGoodsOutsideUk.contains(true))) {
        Seq(vatScheme.vatApplication.exists(_.eoriRequested.isDefined))
      } else {
        Nil
      }
    }
  }

  private def resolveBankDetailsRow(vatScheme: VatScheme)(implicit profile: CurrentProfile) = {
    if (Seq(NETP, NonUkNonEstablished).exists(vatScheme.partyType.contains)) None else Some(bankAccountDetailsRow)
  }

  private[tasklist] def resolveFlatRateSchemeRow(vatScheme: VatScheme)(implicit profile: CurrentProfile) = {
    if (vatScheme.vatApplication.flatMap(_.turnoverEstimate).exists(_ > 150000L) ||
      vatScheme.eligibilitySubmissionData.map(_.registrationReason).exists(GroupRegistration.equals)
    ) {
      None
    } else {
      Some(flatRateSchemeRow)
    }
  }

  private[tasklist] def resolveVATRegistrationDateRow(vatScheme: VatScheme)(implicit profile: CurrentProfile) = {
    vatScheme.registrationReason match {
      case Some(ForwardLook) | Some(BackwardLook) | Some(GroupRegistration) | Some(Voluntary) | Some(IntendingTrader) | Some(SuppliesOutsideUk) =>
        Some(registrationDateRow)
      case _ =>
        None
    }
  }

  private def checkOverseasCompliance(overseasCompliance: OverseasCompliance) = {
    Seq(
      overseasCompliance.goodsToOverseas.isDefined,
      overseasCompliance.storingGoodsForDispatch.isDefined
    ).++ {
      if (overseasCompliance.goodsToOverseas.contains(true)) Seq(overseasCompliance.goodsToEu.isDefined) else Nil
    }.++ {
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

  private def checkVatReturns(partyType: Option[PartyType], vatApplication: VatApplication) = {
    Seq(
      Some(vatApplication.returnsFrequency.isDefined),
      Some(vatApplication.staggerStart.isDefined),
      vatApplication.staggerStart.flatMap {
        case _: AnnualStagger => Some(
          vatApplication.annualAccountingDetails.exists(aasDetails =>
            aasDetails.paymentMethod.isDefined && aasDetails.paymentFrequency.isDefined
          )
        )
        case _ => None
      },
      if (Seq(NETP, NonUkNonEstablished).exists(partyType.contains)) Some(vatApplication.hasTaxRepresentative.isDefined) else None
    )
  }.flatten
}
