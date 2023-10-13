## POL - B&DE - Test Automation Framework

#### 1. Purpose and Contents

The aim of this test automation framework is providing a one-stop solution for test automation requirements across scrum teams for both In-sprint and Regression test automation. Targeting to create a hybrid framework with highly reusable modules to enable browser/mobile app/api testing using Cucumber BDD.

As a first step, this framework is now having options for UI automation(browser based/mobile simulation UI automation). <Br>
More features will be added in the upcoming sprints.

**Note:** To start the test execution, please refer section 5. Quick Checklist to Trigger Tests available at the end of this file.<BR>
****

#### 2. Parameters to run tests

To start the testes from Terminal/Commandline, please use maven commands <BR>
**mvn clean test -Denv="dev"** - **Mandatory** 

**mvn clean test** - To clean the target folder, build and run tests configured in maven project<BR>

**-Denv="dev"** - **Mandatory** - Name of the properties file contains environment specific parameters. E.g.: dev.properties or test1.properties, which is available under src\test\resources\config\environments\ <Br>
Note: If the environment is not set from commandline/terminal, the framework will refer the 'defaultEnvironment' parameter from **defaultSettings.properties**

Can also add other parameters which are available in defaultSettings.properties file.<BR>
**Note: All parameters that are available in the defaultSettings.properties file are overridable when we start tests from commandline/Terminal**

**Example :**

**mvn clean test -Denv=""
-DgenerateAWSCredentials="true" -DserverType="grid"
-DgridURL=""
-Dusername=""
-Dpassword=""
-DapplicationName=""
-DtestSuiteXmlFileName=""
-DenableBackendValidations=""
-DenableTandTValidations=""
-DenableDespatchValidations=""
-DenableZephyrExecution=""
-DzephyrAuthorizationKey=""
-DzephyrProjectKey=""
-DtestCycleFolderName=""**

##### 2.1 Parameters in defaultSettings.properties file
######DEFAULT SETTINGS######<BR>
**defaultEnvironment**=test2<BR>
defaultEnvironment will be used to choose the environment specific parameters(like urls, device details etc.,) from properties files(like dev/test1/test2.properties)<BR>

**driverStack**=chrome<BR>
driverStack will help to choose the browser to run UI tests.

**useWebDriverManager**=true<BR>
When useWebDriverManager is set to 'true', then driver(E.g.: chromedriver.exe) files are not required under folders Drivers/Windows or Linux, the webdrivermanager maven package will automatically download required driver and launch the browser. If it is set to 'false' then we need to manually download and place the required driver exe files.<BR>  
**serverType**=local<BR>
serverType can be 'local' or 'grid' based on the execution environment setup<BR>

**gridURL**=http://localhost:4444<BR>
When serverType is 'grid', then the value for gridURL is mandatory.<BR>

**defaultWait**=10<BR>
**defaultElementRetries**=5<BR>
**keepBrowserOpen**=true<BR>
On setting keepBrowserOpen to 'true', the browser instance can be retained, i.e, will not get quit at the end of each scenario and reused for all scenarios. But in case of any failures, browser session will be terminated and relaunched for next scenario, even though keepBrowserOpen is set to 'true' <BR>

**generateAWSCredentials**=false<BR>
generateAWSCredentials should be set to 'true' for cloud execution, so that the Service Account will be used to generate AWS access tokens to connect to aws services/to genertate jwt tokens to hit APIs. During execution/debugging from local machines, generateAWSCredentials should be set to 'false', so that manually generated aws credentials can be picked up from environment specific property files.<BR>  

######Application name to choose excel data sheet######<BR>
**applicationName**=MailJourney<BR>

Any of below application names can be chosen. The application name will be used to choose the Test Data file, testNG folder to locate the testSuiteXmlFileName(which is required to trigger tests from commandline/Terminal) <BR>
#MailJourney<BR>
#BankingAndPayments<BR>
#CounterTerminal<BR>
#DropAndCollect<BR>
#JourneyEngine<BR>
#DataReference<BR>
#IntegrationAPI<BR>
#TransactionAPI<BR>

######Excel Report File Name######<BR>
**StaticExcelReportFileName=false**<BR>
**ExcelReportFileName**=<BR>
The framework generates the Excel report file name for everyrun, in case if an already created excel report need to be used for the following executions, StaticExcelReportFileName can be set to 'true' and Excel file name can be set to ExcelReportFileName.<BR> 

