
package helpers

import org.joda.time.{LocalDate, LocalDateTime}
import play.api.Environment
import services.TimeService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject

class FakeTimeService @Inject()(env: Environment, servicesConfig: ServicesConfig) extends TimeService(env, servicesConfig) {
  override def currentDateTime: LocalDateTime = LocalDateTime.parse("2020-01-01T09:00:00")
  override def currentLocalDate: LocalDate = LocalDate.parse("2020-01-01")
}
