# vat-registration-frontend

[![Build Status](https://travis-ci.org/hmrc/vat-registration-frontend.svg)](https://travis-ci.org/hmrc/vat-registration-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/vat-registration-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/vat-registration-frontend/_latestVersion)

This is a placeholder README.md for a new repository

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

## Before committing
```
sbt clean coverage test scalastyle coverageReport
```

## NOTE: Only commit if test coverage report is above or equal to 95%, scalastyle warnings are corrected and tests green.

## Running locally

Ensure to run the following command to start the service managed services required locally:

```
sm --start SCRS_ALL -f
```

To run, just cd to cloned directory and execute:

```
sbt run
```

And open your browser to open the following page:

http://local.tax.service.gov.uk:9895/vat-registration/start
