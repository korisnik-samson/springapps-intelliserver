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

# ***SSL Configuration - OPTIONAL***

* Install Certbot
```bash

sudo apt install certbot python3-certbot-nginx -y
```

* Obtain an SSL certificate
```bash

sudo certbot --nginx
```

* Test the SSL certificate renewal
```bash

sudo certbot renew --dry-run
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

