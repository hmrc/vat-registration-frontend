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

package models.view.vatContact

import models.api.{VatFinancials, VatScheme}
import models.{ApiModelTransformer, ViewModelTransformer}
import play.api.libs.json.{Json, OFormat}

case class BusinessContactDetails(
                                   email: String,
                                   daytimePhone: Option[String] = None,
                                   mobile: Option[String] = None,
                                   website: Option[String] = None
                                 )

object BusinessContactDetails {

  implicit val format: OFormat[BusinessContactDetails] = Json.format[BusinessContactDetails]


  //TODO implement once backend model merged
  implicit val modelTransformer = ApiModelTransformer[BusinessContactDetails] { (vs: VatScheme) =>

    Some(BusinessContactDetails("some-email@emailprovider.com"))
    //    vs.financials.map(_.turnoverEstimate).collect {
    //      case turnoverEstimate => BusinessContactDetails(turnoverEstimate)
    //    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: BusinessContactDetails, g: VatFinancials) => {
    g //TODO implement once backend model merged
  }
  }

}
