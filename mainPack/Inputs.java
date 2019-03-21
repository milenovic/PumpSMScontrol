package mainPack;

//functions that handle input events
public class Inputs {

    public static String format(float dec) {
        //voltage reading on demand (unused)
        if (String.valueOf(dec).length() > 5) {
            return String.valueOf(dec).substring(0, 5);
        } else {
            return String.valueOf(dec);
        }
    }
        
    public static void calcPressure(String command) {
        //convert to float and add return for further stuff
        float voltage = 0.00f;
        float press = 0.0f;
        
        command = command.substring(command.indexOf(" ") + 5);
        command = command.substring(0, command.indexOf("\r"));
        voltage = (Float.parseFloat(command) - 7.0f) / 1000f * 5.3333f;//ADC value on specified analog pin
        press = voltage * 3.0f - 1.5f; //this is from the excel equation, calibration is therefore in excel!
        Main.currentP = press;
        //System.out.println("comm " + format(voltage) + " means " + format(press) + "bar");
        System.out.println("Pritisak: " + format(press) + "bar");
        //if (Main.active ==1){
            
        //}
        if (Main.active == 1){
            
        
        if (press > -1.0f) {
            //pump is on for sure
            if (Main.flagNaT == 1) {
                //ukljucila se posle Nivostata (ili posle nekog kvara (bimetal?))
                SMS.send("+" + Main.alertID, "Pumpa se ponovo ukljucila.");
                System.out.println("Pumpa se ponovo ukljucila SMS");
                Main.flagNaT = 0;
                Main.warmingUp = 1;
                //start warmup
                new Thread(new countdownThread()).start(); //5min timer starts
            } else {
                //NaT was 0, paljenje normalno
                if (Main.pumpON == 1) {
                    Main.flagRaN = 0;

                    if (Main.warmingUp == 0) {
                        //Warm, yes.
                        if (press < 2.00f) {
                            if (Main.lp > 1) {
                                Main.lp = 0;
                                //pressure too low, pump not in warmup state (both just after or in the middle)
                                //PUMP SHUTDOWN
                                GPIO.close(4);
                                //Main.offTime = System.currentTimeMillis(); // moved to gpio itself
                                //wait happens in GPIO.close for charger to discharge (REMOVE THE FUCKING CAP)                       
                                System.out.println("Pad pritiska na" + format(press) + "bar, pumpa se gasi. SMS");
                                SMS.send("+" + Main.alertID, "Pad pritiska na" + format(press) + "bar, pumpa se gasi.");

                            }

                            Main.lp = Main.lp + 1;
                        } else {
                        //pump on, warmed up, pressure >2bar, do nothing.
                        //System.out.println("ON, warm, pressOK");
                        }
                    } else {
                    //still warming up
                        //System.out.println("warming up...");
                    }
                } else {
                   //should not run
                    long sinceOFF = System.currentTimeMillis() - Main.offTime;
                    //System.out.println("should be off but is ON");
                   if (Main.flagRaN == 0 && sinceOFF > 22000){ //first time here, send sms
                       //Send sms
                       SMS.send("+" + Main.alertID, "Pumpa radi bez daljinske komande!");
                       System.out.println("Pumpa radi bez daljinske komande SMS");
                   }
                   Main.flagRaN=1;//make sure sms is not sent next time
               }
  
            }//flagNaT=0 case ends
    
        }else{
            //pump is off for sure
            Main.flagRaN = 0;
            Main.warmingUp = 0;
            if (Main.pumpON == 1){
                //pump should be on but is off
                //System.out.println("should be on but is OFF, not sending");
                //Nivostat and other motor shutdowns end up here
               long sinceON = System.currentTimeMillis() - Main.onTime;
               if (Main.flagNaT == 0 && sinceON > 22000) { //first time here, send sms
                //if (Main.flagNaT == 0) { //first time here, send sms
                    //Send sms
                    SMS.send("+" + Main.alertID, "Pumpa ne radi. Zbog nivostata?");
                    System.out.println("Pumpa ne radi. Zbog nivostata? SMS");
                    Main.flagNaT = 1; //moved here Oct '18. Correct way?
                }
                
            }else{
                //off as it should be
                Main.flagNaT = 0;
            }
        }
        
    }else{
         System.out.println("inactive");   
    }
        }
}
