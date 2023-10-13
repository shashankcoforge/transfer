package hsbc.ssd.utils.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import hsbc.ssd.utils.helper.Helper;
import hsbc.ssd.utils.helper.PropertyHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class S3BucketHelper extends AWSBase {

    protected Logger log = LogManager.getLogger(this.getClass().getName());

    public void downloadFileFromBucket(String stream, String fileType, String bucketName, String prefix, String dirPath) {
        try {
            TimeUnit.MINUTES.sleep(3);
            Files.createDirectories(Paths.get(dirPath));
            String actualFilePath = dirPath+fileType;

            TimeUnit.SECONDS.sleep(30);
            AmazonS3 s3 = setS3Credentials();
            ObjectListing listing = s3.listObjects(bucketName, prefix);
            List<S3ObjectSummary> summaries = listing.getObjectSummaries();
            Date referenceDate = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));
            String fileKey = null;
            while (listing.isTruncated()) {
                listing = s3.listNextBatchOfObjects (listing);
                summaries.addAll (listing.getObjectSummaries());
            }

            for (S3ObjectSummary summary: summaries){
                if(summary.getLastModified().after(referenceDate) && !summary.getKey().contains("ftp.")){
                    referenceDate = summary.getLastModified();
                    fileKey = summary.getKey();
                }
            }
            S3Object s3Object = s3.getObject(bucketName, fileKey);
            InputStream s3inputStream = s3Object.getObjectContent();
            FileOutputStream fileOutputStream = null;
            fileOutputStream = new FileOutputStream(new File(actualFilePath));
            byte[] readBuf = new byte[1024];
            int readLen = 0;
            while ((readLen = s3inputStream.read(readBuf)) > 0) {
                fileOutputStream.write(readBuf, 0, readLen);
            }
            s3inputStream.close();
            fileOutputStream.close();
        } catch (SdkClientException | IOException | InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void deletesFileFromBucket(String stream, String bucketName) {
        try {
            AmazonS3 s3 = setS3Credentials();
            String key = null;
            if(stream.trim().toLowerCase().equals("pudo")) {
                key = LocalDateTime.now(ZoneOffset.UTC).minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
                for (S3ObjectSummary file : s3.listObjects(bucketName, key).getObjectSummaries()) {
                    s3.deleteObject(bucketName, file.getKey());
                }
            }
            else if(stream.trim().toLowerCase().equals("parquet")) {

                System.out.print("bucketName: "+ bucketName);
                key = PropertyHelper.getEnvSpecificAppParameters("PARQUETBUCKETFOLDER") + LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                System.out.print("key: "+ key);
                for (S3ObjectSummary file : s3.listObjects(bucketName, key).getObjectSummaries()) {
                    s3.deleteObject(bucketName, file.getKey());
                }
            }
            else Assert.fail("Incorrect stream: "+stream);
        }
        catch (SdkClientException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void uploadsFileToBucket(String fileType, String bucketName, int scenarioNumber) {
        try {
            String key = null;
            File file = null;
            if(fileType.trim().equals("aggregation")) {
                key = LocalDateTime.now(ZoneOffset.UTC).minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM")) + "/pudo" + scenarioNumber + ".csv";
                file = new File("src/test/resources/testdata/inputfiles/pudo/pudo" + scenarioNumber + ".csv");
            }
            else if(fileType.trim().equals("correction")) {
                key = LocalDateTime.now(ZoneOffset.UTC).minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM")) + "/PUDO" + LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "001ERR.csv";
                file = new File("src/test/resources/testdata/inputfiles/pudo/pudo" + scenarioNumber + ".csv");
            }
            else Assert.fail("Incorrect file type");
            AmazonS3 s3 = setS3Credentials();

            // Upload a text string as a new object
            s3.putObject(bucketName, key, file);

        } catch (AwsServiceException | SdkClientException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * This method uploads .CIF file to S3 Bucket - PODG Functionality
     */
    public void uploadFileToBucket(String fileName, String clientName) {
        try {
            AmazonS3 s3Client = setS3Credentials();
            String bucketName = PropertyHelper.getEnvSpecificAppParameters("Sftp_bucket") + clientName;
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, new File(PropertyHelper.getEnvSpecificAppParameters("Testdata_path") + fileName));
            s3Client.putObject(request);
        } catch (AwsServiceException | SdkClientException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void downloadS3BucketFiles(String fileName, String bucketName, String clientName, String fileKey) {
        try {
            AmazonS3 s3Client = setS3Credentials();
            String  actualFilePath= Helper.getAbsolutePath() + Helper.getFileSeparator()+"aws"+ Helper.getFileSeparator()+"PODG"+Helper.getFileSeparator() + bucketName+ Helper.getFileSeparator();
            Files.createDirectories(Paths.get(actualFilePath));
            actualFilePath = actualFilePath + fileName;
            /**Checking whether the file is present in the bucket**/
            boolean result1 = s3Client.doesObjectExist(bucketName, fileKey);
            System.out.println("the fileKey is " +fileKey);
            System.out.println("The file " + fileName + " you are checking in the " + bucketName + " bucket "+ clientName +" folder is available? " + result1);

            /**Getting the content in the file**/
            if(result1) {
                S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, fileKey));
                InputStream in = new BufferedInputStream(s3Object.getObjectContent());
                FileOutputStream fileOutputStream = null;
                fileOutputStream = new FileOutputStream(new File(actualFilePath));
                byte[] readBuf = new byte[1024];
                int readLen = 0;
                while ((readLen = in.read(readBuf)) > 0) {
                    fileOutputStream.write(readBuf, 0, readLen);
                }
                in.close();
                fileOutputStream.close();
            }
        } catch (SdkClientException | IOException e) {
            Assert.fail(e.getMessage());
        }

    }

}
