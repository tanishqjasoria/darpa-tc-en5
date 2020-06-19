package com.bbn.tc.services.kafka;

import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public class InstrumentedKafkaProducer<K, V> extends KafkaProducer<K, V> {

    static final Counter publishCounter;    
    static final Gauge lastPublish;    
    static final Gauge statsGauge;
    static final Counter cdmTypes;
    
    public boolean instrumented = true;
    static public boolean extraInstrumentation = true;
    private String hostName;
    
    static {
        publishCounter = Counter.build().name("ta1_send_total").help("Total send calls")
                .labelNames("topic","host").register();
        lastPublish = Gauge.build().name("ta1_last_send_time").help("Latest timestamp of a send")
                .labelNames("topic","host").register();
        statsGauge = Gauge.build().name("ta1_status").help("Status: 0: not started, 1: producing, 2: paused, 3: done")
                .labelNames("host").register();
        
        // ExtraInstrumentation only
        cdmTypes = Counter.build().name("ta1_cdm_send_types").help("Types of CDM records we sent")
                .labelNames("topic","type","host").register();
    }
    
    public InstrumentedKafkaProducer(Properties properties) {
        super(properties);
        hostName = InstrumentationManager.getHostName();
        statsGauge.labels(hostName).set(0);
    }

    public boolean isInstrumented() {
        return instrumented;
    }

    public void setInstrumented(boolean instrumented) {
        this.instrumented = instrumented;
    }
    
    @Override
    public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback) {
        if (instrumented) {
            handleMetrics(record);
        }
        Future<RecordMetadata> md = super.send(record, callback);
        return md;
    }
    
    private void handleMetrics(ProducerRecord<K, V> record) {
        publishCounter.labels(record.topic(), hostName).inc();
        lastPublish.labels(record.topic(), hostName).setToCurrentTime();
        if (statsGauge.labels(hostName).get() == 0) {
            statsGauge.labels(hostName).set(1);
        }
        if (extraInstrumentation) {
            Object rVal = record.value();
            if (rVal instanceof TCCDMDatum) {
                TCCDMDatum cdm = (TCCDMDatum)record.value();
                Object dObj = cdm.getDatum();
                cdmTypes.labels(record.topic(), dObj.getClass().getSimpleName(), hostName).inc();
            } else if (rVal instanceof GenericData.Record) {
                GenericData.Record r1 = (GenericData.Record)rVal;     
                String label = GenericData.Record.class.getSimpleName();
                Object datum = r1.get("datum");
                if (datum instanceof GenericData.Record) {
                    GenericData.Record dr = (GenericData.Record)datum;
                    Schema schema = dr.getSchema();
                    if (schema != null) {
                        label = schema.getName();
                    }
                } 
                cdmTypes.labels(record.topic(), label, hostName).inc();
            }
        }
    }
    
    public void pause() {
        statsGauge.labels(hostName).set(2);
    }
    
    @Override
    public void close() {
        statsGauge.labels(hostName).set(3);
        InstrumentationManager.turnOffInstrumentationServer();
        super.close();    
    }
}
