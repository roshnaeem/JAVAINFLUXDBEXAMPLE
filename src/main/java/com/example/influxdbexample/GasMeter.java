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

public class GasMeter {
    MqttClient client;

	public GasMeter() {
		try {
			client = new MqttClient("tcp://javainfluxdbexample-mosquitto-container-1:1883", "gas_sensor");
			client.connect();
		} catch (MqttException e) {
			e.printStackTrace();
		}

	}
    public static void main(String[] args) {

		GasMeter meter = new GasMeter();

		meter.gasPublisher();
	}
    public Map<String, String> gasPublisher() {
		Map<String, String> consumptionMessage = new HashMap<String, String>();
		String delimiter = ",";
		// String csvFile = "/Users/rosh/Downloads/JavaInfluxDBExample/data/gas_hourly.csv";
		String csvFile = "gas_hourly.csv";

		try {
			File file = new File(csvFile);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			String[] tempArr;
			String consumption;
			String timestamp;
			boolean firstLine = true;
			while ((line = br.readLine()) != null) {
				if (!firstLine) {

					tempArr = line.split(delimiter);
					timestamp = tempArr[0];
					consumption = tempArr[1];
					// consumption = Float.valueOf(tempArr[tempArr.length-1]);
					System.out.print("time stamp:" + timestamp + "- gas consumption:" + consumption);
					consumptionMessage.put("Date", timestamp);
					consumptionMessage.put("Consumption", consumption);
					String consumptionMessageString = consumptionMessage.toString();
					try {

						MqttMessage message = new MqttMessage();
						message.setPayload(consumptionMessageString.getBytes());
						client.publish("SmartMeter/gas", message);
						//System.out.println("publisheeddd");
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
