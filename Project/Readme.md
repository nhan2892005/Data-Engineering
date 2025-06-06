Below is a comprehensive overview covering:

1. **What Hadoop is** (its components and purpose),
2. **How Hadoop and Apache Spark work together**,
3. **Real-world applications** of Hadoop/Spark in large-scale data processing,
4. A **demo-style project outline** (architecture, step-by-step instructions, and Scala/Spark/Kafka code snippets) that shows data ingestion from an e-commerce platform’s Open API via Kafka, streaming/batch processing with Spark + Hadoop + MongoDB, and finally loading into PostgreSQL for visualization in Power BI.

---

## 1. What Is Hadoop?

**Apache Hadoop** is an open-source framework for distributed storage and processing of very large datasets, built to run on commodity hardware. It is composed of several core components:

1. **HDFS (Hadoop Distributed File System):**

   * A fault-tolerant, distributed filesystem that splits large files into blocks (default size: 128 MB or 256 MB blocks) and replicates them across multiple DataNodes.
   * Provides high throughput access to application data.

2. **YARN (Yet Another Resource Negotiator):**

   * A resource-management layer (job scheduler + resource negotiator).
   * Keeps track of cluster resources (CPU, memory) and allocates them to running applications.

3. **MapReduce (MR):**

   * A programming model for batch processing large datasets in a distributed manner.
   * A job is split into **mappers** (which process input splits in parallel) and **reducers** (which aggregate/merge mapper outputs).

4. **Common/Utilities:**

   * Shared utilities that support the other Hadoop modules.

On top of these core components, there is a rich ecosystem (often called “Hadoop ecosystem”):

* **Hive:** SQL-on-Hadoop (data warehousing, HiveQL → MapReduce/Spark).
* **HBase:** NoSQL columnar store on top of HDFS (random, real-time reads/writes).
* **Pig:** A high-level scripting language that compiles to MapReduce.
* **Oozie:** Workflow scheduler for Hadoop jobs.
* **Sqoop, Flume:** Data ingestion (Sqoop: relational→HDFS; Flume: streaming logs).
* **Zookeeper:** Coordination service often used by HBase, Kafka, etc.

> **Why use Hadoop?**
>
> * **Scalability:** Run on clusters of hundreds or thousands of cheap servers.
> * **Fault-tolerance:** HDFS automatically replicates blocks; if a DataNode dies, another copy is used.
> * **Cost-effective:** Commodity hardware rather than expensive proprietary machines.
> * **High throughput:** Optimized for large, sequential reads/writes rather than random access.

---

## 2. How Hadoop Works with Apache Spark

**Apache Spark** is an open-source, distributed computing engine designed for speed, ease of use, and sophisticated analytics. While Hadoop’s original batch engine is MapReduce, Spark provides:

* **In-memory processing:** Data can be cached in RAM across the cluster, making iterative algorithms (e.g., machine learning, graph processing) much faster than disk-based MapReduce.
* **Rich APIs:** Native support for Scala, Java, Python, R, and SQL (via Spark SQL).
* **Unified engine:** Spark supports batch (Spark Core/RDDs/DataFrames), streaming (Structured Streaming), machine learning (MLlib), graph processing (GraphX), and graph analytics.

### 2.1. Integration Points

1. **Storage**

   * **HDFS as default data source/sink:** Spark can read/write data from/to HDFS out-of-the-box (`spark.read.text("hdfs://...")`, `df.write.parquet("hdfs://...")`, etc.).
   * **Leverage HDFS’s fault tolerance and data locality:** When you submit a Spark job on YARN (the same Hadoop cluster), Spark’s executors will ideally be scheduled on nodes that already hold the HDFS blocks (data locality), reducing network traffic.

2. **Resource Management**

   * **Run Spark on YARN (or Mesos/Kubernetes):** Spark’s driver and executors become YARN applications/containers. YARN allocates CPU/memory to each executor, monitors health, and can restart failed containers.
   * You can also run Spark in **Standalone mode** or on **Kubernetes**, but on a Hadoop cluster, YARN is most common.

3. **Scheduling**

   * Spark jobs are scheduled by the **Spark Context** on top of YARN:

     1. **Application Master** is launched by YARN to coordinate resource requests.
     2. **Executors** are launched on NodeManagers with allocated RAM/CPU.
     3. Spark tasks (partitions of RDD/DataFrame) run inside executors, processing HDFS data.

4. **Input Formats & Data Sources**

   * Spark can directly consume Hadoop’s input formats (SequenceFiles, Avro, Parquet, ORC, etc.).
   * Many Hadoop ecosystem tools (Hive metastore, HBase) can serve as input/output for Spark.

5. **YARN-Managed Libraries**

   * If a Hadoop cluster has HDFS, YARN, Hive, HBase installed, Spark applications can seamlessly use Hive metastore tables, HBase tables, HDFS paths, etc., without manual configuration.

### 2.2. Execution Flow (Typical Spark-on-YARN)

