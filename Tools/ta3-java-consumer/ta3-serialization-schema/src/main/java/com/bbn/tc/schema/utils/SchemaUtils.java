/*
 * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
 */

package com.bbn.tc.schema.utils;

import com.bbn.tc.schema.avro.cdm20.UUID;
import org.apache.avro.Schema;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Some helper utilities for Avro schema manipulation
 * @author jkhoury
 */
public class SchemaUtils {
    private static final Logger logger = Logger.getLogger(SchemaUtils.class);

    private static final int LONG_SIZE = 8;

    private static HashMap<String, Schema> schemaCache;

    static {
        schemaCache = new HashMap<>();
    }

    /**
     * Given a top level schema that contains several types within it (records, enums, etc)
     * in some arbitrary nested order, traverse it and return the schema for the specific
     * type, identified by name
     *
     * Given a full schema definition, we extract a sub schema corresponding to
     * a record with the specified name. We currently handle two structures
     * of schemas: a union type with individual records or a single nested record with sub
     * records. This function recurses through the full schema until it finds
     * the record or it returns null if it doesnt
     *
     * Caching will cache all instances of enums, or records during traversal that have
     * a namespace matching {@link Constants#SCHEMA_NAMESPACE}. This filters out all the
     * primitive types and unions and nulls etc from the cache. The cache is also necessary
     * to break recursion cycles during traversal (e.g., node.child = node )
     *
     * @param topLevelSchema the top level schema containing several types potentially
     * @param fullTypeName the full name of the internal type we want
     * @param cache if cache is true, then all traversed schemas will be cached during lookup
     *              and we always lookup in the cache before traversal
     * @return the schema for the internal type
     */

    public static Schema getTypeSchemaByName(Schema topLevelSchema,
                                             String fullTypeName,
                                             boolean cache){
        HashMap<String, String> seenAlready = new HashMap();
        return getTypeSchemaByName(topLevelSchema, fullTypeName, cache, seenAlready);
    }

    private static Schema getTypeSchemaByName(Schema topLevelSchema,
                                             String fullTypeName,
                                             boolean cache,
                                             HashMap<String, String> seenAlready)
            throws IllegalArgumentException{

        if(topLevelSchema == null)
            throw new IllegalArgumentException("topLevelschema can not be null");
        if(fullTypeName == null || fullTypeName.isEmpty())
            throw new IllegalArgumentException("typeName can not be null or empty");


        // anything that is not a union, record, array, fixed, or enum is not interesting to us at this point
        if(topLevelSchema.getType() != Schema.Type.UNION
            && topLevelSchema.getType() != Schema.Type.ARRAY
                && topLevelSchema.getType() != Schema.Type.RECORD
                    && topLevelSchema.getType() != Schema.Type.ENUM
                        && topLevelSchema.getType() != Schema.Type.FIXED)
            return null;

        // this is to break infinite loops for example when say record node { node child;}
        // this is to break infinite loops for example when say record node { child: node}
        if(seenAlready.containsKey(topLevelSchema.getFullName())) {
            //logger.info("seen already " + topLevelSchema.getFullName());
            return null;
        }
        if(topLevelSchema.getType() == Schema.Type.RECORD
                || topLevelSchema.getType() == Schema.Type.ENUM)
            seenAlready.put(topLevelSchema.getFullName(), null);

        //logger.info("s:"+topLevelSchema.getFullName() + ", type: " + topLevelSchema.getType());

        Schema _schema;
        // caching mechanics: if in the cache return it, if not in cache, add it only if its a record or enum
        if(cache){
            if((_schema = schemaCache.get(fullTypeName)) != null) // return from cache
                return _schema;
            if((topLevelSchema.getType() == Schema.Type.RECORD // add to cache when newly discovered
                || topLevelSchema.getType() != Schema.Type.ENUM
                    || topLevelSchema.getType() != Schema.Type.FIXED)
                    && !schemaCache.containsKey(topLevelSchema.getFullName()))
                schemaCache.put(topLevelSchema.getFullName(), topLevelSchema);
        }

        if(topLevelSchema.getFullName() != null && topLevelSchema.getFullName().equals(fullTypeName)) {
            return topLevelSchema;
        }

        // drill down into unions and records only
        if(topLevelSchema.getType() == Schema.Type.UNION) {
            for (Schema s : topLevelSchema.getTypes()) {
                Schema _ss=null;
                if((_ss=getTypeSchemaByName(s, fullTypeName, cache, seenAlready)) != null)
                    return _ss;
            }
        }else if(topLevelSchema.getType() == Schema.Type.RECORD) {
            for(Schema.Field f:topLevelSchema.getFields()) {
                Schema _s=null;
                if((_s=getTypeSchemaByName(f.schema(), fullTypeName, cache, seenAlready)) != null)
                    return _s;
            }
        }else if(topLevelSchema.getType() == Schema.Type.ARRAY) {
            Schema _s=null;
            if((_s=getTypeSchemaByName(topLevelSchema.getElementType(), fullTypeName, cache, seenAlready)) != null)
                return _s;

        }
        return null;
    }

    /**
     * Clear the schema cache; if will get repopulated as needed
     */
    public static void clearCache(){
        if(schemaCache != null) schemaCache.clear();
    }

    /**
     * Convert a date string to a Date object. The latter could be easily converted to millis
     *   using the {@link Date#getTime()}
     * @param strDate the date string formatted according to format
     * @param format the format of the date string being parsed
     * @param timeZone the time zone of the date, null means assume UTC, used when TZ is embedded in the strDate
     * @return the Date object
     */
    public static Date stringToDate(String strDate, String format, TimeZone timeZone)
            throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        if(timeZone != null) dateFormat.setTimeZone(timeZone);
        return dateFormat.parse(strDate);
    }

    /**
     * Convert an integer to a 256 bit UUID
     * @param a the int to convert
     * @return the UUID object
     */
    public static UUID toUUID(int a){
        return toUUID((long)a);
    }

    /**
     * Convert a long to a 256 bit UUID
     * @param a the long
     * @return the UUID object
     */
    public static UUID toUUID(long a){
        byte [] result = new byte[UUID.getClassSchema().getFixedSize()];
        for(int i=0; i<result.length - LONG_SIZE; i++) result[i] = 0;
        for(int i=result.length-1, j=0; i>=result.length - LONG_SIZE; i--, j++)  result[i] = (byte) (a >> (j*8));
        return new UUID(result);
    }
}
