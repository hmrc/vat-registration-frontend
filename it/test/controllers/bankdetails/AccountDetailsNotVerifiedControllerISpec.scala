/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.bankdetails

import itFixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.Lock
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import java.time.Instant

class AccountDetailsNotVerifiedControllerISpec extends ControllerISpec with ITRegistrationFixtures {

  val url = "/failed-verification"

  "GET /failed-verification" must {

    "redirect to BankDetailsLockoutController when user is locked out" in new Setup {
      given().user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      await(
        barsLockRepository.collection
          .insertOne(
            Lock(currentProfile.registrationId, failedAttempts = 3, lastAttemptedAt = Instant.now())
          )
          .toFuture())

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.BankDetailsLockoutController.show.url)
    }

    "return OK and display the view when user has failed attempts but is not locked" in new Setup {
      given().user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      await(
        barsLockRepository.collection
          .insertOne(
            Lock(currentProfile.registrationId, failedAttempts = 2, lastAttemptedAt = Instant.now())
          )
          .toFuture())

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe OK
    }
  }
}
