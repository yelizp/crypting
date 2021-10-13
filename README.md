Notes: 

1. This application is developed for Windows platform and requires Scala, Docker for Windows and Maven for building the project.
2. You need to run "docker-compose up -d" under the docker-compose.yml file path, to create MongoDB, InfluxDB and Grafana docker containers.
3. Type localhost:8086 in the browser and open InfluxDB UI. You can use username: grafana and password: password to login. 
Create a new bucket called "grafana" with organization "none". Create a new authentication token for user grafana to access bucket grafana.
4. Change the influx2.properties file under the resources directory and set the token you have generated.
5. Type localhost:8081 in the browser and open MongoDB express UI. Create a new database called "crypto_assets" with username "crypto" and password "crypto"
6. Run "mvn clean package" from command line under the project root path
7. Change Use "run.bat" to start the application. The application will create a demo customer if you prove --createCustomer option on the initial run. 
8. Type localhost:3000 in the browser and open Grafana. From the DataSources page create a new InfluxDB connection as:
    Name: InfluxDB (leave it as default as used by the Grafana dashboard)
    Query Language: Flux
    url: http://influxdb:8086 
    Basic Auth checked
    User: grafana (as specified by the docker-compose.yml file)
    Password: password (as specified by the docker-compose.yml file)
    Organization: none
    Token: provide the token you have generated at step 3.
    Default Bucket: grafana
    
    Click the + icon in the side bar and import grafana_dashboard.json file provided with the project.    
