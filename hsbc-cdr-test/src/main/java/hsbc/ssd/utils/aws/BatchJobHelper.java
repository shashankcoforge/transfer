package hsbc.ssd.utils.aws;

import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.model.ContainerOverrides;
import com.amazonaws.services.batch.model.KeyValuePair;
import com.amazonaws.services.batch.model.SubmitJobRequest;
import com.amazonaws.services.batch.model.SubmitJobResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class BatchJobHelper {

    public void submitBatchJOb(String jobName, String jobDescriptionARN, String jobQueueARN, boolean wait) throws InterruptedException {
        if(wait) TimeUnit.MINUTES.sleep(6);
        AWSBatch client = new AWSBase().batchClient();
        SubmitJobRequest request = new SubmitJobRequest().withJobName(jobName+"-test-automation-"+new SimpleDateFormat("ddMMyyHHmmss").format(new Date())).withJobQueue(jobQueueARN).withJobDefinition(jobDescriptionARN);
        SubmitJobResult response = client.submitJob(request);
        System.out.println(response);
    }

    public void submitBatchJObWithEnvVariables(String jobName, String fadcodes, String jobDescriptionARN, String jobQueueARN, boolean wait) throws InterruptedException {
        if(wait) TimeUnit.MINUTES.sleep(6);
        ContainerOverrides containerOverrides = new ContainerOverrides().withEnvironment(new KeyValuePair().withName("APP_FADCODE").withValue(fadcodes), new KeyValuePair().withName("APP_BUSINESSDATE").withValue(new SimpleDateFormat("ddMMyyHH").format(new Date())));
        AWSBatch client = new AWSBase().batchClient();
        SubmitJobRequest request = new SubmitJobRequest().withJobName(jobName+"-test-automation-"+new SimpleDateFormat("ddMMyyHHmmss").format(new Date())).withJobQueue(jobQueueARN).withJobDefinition(jobDescriptionARN).withContainerOverrides(containerOverrides);
        SubmitJobResult response = client.submitJob(request);
        System.out.println(response);
    }

}
