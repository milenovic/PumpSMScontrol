//Sinhronizuje atCommand-e, preuzeto iz HCP programmer to programmer instruction!
package mainPack;

import com.siemens.icm.io.ATCommand;
import com.siemens.icm.io.ATCommandFailedException;

public class SyncATCommand extends ATCommand
{
    public SyncATCommand(boolean csdSupport) throws Exception
    {
    super(csdSupport);
    }
    
    public synchronized String send (String ATCmd) throws
    ATCommandFailedException
    {
    return super.send(ATCmd);
    }
    
    public synchronized String sendRval (String ATCmd) throws
    ATCommandFailedException
    {
    String s = super.send(ATCmd);
    s = s.substring(1);
    s = s.substring(s.indexOf(':')+2,s.indexOf('\r'));
    return s;
    }
}