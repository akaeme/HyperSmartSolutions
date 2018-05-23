import zmq


class Connection:

    def __init__(self, ip, port):
        self.ip = ip
        self.port = port
        #  Prepare our context and sockets
        self.context = zmq.Context()
        self.socket = self.context.socket(zmq.REP)

    def connect(self):
        self.socket.connect("tcp://%s:%s" % (self.ip, self.port))

    def send(self, msg):
        self.socket.send_json(msg)

    def receive(self):
        return self.socket.recv_json()

    def stop(self):
        """ Stops the server closing the socket and destroying the context"""
        try:
            self.socket.close()
            self.context.destroy()
        except zmq.ZMQError:
            print('Server.stop!\n')