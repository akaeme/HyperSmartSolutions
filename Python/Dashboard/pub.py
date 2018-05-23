import json
import paho.mqtt.client as paho
import time
import socket


def on_publish(client, userdata, mid):
	print("mid: " + str(mid))


paho_client = paho.Client()
paho_client.on_publish = on_publish
paho_client.connect('localhost', 1883)
#client.loop_start()
value = {'payload': {'encodeRequests': [{'gtin14': '50605566000126',
										'unit': 4,
										'serialNumber': '10000005',
										'price': 0.25,
										'name': 'Penacova Água mineral 1.5L',
										'epc': ['30B424F5F9E84B0000989682', '30B424F5F9E84B0000989684', '30B424F5F9E84B0000989681', '30B424F5F9E84B0000989683']
										}]
					}
		}

for x in range(0, 1):
	paho_client.loop_start()
	paho_client.publish('dashboard', json.dumps(value))
	paho_client.loop_stop()
	time.sleep(2)