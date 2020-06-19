    /*
     * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
     */

    package com.bbn.tc.schema.utils;

    import com.bbn.tc.schema.avro.*;
    import com.bbn.tc.schema.avro.cdm20.UUID;
    import com.bbn.tc.schema.avro.cdm20.*;
    import org.apache.avro.Schema;
    import org.apache.avro.generic.*;
    import org.apache.log4j.Logger;

    import java.nio.ByteBuffer;
    import java.util.*;

    /**
     * A helper class for generating records
     * @author jkhoury
     */
    public class RecordGenerator {

        private static final Logger logger = Logger.getLogger(RecordGenerator.class);

        static Random random;

        static {
            random = new Random(System.nanoTime());
        }

        /**
         * Create a random record based on the schema.
         * This method is specific to the schema which is why
         * it is located in this package. Other packages can use
         * it to create random records for testing without
         * having to worry about the schema itself if and when
         * that is desired. The values of the record fields should
         * not be of any interest to the caller
         *
         * @param recordSchema the schema of the record to be created
         * @param numKVPairs the number of random k/v pairs to add to the record
         * @param compiled if true, returns an instance of the compiled record object,
         *                 otherwise returns a GenericData.Record instance
         * @return a random record following the schema
         */
        public static GenericContainer randomEdgeRecord(Schema recordSchema, int numKVPairs, boolean compiled)
                            throws Exception{
            if(recordSchema == null) throw new IllegalArgumentException("Null schema");
            if(recordSchema.getType() != Schema.Type.RECORD)
                throw new IllegalArgumentException("Schema type must be a record, instead found "
                        +recordSchema.getType());

            if(Constants.EDGE_SCHEMA_FULLNAME.equals(recordSchema.getFullName())) {
                NODE_LABELS[] nodeLabels = NODE_LABELS.values();
                EDGE_LABELS[] edgeLabels = EDGE_LABELS.values();
                GenericContainer record;

                Map props = getKVPairs(numKVPairs);

                // Build a instance of a compiled Record
                if (compiled) {
                    LabeledNode from = LabeledNode.newBuilder().setId(random.nextLong()).
                            setLabel(nodeLabels[random.nextInt(nodeLabels.length)]).
                            build();
                    LabeledNode to = LabeledNode.newBuilder().setId(random.nextLong()).
                            setLabel(nodeLabels[random.nextInt(nodeLabels.length)]).
                            build();
                    record = LabeledEdge.newBuilder().setFromNode(from).
                            setToNode(to).setLabel(edgeLabels[random.nextInt(edgeLabels.length)])
                            .setProperties(props)
                            .build();
                    return record;
                }

                //Build an instance of a Generic record
                GenericRecordBuilder erb = new GenericRecordBuilder(recordSchema);
                Schema nodeSchema = recordSchema.getField("fromNode").schema();
                GenericRecordBuilder nrb = new GenericRecordBuilder(nodeSchema);

                GenericData.Record from = nrb
                        .set("label", asEnum(nodeSchema, nodeLabels[random.nextInt(nodeLabels.length)].toString()))
                        .set("id", random.nextLong()).build();
                GenericData.Record to = nrb.set("label",
                                        asEnum(nodeSchema,nodeLabels[random.nextInt(nodeLabels.length)].toString()))
                        .set("id", random.nextLong()).build();
                record = erb.set("fromNode", from).set("toNode", to).
                        set("label", asEnum(recordSchema, edgeLabels[random.nextInt(edgeLabels.length)].toString())).
                        set("properties", props).
                        build();
                return record;
            }else if(Constants.TCCDM_SCHEMA_FULLNAME.equals(recordSchema.getFullName())) {
                RecordType[] recordTypes = RecordType.values();
                SubjectType[] subjectTypes = SubjectType.values();
                SrcSinkType[] sensorTypes = SrcSinkType.values();
                HostType[] hostTypes = HostType.values();
                PrincipalType[] principalTypes = PrincipalType.values();
                EventType[] eventTypes = EventType.values();
                FileObjectType[] fileObjectTypes = FileObjectType.values();
                IpcObjectType[] ipcObjectTypes = IpcObjectType.values();
                InstrumentationSource[] instrumentationSources = InstrumentationSource.values();
                TagOpCode tagOpCodes[] = TagOpCode.values();
                ConfidentialityTag [] confTags = ConfidentialityTag.values();
                IntegrityTag [] intTags = IntegrityTag.values();

                Map props = getKVPairs(numKVPairs);

                List<Schema> datumSchemas = recordSchema.getField("datum").schema().getTypes(); // this is a union
                //pick a schema at random
                Schema datumSchema = datumSchemas.get(random.nextInt(datumSchemas.size()));

                // Build a instance of a compiled Record
                if (compiled) {
                    TCCDMDatum tccdmDatum;
                    TCCDMDatum.Builder datumBuilder = null;
                    RecordType recordType = recordTypes[random.nextInt(recordTypes.length)];
                    UUID hostId = SchemaUtils.toUUID(random.nextLong());
                    InstrumentationSource source = instrumentationSources[random.nextInt(instrumentationSources.length)];
                    datumBuilder = TCCDMDatum.newBuilder()
                            .setType(recordType)
                            .setHostId(hostId)
                            .setSessionNumber(random.nextInt())
                            .setSource(source);
                    if (datumSchema.getName().equals("Host")) {
                        UUID uuid = SchemaUtils.toUUID(random.nextLong());
                        List<HostIdentifier> hostIdentifiers = new ArrayList<>();
                        for (int i = 0; i < random.nextInt(5); i++) {
                            hostIdentifiers.add(randomHostIdentifier());
                        }
                        HostType hostType = hostTypes[random.nextInt(hostTypes.length)];
                        List<Interface> interfaces = new ArrayList<>();
                        for (int i = 0; i < random.nextInt(5); i++) {
                            interfaces.add(randomInterface());
                        }
                        Host host = Host.newBuilder()
                                .setUuid(uuid)
                                .setHostName(new String("somehost-" + random.nextInt()))
                                .setTa1Version(new String("somedate somehash"))
                                .setHostIdentifiers(hostIdentifiers)
                                .setOsDetails(new String("some OS details"))
                                .setHostType(hostType)
                                .setInterfaces(interfaces)
                                .build();
                        datumBuilder.setDatum(host);
                    } else if (datumSchema.getName().equals("Principal")) {
                        UUID uuid = SchemaUtils.toUUID(random.nextLong());
                        PrincipalType type = principalTypes[random.nextInt(principalTypes.length)];
                        String userId = "S-1-5-21-3623811015-3361044348-30300820-"+random.nextInt(10000); // windows SID
                        String username = "someuser";
                        List<CharSequence> groupIds = new ArrayList<>();
                        for (int i = 0; i < random.nextInt(5); i++) {
                            groupIds.add("" + random.nextInt(100000));
                        }
                        Principal principal = Principal.newBuilder()
                                .setUuid(uuid)
                                .setType(type)
                                .setUserId(userId)
                                .setUsername(username)
                                .setGroupIds(groupIds)
                                .setProperties(props)
                                .build();
                        datumBuilder.setDatum(principal);
                    } else if(datumSchema.getName().equals("ProvenanceTagNode")) {
                        UUID tagId = SchemaUtils.toUUID(random.nextLong());
                        UUID flowObject = SchemaUtils.toUUID(random.nextLong());
                        UUID subject = SchemaUtils.toUUID(random.nextLong());
                        UUID prevTagId = SchemaUtils.toUUID(random.nextLong());
                        TagOpCode tagOpCode = tagOpCodes[random.nextInt(tagOpCodes.length)];
                        UUID tagIdOp1 = SchemaUtils.toUUID(random.nextLong());
                        UUID tagIdOp2 = SchemaUtils.toUUID(random.nextLong());
                        List<UUID> tagIdOps = new ArrayList<>();
                        tagIdOps.add(tagIdOp1);
                        tagIdOps.add(tagIdOp2);
                        IntegrityTag itag = intTags[random.nextInt(intTags.length)];
                        ConfidentialityTag ctag = confTags[random.nextInt(confTags.length)];
                        ProvenanceTagNode tagNode = ProvenanceTagNode.newBuilder()
                                .setTagId(tagId)
                                .setFlowObject(flowObject)
                                .setSubject(subject)
                                .setPrevTagId(prevTagId)
                                .setOpcode(tagOpCode)
                                .setTagIds(tagIdOps)
                                .setItag(itag)
                                .setCtag(ctag)
                                .setProperties(props)
                                .build();
                        datumBuilder.setDatum(tagNode);
                    } else if(datumSchema.getName().equals("UnknownProvenanceNode")){
                        UUID tagId = SchemaUtils.toUUID(random.nextLong());
                        UUID subject = SchemaUtils.toUUID(random.nextLong());
                        UUID prevTagId = SchemaUtils.toUUID(random.nextLong());
                        UnknownProvenanceNode unknownProvenanceNode = UnknownProvenanceNode.newBuilder()
                                .setUpnTagId(tagId)
                                .setProperties(props)
                                .build();
                        datumBuilder.setDatum(unknownProvenanceNode);
                    } else if(datumSchema.getName().equals("Subject")){
                        UUID uuid = SchemaUtils.toUUID(random.nextLong());
                        SubjectType type = subjectTypes[random.nextInt(subjectTypes.length)];
                        UUID parentSubject = SchemaUtils.toUUID(random.nextLong());
                        UUID localPrincipal = SchemaUtils.toUUID(random.nextLong());
                        long startTimestampNanos = random.nextLong();
                        Subject subject = Subject.newBuilder()
                                .setUuid(uuid)
                                .setType(type)
                                .setCid(random.nextInt())
                                .setParentSubject(parentSubject)
                                .setLocalPrincipal(localPrincipal)
                                .setStartTimestampNanos(startTimestampNanos)
                                .setProperties(props)
                                .build();
                        datumBuilder.setDatum(subject);
                    } else if(datumSchema.getName().equals("FileObject")){
                        UUID uuid = SchemaUtils.toUUID(random.nextLong());
                        AbstractObject baseObject = randomAbstractObject();
                        FileObjectType type = fileObjectTypes[random.nextInt(fileObjectTypes.length)];
                        UUID localPrincipal = SchemaUtils.toUUID(random.nextLong());
                        FileObject fileObject = FileObject.newBuilder()
                                .setUuid(uuid)
                                .setBaseObject(baseObject)
                                .setType(type)
                                .setLocalPrincipal(localPrincipal)
                                .build();
                        datumBuilder.setDatum(fileObject);
                    } else if(datumSchema.getName().equals("IpcObject")){
                        UUID uuid = SchemaUtils.toUUID(random.nextLong());
                        AbstractObject baseObject = randomAbstractObject();
                        IpcObjectType type = ipcObjectTypes[random.nextInt(ipcObjectTypes.length)];
                        IpcObject ipcPipeObject = IpcObject.newBuilder()
                                .setUuid(uuid)
                                .setBaseObject(baseObject)
                                .setType(type)
                                .setUuid1(uuid)
                                .setUuid2(uuid)
                                .build();
                        datumBuilder.setDatum(ipcPipeObject);
                    } else if(datumSchema.getName().equals("RegistryKeyObject")) {
                        UUID uuid = SchemaUtils.toUUID(random.nextLong());
                        AbstractObject baseObject = randomAbstractObject();
                        RegistryKeyObject rko = RegistryKeyObject.newBuilder()
                                .setBaseObject(baseObject)
                                .setUuid(uuid)
                                .setKey("HKEY_LOCAL_ MACHINE\\SOFTWARE\\Policies\\Microsoft\\Windows\\System\\"
                                        + random.nextInt())
                                .build();
                        datumBuilder.setDatum(rko);
                    } else if(datumSchema.getName().equals("PacketSocketObject")){
                        UUID uuid = SchemaUtils.toUUID(random.nextLong());
                        AbstractObject baseObject = randomAbstractObject();
                        byte[] shortBytes = new byte[SHORT.getClassSchema().getFixedSize()];
                        random.nextBytes(shortBytes);
                        SHORT proto = new SHORT(shortBytes);
                        random.nextBytes(shortBytes);
                        SHORT haType = new SHORT(shortBytes);
                        byte[] oneByte = new byte[BYTE.getClassSchema().getFixedSize()];
                        random.nextBytes(oneByte);
                        BYTE pktType = new BYTE(oneByte);
                        random.nextBytes(oneByte);
                        byte[] addrBytes = new byte[random.nextInt(8)];
                        random.nextBytes(addrBytes);
                        PacketSocketObject packetSocketObject = PacketSocketObject.newBuilder()
                                .setUuid(uuid)
                                .setBaseObject(baseObject)
                                .setProto(proto)
                                .setIfIndex(random.nextInt())
                                .setHaType(haType)
                                .setPktType(pktType)
                                .setAddr(ByteBuffer.wrap(addrBytes))
                                .build();
                        datumBuilder.setDatum(packetSocketObject);
                    } else if(datumSchema.getName().equals("NetFlowObject")){
                        UUID uuid = SchemaUtils.toUUID(random.nextLong());
                        AbstractObject baseObject = randomAbstractObject();
                        String localAddress = random.nextInt(255) + "." + random.nextInt(255) + "."
                                + random.nextInt(255) + "." + random.nextInt(255);
                        int localPort = random.nextInt();
                        String remoteAddress = random.nextInt(255) + "." + random.nextInt(255) + "."
                                + random.nextInt(255) + "." + random.nextInt(255);
                        int remotePort = random.nextInt();
                        NetFlowObject netFlowObject = NetFlowObject.newBuilder().setUuid(uuid)
                                .setBaseObject(baseObject)
                                .setLocalAddress(localAddress)
                                .setLocalPort(localPort)
                                .setRemoteAddress(remoteAddress)
                                .setRemotePort(remotePort)
                                .setIpProtocol(6) //TCP
                                .build();
                        datumBuilder.setDatum(netFlowObject);
                    } else if(datumSchema.getName().equals("MemoryObject")){
                        UUID uuid = SchemaUtils.toUUID(random.nextLong());
                        AbstractObject baseObject = randomAbstractObject();
                        MemoryObject memoryObject = MemoryObject.newBuilder().setUuid(uuid)
                                .setBaseObject(baseObject)
                                .setMemoryAddress(random.nextLong())
                                .setPageOffset(random.nextLong())
                                .setPageNumber(random.nextLong())
                                .build();
                        datumBuilder.setDatum(memoryObject);
                    } else if(datumSchema.getName().equals("SrcSinkObject")){
                        UUID uuid = SchemaUtils.toUUID(random.nextLong());
                        AbstractObject baseObject = randomAbstractObject();
                        SrcSinkType type = sensorTypes[random.nextInt(sensorTypes.length)];
                        SrcSinkObject sensorObject = SrcSinkObject.newBuilder()
                                .setUuid(uuid)
                                .setBaseObject(baseObject)
                                .setType(type)
                                .build();
                        datumBuilder.setDatum(sensorObject);
                    } else if(datumSchema.getName().equals("Event")){
                        // flip a coin, and either create a simple event (no parameters) or one with tagged parameters
                        UUID uuid = SchemaUtils.toUUID(random.nextLong());
                        long sequence = random.nextLong();
                        EventType type = eventTypes[random.nextInt(eventTypes.length)];
                        UUID subject = SchemaUtils.toUUID(random.nextLong());
                        UUID predicateObject = SchemaUtils.toUUID(random.nextLong());
                        long timestampNanos = random.nextLong();
                        Event.Builder builder = Event.newBuilder()
                                .setUuid(uuid)
                                .setSequence(sequence)
                                .setType(type)
                                .setThreadId(random.nextInt())
                                .setSubject(subject)
                                .setPredicateObject(predicateObject)
                                .setTimestampNanos(timestampNanos)
                                .setProperties(props);
                        if(random.nextBoolean() == true) { //complex event with tagged params
                            List<Value> vals = new ArrayList<>();
                            // first value is a string ( modeled as a char[10]), note that size=10 representing the number of elements (not bytes)
                            byte[] val1Bytes = "abcdefghiß".getBytes("UTF_32BE"); // note the encoding is UTF32_BE (4 bytes per char)
                            List<TagRunLengthTuple> val1Tag = new ArrayList<>();
                            val1Tag.add(new TagRunLengthTuple(5, SchemaUtils.toUUID(random.nextLong())));
                            val1Tag.add(new TagRunLengthTuple(5, SchemaUtils.toUUID(random.nextLong())));
                            Value value1 = Value.newBuilder()
                                    .setType(ValueType.VALUE_TYPE_SRC)
                                    .setValueDataType(ValueDataType.VALUE_DATA_TYPE_CHAR) // char[]
                                    .setSize(10) // char[10] even though it is converted to a 40 byte array underneath
                                    .setValueBytes(ByteBuffer.wrap(val1Bytes)) // this is the actual bytes BIG_ENDIAN, each char is 4 bytes
                                    .setTag(val1Tag).build();
                            vals.add(value1);
                            // second value is a long
                            byte[] val2Bytes = ByteBuffer.allocate(8).putLong(random.nextLong()).array();
                            List<TagRunLengthTuple> val2Tag = new ArrayList<>();
                            val2Tag.add(new TagRunLengthTuple(1, SchemaUtils.toUUID(random.nextLong())));
                            Value value2 = Value.newBuilder()
                                    .setType(ValueType.VALUE_TYPE_SINK)
                                    .setName("val2Name")
                                    .setValueDataType(ValueDataType.VALUE_DATA_TYPE_LONG) // a long value
                                    .setSize(0) // a primitive long value has size 0
                                    .setValueBytes(ByteBuffer.wrap(val2Bytes))
                                    .setTag(val2Tag).build();
                            vals.add(value2);
                            // third value is a complex type, comprising a String (char[]) and a long
                            // same applies to String [], it is treated as a complex type made of component strings
                            List<Value> componentValues = new ArrayList<>();
                            componentValues.add(value1); // the string (reusing string above instead of creating new one)
                            componentValues.add(value2); // the long (reusing long above instead of creating new one)
                            Value value3 = Value.newBuilder()
                                    .setType(ValueType.VALUE_TYPE_CONTROL)
                                    .setValueDataType(ValueDataType.VALUE_DATA_TYPE_COMPLEX) // a complex value (String[])
                                    .setSize(0)
                                    .setComponents(componentValues)
                                    .build();
                            vals.add(value3);
                            builder.setParameters(vals);
                        }
                        datumBuilder.setDatum(builder.build());
                    } else if(datumSchema.getName().equals("UnitDependency")){
                        UUID uuid = SchemaUtils.toUUID(random.nextLong());
                        UnitDependency unitDependency = UnitDependency.newBuilder()
                                .setUnit(uuid)
                                .setDependentUnit(uuid)
                                .build();
                        datumBuilder.setDatum(unitDependency);
                    } else if(datumSchema.getName().equals("TimeMarker")){
                        TimeMarker timeMarker = TimeMarker.newBuilder()
                                .setTsNanos(random.nextLong())
                                .build();
                        datumBuilder.setDatum(timeMarker);

                    } else if(datumSchema.getName().equals("EndMarker")) {
                        Map recordCounts = getKVPairs(numKVPairs+1); // +1 to ensure > 0; recordCounts is not optional
                        EndMarker endMarker = EndMarker.newBuilder()
                                .setSessionNumber(random.nextInt())
                                .setRecordCounts(recordCounts)
                                .build();
                        datumBuilder.setDatum(endMarker);
                    }
                    tccdmDatum = datumBuilder.build();
                    return tccdmDatum;
                }

                //Build an instance of a Generic record
                GenericContainer record;
                GenericRecordBuilder cdmrb = new GenericRecordBuilder(recordSchema);
                GenericRecordBuilder nrb = new GenericRecordBuilder(datumSchema);
                Schema uuidSchema = SchemaUtils.getTypeSchemaByName(recordSchema, "com.bbn.tc.schema.avro.cdm20.UUID", true);
                byte [] uuidbytes = new byte[UUID.getClassSchema().getFixedSize()];
                random.nextBytes(uuidbytes);
                GenericData.Fixed uuid = new GenericData.Fixed(uuidSchema, uuidbytes);

                if(datumSchema.getName().equals("Host")) {
                    List<HostIdentifier> hostIdentifiers = new ArrayList<>();
                    for (int i = 0; i < random.nextInt(5); i++) {
                        hostIdentifiers.add(randomHostIdentifier());
                    }
                    List<Interface> interfaces = new ArrayList<>();
                    for (int i = 0; i < random.nextInt(5); i++) {
                        interfaces.add(randomInterface());
                    }
                    nrb.set("uuid", uuid)
                            .set("hostName", new String("somehost-" + random.nextInt()))
                            .set("ta1Version", new String("somedate somehash"))
                            .set("hostIdentifiers", hostIdentifiers)
                            .set("osDetails", new String("some OS details"))
                            .set("hostType", asEnum(datumSchema, hostTypes[random.nextInt(hostTypes.length)].toString()))
                            .set("interfaces", interfaces);
                } else if(datumSchema.getName().equals("Principal")){
                    ArrayList<String> gids = new ArrayList<>();
                    for (int i = 0; i < random.nextInt(5); i++) {
                        gids.add(""+random.nextInt());
                    }
                    nrb.set("uuid", uuid)
                            .set("type", asEnum(datumSchema, principalTypes[random.nextInt(principalTypes.length)].toString()))
                            .set("userId", ""+random.nextInt())
                            .set("groupIds", gids)
                            .set("properties", props);
                } else if(datumSchema.getName().equals("ProvenanceTagNode")){
                    nrb.set("tagId", uuid)
                            .set("flowObject", uuid)
                            .set("subject", uuid);
                } else if(datumSchema.getName().equals("UnknownProvenanceNode")){
                    nrb.set("upnTagId", uuid);
                } else if(datumSchema.getName().equals("Subject")){
                    nrb.set("uuid", uuid)
                            .set("type", asEnum(datumSchema, subjectTypes[random.nextInt(subjectTypes.length)].toString()))
                            .set("cid", random.nextInt())
                            .set("parentSubject", uuid)
                            .set("localPrincipal", uuid)
                            .set("startTimestampNanos", random.nextLong())
                            .set("properties", props);
                } else if(datumSchema.getName().equals("FileObject")){
                    nrb.set("uuid", uuid)
                            .set("baseObject", randomAbstractObject(uuid, datumSchema.getField("baseObject").schema()))
                            .set("type", asEnum(datumSchema, fileObjectTypes[random.nextInt(fileObjectTypes.length)].toString()));
                } else if(datumSchema.getName().equals("IpcObject")){
                    nrb.set("uuid", uuid)
                            .set("baseObject", randomAbstractObject(uuid, datumSchema.getField("baseObject").schema()))
                            .set("type", asEnum(datumSchema, ipcObjectTypes[random.nextInt(ipcObjectTypes.length)].toString()))
                            .set("uuid1", uuid)
                            .set("uuid2", uuid);
                } else if(datumSchema.getName().equals("RegistryKeyObject")) {
                    nrb.set("uuid", uuid)
                            .set("baseObject", randomAbstractObject(uuid, datumSchema.getField("baseObject").schema()))
                            .set("key", "HKEY_LOCAL_ MACHINE\\SOFTWARE\\Policies\\Microsoft\\Windows\\System\\"
                                    + random.nextInt());
                } else if(datumSchema.getName().equals("PacketSocketObject")) {
                    byte[] shortBytes = new byte[SHORT.getClassSchema().getFixedSize()];
                    random.nextBytes(shortBytes);
                    SHORT proto = new SHORT(shortBytes);
                    random.nextBytes(shortBytes);
                    SHORT haType = new SHORT(shortBytes);
                    byte[] oneByte = new byte[BYTE.getClassSchema().getFixedSize()];
                    random.nextBytes(oneByte);
                    BYTE pktType = new BYTE(oneByte);
                    random.nextBytes(oneByte);
                    byte[] addrBytes = new byte[random.nextInt(8)];
                    random.nextBytes(addrBytes);
                    nrb.set("uuid", uuid)
                            .set("baseObject", randomAbstractObject(uuid, datumSchema.getField("baseObject").schema()))
                            .set("proto", proto)
                            .set("ifIndex", random.nextInt())
                            .set("haType", haType)
                            .set("pktType", pktType)
                            .set("addr", ByteBuffer.wrap(addrBytes));
                } else if(datumSchema.getName().equals("NetFlowObject")){
                    nrb.set("uuid", uuid)
                            .set("baseObject", randomAbstractObject(uuid, datumSchema.getField("baseObject").schema()))
                            .set("localAddress", random.nextInt(255) + "." + random.nextInt(255) + "."
                                    + random.nextInt(255) + "." + random.nextInt(255))
                            .set("localPort", random.nextInt())
                            .set("remoteAddress", random.nextInt(255) + "." + random.nextInt(255) + "."
                                    + random.nextInt(255) + "." + random.nextInt(255))
                            .set("remotePort", random.nextInt())
                            .set("ipProtocol", 6);
                } else if(datumSchema.getName().equals("MemoryObject")){
                    nrb.set("uuid", uuid)
                            .set("baseObject", randomAbstractObject(uuid, datumSchema.getField("baseObject").schema()))
                            .set("memoryAddress", random.nextLong())
                            .set("pageOffset", random.nextLong())
                            .set("pageNumber", random.nextLong());
                } else if(datumSchema.getName().equals("SrcSinkObject")){
                    nrb.set("uuid", uuid)
                            .set("baseObject", randomAbstractObject(uuid, datumSchema.getField("baseObject").schema()))
                            .set("type", asEnum(datumSchema, sensorTypes[random.nextInt(sensorTypes.length)].toString()));
                } else if(datumSchema.getName().equals("Event")){
                    nrb.set("uuid", uuid)
                            .set("sequence", random.nextLong())
                            .set("type", asEnum(datumSchema, eventTypes[random.nextInt(eventTypes.length)].toString()))
                            .set("threadId", random.nextInt())
                            .set("subject", uuid)
                            .set("predicateObject", uuid)
                            .set("timestampNanos", random.nextLong())
                            .set("properties", props);

                    if(random.nextBoolean() == true) { //complex event with tagged params
                        Schema valueSchema = SchemaUtils.getTypeSchemaByName(datumSchema,
                                "com.bbn.tc.schema.avro.cdm20.Value", true);
                        Schema valueTypeSchema = SchemaUtils.getTypeSchemaByName(datumSchema,
                                "com.bbn.tc.schema.avro.cdm20.ValueType", true);
                        Schema valueDataTypeSchema = SchemaUtils.getTypeSchemaByName(datumSchema, "com.bbn.tc.schema.avro.cdm20.ValueDataType", true);

                        // first value is a string encoded as a char[] in UTF_32BE format
                        byte[] val1Bytes = "abcdefghiß".getBytes("UTF_32BE");
                        List<TagRunLengthTuple> val1Tag = new ArrayList<>();
                        val1Tag.add(new TagRunLengthTuple(5, SchemaUtils.toUUID(random.nextLong())));
                        val1Tag.add(new TagRunLengthTuple(5, SchemaUtils.toUUID(random.nextLong())));

                        // second value is a single long
                        byte[] val2Bytes = ByteBuffer.allocate(8).putLong(random.nextLong()).array();
                        List<TagRunLengthTuple> val2Tag = new ArrayList<>();
                        val2Tag.add(new TagRunLengthTuple(1, SchemaUtils.toUUID(random.nextLong())));

                        List<GenericRecord> vals = new ArrayList<>();
                        vals.add(new GenericRecordBuilder(valueSchema)
                                 .set("size", 10)
                                 .set("type",
                                      asEnum(valueTypeSchema, ValueType.VALUE_TYPE_SRC.toString()))
                                 .set("name", "valueName")
                                 .set("valueDataType",
                                      asEnum(valueDataTypeSchema, ValueDataType.VALUE_DATA_TYPE_CHAR.toString()))
                                 .set("valueBytes", ByteBuffer.wrap(val1Bytes))
                                 .set("tag", val1Tag).build());
                        vals.add(new GenericRecordBuilder(valueSchema)
                                 .set("size", 0) // primitives have size 0
                                 .set("type",
                                      asEnum(valueTypeSchema, ValueType.VALUE_TYPE_SINK.toString()))
                                 .set("valueDataType",
                                      asEnum(valueDataTypeSchema, ValueDataType.VALUE_DATA_TYPE_LONG.toString()))
                                 .set("valueBytes", ByteBuffer.wrap(val2Bytes))
                                 .set("tag", val2Tag).build());
                        nrb.set("parameters", vals);
                    }
                } else if(datumSchema.getName().equals("UnitDependency")) {
                    nrb.set("unit", uuid)
                            .set("dependentUnit", uuid);
                } else if(datumSchema.getName().equals("TimeMarker")){
                    nrb.set("tsNanos", random.nextLong());
                } else if(datumSchema.getName().equals("EndMarker")){
                    Map recordCounts = getKVPairs(numKVPairs+1);
                    nrb.set("sessionNumber", random.nextInt()).set("recordCounts", recordCounts);
                }

                record = cdmrb.set("datum", nrb.build())
                        .set("type", asEnum(datumSchema, recordTypes[random.nextInt(recordTypes.length)].toString()))
                        .set("hostId", uuid)
                        .set("sessionNumber", random.nextInt())
                        .set("source", asEnum(recordSchema,
                                instrumentationSources[random.nextInt(instrumentationSources.length)].toString()))
                        .build();
                return record;

            }else
                throw new RuntimeException("Unknown schema "+recordSchema.getFullName());

        }

        private static GenericData.EnumSymbol asEnum(Schema schema, String s){
            return new GenericData.EnumSymbol(schema, s);
        }

        private static GenericData.Array<Integer> asIntegerArray(Integer[] integers) {
            Schema schema = Schema.createArray(Schema.create(Schema.Type.INT));
            GenericData.Array<Integer> integerArray = new GenericData.Array(integers.length, schema);
            for (Integer i : integers) {
                integerArray.add(i);
            }
            return integerArray;
        }

        private static AbstractObject randomAbstractObject() {
            UUID hostId = SchemaUtils.toUUID(random.nextLong());
            AbstractObject abstractObject = AbstractObject.newBuilder().build();
            return abstractObject;
        }

        private static GenericContainer randomAbstractObject(GenericData.Fixed uuid, Schema schema) {
            GenericRecordBuilder recordBuilder = new GenericRecordBuilder(schema);
            GenericContainer abstractObject = recordBuilder.build();
            return abstractObject;
        }

        private static HostIdentifier randomHostIdentifier() {
            HostIdentifier hostIdentifier = HostIdentifier.newBuilder()
                    .setIdType(new String("some ID type " + random.nextInt()))
                    .setIdValue(new String("some ID value " + random.nextInt()))
                    .build();
            return hostIdentifier;
        }

        private static Interface randomInterface() {
            List<CharSequence> ipAddresses = new ArrayList<>();
            for (int i = 0; i < random.nextInt(5); i++) {
                ipAddresses.add("" + random.nextInt(256) + "." + random.nextInt(256) + "."
                        + random.nextInt(256) + "." + random.nextInt(256));
            }
            Interface iface = Interface.newBuilder()
                    .setName(new String("some name " + random.nextInt()))
                    .setMacAddress(new String("some mac address " + random.nextInt()))
                    .setIpAddresses(ipAddresses)
                    .build();
            return iface;
        }

        private static Map getKVPairs(int n) {
            if (n == 0) return null;
            Map<CharSequence, CharSequence> properties = new HashMap<>();
            for (int i = 0; i < n; i++)
                properties.put("k" + i, ""+random.nextInt());
            return properties;
        }

        public static void setRandomSeed(long seed) {
            random = new Random(seed);
        }

    }
