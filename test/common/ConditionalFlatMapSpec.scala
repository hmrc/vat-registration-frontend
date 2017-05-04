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

package common

import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConditionalFlatMapSpec extends UnitSpec with ScalaFutures {

  private trait TestDummy {
    def methodToCall(s: String): Future[Unit]
  }

  private class Setup {
    val mockDummy = Mockito.mock(classOf[TestDummy])
    when(mockDummy.methodToCall("testString")).thenReturn(Future.successful(()))
  }

  import ConditionalFlatMap._
  import cats.instances.future._

  "an arbitrary boolean condition is true" should {

    "invoke a method on a service in a for-comprehension" in new Setup() {
      val arbitraryCondition = true
      val futureFoo = for {
        foo <- Future.successful("testString")
        _ <- mockDummy.methodToCall(foo) onlyIf arbitraryCondition
      } yield foo

      whenReady(futureFoo) { result =>
        result shouldBe "testString"
        verify(mockDummy, times(1)).methodToCall("testString")
      }
    }

  }


  "an arbitrary boolean condition is false" should {

    "NOT invoke a method on a service in a for-comprehension" in new Setup() {
      val arbitraryCondition = false
      val futureFoo = for {
        foo <- Future.successful("testString")
        _ <- mockDummy.methodToCall(foo) onlyIf arbitraryCondition
      } yield foo

      whenReady(futureFoo) { result =>
        result shouldBe "testString"
        verify(mockDummy, never()).methodToCall("testString")
      }
    }

  }

}
