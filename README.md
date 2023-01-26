



## ⚡️ Getting Started

Clone the project

```bash
git clone https://github.com/roshnaeem/JAVAINFLUXDBEXAMPLE.git
```

Navigate to the project directory

```bash
cd JAVAINFLUXDBEXAMPLE
```

Change the environment variables define in `.env` that are used to setup and deploy the stack
```bash
├── telegraf/
├── .env         <---
├── docker-compose.yml
├── entrypoint.sh
└── ...
```

Customize the `telegraf.conf` file which will be mounted to the container as a persistent volume

```bash
├── telegraf/
│   ├── telegraf.conf <---
├── .env
├── docker-compose.yml
├── entrypoint.sh
└── ...
```

Start the services
```bash
docker-compose up -d
```
## Docker Images Used (Official & Verified)
