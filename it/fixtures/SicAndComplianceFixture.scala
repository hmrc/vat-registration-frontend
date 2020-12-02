
package fixtures

import models._
import models.api.SicCode

trait SicAndComplianceFixture {

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

  val mainBusinessActivityView = MainBusinessActivityView(sicCodeId, Some(SicCode(sicCodeId, sicCodeDesc, sicCodeDisplay)))

  val fullModel = SicAndCompliance(
    description = Some(BusinessActivityDescription(businessActivityDescription)),
    mainBusinessActivity = Some(mainBusinessActivityView),
    supplyWorkers = Some(SupplyWorkers(true)),
    workers = Some(Workers(200)),
    intermediarySupply = Some(IntermediarySupply(true)),
    businessActivities = Some(BusinessActivities(List(SicCode(sicCodeId, sicCodeDesc, sicCodeDisplay))))
  )

  val modelWithoutCompliance = SicAndCompliance(
    description = Some(BusinessActivityDescription(businessActivityDescription)),
    mainBusinessActivity = Some(mainBusinessActivityView)
  )

}