1. **Client Submits Job:**

   ```
   spark-submit \
     --master yarn \
     --deploy-mode cluster \
     --num-executors 10 \
     --executor-memory 4G \
     --executor-cores 2 \
     app.jar
   ```
2. **YM (ResourceManager) Launches Application Master (AM):**

   * AM negotiates resources for Spark executors.
3. **AM Requests Containers (Executors):**

   * Each container is a JVM that runs `ExecutorBackend` which creates a thread-pool for tasks.
   * Executors register with the Driver (which could be in AM or client, depending on mode).
4. **Tasks Scheduled:**

   * Driver divides each stage into tasks based on partitioning (e.g., reading 128 MB HDFS blocks).
   * Tasks are sent to executors; executors read data from local HDFS DataNodes if available (data locality).
5. **Shuffle (if needed):**

   * Output of map tasks is written to local disk on each executor, then fetched by reduce tasks.
   * Intermediate data is stored in memory when possible (with the Tungsten shuffle implementation) for speed.
6. **Result Written Back to HDFS (or External Sink):**

   * The final DataFrame/RDD is saved back to HDFS (or HBase, Kafka, JDBC, etc.).

---

## 3. Real-World Applications of Hadoop/Spark

Below are some common, high-impact use cases where Hadoop (HDFS/YARN/MapReduce) and Spark shine:

1. **Clickstream Analysis & Web Analytics (E-commerce, Adtech):**

   * Collect user click logs (Product views, clicks, add-to-cart, transactions) via Kafka/Flume.
   * Store raw logs in HDFS.
   * Use Spark (batch or streaming) to aggregate click counts, sessionization, funnel analysis, personalized recommendations.

2. **Recommendation Engines (Retail, Media Streaming):**

   * Ingest user–item interaction data into HDFS.
   * Use Spark MLlib to train collaborative filtering (ALS) or content-based models.
   * Periodically re-train models on Hadoop/Spark clusters for offline batch jobs.

3. **Fraud Detection (Banking, FinTech):**

   * Ingest transaction events via Kafka, store in HDFS or HBase.
   * Use Spark Streaming (Structured Streaming) + MLlib to score transactions in near real time.
   * Flag suspicious patterns, feed alerts downstream.

4. **Log Processing & Monitoring (IT, DevOps):**

   * Centralize logs from hundreds of servers using Flume or Kafka → HDFS.
   * Run MapReduce or Spark jobs to parse, index (e.g., push to Elasticsearch), and aggregate key metrics.
   * Use Zeppelin/Jupyter notebooks to visualize graphs.

5. **Genomics & Bioinformatics:**

   * Store massive sequencing datasets in HDFS.
   * Use Hadoop/Spark jobs to align sequences, run variant calling pipelines.
   * Benefit: run parallel analyses on clusters with YARN resource management.

6. **Social Network Analytics (User graph analysis):**

   * Use **GraphX** (Spark’s graph library) on top of Hadoop to compute PageRank, community detection, shortest paths.

7. **Data Warehousing (SQL Analytics on Big Data):**

   * Hive (or Spark SQL) on top of HDFS for large tables (petabytes).
   * BI tools (e.g., Power BI, Tableau, QlikSense) query via JDBC/ODBC connectors into Hive/Spark Thrift Server.

8. **Machine Learning Pipelines (Predictive Maintenance, Churn Prediction):**

   * Pipelines combine ingestion (Kafka), storage (HDFS), feature extraction (Spark), model training (Spark MLlib), and model serving (e.g., MLeap, TensorFlow Serving).

In all these scenarios, you often see **Hadoop** (for storage + resource management) together with **Spark** (for fast, in-memory analytics) forming the backbone of a modern data platform.

---

## 4. Demo Project: End-to-End Pipeline with Kafka → Spark → HDFS (Hadoop) + MongoDB → PostgreSQL → Power BI (All in Scala)

Below is an end-to-end **reference architecture** and a “demo style” walkthrough that shows how you might build a real system ingesting data from an e-commerce Open API, processing it via Kafka + Spark, persisting to different storage layers (HDFS & MongoDB), then loading into Postgres for BI (Power BI). All your data processing code will be in **Scala**.

> **Assumptions (for a “demo”/“POC” environment):**
>
> * You have a single-node pseudo-distributed Hadoop (HDFS + YARN) or a small multi-node cluster.
> * You have Kafka and Zookeeper running (single-broker or multi-broker).
> * MongoDB is up locally (or in a container).
> * PostgreSQL is running locally (or in a container).
> * Power BI desktop is on a Windows machine that can connect to PostgreSQL over TCP.

### 4.1. Overall Architecture Diagram (Logical Flow)

