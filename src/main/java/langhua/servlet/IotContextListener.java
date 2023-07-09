package langhua.servlet;

import langhua.mdns.common.JmdnsThread;
import langhua.mdns.common.MdnsServiceThread;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.webapp.WebAppUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class IotContextListener implements ServletContextListener {
    public static final String MODULE = IotContextListener.class.getName();
    private static ServletContext servletContext = null;

    private static JmdnsThread jmdnsThread;

    private static MdnsServiceThread mdnsServiceThread;

    private static final String JMDNS_THREAD_NAME = "sand-iot-thread";
    private static final String MDNS_SERVICE_THREAD_NAME = "sand-iot-mdns-service";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        servletContext = sce.getServletContext();
        Delegator delegator = WebAppUtil.getDelegator(servletContext);
        LocalDispatcher dispatcher = WebAppUtil.getDispatcher(servletContext);
        Debug.logInfo("Sand-Iot Context initialized, delegator " + delegator + ", dispatcher", MODULE);
        servletContext.setAttribute("delegator", delegator);
        servletContext.setAttribute("dispatcher", dispatcher);
        servletContext.setAttribute("security", WebAppUtil.getSecurity(servletContext));
        initialMdnsThreads();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        Debug.logInfo("Sand-Iot Context destroyed, removing delegator and dispatcher ", MODULE);
        context.removeAttribute("delegator");
        context.removeAttribute("dispatcher");
        context.removeAttribute("security");
        context = null;
        jmdnsThread.interrupt();
        mdnsServiceThread.interrupt();
    }

    private void initialMdnsThreads() {
        Debug.logInfo("Initial Jmdns thread ...", MODULE);
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int i = threadGroup.activeCount();
        Debug.logInfo("--- thread group[" + threadGroup.getName() + "] has [" + i + "] threads.", MODULE);
        Thread[] threads = new Thread[i];
        threadGroup.enumerate(threads, true);
        for (Thread threadInstance : threads) {
            if (threadInstance instanceof JmdnsThread thread) {
                if (thread.getName().equals(JMDNS_THREAD_NAME)) {
                    Debug.logInfo("--- Found sandflower jmdns thread", MODULE);
                    jmdnsThread = thread;
                }
            } else if (threadInstance instanceof MdnsServiceThread thread) {
                if (thread.getName().equals(MDNS_SERVICE_THREAD_NAME)) {
                    Debug.logInfo("--- Found sandflower mdns serivce thread", MODULE);
                    mdnsServiceThread = thread;
                }
            }
        }

        if (mdnsServiceThread == null) {
            mdnsServiceThread = new MdnsServiceThread(threadGroup, MDNS_SERVICE_THREAD_NAME);
            mdnsServiceThread.start();
        }
        if (jmdnsThread == null) {
            jmdnsThread = new JmdnsThread(threadGroup, JMDNS_THREAD_NAME);
            jmdnsThread.start();
        }
    }
}
