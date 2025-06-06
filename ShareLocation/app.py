from flask import Flask, render_template, Response
from kafka import KafkaClient, KafkaConsumer

def kafka_connect(topic):
    kafka = KafkaClient(bootstrap_servers='localhost:9092')
    return kafka.topics[topic]

def kafka_consumer(topic):
    print(topic)
    consumer = KafkaConsumer(
        topic,
        bootstrap_servers='localhost:9092',
        auto_offset_reset='earliest',  # Start from the beginning of the topic
        enable_auto_commit=True,
        value_deserializer=lambda x: x.decode('utf-8')  # Adjust according to your message format)
    )
    return consumer

app = Flask(__name__)

@app.route('/')
def maps():
    return render_template('index.html')

@app.route('/topic/<topicname>')
def get_messages(topicname):
    print(topicname)
    consumer = kafka_consumer(topicname)
    def events():
        for message in consumer:
            yield 'data: {0}\n\n'.format(message.value)
            # print(message.value.decode())

    return Response(events(), mimetype="text/event-stream")

if __name__ == '__main__':
    app.run(debug=True, host='localhost', port=5000)