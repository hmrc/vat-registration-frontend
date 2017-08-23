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

package controllers.frs

import javax.inject.Inject

import cats.syntax.FlatMapSyntax
import connectors.ConfigConnect
import controllers.{CommonPlayDependencies, VatRegistrationController}
import models.view.frs.BusinessSectorView
import models.view.sicAndCompliance.MainBusinessActivityView
import org.apache.commons.lang3.StringUtils
import services.{S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class BusinessSectorAwareController @Inject()(ds: CommonPlayDependencies, configConnect: ConfigConnect)
                                             (implicit s4LService: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with FlatMapSyntax {

  protected def businessSectorView()(implicit headerCarrier: HeaderCarrier): Future[BusinessSectorView] =
    viewModel[BusinessSectorView]().filter(view => StringUtils.isNotBlank(view.businessSector))
      .getOrElseF {
        viewModel[MainBusinessActivityView]()
          .subflatMap(mbaView => mbaView.mainBusinessActivity)
          .map(sicCode => configConnect.getBusinessSectorDetails(sicCode.id))
          .getOrElse(throw new IllegalStateException("Can't determine main business activity"))
      }

}
