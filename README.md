# vat-registration-frontend

[![Build Status](https://travis-ci.org/hmrc/vat-registration-frontend.svg)](https://travis-ci.org/hmrc/vat-registration-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/vat-registration-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/vat-registration-frontend/_latestVersion)

The purpose of this microservice is to allow users to register their business for VAT online.

Testing

## How to start the service locally
To start all the dependent services required by VAT Registration Frontend in service manager, you can either use `sm --start VAT_REG_ALL -r` or `sm2 --start VAT_REG_ALL -r` if you are using sm2.

Alternatively, if you want to start the service individually, you can either use `sm --start VAT_REG_FE -r` or  `sm2 --start VAT_REG_FE -r` if you are using sm2.

Prior to starting the service locally, make sure the instance running in service manager is stopped. This can be done by running either `sm --stop VAT_REG_FE`, or `sm2 --stop VAT_REG_FE` if you are using sm2.

### From source code on your local machine
> The below instructions are for Mac/Linux only. Windows users will have to use SBT to run the service on port `9895`.
1. Clone this repository into the development environment on your machine.
2. Open a terminal and navigate to the folder you cloned the service into.
3. Run either `./run.sh` or `sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes` to start the service locally on port `9895` and enable the `/test-only` routes for local testing.
4. Ensure all dependent services are started, using the `VAT_REG_ALL` service manager profile.

## Starting a VAT Registration Journey

### Individual journey
Individual journeys are not supported by VAT Registration.

### Organisation journey
1. In a browser, navigate to the Auth Login Stub on `http://localhost:9949/auth-login-stub/gg-sign-in`
2. Enter the following information:
    - Redirect URL: `http://localhost:9895/register-for-vat`
    - Affinity group: `Organisation`
3. Click `Submit` to start the journey.

### Agent journey
1. In a browser, navigate to the Auth Login Stub on `http://localhost:9949/auth-login-stub/gg-sign-in`
2. Enter the following information:
    - Redirect URL: `http://localhost:9895/register-for-vat`
    - Affinity group: `Agent`
    - Enrolments (at the bottom of the page):
        - Enrolment Key: `HMRC-AS-AGENT`
        - Identifier name: `AgentReferenceNumber`
        - Identifier value: enter anything
        - Status: `Activated`
3. Click `Submit` to start the journey.
4. During the journey, you will be asked if you want to register for yourself or someone else. Select `Somone else's`
   and providing you are logged in as `Agent` affinity group and have the `HMRC-AS-AGENT` enrolment, you will proceed
   with the agent journey. If you select `Your own` instead, the non-agent journey will apply.

## Test only routes

### Set feature switches
The service calls out to various services throughout the journey which have been stubbed. To set the feature switches, navigate to `http://localhost:9895/register-for-vat/test-only/feature-switches`.

> Note: These pages set feature switches using system properties, which are local to the specific JVM the service is running on.
> If you are testing in an environment that provisions multiple instances (e.g. QA/Staging), you will need to either submit the feature switch page
> several times to ensure all instances are hit, or, alternatively, set the feature switches in config as JVM arguments and
> re-deploy the service.

### Submission payload
To view the json payload from a submission, on the final Check Your Answers page before selecting `Confirm and submit` to submit the application, navigate to ```http://localhost:9895/register-for-vat/test-only/submission-payload```.

## Test the application

### Unit and Integration tests
To run the unit and integration tests, you can either use ```sbt test it:test``` or ```sbt clean coverage test it:test scalastyle coverageReport```.

### Accessibility tests
To run the accessibility tests, use ```sbt a11y:test```. 

You will need to have `Node.js` and `npm` installed to run the accessibility tests. 

# Further Documentation
[Documentation of TEST endpoints](test-endpoints.md)

### License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")