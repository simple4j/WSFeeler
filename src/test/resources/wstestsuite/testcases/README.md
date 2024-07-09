Test cases are organized hierarchically as directories. Each directory represents a test case.
Test steps are organized as *-imput.properties under test case folders.
Within a test case, test steps are executed first in the sequence of the filename ascending.
Once the test steps are executed, sub test cases are excuted under separate threads for efficiency.
If there are depndencies between test steps, it needs to be sequenced based on filenames.
If there are depndencies between test cases, they need to be organized in hierarchical fashion.

The test case directories can have tcvariables.properties to define variables for that test case. There are predefined variables as well. These variables can be used in the test steps of current test case or sub test cases.

The test step output.properties can define variables to be used in subsequent steps or sub test case test steps.
