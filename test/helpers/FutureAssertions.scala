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

package helpers

import cats.data.OptionT
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Assertion, TestSuite}
import org.scalatestplus.play.PlaySpec
import play.api.http.Status
import play.api.mvc.Result

import scala.concurrent.Future

trait FutureAssertions extends ScalaFutures {
  self: TestSuite with PlaySpec =>


  implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

  import play.api.test.Helpers._

  implicit class FutureUnit(fu: Future[Unit]) {

    def completedSuccessfully: Assertion = whenReady(fu)(_ mustBe (()))

  }


  implicit class FutureReturns(f: Future[_]) {

    def returns(o: Any): Assertion = whenReady(f)(_ mustBe o)

    def failedWith(e: Exception): Assertion = whenReady(f.failed)(_ mustBe e)

    def failedWith[F <: Throwable](exClass: Class[F]): Assertion = whenReady(f.failed)(_.getClass mustBe exClass)

  }

  implicit class OptionTReturns[T](ot: OptionT[Future, T]) {

    def returnsSome(t: T): Assertion = whenReady(ot.value)(_ mustBe Some(t))

    def returnsNone: Assertion = whenReady(ot.value)(_ mustBe Option.empty[T])

    def failedWith(e: Exception): Assertion = whenReady(ot.value.failed)(_ mustBe e)

    def failedWith[F <: Throwable](exClass: Class[F]): Assertion = whenReady(ot.value.failed)(_.getClass mustBe exClass)

  }


  implicit class FutureResult(fr: Future[Result]) {

    def redirectsTo(url: String): Assertion = {
      status(fr) mustBe Status.SEE_OTHER
      redirectLocation(fr) mustBe Some(url)
    }

    def isA(httpStatusCode: Int): Assertion = {
      status(fr) mustBe httpStatusCode
    }

    def includesText(s: String): Assertion = {
      status(fr) mustBe OK
      contentType(fr) mustBe Some("text/html")
      charset(fr) mustBe Some("utf-8")
      contentAsString(fr) must include(s)
    }

  }

}