```
┌──────────────────────────┐
│  E-commerce Open API     │
│  (REST endpoints)        │
└───────────┬──────────────┘
            │ (pull via scheduled Scala producer)
            ▼
┌──────────────────────────┐
│  Kafka Cluster           │
│  (Topic = "ecommerce")   │
└───────────┬──────────────┘
            │ (Spark Structured Streaming reads)
            ▼
┌─────────────────────────────────────────────────────┐
│  Apache Spark Structured Streaming Job (Scala)      │
│  – read from Kafka topic "ecommerce"                │
│  – parse JSON / flatten fields                      │
│  – write raw JSON → HDFS (Hadoop) for raw archive   │
│    (e.g., HDFS path: /data/ecom/raw/events/)        │
│  – write cleaned/enriched data → MongoDB for “NoSQL”│
│    (fast lookup / dashboard queries)                │
└───────────┬─────────────────────────────────────────┘
            │
            ├─┐  
            │ │ (Batch ETL Spark job or spark SQL)
            │ │ reads from HDFS (/data/ecom/raw/) or MongoDB
            │ │ transforms and writes to PostgreSQL tables
            │ │ (e.g., analytics aggregates, fact tables)
            ▼ │
┌──────────────────────────────────────────────────┐
│  PostgreSQL Database                             │
│  – Tables: orders_fact, users_dim, products_dim, │
│    revenue_summary, etc.                         │
└─────────┬────────────────────────────────────────┘
          │
          │  (Power BI Desktop connects via JDBC/ODBC)
          ▼
┌───────────────────────────────────────────────────┐
│  Power BI Dashboard (on Windows)                  │
│  – Visualize KPIs: daily revenue, top products,   │
│    user acquisition trends, etc.                  │
└───────────────────────────────────────────────────┘
```

> **Notes on Storage Layers:**
>
> 1. **HDFS (raw archive):** Immutable, append-only storage for all incoming events. Enables re-processing/historical replays.
> 2. **MongoDB (NoSQL):** Schema-flexible store for “cleansed” JSON records to support quick lookups (e.g., look up a specific order by ID, find recent orders for dashboard queries).
> 3. **PostgreSQL (RDBMS):** Relational store structured into star schema / normalized tables. Ideal for BI queries by Power BI.

---

### 4.2. Step 1: Setup Prerequisites / Install Components

1. **Java 8+ installed** (Hadoop, Spark, Kafka require it).
2. **Hadoop** (pseudo-distributed or cluster):

   * HDFS NameNode + DataNode on localhost.
   * YARN ResourceManager + NodeManager on localhost.
3. **Kafka + Zookeeper** (single broker):

   * Download Apache Kafka (e.g., 3.x).
   * Start Zookeeper (`bin/zookeeper-server-start.sh config/zookeeper.properties`).
   * Start Kafka broker (`bin/kafka-server-start.sh config/server.properties`).
4. **Spark** (compatible version with Hadoop 3.x):

   * Prebuilt for Hadoop (e.g., spark-3.3.2-bin-hadoop3).
   * Place `spark-submit` & `spark-shell` in PATH.
5. **MongoDB** (Community Edition) running on default port 27017.
6. **PostgreSQL** (e.g., 13.x or 14.x) running on default port 5432. Create a database `ecom_analytics` and a user (e.g., `spark_user`).
7. **Scala** (2.12.x or 2.13.x) installed.
8. **Power BI Desktop** (on Windows) installed, able to connect to `jdbc:postgresql://<host>:5432/ecom_analytics`.

---

### 4.3. Step 2: Create a Scala Project Skeleton

You’ll create two main Scala sub-projects (using SBT or Maven):

1. **KafkaProducerApp (Scala)** – to poll the e-commerce Open API periodically and push raw events into Kafka.
2. **SparkStreamingApp (Scala)** – to read from Kafka, write raw to HDFS, write processed to MongoDB, and later run a batch job to push to PostgreSQL.

Below is an **SBT** directory layout suggestion:

```
demo-pipeline/
├── build.sbt
├── project/
│   └── build.properties
├── KafkaProducerApp/
│   ├── build.sbt
│   └── src/
│       └── main/scala/
│           └── com/demo/kafka/ProducerApp.scala
├── SparkStreamingApp/
│   ├── build.sbt
│   └── src/
│       └── main/scala/
│           ├── com/demo/spark/StreamingJob.scala
│           ├── com/demo/spark/HadoopToPostgresJob.scala
│           └── com/demo/spark/Utils.scala
└── README.md
```

#### 4.3.1. Top-Level `build.sbt`

