
package fixtures

import models._
import models.api.SicCode

trait SicAndComplianceFixture extends ITRegistrationFixtures {

  val sicCodeId = "81300003"
  val sicCodeDesc = "test2 desc"
  val sicCodeDisplay = "test2 display"
  val businessActivityDescription = "test business desc"

  val jsonListSicCode =
    s"""
       |  [
       |    {
       |      "code": "01110004",
       |      "desc": "gdfgdg d",
       |      "indexes": "dfg dfg g fd"
       |    },
       |    {
       |      "code": "$sicCodeId",
       |      "desc": "$sicCodeDesc",
       |      "indexes": "$sicCodeDisplay"
       |    },
       |    {
       |      "code": "82190004",
       |      "desc": "ry rty try rty ",
       |      "indexes": " rtyrtyrty rt"
       |    }
       |  ]
        """.stripMargin

  val mainBusinessActivity = SicCode(sicCodeId, sicCodeDesc, sicCodeDisplay)

  val fullModel = Business(
    email = Some("test@foo.com"),
    telephoneNumber = Some("987654"),
    hasWebsite = Some(true),
    website = Some("/test/url"),
    ppobAddress = Some(addressWithCountry),
    contactPreference = Some(Email),
    businessDescription = Some(businessActivityDescription),
    mainBusinessActivity = Some(mainBusinessActivity),
    businessActivities = Some(List(mainBusinessActivity)),
    labourCompliance = Some(LabourCompliance(
        supplyWorkers = Some(true),
        numOfWorkersSupplied = Some(200),
        intermediaryArrangement = Some(true),
    ))
  )

  val modelWithoutCompliance = Business(
    businessDescription = Some(businessActivityDescription),
    mainBusinessActivity = Some(mainBusinessActivity)
  )
}
