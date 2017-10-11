--- Service Logic Interpreter --- Dan Timoney --- 2014-11-12 ---

Supported node types
====================

The following built-in node types are currently supported:

-  Flow Control

   -  `**block** <#Block_node>`__

   -  `**call** <#Call_node>`__

   -  `**for** <#For_node>`__

   -  `**return** <#Return_node>`__

   -  `**set** <#Set_node>`__

   -  `**switch** <#Switch_node>`__

-  Device Management

   -  `**configure** <#Configure_node>`__

-  Java Plugin Support

   -  `**execute** <#Execute_node>`__

-  Recording

   -  `**record** <#Record_node>`__

-  Resource Management

   -  `**delete** <#Delete_node>`__

   -  `**exists** <#Exists_node>`__

   -  `**get-resource** <#Get-resource_node>`__

   -  `**is-available** <#Is-available_node>`__

   -  `**notify** <#Notify_node>`__

   -  `**release** <#Release_node>`__

   -  `**reserve** <#Reserve_node>`__

   -  `**save** <#Save_node>`__

   -  `**update** <#Update_node>`__

Flow Control
------------

Block node
~~~~~~~~~~

Description
^^^^^^^^^^^

A **block** node is used to executes a set of nodes.

Attributes
^^^^^^^^^^

+--------------+-----------------------------------------------------------------------------------------------------------------------------------+
| **atomic**   | if *true*, then if a node returns failure, subsequent nodes will not be executed and nodes already executed will be backed out.   |
+--------------+-----------------------------------------------------------------------------------------------------------------------------------+

Parameters
^^^^^^^^^^

None

Outcomes
^^^^^^^^

None

Example
^^^^^^^

::

    <block>
      <record plugin="org.onap.ccsdk.sli.core.sli.recording.FileRecorder">
        <parameter name="file" value="/tmp/sample_r1.log" />
        <parameter name="field1" value="__TIMESTAMP__"/>
        <parameter name="field2" value="RESERVED"/>
        <parameter name="field3" value="$asePort.uni_circuit_id"/>
      </record>
      <return status="success">
        <parameter name="uni-circuit-id" value="$asePort.uni_circuit_id" />
      </return>
    </block>

Call node
~~~~~~~~~

Description
^^^^^^^^^^^

A **call** node is used to call another graph

Attributes
^^^^^^^^^^

+---------------+------------------------------------------------------------------------------------+
| **module**    | Module of directed graph to call. If unset, defaults to that of calling graph      |
+---------------+------------------------------------------------------------------------------------+
| **rpc**       | rpc of directed graph to call.                                                     |
+---------------+------------------------------------------------------------------------------------+
| **version**   | version of graph to call, If unset, uses active version.                           |
+---------------+------------------------------------------------------------------------------------+
| **mode**      | mode (sync/async) of graph to call. If unset, defaults to that of calling graph.   |
+---------------+------------------------------------------------------------------------------------+

Parameters
^^^^^^^^^^

Not applicable

Outcomes
^^^^^^^^

+-----------------+------------------------------+
| **success**     | Sub graph returned success   |
+-----------------+------------------------------+
| **not-found**   | Graph not found              |
+-----------------+------------------------------+
| **failure**     | Subgraph returned success    |
+-----------------+------------------------------+

Table: .

Example
^^^^^^^

::

    <call rpc="svc-topology-reserve" mode="sync" />

For node
~~~~~~~~

Description
^^^^^^^^^^^

A **for** node provides a fixed iteration looping mechanism, similar to
the Java for loop

Attributes
^^^^^^^^^^

+-------------+------------------+
| **index**   | index variable   |
+-------------+------------------+
| **start**   | initial value    |
+-------------+------------------+
| **end**     | maximum value    |
+-------------+------------------+

Parameters
^^^^^^^^^^

Not applicable.

Outcomes
^^^^^^^^

Not applicable. The **status** node has no outcomes.

Example
^^^^^^^