```scala
ThisBuild / scalaVersion     := "2.12.15"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.demo"

lazy val commonSettings = Seq(
  organization := "com.demo",
  version      := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.15"
)

// Define two sub-projects
lazy val root = (project in file("."))
  .aggregate(KafkaProducerApp, SparkStreamingApp)
  .settings(commonSettings: _*)
  .settings(
    publish / skip := true
  )

lazy val KafkaProducerApp = (project in file("KafkaProducerApp"))
  .settings(commonSettings: _*)
  .settings(
    name := "KafkaProducerApp",
    libraryDependencies ++= Seq(
      "org.apache.kafka" %% "kafka"         % "2.8.1",
      "com.typesafe.play" %% "play-json"    % "2.9.2",   // for JSON parsing
      "com.softwaremill.sttp.client3" %% "core" % "3.3.15" // for HTTP calls
    )
  )

lazy val SparkStreamingApp = (project in file("SparkStreamingApp"))
  .settings(commonSettings: _*)
  .settings(
    name := "SparkStreamingApp",
    libraryDependencies ++= Seq(
      // Spark Core + SQL + Streaming
      "org.apache.spark" %% "spark-core"     % "3.3.2" % "provided",
      "org.apache.spark" %% "spark-sql"      % "3.3.2" % "provided",
      "org.apache.spark" %% "spark-streaming" % "3.3.2" % "provided",
      "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.3.2" % "provided",
      // MongoDB Spark Connector
      "org.mongodb.spark" %% "mongo-spark-connector" % "10.0.5",
      // PostgreSQL JDBC
      "org.postgresql" % "postgresql" % "42.5.0"
    )
  )
```

> **Note:** We mark Spark dependencies as `"provided"` because, in a real cluster, Spark’s JARs are already on the classpath. Locally, if you run `spark-submit`, you don’t want to re-ship all Spark internals.

---

### 4.4. Step 3: Kafka Producer (Scala) to Ingest from Open API

Create `ProducerApp.scala` under `KafkaProducerApp/src/main/scala/com/demo/kafka/`.

```scala
package com.demo.kafka

import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import sttp.client3._
import play.api.libs.json._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object ProducerApp {
  // Kafka bootstrap server(s)
  val brokers = "localhost:9092"
  val topic   = "ecommerce"

  // Example: Suppose the e-commerce Open API endpoint is:
  // https://api.example-ecom.com/v1/orders/recent?page=1&limit=100
  val apiBaseUrl = "https://api.example-ecom.com/v1"
  val ordersEndpoint = "/orders/recent"

  // JSON parser for API response
  case class Order(orderId: String, userId: String, amount: Double, timestamp: Long)
  implicit val orderReads: Reads[Order] = Json.reads[Order]

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", brokers)
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    // Make producer a bit safer:
    props.put("acks", "all")
    props.put("retries", "3")

    val producer = new KafkaProducer[String, String](props)
    val backend  = HttpURLConnectionBackend()

    // A simple loop: poll API every 30 seconds
    while (true) {
      try {
        val request = basicRequest
          .get(uri"$apiBaseUrl$ordersEndpoint?limit=50")
          .header("Accept", "application/json")

        val response = request.send(backend)

        response.body match {
          case Right(body) =>
            // Parse JSON array of orders
            Json.parse(body).validate[Seq[Order]] match {
              case JsSuccess(orderSeq, _) =>
                orderSeq.foreach { order =>
                  val jsonStr = Json.stringify(Json.toJson(order))
                  val record  = new ProducerRecord[String, String](topic, order.orderId, jsonStr)
                  producer.send(record)  // asynchronous send
                }
                println(s"[Producer] Sent ${orderSeq.length} orders to Kafka.")
              case JsError(errors) =>
                println(s"[Producer] Failed to parse JSON: $errors")
            }
          case Left(err) =>
            println(s"[Producer] API request failed: $err")
        }
      } catch {
        case ex: Exception => println(s"[Producer] Exception: ${ex.getMessage}")
      }

      // Sleep before next poll
      Thread.sleep(30000)  // 30 seconds
    }

    // producer.close()  // unreachable, since infinite loop
  }
}
```

#### Explanation:

* We use **STTP** (Scala HTTP client) to fetch JSON from the e-commerce API endpoint every 30 seconds.
* Each `Order` record is parsed using **Play JSON**.
* We send each order as a raw JSON string to Kafka topic `ecommerce`.
* The key is `orderId`, value is `jsonStr`.
* In production, you’d handle pagination, rate-limits, and robust error handling. For demo, we simply poll.

> **How to run:**
>
> 1. Build JAR:
>
>    ```bash
>    cd KafkaProducerApp
>    sbt assembly   # or sbt package (if you use sbt-assembly plugin)
>    ```
> 2. Run the JAR (remember to add any dependency JARs to classpath if not using assembly):
>
>    ```bash
>    java -cp "KafkaProducerApp/target/scala-2.12/KafkaProducerApp-assembly-0.1.0-SNAPSHOT.jar" \
>         com.demo.kafka.ProducerApp
>    ```
> 3. Confirm messages are arriving in Kafka:
>
>    ```bash
>    bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
>      --topic ecommerce --from-beginning
>    ```

---

### 4.5. Step 4: Spark Structured Streaming Job (Scala)

This job will:

1. **Consume** from Kafka topic `ecommerce`.
2. **Parse** JSON, apply any schema/enrichment.
3. **Write raw JSON** to HDFS for archival (every microbatch).
4. **Write processed data** (e.g., a DataFrame) to MongoDB for fast queries.

Create `StreamingJob.scala` under `SparkStreamingApp/src/main/scala/com/demo/spark/`.

