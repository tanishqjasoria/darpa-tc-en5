package com.bbn.tc.services.kafka.checker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm20.Event;
import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;

public class TimestampCheck extends CDMChecker {

    private static final Logger logger = Logger.getLogger(TimestampCheck.class);
    
    protected long lastTimestamp = -1;
    protected long firstTimestamp = -1;
    protected long latestTimestamp = -1;
    
    long gapSizeMs = 60000l; // gap of > 1 minute, we record
    int tsDecreases = 0;
    int tsGaps = 0;
    
    int tsReportingPeriod = 100000;
    int curCount = 0;
    int reportingWindowCount = 0;
    
    long tsConversionFactor = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
    
    static public int MAX_CAPACITY = 1000;
    
    HashQueue<Date> periodicTimestamps = new HashQueue<Date>(MAX_CAPACITY);
    
    public TimestampCheck() {
    }
    
    @Override
    public void initialize(Properties properties) {
        periodicTimestamps.clear();
        logger.info("Initializing TimestampCheck from properties");
        String incMax = properties.getProperty("TimestampCheck.gapSizeMs");
        if (incMax != null) {
            try {
                gapSizeMs = Long.parseLong(incMax);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                logger.error(ex.getMessage(), ex);
            }
        }  
        logger.info("TimestampCheck gapSizeMs = "+gapSizeMs);
        
        String tsPeriod = properties.getProperty("TimestampCheck.reporting.period");
        if (tsPeriod != null) {
            try {
                tsReportingPeriod = Integer.parseInt(tsPeriod);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                logger.error(ex.getMessage(), ex);
            }
        }
        logger.info("TimestampCheck reporting period = "+tsPeriod);
        
        tsDecreases = 0;
        tsGaps = 0;       
        curCount = 0;
    }

    @Override
    public String description() {
        StringBuffer sb = new StringBuffer("Check that timestamps increment with no gap greater than ");
        sb.append(gapSizeMs);
        sb.append(System.lineSeparator());
        sb.append("\tProperty: TimestampCheck.gapSizeMs: maximum allowed gap between timestamps in milliseconds");
        return sb.toString();
    }

    @Override
    public void processRecord(String key, TCCDMDatum record) throws Exception {
        curCount++;
        Object datum = record.getDatum();
        if (datum instanceof Event) {
            Event event = (Event)datum;
            long timeStampNanos = event.getTimestampNanos() != null ? event.getTimestampNanos() : -1;
            Long sequence = event.getSequence();
            if (firstTimestamp == -1) {
                logger.info("First Timestamp: "+timeStampNanos);
                firstTimestamp = timeStampNanos;
            }
            
            if (curCount >= tsReportingPeriod) {
                curCount = 0;
                Date d = convertToNanos(timeStampNanos);
                reportingWindowCount++;
                long rcount = tsReportingPeriod * reportingWindowCount;
                logger.info("Timestamp after "+rcount+" records: "+sdf.format(d));
                periodicTimestamps.add(d);
            }
            
            if (lastTimestamp > -1 && timeStampNanos > -1) {
                if (latestTimestamp > -1) {
                    // Have we caught back up yet?
                    if (latestTimestamp <= timeStampNanos) {
                        logger.info("Timestamp caught back up: "+timeStampNanos+" > "+latestTimestamp);
                        //logger.debug("\tCaught up Record: "+jsonSerializer.serializeToJson(record, true));
                        latestTimestamp = -1;
                    }
                }
                if (lastTimestamp > timeStampNanos) {
                    long diff = lastTimestamp - timeStampNanos;
                    if (diff > (gapSizeMs * 1000000l)) {
                        logger.warn("Timestamp decreased: "+lastTimestamp+" -> "+timeStampNanos);
                        logger.warn("Seq number: "+sequence);
                        logger.debug("\tDecreased at Record: "+jsonSerializer.serializeToJson(record, true));
                        tsDecreases++;
                    }
                    latestTimestamp = lastTimestamp;
                } else if (lastTimestamp < timeStampNanos) {
                    long diff = timeStampNanos - lastTimestamp;
                    if (diff > (gapSizeMs * 1000000l)) {
                        logger.info("Timestamp increase of: "+diff);
                        tsGaps++;
                    } 
                }
                lastTimestamp = timeStampNanos;
            } else if (timeStampNanos > -1) {
                lastTimestamp = timeStampNanos;
            }
        }            
    }

