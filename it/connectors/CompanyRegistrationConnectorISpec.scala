
package connectors

import models.external.CompanyRegistrationProfile
import support.AppAndStubs
import uk.gov.hmrc.play.test.UnitSpec
import utils.VATRegFeatureSwitch

class CompanyRegistrationConnectorISpec extends UnitSpec with AppAndStubs {

  val companyRegConnector                 = app.injector.instanceOf[CompanyRegistrationConnector]
  val featureSwitch: VATRegFeatureSwitch  = app.injector.instanceOf[VATRegFeatureSwitch]

  val ctStatusRaw         = "draft"

  override lazy val additionalConfig: Map[String, String] =
    Map(
      "default-ct-status" -> "ZHJhZnQ=",
      "regIdPostIncorpWhitelist" -> "OTgsOTk="
    )

  "getCompanyProfile" should {
    featureSwitch.manager.enable(featureSwitch.useCrStubbed)

    "return the default CompanyRegProfile for a whitelisted regId" in {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
      val res = companyRegConnector.getCompanyProfile("99")(hc)
      await(res) shouldBe Some(CompanyRegistrationProfile("accepted", None))
    }
    "return a CompanyRegProfile for a non-whitelisted regId" in {
      given()
        .corporationTaxRegistration.existsWithStatus("held", "01")
        .audit.writesAudit()
        .audit.writesAuditMerged()
      val res = companyRegConnector.getCompanyProfile("1")(hc)
      await(res) shouldBe Some(CompanyRegistrationProfile("held", Some("01")))
    }
  }

  "getTransactionId" should {
    "return the default transactionId for a whitelisted regId" in {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      val res = companyRegConnector.getTransactionId("99")(hc)
      await(res) shouldBe "fakeTxId-99"

    }
    "return a transactionId for a non-whitelisted regId" in {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .corporationTaxRegistration.existsWithStatus("draft", "01")

      val res = companyRegConnector.getTransactionId("1")(hc)
      await(res) shouldBe "000-431-TEST"
    }
  }
}
