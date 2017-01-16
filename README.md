# vat-registration-frontend

[![Build Status](https://travis-ci.org/hmrc/vat-registration-frontend.svg)](https://travis-ci.org/hmrc/vat-registration-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/vat-registration-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/vat-registration-frontend/_latestVersion)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

## Prior to committing
```
sbt clean coverage test scalastyle coverageReport
```

alternatively, create an alias for the above line, and get in the habit of running it before checking in:

```bash
alias precommit="sbt clean coverage test scalastyle coverageReport"
```

### NOTE: Only commit if test coverage report is above or equal to 95%, scalastyle warnings are corrected and tests green.

## Running locally

Ensure to run the following command to start the service managed services required locally:

```
sm --start SCRS_ALL -f
```

alternatively, create an alias for starting the services required for the VAT Registration Frontend

```bash
alias scrs='sm --start AUTH AUTH_LOGIN_STUB AUTHENTICATOR CA_FRONTEND GG GG_STUBS USER_DETAILS KEYSTORE SAVE4LATER DATASTREAM ASSETS_FRONTEND -f'
```

To run the service, just `cd` to cloned directory and execute:

```
sbt run
```

And open your browser to open the following page:

http://localhost/vat-registration/

### Nginx
if you want to avoid having to include the service port number in the URL, add an nginx rule in your `hmrc.conf` file located under `/usr/local/etc/nginx/servers`

```
location /vat-registration {
  proxy_pass                http://localhost:9895;
}
```
