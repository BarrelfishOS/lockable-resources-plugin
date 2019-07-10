# Quickstart guide for building the plugin

## Environment

You need Maven 3 and a JDK 6.0 or later. See the 
[Jenkins wiki](https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial#Plugintutorial-SettingUpEnvironment) 
for more details.

## Building

You can run a Jenkins test instance which loads the plugin automatically by invoking

```
$ export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
$ mvn hpi:run
```

## Deploying

You can build the plugin hpi file by invoking

```
$ mvn package
```

The resulting hpi file in `target` can be uploaded to Jenkins through Manage
Jenkins > Manage Plugins > Advanced > Upload Plugin.
