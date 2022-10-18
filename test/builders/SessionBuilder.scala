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

package builders

import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.SessionKeys

import java.util.UUID

trait SessionBuilder {

  val userId: String
  val userIdKey: String = "userId"
  val tokenKey: String = "token"

  def updateRequestFormWithSession(fakeRequest: FakeRequest[AnyContentAsFormUrlEncoded], userId: String): FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withSession(
    SessionKeys.sessionId -> s"session-${UUID.randomUUID}",
    tokenKey -> "RANDOMTOKEN",
    userIdKey -> userId
  )

  def buildRequestWithSession(userId: String): FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(
    SessionKeys.sessionId -> s"session-${UUID.randomUUID}",
    tokenKey -> "RANDOMTOKEN",
    userIdKey -> userId
  )

  def updateRequestWithSession[T](req: FakeRequest[T]): FakeRequest[T] = {
    val sessionId = req.headers.get(SessionKeys.sessionId).fold(s"session-${UUID.randomUUID}")(s => s)
    req.withSession(
      SessionKeys.sessionId -> sessionId,
      tokenKey -> "RANDOMTOKEN",
      userIdKey -> userId
    )
  }
}
