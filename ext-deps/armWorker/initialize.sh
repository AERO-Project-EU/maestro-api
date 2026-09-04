#Install CJDNS
#wget https://raw.githubusercontent.com/tomeshnet/prototype-cjdns-pi/master/scripts/install && chmod +x install && ./install 

#Create folder which will contain the init scripts
echo $'\nCreating folder for scripts\n'
sudo mkdir -p /opt/scripts

echo $'\nInstalling Java\n'
sudo apt-get install oracle-java8-jdk -y

echo $'\nDownloading the agent\n'
sudo rm /opt/iotagent-1.0-SNAPSHOT-spring-boot.jar
sudo wget "$IOT_AGENT_URL" -P /opt/

echo $'\nInstalling Docker\n'
curl -sSL https://get.docker.com | sh
sudo groupadd docker
sudo usermod -aG docker pi

echo $'\nInstalling Netdata\n'
curl -Ss https://my-netdata.io/kickstart.sh >> netdata.sh
chmod +x netdata.sh
printf '\nY\n\n\n' | ./netdata.sh
printf '\nY\n\n\n' | ./netdata.sh

echo $'\nInstalling Consule Agent\n'
# Downloading the consul
wget https://releases.hashicorp.com/consul/1.0.7/consul_1.0.7_linux_arm.zip
# Instal unzip and unzip the file
sudo apt-get install unzip -y
unzip consul_1.0.7_linux_arm.zip
# Remove unnecessary unzip installation and consul zip file
sudo apt-get remove unzip -y
rm consul_1.0.7_linux_arm.zip
# Move consul to /opt folder and make /etc/consul.d folder
sudo mv consul /opt/
sudo mkdir /etc/consul.d

#Create consul as a service
echo "#!/bin/bash
mkdir -p /tmp/consul
privateIP=$(ip a s | grep -A8 -m1 MULTICAST | grep -m1 inet | cut -d' ' -f6 | cut -d'/' -f1)
masterIP=$CONSUL_MASTER_IP
sudo /opt/consul agent -join=\$masterIP -data-dir=/tmp/consul -bind=\$privateIP -advertise=\$privateIP -enable-script-checks=true -client=0.0.0.0 -config-dir=/etc/consul.d " | sudo tee /opt/scripts/consul-start.sh
sudo chmod +x /opt/scripts/consul-start.sh

echo -e '#!/bin/bash\nps -e | grep consul | awk "{print $1}" | xargs kill -9 ' | sudo tee /opt/scripts/consul-stop.sh
sudo chmod +x /opt/scripts/consul-stop.sh

echo -e '[Unit]
Description=consul
After=syslog.target network.target

[Service]
User=root
ExecStart=/opt/scripts/consul-start.sh
#ExecStop=/opt/scripts/consul-stop.sh
Restart=on-failure
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target' | sudo tee /etc/systemd/system/consul.service

#Start consul service
sudo systemctl daemon-reload
sudo systemctl disable consul.service
#sudo systemctl start consul.service

#Install iperf3
echo $'\nInstalling iperf3\n'
sudo apt-get install iperf3 -y

#Create agent as a service
echo -e '#!/bin/bash
java -jar /opt/iotagent-1.0-SNAPSHOT-spring-boot.jar --spring.profiles.active=node >> /home/pi/agent.log ' | sudo tee /opt/scripts/agent-start.sh

sudo chmod +x /opt/scripts/agent-start.sh
echo -e '#!/bin/bash\n ps -ef | grep iotagent | awk \'{print $2}\' | xargs kill -9 ' | sudo tee /opt/scripts/agent-stop.sh
sudo chmod +x /opt/scripts/agent-stop.sh

echo -e '[Unit]
Description=agent
After=syslog.target network.target

[Service]
User=root
ExecStart=/opt/scripts/agent-start.sh
ExecStop=/opt/scripts/agent-stop.sh
Restart=on-failure
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target' | sudo tee /etc/systemd/system/agent.service

#Start agent service
sudo systemctl daemon-reload
sudo systemctl enable agent.service
sudo systemctl start agent.service

echo $'\nCheck installation\n'
echo $'Java Version'
java -version
echo $'\nDocker Version'
docker --version
echo $'\nConsul version'
sudo /opt/consul --version
echo $'\nNetdata service status'
sudo systemctl status netdata.service
