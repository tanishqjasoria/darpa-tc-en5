package com.bbn.tc.services.kafka;

public class Distribution {

    float min;
    float max;
    float mean;
    long count;
    float sum;
    String label;
    
    public Distribution(String label) {
        count = 0;
        this.label = label;
        min = Float.MAX_VALUE;
        max = Float.MIN_VALUE;
        mean = Float.NaN;
    }
    
    public void sample(float val) {
        if (val < min) {
            min = val;
        }
        if (val > max) {
            max = val;
        }
        sum += val;
        count++;
    }
    
    public void computeMean() {
        if (count > 0) {
            mean = sum / count;
        }
    }
    
    public void merge(Distribution d2) {
        if (d2.min < min) {
            min = d2.min;
        }
        if (d2.max > max) {
            max = d2.max;
        }
        sum += d2.sum;
        count += d2.count;
        computeMean();
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(label+" Min: "+min+" ");
        sb.append(" Max: "+max+" ");
        sb.append(" Mean: "+mean+" ");
        sb.append(" Count: "+count);
        return sb.toString();
    }
}
