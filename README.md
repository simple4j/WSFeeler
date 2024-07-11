# WSFeeler

Simple4j WSFeeler is a configurable WS testing framework that can be used for all types of services (XML, JSON, SOAP, REST) over both http and https protocols.
This also has DB verification step to verify persisted data.

There is a sample test web service and test suite under the maven standard src/test/resources path.
The root of directory for configuration is wstestsuite.
wstestsuite will contain 2 directories connectors, testcases and a file tsvariables.properties.
connectors directory contain spring configuration for web services and any database dao needed for the test suite.
testcases directory contains configuration for test cases and test steps in the test cases.
tsvariables.properties is optional and can contain variable definition for the whole test suite with values assigned at the start of the test suite. Below is the list of predefined variables that can be used as well in test steps.
TESTSUITE/HOSTNAME=InetAddress.getLocalHost().getHostName()
TESTSUITE/HOSTIP=InetAddress.getLocalHost().getHostAddress()
TESTSUITE/STARTTIME=""+System.currentTimeMillis()
TESTSUITE/UUID=UUID.randomUUID().toString()
TESTSUITE/RAND5=Math.round(Math.random()*99999)
TESTSUITE/RAND10=Math.round(Math.random()*9999999999)


Test cases are organized hierarchically as directories. Each directory represents a test case.
Test steps are organized as *-imput.properties under test case folders.
Within a test case, test steps are executed first in the ascending sequence of the filename.
Once the test steps are executed, sub test cases are executed under separate threads for efficiency.
If there are dependencies between test steps, it needs to be sequenced based on filenames.
If there are dependencies between test cases, they need to be organized in hierarchical fashion.

The test case directories can have tcvariables.properties to define variables for that test case. These variables can be used in the test steps of current test case or sub test cases. Below is the list of predefined variables that can be used as well in test steps. These variables will have unique value within the test case for each run where as test suite variables are unique for each run.
TESTCASE/HOSTNAME=InetAddress.getLocalHost().getHostName()
TESTCASE/HOSTIP=InetAddress.getLocalHost().getHostAddress()
TESTCASE/STARTTIME=""+System.currentTimeMillis()
TESTCASE/UUID=UUID.randomUUID().toString()
TESTCASE/RAND5=Math.round(Math.random()*99999)
TESTCASE/RAND10=Math.round(Math.random()*9999999999)

The test steps are defined by a pair of input and output properties. For example, 010-allFields-input.properties and 010-allFields-output.properties. For this step, the step name is 010-allFields
The test step output.properties can define variables to be used in subsequent steps or sub test case test steps. The test step variables are a collection of input variables, output variables and predefined variables HTTP_RESPONSE_OBJECT, HTTP_STATUS_CODE, HTTP_RESPONSE_HEADERS
The variable reference between test steps can be referenced in the format <test step name>/<variable name>. The variables from parent test case can be referenced in the format ../<parent test case test step name>/<variable name>
