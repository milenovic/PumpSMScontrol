package mainPack;

import com.siemens.icm.io.ATCommandListener;

public class MyATCommandListener implements ATCommandListener {

    String strATCommand;
    boolean HasNewEvent = false;

    public MyATCommandListener() {
    }

    public boolean hasNewEvent() //koristi je ProcessUnsolicitedEvents() da proveri da nije greskom pozvan, mozda je nepotrebno...
    {
        if (HasNewEvent) {
            HasNewEvent = false;
            return true; //kad ima ova metoda vraca TRUE
        }
        return false;  //ovo je ustvari ako nema new event (valjda!)
    }

    public void ATEvent(String str) {
        strATCommand = str;
        HasNewEvent = true;
        try {

            Main.ProcessUnsolicitedEvents(); //preuzima poso sa tim evenotm...
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }

    public void RINGChanged(boolean ring) {

    }

    public void CONNChanged(boolean ring) {

    }

    public void DSRChanged(boolean ring) {

    }

    public void DCDChanged(boolean ring) {

    }
}
