package com.bbn.tc.services.kafka;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public class InstrumentedKafkaConsumer<K, V> extends KafkaConsumer<K, V> {

    static final Counter consumedCounter;
    static final Counter pollCalls;
    static final Gauge lastConsume;
    static final Gauge lastPoll;
    static final Gauge statsGauge;
    static final Counter cdmTypes;
    
    public boolean instrumented = true;
    static public boolean extraInstrumentation = true;
    private String groupId; 
    private String hostName;
    
    private Counter.Child lConsumedCounter = null;
    private Counter.Child lPollCalls = null;
    private Gauge.Child lLastConsume = null;
    private Gauge.Child lLastPoll = null;
    private Gauge.Child lStatsGauge = null;
    
    static {
        consumedCounter = Counter.build().name("ta2_recv_total").help("Total consumed records")
                .labelNames("group","host").register();
        pollCalls = Counter.build().name("ta2_poll_total").help("Total poll calls")
                .labelNames("group","host").register();
        lastConsume = Gauge.build().name("ta2_last_recv_time").help("Latest timestamp of consumed record")
                .labelNames("group","host").register(); 
        lastPoll = Gauge.build().name("ta2_last_poll_time").help("Latest timestamp of a call to poll")
                .labelNames("group","host").register();
        statsGauge = Gauge.build().name("ta2_status").help("Status: 0: not started, 1: consuming, 2: paused, 3: done")
                .labelNames("host").register();
        
        // ExtraInstrumentation only
        cdmTypes = Counter.build().name("ta2_cdm_recv_types").help("Types of CDM records we received")
                .labelNames("topic","type","host").register();
    }
    
    public InstrumentedKafkaConsumer(Properties properties) {
        super(properties);
        this.groupId = properties.getProperty(ConsumerConfig.GROUP_ID_CONFIG);
        this.hostName = InstrumentationManager.getHostName();
        
        lConsumedCounter = consumedCounter.labels(groupId, hostName);
        lPollCalls = pollCalls.labels(groupId, hostName);
        lLastConsume = lastConsume.labels(groupId, hostName);
        lLastPoll = lastPoll.labels(groupId, hostName);
        lStatsGauge = statsGauge.labels(hostName);
        
        statsGauge.labels(hostName).set(0);
    }

    public boolean isInstrumented() {
        return instrumented;
    }

    public void setInstrumented(boolean instrumented) {
        this.instrumented = instrumented;
    }
    
    @Override
    public ConsumerRecords<K, V> poll(long timeout) {
        ConsumerRecords<K,V> records = super.poll(timeout);
        if (instrumented) {
            lPollCalls.inc();
            lLastPoll.setToCurrentTime();
            if (lStatsGauge.get() == 0) {
                lStatsGauge.set(1);
            }
            if (!records.isEmpty()) { 
                lConsumedCounter.inc(records.count());
                lLastConsume.setToCurrentTime();
            }            
        }
        return records;
    }
    
    public void pause() {
        lStatsGauge.set(2);
    }
    
    @Override
    public void close() {
        lStatsGauge.set(3);
        super.close();    
    }
    
    public void recordCDMMetrics(ConsumerRecord<K,V> cdm) {
        V cdmVal = cdm.value();
        if (cdmVal instanceof TCCDMDatum) {
            Object dObj = ((TCCDMDatum)cdmVal).getDatum();
            cdmTypes.labels(cdm.topic(), dObj.getClass().getSimpleName(), hostName).inc();
        } else {
            cdmTypes.labels(cdm.topic(), cdmVal.getClass().getName(), hostName).inc();
        }
    }
}