::

    <for index="i" start="0" end="`$service-data.universal-cpe-ft.l2-switch-interfaces_length`">
       <record plugin="org.onap.ccsdk.sli.core.sli.recording.Slf4jRecorder">
          <parameter name="logger" value="message-log"/>
          <parameter name="level" value="info"/>
          <parameter name="field1" value="`'current l2-switch-interface name is ' + $service-data.universal-cpe-ft.l2-switch-interfaces[$i].name`"/>
       </record>
    </for>

Return node
~~~~~~~~~~~

Description
^^^^^^^^^^^

A **return** node is used to return a status to the invoking MD-SAL
application

Attributes
^^^^^^^^^^

+--------------+---------------------------------------------------+
| **status**   | Status value to return (*success* or *failure*)   |
+--------------+---------------------------------------------------+

Parameters
^^^^^^^^^^

The following optional parameters may be passed to convey more detailed
status information.

+---------------------+-----------------------------------------------------------------+
| **error-code**      | A brief, usually numeric, code indicating the error condition   |
+---------------------+-----------------------------------------------------------------+
| **error-message**   | A more detailed error message                                   |
+---------------------+-----------------------------------------------------------------+

Outcomes
^^^^^^^^

Not applicable. The **status** node has no outcomes.

Example
^^^^^^^

::

    <return status="failure">
      <parameter name="error-code" value="1542" />
      <parameter name="error-message" value="Activation failure" />
    </return>

Set node
~~~~~~~~

Description
^^^^^^^^^^^

A **set** node is used to set one or more values in the execution
context

Attributes
^^^^^^^^^^

+---------------------+-------------------------------------------------------------------------------------+
| **only-if-unset**   | If true the set node will only execute if the current value of the target is null   |
+---------------------+-------------------------------------------------------------------------------------+

Parameters
^^^^^^^^^^

Values to be set are passed as parameters

Outcomes
^^^^^^^^

Not applicable. The **set** node has no outcomes.

Example
^^^^^^^

::

    <set>
      <parameter name="vlan" value="$network.provider-segmentation-id" />
    </set>

Switch node
~~~~~~~~~~~

Description
^^^^^^^^^^^

A **switch** node is used to make a decision based on its **test**
attribute.

Attributes
^^^^^^^^^^

+------------+---------------------+
| **test**   | Condition to test   |
+------------+---------------------+

Parameters
^^^^^^^^^^

None

Outcomes
^^^^^^^^

Depends on the **test** condition

Example
^^^^^^^

::

    <switch test="$uni-cir-units">
      <outcome value="Mbps">
        <reserve plugin="org.onap.ccsdk.sli.adaptors.samplesvc.SampleServiceResource"
                 resource="ase-port"
                 key="resource-emt-clli == $edge-device-clli and speed >= $uni-cir-value"
                 pfx="asePort">

          <outcome value="success">
            <return status="success">
              <parameter name="uni-circuit-id" value="$asePort.uni_circuit_id" />
            </return>
          </outcome>
          <outcome value="Other">
            <return status="failure">
              <parameter name="error-code" value="1010" />
              <parameter name="error-message" value="No ports found that match criteria" />
            </return>
          </outcome>
        </reserve>
      </outcome>
      <outcome value="Gbps">
        <reserve plugin="org.onap.ccsdk.sli.adaptors.samplesvc.SampleServiceResource"
                 resource="ase-port"
                 key="resource-emt-clli == $edge-device-clli and speed >= $uni-cir-value*1000"
                 pfx="asePort">

          <outcome value="success">
            <return status="success">
              <parameter name="uni-circuit-id" value="$asePort.uni_circuit_id" />
            </return>
          </outcome>
          <outcome value="Other">
            <return status="failure">
              <parameter name="error-code" value="1010" />
              <parameter name="error-message" value="No ports found that match criteria" />
            </return>
          </outcome>
        </reserve>
      </outcome>
    </switch>

Device Management
-----------------

Configure node
~~~~~~~~~~~~~~

Description
^^^^^^^^^^^

A **configure** node is used to configure a device.

Attributes
^^^^^^^^^^