```scala
package com.demo.spark

import org.apache.spark.sql.{SparkSession, DataFrame, Row}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.streaming.Trigger

object StreamingJob {

  def main(args: Array[String]): Unit = {
    // 1. Initialize SparkSession (with MongoDB connector)
    val spark = SparkSession.builder()
      .appName("EComStreamingJob")
      .master("local[*]")  // for demo; in cluster, use spark-submit --master yarn
      .config("spark.mongodb.output.uri", "mongodb://127.0.0.1:27017/ecom_db.orders")
      // (You can also set input URI if you want to read from Mongo later)
      .getOrCreate()

    import spark.implicits._

    // 2. Define schema for incoming JSON (must match API)
    val orderSchema = new StructType()
      .add("orderId", StringType)
      .add("userId", StringType)
      .add("amount", DoubleType)
      .add("timestamp", LongType)

    // 3. Read from Kafka
    val kafkaDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "ecommerce")
      .option("startingOffsets", "latest")   // for demo; production might use committed offsets
      .load()

    // 4. Convert binary "value" column to string
    val rawJsonDF = kafkaDF.selectExpr("CAST(value AS STRING) as json_str")

    // 5. Parse JSON
    val ordersDF = rawJsonDF
      .select(from_json($"json_str", orderSchema).as("order"))
      .select("order.*")
      .withColumn("event_time", ($"timestamp" / 1000).cast(TimestampType))  // convert epoch millis to timestamp
      .withColumn("order_date", to_date($"event_time"))

    // 6. Write raw JSON to HDFS for archival
    val rawQuery = rawJsonDF.writeStream
      .format("parquet")
      .option("path", "hdfs://localhost:9000/data/ecom/raw_json/")   // folder in HDFS
      .option("checkpointLocation", "hdfs://localhost:9000/checkpoints/raw_json/")
      .trigger(Trigger.ProcessingTime("30 seconds"))
      .outputMode("append")
      .start()

    // 7. Write structured data to MongoDB
    val mongoQuery = ordersDF.writeStream
      .format("mongo")
      .outputMode("append")
      .option("checkpointLocation", "/tmp/checkpoints/mongo_orders/")  // local filesystem for checkpoints
      .start()

    // 8. Keep job running
    spark.streams.awaitAnyTermination()
  }
}
```

#### Explanation:

* **`spark.readStream.format("kafka")`** reads from the `ecommerce` topic.
* We cast the Kafka `value: binary` to `STRING` as `json_str`.
* We parse JSON using `from_json()` into a DataFrame with schema `(orderId, userId, amount, timestamp)`.
* We add two additional columns:

  * `event_time`: a `TimestampType` for later time-based aggregations.
  * `order_date`: the date part, useful for partitioning.
* **Raw JSON archival:**

  * We write the raw JSON strings to HDFS as **Parquet**, so each microbatch writes a new set of Parquet files under `/data/ecom/raw_json/`, partitioned by default “date” (or no partition).
  * Checkpointing is required so Spark knows which Kafka offsets it has processed and to avoid re-reading old data.
* **MongoDB sink:**

  * We use the `mongo-spark-connector` to append parsed orders to a MongoDB collection `ecom_db.orders`.
  * Checkpoint directory is local (`/tmp/checkpoints/mongo_orders/`).

> **How to submit on a Hadoop/YARN cluster:**
>
> ```bash
> spark-submit \
>   --master yarn \
>   --deploy-mode cluster \
>   --class com.demo.spark.StreamingJob \
>   --packages org.mongodb.spark:mongo-spark-connector_2.12:10.0.5,\
>              org.apache.spark:spark-sql-kafka-0-10_2.12:3.3.2 \
>   SparkStreamingApp/target/scala-2.12/SparkStreamingApp-assembly-0.1.0-SNAPSHOT.jar
> ```

---

### 4.6. Step 5: Hadoop (HDFS) Usage

Because the Spark Structured Streaming job writes raw JSON → HDFS, you should see new Parquet files appearing every 30 seconds under HDFS path:

```
hdfs dfs -ls /data/ecom/raw_json/
```

You might see something like:

```
drwxr-xr-x   - user hadoop        0 2025-05-31 10:00 /data/ecom/raw_json/_spark_metadata
drwxr-xr-x   - user hadoop        0 2025-05-31 10:00 /data/ecom/raw_json/event_date=2025-05-31
...
```

To read back for a batch job, use standard Spark:

```scala
val rawArchiveDF = spark.read
  .parquet("hdfs://localhost:9000/data/ecom/raw_json/")
```

---

### 4.7. Step 6: Batch ETL from HDFS/MongoDB → PostgreSQL

Once you have a few minutes (or hours) of data, you want to periodically (say, daily or hourly) run a **batch Spark job** to:

1. **Read** cleansed data from **MongoDB** (or HDFS).
2. **Perform transformations/aggregations** to build your star schema tables (e.g., fact\_orders, dim\_users, dim\_products, daily\_revenue).
3. **Write** final tables into **PostgreSQL** via JDBC.

