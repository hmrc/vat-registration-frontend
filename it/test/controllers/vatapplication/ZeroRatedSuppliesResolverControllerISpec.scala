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

package controllers.vatapplication

import itutil.ControllerISpec
import models.api.vatapplication.VatApplication
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class ZeroRatedSuppliesResolverControllerISpec extends ControllerISpec {

  val url = "/resolve-zero-rated-turnover"

  "GET /resolve-zero-rated-supplies" when {
    "the user has entered £0 as their turnover estimate" must {
      "store £0 as the zero rated estimate and bypass the zero-rated supplies page" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.replaceSection[VatApplication](VatApplication(turnoverEstimate = Some(0), zeroRatedSupplies = Some(0)))
          .registrationApi.getSection[VatApplication](Some(VatApplication(turnoverEstimate = Some(0), zeroRatedSupplies = Some(0))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.SellOrMoveNipController.show.url)
      }
    }
    "the user has entered a non-zero value for their turnover estimate" must {
      "redirect to the zero-rated supplies page" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](Some(fullVatApplication))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ZeroRatedSuppliesController.show.url)
      }
    }
    "the vat scheme doesn't contain turnover" must {
      "redirect to the missin answer page" in new Setup {
        given
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
      }
    }
  }

}