+----------------+-----------------------------------------------------------------------------------+
| **adaptor**    | Fully qualified Java class of resource adaptor to be used                         |
+----------------+-----------------------------------------------------------------------------------+
| **activate**   | Activate device/interface, for devices that support a separate activation step.   |
+----------------+-----------------------------------------------------------------------------------+
| **key**        | SQL-like string specifying criteria for item to configure                         |
+----------------+-----------------------------------------------------------------------------------+

Parameters
^^^^^^^^^^

Specific to device adaptor.

Outcomes
^^^^^^^^

+----------------------+------------------------------------------------------------------+
| **success**          | Device successfully configured                                   |
+----------------------+------------------------------------------------------------------+
| **not-found**        | Element to be configured does not exist.                         |
+----------------------+------------------------------------------------------------------+
| **not-ready**        | Element is not in a state where it can be configured/activated   |
+----------------------+------------------------------------------------------------------+
| **already-active**   | Attempt to activate element that is already active               |
+----------------------+------------------------------------------------------------------+
| **failure**          | Configure failed for some other reason                           |
+----------------------+------------------------------------------------------------------+

Example
^^^^^^^

::

    <configure adaptor="org.onap.ccsdk.sli.adaptors.emt.EmtAdaptor"
               key="$uni-circuit-id" activate="true">
      <parameter name="circuit.id" value="$uni-circuit-id" />
      <parameter name="subscriber.name" value="$subscriber-name" />
      <parameter name="emt.clli" value="$edge-device-clli" />
      <parameter name="port.tagging" value="$port-tagging" />
      <parameter name="port.mediaSpeed" value="$media-speed" />
      <parameter name="location.state" value="$uni-location-state" />
      <parameter name="location.city" value="$uni-location-city" />
      <parameter name="cosCategory" value="$cos-category" />
      <parameter name="gosProfile" value="$gos-profile" />
      <parameter name="lldp" value="$asePort.resource-lldp" />
      <parameter name="mtu" value="$asePort.resource-mtu" />
      <outcome value="success">
        <block>
          <record plugin="org.onap.ccsdk.sli.core.sli.recording.FileRecorder">
            <parameter name="file" value="/tmp/sample_r1.log" />
            <parameter name="field1" value="__TIMESTAMP__"/>
            <parameter name="field2" value="ACTIVE"/>
            <parameter name="field3" value="$uni-circuit-id"/>
          </record>
          <return status="success">
            <parameter name="edge-device-clli" value="$asePort.resource-emt-clli" />
          </return>
        </block>
      </outcome>
      <outcome value="already-active">
        <return status="failure">
          <parameter name="error-code" value="1590" />
          <parameter name="error-message" value="Port already active" />
        </return>
      </outcome>
      <outcome value="Other">
        <return status="failure">
          <parameter name="error-code" value="1542" />
          <parameter name="error-message" value="Activation failure" />
        </return>
      </outcome>
    </configure>

Java Plugin Support
-------------------

Execute node
~~~~~~~~~~~~

Description
^^^^^^^^^^^

An **execute** node is used to execute Java code supplied as a plugin

Attributes
^^^^^^^^^^

+--------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| **plugin**   | Fully qualified Java class of plugin to be used                                                                                                                                                    |
+--------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| **method**   | Name of method in the plugin class to execute. Method must return void, and take 2 arguments: a Map (for parameters) and a SvcLogicContext (to allow plugin read/write access to context memory)   |
+--------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

Parameters
^^^^^^^^^^

Specific to plugin / method

Outcomes
^^^^^^^^

+--------------------------+-----------------------------------------------------------------+
| **success**              | Device successfully configured                                  |
+--------------------------+-----------------------------------------------------------------+
| **not-found**            | Plugin class could not be loaded                                |
+--------------------------+-----------------------------------------------------------------+
| **unsupported-method**   | Named method taking (Map, SvcLogicContext) could not be found   |
+--------------------------+-----------------------------------------------------------------+
| **failure**              | Configure failed for some other reason                          |
+--------------------------+-----------------------------------------------------------------+

Example
^^^^^^^

