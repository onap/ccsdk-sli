Introduction
======================
You have generated an MD-SAL module.

* You should be able to successfully run ```mvn clean install``` on this project.

Next Steps
======================
* Run a ```mvn clean install``` if you haven't already. This will generate some code from the yang models.
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
The purpose of this ODL feature is to support geo-redundancy through a series of ODL integrated health checks and tools.

## Properties File
On initialization gr-toolkit expects to find a file named ```gr-toolkit.properties``` located in the ```SDNC_CONFIG``` directory. The properties file should contain:
- ```akka.conf.location```
    - The path to the akka.conf configuration file.
- ```adm.useSsl```
    - true/false; Determines whether or not to use http or https when making requests to the Admin Portal.
- ```adm.fqdn```
    - The FQDN or url of the site's Admin Portal.
- ```adm.healthcheck```
    - The url path of the Admin Portal's health check page.
- ```adm.port.http```
    - The HTTP port for the Admin Portal.
- ```adm.port.ssl```
    - The HTTPS port for the Admin Portal.
- ```controller.credentials```
    - username:password; The credentials used to make requests to the ODL controller.
- ```controller.useSsl```
    - true/false; Determines whether or not to use http or https when making requests to the controller.
- ```controller.port.http```
    - The HTTP port for the ODL Controller.
- ```controller.port.ssl```
    - The HTTPS port for the ODL Controller.
- ```controller.port.akka```
    - The port used for Akka communications on the ODL Controller.
- ```mbean.cluster```
    - The Jolokia path for the Akka Cluster MBean.
- ```mbean.shardManager```
    - The Jolokia path for the Akka ShardManager MBean.
- ```mbean.shard.config```
    - The Jolokia path for the Akka Shard MBean. This should be templated to look like ```/jolokia/read/org.opendaylight.controller:Category=Shards,name=%s,type=DistributedConfigDatastore```. GR Toolkit will use this template with information pulled from the Akka ShardManager MBean.
- ```site.identifier```
    - A unique identifier for the site the ODL Controller resides on.

## Site Identifier
Returns a unique site identifier of the site the ODL resides on.

> ### Input: None
> 
> ### Output
> ```json
> {
>   "output": {
>     "id": "UNIQUE_IDENTIFIER_HERE",
>     "status": "200"
>   }
> }
> ```

## Admin Health
Returns HEALTHY/FAULTY based on whether or not a 200 response is received from the Admin Portal's health check page.

> ### Input: None
> 
> ### Output
> ```json
> {
>   "output": {
>     "status": "200",
>     "health": "HEALTHY"
>   }
> }
> ```

## Database Health
Returns HEALTHY/FAULTY based on if DbLib can obtain a writeable connection from its pool.

> ### Input: None
> 
> ### Output
> ```json
> {
>   "output": {
>     "status": "200",
>     "health": "HEALTHY"
>   }
> }
> ```

## Cluster Health
Uses Jolokia queries to determine shard health and voting status. In a 3 ODL node configuration, 2 FAULTY nodes constitutes a FAULTY site. In a 6 node configuration it is assumed that there are 2 sites consiting of 3 nodes each.

> ### Input: None
> 
> ### Output
> ```json
> {
>   "output": {
>     "site1-health": "HEALTHY",
>     "members": [
>       {
>         "address": "member-3.node",
>         "role": "member-3",
>         "unreachable": false,
>         "voting": true,
>         "up": true,
>         "replicas": [
>           {
>             "shard": "member-3-shard-default-config"
>           }
>         ]
>       },
>       {
>         "address": "member-1.node",
>         "role": "member-1",
>         "unreachable": false,
>         "voting": true,
>         "up": true,
>         "replicas": [
>           {
>             "shard": "member-1-shard-default-config"
>           }
>         ]
>       },
>       {
>         "address": "member-5.node",
>         "role": "member-5",
>         "unreachable": false,
>         "voting": false,
>         "up": true,
>         "replicas": [
>           {
>             "shard": "member-5-shard-default-config"
>           }
>         ]
>       },
>       {
>         "address": "member-2.node",
>         "role": "member-2",
>         "unreachable": false,
>         "leader": [
>           {
>             "shard": "member-2-shard-default-config"
>           }
>         ],
>         "commit-status": [
>           {
>             "shard": "member-5-shard-default-config",
>             "delta": 148727
>           },
>           {
>             "shard": "member-4-shard-default-config",
>             "delta": 148869
>           }
>         ],
>         "voting": true,
>         "up": true,
>         "replicas": [
>           {
>             "shard": "member-2-shard-default-config"
>           }
>         ]
>       },
>       {
>         "address": "member-4.node",
>         "role": "member-4",
>         "unreachable": false,
>         "voting": false,
>         "up": true,
>         "replicas": [
>           {
>             "shard": "member-4-shard-default-config"
>           }
>         ]
>       },
>       {
>         "address": "member-6.node",
>         "role": "member-6",
>         "unreachable": false,
>         "voting": false,
>         "up": true,
>         "replicas": [
>           {
>             "shard": "member-6-shard-default-config"
>           }
>         ]
>       }
>     ],
>     "status": "200",
>     "site2-health": "HEALTHY"
>   }
> }
> ```

## Site Health
Aggregates data from Admin Health, Database Health, and Cluster Health and returns a simplified payload containing the health of a site. A FAULTY Admin Portal or Database health status will constitute a FAULTY site; in a 3 ODL node configuration, 2 FAULTY nodes constitutes a FAULTY site. If any portion of the health check registers as FAULTY, the entire site will be designated as FAULTY. In a 6 node configuration these health checks are performed cross site as well.

> ### Input: None
> 
> ### Output
> ```json
> {
>   "output": {
>     "sites": [
>       {
>         "id": "SITE_1",
>         "role": "ACTIVE",
>         "health": "HEALTHY"
>       },
>       {
>         "id": "SITE_2",
>         "role": "STANDBY",
>         "health": "FAULTY"
>       }
>     ],
>     "status": "200"
>   }
> }
> ```

## Halt Akka Traffic
Places rules in IP Tables to block Akka traffic to/from a specific node on a specified port.

> ### Input:
> ```json
> {
>   "input": {
>     "node-info": [
>       {
>         "node": "your.odl.node",
>         "port": "2550"
>       }
>     ]
>   }
> }
> ```
> 
> ### Output
> ```json
> {
>   "output": {
>     "status": "200"
>   }
> }
> ```

## Resume Akka Traffic
Removes rules in IP Tables to allow Akka traffic to/from a specifc node on a specified port.

> ### Input:
> ```json
> {
>   "input": {
>     "node-info": [
>       {
>         "node": "your.odl.node",
>         "port": "2550"
>       }
>     ]
>   }
> }
> ```
> 
> ### Output
> ```json
> {
>   "output": {
>     "status": "200"
>   }
> }
> ```

## Failover
Only usable in a 6 ODL node configuration. Determines which site is active/standby, switches voting to the standby site, and isolates the old active site. If backupData=true an MD-SAL export will be scheduled and backed up to a Nexus server (requires ccsdk.sli.northbound.daexim-offsite-backup feature).

> ### Input:
> ```json
> {
>   "input": {
>     "backupData": "true"
>   }
> }
> ```
> 
> ### Output
> ```json
> {
>   "output": {
>     "status": "200",
>     "message": "Failover complete."
>   }
> }
> ```