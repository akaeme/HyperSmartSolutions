from flask import Flask, render_template, url_for, request, redirect, jsonify
from wtforms import Form, StringField, PasswordField, validators
from flask_bcrypt import Bcrypt
from flask_sqlalchemy import SQLAlchemy
from flask_login import LoginManager, UserMixin, login_user, login_required, logout_user, current_user
from threading import Thread
from flask.ext.socketio import SocketIO, emit
import paho.mqtt.client as mqtt
import json
import logging
import coloredlogs
import time
import ast

logger = logging.getLogger('HyperSmartDashboard Logger')
ch = logging.StreamHandler()
ch.setLevel(logging.DEBUG)
formatter = logging.Formatter('%(asctime)s.%(msecs)03d - %(name)s - %(levelname)s - %(message)s', datefmt="%H:%M:%S")
ch.setFormatter(formatter)
logger.addHandler(ch)

coloredlogs.install(level='DEBUG', logger=logger, fmt='%(asctime)s.%(msecs)03d - %(name)s - %(levelname)s - '
                                                      '%(message)s', datefmt="%H:%M:%S")

app = Flask('HyperSmartDashboard')
app.logger.addHandler(ch)
app.threaded = True
bcrypt = Bcrypt(app)
socket_io = SocketIO(app)
mqtt_thread = None

TOPIC = 'dashboard'

app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///hypersmart_dashboard.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = True
app.config['SECRET_KEY'] = 'development'
db = SQLAlchemy(app)
login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'index'
# key gtin14 - value list of epcs
epcs = {}
# key gtin14 - value list of info
products = {}

class MQTT_Thread(Thread):
    def __init__(self):
        Thread.__init__(self)
        self.stop = False

    def run(self):
        while not self.stop and paho_client.loop() == 0:
            pass


def on_message(client, user_data, msg):
    print(str(msg.payload))
    message = json.loads(msg.payload)
    message = json.loads(message)
    if 'encodeRequests' not in list(message.keys()):
        logger.warning('Invalid Payload')
    else:
        requests = message['encodeRequests']
        for request_ in requests:
            name = request_['name']
            unit = request_['unit']
            price = request_['price']
            epcs_list = ast.literal_eval(request_['epc'])
            if request_['gtin14'] not in list(products.keys()):
                products[request_['gtin14']] = [name, price, unit, int(time.time())]
                epcs[request_['gtin14']] = epcs_list
            else:
                products[request_['gtin14']][2] += unit
                products[request_['gtin14']][3] = int(time.time())
                epcs[request_['gtin14']] += epcs_list
            logger.info(request_)
            request_['epc'] = epcs_list
            socket_io.emit('request', request_)

@socket_io.on('load')
def handleMessage(msg):
    if products == {}:
        return {'products': {},
                'epc': {},
                'unit': 0}
    unit = sum([x[2] for x in products.values()])
    tmp = dict(sorted(products.items(), key=lambda x: x[1][3], reverse=True)[:5])
    epc_tmp = {}
    for gtin14 in list(tmp.keys()):
        epc_tmp[gtin14] = epcs[gtin14][-1]
    return {'products': tmp,
            'epc': epc_tmp,
            'unit': unit}

@socket_io.on('control')
def handleMessage(msg):
    if products == {}:
        return {'products': {},
                'epc': {},
                'unit': 0}
    unit = sum([x[2] for x in products.values()])
    tmp = dict(sorted(products.items(), key=lambda x: x[1][3]))
    return {'products': tmp,
            'epc': epcs,
            'unit': unit}

@socket_io.on('retrieve_epcs')
def handleMessage(gtin14):
    return epcs[int(gtin14)]


@app.route('/')
def index():
    if current_user.is_authenticated:
        return redirect(url_for('overview'))
    register_form = UserRegisterForm(request.form, prefix="register-form")
    login_form = UserLoginForm(request.form, prefix="login-form")
    return render_template('index.html', register_form=register_form, login_form=login_form)

@app.route("/logout")
@login_required
def logout():
    logout_user()
    return redirect(url_for('index'))

@app.errorhandler(404)
def page_not_found(e):
    return render_template('404.html')

