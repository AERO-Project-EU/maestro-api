# ! /bin/bash

echo $'\nInstalling Java\n'
# sudo apt-get update 
# sudo apt-get upgrade -y && \
# sudo apt-get install -y  software-properties-common && \
# sudo add-apt-repository ppa:webupd8team/java -y && \
# sudo apt-get update && \
# sudo echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
# sudo apt-get install -y oracle-java8-installer && \
# sudo apt-get install -y oracle-java8-set-default

sudo apt-get update 
sudo apt-get upgrade -y
# sudo apt-get install oracle-java8-jdk -y
sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update
sudo apt-get install -y oracle-java8-set-default


echo $'\nInstalling Ubi Agent\n'
# Set AGENT_URL to where you host the agent jar, and AGENT_TOKEN if that host needs a token.
sudo wget --header "PRIVATE-TOKEN: $AGENT_TOKEN" "$AGENT_URL"  -O /opt/agent.jar

echo $'\nInstalling Python\n'
sudo apt-get install -y python2.7

echo $'\nInstalling Docker\n'
sudo apt-get remove docker docker-engine docker.io -y
sudo apt-get update -y
sudo apt-get install apt-transport-https ca-certificates curl software-properties-common -y
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get update -y
sudo apt-get install docker-ce -y
sudo groupadd docker
sudo usermod -aG docker ubuntu

echo $'\nInstalling Netdata Slave\n'
curl https://my-netdata.io/kickstart-static64.sh > /tmp/kickstart-static64.sh
echo "y" | sh /tmp/kickstart-static64.sh

echo $'\nInstalling Consule Agent\n'
# Downloading the consul
wget https://releases.hashicorp.com/consul/1.6.1/consul_1.6.1_linux_amd64.zip
# Instal unzip and unzip the file
sudo apt-get install unzip -y
unzip consul_1.6.1_linux_amd64.zip
# Remove unnecessary unzip installation and consul zip file
sudo apt-get remove unzip -y
rm consul_1.6.1_linux_amd64.zip
# Move consul to /opt folder and make /etc/consul.d folder
sudo mv consul /opt/
sudo mkdir /etc/consul.d

echo $'\nEnable IPV6 for docker\n'
daemon="{
    	\"ipv6\": true,
     	\"fixed-cidr-v6\": \"2001:db8::/122\"
}"

sudo echo $daemon > daemon.json
sudo mv daemon.json /etc/docker/daemon.json

echo $'\nInstall cjdns\n'
sudo apt-get install -y git build-essential nodejs
sudo mkdir -p /opt/cjdns
sudo git clone https://github.com/cjdelisle/cjdns.git /opt/cjdns
cd /opt/cjdns
sudo ./do
sudo mkdir -p /etc/cjdns
sudo chmod 755 /etc/cjdns

echo $'\nInstall jq\n'
sudo apt-get install -y jq

echo $'\nInstall xdp prerequisits\n'
#mkdir ~/xdp
#cd xdp
#cp xdp_ddos01_blacklist xdp_ddos01_blacklist_cmdline xdp_ddos01_blacklist_kern.o
#sudo mount -t bpf bpf /sys/fs/bpf/
#copy the following line to the /etc/fstab
#bpf    /sys/fs/bpf/  bpf  defaults    0   0

echo $'\nCleaning\n'
sudo apt-get clean
sudo rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

