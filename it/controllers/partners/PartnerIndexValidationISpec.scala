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

package controllers.partners

import config.FrontendAppConfig
import controllers.partners.PartnerIndexValidation.minPartnerIndex
import itutil.ControllerISpec
import models.api.{Individual, UkCompany}
import models.{CurrentProfile, Entity}
import play.api.mvc.Results._
import play.api.mvc._
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.EntityService

import scala.concurrent.{ExecutionContext, Future}

class PartnerIndexValidationISpec extends ControllerISpec {

  object TestController extends PartnerIndexValidation {
    val entityService: EntityService = app.injector.instanceOf[EntityService]
  }

  def testCall(index: Int): Call = Call(url = s"testUrl$index", method = "GET")

  def testFunction(optEntity: Option[Entity]): Future[Result] = Future.successful(Ok(optEntity.toString))

  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  implicit val testCurrentProfile: CurrentProfile = currentProfile
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  "validateIndex" must {
    "redirect to min if index is below minimum" in {
      val response = await(TestController.validateIndex(minPartnerIndex - 1, testCall)(testFunction))

      response mustBe Redirect(testCall(minPartnerIndex))
    }

    "redirect to max if index is above maximum" in {
      val response = await(TestController.validateIndex(appConfig.maxPartnerCount + 1, testCall)(testFunction))

      response mustBe Redirect(testCall(appConfig.maxPartnerCount))
    }

    "redirect to tasklist if there is no lead partner" in {
      given().registrationApi.getListSection[Entity](None)

      val response = await(TestController.validateIndex(appConfig.maxPartnerCount + 1, testCall)(testFunction))

      response mustBe Redirect(testCall(appConfig.maxPartnerCount))
    }

    "redirect to max possible index if index is too high" in {
      val entityList = List(Entity(None, Individual, None, None))
      given().registrationApi.getListSection[Entity](Some(entityList))

      val response = await(TestController.validateIndex(entityList.length + 2, testCall)(testFunction))

      response mustBe Redirect(testCall(entityList.length + 1))
    }

    "return the page show function if all checks pass" in {
      val entityList = List(Entity(None, Individual, None, None))
      given().registrationApi.getListSection[Entity](Some(entityList))

      val response = await(TestController.validateIndex(2, testCall)(testFunction))

      response mustBe Ok(None.toString)
    }

    "return the page show function with the relevant entity for prepop if all checks pass" in {
      val entityList = List(
        Entity(None, UkCompany, None, None),
        Entity(None, Individual, None, None)
      )
      given().registrationApi.getListSection[Entity](Some(entityList))

      val response = await(TestController.validateIndex(2, testCall)(testFunction))

      response mustBe Ok(Some(Entity(None, Individual, None, None)).toString)
    }
  }

  "validateIndexSubmit" must {
    "redirect to min if index is below minimum" in {
      val response = await(TestController.validateIndexSubmit(minPartnerIndex - 1, testCall)(Future.successful(Ok(""))))

      response mustBe Redirect(testCall(minPartnerIndex))
    }

    "redirect to max if index is above maximum" in {
      val response = await(TestController.validateIndexSubmit(appConfig.maxPartnerCount + 1, testCall)(Future.successful(Ok(""))))

      response mustBe Redirect(testCall(appConfig.maxPartnerCount))
    }

    "return the page show function if all checks pass" in {
      val entityList = List(Entity(None, Individual, None, None))
      given().registrationApi.getListSection[Entity](Some(entityList))

      val response = await(TestController.validateIndexSubmit(2, testCall)(Future.successful(Ok(""))))

      response mustBe Ok("")
    }
  }

}
