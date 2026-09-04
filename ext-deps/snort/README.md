# Snort  
An Network Intrusion Detection & Prevention System

## Prerequisites

* Docker 18.03.x

Before moving on, make sure you have the required JDK and Maven version.
    
	# In sudo mode run the following script
	./initSnort.sh
## How to use it
In order to run it in fast mode without geting a shell

    docker run -d --rm --privileged --cap-add=NET_RAW --name=snort --network=host -it  -v /data/snort/config:/var/lib/snort -v /data/snort/logs:/var/log/snort snort:1.0.0

In order to get a shell run 
    
    docker run --rm --privileged --cap-add=NET_RAW --name=snort --network=host -it  -v /data/snort/config:/var/lib/snort -v /data/snort/logs:/var/log/snort snort:1.0.0 /bin/sh
    
For running snort in the different modes
    
    # Full mode
    snort -A full -q -c /var/lib/snort/etc/snort.conf -i any -N
    # Fast mode
    snort -A fast -q -c /var/lib/snort/etc/snort.conf -i any -N
    # Console mode
    snort -A console -q -c /var/lib/snort/etc/snort.conf -i any -N
    
# Testing
    tail -f /data/snort/logs/alert
    ping localhost