package com.bbn.tc.services.kafka.checker;

import java.math.BigInteger;
import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm20.*;
import com.bbn.tc.schema.serialization.Utils;

public class NullValueCheck extends CDMChecker {

    private static final Logger logger = Logger.getLogger(NullValueCheck.class);

    int nullValueCount = 0;
    
    public NullValueCheck() {
        nullValueCount = 0;
    }
    
    @Override
    public String description() {
        return "Check for any required fields having a null value";
    }
    
    public String prettyUuid(UUID uuid) {
        if (uuid == null) {
            return "NULL";
        }
	byte[] bytes = uuid.bytes();
	return Utils.toUuidString(bytes, 0, bytes.length);
    }

    @Override
    public void processRecord(String key, TCCDMDatum datum) throws Exception {
        Object record = datum.getDatum();
        UUID uuid = null;
        String sUuid = null;
        boolean writeRecord = false;
        
        if(record instanceof Principal) {
            Principal principal = (Principal)record;
            uuid = principal.getUuid();
            sUuid = prettyUuid(uuid);
            if (uuid == null) {
                logger.warn("NULL uuid for Principal: "+key);
                nullValueCount++;
                writeRecord = true;
            }
            
            if (principal.getUserId() == null) {
                logger.warn("NULL userId for Principal: "+sUuid);
                nullValueCount++;
                writeRecord = true;
            }            
        } else if (record instanceof ProvenanceTagNode) {
            ProvenanceTagNode tagNode = (ProvenanceTagNode)record;
            uuid = tagNode.getTagId();
            sUuid = prettyUuid(uuid);
            if (tagNode.getSubject() == null) {
                logger.warn("NULL subject for ProvenanceTagNode: "+sUuid);
                nullValueCount++;
                writeRecord = true;
            }
        }
        else if(record instanceof Subject) {
            Subject subject = (Subject)record;
            uuid = subject.getUuid();
            sUuid = prettyUuid(uuid);
            
            if (subject.getLocalPrincipal() == null) {
                logger.warn("NULL localPrincipal for Subject: "+sUuid);
                nullValueCount++;
                writeRecord = true;
            }                   
        }
        else if(record instanceof FileObject) {
            FileObject fileObject = (FileObject)record;
            uuid = fileObject.getUuid();
            // Everything can be null
        }
        else if(record instanceof IpcObject) {
            IpcObject upObject = (IpcObject)record;
            uuid = upObject.getUuid();
            // Could check fileDescriptors for valid values
        }
        else if(record instanceof RegistryKeyObject) {
            RegistryKeyObject rkObject = (RegistryKeyObject)record;
            uuid = rkObject.getUuid();
            sUuid = prettyUuid(uuid);
            if (rkObject.getKey() == null) {
                logger.warn("NULL key for RegistryKeyObject: "+sUuid);
                nullValueCount++;
                writeRecord = true;
            }
        }
        else if(record instanceof NetFlowObject) {
            NetFlowObject nObject = (NetFlowObject)record;
            uuid = nObject.getUuid();
            sUuid = prettyUuid(uuid);
            if (nObject.getLocalAddress() == null) {
                logger.warn("NULL localAddress for NetFlowObject: "+sUuid);
                nullValueCount++;
                writeRecord = true;
            }
            if (nObject.getRemoteAddress() == null) {
                logger.warn("NULL remoteAddress for NetFlowObject: "+sUuid);
                nullValueCount++;
                writeRecord = true;
            }
        }
        else if(record instanceof MemoryObject) {
            MemoryObject mObject = (MemoryObject)record;
            uuid = mObject.getUuid();
        }
        else if(record instanceof SrcSinkObject) {
            SrcSinkObject ssObject = (SrcSinkObject)record;
            uuid = ssObject.getUuid();
        }
        else if(record instanceof Event) {
            Event event = (Event)record;
            uuid = event.getUuid();
            sUuid = prettyUuid(uuid);
            
            UUID subjectUuid = event.getSubject();
            if (event.getType() != EventType.EVENT_ADD_OBJECT_ATTRIBUTE) {
                if (subjectUuid == null) {
                    logger.warn("NULL subject for Event: "+sUuid);
                    nullValueCount++;
                    writeRecord = true;
                }
            }
        } else if (record instanceof UnitDependency) {
            UnitDependency uDepend = (UnitDependency)record;
            UUID unitUuid = uDepend.getUnit();
            if (unitUuid == null) {
                logger.warn("NULL unit for UnitDependency: "+sUuid);
                nullValueCount++;
                writeRecord = true;
            }
            UUID dUuid = uDepend.getDependentUnit();
            if (dUuid == null) {
                logger.warn("NULL dependentUnit for UnitDependency: "+sUuid);
                nullValueCount++;
                writeRecord = true;
            }            
        }
        
        if (writeRecord) {
            logger.debug("Record: "+jsonSerializer.serializeToJson(datum, true));
        }    
    }

    @Override
    public boolean issuesFound() {
        if (nullValueCount > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        if (nullValueCount > 0) {
            logger.warn("Null value for required fields count: "+nullValueCount);
        } else {
            logger.info("NO cases of a missing required value");
        }
    }    

}