    @Override
    public boolean issuesFound() {
       if (tsDecreases == 0 && tsGaps == 0) {
           return false;
       }
       return true;
    }
    
    static public long computeTimestampConversionFactor(long timestamp) {
        long tsConversionFactor = 1;
        // What units are the timestamps in?
        Date assumeNanos = new Date(timestamp / 1000000);
        Calendar c1 = new GregorianCalendar();
        c1.setTime(assumeNanos);
        int year = c1.get(Calendar.YEAR);
        long timestampNanos = timestamp;
        
        if (year <= 1970) {
            // Try microseconds, milliseconds, seconds
            boolean found = false;
            int mIndex = 0;
            for (mIndex=0; mIndex<3 && !found; ++mIndex) {
                timestampNanos = timestampNanos * 1000;
                tsConversionFactor = tsConversionFactor * 1000;
                Date assumeNext = new Date(timestampNanos / 1000000);
                c1.setTime(assumeNext);
                year = c1.get(Calendar.YEAR);
                if (year > 1970) { // assume we're not running this code in the 70s
                    found = true;
                }
            }
            switch (mIndex) {
            case 0 : logger.warn("Checker logic error!"); break;
            case 1 : logger.warn("Timestamp is in microseconds, should be nanoseconds!"); break;
            case 2 : logger.warn("Timestamp is in milliseconds, should be nanoseconds!"); break;
            default : logger.warn("Timestamp is in seconds, should be nanoseconds!"); break;
            }
        } else {        
            logger.info("Timestamp is in nanoseconds");
        }  
        return tsConversionFactor;
    }
    
    public Date convertToNanos(long timestamp) {
        if (tsConversionFactor == 0) {
            tsConversionFactor = TimestampCheck.computeTimestampConversionFactor(timestamp);
        }
        
        Date d = new Date(timestamp * tsConversionFactor / 1000000);
        return d;        
    }        

    @Override
    public void close() {
        logger.info("Close: First Timestamp: "+firstTimestamp);
        logger.info("Close: Last Timestamp: "+lastTimestamp);
        
        Date firstTimestampDate = convertToNanos(firstTimestamp);
        Date lastTimestampDate = convertToNanos(lastTimestamp);
        
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMMM yyyy HH:mm:ss");
        
        logger.info("First timestamp: "+firstTimestamp+": "+sdf.format(firstTimestampDate));
        logger.info("Last Timestamp: "+lastTimestamp+": "+sdf.format(lastTimestampDate));
        long duration = lastTimestampDate.getTime() - firstTimestampDate.getTime();
        logger.info("Time duration (ms): "+duration);
        SimpleDateFormat durFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
        logger.info("Duration: " + durFormat.format(new Date(duration - TimeZone.getDefault().getRawOffset())));
  
        // Periodic windows
        long startCount = 0;
        if (reportingWindowCount > MAX_CAPACITY) {
            // We dropped some of the earlier times
            startCount = reportingWindowCount - MAX_CAPACITY;
        }
        
        for (Date wDate : periodicTimestamps) {
            startCount++;
            logger.info("Record: "+(startCount * this.tsReportingPeriod)+"  Time: "+sdf.format(wDate));
        }        
    }

    public static void main(String[] args) {
        TimestampCheck tCheck = new TimestampCheck();
        tCheck.firstTimestamp = 1494261812378458000L;
        tCheck.lastTimestamp = 1494261978439801000L;
        tCheck.close();
        
        TimestampCheck tCheck2 = new TimestampCheck();
        tCheck2.firstTimestamp = 1494261812378458L;
        tCheck2.lastTimestamp = 1494261978439801L;
        tCheck2.close();
        
        TimestampCheck tCheck3 = new TimestampCheck();
        tCheck3.firstTimestamp = 1494261812378L;
        tCheck3.lastTimestamp = 1494261978439L;
        tCheck3.close();
        
        TimestampCheck tCheck4 = new TimestampCheck();
        tCheck4.firstTimestamp = 1494261812L;
        tCheck4.lastTimestamp = 1494261978L;
        tCheck4.close();
    }
}
