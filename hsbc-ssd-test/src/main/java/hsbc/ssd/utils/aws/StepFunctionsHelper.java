package hsbc.ssd.utils.aws;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.model.ListExecutionsResult;
import hsbc.ssd.utils.helper.Helper;
import hsbc.ssd.utils.helper.JsonHelper;
import hsbc.ssd.utils.helper.PropertyHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import hsbc.ssd.utils.Constants;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sfn.SfnClient;
import com.amazonaws.services.stepfunctions.model.ExecutionListItem;
import com.amazonaws.services.stepfunctions.model.ListExecutionsRequest;
import software.amazon.awssdk.services.sfn.model.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class StepFunctionsHelper extends AWSBase {

    protected Logger log = LogManager.getLogger(this.getClass().getName());

    public void triggersStepFunction(String stream, boolean wait, boolean withPayload) {
        try {
            if(wait) TimeUnit.MINUTES.sleep(6);
            SfnClient sfnClient = sfnClientBuilder();
            String stateMachineArn = PropertyHelper.getEnvSpecificAppParameters(stream+"_ARN");
            System.out.println("Step Function ARN : "+PropertyHelper.getEnvSpecificAppParameters(stream+"_ARN"));
            if(withPayload) {
                String payloadPath = Constants.TESTDATAPATH+ "aws"+ Helper.getFileSeparator()+stream.trim().toLowerCase()+Helper.getFileSeparator()+"payload.json";
                String json =  JsonHelper.getJSONString(payloadPath);
                StartExecutionRequest executionRequest = StartExecutionRequest.builder()
                        .input(json)
                        .stateMachineArn(stateMachineArn)
                        .build();
                StartExecutionResponse response = sfnClient.startExecution(executionRequest);
                log.info("Executed the "+stream+" with ARN: " + response.executionArn());
            }else {
                StartExecutionRequest executionRequest = StartExecutionRequest.builder()
                        .stateMachineArn(stateMachineArn)
                        .build();
                StartExecutionResponse response = sfnClient.startExecution(executionRequest);
                log.info("Executed the "+stream+" with ARN: " + response.executionArn());
            }
            sfnClient.close();
        } catch (AwsServiceException | SdkClientException | InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }

    public List<ExecutionListItem> getExecutionList(String stepFunctionARN, int... maxResults){
        List<ExecutionListItem> executions = null;
        try {
            AWSStepFunctions sfnClient = stepFunctionClientBuilder();

            ListExecutionsRequest listRequest = new ListExecutionsRequest()
                    .withMaxResults((maxResults.length > 0? maxResults[0] : 10))
                    .withStateMachineArn(stepFunctionARN);

            ListExecutionsResult listExecutionsResult = sfnClient
                    .listExecutions(listRequest);

            executions = listExecutionsResult
                    .getExecutions();

        } catch (SfnException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        return executions;
    }

    public List<HistoryEvent> getExeHistory(String exeARN, int... maxResults) {

        List<HistoryEvent> events = null;

        try {
            SfnClient sfnClient = sfnClientBuilder();

            GetExecutionHistoryRequest historyRequest = GetExecutionHistoryRequest.builder()
                    .executionArn(exeARN)
                    .maxResults((maxResults.length > 0? maxResults[0] : 10))
                    .build();

            GetExecutionHistoryResponse historyResponse = sfnClient.getExecutionHistory(historyRequest);
            events = historyResponse.events();

        } catch (SfnException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        return events;
    }

}
