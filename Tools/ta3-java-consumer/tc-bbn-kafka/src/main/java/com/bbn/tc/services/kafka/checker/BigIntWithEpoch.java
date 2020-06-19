package com.bbn.tc.services.kafka.checker;

import java.math.BigInteger;

public class BigIntWithEpoch {

    public BigInteger uuid;
    public int epoch;
    
    public BigIntWithEpoch(BigInteger uuid) {
        this.uuid = uuid;
        epoch = 0;
    }

    public BigInteger getUuid() {
        return uuid;
    }

    public void setUuid(BigInteger uuid) {
        this.uuid = uuid;
    }

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + epoch;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BigIntWithEpoch other = (BigIntWithEpoch) obj;
        if (epoch != other.epoch)
            return false;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }
    
    public String toString() {
        if (uuid == null) {
            return "NULL";
        }
        if (epoch == 0) {
            return String.valueOf(uuid);
        } else {
            return String.valueOf(uuid)+"_e"+epoch;
        }        
    }
}
