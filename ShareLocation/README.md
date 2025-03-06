# Notes

<details>
<summary><code>Download Docker in Ubuntu</code></summary>

---

### 1. Update list package
```sh
sudo apt update
```
---

### 2. Download required packages dependencies
```sh
sudo apt install apt-transport-https ca-certificates curl software-properties-common gnupg lsb-release -y
```
---

### 3. Add Docker's official GPG key
```sh
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
```
---

### 4. Add Docker repository into the system
```sh
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```
---

### 5. Update list package
```sh
sudo apt update
```
---

### 6. Install Docker Engine
```sh
sudo apt install docker-ce docker-ce-cli containerd.io -y
```
---

### 7. Check Docker status
```sh
sudo systemctl status docker
```
---

### 8. Test Docker with "hello-world" container
```sh
sudo docker run hello-world
```
---

### 9. (Optional) Add your user to the docker group
```sh
sudo usermod -aG docker ${USER}
```
---
</details>