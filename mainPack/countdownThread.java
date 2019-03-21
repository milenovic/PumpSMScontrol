//5min countdown before setting the warmUp flag back to 0
package mainPack;

public final class countdownThread implements Runnable {

    public countdownThread() {
    }

    public final void run() {
        try {
            //System.out.println("Warming...");
            Main.warmingUp = 1;
            Thread.sleep(150000); //sleep
            //System.out.println("ALIVE AGAIN");
            if (Main.warmingUp == 1 && Main.pumpON == 1) {
                Main.warmingUp = 0;
                System.out.println("Warm"); //warmed up flag set, pressure monitor is now enabled
                if (Main.currentP >= 2.0f){
                    System.out.println("After warmup press OK and =" + Main.currentP);
                   //new Thread(new caller(alertID)).start(); //Cimni ako je pritisak OK, nepotrebno?
                }
                //if press low regular chack will detect
            } else if (Main.warmingUp == 0 && Main.pumpON == 0) {
                System.out.println("Strt aborted in the meantime.");
            }
        }
        catch (Exception ex) {
            System.out.println("countDown ex: " + ex);
        }
    }
}
