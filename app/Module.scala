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

import java.time.LocalDate
import javax.inject.Singleton

import com.google.inject.{AbstractModule, Scopes, TypeLiteral}
import common.Now
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws.WSHttp

@Singleton
class HmrcHttpClient extends WSHttp {
  override val hooks: Seq[HttpHook] = NoneRequired
}

@Singleton
class LocalDateNow extends Now[LocalDate] {
  override def apply(): LocalDate = LocalDate.now()
}

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[WSHttp]).to(classOf[HmrcHttpClient])

    bind(new TypeLiteral[Now[LocalDate]] {})
      .to(classOf[LocalDateNow])
      .in(Scopes.SINGLETON)
  }


}