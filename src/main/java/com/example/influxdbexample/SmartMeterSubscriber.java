package com.example.influxdbexample;

import static java.util.concurrent.TimeUnit.SECONDS;

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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.LogEvent;
import com.influxdb.client.domain.Query;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import com.influxdb.client.WriteApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

public class SmartMeterSubscriber implements MqttCallback {

  MqttClient client;
  InfluxDBClient influxDBClient_water;
  InfluxDBClient influxDBClient_gas;
  InfluxDBClient influxDBClient_electricity;
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

    if (topic.equals("SmartMeter/electricity")) {

      System.out.println(String.format(" received electricity: %s", messageText));
      int consumption_electricity = Math.round(Float.parseFloat(messageText.split(",")[0].split("=")[1]));
      String date = messageText.split(",")[1].split("=")[1].split("}")[0];
      System.out.println("consumption is:" + consumption_electricity);
      System.out.println("Date is:" + date);

      try{
      Point point_electricity = Point
          .measurement("home_consumption")
          .addTag("electricity_consumption", "electricity").addField("electricity", consumption_electricity)
          .time(Instant.now(), WritePrecision.NS);

      WriteApiBlocking writeApi_electricity = influxDBClient_electricity.getWriteApiBlocking();
      writeApi_electricity.writePoint(bucket, org, point_electricity);
      }catch(InfluxException e){
        System.out.println(e);
      }

    } else if (topic.equals("SmartMeter/gas")) {

      System.out.println(String.format(" received gas: %s", messageText));
      int consumption = Math.round(Float.parseFloat(messageText.split(",")[0].split("=")[1]));
      //String date = messageText.split(",")[1].split("=")[1].split("}")[0];
      System.out.println("gas consumption is:" + consumption);

      Point point = Point
          .measurement("home_consumption")
          .addTag("gas_consumption", "gas").addField("gas", consumption)
          //.time(Instant.now(), WritePrecision.NS);
          .time(Instant.now(), WritePrecision.NS);

      WriteApiBlocking writeApi_gas = influxDBClient_gas.getWriteApiBlocking();
      writeApi_gas.writePoint(bucket, org, point);

      System.out.println("point:");
      System.out.println(point.hasFields());

    } else if (topic.equals("SmartMeter/water")) {

      System.out.println(String.format(" received water: %s", messageText));
      int consumption_water = Integer.parseInt(messageText.split(",")[0].split("=")[1]);

      String date = messageText.split(",")[1].split("=")[1].split("}")[0];
      System.out.println("water consumption is:" + consumption_water);
      System.out.println("Date is:" + date);

      Point point_water = Point
          .measurement("home_consumption")
          .addTag("water_consumption", "water").addField("water", consumption_water)
          .time(Instant.now(), WritePrecision.NS);

      WriteApiBlocking writeApi_water = influxDBClient_water.getWriteApiBlocking();
      writeApi_water.writePoint(bucket, org, point_water);

      System.out.println("point:");
      System.out.println(point_water.hasFields());
    }

  }

  public static void main(String[] args) {

    // new PahoDemoImplCallBack().publish1();
    SmartMeterSubscriber SmartMeter = new SmartMeterSubscriber();
    SmartMeter.setupDB();
    SmartMeter.subscribe();

  }

  public void setupDB() {

    try {
      influxDBClient_gas = InfluxDBClientFactory.create("http://javainfluxdbexample-influxdb-1:8086", token.toCharArray());
      influxDBClient_water = InfluxDBClientFactory.create("http://javainfluxdbexample-influxdb-1:8086", token.toCharArray());
      influxDBClient_electricity = InfluxDBClientFactory.create("http://javainfluxdbexample-influxdb-1:8086", token.toCharArray());
      System.out.println("setup db");

    } catch (InfluxException e) {

      System.out.print("are we here?");
      System.out.print(e);
    }

    // influxDBClient = InfluxDBClientFactory.create(url, token, org, bucket);
    System.out.println(influxDBClient_gas.getDashboardsApi());
    System.out.println(influxDBClient_water.getDashboardsApi());
    System.out.println(influxDBClient_electricity.getDashboardsApi());

  }

  public void subscribe() {
    try {
      client = new MqttClient("tcp://javainfluxdbexample-mosquitto-container-1:1883", "SmartMeter");

      //client = new MqttClient("tcp://localhost:1883", "SmartMeter");

      client.setCallback(this);
      client.connect();
      try {

        client.subscribe("SmartMeter/electricity", 2);
        client.subscribe("SmartMeter/gas", 2);
        client.subscribe("SmartMeter/water", 2);

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