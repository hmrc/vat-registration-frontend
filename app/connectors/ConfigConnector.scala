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
package connectors

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import models.api.SicCode
import uk.gov.hmrc.play.config.ServicesConfig


@Singleton
class ConfigConnector extends ConfigConnect with ServicesConfig {

  override def getSicCodesListFromCodes(codes: List[String]) : List[SicCode]= {
    val sicCodeSuffixKey = "sic.codes."
    codes.map(sicCode => {
      SicCode(id = sicCode,
        description = getString(s"${sicCodeSuffixKey}${sicCode}.description"),
        displayDetails = getString(s"${sicCodeSuffixKey}${sicCode}.displayDetails"))
    }
    )
  }
}

@ImplementedBy(classOf[ConfigConnector])
trait ConfigConnect {
  self =>
  val className = self.getClass.getSimpleName
  def getSicCodesListFromCodes(codes: List[String]) : List[SicCode]

}
