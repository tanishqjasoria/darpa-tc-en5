package com.bbn.tc.services.kafka;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.mortbay.log.Log;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.exporter.PushGateway;
import io.prometheus.client.exporter.common.TextFormat;

/**
 * Provide the instrumented metrics to a metric server
 * Could do push or pull, for now we just implement pull
 * So this class embeds a Jetty server that lets the metric server do HTTP GETs
 * Could implement a push version that pushes metrics out to a pullgateway.
 * @author bbenyo
 *
 */
public class InstrumentationManager {
    protected static final Logger logger = Logger.getLogger(InstrumentationManager.class.getCanonicalName());
    
    static protected boolean InstrumentationPull = false; // False = use the PushGateway
    static protected Server instrumentationServer = null;
    static protected PushGateway pushGateway = null;
    static protected String pushGatewayAddress = "ta3-prometheus-1.tc.bbn.com:3332"; // Assume prometheus is in dns or /etc/hosts, or override via cmd line
    static public int pushPeriodMs = 30000; // Push every 30 sec
    static public int pushPeriodRetry = 300000; // Retry every 5 minute if the push gateway is unreachable
    static public boolean pushRetryMode = false; // true means we're retrying every minute and not spamming the log since the gateway was down
    static public String pushLabel = null; // Use hostname if null
    
    static protected int instrumentationServerPort = 9193;
    static public int metricScrapeInterval = 10000; // 10 secs, set in prometheus
      // Just need it here so we can wait at the end to give prometheus time to scrape the last update
    static String hostName;
    static private boolean UnknownHostname = true;
    static protected Thread pusher = null;
    
    public static String getConfigStr(char separator, char fSeparator) {
        StringBuffer sb = new StringBuffer();
        sb.append("pullMode").append(separator).append(InstrumentationPull).append(fSeparator);
        if (InstrumentationPull) {
            sb.append("instrumentationServerPort").append(separator).append(instrumentationServerPort).append(fSeparator);
            sb.append("scrapeInterval").append(separator).append(metricScrapeInterval).append(fSeparator);
        } else {
            sb.append("pushGateway").append(separator).append(pushGatewayAddress).append(fSeparator);
            sb.append("pushPeriodMs").append(separator).append(pushPeriodMs).append(fSeparator);
            sb.append("pushLabel").append(separator).append(pushLabel).append(fSeparator);
        }
        return sb.toString();
    }
    
    public static boolean turnOnInstrumentationServer()  {
        if (InstrumentationPull) {        
            if (instrumentationServer == null) {
                logger.info("Creating new Instrumenetation Server on "+instrumentationServerPort);
                instrumentationServer = new Server(instrumentationServerPort);
                ServletContextHandler context = new ServletContextHandler();
                context.setContextPath("/");
                instrumentationServer.setHandler(context);
                context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
                instrumentationServer.setStopAtShutdown(true);
            } else {
                if (instrumentationServer.isRunning()) {
                    logger.warn("Instrumentation Server is already on");
                    return true;
                } 
            }
            
            try {
                logger.info("Starting Instrumentation Server");
                instrumentationServer.start();
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                instrumentationServer.destroy();
                return false;
            }
        } else {
            pushGateway = new PushGateway(pushGatewayAddress);
            // Test the connection before we try to push
            String pgUrl = "http://" + pushGatewayAddress + "/metrics";         
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(pgUrl).openConnection();
                connection.setRequestProperty("Content-Type", TextFormat.CONTENT_TYPE_004);
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);
                connection.connect();
                if (connection.getResponseCode() <= 0) {
                    // We'll accept any response code for now, just want to see if we can connect
                    // HEAD is blocked as invalid
                    logger.warn("Unable to connect to PushGateway at "+pgUrl);
                    pushGateway = null;
                    return false;
                }
            } catch (Exception ex) {
                logger.warn("Unable to connect to prometheus for metrics: "+ex.toString());
                return false;
            }
            logger.info("PushGateway initialized");
            pusher = new Thread(new Runnable() {
                public void run() {
                    while(pushGateway != null) {
                        try {
                            if (pushRetryMode) {
                                Thread.sleep(pushPeriodRetry);
                            } else {
                                Thread.sleep(pushPeriodMs);
                            }
                        } catch (InterruptedException e) {
                            logger.info("Metric pusher interrupted: "+e.getLocalizedMessage());
                        }
                        logger.debug("Pushing metrics at time "+System.currentTimeMillis());
                        pushMetrics();
                    }
                }
            });
            pusher.start();
            return true;
        }
    }
    
    public static void pushMetrics() {
        String label = pushLabel;
        if (label == null) {
            if (hostName == null) {
                getHostName();
            }
            label = hostName;            
        }
        try {
            if (pushGateway != null) {
                pushGateway.pushAdd(CollectorRegistry.defaultRegistry, label);
                pushRetryMode = false;
            }
        } catch (IOException e) {
            if (!pushRetryMode) {
                // Only spam the log with the stack trace once
                logger.error(e.getMessage(), e);
                logger.warn("Unable to push to the TA3 metrics PushGateway");
            } else {
                logger.warn("Still unable to connect to the TA3 metrics PushGateway");
            }
            pushRetryMode = true;
        }
    }
        
    public static boolean turnOffInstrumentationServer() {
        if (instrumentationServer != null) {
            try {
                logger.info("Stopping Instrumentation Server");
                // Give Prometheus time to scrape the last updates
                if (InstrumentationPull) {
                    logger.info("Waiting for the server to scrape the last update");
                    Thread.sleep(metricScrapeInterval);
                }
                instrumentationServer.stop();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }
        if (pushGateway != null) {
            pushMetrics();
            logger.info("Disabling the PushGateway");
            pushGateway = null;
        }
	if (pusher != null) {
	    logger.info("Interrupting the pusher");
	    pusher.interrupt();
	    logger.info("Interrupt sent");
	} else {
	    logger.info("No pusher thread to interrupt");
	}
	logger.warn("Instrumentation Server is already off");
        return true;
    }
    
    /**
     * Return local host host name, can return null if there's an exception
     * @return
     */
    public static String getHostName() {
        if (UnknownHostname) {
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                hostName = null;
            }
            UnknownHostname = false;
        }
        return hostName;
    }

}
