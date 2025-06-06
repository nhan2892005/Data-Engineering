
# BIG DATA PET PROJECT

## 1. Tổng quan

Dự án này xây dựng một pipeline xử lý dữ liệu streaming theo thời gian thực, bao gồm các bước sau:

- **Data Generation & Streaming:**  
  - **Airflow** lên lịch thực hiện công việc hàng ngày.  
  - Gọi API để lấy dữ liệu người dùng, sau đó định dạng và gửi lên Kafka thông qua Producer.

- **Data Processing & Storage:**  
  - **Apache Spark** sử dụng khả năng streaming để đọc dữ liệu từ Kafka, xử lý và định dạng lại.  
  - Dữ liệu được lưu trữ vào **Apache Cassandra**, một cơ sở dữ liệu NoSQL với khả năng mở rộng và độ tin cậy cao.

- **Infrastructure:**  
  - Các thành phần như Kafka, Airflow, Spark, Cassandra cùng với các dịch vụ phụ trợ được triển khai và quản lý qua **Docker Compose** nhằm đảm bảo tính nhất quán và dễ dàng quản lý.

---

## 2. Kiến Trúc Hệ Thống

![System Design](Images/system_arch.png)

**Mô tả:**  
- **Apache Airflow:** Chạy các tác vụ định kỳ, khởi chạy quá trình lấy và gửi dữ liệu.
- **Postgres:** Lưu trữ metadata của Airflow, bao gồm thông tin DAG, task, và lịch trình chạy.
- **Apache Kafka:** Đóng vai trò trung gian (message queue), nơi Producer gửi dữ liệu và Spark Consumer đọc dữ liệu.
- **Apache Spark:** Xử lý dữ liệu theo thời gian thực và chuyển kết quả xử lý vào Cassandra.
- **Apache Cassandra:** Lưu trữ dữ liệu đã được xử lý, đáp ứng nhu cầu truy vấn nhanh và độ bền cao.
- **Docker Compose:** Tích hợp và triển khai các container của từng dịch vụ một cách nhất quán.
---

## 3. Triển Khai

### 3.1. Apache Kafka

#### A. Triển khai Kafka

**Các Service cần triển khai:**  
1. **Zookeeper:**  
   - **Vai trò:** Quản lý metadata của cluster Kafka, theo dõi trạng thái các broker.  
   - **Triển khai:**  
     - **Image:** `confluentinc/cp-zookeeper:7.4.0`  
     - **Ports:** 2181 (host:2181)  
     - **Cấu hình:** Sử dụng biến môi trường `ZOOKEEPER_CLIENT_PORT`, `ZOOKEEPER_TICK_TIME`.

2. **Kafka Broker:**  
   - **Vai trò:** Nhận message từ Producer, lưu trữ và chuyển tiếp đến Consumer.  
   - **Triển khai:**  
     - **Image:** `confluentinc/cp-server:7.4.0`
     - **Ports:**  
       - **9092:** Dành cho kết nối từ bên ngoài Docker.  
       - **29092:** Kết nối nội bộ trong mạng Docker (giúp các container giao tiếp với nhau).  
     - **Cấu hình quan trọng:**  
       - `KAFKA_BROKER_ID`: Xác định định danh của broker.  
       - `KAFKA_ZOOKEEPER_CONNECT`: Liên kết với Zookeeper.  
       - `KAFKA_ADVERTISED_LISTENERS`: Công bố địa chỉ kết nối, thường là dạng:  
         - `PLAINTEXT://broker:29092` cho các container nội bộ,  
         - `PLAINTEXT_HOST://localhost:9092` cho truy cập từ host.

#### B. Khởi tạo Kafka Producer

Để gửi message lên Kafka, sử dụng thư viện `kafka-python`:

```python
from kafka import KafkaProducer
import json

producer = KafkaProducer(
    bootstrap_servers=['broker:29092'],  # 'broker' is a service in Docker Compose, 29092 is internal port
    max_block_ms=5000  # Max time to wait for the broker to respond
)

message = {
    'id': 'example_id',
    'first_name': 'John',
    'last_name': 'Doe'
}

producer.send('users_created', json.dumps(message).encode('utf-8'))
# Make sure all messages are sent before closing the producer
producer.flush()
```

