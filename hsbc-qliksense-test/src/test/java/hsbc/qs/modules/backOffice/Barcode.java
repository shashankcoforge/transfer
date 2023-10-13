package hsbc.qs.modules.backOffice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.util.Random;

public class Barcode {

    protected Logger log = LogManager.getLogger(this.getClass().getName());
    public String generateBarcodeForPouchPreparation(String type){
        StringBuilder barcode = new StringBuilder();
        barcode.append("3");
        String cashOrCoinDigit = "";
        String cashDigit = "08";
        String[] coinDigit = {"58", "57"};
        if(type.equalsIgnoreCase("cash")){
            cashOrCoinDigit = cashDigit;
        }else if(type.equalsIgnoreCase("coin")){
            int rand = new Random().nextInt((2 - 1) + 1) + 1;
            cashOrCoinDigit = coinDigit[rand-1];
        }else{
            Assert.fail("Unable to generate barcode. Invalid pouch type : "+type);
        }
        barcode.append(cashOrCoinDigit);
        String[] fourthDigit = {"0", "1"};
        int rand = new Random().nextInt((2 - 1) + 1) + 1;
        barcode.append(fourthDigit[rand-1]);

        for(int i = 1; i<=7;i++){
            int r = new Random().nextInt((9 - 1) + 1) + 1;
            barcode.append(r);
        }

        int sumOfOddNumbers = 0;
        int sumOfEvenNumbers = 0;

        for(int n=0;n<barcode.toString().length();n++){
            int c = n+1;
            int m = Integer.parseInt(String.valueOf(barcode.toString().charAt(n)));
            if((c%2)==0){
                sumOfEvenNumbers = sumOfEvenNumbers+m;
            }else{
                sumOfOddNumbers = sumOfOddNumbers+m;
            }
        }
        int val = (sumOfOddNumbers*3)+sumOfEvenNumbers;

        if((val%10)==0) {
            barcode.append("0");
        }else{
            int x = (10-(val%10));
            barcode.append(x);
        }

        log.info("Barcode generated for "+type+" : "+val);
        return barcode.toString();
    }

}
