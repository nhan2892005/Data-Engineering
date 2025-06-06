from kafka import KafkaProducer
import json
import datetime
import uuid
import time
from threading import Thread

# Kafka producer
producer = KafkaProducer(bootstrap_servers='localhost:9092')

# Đọc dữ liệu tọa độ của 3 xe buýt từ file JSON
with open('./locationDataGenerate/data/busLine1.json') as f1:
    json_data1 = json.load(f1)
    coordinates1 = json_data1['features'][0]['geometry']['coordinates']

with open('./locationDataGenerate/data/busLine2.json') as f2:
    json_data2 = json.load(f2)
    coordinates2 = json_data2['features'][0]['geometry']['coordinates']

with open('./locationDataGenerate/data/busLine3.json') as f3:
    json_data3 = json.load(f3)
    coordinates3 = json_data3['features'][0]['geometry']['coordinates']

def checkpoint(coordinate, busId):
    i = 0
    while True:
        # Tạo dữ liệu mới cho mỗi checkpoint
        data = {
            'busId': busId,
            'key': f"{busId}_{uuid.uuid4().hex}",
            'timestamp': str(datetime.datetime.now()),
            'latitude': coordinate[i][1],
            'longitude': coordinate[i][0]
        }
        message = json.dumps(data)
        print(message)
        # Gửi message tới Kafka topic 'busLine'
        producer.send('busLine', message.encode('utf-8'))
        # Di chuyển đến checkpoint tiếp theo, nếu hết thì quay lại đầu
        i = (i + 1) % len(coordinate)
        time.sleep(1)

# Tạo 3 thread cho 3 xe buýt với busId khác nhau
t1 = Thread(target=checkpoint, args=(coordinates1, '001'))
t2 = Thread(target=checkpoint, args=(coordinates2, '002'))
t3 = Thread(target=checkpoint, args=(coordinates3, '003'))

# Khởi chạy các thread
t1.start()
t2.start()
t3.start()

# Nếu muốn chương trình chạy mãi, có thể dùng join
t1.join()
t2.join()
t3.join()
