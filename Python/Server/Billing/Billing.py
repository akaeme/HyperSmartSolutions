import base64


class Billing:
    def __init__(self, number):
        self.number = number
        self.products = {}
        self.count = 0
        self.total = 0
        self.header = ''
        f = open('Billing/header.txt', mode='r')
        self.header = f.read()
        f.close()
        self.bottom = ''
        f = open('Billing/bottom.txt', mode='r')
        self.bottom = f.read()
        f.close()
        self.body = ''
        self.body += self.header
        self.add = lambda name, qty, price: '   {:42}     {:>7}   \n'.format(name, str(price)) if (qty == 1) else \
            ('   {:57}\n'.format(name) + '   {:^42}     {:>7}   \n'.format(str(qty) + ' X ' + str(price),
                                                                           str(qty * price)))
        self.response_gui = None

    def add_product(self, gtin14, name, price):
        if gtin14 in list(self.products.keys()):
            self.products[gtin14][1] += 1
        else:
            self.products[gtin14] = [name, 1, price]
        self.count += 1
        self.total += price

    def process_billing(self):
        for key, value in self.products.items():
            name = value[0]
            qty = value[1]
            price = value[2]
            self.body += self.add(name, qty, price)
            self.products[key][0] = base64.b64encode(name.encode()).decode('ascii')
        self.body += '   {:-^54}   \n'.format('')
        self.body += '   {:42}     {:>7}   \n'.format('TOTAL .: ', str(self.total))
        self.body += self.bottom
        f = open('bill.txt', mode='w')
        f.write(self.body)
        f.close()
        self.response_gui = [list(v) for k,v in self.products.items()]
        for l in self.response_gui:
            price = l[2]
            count = l[1]
            l[1] = str(str(count) + 'x' + str(price))
            l[2] = str(price * count)
        response = {'total': str(round(self.total,2)),
                    'products': self.response_gui,
                    'count': self.count,
                    'bill': base64.b64encode(self.body.encode()).decode('ascii')}
        return response
