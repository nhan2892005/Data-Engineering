sudo apt update

# Install PostgreSQL and its contrib package
sudo apt install -y postgresql postgresql-contrib

# Check the status of PostgreSQL service
sudo systemctl status postgresql
