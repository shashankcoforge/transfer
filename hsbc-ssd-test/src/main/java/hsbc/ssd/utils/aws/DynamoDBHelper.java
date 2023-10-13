package hsbc.ssd.utils.aws;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import hsbc.ssd.utils.selenium.TestParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DynamoDBHelper extends AWSBase {

    static Logger log = LogManager.getLogger(DynamoDB.class);

    public ItemCollection<QueryOutcome> getDataFromTable(String hkPrimaryKey, String tableName){
        ItemCollection<QueryOutcome> items = null;
        //try {
            DynamoDB dynamoDB = amazonDynamoDBConn();
            Table table = dynamoDB.getTable(tableName);

            QuerySpec spec = new QuerySpec()
                    .withKeyConditionExpression("HK = :partitionKey")
                    .withValueMap(new ValueMap()
                            .withString(":partitionKey", hkPrimaryKey));
            items = table.query(spec);
//        }catch(Exception e){
//            TestParameters.getInstance().sa().fail(e.getMessage()+" HK "+hkPrimaryKey+ " tableName "+tableName);
//            log.info(e.getMessage()+" HK "+hkPrimaryKey+ " tableName "+tableName);
//        }
        return items;
    }

    public ItemCollection<QueryOutcome> getDataFromTableForBackOffice(String hkPrimaryKey, String skSortKey, String tableName){
        ItemCollection<QueryOutcome> items = null;
        DynamoDB dynamoDB = amazonDynamoDBConn();
        Table table = dynamoDB.getTable(tableName);

        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("HK = :partitionKey AND SK = :sortKey")
                .withValueMap(new ValueMap()
                        .withString(":partitionKey", hkPrimaryKey)
                        .withString(":sortKey", skSortKey));
        items = table.query(spec);
        return items;
    }

    public ItemCollection<QueryOutcome> getDataFromTableTandT(String hkPrimaryKey, String tableName){
        ItemCollection<QueryOutcome> items = null;
        DynamoDB dynamoDB = amazonDynamoDBConnTandT();
        Table table = dynamoDB.getTable(tableName);

        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("transactionId = :partitionKey")
                .withValueMap(new ValueMap()
                        .withString(":partitionKey", hkPrimaryKey));
        items = table.query(spec);
        return items;
    }

    public void deleteDBData(String partitionKey, String tableName) {
        try {
            DynamoDB dynamoDB = amazonDynamoDBConn();
            Table table = dynamoDB.getTable(tableName);

            QuerySpec spec = new QuerySpec()
                    .withKeyConditionExpression("HK = :partitionKey")
                    .withValueMap(new ValueMap()
                            .withString(":partitionKey", partitionKey));

            ItemCollection<QueryOutcome> items = table.query(spec);

            Iterator<Item> iterator = items.iterator();
            Item item = null;
            while (iterator.hasNext()) {
                item = iterator.next();
                String sk = item.get("SK").toString();
                table.deleteItem("HK", partitionKey, "SK", sk);
            }
        }catch(Exception e){
            TestParameters.getInstance().sa().fail(partitionKey+ " ");
        }
    }


    public ItemCollection<ScanOutcome> fetchDBEntriesUsingTimeRange(String field, int from, int to) {
        DynamoDB dynamoDB = amazonDynamoDBConn();
        Table table = dynamoDB.getTable("spm-transactions");
        Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
        expressionAttributeValues.put(":from", from);
        expressionAttributeValues.put(":to", to);

        ItemCollection<ScanOutcome> items = table.scan(field+" BETWEEN :from AND :to",null,null, expressionAttributeValues);
        return items;
    }

    public ItemCollection<ScanOutcome> fetchDBEntriesUsingBrNd(String value) {
        DynamoDB dynamoDB = amazonDynamoDBConn();
        Table table = dynamoDB.getTable("spm-transactions");
        Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
        expressionAttributeValues.put(":brNd", value);

        ItemCollection<ScanOutcome> items = table.scan("HK = :brNd",null,null, expressionAttributeValues);
        return items;

    }

    public ItemCollection<ScanOutcome> fetchDBEntriesUsingBK(String value) {
        DynamoDB dynamoDB = amazonDynamoDBConn();
        Table table = dynamoDB.getTable("spm-transactions");
        Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
        expressionAttributeValues.put(":bk", value);

        ItemCollection<ScanOutcome> items = table.scan("HK = :bk",null,null, expressionAttributeValues);
        return items;

    }

    public ItemCollection<ScanOutcome> fetchDBEntriesForBasketID(String basketID) {
        DynamoDB dynamoDB = amazonDynamoDBConn();
        Table table = dynamoDB.getTable("spm-transactions");
        Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
        expressionAttributeValues.put(":basketID", basketID);

        ItemCollection<ScanOutcome> items = table.scan("basketID = :basketID",null,null, expressionAttributeValues);
        return items;

    }



}
