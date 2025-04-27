# Azure VM Credentials
* vm-username => intelli
* instance-vm-name => intelli-vm

# Install Java & JDKS
```bash

sudo apt install openjdk-22-jre-headless
```

**Try to stay with versions supported by starter.spring.io**

# Git & SSH Setup

```bash

sudo apt install git -y 

git config --global user.name 'myusername'
git config --global user.email 'myemail'

ssh-keygen -t ed25519 -C "myemail"
```

* You can skip the next two prompts

```bash

cat ~/.ssh/id_ed25519.pub
```

* Then copy that to your GitHub keys

# Spring Boot Installation & SDK Man Installation (SDK Shell Client)

```bash

curl -s https://get.sdkman.io | bash

source "/root/.sdkman/bin/sdkman-init.sh"

sdk install springboot
```

**SDK Man is a tool that makes it much easier to install and manage multiple versions of SDKs**

```bash 

./mvnw spring-boot:run
```

## Possible Errors 
* If you get an error like "Permission Denied" when running the above command, you need to add execute permissions to the mvnw file
* Issues with some Maven packages not installed (mvnw is a maven wrapper)
* Database not yet configured (Use Postgres

# Postgres Installation

* Install Postgres
```bash

sudo apt update
sudo apt install postgresql postgresql-contrib
```

* Start Postgres
```bash

sudo -u postgres psql
```

* Create a new user and database
```postgresql
CREATE USER your_username WITH PASSWORD 'your_password';
CREATE DATABASE your_database_name OWNER your_username;
```

* Exit Postgres
```postgresql
\q
```

* Configure Postgres to accept connections (Optional - replacing 16 with current version)
```bash

sudo nano /etc/postgresql/16/main/postgresql.conf 
```

* Change the line in ``postgresql.conf`` to listen to all addresses
```text
listen_addresses = '*'
```

*  Allow the user (your_username) to connect to the database your_database_name from any IP address (0.0.0.0/0) using MD5 
   authentication.
```bash

sudo nano /etc/postgresql/15/main/pg_hba.conf
```

* Restart Postgres
```bash

sudo systemctl restart postgresql
```

# Spring Application Service Configuration

* Create a new file in ``/etc/systemd/system``

```bash

sudo nano /etc/systemd/system/springapps-intelliserver.service
```

* Add the following content to the file
```text
[Unit]
Description=<Application Name>
After=syslog.target
After=network.target[Service]
User=root
Type=simple

[Service]
ExecStart=/usr/bin/java -jar absolute/path/to/your/jarfile.jar
Restart=always
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=<application-name>

[Install]
WantedBy=multi-user.target
```

* Reload the systemd daemon
```bash

sudo systemctl daemon-reload
```

* Start the service
```bash

sudo systemctl start springapps-intelliserver
```

* Enable the service to start on boot
```bash

sudo systemctl enable springapps-intelliserver
```

* Check the status of the service
```bash

sudo systemctl status springapps-intelliserver
```


# NGINX Installation & Configuration

* Install NGINX
```bash

sudo apt install nginx -y
```

* Configure NGINX as a virtual host
```bash

sudo nano /etc/nginx/conf.d/helloworld.conf
```

* Add the following content to the file
```text

server {
        listen 80;

        server_name domain.example.com;

        location / {
            proxy_pass http://localhost:8080/;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_set_header X-Forwarded-Port $server_port;
        }
}
```

* Edit the default NGINX configuration
```bash

nano /etc/nginx/nginx.conf
```

* Add the following line to the file in the http block
```text

server_names_hash_bucket_size 64;
```

* Test the NGINX configuration
```bash

sudo nginx -t
```

* Restart NGINX
```bash

sudo systemctl restart nginx
```

* Enable NGINX to start on boot
```bash

sudo systemctl enable nginx
```

# ***SSL Configuratio***

* Obtain a keystore from Java for the Spring boot Application
```bash

${JAVA_HOME}/bin/keytool -genkeypair -alias springapps-intelliserver -keystore 
C:\Users\sammi\Desktop\repositories\springapps-intelliserver\src\main\resources\springapps-intelliserver
-keystore.p12 -keypass secret -storeType PKCS12 -storepass secret -keyalg RSA -keysize 2048 -validity 365 -dname "C=Serbia, ST=Belgrade, L=Zvezdara, O=Samson, OU=YourUnit, CN=Samson" -ext "SAN=dns:localhost"
```

* Extract the Certificate and Private key which will be used for NGINX
```bash

# Extract the certificate
openssl pkcs12 -in /home/intelliuser/repository/springapps-intelliserver/src/main/resources/springapps-intelliserver-keystore.p12 -clcerts -nokeys -out /home/intelliuser/repository/cert.pem -password pass:secret

# Extract the private key
openssl pkcs12 -in /home/intelliuser/repository/springapps-intelliserver/src/main/resources/springapps-intelliserver-keystore.p12 -nocerts -nodes -out /home/intelliuser/repository/key.pem -password pass:secret
```

* Making necessary changes to the NGINX configuration
```bash
server {
        listen 443 ssl;
        server_name userservice.westeurope.cloudapp.azure.com;
        
        # Point to the extracted PEM files
        ssl_certificate /home/intelliuser/repository/cert.pem;
        ssl_certificate_key /home/intelliuser/repository/key.pem;
        
        # Adding recommended SSL parameters is optional
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_prefer_server_ciphers on;
        ssl_ciphers 'ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384';
        
        location / {
            proxy_pass https://localhost:8443/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_set_header X-Forwarded-Port $server_port;

            proxy_buffer_size 128k;
            proxy_buffers 4 256k;
            proxy_busy_buffers_size 256k;
        }
}
````

* Test the SSL certificate renewal
```bash

sudo nginx -t
```

# Automation Script

* Create a new file ``desiredname.sh``

```shell

#!/bin/bash

cd server/repos

sudo systemctl stop springappsintelliserver

rm -rf ./springapps-intelliserver

git clone https://github.com/korisnik-samson/springapps-intelliserver

cp ./backup/pom.xml ./springapps-intelliserver/
cp ./backup/application.properties ./springapps-intelliserver/src/main/resources/

if [ -f "./springapps-intelliserver/pom.xml" ]; then
  echo "pom.xml copied successfully!"
else
  echo "Error copying pom.xml!"
  exit 1
fi

if [ -f "./springapps-intelliserver/src/main/resources/application.properties" ]; then
  echo "application.properties copied successfully!"
else
  echo "Error copying application.properties!"
  exit 1
fi

cd springapps-intelliserver

mvn package

sudo nginx -t

sudo systemctl restart nginx
sudo systemctl enable nginx

sudo systemctl start springapps-intelliserver
sudo systemctl enable springapps-intelliserver

if [ $(sudo systemctl is-active springapps-intelliserver) = "active" ]; then
  echo "IntelliServer listening on PORT 8080"
else
  echo "Error starting springapps-intelliserver!"
  exit 1
fi
```

# ***Cron Job***

* Open the crontab file
```bash

crontab -e
```

* Add the following line to the file
```text

0 0 * * * /path/to/your/script.sh
```

* Save and exit the file
* This will run the script every day at midnight