---

### 3.2. Apache Airflow

#### A. Triển khai Airflow bằng Docker

**Các thành phần chính:**  
1. **Webserver:**  
   - **Vai trò:** Giao diện quản lý DAG, theo dõi trạng thái công việc.
   - **Triển khai:**  
     - **Image:** `apache/airflow:2.6.0-python3.9`  
     - **Ports:** 8080 (để truy cập giao diện web)  
     - **Cấu hình:** Sử dụng file `docker-compose.yml` để mount các thư mục chứa DAG và script, đồng thời cấu hình biến môi trường cho kết nối database.

2. **Scheduler:**  
   - **Vai trò:** Lên lịch và thực thi các task theo định nghĩa trong DAG.
   - **Triển khai:** Tương tự như webserver, chạy trên cùng image Airflow nhưng với lệnh khởi động khác.

3. **Postgres:**  
   - **Vai trò:** Lưu trữ metadata cho Airflow (thông tin DAG, trạng thái task, log, …).
   - **Triển khai:**  
     - **Image:** `postgres:14.0`  
     - **Biến môi trường:** `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`.


#### B. Sử Dụng Airflow để Khởi Chạy Pipeline

- **DAG:** Định nghĩa workflow, ví dụ: lấy dữ liệu từ API, gửi dữ liệu lên Kafka thông qua `PythonOperator` gọi hàm `kafka_stream`.
- **Lợi ích:**  
  - Quản lý lịch trình chạy định kỳ.
  - Theo dõi và log trạng thái của từng task.
  - Hỗ trợ phụ thuộc giữa các task.

Ví dụ định nghĩa một DAG:

```python
from datetime import datetime
from airflow import DAG
from airflow.operators.python import PythonOperator

default_args = {
    'owner': 'airflow',
    'start_date': datetime(2025, 3, 12, 14, 30),
}

with DAG('user_info', default_args=default_args, schedule_interval='@daily', catchup=False) as dag:
    streaming_task = PythonOperator(
        task_id='streaming_task',
        python_callable=kafka_stream,  # Function to send data to Kafka
    )
```

---

### 3.3. Apache Spark

#### A. Triển khai Spark bằng Docker

**Các thành phần chính:**  
1. **Spark Master:**  
   - **Vai trò:** Quản lý và phân phối công việc cho các worker.
   - **Triển khai:**  
     - **Image:** `bitnami/spark:latest`  
     - **Ports:**  
       - 9090 (giao diện web quản lý)  
       - 7077 (cổng giao tiếp với worker)

2. **Spark Worker:**  
   - **Vai trò:** Thực hiện các task xử lý dữ liệu.
   - **Triển khai:**  
     - **Image:** `bitnami/spark:latest`  
     - **Cấu hình:** Xác định số core, bộ nhớ sử dụng, và địa chỉ của Spark Master. `spark://spark-master:7077`.


#### B. Kết Nối Spark với Kafka và Cassandra

**Kết nối với Kafka:**

- Sử dụng Spark Structured Streaming để đọc dữ liệu từ Kafka:
  
```python
spark_df = spark_conn.readStream \
    .format('kafka') \
    .option('kafka.bootstrap.servers', 'localhost:9092') \
    .option('subscribe', 'users_created') \
    .option('startingOffsets', 'earliest') \
    .load()
```

- **Giải thích:**  
  - **kafka.bootstrap.servers:** Địa chỉ của Kafka broker (có thể dùng `localhost:9092` nếu đang chạy trên host hoặc điều chỉnh theo cấu hình Docker).
  - **subscribe:** Chọn topic cần đọc dữ liệu.

**Ghi dữ liệu vào Cassandra:**

- Sử dụng Spark Cassandra Connector để ghi dữ liệu sau khi xử lý:
  
```python
streaming_query = (selection_df.writeStream
                   .format("org.apache.spark.sql.cassandra")
                   .option('checkpointLocation', '/tmp/checkpoint')
                   .option('keyspace', 'spark_streams')
                   .option('table', 'created_users')
                   .start())

streaming_query.awaitTermination()
```

