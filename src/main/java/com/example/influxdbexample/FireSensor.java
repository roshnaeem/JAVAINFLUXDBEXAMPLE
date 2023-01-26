package com.example.influxdbexample;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class FireSensor {
    
        MqttClient client;
    
        public FireSensor() {
            try {
                client = new MqttClient("tcp://javainfluxdbexample-mosquitto-container-1:1883", "fire_sensor");
                client.connect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
    
        }
    
        public static void main(String[] args) {
    
            FireSensor sensor = new FireSensor();
            sensor.firePublisher();
        }
    
        public Map<String, String> firePublisher() {
            Map<String, String> consumptionMessage = new HashMap<String, String>();
            String delimiter = ",";
            String csvFile = "smoke_detection.csv";
            try {
                File file = new File(csvFile);
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String line = "";
                String[] tempArr;
                String fireAlarm;
                String timestamp;
                String temperatue;
                String humidity;
                String pressure;
                String co2;
                boolean firstLine = true;
                while ((line = br.readLine()) != null) {
                    if (!firstLine) {
    
                        tempArr = line.split(delimiter);
                        timestamp =convertDate(Long.valueOf(tempArr[1]));
                        // timestamp = tempArr[1];
                        fireAlarm = tempArr[tempArr.length - 1];
                        temperatue = tempArr[2];
                        humidity = tempArr[3];
                        pressure = tempArr[8];
                        co2 = tempArr[5];
                        // consumption = Float.valueOf(tempArr[tempArr.length-1]);
                        System.out.print("time stamp:" + timestamp + "- FireAlarm:" + fireAlarm);
                        consumptionMessage.put("Date", timestamp);
                        consumptionMessage.put("FireAlarm", fireAlarm);
                        consumptionMessage.put("Temperatue", temperatue);
                        consumptionMessage.put("Humidity", humidity);
                        consumptionMessage.put("Pressure", pressure);
                        consumptionMessage.put("co2", co2);
                        String consumptionMessageString = consumptionMessage.toString();
                        try {
    
                            MqttMessage message = new MqttMessage();
                            message.setPayload(consumptionMessageString.getBytes());
                            client.publish("FireSensor", message);
                           // System.out.println("publisheeddd");
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            // client.disconnect();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }else{
                        firstLine = false;
                    }
    
                }
                br.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return consumptionMessage;
        }
        public String convertDate(Long timeStamp){
            java.util.Date time=new java.util.Date((Long)timeStamp*1000);
            return time.toString();
        }
    
}
