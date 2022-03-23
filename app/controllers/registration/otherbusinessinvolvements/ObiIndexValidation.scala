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

package controllers.registration.otherbusinessinvolvements

import models.OtherBusinessInvolvement
import play.api.mvc.{Call, Result}
import play.api.mvc.Results.Redirect

import scala.concurrent.Future

trait ObiIndexValidation {

  def validateIndex(index: Int, call: Int => Call)(function: Future[Result]): Future[Result] =
    if (index > OtherBusinessInvolvement.maxIndex) {
      Future.successful(Redirect(call(OtherBusinessInvolvement.maxIndex)))
    } else if (index < OtherBusinessInvolvement.minIndex) {
      Future.successful(Redirect(call(OtherBusinessInvolvement.minIndex)))
    } else {
      function
    }

}
