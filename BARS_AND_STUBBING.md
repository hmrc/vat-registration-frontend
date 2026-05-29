# Bank Details Journey, BARS Checks and Stubbing

The 'Bank account details'
journey in section 5 gives the user the option to enter their bank account details or to give a reason why not to.

If they opt to give their personal or business account details,
they must enter their account name, number, sort code and optionally their roll call number.
These details are then used to make a BARS (Bank Account Reputation Service) check which returns a Valid,
Indeterminate or Invalid status.

> Documentation: The documentation for the BARS Verify API can be found [here](https://github.com/hmrc/bank-account-reputation/blob/main/public/api/conf/1.0/docs/business/verify.md). It explains the API response values which are used to determine the status [here](app/models/bars/BarsVerificationResponse.scala). 

## BARS Checks
- The user has 3 attempts to successfully make a BARS check.
- The BARS check can result in a `Valid`, `Indeterminate` or `Invalid` status.
- If they succeed, they are returned to the tasklist page and the 'Bank account details' journey is marked as `Completed`.
- If they fail, they are prompted to try again and are warned of their remaining attempts.
- After a 3rd failed attempt, the user is informed they are no longer able to attempt, and they are prompted back to the tasklist page with the journey marked as complete. This is because it is more important to us for a user to finish their registration than it is for them to accurately add their bank account details.

### Valid
The user proceeds through the journey and their bank account details will be submitted at the end of registration.

### Indeterminate
The user proceeds through the journey as if they have received a valid status,
however, their bank details shall be submitted with a `bankDetailsNotValid = true` flag to ETMP.

### Invalid
The user must reattempt to enter their bank details.
After 3 failed attempts,
they are locked out and automatically given the `reasonBankAccNotProvided = "7" (DontWantToProvide)` reason.
> **Important:** If a user makes an invalid BARS check then goes back and gives a reason instead (or is assigned the lockout reason) then their failed bank details are still saved on file and sent to the API at submission.
> These are used for fraud detection purposes only and are not send to ETMP.


## Stubbing

### Full VAT Registration stubbed data
A full VAT registration journey can be created from stubs to avoid filling in each journey from scratch.
1. Sign in using the auth-login for [localhost](http://localhost:9949/auth-login-stub/gg-sign-in) or [staging](https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in).
2. Follow the stub link for [localhost](http://localhost:9895/register-for-vat/test-only/vat-stub) or [staging](https://www.staging.tax.service.gov.uk/register-for-vat/test-only/vat-stub).
3. Enter a name and continue.
4. Agree to the declaration and continue.
5. Click the stub link for [localhost](http://localhost:9895/register-for-vat/test-only/vat-stub) or [staging](https://www.staging.tax.service.gov.uk/register-for-vat/test-only/vat-stub) again.

### Stubbing BARS API Response
When `StubBars` switch is on, response is stubbed by [BankAccountReputationStubController.verifyBankDetails()](app/controllers/test/BankAccountReputationStubController.scala).

Response status is determined by the first 2 digits of the sort code:
- 11 XX XX -> `Invalid`
- 22 XX XX -> `Indeterminate`
- XX XX XX -> `Valid`