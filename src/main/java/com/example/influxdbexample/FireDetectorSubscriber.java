package com.example.influxdbexample;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class FireDetectorSubscriber implements MqttCallback {
    MqttClient client;
    InfluxDBClient influxDBClient_fire;
    InfluxDBClient influxDBClient_humidity;
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
        if (topic.equals("FireSensor")) {

            System.out.println(String.format(" received Fire: %s", messageText));
            Properties props = new Properties();
            props.load(new StringReader(messageText.substring(1, messageText.length() - 1).replace(", ", "\n")));
            Map<String, String> map2 = new HashMap<String, String>();
            for (Map.Entry<Object, Object> e : props.entrySet()) {
            map2.put((String)e.getKey(), (String)e.getValue());
            }

            int FireAlarm = Math.round(Float.parseFloat(map2.get("FireAlarm")));

            System.out.println("fire alarm is:! " + FireAlarm);
            Point point_fire = Point
            .measurement("home_consumption")
            .addTag("fire_consumption", "fire").addField("fire", FireAlarm)
            .time(Instant.now(), WritePrecision.NS);

            WriteApiBlocking writeApi_fire = influxDBClient_fire.getWriteApiBlocking();
            writeApi_fire.writePoint(bucket, org, point_fire);
        }
        else if (topic.equals("humidity")) {
            System.out.println(String.format(" received humidity: %s", messageText));

            int humidity = Math.round(Float.parseFloat(messageText));

            System.out.println("fire alarm is:! " + humidity);
            Point point_humidity = Point
            .measurement("home_consumption")
            .addTag("humidity_value", "humidity").addField("humidity", humidity)
            .time(Instant.now(), WritePrecision.NS);

            WriteApiBlocking writeApi_humidity = influxDBClient_humidity.getWriteApiBlocking();
            writeApi_humidity.writePoint(bucket, org, point_humidity);

        }

        // System.out.println("A new message arrived from the topic: \"" + arg0 + "\".
        // The payload of the message is " + message.toString());

    }

    public static void main(String[] args) {

        // new PahoDemoImplCallBack().publish1();
        
        //new FireDetectorSubscriber().subscribe();

        FireDetectorSubscriber FireDetector = new FireDetectorSubscriber();
        FireDetector.setupDB();
        FireDetector.subscribe();
        
    }

    public void setupDB() {

        try {
            influxDBClient_fire = InfluxDBClientFactory.create("http://javainfluxdbexample-influxdb-1:8086", token.toCharArray());
            influxDBClient_humidity = InfluxDBClientFactory.create("http://javainfluxdbexample-influxdb-1:8086", token.toCharArray());
            System.out.println("setup db!");
    
        } catch (InfluxException e) {
    
          System.out.print("we are in exception");
          System.out.print(e);
        }
    
        // influxDBClient = InfluxDBClientFactory.create(url, token, org, bucket);
        System.out.println(influxDBClient_fire.getDashboardsApi());
        System.out.println("in influxDB");
    
      }
    

    public void subscribe() {
        try {
            client = new MqttClient("tcp://javainfluxdbexample-mosquitto-container-1:1883", "FireSensor");

            client.setCallback(this);
            client.connect();
            try {

                client.subscribe("FireSensor", 2);
                client.subscribe("humidity", 2);

            } catch (MqttException e) {
                e.printStackTrace();
            }

            // client.subscribe(new
            // String[]{"home/outsidetemperature","home/livingroomtemperature"});
            // client.subscribe("home/#");
            System.out.println("Hello!");

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    
}
