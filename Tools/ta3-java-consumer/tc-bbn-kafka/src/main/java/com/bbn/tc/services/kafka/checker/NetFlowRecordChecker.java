package com.bbn.tc.services.kafka.checker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm20.NetFlowObject;
import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;

public class NetFlowRecordChecker extends CDMChecker {
    private static final Logger logger = Logger.getLogger(NetFlowRecordChecker.class);
    
    int netFlowsFound = 0;
    int validNetFlowsFound = 0;
    int ipv4NetFlowsFound = 0;
    
    int netFlowsWithTCPSeqNumber = 0;
    
    public static String IPv4_REGEX = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    Pattern ipv4Pattern;
    
    public NetFlowRecordChecker() {
        netFlowsFound = 0;
        validNetFlowsFound = 0;
        
        ipv4Pattern = Pattern.compile(IPv4_REGEX);
    }
    
    @Override
    public String description() {
        return "Valitate NetFlow records";
    }

    protected boolean isIPv4(CharSequence addr) {
        Matcher matcher = ipv4Pattern.matcher(addr);
        return matcher.matches();
    }
    
    @Override
    public void processRecord(String key, TCCDMDatum datum) throws Exception {  
        Object record = datum.getDatum();
        if (record instanceof NetFlowObject) {
            netFlowsFound++;
            NetFlowObject nfo = (NetFlowObject)record;
            boolean valid = true;
            boolean ipv4 = false;
            
            CharSequence localAddr = nfo.getLocalAddress();
            if (localAddr.length() <= 1) {
                logger.warn("NetFlow localAddress length < 1: "+localAddr);
                valid = false;
            } else {
                // Expect IPv4 addresses
                if (!isIPv4(localAddr)) {
                    logger.warn("NetFlow localAddress is not IPv4: "+localAddr);
                    ipv4 = false;
                } else {
                    ipv4 = true;
                }
            }
            
            Integer seqNum = nfo.getInitTcpSeqNum();
            if (seqNum != null) {
                this.netFlowsWithTCPSeqNumber++;
            }
            
            Integer localPort = nfo.getLocalPort();
            if (localPort == null) {
                logger.warn("No local port");
                valid = false;
            } else if (localPort < 0 || localPort > 65535) {
                logger.warn("Invalid local port number: "+localPort);
                valid = false;
            } 
            
            CharSequence remoteAddr = nfo.getRemoteAddress();
            if (remoteAddr.length() <= 1) {
                logger.warn("NetFlow remoteAddress length < 1: "+remoteAddr);
                valid = false;
            } else {
                // Expect IPv4 addresses
                if (!isIPv4(remoteAddr)) {
                    logger.warn("NetFlow remoteAddress is not IPv4: "+remoteAddr);
                    ipv4 = false;
                } 
            }
            
            Integer remotePort = nfo.getRemotePort();
            if (remotePort == null) {
                logger.warn("No remote port");
                valid = false;
            } else if (remotePort < 0 || remotePort > 65535) {
                logger.warn("Invalid local port number: "+remotePort);
                valid = false;
            } 
            
            if (valid) {
                validNetFlowsFound++;
                if (!ipv4) {
                    logger.warn("NetFlow is not ipv4: "+jsonSerializer.serializeToJson(datum, true));
                }
            } else {
                logger.debug("Record: "+jsonSerializer.serializeToJson(datum, true));
            }
            
            if (ipv4) {
                ipv4NetFlowsFound++;
            }
      
        }
    }
    
    @Override
    public void close() {
        if (netFlowsFound == 0) {
            logger.warn("NO NetFlow records were found!");
        } else {
            logger.info(netFlowsFound+" NetFlow records were found");
        }
        
        if (validNetFlowsFound == netFlowsFound) {
            logger.info("ALL NetFlow records found were valid");
        } else if (validNetFlowsFound > 0) {
            logger.warn(validNetFlowsFound+" out of "+netFlowsFound+" NetFlow records had fully valid parameters");
        } else if (netFlowsFound > 0) {
            logger.error("NO NetFlow records had full valid parameters!");
        }
        
        if (netFlowsFound > 0) {
            if (ipv4NetFlowsFound == netFlowsFound) {
                logger.info("ALL NetFlow records were IPv4");
            } else if (ipv4NetFlowsFound > 0) {
                logger.warn(ipv4NetFlowsFound+" out of "+netFlowsFound+" NetFlow records were IPv4");
            } else {
                logger.warn("NO NetFlow records were IPv4");
            }
            
            logger.info(netFlowsWithTCPSeqNumber+" NetFlows with a Initial TCP Sequence Number were found");
        }
    }
    
}
