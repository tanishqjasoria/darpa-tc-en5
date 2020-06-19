package com.bbn.tc.services.kafka.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.TreeSet;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JMXGet {

    static public void main(String[] args) {
        String host = "localhost";
        int port = 9999;
        String key = "kafka.server:type=KafkaServer,name=BrokerState";
        boolean list = false;
        String retval = null;
        
        for (int i=0; i<args.length; ++i) {
            String an = null;
            if (i+1 < args.length) {
                an = args[i+1];
            }
            String arg = args[i];
            if (arg.equalsIgnoreCase("-host") || arg.equalsIgnoreCase("-h")) {
                host = an;
                i++;
            } else if (arg.equalsIgnoreCase("-port") || arg.equalsIgnoreCase("-p")) {
                port = Integer.parseInt(an);
                i++;
            } else if (arg.equalsIgnoreCase("-key") || arg.equalsIgnoreCase("-k")) {
                key = an;
                i++;
            } else if (arg.equalsIgnoreCase("-l") || arg.equalsIgnoreCase("-list")) {
                list = true;
            }
        }
        
        try {
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+host+":"+port+"/jmxrmi");
            System.out.println("Connecting to JMX at "+url);
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
            if (list) {
                Set<ObjectName> names = new TreeSet<ObjectName>(mbsc.queryNames(null, null));
                for (ObjectName name : names) {
                    System.out.println("\tObjectName = " + name);
                }
            }
            if (key != null) {
                System.out.println("Getting object: "+key);
                ObjectName kName = new ObjectName(key);
                String[] attribs = new String[] {};
                AttributeList aList = mbsc.getAttributes(kName, attribs);
                for (Attribute att : aList.asList()) {
                    System.out.println(att.getName()+": "+att.getValue());
                }            
                
                try {
                    MBeanInfo minfo = mbsc.getMBeanInfo(kName);
                    System.out.println("Class Name: "+minfo.getClassName());
                    System.out.println("Description: "+minfo.getDescription());
                    MBeanOperationInfo[] opsArray = minfo.getOperations();
                    for (MBeanOperationInfo ops : opsArray) {
                        System.out.println("MBean Operation: "+ops.getName()+": "+ops.getDescription()+" ret: "+ops.getReturnType());
                    }
                    MBeanAttributeInfo[] attArray = minfo.getAttributes();
                    for (MBeanAttributeInfo att : attArray) {
                        System.out.println("MBean Attribute: "+att.getName()+": "+att.getType()+" "+att.getDescription());
                        if (att.getName().equalsIgnoreCase("Value")) {
                            Object val = mbsc.getAttribute(kName, "Value");
                            System.out.println("\tValue: "+val.toString());
                            retval = val.toString();
                        }
                    }
                    
                } catch (IntrospectionException e) {
                    e.printStackTrace();
                } catch (AttributeNotFoundException e) {
                    e.printStackTrace();
                } catch (MBeanException e) {
                    e.printStackTrace();
                }
                
            }

            jmxc.close();
            System.out.println("Return Value: "+retval);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        }
        
        
    }
}
