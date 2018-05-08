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
package itutil

import uk.gov.hmrc.mongo.MongoSpecSupport

trait FakeAppConfig extends MongoSpecSupport {

  val mockHost: String
  val mockPort: Int

  val mockUrl = s"http://$mockHost:$mockPort"

  def fakeConfig(extraConfig: (String,String)*): Map[String, String] = {
    Map(
      "json.encryption.key" -> "fqpLDZ4sumDsekHkeEBlCA==",
      "play.filters.csrf.header.bypassHeaders.X-Requested-With" -> "*",
      "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
      "auditing.enabled" -> "false",
      "auditing.consumer.baseUri.host" -> s"$mockHost",
      "auditing.consumer.baseUri.port" -> s"$mockPort",
      "microservice.services.auth.host" -> s"$mockHost",
      "microservice.services.auth.port" -> s"$mockPort",
      "microservice.services.cachable.session-cache.host" -> s"$mockHost",
      "microservice.services.cachable.session-cache.port" -> s"$mockPort",
      "microservice.services.cachable.session-cache.domain" -> "keystore",
      "microservice.services.cachable.short-lived-cache.host" -> s"$mockHost",
      "microservice.services.cachable.short-lived-cache.port" -> s"$mockPort",
      "microservice.services.vat-registration.host" -> s"$mockHost",
      "microservice.services.vat-registration.port" -> s"$mockPort",
      "microservice.services.bank-account-reputation.host" -> s"$mockHost",
      "microservice.services.bank-account-reputation.port" -> s"$mockPort",
      "mongodb.uri" -> s"$mongoUri"
    ) ++ extraConfig
  }
}