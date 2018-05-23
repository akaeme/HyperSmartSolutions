from Database import databaseHandler
from Billing import Billing
import zmq
import logging
import coloredlogs
import time
import json
import paho.mqtt.client as paho

logger = logging.getLogger('HyperSmart Logger')
ch = logging.StreamHandler()
ch.setLevel(logging.DEBUG)
formatter = logging.Formatter('%(asctime)s.%(msecs)03d - %(name)s - %(levelname)s - %(message)s', datefmt="%H:%M:%S")
ch.setFormatter(formatter)
logger.addHandler(ch)

coloredlogs.install(level='DEBUG', logger=logger, fmt='%(asctime)s.%(msecs)03d - %(name)s - %(levelname)s - '
                                                      '%(message)s', datefmt="%H:%M:%S")

HOST = '192.168.43.203'
PORT = '5560'
PORT_GUI = '6666'
DATABASE = 'database.db'
TOPIC = 'dashboard'

class Server:
    def __init__(self, host, port, port_gui):
        #  Prepare our context and sockets
        self.context = zmq.Context()
        self.ss = self.context.socket(zmq.REP)
        self.ss.connect("tcp://%s:%s" % (host, port))
        self.ss_gui = self.context.socket(zmq.REQ)
        self.ss_gui.connect("tcp://%s:%s" % (host, port_gui))
        self.database = databaseHandler.Database(DATABASE)
        self.paho_client = paho.Client()
        self.paho_client.connect('localhost', 1883)

    def stop(self):
        """ Stops the server closing the socket and destroying the context"""
        logger.warning('Stopping HyperSmart Server')
        try:
            self.ss.close()
            self.context.destroy()
        except zmq.ZMQError:
            logger.exception('Server.stop')

    def process_request(self, serial, epc, gtin14, billing):
        """ Request associated operations """
        # 1 query per product is computationally awful
        # transaction - mark as read, decrease product units, return product info
        msg = self.database.transaction_request(epc=epc, gtin14=gtin14)
        if msg['success']:
            # add to bill
            billing.add_product(gtin14=gtin14, name=msg['payload']['name'], price=msg['payload']['price'])
            return True, ''
        else:
            logger.warning('Error on Bill Transaction: ' + str(msg['payload']['error']))
            return False, {'type': 'Unacknowledged',
                    'payload': str(msg['payload']['error'])}

    def handle_requests(self, requests):
        """ Handle a group of requests """
        redirect = {'encodeRequest': self.encode_request,
                    'billRequest': self.bill_request,
                    }
        if not all(k in list(requests.keys()) for k in ('src', 'type', 'payload')):
            return {'error': ''}
        return redirect[requests['type']](json.loads(requests['payload']))

    def encode_request(self, payload):
        """ Process encode requests """
        # 0 - transaction errors, 1 - successfull transactions 
        # it contains the gtin14 of products
        status_dict = {0: [], 1: []}
        for req in payload['encodeRequests']:
            if not all(k in list(req.keys()) for k in ('serialNumber', 'epc', 'gtin14', 'price', 'name')):
                return {'error': ''}
            else:
                serial = req['serialNumber']
                epcs = json.loads(req['epc'])
                gtin14 = req['gtin14']
                price = req['price']
                name = req['name']
                unit = req['unit']
                msg = self.database.transaction_encode(gtin14=gtin14, epcs=epcs, price=price, name=name, unit=unit,
                                                       serialNumber=serial)
                if msg['success']:
                    logger.info('Encode Transaction processed successfully')
                    status_dict[1].append(gtin14)
                    
                else:
                    logger.info('Error on Encode Transaction: ' + str(msg['payload']['error']))
                    status_dict[0].append(gtin14)
                    
        if len(status_dict[1]) == len(payload['encodeRequests']):
            return {'type': 'Acknowledge'}
        else:
            return {'type': 'Unacknowledged',
                    'errors': status_dict[0],
                    'msg': 'Error in products \n'.join('{}: {}'.format(*k) for k in enumerate(status_dict[0]))}

    def bill_request(self, payload):
        """ Process billing for the bill request """
        billing = Billing.Billing(20)
        for req in payload['billRequests']:
            if not all(k in list(req.keys()) for k in ('serialNumber', 'epc', 'gtin14')):
                return {'error': ''}
            else:
                serial = req['serialNumber']
                epc = req['epc']
                gtin14 = req['gtin14']
                flag, content = self.process_request(serial, epc, gtin14, billing)
                if not flag:
                    return content
                else:
                    logger.info('Bill Transaction processed successfully')

        response = billing.process_billing()
        # response to client and response to gui
        return {'type':'acknowledge'}, {'src': 'GBD',
                'type': 'Acknowledge',
                'payload': response}

    def loop(self):
        while True:
            #  Wait for next request from client
            requests = self.ss.recv_json()
            logger.info('HANDLING message %r', repr(requests))

            response = self.handle_requests(requests)
            print(response)

            if isinstance(response, dict):
                if 'errors' in list(response.keys()):
                    logger.warning('Some errors found!')
                    success = [x for x in requests['payload']['encodeRequests'] if x['gtin14'] in response['errors']]
                    logger.info('Sending {}'.format(success.__str__()))
                    self.paho_client.publish(TOPIC, json.dumps(success))
                    del response['errors']
                    self.ss.send_json(response)
                else:
                    logger.info('Sending {}'.format(requests['payload'].__str__()))
                    self.paho_client.publish(TOPIC, json.dumps(requests['payload']))
                    self.ss.send_json(response)
                logger.info('StockManager response')
            else:
                logger.info('Send to GUI.')
                self.ss.send_json(response[0])
                self.ss_gui.send_json(response[1])
                resp = self.ss_gui.recv_json()
                print(resp)

if __name__ == '__main__':
    server = None
    while True:
        try:
            logger.info('Starting Hyper Smart Server')
            server = Server(HOST, PORT, PORT_GUI)
            server.loop()
        except KeyboardInterrupt:
            server.stop()
            try:
                logger.info('Press CTRL-C again within 2 sec to quit')
                time.sleep(2)
            except KeyboardInterrupt:
                logger.info('CTRL-C pressed twice: Quitting!')
                break
        logger.exception('Server ERROR')
