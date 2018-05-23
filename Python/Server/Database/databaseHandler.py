import sqlite3


def builder(x, y, msg=False):
    if x:
        return {'success': ''} if not msg else {'success': y}
    else:
        return {'error': y}


class Database:
    cursor = None

    def __init__(self, db_name):
        self.connection = sqlite3.connect(db_name)
        self.connection.isolation_level = None

    def transaction_request(self, epc, gtin14):
        cursor = self.connection.cursor()
        try:
            cursor.execute("BEGIN;")
            cursor.execute("SELECT UNIT FROM PRODUCTS WHERE GTIN14 = ?;", (gtin14,))
            stock = cursor.fetchall()[0][0]
            if stock is not None:
                cursor.execute("UPDATE PRODUCTS SET UNIT=? WHERE GTIN14 = ?;",
                           (stock - 1, gtin14))
            else:
                return {'success': False,
                        'payload': {'error': 'Product with gtin14: ' + gtin14 + 'does not exist. Administration has been '
                                                                                'notified about this.'}}
            cursor.execute("UPDATE PRODUCT SET READ=? WHERE EPC=?;", (1, epc))
            cursor.execute("SELECT PRICE, NAME FROM PRODUCTS WHERE GTIN14 = ?;", (gtin14,))
            results = cursor.fetchone()
            cursor.execute("COMMIT;")
        except (sqlite3.Error, sqlite3.DatabaseError, sqlite3.IntegrityError, sqlite3.ProgrammingError,
                sqlite3.Warning, Exception) as error:
            cursor.execute("ROLLBACK;")
            cursor.close()
            return {'success': False,
                    'payload': {'error': str(error)}}
        cursor.close()
        return {'success': True,
                'payload': {'name': results[1],
                            'price': results[0]}}

    def transaction_encode(self, gtin14, epcs, price, name, unit, serialNumber):
        cursor = self.connection.cursor()
        try:
            cursor.execute("SELECT UNIT FROM PRODUCTS WHERE GTIN14 = ?;", (gtin14,))
        except sqlite3.Error:
            pass
        stock = cursor.fetchone()
        cursor.close()
        if stock is None:
            # TODO CHECK READ AND NOT COUNT
            cursor = self.connection.cursor()
            try:
                cursor.execute("BEGIN;")
                cursor.execute("INSERT INTO PRODUCTS VALUES (NULL,?,?,?,?);", (gtin14, price, unit, name))
                for epc in epcs:
                    cursor.execute("INSERT INTO PRODUCT VALUES (?,?,?,?);",(epc, 0, gtin14, serialNumber))
                cursor.execute("COMMIT;")
            except (sqlite3.Error, sqlite3.DatabaseError, sqlite3.IntegrityError, sqlite3.ProgrammingError,
                    sqlite3.Warning) as error:
                cursor.execute("ROLLBACK;")
                cursor.close()
                return {'success': False,
                        'payload': {'error': str(error)}}
            cursor.close()
            return {'success': True}
        else:
            cursor = self.connection.cursor()
            try:
                cursor.execute("BEGIN;")
                cursor.execute("UPDATE PRODUCTS SET UNIT = ? WHERE GTIN14 = ?;", (int(stock[0]) + unit , gtin14))
                for epc in epcs:
                    cursor.execute("INSERT INTO PRODUCT VALUES (NULL,?,?,?,?);", (epc, 0, gtin14, serialNumber))
                cursor.execute("COMMIT;")
            except (sqlite3.Error, sqlite3.DatabaseError, sqlite3.IntegrityError, sqlite3.ProgrammingError,
                    sqlite3.Warning) as error:
                cursor.execute("ROLLBACK;")
                cursor.close()
                return {'success': False,
                        'payload': {'error': str(error)}}
            cursor.close()
            return {'success': True}