Create `HadoopToPostgresJob.scala` under `SparkStreamingApp/src/main/scala/com/demo/spark/`.

```scala
package com.demo.spark

import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._

object HadoopToPostgresJob {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("HadoopToPostgresJob")
      .master("local[*]")  // change to YARN in cluster mode
      .config("spark.mongodb.input.uri", "mongodb://127.0.0.1:27017/ecom_db.orders")
      .getOrCreate()

    import spark.implicits._

    // 1. Read from MongoDB (cleansed orders collection)
    val ordersDF = spark.read
      .format("mongo")
      .load()  // reads from the above URI

    // Alternatively, if you want to read from HDFS raw JSON:
    // val ordersDF = spark.read
    //   .schema(Utils.orderSchema)
    //   .json("hdfs://localhost:9000/data/ecom/raw_json/*.parquet")

    // 2. Create/Extract dimension tables and fact table

    // 2.1. Dimension: users_dim (unique users)
    val usersDimDF = ordersDF
      .select($"userId")
      .distinct()
      .withColumnRenamed("userId", "user_id")
      .withColumn("insert_ts", current_timestamp())

    // 2.2. Dimension: products_dim (for demo, assume each order has a productId field)
    // If you have a more complex schema (e.g., nested product details), adjust accordingly.
    val productsDimDF = ordersDF
      .select($"order".getField("productId").as("product_id"))  // adjust if your schema has productId
      .distinct()
      .withColumn("insert_ts", current_timestamp())

    // 2.3. Fact: orders_fact
    val ordersFactDF = ordersDF
      .select(
        $"orderId".as("order_id"),
        $"userId".as("user_id"),
        $"order".getField("productId").as("product_id"), // adjust as needed
        $"amount".as("order_amount"),
        ($"timestamp" / 1000).cast("timestamp").as("order_ts")
      )
      .withColumn("order_date", to_date($"order_ts"))
      .withColumn("insert_ts", current_timestamp())

    // 2.4. Daily revenue summary (aggregate)
    val dailyRevenueDF = ordersFactDF
      .groupBy($"order_date")
      .agg(
        count($"order_id").as("total_orders"),
        sum($"order_amount").as("total_revenue")
      )
      .withColumn("insert_ts", current_timestamp())

    // 3. Write each DataFrame to PostgreSQL

    // PostgreSQL connection properties
    val pgUrl  = "jdbc:postgresql://localhost:5432/ecom_analytics"
    val pgUser = "spark_user"
    val pgPwd  = "spark_password"

    def writeToPostgres(df: DataFrame, tableName: String): Unit = {
      df.write
        .format("jdbc")
        .option("url", pgUrl)
        .option("dbtable", tableName)
        .option("user", pgUser)
        .option("password", pgPwd)
        .option("driver", "org.postgresql.Driver")
        .mode("overwrite")  // or "append" depending on your needs
        .save()
      println(s"[Batch ETL] Written DataFrame to PostgreSQL table: $tableName")
    }

    writeToPostgres(usersDimDF, "dim_users")
    writeToPostgres(productsDimDF, "dim_products")
    writeToPostgres(ordersFactDF, "fact_orders")
    writeToPostgres(dailyRevenueDF, "daily_revenue")

    spark.stop()
  }
}
```

#### Explanation:

* **Read from MongoDB** via the Mongo Spark Connector (configured in `SparkSession`). You could also read directly from HDFS if you prefer.
* Build three dimension tables:

  * **`dim_users(user_id, insert_ts)`**
  * **`dim_products(product_id, insert_ts)`**
* Build a fact table **`fact_orders(order_id, user_id, product_id, order_amount, order_ts, order_date, insert_ts)`**.
* Build a pre-aggregated summary table **`daily_revenue(order_date, total_orders, total_revenue, insert_ts)`**.
* Write each to PostgreSQL via JDBC. For real pipelines, you might choose `mode("append")` and run incremental loads; for a demo, `mode("overwrite")` will “refresh” tables each run.

> **How to run:**
>
> 1. Package JAR:
>
>    ```bash
>    cd SparkStreamingApp
>    sbt assembly
>    ```
> 2. Submit to YARN or local:
>
>    ```bash
>    spark-submit \
>      --master yarn \
>      --deploy-mode cluster \
>      --packages org.mongodb.spark:mongo-spark-connector_2.12:10.0.5 \
>      --class com.demo.spark.HadoopToPostgresJob \
>      SparkStreamingApp/target/scala-2.12/SparkStreamingApp-assembly-0.1.0-SNAPSHOT.jar
>    ```
>
>    Or, for local testing, you can set `--master local[*]` instead of `yarn`.

---

### 4.8. Step 7: Verify Data in PostgreSQL & Connect Power BI

1. **Inspect tables in PostgreSQL**:

   ```sql
   \c ecom_analytics
   \dt
   select * from dim_users limit 10;
   select * from fact_orders limit 10;
   select * from daily_revenue order by order_date desc limit 5;
   ```

