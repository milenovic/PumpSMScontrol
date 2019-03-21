package mainPack;
//Pin open/close
import com.siemens.icm.io.ATCommandFailedException;

public class GPIO {

    public static void open(int GPIOnum){
         //dodeljuje vrednost 1 pomocu AT^SSIO i vraca report pomocu AT^SGIO
        //testirati, ako nece staviti Void umesto String i obrisati return-e!!!
        int GPIOindex = GPIOnum -1;  //neophodno jer se pinovi broje od nule...
        try {
            Main.atCommand.send("AT^SSIO=" + GPIOindex + ",1\r"); //Dodeljuje vrednost 1 (open-high state) "AT^SSIO=id,value"
            Thread.sleep(100);
            String response = Main.atCommand.send("AT^SGIO=" + GPIOindex + "\r");
            if (response.indexOf("^SGIO: 1") != -1) {
                Main.pumpON = 1;
                Main.warmingUp = 1;
                System.out.println("pumpON=1");
                Main.onTime = System.currentTimeMillis();
            }else{
                Main.pumpON = 0;
                Main.warmingUp = 0;
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (ATCommandFailedException ex) {
            ex.printStackTrace();
        }
    }//open() ends here
    
    public static void close(int GPIOnum){
        int GPIOindex = GPIOnum -1;
        try {
            Main.atCommand.send("AT^SSIO=" + GPIOindex + ",0\r");
            Thread.sleep(100);
            String response = Main.atCommand.send("AT^SGIO=" + GPIOindex + "\r");
            if (response.indexOf("^SGIO: 0") != -1) {
                Main.pumpON = 0;
                Main.warmingUp = 0;
                Main.flagNaT = 0;
                Main.offTime = System.currentTimeMillis();
            }else{
                Main.pumpON = 1;
                //Main.warmingUp = 1;
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (ATCommandFailedException ex) {
            ex.printStackTrace();
        }  
    }
}
