# Create user hadoop and group hadoop
#!/bin/bash
sudo addgroup hadoop
sudo adduser --ingroup hadoop hduser


# Install Hadoop
cd ~
wget https://dlcdn.apache.org/hadoop/common/hadoop-3.4.1/hadoop-3.4.1.tar.gz
tar -xzf hadoop-3.4.1.tar.gz
mv hadoop-3.4.1 hadoop

# Hadoop
# code to add to ~/.bashrc
# export HADOOP_HOME="$HOME/hadoop"
# export PATH="$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH"
# export HADOOP_CONF_DIR="$HADOOP_HOME/etc/hadoop"
# export JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"
# source ~/.bashrc