package hsbc.qs.utils.zephyr.ZephyrPOJO;

public class TestExecutionPOJO {

    private String projectKey;
    private String testCaseKey;
    private String testCycleKey;
    private String statusName;
    private String environmentName;
    private String comment;
    private TestScriptResults[] testScriptResults;

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getTestCaseKey() {
        return testCaseKey;
    }

    public void setTestCaseKey(String testCaseKey) {
        this.testCaseKey = testCaseKey;
    }

    public String getTestCycleKey() {
        return testCycleKey;
    }

    public void setTestCycleKey(String testCycleKey) {
        this.testCycleKey = testCycleKey;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setExecStatus(String statusName) {
        this.statusName = statusName;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public TestScriptResults[] getTestScriptResults() {
        return testScriptResults;
    }

    public void setTestScriptResults(TestScriptResults[] testScriptResults) {
        this.testScriptResults = testScriptResults;
    }
}
