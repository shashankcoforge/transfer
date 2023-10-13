package hsbc.ssd.utils.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.batch.AWSBatch;
import com.amazonaws.services.batch.AWSBatchClientBuilder;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import hsbc.ssd.utils.helper.PropertyHelper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class AWSBase {

    public Map<String, String> login(String email, String password) {
        AWSCognitoIdentityProvider client = AWSCognitoIdentityProviderClientBuilder.defaultClient();
        try {
            Map<String, String> authParams = new LinkedHashMap<String, String>() {
                {
                    put("USERNAME", email);
                    put("PASSWORD", password);
                }
            };

            AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                    .withAuthFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                    .withUserPoolId(PropertyHelper.getEnvSpecificAppParameters("userPoolID"))
                    .withClientId(PropertyHelper.getEnvSpecificAppParameters("clientID"))
                    .withAuthParameters(authParams);

            AdminInitiateAuthResult authResult = client.adminInitiateAuth(authRequest);
            AuthenticationResultType resultType = authResult.getAuthenticationResult();
            System.setProperty("aws.accessKeyId", resultType.getIdToken());
            System.setProperty("aws.secretAccessKey", resultType.getAccessToken());
            System.setProperty("aws.sessionToken", resultType.getRefreshToken());

            return new LinkedHashMap<String, String>() {
                {
                    put("idToken", System.getProperty("aws.accessKeyId"));
                    put("accessToken", System.getProperty("aws.secretAccessKey"));
                    put("refreshToken", System.getProperty("aws.sessionToken"));
                    put("message", "Successfully login");
                }
            };
        } catch (Exception e) {
            System.out.println("Exception Occurred during changing the user password");
            e.printStackTrace();
        } finally {
            client.shutdown();
        }
        return new LinkedHashMap<String, String>() {
            {
            }
        };
    }

    public String getJWTToken(String deviceID, String password) {
        Map<String, String> authParams = new LinkedHashMap<String, String>() {
            {
                put("USERNAME", deviceID);
                put("PASSWORD", password);
            }
        };
        final AWSCognitoIdentityProvider client = getAWSCognitoIdentityClient();
        try {

            final AdminSetUserPasswordRequest adminSetUserPasswordRequest = new AdminSetUserPasswordRequest()
                    .withUserPoolId(PropertyHelper.getEnvSpecificAppParameters("userPoolID"))
                    .withUsername(deviceID)
                    .withPassword(password)
                    .withPermanent(true);

            client.adminSetUserPassword(adminSetUserPasswordRequest);

            AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                    //.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .withAuthFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                    .withUserPoolId(PropertyHelper.getEnvSpecificAppParameters("userPoolID"))
                    .withClientId(PropertyHelper.getEnvSpecificAppParameters("clientID"))
                    .withAuthParameters(authParams);
            AdminInitiateAuthResult authResult = client.adminInitiateAuth(authRequest);
            AuthenticationResultType resultType = authResult.getAuthenticationResult();
            return resultType.getIdToken();
        } catch (final Exception e) {
            System.out.println("Exception Occured during changing the user password");
            e.printStackTrace();
        } finally {
            client.shutdown();
        }
        return "";
    }

    public String getJWTToken_UsingCognitoSRPAuth(String username, String password, String clientID,
                                                  String userPoolID) {
        return new SRPAuthenticationHelper(userPoolID, clientID).PerformSRPAuthentication(getAWSCognitoIdentityClient(),
                username, password);
    }

    public void resetPasswordForDevice(String deviceID, String password) {
        try {
            final AWSCognitoIdentityProvider client = getAWSCognitoIdentityClient();
            final AdminSetUserPasswordRequest adminSetUserPasswordRequest = new AdminSetUserPasswordRequest()
                    .withUserPoolId(PropertyHelper.getEnvSpecificAppParameters("userPoolID"))
                    .withUsername(deviceID)
                    .withPassword(password)
                    .withPermanent(true);

            client.adminSetUserPassword(adminSetUserPasswordRequest);
        } catch (Exception e) {
            System.out.println("Unable to reset password for the device : " + deviceID);
            e.printStackTrace();
        }
    }

    public static AWSCognitoIdentityProvider getAWSCognitoIdentityClient() {
        String generateAWSCredentials = (PropertyHelper.getVariable("generateAWSCredentials") != null
                && (!PropertyHelper.getVariable("generateAWSCredentials").equalsIgnoreCase("")))
                ? PropertyHelper.getVariable("generateAWSCredentials")
                : PropertyHelper.getDefaultProperty("generateAWSCredentials");
        String getAWSCredentialsFromGitHubAction = (PropertyHelper
                .getVariable("getAWSCredentialsFromGitHubAction") != null
                && (!PropertyHelper.getVariable("getAWSCredentialsFromGitHubAction").equalsIgnoreCase("")))
                ? PropertyHelper.getVariable("getAWSCredentialsFromGitHubAction")
                : PropertyHelper.getDefaultProperty("getAWSCredentialsFromGitHubAction");

        if (getAWSCredentialsFromGitHubAction.equalsIgnoreCase("true")) {
            System.out.println("aws.* will be picked from AWS official GitHub action");
            // Do Nothing, as aws.* will be generated from AWS official GitHub action
            // configured with security recommended OIDC connection
            System.setProperty("aws.accessKeyId", System.getProperty("aws.accessKeyId"));
            System.setProperty("aws.secretKey", System.getProperty("aws.secretAccessKey"));
            System.setProperty("aws.sessionToken", System.getProperty("aws.sessionToken"));
        } else if (generateAWSCredentials.equalsIgnoreCase("true")) {

            String roleARN = PropertyHelper.getEnvSpecificAppParameters("roleARN");
            String roleSessionName = "test-automation" + new SimpleDateFormat("ddMMyyHHmmss").format(new Date());
            String externalID = PropertyHelper.getEnvSpecificAppParameters("externalID");

            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                    .withRegion(Regions.EU_WEST_2)
                    .build();

            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn(roleARN)
                    .withRoleSessionName(roleSessionName)
                    .withExternalId(externalID);

            AssumeRoleResult roleResponse = stsClient.assumeRole(roleRequest);
            Credentials sessionCredentials = roleResponse.getCredentials();

            // System.out.println("AccessKeyId: "+sessionCredentials.getAccessKeyId());
            // System.out.println("SecretAccessKey:"+sessionCredentials.getSecretAccessKey());
            // System.out.println("SessionToken: "+sessionCredentials.getSessionToken());

            System.setProperty("aws.accessKeyId", sessionCredentials.getAccessKeyId());
            System.setProperty("aws.secretKey", sessionCredentials.getSecretAccessKey());
            System.setProperty("aws.sessionToken", sessionCredentials.getSessionToken());
        } else {
            String awsAccessKeyID = (PropertyHelper.getVariable("AWS_ACCESS_KEY_ID") != null
                    && (!PropertyHelper.getVariable("AWS_ACCESS_KEY_ID").equalsIgnoreCase("")))
                    ? PropertyHelper.getVariable("AWS_ACCESS_KEY_ID")
                    : PropertyHelper.getEnvSpecificAppParameters("AWS_ACCESS_KEY_ID");
            String awsSecretAccessKey = (PropertyHelper.getVariable("AWS_SECRET_ACCESS_KEY") != null
                    &&
                    (!PropertyHelper.getVariable("AWS_SECRET_ACCESS_KEY").equalsIgnoreCase("")))
                    ? PropertyHelper.getVariable("AWS_SECRET_ACCESS_KEY")
                    : PropertyHelper.getEnvSpecificAppParameters("AWS_SECRET_ACCESS_KEY");
            String awsSessionToken = (PropertyHelper.getVariable("AWS_SESSION_TOKEN") != null
                    && (!PropertyHelper.getVariable("AWS_SESSION_TOKEN").equalsIgnoreCase("")))
                    ? PropertyHelper.getVariable("AWS_SESSION_TOKEN")
                    : PropertyHelper.getEnvSpecificAppParameters("AWS_SESSION_TOKEN");

            System.setProperty("aws.accessKeyId", awsAccessKeyID);
            System.setProperty("aws.secretKey", awsSecretAccessKey);
            System.setProperty("aws.sessionToken", awsSessionToken);
        }
        return AWSCognitoIdentityProviderClientBuilder.standard().withRegion(Regions.EU_WEST_2)
                .withCredentials(new SystemPropertiesCredentialsProvider()).build();
    }

    public Map<String, String> refreshToken(String refreshToken) {
        Map<String, String> authParams = new LinkedHashMap<String, String>() {
            {
                put("REFRESH_TOKEN", refreshToken);
            }
        };
        InitiateAuthRequest authRequest = new InitiateAuthRequest()
                .withAuthFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .withClientId("15mau74po0goql54lca0ds05vs")
                .withAuthParameters(authParams);
        AWSCognitoIdentityProvider cognitoClient = AWSCognitoIdentityProviderClientBuilder.defaultClient();
        InitiateAuthResult authResult = cognitoClient.initiateAuth(authRequest);
        AuthenticationResultType resultType = authResult.getAuthenticationResult();
        return new LinkedHashMap<String, String>() {
            {
                put("idToken", resultType.getIdToken());
                put("accessToken", resultType.getAccessToken());
                put("message", "Successfully login");
            }
        };
    }

    public String getUserJWTToken(String userId, String password) {
        Map<String, String> authParams = new LinkedHashMap<String, String>() {{
            put("USERNAME", userId);
            put("PASSWORD", password);
        }};
        final AWSCognitoIdentityProvider client = getAWSCognitoIdentityClient();
        try {

            final AdminSetUserPasswordRequest adminSetUserPasswordRequest = new AdminSetUserPasswordRequest()
                    .withUserPoolId(PropertyHelper.getEnvSpecificAppParameters("UserPooluserId"))
                    .withUsername(userId) //CT-Device1_forgeRockSmartID
                    .withPassword(password)
                    .withPermanent(true);

            client.adminSetUserPassword(adminSetUserPasswordRequest);

            AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                    //.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .withAuthFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                    .withUserPoolId(PropertyHelper.getEnvSpecificAppParameters("UserPooluserId"))
                    .withClientId(PropertyHelper.getEnvSpecificAppParameters("userClientId"))
                    .withAuthParameters(authParams);
            AdminInitiateAuthResult authResult = client.adminInitiateAuth(authRequest);
            AuthenticationResultType resultType = authResult.getAuthenticationResult();
            return resultType.getIdToken();
        } catch (final Exception e) {
            System.out.println("Exception Occured during changing the user password");
            e.printStackTrace();
        } finally {
            client.shutdown();
        }
        return "";
    }

    public BasicSessionCredentials connectToAWS() {

        String generateAWSCredentials = (PropertyHelper.getVariable("generateAWSCredentials") != null
                && (!PropertyHelper.getVariable("generateAWSCredentials").equalsIgnoreCase("")))
                ? PropertyHelper.getVariable("generateAWSCredentials")
                : PropertyHelper.getDefaultProperty("generateAWSCredentials");
        String getAWSCredentialsFromGitHubAction = (PropertyHelper
                .getVariable("getAWSCredentialsFromGitHubAction") != null
                && (!PropertyHelper.getVariable("getAWSCredentialsFromGitHubAction").equalsIgnoreCase("")))
                ? PropertyHelper.getVariable("getAWSCredentialsFromGitHubAction")
                : PropertyHelper.getDefaultProperty("getAWSCredentialsFromGitHubAction");

        if (getAWSCredentialsFromGitHubAction.equalsIgnoreCase("true")) {
            System.out.println("aws.* will be picked from AWS official GitHub action");
            // Do Nothing, as aws.* will be generated from AWS official GitHub action
            // configured with security recommended OIDC connection
            System.setProperty("aws.accessKeyId", System.getProperty("aws.accessKeyId"));
            System.setProperty("aws.secretKey", System.getProperty("aws.secretAccessKey"));
            System.setProperty("aws.sessionToken", System.getProperty("aws.sessionToken"));

            return new BasicSessionCredentials(System.getProperty("aws.accessKeyId"),
                    System.getProperty("aws.secretKey"),
                    System.getProperty("aws.sessionToken"));
        } else if (generateAWSCredentials.equalsIgnoreCase("true")) {

            String roleARN = PropertyHelper.getEnvSpecificAppParameters("roleARN");
            String roleSessionName = "test-automation" + new
                    SimpleDateFormat("ddMMyyHHmmss").format(new Date());
            String externalID = PropertyHelper.getEnvSpecificAppParameters("externalID");

            AWSSecurityTokenService stsClient =
                    AWSSecurityTokenServiceClientBuilder.standard()
                            .withRegion(Regions.EU_WEST_2)
                            .build();

            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn(roleARN)
                    .withRoleSessionName(roleSessionName)
                    .withExternalId(externalID);

            AssumeRoleResult roleResponse = stsClient.assumeRole(roleRequest);
            Credentials sessionCredentials = roleResponse.getCredentials();

            System.out.println("AccessKeyId: " + sessionCredentials.getAccessKeyId());
            System.out.println("SecretAccessKey:" + sessionCredentials.getSecretAccessKey());
            System.out.println("SessionToken: " + sessionCredentials.getSessionToken());

            System.setProperty("aws.accessKeyId", sessionCredentials.getAccessKeyId());
            System.setProperty("aws.secretKey", sessionCredentials.getSecretAccessKey());
            System.setProperty("aws.sessionToken", sessionCredentials.getSessionToken());

            return new BasicSessionCredentials(System.getProperty("aws.accessKeyId"),
                    System.getProperty("aws.secretKey"),
                    System.getProperty("aws.sessionToken"));
        } else {

            System.setProperty("aws.accessKeyId", PropertyHelper.getEnvSpecificAppParameters("AWS_ACCESS_KEY_ID"));
            System.setProperty("aws.secretKey", PropertyHelper.getEnvSpecificAppParameters("AWS_SECRET_ACCESS_KEY"));
            System.setProperty("aws.sessionToken", PropertyHelper.getEnvSpecificAppParameters("AWS_SESSION_TOKEN"));
            return new BasicSessionCredentials(System.getProperty("aws.accessKeyId"),
                    System.getProperty("aws.secretKey"),
                    System.getProperty("aws.sessionToken"));
        }
    }

    public BasicSessionCredentials connectToAWSTandT() {
        String generateAWSCredentials = (PropertyHelper.getVariable("generateAWSCredentials") != null
                && (!PropertyHelper.getVariable("generateAWSCredentials").equalsIgnoreCase("")))
                ? PropertyHelper.getVariable("generateAWSCredentials")
                : PropertyHelper.getDefaultProperty("generateAWSCredentials");
        String getAWSCredentialsFromGitHubAction = (PropertyHelper
                .getVariable("getAWSCredentialsFromGitHubAction") != null
                && (!PropertyHelper.getVariable("getAWSCredentialsFromGitHubAction").equalsIgnoreCase("")))
                ? PropertyHelper.getVariable("getAWSCredentialsFromGitHubAction")
                : PropertyHelper.getDefaultProperty("getAWSCredentialsFromGitHubAction");

        if (getAWSCredentialsFromGitHubAction.equalsIgnoreCase("true")) {
            System.out.println("aws.* will be picked from AWS official GitHub action");
            // Do Nothing, as aws.* will be generated from AWS official GitHub action
            // configured with security recommended OIDC connection
            System.setProperty("aws.accessKeyId", System.getProperty("aws.accessKeyId"));
            System.setProperty("aws.secretKey", System.getProperty("aws.secretAccessKey"));
            System.setProperty("aws.sessionToken", System.getProperty("aws.sessionToken"));

            return new BasicSessionCredentials(System.getProperty("aws.accessKeyId"),
                    System.getProperty("aws.secretKey"),
                    System.getProperty("aws.sessionToken"));
        } else if (generateAWSCredentials.equalsIgnoreCase("true")) {

            String roleARN = PropertyHelper.getEnvSpecificAppParameters("roleARN");
            String roleSessionName = "test-automation" + new SimpleDateFormat("ddMMyyHHmmss").format(new Date());
            String externalID = PropertyHelper.getEnvSpecificAppParameters("externalID");

            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                    .withRegion(Regions.EU_WEST_2)
                    .build();

            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn(roleARN)
                    .withRoleSessionName(roleSessionName)
                    .withExternalId(externalID);

            AssumeRoleResult roleResponse = stsClient.assumeRole(roleRequest);
            Credentials sessionCredentials = roleResponse.getCredentials();

            System.out.println("AccessKeyId: " + sessionCredentials.getAccessKeyId());
            System.out.println("SecretAccessKey: " + sessionCredentials.getSecretAccessKey());
            System.out.println("SessionToken: " + sessionCredentials.getSessionToken());

            System.setProperty("aws.accessKeyId", sessionCredentials.getAccessKeyId());
            System.setProperty("aws.secretKey", sessionCredentials.getSecretAccessKey());
            System.setProperty("aws.sessionToken", sessionCredentials.getSessionToken());
            return new BasicSessionCredentials(sessionCredentials.getAccessKeyId(),
                    sessionCredentials.getSecretAccessKey(),
                    sessionCredentials.getSessionToken());
        } else {
            System.setProperty("aws.accessKeyId", PropertyHelper.getEnvSpecificAppParameters("TT_AWS_ACCESS_KEY_ID"));
            System.setProperty("aws.secretKey", PropertyHelper.getEnvSpecificAppParameters("TT_AWS_SECRET_ACCESS_KEY"));
            System.setProperty("aws.sessionToken", PropertyHelper.getEnvSpecificAppParameters("TT_AWS_SESSION_TOKEN"));
            return new BasicSessionCredentials(PropertyHelper.getEnvSpecificAppParameters("TT_AWS_ACCESS_KEY_ID"),
                    PropertyHelper.getEnvSpecificAppParameters("TT_AWS_SECRET_ACCESS_KEY"),
                    PropertyHelper.getEnvSpecificAppParameters("TT_AWS_SESSION_TOKEN"));
        }
    }

    public void setConfigProperties() {
        String generateAWSCredentials = (PropertyHelper.getVariable("generateAWSCredentials") != null
                && (!PropertyHelper.getVariable("generateAWSCredentials").equalsIgnoreCase("")))
                ? PropertyHelper.getVariable("generateAWSCredentials")
                : PropertyHelper.getDefaultProperty("generateAWSCredentials");
        String getAWSCredentialsFromGitHubAction = (PropertyHelper
                .getVariable("getAWSCredentialsFromGitHubAction") != null
                && (!PropertyHelper.getVariable("getAWSCredentialsFromGitHubAction").equalsIgnoreCase("")))
                ? PropertyHelper.getVariable("getAWSCredentialsFromGitHubAction")
                : PropertyHelper.getDefaultProperty("getAWSCredentialsFromGitHubAction");

        if (getAWSCredentialsFromGitHubAction.equalsIgnoreCase("true")) {
            System.out.println("aws.* will be picked from AWS official GitHub action - called from setConfigProperties()");
            // Do Nothing, as aws.* will be generated from AWS official GitHub action
            // configured with security recommended OIDC connection
            System.setProperty("aws.accessKeyId", System.getProperty("aws.accessKeyId"));
            System.setProperty("aws.secretKey", System.getProperty("aws.secretAccessKey"));
            System.setProperty("aws.sessionToken", System.getProperty("aws.sessionToken"));
        } else if (generateAWSCredentials.equalsIgnoreCase("true")) {

            String roleARN = PropertyHelper.getEnvSpecificAppParameters("roleARN");
            String roleSessionName = "test-automation" + new SimpleDateFormat("ddMMyyHHmmss").format(new Date());
            String externalID = PropertyHelper.getEnvSpecificAppParameters("externalID");

            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                    .withRegion(Regions.EU_WEST_2)
                    .build();

            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn(roleARN)
                    .withRoleSessionName(roleSessionName)
                    .withExternalId(externalID);

            AssumeRoleResult roleResponse = stsClient.assumeRole(roleRequest);
            Credentials sessionCredentials = roleResponse.getCredentials();

            System.out.println("AccessKeyId: " + sessionCredentials.getAccessKeyId());
            System.out.println("SecretAccessKey: " + sessionCredentials.getSecretAccessKey());
            System.out.println("SessionToken: " + sessionCredentials.getSessionToken());

            System.setProperty("aws.accessKeyId", sessionCredentials.getAccessKeyId());
            System.setProperty("aws.secretKey", sessionCredentials.getSecretAccessKey());
            System.setProperty("aws.sessionToken", sessionCredentials.getSessionToken());
        } else {
            System.setProperty("aws.accessKeyId", PropertyHelper.getEnvSpecificAppParameters("AWS_ACCESS_KEY_ID"));
            System.setProperty("aws.secretKey", PropertyHelper.getEnvSpecificAppParameters("AWS_SECRET_ACCESS_KEY"));
            System.setProperty("aws.sessionToken", PropertyHelper.getEnvSpecificAppParameters("AWS_SESSION_TOKEN"));
        }
    }

    public AmazonS3 setS3Credentials() {
        return AmazonS3Client.builder().withCredentials(new AWSStaticCredentialsProvider(connectToAWS()))
                .withRegion(Regions.EU_WEST_2).build();
    }

    public SfnClient sfnClientBuilder() {
        setConfigProperties();
        return SfnClient.builder().region(Region.EU_WEST_2).build();
    }

    public AWSBatch batchClient() {
        setConfigProperties();
        return AWSBatchClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
    }

    public AWSStepFunctions stepFunctionClientBuilder() {
        return AWSStepFunctionsClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(connectToAWS()))
                .withRegion(Regions.EU_WEST_2).build();
    }

    public DynamoDB amazonDynamoDBConn() {
        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(connectToAWS()))
                .withRegion(Regions.EU_WEST_2).build();
        return new DynamoDB(amazonDynamoDB);
    }

    public DynamoDB amazonDynamoDBConnTandT() {
        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(connectToAWSTandT()))
                .withRegion(Regions.EU_WEST_2).build();
        return new DynamoDB(amazonDynamoDB);
    }

    public AmazonDynamoDB amazonDynamoDBClientBuilder() {
        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(connectToAWS()))
                .withRegion(Regions.EU_WEST_2).build();
        return amazonDynamoDB;
    }

}