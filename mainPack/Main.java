
package mainPack;

import com.siemens.icm.io.*;
import javax.microedition.midlet.*;

public class Main extends MIDlet {
    //all public fields are here! this is main class, here is app entry point
    public static MIDlet instance;
    public static int warmingUp = 0;
    public static int active = 1;
    public static int pumpON = 0;
    public static int flagNaT = 0; //Ne radi a Treba
    public static int flagRaN = 0; //Radi a Ne treba
    public static float currentP = 0.0f; //Current pressure, periodically updated
    public static long offTime = 0;
    public static long onTime = 0;
    public static long lp = 0;
    public static SyncATCommand atCommand; //sync added according to Programmer to Programmer instruction
    public static MyATCommandListener atCommandListener; // listen to the AT-commands from VMS, and other events
    public static final String allowedIDs = "381600000000, 381600000000"; //allowed phone numbers with country code
    public static final String alertID = "381600000000"; //phone number to send alerts to
    
    public Main() {

        //System.out.println("Constructor"); //debuging
        try {
            atCommand = new SyncATCommand(false); //sync added according to Prog to Prog instruction
            atCommandListener = new MyATCommandListener();
            atCommand.addListener(atCommandListener);
        } catch (Exception e) {
            System.out.println(e);
            notifyDestroyed();
        } //end catch
    } //end constructor

    void init() //inicijalizacija.... (resetuje)
    {
        try {
            atCommand.send("AT&F\r"); // Reset  
            Thread.sleep(100); // sleep according to the manual...
            atCommand.send("AT+CMGF=0\r"); //set PDU mode
            Thread.sleep(100);
            atCommand.send("AT+CNMI=2,1,0,0,1\r");  //podesava da stigne URC za sms sa mem i index
            Thread.sleep(100);
            atCommand.send("AT^SM20=0,1\r"); // konfigurise da se pojavi OK posle ATD komande, odblokira waiting for response...
            Thread.sleep(100);
            //konfigurisanje pinova i IO driver-a
            atCommand.send("AT^SPIO=0\r"); //zatvara IO driver
            Thread.sleep(100);
            atCommand.send("AT^SPIO=1\r"); //otvara IO driver
            Thread.sleep(100);
            atCommand.send("AT^SCPIN=1,4,1\r"); //konfigurise pin "AT^SCPIN=mode,id,direction,[startValue]" ovo je GPIO5 za WD
            Thread.sleep(100);
            atCommand.send("AT^SCPIN=1,3,1\r"); //konfigurise pin "AT^SCPIN=mode,id,direction,[startValue]" ovo je GPIO4 za prvi IZLAZ (relej)
            Thread.sleep(100);
            atCommand.send("AT^SRADC=0,1,5000\r"); //zapocinje merenje analognon pina 0 na svakih 5 sekundi
            Thread.sleep(100);
            //gotovo konfigurisanje
            Watchdog WD_instance = new Watchdog(); //instancira WD
            WD_instance.start(); //Watchdog started!
            //new Thread(new cleaner()).start(); //cisti SMS memoriju
            Main.cleanSMSstorage(); //cistac
            System.out.println("Gotova inicijalizacija!");
            SMS.send("+" + Main.alertID, "Uredjaj aktivan.");
        } catch (ATCommandFailedException e) {
            System.out.println("ERROR: init failed: " + e);  //debuging print
        } catch (InterruptedException e) {
            System.out.println("ERROR: init interrupted: " + e);  //debuging print
        }
    } //init() ends here

