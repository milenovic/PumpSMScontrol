//napraviti WD!!!
//wd instance =new watchdog
//wd instace.start()
package mainPack;

import com.siemens.icm.io.ATCommandFailedException;

public final class Watchdog extends Thread {
    
boolean cond = true;

public  Watchdog()
    { 
    }

    public final void run()
    {
        try{
            System.out.println("WD startrted!");
        while(cond)
        {
           try{
           for(int i=0; i<2; i++)
           {
                try {
                    //Dodeljuje vrednost 1 (open-high state) "AT^SSIO=id,value"
                    Main.atCommand.send("AT^SSIO=4,1\r"); //Dodeljuje vrednost 1 (open-high state) "AT^SSIO=id,value"
                    Thread.sleep(1);
                    Main.atCommand.send("AT^SSIO=4,0\r"); //Dodeljuje vrednost 1 (open-high state) "AT^SSIO=id,value"
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } catch (ATCommandFailedException ex) {
                    ex.printStackTrace();
                }
           }
           //System.out.println("WD kick");
           }//try ends here
           catch (Exception exception)
           {
                System.out.println("WD exception " + exception);
           }//catch ends here
           Thread.sleep(20000); //spava 20sec pa ponovo kickWD
        }
        }//1st try ends here
        catch(Exception ex)
        {
        System.out.println("WD,totalni propast,sleepEX: " + ex);
        }//catch ends here
    }//run ends here
} //class ends here!
