package com.bbn.tc.services.kafka.checker;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm18.*;
import com.bbn.tc.schema.serialization.Utils;

public class UUIDCheck extends CDMChecker {

    private static final Logger logger = Logger.getLogger(UUIDCheck.class);

    HashMap<Class<?>, HashQueue<BigIntWithEpoch>> seenUuids;    
    HashMap<Class<?>, Integer> dupCounts;
    int useBeforeDefine = 0;
    BigInteger zeroBI = BigInteger.valueOf(0);
    
    int hashQueueCapacity = 100000;
    
    public UUIDCheck() {
        seenUuids = new HashMap<Class<?>, HashQueue<BigIntWithEpoch>>();
        seenUuids.put(Subject.class, new HashQueue<BigIntWithEpoch>(hashQueueCapacity));
	seenUuids.put(Host.class, new HashQueue<BigIntWithEpoch>(hashQueueCapacity));
        seenUuids.put(Principal.class, new HashQueue<BigIntWithEpoch>(hashQueueCapacity));
        seenUuids.put(AbstractObject.class, new HashQueue<BigIntWithEpoch>(hashQueueCapacity));
        seenUuids.put(Event.class, new HashQueue<BigIntWithEpoch>(hashQueueCapacity));
        seenUuids.put(ProvenanceTagNode.class, new HashQueue<BigIntWithEpoch>(hashQueueCapacity));
        dupCounts = new HashMap<Class<?>, Integer>();
        useBeforeDefine = 0;
    }
    
    @Override
    public void initialize(Properties properties) {
        logger.info("Initializing UUIDCheck from properties");
        String qc = properties.getProperty("UUIDCheck.queueCapacity");
        if (qc != null) {
            try {
                hashQueueCapacity = Integer.parseInt(qc);
                logger.info("HashQueue capacity = "+hashQueueCapacity);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                logger.error(ex.getMessage(), ex);
            }
        }  
    }
    
    @Override
    public String description() {
        return "Check for any duplicate UUIDs or UUIDs referenced before being defined";
    }
    
    public String prettyUuid(UUID uuid) {
        if (uuid == null) {
            return "NULL";
        }
        //BigInteger bi = new BigInteger(uuid.bytes());
        //return String.valueOf(bi);
	byte[] bytes = uuid.bytes();
	return Utils.toUuidString(bytes, 0, bytes.length);
    }
  
    public BigIntWithEpoch toBigInt(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        BigInteger bi = new BigInteger(uuid.bytes());
        BigIntWithEpoch bii = new BigIntWithEpoch(bi);
        return bii;
    }