    public static void ProcessUnsolicitedEvents() throws InterruptedException {
        if (atCommandListener.hasNewEvent()) {
            if (atCommandListener.strATCommand.indexOf("RING") != -1) {
                //u slucaju zvona (RING)
                //System.out.println("RING"); //debuging print
            //TO DO: mogo bi ovde da odbije call!!!
            } else if (atCommandListener.strATCommand.indexOf("+CMTI:") != -1) {
                //u slucaju nove poruke...
                System.out.println("New SMS"); //debuging print
                readSMS(atCommandListener.strATCommand);  //readSMS preuzima poso sa tim sms-om
            } else if (atCommandListener.strATCommand.indexOf("^SMGO:") != -1) {
                //u slucaju pune sms memorije, brise sve!
                Main.cleanSMSstorage(); //cistac
            } else if (atCommandListener.strATCommand.indexOf("^SRADC:") != -1) {
                //Stiglo je zakazano merenje analognog ulaza
                //System.out.println("New event: Press measure"); //debuging print
                Inputs.calcPressure(atCommandListener.strATCommand);  //calcPress preuzima poso sa tim sms-om
            } else //ako nije nista od gore ispitanog...
            {
                System.out.println("Event(?):" + atCommandListener.strATCommand);
            } //debuging print
        }
        atCommandListener.strATCommand = ""; //za svaki slucaj stavlja prazan string u strATCommand field.
    } //kraj ProcessUnsolicitedEvents()