2. **In Power BI Desktop (Windows)**:

   * **Get Data → PostgreSQL Database**.

     * Server: `localhost` (or the IP of your PostgreSQL host).
     * Database: `ecom_analytics`.
     * Choose **Import** or **DirectQuery**.
   * **Authentication:** Enter `spark_user` / `spark_password`.
   * Once connected, you will see tables: `dim_users`, `dim_products`, `fact_orders`, `daily_revenue`.
   * Build visuals:

     * **Line chart** of `order_date` vs. `total_revenue` from `daily_revenue`.
     * **Bar chart** of top 10 products by sum(`order_amount`) (you might need to join `fact_orders` with `dim_products`).
     * **Card visuals** showing total orders, total revenue, active users (aggregate queries).
   * You can schedule daily data refresh (assuming you push new data daily), or manually refresh.

---

## 5. Putting It All Together: Execution Flow Summary

1. **Start Hadoop Services** (HDFS NameNode/DataNode, YARN ResourceManager/NodeManager).
2. **Start Kafka & Zookeeper** on your machine.
3. **Start MongoDB** (port 27017).
4. **Start PostgreSQL** (port 5432) and create database/user.
5. **Build & Launch KafkaProducerApp** (Scala) → polls e-commerce API and sends messages to Kafka topic `ecommerce`.
6. **Build & Launch SparkStreamingApp → StreamingJob** (Scala):

   * Reads from Kafka → writes raw JSON to HDFS → writes structured orders to MongoDB.
7. **At some cadence** (e.g., daily at midnight), launch **SparkStreamingApp → HadoopToPostgresJob** (Scala) to:

   * Read cleansed orders from MongoDB (or HDFS),
   * Build star schema tables (dim\_users, dim\_products, fact\_orders, daily\_revenue),
   * Write them into PostgreSQL via JDBC.
8. **Open Power BI Desktop** and connect to PostgreSQL to visualize the most up-to-date analytics.

---

## 6. Why Use Each Component?

1. **Kafka** → Decouples data ingestion from processing. The producer can fail/restart without losing data; streaming jobs can replay from Kafka offsets.
2. **Spark Structured Streaming** → Unified API for both streaming and micro-batch. We can write to multiple sinks (HDFS for raw archive, MongoDB for fast document queries).
3. **HDFS (Hadoop)** → Durable, scalable storage of raw JSON events. Even if downstream logic changes, you can re-process from HDFS anytime.
4. **MongoDB** → Schema-free, document-oriented store for “cleaned” order data (e.g., to quickly power a simple dashboard or enable ad-hoc lookups without waiting for relational ETL).
5. **PostgreSQL** → Relational star schema optimized for BI queries. Power BI can connect via JDBC/ODBC directly.
6. **Power BI** → Rich drag-and-drop visuals, scheduled refreshes, and KPI dashboards.
7. **Scala** → Native Spark language (fast, type-safe). Scala’s Kafka and MongoDB clients integrate well.

---

## 7. Sample Folder Structure Recap

```
demo-pipeline/
├── build.sbt
├── project/
│   └── build.properties
├── KafkaProducerApp/
│   ├── build.sbt
│   └── src/
│       └── main/scala/com/demo/kafka/ProducerApp.scala
├── SparkStreamingApp/
│   ├── build.sbt
│   └── src/
│       └── main/scala/com/demo/spark/
│           ├── StreamingJob.scala
│           ├── HadoopToPostgresJob.scala
│           └── Utils.scala       // (optional helper functions, e.g., shared schemas)
└── README.md
```

* **`Utils.scala`** (optional) could hold shared schemas:

  ```scala
  package com.demo.spark

  import org.apache.spark.sql.types._

  object Utils {
    val orderSchema: StructType = new StructType()
      .add("orderId", StringType)
      .add("userId", StringType)
      .add("productId", StringType)  // if present in JSON
      .add("amount", DoubleType)
      .add("timestamp", LongType)
  }
  ```

---

## 8. Tips for Running & Scaling

1. **Local vs. Cluster Mode:**

   * For simple testing, you can run Spark jobs in `master("local[*]")`.
   * For production, build with `provided` Spark dependencies and deploy via `spark-submit --master yarn`.

2. **Checkpoints & Fault Tolerance:**

   * Always specify `checkpointLocation` for streaming → ensures “exactly-once” semantics and offset tracking.
   * In production clusters, use a reliable, durable HDFS location for checkpoints (avoid local paths).

3. **Scaling Kafka:**

   * Increase Kafka partitions on topic `ecommerce` for parallelism (e.g., `kafka-topics.sh --alter --partitions 10 …`).
   * Spark’s structured streaming will create one task per Kafka partition.

4. **MongoDB Tuning:**

   * For high throughput, enable **Write Concern** tuning (e.g., `w=majority`, `journaled`), create indexes on fields you query often (e.g., `orderId`, `userId`).
   * If data volume grows, consider sharding collections.

