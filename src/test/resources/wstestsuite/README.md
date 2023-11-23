This is the root directory for the configuration of WSFeeler.
This will contain 2 directories connectors and testcases

- connectors will contain connection configurations for each of the services and db the testsuite will test.
- testcases will contain nested structure of test cases.
	The test cases will get executed from root to leaf.
	Sibling test cases will get executed as per the natural order of the test case directory name.
	Each test case will contain one or more test steps executed as per the natural order of the test steps file name.