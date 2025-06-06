!/bin/bash

# Install Java
sudo apt install -y openjdk-11-jdk
## Set env variables
echo 'export JAVA_HOME=/usr/bin/java' >> ~/.bashrc

# Create user hadoop and group hadoop
sudo addgroup hadoop
sudo adduser --ingroup hadoop hduser

# Install Hadoop
cd ~
wget https://dlcdn.apache.org/hadoop/common/hadoop-3.4.1/hadoop-3.4.1.tar.gz
tar -xzf hadoop-3.4.1.tar.gz
mv hadoop-3.4.1 hadoop
## Set env variables
echo 'export HADOOP_HOME="$HOME/hadoop"' >> ~/.bashrc
echo 'export PATH="$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH"' >> ~/.bashrc
echo 'export HADOOP_CONF_DIR="$HADOOP_HOME/etc/hadoop"' >> ~/.bashrc

source ~/.bashrc