/*
 * Copyright 2023 HM Revenue & Customs
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

package featureswitch.frontend.services

import featureswitch.core.models.FeatureSwitchSetting
import featureswitch.frontend.config.FeatureSwitchProviderConfig
import featureswitch.frontend.connectors.FeatureSwitchApiConnector
import featureswitch.frontend.models.FeatureSwitchProvider
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FeatureSwitchRetrievalService @Inject()(featureSwitchConfig: FeatureSwitchProviderConfig,
                                              featureSwitchApiConnector: FeatureSwitchApiConnector)
                                             (implicit ec: ExecutionContext) {

  type MicroserviceSwitchSettings = Seq[(FeatureSwitchProvider, Seq[FeatureSwitchSetting])]

  def retrieveFeatureSwitches(implicit hc: HeaderCarrier): Future[MicroserviceSwitchSettings] = {

    val featureSwitchSeq: Seq[(FeatureSwitchProvider, Future[Seq[FeatureSwitchSetting]])] =
      featureSwitchConfig.featureSwitchProviders.map {
        featureSwitchProvider =>
          featureSwitchProvider -> featureSwitchApiConnector.retrieveFeatureSwitches(featureSwitchProvider.url)
      }

    Future.traverse(featureSwitchSeq) {
      case (featureSwitchProvider, futureSeqFeatureSwitchSetting) =>
        futureSeqFeatureSwitchSetting.map {
          featureSwitchSettingSeq => featureSwitchProvider -> featureSwitchSettingSeq
        }
    }
  }

  def updateFeatureSwitches(switchesToEnable: Iterable[String]
                           )(implicit hc: HeaderCarrier): Future[MicroserviceSwitchSettings] =
   retrieveFeatureSwitches
     .map(currentSettings => updateSwitchSettings(currentSettings, switchesToEnable))
     .flatMap {
      Future.traverse(_) {
        case (featureSwitchProvider, featureSwitchSettings) =>
          featureSwitchApiConnector.updateFeatureSwitches(featureSwitchProvider.url, featureSwitchSettings).map {
            updatedFeatureSwitches => featureSwitchProvider -> updatedFeatureSwitches
          }
      }
    }


  private def updateSwitchSettings(currentSettings: MicroserviceSwitchSettings, switchesToEnable: Iterable[String]): MicroserviceSwitchSettings =
    for {
      (microservice, switchSettings) <- currentSettings
      updatedSettings = switchSettings.map(switch =>
        switch.copy(isEnabled = switchesToEnable.exists(_ == s"${microservice.id}.${switch.configName}"))
      )
    } yield (microservice, updatedSettings)

}