'''@app.context_processor
def override_url_for():
    return dict(url_for=dated_url_for)


def dated_url_for(endpoint, **values):
    if endpoint == 'static':
        filename = values.get('filename', None)
        if filename:
            file_path = os.path.join(app.root_path,
                                     endpoint, filename)
            values['q'] = int(os.stat(file_path).st_mtime)
    return url_for(endpoint, **values)'''
# The callback for when a PUBLISH message is received from the server.

@app.route('/overview/')
@login_required
def overview():
    return render_template("overview.html")


@app.route('/warehouse_control/')
@login_required
def warehouses():
    return render_template("warehouse_control.html")


@app.route('/login/', methods=['GET', 'POST'])
def login():
    username = request.json['username']
    password = request.json['password']
    form = UserLoginForm(prefix='login-form', username=username, password=password)
    validation = form.validate()
    if request.method == 'POST' and validation:
        results = User.query.filter_by(username=username).all()
        if len(results) > 0:
            user = results[0]
            if user.allowed:
                if bcrypt.check_password_hash(user.password, password):
                    login_user(user)
                    return jsonify(success='')
                else:
                    return jsonify(error={'password': 'Wrong password'})
            else:
                return jsonify(error={'authorization': 'False'})
        else:
            return jsonify(error={'username': 'Username does not exist'})


@app.route('/register/', methods=['GET', 'POST'])
def register():
    username = request.json['username']
    password = request.json['password']
    confirm = request.json['confirm']
    email = request.json['email']
    form = UserRegisterForm(prefix='register-form', username=username, email=email, password=password, confirm=confirm)
    validation = form.validate()
    if request.method == 'POST' and validation:
        username = form.username.data
        email = form.email.data
        password = bcrypt.generate_password_hash(form.password.data)
        results = User.query.filter_by(username=username).all()
        if len(results) > 0:
            error_dict = {'username': 'Username already taken'}
            return jsonify(error=error_dict)
        else:
            user = User(username=username, email=email, password=password, allowed=False)
            db.session.add(user)
            db.session.commit()
            return jsonify(success='')
    else:
        error_dict = {}
        error_keys = form.errors.keys()
        if 'username' in error_keys:
            error_dict['username'] = form.errors['username']
        if 'email' in error_keys:
            error_dict['email'] = form.errors['email']
        if 'password' in error_keys:
            error_dict['password'] = form.errors['password']
        return jsonify(error=error_dict)


class User(db.Model, UserMixin):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(25), unique=True)
    email = db.Column(db.String(50), unique=False)
    password = db.Column(db.String(150))
    allowed = db.Column(db.Boolean)

    def __init__(self, username, email, password, allowed):
        self.username = username
        self.email = email
        self.password = password
        self.allowed = allowed

    def __repr__(self):
        return '<User %r>' % self.username


@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))


class UserLoginForm(Form):
    username = StringField('Username', [validators.DataRequired(message='May not be null or empty'),
                                        validators.Length(min=4, max=25)],
                           render_kw={"placeholder": "username"})
    password = PasswordField('Password', [validators.DataRequired(message='May not be null or empty'),
                                          validators.Length(min=6, max=30)],
                             render_kw={"placeholder": "password"})


class UserRegisterForm(Form):
    username = StringField('Username', [validators.DataRequired(message='May not be null or empty'),
                                        validators.Length(min=4, max=25, message='Length must be between 4-25')],
                           render_kw={"placeholder": "username"})
    email = StringField('Email Address', [validators.Length(min=6, max=50, message='Invalid email')],
                        render_kw={"placeholder": "email"})
    password = PasswordField('New Password', [
        validators.DataRequired(),
        validators.EqualTo('confirm', message='Passwords must match')
    ], render_kw={"placeholder": "password"})
    confirm = PasswordField('Confirm Password', render_kw={"placeholder": "confirm password"})


paho_client = mqtt.Client(client_id='Dashboard')
paho_client.on_message = on_message
paho_client.connect(host='localhost', port=1883, keepalive=60)
paho_client.subscribe(TOPIC)

if __name__ == '__main__':
    mqtt_thread = MQTT_Thread()
    mqtt_thread.start()

    socket_io.run(app, host='0.0.0.0', port=8000, debug=True)