- **Giải thích:**  
  - **checkpointLocation:** Thư mục lưu trạng thái tiến trình của stream, giúp Spark duy trì trạng thái khi có sự cố.
  - **keyspace/table:** Tên keyspace và bảng trong Cassandra để lưu dữ liệu.

- **Cấu hình Spark Session:**

```python
from pyspark.sql import SparkSession

spark_conn = SparkSession.builder \
    .appName('SparkDataStreaming') \
    .config('spark.jars.packages', "com.datastax.spark:spark-cassandra-connector_2.13:3.4.1," 
                                   "org.apache.spark:spark-sql-kafka-0-10_2.13:3.4.1") \
    .config('spark.cassandra.connection.host', 'localhost') \
    .getOrCreate()
```

---

### 3.4. Apache Cassandra

#### A. Triển khai Cassandra bằng Docker

- **Vai trò:** Lưu trữ dữ liệu đã được xử lý theo mô hình NoSQL, tối ưu cho các ghi dữ liệu lớn và truy vấn nhanh.
- **Triển khai:**  
  - **Image:** `cassandra:latest`  
  - **Ports:** 9042 (cho giao tiếp CQL)
  - **Biến môi trường:** Có thể cấu hình heap size, user và password.


#### B. Tạo Keyspace và Table

- **Keyspace:** Nơi tổ chức các bảng, ví dụ `spark_streams`.  
- **Table:** Ví dụ `created_users` lưu thông tin người dùng.

Ví dụ sử dụng Python (với thư viện `cassandra-driver`) để tạo keyspace và bảng:

```python
from cassandra.cluster import Cluster

cluster = Cluster(['localhost'])
session = cluster.connect()

# Tạo keyspace nếu chưa tồn tại
session.execute("""
    CREATE KEYSPACE IF NOT EXISTS spark_streams
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};
""")

# Tạo bảng để lưu dữ liệu người dùng
session.execute("""
CREATE TABLE IF NOT EXISTS spark_streams.created_users (
    id UUID PRIMARY KEY,
    first_name TEXT,
    last_name TEXT,
    gender TEXT,
    address TEXT,
    post_code TEXT,
    email TEXT,
    username TEXT,
    registered_date TEXT,
    phone TEXT,
    picture TEXT
);
""")
```

---

### 3.5. Docker Compose – Điều Hành Hệ Thống

**Vai trò:**  
- Tích hợp và quản lý tất cả các container (Kafka, Zookeeper, Airflow, Spark, Cassandra, ...) trong một môi trường nhất quán.
- Dễ dàng cấu hình mạng nội bộ giữa các container qua network (ví dụ: network `confluent`).

Xem cách triển khai [`docker-compose.yml`](docker-compose.yml):

---

## 4. Lý Do Sử Dụng Các Công Nghệ

- **Apache Kafka:**  
  - **Ưu điểm:** Xử lý dữ liệu streaming với hiệu suất cao, khả năng mở rộng và đảm bảo độ bền của dữ liệu.  
  - **Ứng dụng:** Là trung tâm message queue, giúp kết nối giữa các producer (Airflow/Python) và consumer (Spark).

- **Apache Airflow:**  
  - **Ưu điểm:** Tự động hóa, lên lịch và theo dõi các workflow phức tạp, đảm bảo các task chạy đúng trình tự.  
  - **Ứng dụng:** Quản lý lịch trình và thực thi DAG định nghĩa pipeline xử lý dữ liệu.

- **Apache Spark:**  
  - **Ưu điểm:** Xử lý dữ liệu phân tán, hỗ trợ streaming và batch processing, tích hợp tốt với nhiều nguồn dữ liệu khác nhau.  
  - **Ứng dụng:** Đọc dữ liệu từ Kafka, xử lý và chuyển kết quả vào Cassandra bằng cách sử dụng Spark Structured Streaming.

- **Apache Cassandra:**  
  - **Ưu điểm:** Cơ sở dữ liệu NoSQL có khả năng mở rộng ngang, độ sẵn sàng cao, phù hợp với các ứng dụng ghi dữ liệu lớn.  
  - **Ứng dụng:** Lưu trữ dữ liệu sau khi được Spark xử lý, đáp ứng nhanh các truy vấn trong môi trường production.
