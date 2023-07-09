package langhua.mdns.common;

import org.apache.commons.collections4.list.TreeList;
import org.apache.ofbiz.base.util.Debug;

import javax.jmdns.JmmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;
import java.io.IOException;

public class MdnsServiceThread extends Thread implements ServiceTypeListener, ServiceListener {
    private static final String MODULE = MdnsServiceThread.class.getName();
    private static TreeList<String> serviceTypes = new TreeList<>();
    private static TreeList<String> services = new TreeList<>();

    protected boolean doomed;
    private final long startTime;

    private JmmDNS registry;

    public MdnsServiceThread(ThreadGroup threadGroup, String name) {
        super(threadGroup, name);
        setDaemon(false);
        doomed = false;
        // set start time
        startTime = System.currentTimeMillis();
    }

    public void serviceTypeAdded(ServiceEvent event) {
        String type = event.getType();
        if (!serviceTypes.contains(type)) {
            Debug.logVerbose("Mdns Service Type added   : " + event.getType(), MODULE);
            serviceTypes.add(type);
            registry.addServiceListener(type, this);
        }
    }

    @Override
    public void subTypeForServiceTypeAdded(ServiceEvent event) {
        Debug.logInfo("Jmdns SUBTYPE added: " + event.getType(), MODULE);
    }

    @Override
    public void serviceAdded(ServiceEvent event) {
        final String name = event.getName();
        Debug.logInfo("Mdns Service added   : " + event.getInfo(), MODULE);
        services.add(name);
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        final String name = event.getName();
        Debug.logInfo("Mdns Service removed : " + name + "." + event.getType(), MODULE);
        services.remove(name);
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        Debug.logInfo("Mdns Service resolved: " + event.getInfo(), MODULE);
    }

    public TreeList<String> getServiceTypes() {
        return serviceTypes;
    }

    public TreeList<String> getServices() {
        return services;
    }

    public void start() {
        Debug.logInfo("Start to scan mdns ...", MODULE);
        registry = JmmDNS.Factory.getInstance();
        try {
            registry.addServiceTypeListener(this);
        } catch (IOException e) {
            Debug.logError(e, MODULE);
        }
    }

    public void interrupt() {
        if (registry != null) {
            try {
                registry.close();
            } catch (IOException e) {
                Debug.logError(e, MODULE);
            }
        }
        Debug.logInfo("MdnsServiceThread run for " + (System.currentTimeMillis() - startTime)/1000 + " seconds", MODULE);
    }
}