::

    <execute plugin="org.onap.ccsdk.sli.plugins.HelloWorld"
               method="log">
      <parameter name="message" value="Hello, world!" />
      <outcome value="success">
          <return status="success"/>
      </outcome>
      <outcome value="not-found">
        <return status="failure">
          <parameter name="error-code" value="1590" />
          <parameter name="error-message" value="Could not locate plugin" />
        </return>
      </outcome>
      <outcome value="Other">
        <return status="failure">
          <parameter name="error-code" value="1542" />
          <parameter name="error-message" value="Internal error" />
        </return>
      </outcome>
    </execute>

Recording
---------

Record node
~~~~~~~~~~~

Description
^^^^^^^^^^^

A **record** node is used to record an event. For example, this might be
used to log provisioning events.

Attributes
^^^^^^^^^^

+--------------+---------------------------------------------------+
| **plugin**   | Fully qualified Java class to handle recording.   |
+--------------+---------------------------------------------------+

Parameters
^^^^^^^^^^

Parameters will depend on the plugin being used. For the FileRecorder
class, the parameters are as follows

+--------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| **file**     | The file to which the record should be written                                                                                                                                                                       |
+--------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| **field1**   | First field to write. There will be **field** parameters for each field to write, from **field1** through **fieldN**. A special value \_\_TIMESTAMP\_\_ may be assigned to a field to insert the current timestamp   |
+--------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

Outcomes
^^^^^^^^

+---------------+--------------------------------------------+
| **success**   | Record successfully written                |
+---------------+--------------------------------------------+
| **failure**   | Record could not be successfully written   |
+---------------+--------------------------------------------+

Example
^^^^^^^

::

    <record plugin="org.onap.ccsdk.sli.core.sli.recording.FileRecorder">
      <parameter name="file" value="/tmp/sample_r1.log" />
      <parameter name="field1" value="__TIMESTAMP__"/>
      <parameter name="field2" value="ACTIVE"/>
      <parameter name="field3" value="$uni-circuit-id"/>
    </record>

Resource Management
-------------------

Delete node
~~~~~~~~~~~

Description
^^^^^^^^^^^

A **delete** node is used to delete a resource from the local resource
inventory.

Attributes
^^^^^^^^^^

+----------------+-------------------------------------------------------------+
| **plugin**     | Fully qualified Java class of resource adaptor to be used   |
+----------------+-------------------------------------------------------------+
| **resource**   | Type of resource to delete                                  |
+----------------+-------------------------------------------------------------+
| **key**        | SQL-like string specifying key to delete                    |
+----------------+-------------------------------------------------------------+

Parameters
^^^^^^^^^^

None

Outcomes
^^^^^^^^

+---------------+--------------------------------------------+
| **success**   | Resource specified deleted successfully.   |
+---------------+--------------------------------------------+
| *failure*>    | Resource specified was not deleted         |
+---------------+--------------------------------------------+

Example
^^^^^^^

::

    <delete plugin="org.onap.ccsdk.sli.adaptors.samplesvc.SampleServiceResource"
            resource="ase-port"
            key="uni_circuit_id == $uni-circuit-id">
      <outcome value="true">
        <return status="success"/>
      </outcome>
      <outcome value="false">
        <return status="failure"/>
      </outcome>
    </delete>

Exists node
~~~~~~~~~~~

Description
^^^^^^^^^^^

An **exists** node is used to determine whether a particular instance of
a resource exists. For example, this might be used to test whether a
particular switch CLLI is provisioned.

Attributes
^^^^^^^^^^

+----------------+-------------------------------------------------------------+
| **plugin**     | Fully qualified Java class of resource adaptor to be used   |
+----------------+-------------------------------------------------------------+
| **resource**   | Type of resource to check                                   |
+----------------+-------------------------------------------------------------+
| **key**        | SQL-like string specifying key to check for                 |
+----------------+-------------------------------------------------------------+

Parameters
^^^^^^^^^^

None

Outcomes
^^^^^^^^

+-------------+---------------------------------+
| **true**    | Resource specified exists.      |
+-------------+---------------------------------+
| **false**   | Resource specified is unknown   |
+-------------+---------------------------------+

