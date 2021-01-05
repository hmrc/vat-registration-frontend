/*
 * Copyright 2021 HM Revenue & Customs
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

import mocks.AuthMock
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc._
import play.api.test.FakeRequest

import scala.concurrent.Future

trait AuthBuilder extends SessionBuilder with AuthMock {
  self: MockitoSugar =>

  val userId = "testUserId"

  def requestWithAuthorisedUser[T <: AnyContent](action: Action[AnyContent], request: FakeRequest[T])
                                                (test: Future[Result] => Any) {
    mockAuthenticated()
    val result = action(updateRequestWithSession(request))
    test(result)
  }

  def requestWithAuthorisedUser(action: Action[AnyContent])(test: Future[Result] => Any) {
    val userId = "testUserId"
    mockAuthenticated()
    val result = action(buildRequestWithSession(userId))
    test(result)
  }

  def withAuthorisedUser(action: Action[AnyContent])(test: Future[Result] => Any) {
    val userId = "testUserId"
    mockAuthenticated()
    val result = action(buildRequestWithSession(userId))
    test(result)
  }

  def withAuthorisedOrgUser(action: Action[AnyContent])(test: Future[Result] => Any): Unit = {
    val userId = "testUserId"
    mockAuthenticatedOrg()
    val result = action(buildRequestWithSession(userId))
    test(result)
  }

  def submitWithUnauthorisedUser(action: Action[AnyContent], request: FakeRequest[AnyContentAsFormUrlEncoded])
                                (test: Future[Result] => Any) {
    mockNotAuthenticated()
    val result = action.apply(updateRequestFormWithSession(request, ""))
    test(result)
  }

  def submitWithAuthorisedUser(action: Action[AnyContent], request: FakeRequest[AnyContentAsFormUrlEncoded])
                              (test: Future[Result] => Any) {
    mockAuthenticated()
    val result = action.apply(updateRequestFormWithSession(request, userId))
    test(result)
  }
}
