package com.example.influxdbexample;

import java.time.Instant;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

public class App {


    // private static Result oneResult;

    public static void main(String[] args) {

        char[] token = "bdeb43c95a98f677466257a2fdd013e31203a8a7680c3dbf44a68164b1634d0d".toCharArray();;
        String bucket = "rosh_sht_bucket";
        String org = "rosh_org";
        String url = "http://localhost:8086";
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org, bucket);
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        Point point = Point.measurement("_measurement")
                .addTag("location", "pouyeh")
                .addField("_value", 55D)
                .time(Instant.now().toEpochMilli(), WritePrecision.MS);

        writeApi.writePoint(point);

    }

}