######Screenshot######<BR>
scrollingScreenshot=false<BR>
screenshotDelay=0<BR>
screenshotOnFailure=true<BR>

######Chrome Options######<BR>
scrollToElements.chrome=true<BR>
options.chrome=--no-sandbox;start-maximized;disable-infobars;--disable-extensions"<BR>
#options.chrome=--headless;start-maximized;<BR>


######Backend Validations######<BR>
enableBackendValidations=true<BR>
enableBranchDetailsValidations=true<BR>
enableTandTValidations=false<BR>
enableDespatchValidations=true<BR>
enableCFSValidation=false<BR>
enableCredenceValidation=false<BR>
checkForAdditionalCredenceEntries=false<BR>

######Log DB Data to CSV######<BR>
DBCSVLogging=true<BR>

######Zephyr Scale Params######<BR>
enableZephyrExecution=false<BR>
zephyrBaseURI=https://api.zephyrscale.smartbear.com/v2<BR>
zephyrAuthorizationKey=<BR>
zephyrProjectKey=<BR>
testCycleFolderName=<BR>
######Please enter folder ID and Cycle ID only for manual executions######<BR>
######Please leave them empty for cloud uninterrupted executions######<BR>
zephyrFolderId=<BR>
zephyrCycleID=<BR>

######Credentials for device manager and simulator######<BR>
username=<BR>
password=<BR>

######Back Office######<BR>
BackOfficeUserName=<BR>
BackOfficePassWord=<BR>

#### 3. Base Classes and Run Configs

| Package  | Class                  | Purpose                                                      |
| -------- | -----------------------| ------------------------------------------------------------ |
| pol.bde.steps | BaseSteps              | Base class providing access to the driver and wait objects. This class will be extended by each 'step definition' class in the test project. |
| pol.bde.steps | CommonSteps            | Small set of common step definitions used for browser/app testing |
| pol.bde.hooks | Hooks                  | Teardown cucumber hooks that ensure screenshot capture for failed tests.  Screenshots are written to the report directory within the test project. The screenshot file path is also written to the test parameters (Common Library object) and if the Reporting Library is used by the test project then the screenshot will be automatically embedded in the run report. |
| pol.bde.runners | SequentialBaseRunner   | Base runner class to run scenarios one by one that sets the initial @CucumberOptions and the @Test method that invokes cucumber via TestNG (with data provider). This class will be extended by each 'runner' class in the test project used for browser / app testing. |
| pol.bde.runners | ParallelBaseRunner   | Base runner class to run scenarios in parallel that sets the initial @CucumberOptions and the @Test method that invokes cucumber via TestNG (with data provider). This class will be extended the 'runner' class in the test project to run scenarios in parallel. |

The runner files need to be configured in the testng file as below
E.g. for sequential run:
```
<suite name="suite1" parallel="classes" thread-count="10" data-provider-thread-count="10">
    <test name="e2e-1">
        <classes>
            <class name="pol.bde.runners.e2e.e2e"/>
        </classes>
    </test>
</suite>
```
E.g. for parallel run: <BR> 
- The parallel threads can be increased or decreased based on the requirement
- More classes or tests can be added to increase the coverage based on the requirement
```
<suite name="suite1" parallel="methods" thread-count="10" data-provider-thread-count="10">
    <test name="e2e-1">
        <classes>
            <class name="pol.bde.runners.e2e.e2e"/>
        </classes>
    </test>
</suite>
```
The testNG xml file needs be mapped in corresponding profile in pom.xml file 
```
<profile>
    <!--Usage: mvn clean test -P <profileName>-->
    <id>E2E</id>
    <activation>
        <activeByDefault>false</activeByDefault>
    </activation>
    <properties>
        <testNG.suiteXmlFile>src/test/resources/testNG/e2e.xml</testNG.suiteXmlFile>
    </properties>
    <build>
    ....
    ....
    </build>
</profile>
```
#### 4. Utilities
#### 4.1 Page Object Utils

provides a set of page level methods that generally return the Element class when performing find operations.  Also includes methods that perform page level waits for DOM loading and JQuery readiness.

##### PageObjectUtils class should be extended in all page object class files:

```
public class LoginPage extends PageObjectUtils {

    public LoginPage() {
        initialise(this);
    }
...
}
```



##### Then for the page object classes the following operations will then be available:

- Obtain the driver instance from the factory for the current thread:

```
getDriver()
```

- Obtain a default selenium wait object for the driver for the current thread:

```
getWait()
```

- Navigate to a given url:

