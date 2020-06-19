package com.bbn.tc.services.kafka.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.ConfigResource;

public class StarcAdminClient {

    static public String KAFKA_STRING = "localhost:9092";
    
    public static void main(String args[]) {
        String connString = KAFKA_STRING;
                
        for (int i=0; i<args.length; ++i) {
            String arg = args[i];
            if (arg.equals("-ks")) {
                if (i+1 < args.length) {
                    connString = args[i+1];
                    i++;
                }                
            } 
        }
      
        Properties properties = new Properties();
        properties.put("bootstrap.servers", connString);
        properties.put("connections.max.idle.ms", 10000);
        properties.put("request.timeout.ms", 5000);

        System.out.println("Opening AdminClient ("+connString+")");

        AdminClient client = KafkaAdminClient.create(properties);
        DescribeClusterResult dcr = client.describeCluster();
        Collection<Node> nodes = null;
        try {
            KafkaFuture<String> cid = dcr.clusterId();
            System.out.println("Cluster ID: "+cid.get());
            nodes = dcr.nodes().get();
            for (Node n : nodes) {
                System.out.println("\tNode: "+n.id());
                System.out.println("\t  Host: "+n.host());
                System.out.println("\t  Port: "+n.port());
                System.out.println("\t  Rack: "+n.rack());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        
        if (nodes != null) {
            System.out.println("\n Getting broker configs");
            ArrayList<ConfigResource> nodeConfigs = new ArrayList<ConfigResource>();
            for (Node n : nodes) {
                ConfigResource cr = new ConfigResource(ConfigResource.Type.BROKER, n.idString()); 
                nodeConfigs.add(cr);
            }
            DescribeConfigsResult dcr2 = client.describeConfigs(nodeConfigs);
            for (ConfigResource cr : nodeConfigs) {
                KafkaFuture<Config> nfConfig = dcr2.values().get(cr);
                try {
                    Config config = nfConfig.get();
                    Collection<ConfigEntry> entries = config.entries();
                    System.out.println("Configs for "+cr.name());
                    for (ConfigEntry entry : entries) {
                        StringBuffer eb = new StringBuffer();
                        eb.append("\t"+entry.name());
                        if (entry.isDefault()) {
                            eb.append(" (default) ");
                        } 
                        if (entry.isSensitive()) {
                            eb.append(" SENSITIVE");
                        } else {
                            eb.append(": "+entry.value());
                        }
                        System.out.println(eb.toString());
                    }                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            
        }
        System.out.println("Closing AdminClient");
        client.close();        
    }
}
