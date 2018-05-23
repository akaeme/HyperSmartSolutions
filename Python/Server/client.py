import zmq
import time

#  Prepare our context and sockets
context = zmq.Context()
socket = context.socket(zmq.REQ)
socket.connect("tcp://localhost:6666")
while True:
	data = {'src': 'GBD', 'type': '', 'payload': {'product': [['cuecas','9999x9999.99','9999.99'], ['xxx','9x99','99.99']], 'total': '30'}}
	socket.send_json(data)
	rep = socket.recv_json()

	print("sent: ", data)
	print("Received: ", rep)

