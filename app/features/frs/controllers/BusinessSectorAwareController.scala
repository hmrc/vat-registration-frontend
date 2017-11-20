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

package models.view.frs {

  import java.text.DecimalFormat

  import models._
  import models.api.VatScheme
  import play.api.libs.json.Json

  final case class BusinessSectorView(businessSector: String, flatRatePercentage: BigDecimal) {
    val flatRatePercentageFormatted = BusinessSectorView.decimalFormat.format(flatRatePercentage)
  }

  object BusinessSectorView {

    val decimalFormat = new DecimalFormat("#0.##")

    implicit val format = Json.format[BusinessSectorView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (_: S4LFlatRateScheme).categoryOfBusiness,
      updateF = (c: BusinessSectorView, g: Option[S4LFlatRateScheme]) =>
        g.getOrElse(S4LFlatRateScheme()).copy(categoryOfBusiness = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[BusinessSectorView] { (vs: VatScheme) =>
      for {
        frs <- vs.vatFlatRateScheme
        sector <- frs.categoryOfBusiness
        percentage <- frs.percentage
      } yield BusinessSectorView(sector, percentage)
    }
  }
}

package controllers.frs {

  import connectors.ConfigConnect
  import controllers.VatRegistrationControllerNoAux
  import models.CurrentProfile
  import models.view.frs.BusinessSectorView
  import org.apache.commons.lang3.StringUtils
  import services.VatRegistrationService
  import uk.gov.hmrc.play.http.HeaderCarrier

  import scala.concurrent.Future

  // TODO refactor this - controller being treated like a service by RegisterForFrsWithSector
  trait BusinessSectorAwareController extends VatRegistrationControllerNoAux {

    val service: VatRegistrationService
    val configConnect: ConfigConnect

    def businessSectorView()(implicit headerCarrier: HeaderCarrier,profile: CurrentProfile): Future[BusinessSectorView] = {
      service.fetchFlatRateScheme flatMap { flatRateScheme =>
        //TODO StringUtils.isNotBlank(???) - use ???.trim.nonEmpty ?
        flatRateScheme.categoryOfBusiness match {
          case Some(categoryOfBusiness) if StringUtils.isNotBlank(categoryOfBusiness.businessSector) => Future.successful(categoryOfBusiness)
          case _ => service.fetchSicAndCompliance map { sicAndCompliance =>
            sicAndCompliance.mainBusinessActivity match {
              case Some(mainBusinessActivity) => configConnect.getBusinessSectorDetails(mainBusinessActivity.id)
              case None => throw new IllegalStateException("Can't determine main business activity")
            }
          }
        }
      }

//            viewModel[BusinessSectorView]().filter(view => StringUtils.isNotBlank(view.businessSector))
//              .getOrElseF {
//                viewModel[MainBusinessActivityView]()
//                  .subflatMap(mbaView => mbaView.mainBusinessActivity)
//                  .map(sicCode => configConnect.getBusinessSectorDetails(sicCode.id))
//                  .getOrElse(throw new IllegalStateException("Can't determine main business activity"))
//              }
    }
  }
//
//  class Test @Inject()(ds: CommonPlayDependencies, configConnect: ConfigConnect) extends VatRegistrationController(ds) {
//    viewModel[BusinessSectorView]().filter(view => StringUtils.isNotBlank(view.businessSector))
//      .getOrElseF {
//        viewModel[MainBusinessActivityView]()
//          .subflatMap(mbaView => mbaView.mainBusinessActivity)
//          .map(sicCode => configConnect.getBusinessSectorDetails(sicCode.id))
//          .getOrElse(throw new IllegalStateException("Can't determine main business activity"))
//      }
//  }
}