Example
^^^^^^^

::

    <exists plugin="org.onap.ccsdk.sli.adaptors.samplesvc.SampleServiceResource"
            resource="ase-port"
            key="uni_circuit_id == $uni-circuit-id">
      <outcome value="true">
        <return status="success"/>
      </outcome>
      <outcome value="false">
        <return status="failure"/>
      </outcome>
    </exists>

Get-resource node
~~~~~~~~~~~~~~~~~

Description
^^^^^^^^^^^

A **get-resource** node is used to retrieve information about a
particular resource and make it available to other nodes in the service
logic tree. For example, this might be used to retrieve information
about a particular uni-port.

Attributes
^^^^^^^^^^

+----------------+------------------------------------------------------------------------------------------+
| **plugin**     | Fully qualified Java class of resource adaptor to be used                                |
+----------------+------------------------------------------------------------------------------------------+
| **resource**   | Type of resource to retrieve                                                             |
+----------------+------------------------------------------------------------------------------------------+
| **key**        | SQL-like string specifying criteria for retrieval                                        |
+----------------+------------------------------------------------------------------------------------------+
| **pfx**        | Prefix to add to context variable names set for data retrieved                           |
+----------------+------------------------------------------------------------------------------------------+
| **select**     | String to specify, if key matches multiple entries, which entry should take precedence   |
+----------------+------------------------------------------------------------------------------------------+
| **order-by**   | Prefix to add to context variable names set for data retrieved                           |
+----------------+------------------------------------------------------------------------------------------+

Parameters
^^^^^^^^^^

None

Outcomes
^^^^^^^^

+-----------------+--------------------------------------------------+
| **success**     | Resource successfully retrieved                  |
+-----------------+--------------------------------------------------+
| **not-found**   | Resource referenced does not exist               |
+-----------------+--------------------------------------------------+
| **failure**     | Resource retrieve failed for some other reason   |
+-----------------+--------------------------------------------------+

Example
^^^^^^^

::

    <get-resource plugin="org.onap.ccsdk.sli.adaptors.samplesvc.SampleServiceResource"
                  resource="ase-port"
                  key="uni_circuit_id == $uni-circuit-id"
                  pfx="current-port">
      <outcome value="success">
        <return status="success"/>
      </outcome>
      <outcome value="not-found">
        <return status="failure"/>
      </outcome>
      <outcome value="failure">
        <return status="failure"/>
      </outcome>
    </get-resource>

Is-available node
~~~~~~~~~~~~~~~~~

Description
^^^^^^^^^^^

An **is-available** node is used to determine whether a particular type
of resource is available. For example, this might be used to test
whether any ports are available for assignment on a particular switch.

Attributes
^^^^^^^^^^

+----------------+------------------------------------------------------------------+
| **plugin**     | Fully qualified Java class of resource adaptor to be used        |
+----------------+------------------------------------------------------------------+
| **resource**   | Type of resource to check                                        |
+----------------+------------------------------------------------------------------+
| **key**        | SQL-like string specifying key to check for                      |
+----------------+------------------------------------------------------------------+
| **pfx**        | Prefix to add to context variable names set for data retrieved   |
+----------------+------------------------------------------------------------------+

Parameters
^^^^^^^^^^

None

Outcomes
^^^^^^^^

+-------------+---------------------------------------+
| **true**    | Resource requested is available       |
+-------------+---------------------------------------+
| **false**   | Resource requested is not available   |
+-------------+---------------------------------------+

Example
^^^^^^^

::

    <is-available plugin="org.onap.ccsdk.sli.adaptors.samplesvc.SampleServiceResource"
                  resource="ase-port"
                  key="resource-emt-clli == $edge-device-clli and speed >= $uni-cir-value">
      <outcome value="true">
        <return status="success"/>
      </outcome>
      <outcome value="false">
        <return status="failure"/>
      </outcome>
    </is-available>

Notify node
~~~~~~~~~~~

Description
^^^^^^^^^^^

