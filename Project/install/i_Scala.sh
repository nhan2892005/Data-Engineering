sudo apt install -y openjdk-11-jdk
java -version

# add sbt repository
echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" \
  | sudo tee /etc/apt/sources.list.d/sbt.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x99E82A75642AC823" \
  | sudo apt-key add -

sudo apt update
sudo apt install -y sbt
sbt --version