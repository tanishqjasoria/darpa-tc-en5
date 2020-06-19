/*
 * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
 */

package com.bbn.tc.schema.utils;

import com.bbn.tc.schema.avro.LabeledEdge;
import com.bbn.tc.schema.avro.LabeledNode;
import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;

/**
 * @author jkhoury
 */
public final class Constants {
    public static final String NODE_SCHEMA_FULLNAME = LabeledNode.getClassSchema().getFullName();
    public static final String EDGE_SCHEMA_FULLNAME = LabeledEdge.getClassSchema().getFullName();
    public static final String TCCDM_SCHEMA_FULLNAME = TCCDMDatum.getClassSchema().getFullName();
    public static final String SCHEMA_NAMESPACE = TCCDMDatum.getClassSchema().getNamespace();

    public static final String DATE_FORMAT_A = "yyyy-MM-dd'T'HH:mm:ssX";
    public static final String DATE_FORMAT_B = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

}
