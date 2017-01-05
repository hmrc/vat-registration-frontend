# vat-registration-frontend

[![Build Status](https://travis-ci.org/hmrc/vat-registration-frontend.svg)](https://travis-ci.org/hmrc/vat-registration-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/vat-registration-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/vat-registration-frontend/_latestVersion)

This is a placeholder README.md for a new repository

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

## Running locally

Ensure to run the following command to start the service managed services required locally:

```
sm --start SCRS_ALL -f
```

Then enter the following command:

```
sbt run
```

And open your browser to open the following page:

http://local.tax.service.gov.uk:9000/vat-registration/start