```
gotoURL(<url>);
```

- Wait for page loading (DOM, JQuery, Angular):

```
waitPageToLoad();
```

- Find element with retries

```
findElement(<By>, <boolean retryFindElement>)
findElement(<By>, <retries>)
findElementUsingJQuery(<queryString>)
```

-Static waits

```
waitFor(<milliSecs>)
waitInSeconds(<seconds>)
```
- Wait until the specified element is located
```
waitUntilElementLocated(<By>, <timeOutInSeconds>)
waitUntilElementLocated(<By>)
```

- Wait until the specified element is visible

```
waitUntilElementVisible(<By>, <timeOutInSeconds>)
waitUntilElementVisible(<By>)
waitUntilElementVisible(<WebElement>, <timeOutInSeconds>)
waitUntilElementVisible(<WebElement>)
```

- Wait until the specified element is clickable

```
waitUntilElementClickable(<By>, <timeOutInSeconds>)
waitUntilElementClickable(<By>)
waitUntilElementClickable(<WebElement>, <timeOutInSeconds>)
waitUntilElementClickable(<WebElement>)
```

- Wait until the specified element is visible and clickable

```
waitUntilElementVisibleAndClickable(<WebElement>, <timeOutInSeconds>)
waitUntilElementVisibleAndClickable(<WebElement>)
```

- Wait until the specified element is disabled

```
waitUntilElementDisabled(<By>, <timeOutInSeconds>)
waitUntilElementDisabled(<By>)
waitUntilElementDisabled(<WebElement>, <timeOutInSeconds>)
waitUntilElementDisabled(<WebElement>)
```

- To verify if an element exists
```
isElementExists(<By>, <retries>)
isElementExists(<WebElement>)
```

- To verify if an element is visible
```
isElementVisible(<By>, <retries>)
isElementVisible(<WebElement>)
```

- To verify if an element is displayed
```
isElementDisplayed(<By>, <retries>)
isElementDisplayed(<WebElement>)
```

- To verify if an element is enabled
```
isElementEnabled(<By>, <retries>)
isElementEnabled(<WebElement>)
```

- To select the specified value from a dropdown

```
selectListItem(<By>, <item>)
selectListItem(<WebElement>>, <item>)
```

- To verify whether the specified object exists within the current

```
objectExists(<By>)
objectExists(<List<WebElement>>)
```

- To verify whether the specified text is present within the current page
```
isTextPresent(<textPattern>)
isTextPresent(<WebElement>, <textPattern>)
```
- To check if an alert is present on the current page

```
isAlertPresent(<timeOutInSeconds>)
isAlertPresent()
```

- To performs mouse click action on the element using javascript where native click does not work
```
clickJS(<WebElement>) 
```

- Send text using javascript

```
sendKeysJS(<WebElement>, <val>)
```

- Scrolls to element

```
scroll(<WebElement>)
scroll(<WebElement>, <hAlign>, <vAlign>)
```

- Switch driver to another window:

```
switchWindow(<currentwindowhandle>);
```

- switch driver to another frame:

```
switchFrame(<frameLocator>);
switchFrame(<By>);
switchFrame(<Element>);
switchToDefaultContent();
```

#### 4.2 Test Parameters
The framework includes a thread specific Context object can be used to share any data across multiple step classes or be accessed from any test code including page objects.  This object includes a map variable that can be used to store and share values or objects throughout the executing thread.

```
TestParameters.getInstance().putTestDataClass(<key>, <object>);
TestParameters.getInstance().putTestData("foo", "bar");
.....

- To pull back test data from the test context for the current thread :

(<objectType>) TestParameters.getInstance().getTestDataClass(<key>); //casting to required object type
TestParameters.getInstance().getTestData("foo");
```
#### 4.3 Property Helper
- To read a property file from a given file path:

```
PropertiesConfiguration props = Property.getProperties(<filepath>);			
```

- To get a property value from a given property file:

```
String val = Property.getProperty(<filepath>, <key>);
```

- To get set of values from a given property file where multiple entries exist for same key:

```
String[] vals = Property.getPropertyArray(<filepath>, <key>);
```

- To get a java system property or environment variable value:

```
String val = Property.getVariable(<variable>);
```

- To get a property value from a defaultSettings.properties file:

```
String val = Property.getDefaultProperty(<variable>);
```

- To get a property value from a environment specific properties file: <BR>
E.g.: If -Denv="dev" has been passed to start a test run, then the values configured in the dev.properties file will be referred

