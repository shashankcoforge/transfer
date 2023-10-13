package hsbc.qs.modules.constants;

public enum PayloadConstants {
    SERVICE_NAME("serviceName"),
    SERVICE_ID("serviceId"),
    DESTINATION("destination"),
    ACCLINE_TYPE("accLineType"),
    ORIGINAL_REQ_AMOUNT("OriginalRequestedAmount"),

    AFTER_LAST_DESPATCH("afterLastDespatch"),
    PREPAID_AMOUNT("prepaidAmount"),
    BASKET_ID("basketID"),
    DESTINATION_AREA_ID("destinationAreaId"),
    CARRIER("carrier"),
    DEST("Dest"),
    UPU_TRACK_NO("UPUTrackingNumber"),
    T_T_TYPE("TandTType"),
    T_T_CARRIER("TandTCarrier"),
    INCIDENT_CODES("incidentCodes"),
    PARCEL_STATUS("parcelStatus"),
    CAPTURED_ID("capturedID"),
    SENDER_POST_CODE("SenderPostcode"),
    SENDER_NAME("SenderName"),
    SENDER_ADDRESS("SenderAddress"),
    NUMBER_OF_ITEMS("NumberOfItems"),
    TOKEN_ID("tokenIdentifier"),
    ITEM_ON_HAND("itemOnHand"),
    HK("HK"),

    SPM_TRANSACTION("spm-transactions"),

    ITEAM_IDS("ItemIDs"),
    DESPATCH("Despatch"),
    PAYLOAD_TYPE("PayloadType"),
    TANDT("TandT"),
    TR("TR"),
    TOKENS("Tokens"),
    BARCODE("barcode"),
    VALUE("value"),
    VALUE_IN_PENCE("valueInPence"),
    FORMAT("Format"),
    PRODUCT_TYPE_DB("Product Type_DB"),
    WEIGHT("weight"),
    PIPFORMAT("pipFormat"),

    TRACKING_NUMBER("TrackingNumber"),

    RETURNS("returns"),

    DESTINATION_ADD("destinationAddress"),
    FIRST_ADD_LINE("firstAddressLine"),
    COUNTRY("country"),
    REQUEST_ID("requestUDID"),
    TOWN("town"),
    INTERNATIONAL("international"),
    PREPAID_LINE("prePaidLine"),
    REQUEST_UUID("requestUDID"),
    VERSION_NUMBER("versionNumber"),
    CLIENT_ACCOUNT_NO("clientAccountNo"),
    CLIENT_SRV_CODE("clientSrvCode"),
    PAYMENT_CODE("paymentCode"),
    CUSTOMER_REF_NO("customerRefNumber"),
    CLIENT_ACC_NO("clientAccountNo"),
    AP_TRANS_REF("APTransactionReference"),
    TANDT_SVC_ID("TandTSvcID"),
    ITEAM_ID("itemID"),

    AMOUNT_REQUESTED("amountRequested"),

    HORIZON_TRANSACTION_ID("horizonTransactionID"),

    PAN("pan"),

    TRANSACTION_TYPE("transactionType"),

    FULFILMENT_ACTION("fulfilmentAction"),

    FULFILMENT_TYPE("fulfilmentState");

    private String value;
    private PayloadConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
