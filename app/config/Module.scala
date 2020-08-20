/*
 * Copyright 2020 HM Revenue & Customs
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

package config

import com.google.inject.AbstractModule
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache, ShortLivedHttpCaching}

class Module extends AbstractModule {

  override def configure(): Unit = {
    hmrcDependencyBindings()
  }

  private def hmrcDependencyBindings(): Unit = {
    bind(classOf[AuthConnector]).to(classOf[AuthClientConnector]).asEagerSingleton()
    bind(classOf[SessionCache]).to(classOf[VatSessionCache]).asEagerSingleton()
    bind(classOf[ShortLivedHttpCaching]).to(classOf[VatShortLivedHttpCaching]).asEagerSingleton()
    bind(classOf[ShortLivedCache]).to(classOf[VatShortLivedCache]).asEagerSingleton()
  }
}