5. **HDFS Partitioning:**

   * When writing raw JSON to HDFS, partition by `order_date` so downstream readers can prune partitions (e.g., `df.write.partitionBy("order_date").parquet(...)`).

6. **PostgreSQL Batch Partitioning:**

   * If daily data is large, consider loading into partitioned tables (e.g., partition `fact_orders` by `order_date`).
   * Use bulk copy (e.g., load Parquet→CSV→COPY) for large throughput, or let Spark’s JDBC “batchsize” optimization handle it.

7. **Power BI Scheduling:**

   * If your PostgreSQL is on a network‐accessible server, you can configure scheduled refresh in Power BI Service (not just desktop).
   * Use parameterized queries to filter large date ranges.

---

## 9. Example: Running Everything (Step-by-Step):

1. **Ensure all services are running:**

   ```bash
   # HDFS
   start-dfs.sh
   start-yarn.sh

   # Kafka (in another terminal)
   cd /opt/kafka
   bin/zookeeper-server-start.sh config/zookeeper.properties &
   bin/kafka-server-start.sh config/server.properties &

   # MongoDB
   sudo systemctl start mongod

   # PostgreSQL
   sudo systemctl start postgresql
   # Create DB & user:
   sudo -u postgres psql -c "CREATE DATABASE ecom_analytics;"
   sudo -u postgres psql -c "CREATE USER spark_user WITH PASSWORD 'spark_password';"
   sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE ecom_analytics TO spark_user;"
   ```

2. **Create Kafka topic (if not auto-created):**

   ```bash
   bin/kafka-topics.sh --create --topic ecommerce \
     --bootstrap-server localhost:9092 \
     --partitions 3 --replication-factor 1
   ```

3. **Build & start KafkaProducerApp** (in one terminal):

   ```bash
   cd demo-pipeline/KafkaProducerApp
   sbt assembly
   java -cp target/scala-2.12/KafkaProducerApp-assembly-0.1.0-SNAPSHOT.jar \
        com.demo.kafka.ProducerApp
   ```

4. **Build & start Spark Streaming Job** (in another terminal):

   ```bash
   cd demo-pipeline/SparkStreamingApp
   sbt assembly
   spark-submit \
     --master local[*] \
     --packages org.mongodb.spark:mongo-spark-connector_2.12:10.0.5,\
                org.apache.spark:spark-sql-kafka-0-10_2.12:3.3.2 \
     --class com.demo.spark.StreamingJob \
     target/scala-2.12/SparkStreamingApp-assembly-0.1.0-SNAPSHOT.jar
   ```

   * You should see logs like `[Producer] Sent X orders …` and Spark logs indicating microbatches writing to HDFS + MongoDB.

5. **Verify Data in HDFS & MongoDB:**

   ```bash
   hdfs dfs -ls /data/ecom/raw_json/
   mongo
   > use ecom_db
   > db.orders.find().limit(5).pretty()
   ```

6. **Run Batch ETL (HadoopToPostgresJob)**:

   ```bash
   spark-submit \
     --master local[*] \
     --packages org.mongodb.spark:mongo-spark-connector_2.12:10.0.5 \
     --class com.demo.spark.HadoopToPostgresJob \
     target/scala-2.12/SparkStreamingApp-assembly-0.1.0-SNAPSHOT.jar
   ```

   * Check in PostgreSQL if tables exist and have rows:

   ```bash
   sudo -u postgres psql -d ecom_analytics -c "SELECT * FROM daily_revenue LIMIT 5;"
   ```

7. **Open Power BI Desktop** (on Windows) → **Get Data → PostgreSQL** → provide credentials → select tables → build visual.

---

## 10. Conclusion & Next Steps

This demo project outlines a **fully integrated big-data pipeline** using:

* **Kafka** as the ingestion layer (for decoupling source from processors).
* **Spark Structured Streaming** (Scala) for real-time parsing + routing:

  * **Raw archival** to **HDFS** (Hadoop’s distributed store).
  * **Cleaned records** to **MongoDB** (NoSQL for low-latency reads).
* **Spark batch** (Scala) to transform and load into **PostgreSQL** (relational store for BI).
* **Power BI** for visual analytics on the relational data.

Feel free to customize:

* Add **Spark SQL transformations** (e.g., join with a `dim_products` table in Hive).
* Use **HBase** instead of MongoDB for larger, millions-row lookups.
* Swap **PostgreSQL** for a data warehouse like **Amazon Redshift** or **Snowflake**.
* Use **Airflow**/Oozie for orchestrating daily batch jobs.
* Replace **Play JSON** with **Circe** or **uPickle** for faster JSON parsing in Scala.
* Secure your clusters: enable SSL for Kafka, authentication for MongoDB/PostgreSQL, Kerberos for Hadoop.

This should give you a solid, end-to-end reference architecture and codebase to demonstrate how Hadoop, Spark, Kafka, MongoDB, PostgreSQL, Power BI, and Scala can all interoperate in a modern data pipeline.
