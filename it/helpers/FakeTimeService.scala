
package helpers

import connectors.BankHolidaysConnector
import play.api.cache.SyncCacheApi
import services.TimeService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.{LocalDate, LocalDateTime}
import javax.inject.Inject

class FakeTimeService @Inject()(bankHolidaysConnector: BankHolidaysConnector, cache: SyncCacheApi, servicesConfig: ServicesConfig)
  extends TimeService(bankHolidaysConnector, cache, servicesConfig) {
  override def currentDateTime: LocalDateTime = LocalDateTime.parse("2020-01-01T09:00:00")

  override def currentLocalDate: LocalDate = LocalDate.parse("2020-01-01")
}