    public static void readSMS(String bareURC) throws InterruptedException {
        //prvo nalazi memory index a posle cita poruku iz memorije
        if (bareURC.indexOf("CMTI: \"") != -1) {
            int place = bareURC.indexOf(": \"");
            if (place != -1) {
                String memNo = bareURC.substring(place + 7).trim();
                //System.out.println("Located memNo is:" + memNo);  //debuging print
                //sad ide citanje poruke! try-catch mora zbog slanja ATC komande
                Thread.sleep(100); // spava...
                try {
                    String response = atCommand.send("AT+CMGR=" + memNo + "\r");
                    if (response.indexOf("+CMGR: ") != 1) {
                        String PDUonly = response.substring(response.indexOf("+CMGR: ")).substring(response.substring(response.indexOf("+CMGR: ")).indexOf(0x0d) + 2, response.substring(response.indexOf("+CMGR: ")).indexOf("OK") - 2);
                        String smsText = SMS.getText(PDUonly);
                        System.out.println("text:|" + smsText + "|"); //debuging print
                        String smsSenderID = SMS.getSender(PDUonly);
                        System.out.println("sender:|" + smsSenderID + "|"); //debuging print
                        doTheJob(smsText, smsSenderID); //salje rezultate citanja, tamo se vrsi sta treba sa tim SMS-om
                    }
                } catch (ATCommandFailedException ex) {
                    ex.printStackTrace();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                }
            //dovde se cita
            } else {
                System.out.println("Prazan CMTI?");
            } //debuging print
        } else {
            System.out.println("URC not +CMTI ERR");
        }//debuging print

    }//end reading
    public static void doTheJob(String smsText, String smsSenderID) {
        //radi sta treba na osnovu teksta poruke i broja
        if (allowedIDs.indexOf(smsSenderID + ",") != -1) {
            if (smsText.equals("shutdown")) {
                //tekst za gasenje jave, da bi moglo da se nastavi programiranje bez gaseja uredjaja....
                System.out.println("Java se gasi!!"); //debuging print
                instance.notifyDestroyed();

            } else if (smsText.equals("P")) {
                //test primanja i slanja
                SMS.send("+" + smsSenderID, currentP + " bar"); // salje pritisak
                Main.cleanSMSstorage(); //cistac

            } else if (smsText.equals("U")) {
                GPIO.open(4); //otvara izlazni pin 1 (po semi to je GPIO4)
                if (pumpON == 1) {
                    new Thread(new countdownThread()).start(); //5min timer starts
                    //SMS.send("+" + smsSenderID, "Upaljeno!"); //removed, cimanje mora iz countdown
                    //new Thread(new caller(smsSenderID)).start(); //Cimni ako je pritisak OK
                    call(smsSenderID); //pupupu
                    
                } else {
                    SMS.send("+" + smsSenderID, "GRESKA!!! Paljenje nije uspelo!");
                    Main.cleanSMSstorage(); //cistac
                }

            } else if (smsText.equals("I")) {
                GPIO.close(4); // zatvara izlazni pin 1 (po semi to je GPIO4)
                //Main.offTime = System.currentTimeMillis(); moved to gpio itself
                if (pumpON == 0) {//otvara izlazni pin 1 (po semi to je GPIO4)
                    //SMS.send("+" + smsSenderID, "Ugaseno!"); //REPLACE WITH CALL
                    //new Thread(new caller(smsSenderID)).start(); //Cimni
                    call(smsSenderID); //pupupu
                } else {
                    SMS.send("+" + smsSenderID, "GRESKA!!! Gasenje nije uspelo!");
                    Main.cleanSMSstorage(); //cistac
                }

            } else if (smsText.equals("Cimni")) {
                //ovde za nesto drugo...  i tako dalje..... 
                call(smsSenderID);
                Main.cleanSMSstorage(); //cistac

            } else {
                //ovde za nepoznato 
                System.out.println("nepoznato?: " + smsText);
                Main.cleanSMSstorage(); //cistac

            }
        } else {
            System.out.println("SMS sa nedozvoljenog broja");
            Main.cleanSMSstorage(); //cistac
        } //kraj IF dela... bla bla
        //new Thread(new cleaner()).start();  //brise SVE poruke u memoriji... MOVED
    }//doTheJob() ends here
    public static void call(String number){
        active = 0;
        System.out.println("calling");
        try {
            Main.atCommand.send("AT^SRADC=0,0,5000\r\n"); //stop measure so it does not clogg
            Main.atCommand.send("ATH\r\n"); //end possible ongoing calls
            Thread.sleep(100);
            //String response = Main.atCommand.send("ATD+381600591522;\r\n"); //call
            Main.atCommand.send("ATD+" + number + ";\r\n"); //call     
            //System.out.println(response);
            Thread.sleep(17000); // spava...
            Main.atCommand.send("ATH\r\n");
            System.out.println("Hang");
            Thread.sleep(200); // spava...
            Main.atCommand.send("AT^SRADC=0,1,5000\r\n"); //restart measure 
            //active = 1;
        } catch (Exception ex) {
            System.out.println("Call ex:" + ex);
            active = 1;
            return;
        }
        active =1;
    }
 public static void cleanSMSstorage() {
        //brise SVE sms poruke iz memorije, redom...
        try {
            Main.atCommand.send("AT^SRADC=0,0,5000\r\n"); //stop measure so it does not clogg
            Thread.sleep(100);
        } catch (Exception ex) {
            System.out.println("stopM:" + ex);
        }
        try {
            int i = 1;

            while (atCommand.send("AT+CMGD=" + i + "\r").indexOf("OK") != -1) {
                Thread.sleep(100);
                i++;
            }
            System.out.println("SMS memory cleared!"); //debuging print
        } catch (Exception ex) {
            return;
        }

        try {
            Main.atCommand.send("AT^SRADC=0,1,5000\r\n"); //restart measure
            Thread.sleep(100);
        } catch (Exception ex) {
            System.out.println("startM:" + ex);
        }
    } //cleanSMSstorage() ends here

    public void startApp() throws MIDletStateChangeException {
        instance = this;
        //System.out.println("startApp() started, krece init()!!!"); //debuging print
        init();
    //System.out.println("Initialized, waiting for URCs..."); //debuging print
    //destroyApp(true); //ne sme da se desi ovde!
    } //startApp() ends here!

    public void pauseApp() {
    //nije korisceno u ovom programu i NEMA POTREBE...
    //System.out.println("pauseApp()");
    }

    public void destroyApp(boolean cond) {
        System.out.println("destroyApp(" + cond + ") - kraj!"); //debuging print
        notifyDestroyed();
    }
}
