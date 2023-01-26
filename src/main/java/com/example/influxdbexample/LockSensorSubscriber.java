package com.example.influxdbexample;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;

public class LockSensorSubscriber implements MqttCallback {
    MqttClient client;
    InfluxDBClient influxDBClient_lock;
    String token = "QcgvGtcPqEV-c2kX8kKCc4MCFyLttK3jywLzIkvpFx6WlVcoRO1JfG2wa-kpRvVozuBgozDWItb23vwEPlHMrw==";
    String bucket = "rosh_sht_bucket";
    String org = "rosh_org";

    @Override
    public void connectionLost(Throwable arg0) {
        System.out.println("The connection with the server is lost. !!!!");

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken arg0) {
        System.out.println("The delivery has been complete. The delivery token is " + arg0.toString());

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        String messageText = new String(message.getPayload());

        if (topic.equals("LockSensor")) {

            System.out.println(String.format(" received lock data: %s", messageText));
            int lockStatus = Integer.parseInt(messageText.split(",")[0].split("=")[1]);
            String date = messageText.split(",")[1].split("=")[1].split("}")[0];
            System.out.println("lock status is:" + lockStatus);
            System.out.println("Date is:" + date);

            Point point_lock = Point
            .measurement("home_consumption")
            .addTag("lock_consumption", "lock").addField("lock", lockStatus)
            .time(Instant.now(), WritePrecision.NS);


            WriteApiBlocking writeApi_lock = influxDBClient_lock.getWriteApiBlocking();
            writeApi_lock.writePoint(bucket, org, point_lock);
        }

        // System.out.println("A new message arrived from the topic: \"" + arg0 + "\".
        // The payload of the message is " + message.toString());

    }

    public static void main(String[] args) {

        // new PahoDemoImplCallBack().publish1();
        LockSensorSubscriber LockDetector = new LockSensorSubscriber();
        LockDetector.setupDB();
        LockDetector.subscribe();
    }

    
    public void setupDB() {

        try {
            influxDBClient_lock = InfluxDBClientFactory.create("http://javainfluxdbexample-influxdb-1:8086", token.toCharArray());
            System.out.println("setup db!");
    
        } catch (InfluxException e) {
    
          System.out.print("we are in exception");
          System.out.print(e);
        }
    
        // influxDBClient = InfluxDBClientFactory.create(url, token, org, bucket);
        System.out.println(influxDBClient_lock.getDashboardsApi());
        System.out.println("in influxDB");
    
      }
    

    public void subscribe() {
        try {
            client = new MqttClient("tcp://javainfluxdbexample-mosquitto-container-1:1883", "LockSensor");

            client.setCallback(this);
            client.connect();
            try {

                client.subscribe("LockSensor", 2);

            } catch (MqttException e) {
                e.printStackTrace();
            }

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
