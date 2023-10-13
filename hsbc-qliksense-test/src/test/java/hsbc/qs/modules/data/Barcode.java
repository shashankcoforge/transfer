package hsbc.qs.modules.data;

import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import hsbc.qs.utils.helper.ExcelHelper;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Barcode {

    public String generateBarcode(String barcode, String... sheetAndRowNum){
        String newBarcode = "";
        if (barcode.startsWith("JGB")) {
            if (barcode.charAt(4) == '6') {
                newBarcode = generateJGB6Barcode(barcode);
            } else if (barcode.charAt(4) == '8') {
                if(sheetAndRowNum.length>0) {
                    newBarcode = generateJGB8Barcode(barcode, sheetAndRowNum[0].split(":")[0], Integer.parseInt(sheetAndRowNum[0].split(":")[1]));
                }else{
                    Assert.fail("Sheet name and row no with colon delimited is required to generate JGB8 barcodes");
                }
            } else {
                Assert.fail("Invalid barcode format: " + barcode);
            }
        } else if (barcode.startsWith("Label=")) {
            newBarcode = generateL2GBarcode(barcode);
        } else if (barcode.endsWith("GB") && barcode.length() == 13) {
            newBarcode = generate1DChecksumBarcode(barcode.substring(0, 2));
        } else if (!barcode.endsWith("GB")
                && Character.isAlphabetic(barcode.charAt(2))
                && Character.isAlphabetic(barcode.charAt(3)) && !Character.isAlphabetic(barcode.charAt(barcode.length()-1))) {
            newBarcode = generatePFCDBarcode(barcode.substring(0, 2));
        } else {
            Assert.fail("Invalid barcode format: " + barcode);
        }
        return newBarcode;
    }

    public String getReferenceNo(String barcode){
        String refNo = "";
        try {
            if (barcode.startsWith("JGB")) {
                if (barcode.charAt(4) == '6') {
                    refNo = getOldOneDBarcodeFromJGB6(barcode);
                } else if (barcode.charAt(4) == '8') {
                    String tempRefNo = barcode.substring(45, 58);
                    if (tempRefNo.contains("GB")) {
                        refNo = tempRefNo;
                    }
                } else {
                    Assert.fail("Invalid barcode format: " + barcode);
                }
            } else if (barcode.startsWith("Label=")) {
                final String[] midSplit = barcode.split("1DBarcode=");
                refNo = midSplit[1].split("GB\\|")[0] + "GB";
            } else if (barcode.endsWith("GB") && barcode.length() == 13) {
                refNo = barcode;
            } else if (!barcode.endsWith("GB")
                    && Character.isAlphabetic(barcode.charAt(2))
                    && Character.isAlphabetic(barcode.charAt(3)) && !Character.isAlphabetic(barcode.charAt(barcode.length() - 1))) {
                refNo = barcode;
            } else {
                Assert.fail("Invalid barcode format: " + barcode);
            }
        }catch(Exception e){ Assert.fail("Invalid barcode format: " + barcode+"\nException : "+ Arrays.toString(e.getStackTrace()));}
        return refNo;
    }

    public String generateJGB6Barcode(String jgbBarcode, String... oneDBarcode){
        StringBuilder barcode = new StringBuilder();
        StringBuilder tempBarcode = new StringBuilder();
        for(int i = 0, n = jgbBarcode.length(); i < n ; i++) {
            if(i>=24 && i<=31){
                if(Character.isAlphabetic(jgbBarcode.charAt(i)) && !Character.isWhitespace(jgbBarcode.charAt(i))){
                    tempBarcode.append((char) (new Random().nextInt(26) + 'A'));
                }else if(!Character.isWhitespace(jgbBarcode.charAt(i))){
                    tempBarcode.append((new Random().nextInt((9 - 1) + 1) + 1));
                }else{
                    tempBarcode.append(jgbBarcode.charAt(i));
                }
            }else{
                tempBarcode.append(jgbBarcode.charAt(i));
            }
        }

        String oldOneDBarcode = getOldOneDBarcodeFromJGB6(tempBarcode.toString());

        String prefix = tempBarcode.substring(0, 39);

        String[] midSplit = {};
        String returnType = "";
        String date = "";
        String returnTypesplit = jgbBarcode.split(oldOneDBarcode)[0];
        if(returnTypesplit.contains("TSN")){
            midSplit = jgbBarcode.split("TSN");
            String dateOfproduction =  LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("ddMMyy"));
            String dateOfShipment = Character.isWhitespace(midSplit[0].charAt(midSplit[0].length()-1)) ? "      ":LocalDate.now().plusWeeks(2).format(DateTimeFormatter.ofPattern("ddMMyy"));
            date= dateOfproduction+dateOfShipment;
            returnType = "TSN";
        }else if(returnTypesplit.contains("TSS")){
            midSplit = jgbBarcode.split("TSS");
            String dateOfproduction =  LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("ddMMyy"));
            String dateOfShipment = Character.isWhitespace(midSplit[0].charAt(midSplit[0].length()-1)) ? "      ":LocalDate.now().plusWeeks(2).format(DateTimeFormatter.ofPattern("ddMMyy"));
            date= dateOfproduction+dateOfShipment;
            returnType = "TSS";
        }else{
            Assert.fail("Unable to identify tracked return type. Invalid JGB 6 barcode format: " + jgbBarcode);
        }
        barcode.append(prefix)
                .append(date)
                .append(returnType)
                .append(jgbBarcode.split(returnType)[1].split(oldOneDBarcode)[0])
                .append(oneDBarcode.length > 0 ? oneDBarcode[0] : generate1DChecksumBarcode(oldOneDBarcode.substring(0, 2)))
                .append(tempBarcode.toString().split(oldOneDBarcode)[1]);

        return barcode.toString();
    }

    private String getOldOneDBarcodeFromJGB6(String jgbBarcode){

        String oldOneDBarcode = "";
        String oldOneDBarcodeTemp = "";
        if(jgbBarcode.contains("TSN")){
            oldOneDBarcodeTemp = jgbBarcode.split("TSN")[1];
        }else if(jgbBarcode.contains("TSS")){
            oldOneDBarcodeTemp = jgbBarcode.split("TSS")[1];
        }else{
            Assert.fail("Unable to identify tracked return type. Invalid JGB 6 barcode format: " + jgbBarcode);
        }

        if(oldOneDBarcodeTemp.contains("GB ")) {
            oldOneDBarcode = oldOneDBarcodeTemp.substring(4);
            oldOneDBarcode = oldOneDBarcode.split("GB")[0].replaceAll("\\s+", "")+"GB";
        }else{
            Assert.fail("Unable to identify 1 D barcode. Invalid JGB 6 barcode format: " + jgbBarcode);
        }

        return oldOneDBarcode;
    }

    public String generateJGB8Barcode(String jgbBarcode, String sheet, int rowNo){
        StringBuilder barcode = new StringBuilder();
        StringBuilder tempBarcode = new StringBuilder();
        try{
            for(int i = 0, n = jgbBarcode.length(); i < n ; i++) {
                if(i>=15 && i<=21){
                    if(Character.isAlphabetic(jgbBarcode.charAt(i)) && !Character.isWhitespace(jgbBarcode.charAt(i))){
                        tempBarcode.append((char) (new Random().nextInt(26) + 'A'));
                    }else if(!Character.isWhitespace(jgbBarcode.charAt(i))){
                        tempBarcode.append((new Random().nextInt((9 - 1) + 1) + 1));
                    }else{
                        tempBarcode.append(jgbBarcode.charAt(i));
                    }
                }
                else{
                    tempBarcode.append(jgbBarcode.charAt(i));
                }
            }

            String reqWeight = ExcelHelper.getField(sheet, "Weight", rowNo).trim().split("\\.")[0];
            String weight = StringUtils.repeat("0", 7-reqWeight.length())+reqWeight;
            String weightType = jgbBarcode.substring(28, 29);
            String reqPrice = ExcelHelper.getField(sheet, "Value", rowNo).trim().replaceAll("\\.", "");
            String price = StringUtils.repeat("0", 5-reqPrice.length())+reqPrice;
            String date = new SimpleDateFormat("ddMMyy").format(Calendar.getInstance().getTime());
            String oldOneDBarcode =  jgbBarcode.substring(45, 58);
            String productAndType = jgbBarcode.substring(40, 44);
            barcode.append(tempBarcode.toString().substring(0, 21))
                    .append(weight)
                    .append(weightType)
                    .append(price)
                    .append(date)
                    .append(productAndType);

            if(oldOneDBarcode.contains("GB")){
                barcode.append(jgbBarcode.split(oldOneDBarcode)[0].split(productAndType)[1])
                        .append(generate1DChecksumBarcode(oldOneDBarcode.substring(0,2)))
                        .append(tempBarcode.substring(58));
            }else{
                barcode.append(tempBarcode.substring(44));
            }
        }catch(Exception e){
            Assert.fail("Invalid barcode format: " + jgbBarcode);
        }
        return barcode.toString();
    }

    public String generateL2GBarcode(String L2GBarcode){
        StringBuilder barcode = new StringBuilder();
            String prefix = L2GBarcode.substring(0, 144);
            String jgbBarcode = prefix.split("QRCode=")[1];
            String oldOneDBarcode = getOldOneDBarcodeFromJGB6(jgbBarcode);
            String oneDBarcode = generate1DChecksumBarcode(oldOneDBarcode.substring(0, 2));
            String newJGBBarcode = generateJGB6Barcode(jgbBarcode, oneDBarcode);
            final String[] midSplit = L2GBarcode.substring(144).split("1DBarcode=");
            String oldOneDBarcode_2 = midSplit[1].substring(0, 13);
            if(oldOneDBarcode_2.substring(11).equals("GB")){
                barcode.append(prefix.split("QRCode=")[0])
                        .append("QRCode=")
                        .append(newJGBBarcode)
                        .append(midSplit[0])
                        .append("1DBarcode=")
                        .append(oneDBarcode)
                        .append("|")
                        .append(midSplit[1].split("GB\\|")[1]);
            }else{
                barcode.append(prefix.split("QRCode=")[0])
                        .append("QRCode=")
                        .append(newJGBBarcode)
                        .append(midSplit[0])
                        .append("1DBarcode=")
                        .append(generate1DBarcodeWithAlphabPrefix(oldOneDBarcode_2))
                        .append(midSplit[1].substring(13));
            }

        return barcode.toString();
    }

    public String generatePFCDBarcode(String prefix){
        StringBuilder barcode = new StringBuilder(prefix);
        Random rand = new Random();
        barcode.append((char) (rand.nextInt(26) + 'A'));
        barcode.append((char) (rand.nextInt(26) + 'A'));
        if(prefix.length()==2) {
            if(Character.isAlphabetic(prefix.charAt(0)) && Character.isAlphabetic(prefix.charAt(1))) {

                List<Integer> w = Arrays.asList(8, 6, 4, 2, 3, 5, 9, 7);

                List<Integer> r = Arrays.asList((rand.nextInt((9 - 1) + 1) + 1),
                        (rand.nextInt((9 - 1) + 1) + 1),
                        (rand.nextInt((9 - 1) + 1) + 1),
                        (rand.nextInt((9 - 1) + 1) + 1),
                        (rand.nextInt((9 - 1) + 1) + 1),
                        (rand.nextInt((9 - 1) + 1) + 1));



                int n = 0;

                for (int i = 0; i <= 5; i++) {
                    n = n + (w.get(i+2) * r.get(i));
                }

                int checksum = 11 - (n % 11);

                if (checksum == 11) {
                    checksum = 5;
                } else if (checksum == 10) {
                    checksum = 0;
                }
                System.out.println("Checksum Digit "+checksum);
                for (int rn : r) {
                    barcode.append(rn);
                }
                barcode.append(checksum);
                barcode.append(rand.nextInt((9 - 1) + 1) + 1);
                barcode.append(rand.nextInt((9 - 1) + 1) + 1);
                barcode.append(rand.nextInt((9 - 1) + 1) + 1);
                System.out.println("New Barcode is"+barcode);
            }else{
                Assert.fail("Unable to generate 1D checksum barcode. Invalid prefix: " + prefix);
            }
        }else{
            Assert.fail("Unable to generate 1D checksum barcode. Invalid prefix: " + prefix);
        }
        return barcode.toString();
    }

    public String generate1DBarcodeWithAlphabPrefix(String barcode){
        String prefix = barcode.substring(0,2);
        String tempSuffix = barcode.substring(4);
        StringBuilder newBarcode = new StringBuilder(prefix);
        Random rand = new Random();
        newBarcode.append((char) (rand.nextInt(26) + 'A'));
        newBarcode.append((char) (rand.nextInt(26) + 'A'));
        for(int i = 1; i<=tempSuffix.length(); i++){
            newBarcode.append(rand.nextInt((9 - 1) + 1) + 1);
        }
        return newBarcode.toString();
    }

    public String generate1DBarcodeBasedOnRegEx(String barcode, String regex){
        String prefix = barcode.substring(0,2);
        Random rand = new Random();
        StringBuilder newBarcode = new StringBuilder();
        if(regex.equalsIgnoreCase("XX|d{9}|GB")){
            newBarcode.append(generate1DChecksumBarcode(prefix));
        }else if(regex.equalsIgnoreCase("XX|d{9}|ZD{2}")){
            newBarcode.append(generate1DChecksumBarcode(prefix, true));
        }else if(regex.equalsIgnoreCase("XX|D|d{10}")){
            newBarcode.append(prefix);
            newBarcode.append((char) (rand.nextInt(26) + 'A'));
            newBarcode.append(getCheckSumNumbers(6));
            newBarcode.append(rand.nextInt((9 - 1) + 1) + 1);
            newBarcode.append(rand.nextInt((9 - 1) + 1) + 1);
            newBarcode.append(rand.nextInt((9 - 1) + 1) + 1);
        }else if(regex.equalsIgnoreCase("XX|w{2}|d{10}")){
            newBarcode.append(prefix);
            newBarcode.append((char) (rand.nextInt(26) + 'A'));
            newBarcode.append((char) (rand.nextInt(26) + 'A'));
            newBarcode.append(getCheckSumNumbers(6));
            newBarcode.append(rand.nextInt((9 - 1) + 1) + 1);
            newBarcode.append(rand.nextInt((9 - 1) + 1) + 1);
            newBarcode.append(rand.nextInt((9 - 1) + 1) + 1);
        }else if(regex.equalsIgnoreCase("XX|D{2}|d{7}|GB")){
            newBarcode.append(prefix);
            newBarcode.append((char) (rand.nextInt(26) + 'A'));
            newBarcode.append((char) (rand.nextInt(26) + 'A'));
            newBarcode.append(getCheckSumNumbers(6));
            newBarcode.append("GB");
        }else if(regex.equalsIgnoreCase("XX|d{7}|D|d{3}")){
            newBarcode.append(prefix);
            newBarcode.append(getCheckSumNumbers(6));
            newBarcode.append((char) (rand.nextInt(26) + 'A'));
            newBarcode.append(rand.nextInt((9 - 1) + 1) + 1);
            newBarcode.append(rand.nextInt((9 - 1) + 1) + 1);
            newBarcode.append(rand.nextInt((9 - 1) + 1) + 1);
        }else {
            Assert.fail("Unable to generate 1 D checksum barcode. Regex "+regex+" not found!");
        }
        return newBarcode.toString();
    }

    public String getCheckSumNumbers(int length){
        Random rand = new Random();
        List<Integer> w = Arrays.asList(8, 6, 4, 2, 3, 5, 9, 7);
        List<Integer> r = new ArrayList<>();
        int len = length;
        for(int v = 1; v<=8;v++){
            if(len<8){
                len++;
                r.add(0);
            }else{
                r.add(rand.nextInt((9 - 1) + 1) + 1);
            }
        }

        int n = 0;

        for (int i = 0; i <= 7; i++) {
            n = n + (w.get(i) * r.get(i));
        }

        int checksum = 11 - (n % 11);

        if (checksum == 11) {
            checksum = 5;
        } else if (checksum == 10) {
            checksum = 0;
        }

        StringBuilder checksumNum = new StringBuilder();
        int leng = length;
        for (int rn : r) {
            if(leng<r.size()){
                leng++;
            }else{
                checksumNum.append(rn);
            }
        }

        checksumNum.append(checksum);

        return checksumNum.toString();
    }

    public String generate1DChecksumBarcode(String prefix, boolean... randomSuffix){
        StringBuilder barcode = new StringBuilder();

        if(prefix.length()==2) {
            if(Character.isAlphabetic(prefix.charAt(0)) && Character.isAlphabetic(prefix.charAt(1))) {
                barcode.append(prefix);
                List<Integer> w = Arrays.asList(8, 6, 4, 2, 3, 5, 9, 7);
                Random rand = new Random();
                List<Integer> r = Arrays.asList((rand.nextInt((9 - 1) + 1) + 1),
                        (rand.nextInt((9 - 1) + 1) + 1),
                        (rand.nextInt((9 - 1) + 1) + 1),
                        (rand.nextInt((9 - 1) + 1) + 1),
                        (rand.nextInt((9 - 1) + 1) + 1),
                        (rand.nextInt((9 - 1) + 1) + 1),
                        (rand.nextInt((9 - 1) + 1) + 1),
                        (rand.nextInt((9 - 1) + 1) + 1));

                int n = 0;

                for (int i = 0; i <= 7; i++) {
                    n = n + (w.get(i) * r.get(i));
                }

                int checksum = 11 - (n % 11);

                if (checksum == 11) {
                    checksum = 5;
                } else if (checksum == 10) {
                    checksum = 0;
                }

                for (int rn : r) {
                    barcode.append(rn);
                }

                if(randomSuffix.length>0){
                    barcode.append(checksum);
                    barcode.append((char) (rand.nextInt(26) + 'A'));
                    barcode.append((char) (rand.nextInt(26) + 'A'));
                }else{
                    barcode.append(checksum).append("GB");
                }
            }else{
                Assert.fail("Unable to generate 1D checksum barcode. Invalid prefix: " + prefix);
            }
        }else{
            Assert.fail("Unable to generate 1D checksum barcode. Invalid prefix: " + prefix);
        }
        return barcode.toString();
    }
}
