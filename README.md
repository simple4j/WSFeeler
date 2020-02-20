# WSFeeler

Simple4j WSClient is a generic web service client that can be used for all types of data exchanges (XML, JSON, SOAP, REST and more) over both http and https protocols.
Many of the data marshallers and parsers adds heavier dependency between client and server components for stricter type.
This can result in tight coupling of web services and its client defeating the purpose of the creation of web service technology.

WSClient focuses on loose coupling by using templating on the request marshaling side opening up the possibility to even any non-standard format. On the response side, it uses generic parsing of XML and JSON to nested Java Collections object tree. It also support custom parsing of any non-standard response formats other than XML and JSON.
This flexible and configurable design allows easy adaptability and maintainability of client application without making any code change even if the interface changes on the server side.

Currently, Freemarker, Velocity and a custom simple templates are being supported. This can be extended from IFormatter class if other template engines are needed. The IFormatter instance can be used for generation of HTTP request URL, headers and body.

On the response parsing side, XML and JSON are supported and it can be extended from IParser for any additional support. If the response body has lot of nested attributes, custom retrieval can be configured (Caller.responseBodyToCustomFieldMapping) using nested wildcardable paths.

The entry point for the client code is org.simple4j.wsclient.caller.Caller.call

Sample code showing how to configure and use various capabilities of the framework can be found in test cases. The test cases use WireMock to have a mock web service. The client code is configured and executed from the test cases.

Test cases from simple to complex case.
* ConnectionConfigTest
* MethodsTest
* RequestObjectTypeTest
* ResponseObjectTypeTest