A **notify** node is used to inform an external application (e.g. A&AI)
that a resource was updated.

Attributes
^^^^^^^^^^

+----------------+---------------------------------------------------------------------+
| **plugin**     | Fully qualified Java class of resource adaptor to be used           |
+----------------+---------------------------------------------------------------------+
| **resource**   | Identifies resource that was updated                                |
+----------------+---------------------------------------------------------------------+
| **action**     | Action that triggered notification to be sent (ADD/UPDATE/DELETE)   |
+----------------+---------------------------------------------------------------------+

Parameters
^^^^^^^^^^

None

Outcomes
^^^^^^^^

+---------------+----------------------------------------+
| **success**   | Notification was successful            |
+---------------+----------------------------------------+
| **failure**   | Notification failed is not available   |
+---------------+----------------------------------------+

Example
^^^^^^^

::

    <notify plugin="org.onap.ccsdk.sli.adaptors.samplesvc.SampleServiceResource"
                  resource="ase-port"
                  action="ADD">
      <outcome value="success">
        <return status="success"/>
      </outcome>
      <outcome value="Other">
        <return status="failure"/>
      </outcome>
    </notify>

Release node
~~~~~~~~~~~~

Description
^^^^^^^^^^^

A **release** node is used to mark a resource as no longer in use, and
thus available for assignment.

Attributes
^^^^^^^^^^

+----------------+------------------------------------------------------------------+
| **plugin**     | Fully qualified Java class of resource adaptor to be used        |
+----------------+------------------------------------------------------------------+
| **resource**   | Type of resource to release                                      |
+----------------+------------------------------------------------------------------+
| **key**        | SQL-like string specifying key to check of resource to release   |
+----------------+------------------------------------------------------------------+

Parameters
^^^^^^^^^^

None

Outcomes
^^^^^^^^

+-----------------+-------------------------------------------------+
| **success**     | Resource successfully released                  |
+-----------------+-------------------------------------------------+
| **not-found**   | Resource referenced does not exist              |
+-----------------+-------------------------------------------------+
| **failure**     | Resource release failed for some other reason   |
+-----------------+-------------------------------------------------+

Example
^^^^^^^

::

    <release plugin="org.onap.ccsdk.sli.adaptors.SampleServiceResource"
             resource="ase-port"
             key="uni_circuit_id == $uni-circuit-id">
      <outcome value="success">
        <return status="success"/>
      </outcome>
      <outcome value="not-found">
        <return status="failure"/>
      </outcome>
      <outcome value="failure">
        <return status="failure"/>
      </outcome>
    </release>

Reserve node
~~~~~~~~~~~~

Description
^^^^^^^^^^^

A **reserve** node is used to reserve a particular type of resource..
For example, this might be used to reserve a port on a particular
switch.

Attributes
^^^^^^^^^^

+----------------+----------------------------------------------------------------------------------------------+
| **plugin**     | Fully qualified Java class of resource adaptor to be used                                    |
+----------------+----------------------------------------------------------------------------------------------+
| **resource**   | Type of resource to reserve                                                                  |
+----------------+----------------------------------------------------------------------------------------------+
| **key**        | SQL-like string specifying criteria for reservation                                          |
+----------------+----------------------------------------------------------------------------------------------+
| **select**     | String to specify, if **key** matches multiple entries, which entry should take precedence   |
+----------------+----------------------------------------------------------------------------------------------+

Parameters
^^^^^^^^^^

None

Outcomes
^^^^^^^^

+---------------+----------------------------------------------------+
| **success**   | Resource requested was successfully reserved       |
+---------------+----------------------------------------------------+
| **failure**   | Resource requested was not successfully reserved   |
+---------------+----------------------------------------------------+

Example
^^^^^^^

::

    <reserve plugin="org.onap.ccsdk.sli.adaptors.samplesvc.SampleServiceResource"
             resource="ase-port"
             key="resource-emt-clli == $edge-device-clli and speed >= $uni-cir-value"
             select="min(speed)">
      <outcome value="success">
        <return status="success"/>
      </outcome>
      <outcome value="failure">
        <return status="failure"/>
      </outcome>
    </reserve>

