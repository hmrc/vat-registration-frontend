
package pages.partners

import forms.partners.PartnerSummaryForm
import helpers.A11ySpec
import models.Entity
import models.api.LtdPartnership
import models.external.{PartnershipIdEntity, RegisteredStatus}
import views.html.partners.PartnerSummary

class PartnerSummaryA11ySpec extends A11ySpec {
  val view: PartnerSummary = app.injector.instanceOf[PartnerSummary]

  val entityDetails: PartnershipIdEntity = PartnershipIdEntity(
    companyName = Some("test-partnership"),
    registration = RegisteredStatus,
    identifiersMatch = true,
    sautr = None, postCode = None, businessVerification = None
  )
  val entity: Entity = Entity(Some(entityDetails), LtdPartnership, Some(true), None, None, None, None)

  "partner summary page" when {
    "rendered with an empty partner list and there are no form errors" must {
      "pass all a11y checks" in {
        view(PartnerSummaryForm(), List.empty[Entity]).body must passAccessibilityChecks
      }
    }

    "rendered with just lead partner entity and there are no form errors" must {
      "pass all a11y checks" in {
        view(PartnerSummaryForm(), List(entity)).body must passAccessibilityChecks
      }
    }

    "rendered with just multiple partner entities and there are no form errors" must {
      "pass all a11y checks" in {
        view(PartnerSummaryForm(), List.fill(2)(entity)).body must passAccessibilityChecks
      }
    }

    "submitted with selection for no more additional partners and rendered without errors" must {
      "pass all a11y checks" in {
        view(PartnerSummaryForm().bind(Map("value" -> "false")), List.fill(2)(entity)).body must passAccessibilityChecks
      }
    }

    "submitted with no selection for additional partners and rendered with errors" must {
      "pass all a11y checks" in {
        view(PartnerSummaryForm().bind(Map("value" -> "")), List.fill(2)(entity)).body must passAccessibilityChecks
      }
    }
  }
}
