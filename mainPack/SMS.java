//Tri metode. Prima PDU string, vraca text; Prima PDU string, vraca broj; salje SMS.
package mainPack;

import com.siemens.icm.io.ATCommandFailedException;
import PDU.PDU;

public class SMS {

    public static String getText(String submited) {
//        System.out.println("PDU parsing starts");
//        PDU pdu = new PDU();
//        pdu.ParsePDU(submited);
//        String text = pdu.smsText;
        PDU pdu = new PDU(submited);

        String text = pdu.getUserData();

        return text;
    }

    public static String getSender(String submited) {
        PDU pdu = new PDU(submited);
        String number = pdu.getSenderNumber().replace('F', ' ').replace('+', ' ').trim();
        return number;
    }

    public static void send(String toNumber, String SMStext) {
        //TO DO:nemam nikakvu potvrdu da je slanje uspelo . . . . :(        
        try {
            System.out.println("sending");
            Main.atCommand.send("AT+CMGF=1\r"); //podesava text mode      
            //Thread.sleep(100); // spava...
            String response = Main.atCommand.send("AT+CMGS=\"" + toNumber + "\"\r\n");
            //Thread.sleep(100); // spava...
            if (response.indexOf(">") != -1) {
                Main.atCommand.send(SMStext + '\032');
            }
            System.out.println("sent");
        } catch (ATCommandFailedException ex) {
            ex.printStackTrace();
        }

        try {
            //Thread.sleep(1000); // spava...
            Main.atCommand.send("AT+CMGF=0\r"); //Vraca na PDU mode, zbog primanja poruka
        } catch (ATCommandFailedException ex) {
            ex.printStackTrace();
        }

    }//send() ends here
}//end of class!!!