Save node
~~~~~~~~~

Description
^^^^^^^^^^^

A **save** node is used to save information about a particular resource
to persistent storage. For example, this might be used to save
information about a particular uni-port.

Attributes
^^^^^^^^^^

+----------------+------------------------------------------------------------------------------------------+
| **plugin**     | Fully qualified Java class of resource adaptor to be used                                |
+----------------+------------------------------------------------------------------------------------------+
| **resource**   | Type of resource to save                                                                 |
+----------------+------------------------------------------------------------------------------------------+
| **key**        | SQL-like string specifying criteria for retrieval                                        |
+----------------+------------------------------------------------------------------------------------------+
| **force**      | If "true", save resource even if this resource is already stored in persistent storage   |
+----------------+------------------------------------------------------------------------------------------+
| **pfx**        | Prefix to be prepended to variable names, when attributes are set in SvcLogicContext     |
+----------------+------------------------------------------------------------------------------------------+

Parameters
^^^^^^^^^^

Values to save (columns) are specified as parameters, with each name
corresponding to a column name and each value corresponding to the value
to set.

Outcomes
^^^^^^^^

+---------------+-------------------------------+
| **success**   | Resource successfully saved   |
+---------------+-------------------------------+
| **failure**   | Resource save failed          |
+---------------+-------------------------------+

Example
^^^^^^^

::

    <save plugin="`$sample-resource-plugin`" resource="vnf"
        key="vnf-name = $requests.vnf.vnf-name" force="true"
        pfx="requests.vnf">
        <parameter name="vnf-name"
            value="`$requests.cust-country-code + $requests.cust-id + $requests.cust-city + $requests.cust-state + '001VCE'`" />
        <parameter name="vnf-type" value="vce" />
        <parameter name="orchestration-status" value="pending-create" />
        <parameter name="heat-stack-id" value="`$requests.heat-stack-id`" />
        <parameter name="mso-catalog-key" value="`$requests.mso-catalog-key`" />
        <parameter name="oam-ipv4-address" value="`$vce-ipv4-oam-addr.ipv4-addr`" />
    </save>

Update node
~~~~~~~~~~~

Description
^^^^^^^^^^^

An **update** node is used to update information about a particular
resource to persistent storage.

Attributes
^^^^^^^^^^

+----------------+----------------------------------------------------------------------------------------+
| **plugin**     | Fully qualified Java class of resource adaptor to be used                              |
+----------------+----------------------------------------------------------------------------------------+
| **resource**   | Type of resource to update                                                             |
+----------------+----------------------------------------------------------------------------------------+
| **key**        | SQL-like string specifying criteria for retrieval                                      |
+----------------+----------------------------------------------------------------------------------------+
| **pfx**        | Prefix to be prepended to variable names, when attributes are set in SvcLogicContext   |
+----------------+----------------------------------------------------------------------------------------+

Parameters
^^^^^^^^^^

Values to save (columns) are specified as parameters, with each name
corresponding to a column name and each value corresponding to the value
to set.

Outcomes
^^^^^^^^

+---------------+-------------------------------+
| **success**   | Resource successfully saved   |
+---------------+-------------------------------+
| **failure**   | Resource save failed          |
+---------------+-------------------------------+

Example
^^^^^^^

::

    <update plugin="`$sample-resource-plugin`" resource="vnf"
        key="vnf-name = $requests.vnf.vnf-name"
        pfx="requests.vnf">
        <parameter name="vnf-name"
            value="`$requests.cust-country-code + $requests.cust-id + $requests.cust-city + $requests.cust-state + '001VCE'`" />
        <parameter name="vnf-type" value="vce" />
        <parameter name="orchestration-status" value="pending-create" />
        <parameter name="heat-stack-id" value="`$requests.heat-stack-id`" />
        <parameter name="mso-catalog-key" value="`$requests.mso-catalog-key`" />
        <parameter name="oam-ipv4-address" value="`$vce-ipv4-oam-addr.ipv4-addr`" />
    </update>