    @Override
    public void processRecord(String key, TCCDMDatum datum) throws Exception {
        Object record = datum.getDatum();
        UUID uuid = null;
        BigIntWithEpoch sUuid = null;
        boolean writeRecord = false;
        Class<?> uuidClass = record.getClass();
        
        if(record instanceof Principal) {
            uuid = ((Principal)record).getUuid();
        } else if (record instanceof Host) {
	    uuid = ((Host)record).getUuid();
	} else if (record instanceof ProvenanceTagNode) {
            ProvenanceTagNode tagNode = (ProvenanceTagNode)record;
            uuid = tagNode.getTagId();
            
            UUID flowObjId = tagNode.getFlowObject();
            HashSet<BigIntWithEpoch> objectUuids = seenUuids.get(AbstractObject.class);
            if (flowObjId != null) {
                BigIntWithEpoch fUuid = toBigInt(flowObjId);
                if (!objectUuids.contains(fUuid)) {
                    useBeforeDefine++;
                    logger.warn("Undefined UUID: ProvenanceTagNode: "+prettyUuid(uuid)+" flowObjectId: " + prettyUuid(flowObjId) + " Key: "+key);
                    writeRecord = true;
                }
            }
            
            UUID subjectId = tagNode.getSubject();
            HashSet<BigIntWithEpoch> subjectUuids = seenUuids.get(Subject.class);
            if (subjectId != null) {
                BigIntWithEpoch subUuid = toBigInt(subjectId);
                if (!subjectUuids.contains(subUuid)) {
                    useBeforeDefine++;
                    logger.warn("Undefined UUID: ProvenanceTagNode: "+prettyUuid(uuid)+" subject: " + prettyUuid(subjectId));
                    writeRecord = true;
                }
            }
            
            UUID prevId = tagNode.getPrevTagId();
            if (prevId != null) {
                HashSet<BigIntWithEpoch> tagUuids = seenUuids.get(ProvenanceTagNode.class);
                BigIntWithEpoch tUuid = toBigInt(prevId);
                if (!tagUuids.contains(tUuid)) {
                    useBeforeDefine++;
                    logger.warn("Undefined UUID: ProvenanceTagNode: "+prettyUuid(uuid)+" prevTagId: " + prettyUuid(prevId));
                    writeRecord = true;
                }
            }
            
            List<UUID> tagIds = tagNode.getTagIds();
            if (tagIds != null && !tagIds.isEmpty()) { 
                HashSet<BigIntWithEpoch> tagUuids = seenUuids.get(ProvenanceTagNode.class);
                for (UUID tagId : tagIds) {
                    BigIntWithEpoch tUuid = toBigInt(tagId);
                    if (!tagUuids.contains(tUuid)) {
                        useBeforeDefine++;
                        logger.warn("Undefined UUID: ProvenanceTagNode: "+prettyUuid(uuid)+" tagId: " + prettyUuid(tagId));
                        writeRecord = true;
                    }
                }
            }            
        }
        else if(record instanceof Subject) {
            Subject subject = (Subject)record;
            uuid = subject.getUuid();
           
            if (subject.getParentSubject() != null) {
                HashSet<BigIntWithEpoch> subjectUuids = seenUuids.get(Subject.class);
                BigIntWithEpoch psUuid = toBigInt(subject.getParentSubject());
                if (!subjectUuids.contains(psUuid) && !uuid.equals(subject.getParentSubject())) {
                    useBeforeDefine++;
                    logger.warn("Undefined UUID: Subject "+prettyUuid(uuid)+" parentSubject: " + prettyUuid(subject.getParentSubject()));
                    writeRecord = true;
                }
            }
            
            HashSet<BigIntWithEpoch> principalUuids = seenUuids.get(Principal.class);
	    // Can Hosts be principals too?  Some TA1s do this, allow it for now
            HashSet<BigIntWithEpoch> hostUuids = seenUuids.get(Host.class);
            if (subject.getLocalPrincipal() != null) {
                BigIntWithEpoch pUuid = toBigInt(subject.getLocalPrincipal());
                if (!principalUuids.contains(pUuid) && !hostUuids.contains(pUuid)) {
                    useBeforeDefine++;
                    logger.warn("Undefined UUID: Subject "+prettyUuid(uuid)+" localPrincipal: " + prettyUuid(subject.getLocalPrincipal()));
                    writeRecord = true;
                }
            }
                   
        }
        else if(record instanceof FileObject) {
            uuidClass = AbstractObject.class;
            FileObject fileObject = (FileObject)record;
            uuid = fileObject.getUuid();
            Integer epoch = fileObject.getBaseObject().getEpoch();
            
            if (epoch != null) {
                sUuid = toBigInt(uuid);
                sUuid.setEpoch(epoch.shortValue());
            }
            
            if (fileObject.getLocalPrincipal() != null) {
                HashSet<BigIntWithEpoch> principalUuids = seenUuids.get(Principal.class);
                BigIntWithEpoch pUuid = toBigInt(fileObject.getLocalPrincipal());
                if (!principalUuids.contains(pUuid)) {
                    useBeforeDefine++;
                    logger.warn("Undefined UUID: FileObject "+sUuid+" localPrincipal: " + prettyUuid(fileObject.getLocalPrincipal()));
                    writeRecord = true;
                }
            }
        }
        else if(record instanceof UnnamedPipeObject) {
            uuidClass = AbstractObject.class;
            UnnamedPipeObject upObject = (UnnamedPipeObject)record;
            uuid = upObject.getUuid();
            Integer epoch = upObject.getBaseObject().getEpoch();
            if (epoch != null) {
                sUuid = toBigInt(uuid);
                sUuid.setEpoch(epoch.shortValue());
            }
        }
        else if(record instanceof RegistryKeyObject) {
            uuidClass = AbstractObject.class;
            RegistryKeyObject rkObject = (RegistryKeyObject)record;
            uuid = rkObject.getUuid();
            Integer epoch = rkObject.getBaseObject().getEpoch();
            if (epoch != null) {
                sUuid = toBigInt(uuid);
                sUuid.setEpoch(epoch.shortValue());
            }
        }
        else if(record instanceof NetFlowObject) {
            uuidClass = AbstractObject.class;
            NetFlowObject nObject = (NetFlowObject)record;
            uuid = nObject.getUuid();
            Integer epoch = nObject.getBaseObject().getEpoch();
            if (epoch != null) {
                sUuid = toBigInt(uuid);
                sUuid.setEpoch(epoch.shortValue());
            }
        }
        else if(record instanceof MemoryObject) {
            uuidClass = AbstractObject.class;
            MemoryObject mObject = (MemoryObject)record;
            uuid = mObject.getUuid();
            Integer epoch = mObject.getBaseObject().getEpoch();
            if (epoch != null) {
                sUuid = toBigInt(uuid);
                sUuid.setEpoch(epoch.shortValue());
            }
        }
        else if(record instanceof SrcSinkObject) {
            uuidClass = AbstractObject.class;
            SrcSinkObject ssObject = (SrcSinkObject)record;
            uuid = ssObject.getUuid();
            Integer epoch = ssObject.getBaseObject().getEpoch();
            if (epoch != null) {
                sUuid = toBigInt(uuid);
                sUuid.setEpoch(epoch.shortValue());
            }
        }
        else if(record instanceof Event) {
            Event event = (Event)record;
            uuid = event.getUuid();
            sUuid = toBigInt(uuid);
            
            UUID subjectUuid = event.getSubject();
            if (subjectUuid != null) {
                HashSet<BigIntWithEpoch> subjectUuids = seenUuids.get(Subject.class);
                BigIntWithEpoch pUuid = toBigInt(subjectUuid);
                if (!pUuid.getUuid().equals(zeroBI) && !subjectUuids.contains(pUuid)) {
                    useBeforeDefine++;
                    logger.warn("Undefined UUID: Event "+prettyUuid(uuid)+" subject: " + prettyUuid(subjectUuid));
                    writeRecord = true;
                }
            }
            UUID objectUuid = event.getPredicateObject();
            if (objectUuid != null) {
                BigIntWithEpoch oUuid = toBigInt(objectUuid);
                // this uuid could be a subject, object, principal, (event?)
                HashSet<BigIntWithEpoch> subjectUuids = seenUuids.get(Subject.class);
                HashSet<BigIntWithEpoch> principalUuids = seenUuids.get(Principal.class);
                HashSet<BigIntWithEpoch> objectUuids = seenUuids.get(AbstractObject.class);
                if (!oUuid.getUuid().equals(zeroBI) && !subjectUuids.contains(oUuid) && !principalUuids.contains(oUuid) && !objectUuids.contains(oUuid)) {
                    useBeforeDefine++;
                    logger.warn("Undefined UUID: Event "+prettyUuid(uuid)+" object: " + prettyUuid(objectUuid));
                    writeRecord = true;
                }
            }
            
            objectUuid = event.getPredicateObject2();
            if (objectUuid != null) {
                BigIntWithEpoch oUuid = toBigInt(objectUuid);
                // this uuid could be a subject, object, principal, (event?)
                HashSet<BigIntWithEpoch> subjectUuids = seenUuids.get(Subject.class);
                HashSet<BigIntWithEpoch> principalUuids = seenUuids.get(Principal.class);
                HashSet<BigIntWithEpoch> objectUuids = seenUuids.get(AbstractObject.class);
                if (!oUuid.getUuid().equals(zeroBI) && !subjectUuids.contains(oUuid) && !principalUuids.contains(oUuid) && !objectUuids.contains(oUuid)) {
                    useBeforeDefine++;
                    logger.warn("Undefined UUID: Event "+prettyUuid(uuid)+" object2: " + prettyUuid(objectUuid));
                    writeRecord = true;
                }
            }
        } else if (record instanceof UnitDependency) {
            UnitDependency uDepend = (UnitDependency)record;
            UUID unitUuid = uDepend.getUnit();
            
            // this uuid could be a subject, object, principal, (event?)
            HashSet<BigIntWithEpoch> subjectUuids = seenUuids.get(Subject.class);
            HashSet<BigIntWithEpoch> principalUuids = seenUuids.get(Principal.class);
            HashSet<BigIntWithEpoch> objectUuids = seenUuids.get(AbstractObject.class);
            HashSet<BigIntWithEpoch> eventUuids = seenUuids.get(Event.class);
            if (unitUuid != null) {
                BigIntWithEpoch uuUuid = toBigInt(unitUuid);
                if (!subjectUuids.contains(uuUuid) && !principalUuids.contains(uuUuid) && !objectUuids.contains(uuUuid) &&
                        !eventUuids.contains(uuUuid)) {
                    useBeforeDefine++;
                    logger.warn("Undefined UUID: UnitDependency unit: " + prettyUuid(unitUuid));
                    writeRecord = true;
                }
            }
            UUID dUuid = uDepend.getDependentUnit();
            if (dUuid != null) {
                BigIntWithEpoch uuUuid = toBigInt(dUuid);
                if (!subjectUuids.contains(uuUuid) && !principalUuids.contains(uuUuid) && !objectUuids.contains(uuUuid) &&
                        !eventUuids.contains(uuUuid)) {
                    useBeforeDefine++;
                    logger.warn("Undefined UUID: UnitDependency dependent unit: " + prettyUuid(dUuid));
                    writeRecord = true;
                }
            }            
        }
        
        if (writeRecord) {
            logger.debug("Record: "+jsonSerializer.serializeToJson(datum, true));
        }    

        if (uuid != null || sUuid != null) {
            HashSet<BigIntWithEpoch> typeUuids = seenUuids.get(uuidClass);
            if (typeUuids == null) {
                logger.error("Logic error in the UUIDChecker: "+uuidClass+" not in the seen UUID table!");
                return;
            }
            if (sUuid == null) {
                sUuid = toBigInt(uuid);
            }
            if (typeUuids.contains(sUuid)) {
		if (uuid != null) {
		    logger.warn("Duplicate "+record.getClass().getSimpleName()+" UUID: "+prettyUuid(uuid));
		} else {
		    logger.warn("Duplicate "+record.getClass().getSimpleName()+" UUID: "+sUuid);
		}
                logger.debug("Record: "+jsonSerializer.serializeToJson(datum, true));
                Integer dCount = dupCounts.get(record.getClass());
                if (dCount == null) {
                    dCount = new Integer(1);
                } else {
                    dCount++;
                }
                dupCounts.put(record.getClass(), dCount);
            } else {
                typeUuids.add(sUuid);
            }
        }
    }

    @Override
    public boolean issuesFound() {
        if (useBeforeDefine > 0) {
            return true;
        }
        for (Integer count : dupCounts.values()) {
            if (count > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        if (issuesFound()) {
            for (Class<?> key : dupCounts.keySet()) {
                Integer val = dupCounts.get(key);
                if (val > 0) {
                    logger.warn("Duplicate "+key.getSimpleName()+" UUIDs found: "+val);
                } else {
                    logger.info("NO duplicate "+key.getSimpleName()+" UUIDs found");
                }
            }
        } else {
            logger.info("NO duplicate UUIDs found");
        }
        
        if (useBeforeDefine > 0) {
            logger.warn("Use before define cases: "+useBeforeDefine);
        } else {
            logger.info("NO cases of using a UUID before defining it");
        }
    }    

}
