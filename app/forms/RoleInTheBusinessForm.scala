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

package forms

import models.{CompanySecretary, Director, RoleInTheBusiness}
import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}


object RoleInTheBusinessForm {

  val roleInTheBusiness: String = "value"

  val director: String = "director"

  val companySecretary: String = "companySecretary"

  val roleInTheBusinessError: String = "pages.roleInTheBusiness.error.message"

  def apply(): Form[RoleInTheBusiness] = Form(
    single(
      roleInTheBusiness -> of(formatter)
    )
  )

  def formatter: Formatter[RoleInTheBusiness] = new Formatter[RoleInTheBusiness] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], RoleInTheBusiness] = {
      data.get(key) match {
        case Some(`director`) => Right(Director)
        case Some(`companySecretary`) => Right(CompanySecretary)
        case _ => Left(Seq(FormError(key, roleInTheBusinessError)))
      }
    }

    override def unbind(key: String, value: RoleInTheBusiness): Map[String, String] = {
      val stringValue = value match {
        case Director => director
        case CompanySecretary => companySecretary
      }
      Map(key -> stringValue)
    }
  }
}