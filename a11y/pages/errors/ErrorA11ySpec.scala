
package pages.errors

import helpers.A11ySpec
import views.html.errors.{AlreadySubmittedKickout, ContactView, SubmissionFailed, SubmissionRetryableView}

class ErrorA11ySpec extends A11ySpec {

  val submissionRetryableView: SubmissionRetryableView = app.injector.instanceOf[SubmissionRetryableView]
  val submissionFailedView: SubmissionFailed = app.injector.instanceOf[SubmissionFailed]
  val alreadySubmittedKickoutView: AlreadySubmittedKickout = app.injector.instanceOf[AlreadySubmittedKickout]
  val contactView: ContactView = app.injector.instanceOf[ContactView]

  "the SubmissionRetryableView page" must {
    "pass all a11y checks" in {
      submissionRetryableView().body must passAccessibilityChecks
    }
  }

  "the SubmissionFailed page" must {
    "pass all a11y checks" in {
      submissionFailedView().body must passAccessibilityChecks
    }
  }

  "the AlreadySubmittedKickout page" must {
    "pass all a11y checks" in {
      alreadySubmittedKickoutView().body must passAccessibilityChecks
    }
  }

  "the ContactView page" must {
    "pass all a11y checks" in {
      contactView().body must passAccessibilityChecks
    }
  }
}
