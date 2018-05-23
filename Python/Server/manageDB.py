import sqlite3
import sys
import argparse

class Database:
    cursor = None

    def __init__(self, db_name):
        self.connection = sqlite3.connect(db_name)

    def insert_product(self, epc, read, barcode, serial):
        try:
            self.cursor = self.connection.execute("SELECT barcode FROM PRODUCTS WHERE "
                                                  "BARCODE = ?;", (barcode,))
        except sqlite3.Error:
            pass
        results = self.cursor.fetchall()
        if len(results) > 0:
            self.connection.execute("INSERT INTO PRODUCT VALUES (NULL,?,?,?,?);", (epc, read, barcode, serial)
                                    )
            self.connection.commit()
        else:
            print('There are no products with the barcode ', barcode)
            print('Invalid operation! Closing....')
            exit()

    def insert_products(self, barcode, price, unit, name):
        self.connection.execute("INSERT INTO PRODUCTS VALUES (NULL,?,?,?,?);", (barcode, price, unit, name)
                                )
        self.connection.commit()

    def delete_product(self):
        try:
            self.cursor = self.connection.execute("SELECT * FROM PRODUCT;")
        except sqlite3.Error:
            pass
        results = self.cursor.fetchall()
        for i in range(len(results)):
            print('#{:2} - epc({:25}) barcode({:15})'.format(i, results[i][1], results[i][3]))
        delete = int(input('Select a entry to delete: '))
        self.connection.execute("DELETE FROM PRODUCT WHERE ID=?;", (str(results[delete][0])))
        self.connection.commit()
        print('Product Deleted!')

    def delete_products(self):
        try:
            self.cursor = self.connection.execute("SELECT * FROM PRODUCTS;")
        except sqlite3.Error:
            pass
        results = self.cursor.fetchall()
        for i in range(len(results)):
            print('#{:2} - barcode({:15}) name({:15})'.format(i, results[i][1], results[i][4]))
        delete = int(input('Select a entry to delete: '))
        print(str(results[delete][1]))
        print(type(str(results[delete][1])))
        self.connection.execute("DELETE FROM PRODUCT WHERE BARCODE = ?;", (str(results[delete][1]),))
        self.connection.commit()
        self.connection.execute("DELETE FROM PRODUCTS WHERE ID=?;", (str(results[delete][0]),))
        self.connection.commit()
        print('Products Deleted!')

if __name__ == '__main__':

    parser = argparse.ArgumentParser(
        description='Simple script to add entries on db. Add products entity and then product.')
    parser.add_argument('-products', action="store_true", dest='products', help='To add Products(Barcode, Price, Unit, Name)')
    parser.add_argument('-product', action="store_true", dest='product', help='To add Product(Epc, Read, Barcode, SerialNumber)')
    parser.add_argument('-dproduct', action="store_true", dest='dproduct', help='To del Product')
    parser.add_argument('-dproducts', action="store_true", dest='dproducts', help='To del Products')

    args = parser.parse_args()

    if len(sys.argv) == 1:
        parser.print_help()
        sys.exit(1)

    args = vars(args)
    db = Database('database.db')
    if args['products']:
        barcode = input('Barcode: ')
        price = input('Price: ')
        unit = input('Unit: ')
        name = input('Name: ')
        db.insert_products(barcode, price, unit, name)
        print('Products added successfully')
    if args['product']:
        epc = input('Epc: ')
        read = 0
        barcode = input('Barcode: ')
        serial = input('Serial: ')
        db.insert_product(epc, read, barcode, serial)
        print('Product added successfully')
    if args['dproduct']:
        db.delete_product()
    if args['dproducts']:
        db.delete_products()