```
String val = Property.getEnvSpecificAppParameters(<variable>);
```

#### 4.4 ExcelHelper

The environment specific excel sheet name should be updated in the properties file <Br>
E.g. File name in dev.properties:
```
TestDataExcel=DevTestDataExcel.xlsx
```

The location should be **\src\test\resources\testData**

The first column of data excel should have the scenario title. The framework will automatically match the scenario in the feature file & excel file(Scenario title is the unique key) and get the data based on the below details provided during run time

```
String val = ExcelHelper.getData(<worksheet>, <columnName>);
```
The user can also store values to the excel sheet using below method:
```
ExcelHelper.setData(<worksheet>, <columnName>, <dataToBeStored>);
```
Example Excel sheet format(First column containing scenario title is the primary key):

| Scenario  |  DeviceID           | Device Name                                                      |
| -------- | -----------------------| ------------------------------------------------------------ |
| As a customer, I want to launch a device     | Sample_device_1              | Sample-Device-1 |

For scenario Outlines which is having multiple set of data, which is similar to the below sample feature file
```
Feature: Product Journey Engine
  Scenario Outline: As a customer, I want to launch multiple devices 
    Given the application "DeviceManager"
    When the user login into device manager application
    Then launch the devide with <DeviceID>
  
  Example:
    | DeviceID        |
    | sample_device_1 |
    | sample_device_2 |
    | sample_device_3 |
```
Then below methods can be used to get and set data:
```
String val = ExcelHelper.getSubScenarioData(<worksheet>, <columnName>, <secondaryKey>);
ExcelHelper.getSubScenarioData(<worksheet>, <columnName>, <secondaryKey>, <dataToBeStored>);
```
- The first column cointaining scenario title in the excel file will be the primary key
- The second column in the excel sheet will be the secondary key

Example Excel sheet format:

| Scenario  |  DeviceID           | Device Name                                                      |
| -------- | -----------------------| ------------------------------------------------------------ |
| As a customer, I want to launch multiple devices     | sample_device_1              | Sample-Device-1 |
|                                                      | sample_device_2              | Sample-Device-2 |
|                                                      | sample_device_3              | Sample-Device-3 |

### 5. Quick Checklist to Trigger Tests
#### 5.1 Local execution setup

Please check below parameters are having required values to start the run in local environment

###### DEFAULT SETTINGS######

defaultEnvironment=dev/test1/test2/int

driverStack=chrome

webDriverManager=true/false

serverType=local

multipleDataFiles=false

generateAWSCredentials=false

applicationName=

###### Backend Validations. Please enable required backend validations 
###### Please set false for below three parameters, if only UI validations are required
enableBackendValidations=true/false<BR>
enableBranchDetailsValidations=true/false<BR>
enableTandTValidations=true/false<BR>
enableDespatchValidations=true/false<BR>
enableCFSValidation=true/false<BR>
enableCredenceValidation=true/false<BR>
checkForAdditionalCredenceEntries=true/false<BR>

###### Enable below parameter to log DB Data to CSV<BR>
DBCSVLogging=true<BR>

###### Zephyr Scale Params<BR>
###### Set false, if Zephyr update is not required. E.g.: debugging in local machines<BR>
enableZephyrExecution=true<BR>
zephyrBaseURI=https://api.zephyrscale.smartbear.com/v2 <BR>
zephyrAuthorizationKey=<BR>
zephyrProjectKey=<BR>
testCycleFolderName=<BR>

###### Please enter folder ID and Cycle ID only for manual executions

###### Please leave them empty for cloud uninterrupted executions######

zephyrFolderId=<BR>
zephyrCycleID=<BR>

#### 5.2 Remote execution setup(Argo WorkFlow/Commandline/Terminal)<BR>

serverType=local<BR>
generateAWSCredentials=true <BR>

Please refer other parameters for local execution setup section<BR>
Make sure correct testNG file is configured for <testNG.suiteXmlFile> tag in pom.xml file<BR>

**Example command:**

**mvn clean test -Denv=""
-DgenerateAWSCredentials="true" -DserverType="grid"
-DgridURL=""
-Dusername=""
-Dpassword=""
-DapplicationName=""
-DtestSuiteXmlFileName=""
-DenableBackendValidations=""
-DenableTandTValidations=""
-DenableDespatchValidations=""
-DenableZephyrExecution=""
-DzephyrAuthorizationKey=""
-DzephyrProjectKey=""
-DtestCycleFolderName=""**