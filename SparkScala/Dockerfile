# 1. Dùng base image Bitnami Spark (đã kèm Spark runtime)
FROM bitnami/spark:latest

# 2. Thiết lập thư mục làm việc
WORKDIR /app

# 3. Copy fat JAR vào container
#    Lưu ý: đường dẫn phía trái phải đúng với file jar bạn build được
COPY introduction/target/scala-2.12/SparkApp-assembly-0.1.jar /app/SparkApp.jar
COPY data/ /app/data/

# 4. (Tùy chọn) Nếu cần file cấu hình spark-defaults.conf, log4j.properties… có thể copy thêm
# COPY conf/spark-defaults.conf /opt/bitnami/spark/conf/spark-defaults.conf

# 5. Khi container chạy sẽ tự động gọi spark-submit
#    - --master sẽ được truyền khi docker-compose khởi nếu muốn connect đến cluster
#    - Ở đây đặt master mặc định là local[*] (debug local). 
#      Khi dùng YARN/cluste, override lại trong docker-compose hoặc command
CMD [ "/opt/bitnami/spark/bin/spark-submit", "--class", "com.intro.Main", "--master", "local[*]", "/app/SparkApp.jar" ]
