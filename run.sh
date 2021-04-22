#!/usr/bin/env bash
sbt validate
sbt -Dfrontend -Dapplication.router=testOnlyDoNotUseInAppConf.Routes run

