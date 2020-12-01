Introduction
======================
You have generated an MD-SAL module.

* You should be able to successfully run ```mvn clean install``` on this project.

Next Steps
======================
* run a ```mvn clean install``` if you haven't already. This will generate some code from the yang models.
* Modify the model yang file under the model project.
* Follow the comments in the generated provider class to wire your new provider into the generated 
code.
* Modify the generated provider model to respond to and handle the yang model. Depending on what
you added to your model you may need to inherit additional interfaces or make other changes to
the provider model.

Generated Bundles
======================
* model
    - Provides the yang model for your application. This is your primary northbound interface.
* provider
    - Provides a template implementation for a provider to respond to your yang model.
* features
    - Defines a karaf feature. If you add dependencies on third-party bundles then you will need to
      modify the features.xml to list out the dependencies.
* installer
    - Bundles all of the jars and third party dependencies (minus ODL dependencies) into a single
      .zip file.

Usage
======================
## Purpose
The purpose of this ODL feature is to support local and geo-redundancy by providing a way to
back up and retrieve MD-SAL data exports to and from a Sonatype Nexus server. In order to function,
this module requires the controller to have an installation of the ```data-export-import``` module and a valid export of MD-SAL data.

## Backup
MD-SAL Backup can be achieved using the ```daexim-offsite-backup:backup-data``` RPC either through the
RESTConf portal or through a tool such as cURL or Postman. While no input is required for this RPC,
the RPC does require the operational, models, and config .JSONs to be present in the daexim directory of the controller.
```sh
export USER=user
export PASSWORD=password
export ODL_HOST=https://yourhost.com:8181
curl -X POST -u$USER:$PASSWORD ${ODL_HOST}/restconf/operations/daexim-offsite-backup:backup-data
```
Through this process a timestamped archive is created in the form of ```POD_NAME-yyyyMMdd_HH-odl_backup.zip``` 
where ```POD_NAME``` is the name of the ODL, specified through the properties file or through an environment variable.

## Retrieval
MD-SAL Retrieval can be achieved by using the ```daexim-offsite-backup:retrieve-data``` RPC either through the
RESTConf portal or through a tool such as cURL or Postman. This RPC requires timestamp information and may 
be supplied with an optional podName.

```sh
export USER=user
export PASSWORD=password
export ODL_HOST=https://yourhost.com:8181
export TARGET_ODL=targetOdlPodName
export TIMESTAMP=yyyyMMdd_HH
export DATA= '
  {
    "input": {
      "pod-name": "'"$TARGET_ODL"'",
      "timestamp": "'"$TIMESTAMP"'"
    }
  }'
curl -X POST -u$USER:$PASSWORD --data $DATA ${ODL_HOST}/restconf/operations/daexim-offsite-backup:retrieve-data
```

Through this process an archive with the specified timestamp (and optional pod name) is downloaded from 
the Nexus server and extracted into the controller's daexim directory. After this it is up to the user 
to trigger an MD-SAL import.

## Properties File
Before each RPC execution this module pulls information from a user supplied properties file. The module expects to find:
- daeximDirectory
- credentials
- nexusUrl
- podName
- file.operational
- file.models
- file.config

> Refer to the example properties file

If the module cannot find the properties file it will default to generic values and attempt to move forward.
