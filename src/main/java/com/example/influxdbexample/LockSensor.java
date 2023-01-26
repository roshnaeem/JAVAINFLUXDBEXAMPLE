package com.example.influxdbexample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.joda.time.DateTime;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

public class LockSensor{
    MqttClient client;

	public LockSensor() {
		try {
			client = new MqttClient("tcp://javainfluxdbexample-mosquitto-container-1:1883", "lock_sensor");
			client.connect();
		} catch (MqttException e) {
			e.printStackTrace();
		}

	}
    public static void main(String[] args) {

		LockSensor sensor = new LockSensor();
		sensor.lockPublisher();

	}
    public Map<String, String> lockPublisher() {
		Map<String, String> consumptionMessage = new HashMap<String, String>();
		String delimiter = ",";
		String csvFile = "lock_sensor.csv";
		try {
			File file = new File(csvFile);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			String[] tempArr;
			String lock;
			String timestamp;
			boolean firstLine = true;
			while ((line = br.readLine()) != null) {
				if (!firstLine) {

					tempArr = line.split(delimiter);
					timestamp = tempArr[3];
					lock = tempArr[tempArr.length - 1];
					// consumption = Float.valueOf(tempArr[tempArr.length-1]);
					System.out.print("time stamp:" + timestamp + "- door lock:" + lock);
					consumptionMessage.put("Date", timestamp);
					consumptionMessage.put("Lock Status", lock);
					String consumptionMessageString = consumptionMessage.toString();
					try {

						MqttMessage message = new MqttMessage();
						message.setPayload(consumptionMessageString.getBytes());
						client.publish("LockSensor", message);
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

}
