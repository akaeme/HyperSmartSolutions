import zmq
from termcolor import colored

def main():
    context = zmq.Context()
    #### GBD ####
    GBD_client = context.socket(zmq.ROUTER)
    GBD_server = context.socket(zmq.DEALER)
    GBD_client.bind("tcp://0.0.0.0:5559")
    GBD_server.bind("tcp://0.0.0.0:5560")
    #### OpenCV ####
    OCV_client = context.socket(zmq.ROUTER)
    OCV_server = context.socket(zmq.DEALER)
    OCV_client.bind("tcp://0.0.0.0:5561")
    OCV_server.bind("tcp://0.0.0.0:5562")
    #### GUI ####
    GUI_client = context.socket(zmq.ROUTER)
    GUI_server = context.socket(zmq.DEALER)
    GUI_client.bind("tcp://0.0.0.0:6666")
    GUI_server.bind("tcp://0.0.0.0:6667")
    #### APP ####
    APP_client = context.socket(zmq.ROUTER)
    APP_server = context.socket(zmq.DEALER)
    APP_client.bind("tcp://0.0.0.0:7777")
    APP_server.bind("tcp://0.0.0.0:7778")

    # Initialize poll set
    poller = zmq.Poller()
    #### GBD ####
    poller.register(GBD_client, zmq.POLLIN)
    poller.register(GBD_server, zmq.POLLIN)
    #### OpenCV ####
    poller.register(OCV_client, zmq.POLLIN)
    poller.register(OCV_server, zmq.POLLIN)
    #### GUI ####
    poller.register(GUI_client, zmq.POLLIN)
    poller.register(GUI_server, zmq.POLLIN)
    #### APP ####
    poller.register(APP_client, zmq.POLLIN)
    poller.register(APP_server, zmq.POLLIN)

    # Switch messages between sockets
    while True:
        socks = dict(poller.poll())

        #### GBD ####
        if socks.get(GBD_client) == zmq.POLLIN:
            message = GBD_client.recv_multipart()
            print(colored("client: "+str(message), 'green'))
            GBD_server.send_multipart(message)

        if socks.get(GBD_server) == zmq.POLLIN:
            message = GBD_server.recv_multipart()
            print(colored("server: "+str(message), 'red'))
            GBD_client.send_multipart(message)
        #### OpenCV ####
        if socks.get(OCV_client) == zmq.POLLIN:
            message = OCV_client.recv_multipart()
            OCV_server.send_multipart(message)

        if socks.get(OCV_server) == zmq.POLLIN:
            message = OCV_server.recv_multipart()
            OCV_client.send_multipart(message)

        #### GUI ####
        if socks.get(GUI_client) == zmq.POLLIN:
            message = GUI_client.recv_multipart()
            #print(colored("gui client: " + str(message), 'green'))
            GUI_server.send_multipart(message)

        if socks.get(GUI_server) == zmq.POLLIN:
            message = GUI_server.recv_multipart()
            #print(colored("gui server: " + str(message), 'red'))
            GUI_client.send_multipart(message)

        #### APP ####
        if socks.get(APP_client) == zmq.POLLIN:
            message = APP_client.recv_multipart()
            APP_server.send_multipart(message)

        if socks.get(APP_server) == zmq.POLLIN:
            message = APP_server.recv_multipart()
            APP_client.send_multipart(message)


if __name__ == "__main__":
    main()
