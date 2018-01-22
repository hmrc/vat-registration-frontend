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
package controllers

import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import features.sicAndCompliance.models.test.SicStub
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import support.AppAndStubs

class DeleteSessionItemsControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures {

  "deleteVatRegistration" should {
    "return an OK" in {

      given()
        .user.isAuthorised
        .currentProfile.withProfile(Some(STARTED), Some("Current Profile"))
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vrefe.deleteVREFESession()
        .s4lContainer[SicStub].cleared
        .keystore.deleteKS()
        .vatScheme.deleted

      val response = buildInternalClient("/1/delete")
        .withHeaders("X-Session-ID" -> "session-1112223355556")
        .delete()

      whenReady(response) {
        _.status mustBe 200
      }
    }
  }
}
