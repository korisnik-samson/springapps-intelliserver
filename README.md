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
