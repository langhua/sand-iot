package langhua.mdns.common;

import langhua.mdns.services.MdnsService;
import org.apache.ofbiz.base.util.Debug;

public class MdnsServiceThread extends Thread {
    private static final String MODULE = MdnsServiceThread.class.getName();
    private boolean doomed;
    private final long startTime;

    private static MdnsService mdnsService;

    public MdnsServiceThread(ThreadGroup threadGroup, String name) {
        super(threadGroup, name);
        setDaemon(false);
        doomed = false;
        // set start time
        startTime = System.currentTimeMillis();
    }

    @Override
    public void start() {
        Debug.logInfo("Start to scan mdns ...", MODULE);
        mdnsService = new MdnsService();
    }

    @Override
    public void interrupt() {
        if (mdnsService != null) {
            mdnsService.close();
        }
        Debug.logInfo("MdnsServiceThread run for " + (System.currentTimeMillis() - startTime) / 1000 + " seconds", MODULE);
    }

    public static MdnsService getMdnsService() {
        return mdnsService;
    }
}
