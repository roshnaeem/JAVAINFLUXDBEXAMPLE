

# Smart Building IOT System

<img src="/dashboard-iot.png" alt="Dashboard IOT" style="height: 400px; width:600px;"/>

## Getting Started

Clone the project

```bash
git clone https://github.com/roshnaeem/JavaInfluxDBExample.git
```

Navigate to the project directory

```bash
cd JavaInfluxDBExample
```

Start the services
```bash
docker-compose up
```

Services can be accessed at follwing ports:

* InfluxDB: http://localhost:8086/
* Grafana: http://localhost:3000/
* Nodered: http://localhost:1880/

## Services Used

### Mqtt
Docker Image: eclipse-mosquitto
### InfluxDB
Docker Image: influxdb:2.1.1
### Telegraf
Telegraf Image: telegraf:1.19
### Grafana
Grafana image: grafana/grafana-oss:8.4.3
### Nodered
Nodered image: nodered:v1.2.0
