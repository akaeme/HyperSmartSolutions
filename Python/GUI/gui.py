import kivy,sys,base64, json
from termcolor import colored
sys.path.append('../')
kivy.require('1.10.0') # replace with your current kivy version !
from GUI.connection import Connection
import threading
from kivy.app import App
from kivy.uix.label import Label
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.stacklayout import StackLayout
from kivy.uix.gridlayout import GridLayout
from kivy.uix.scrollview import ScrollView
from kivy.uix.slider import Slider
from kivy.uix.image import Image
from functools import partial
from kivy.core.window import Window
from termcolor import colored
import netifaces as ni
from sys import platform
import qrcode
#from PIL import Image as img

if platform == "linux" or platform == "linux2":
    # linux
    interf = 'wlp5s0'
elif platform == "darwin":
    # OS X
    interf = 'en0'


ip = ni.ifaddresses(interf)[ni.AF_INET][0]['addr']

img = qrcode.make(ip)
img.save('./qr.png')

class MyApp(App):


    def BoxLayout(self):

        # top layout
        self.layout_top = BoxLayout(orientation='horizontal')
        self.next_button = Button(text='Next Client',on_press=lambda a: self.next_client())
        self.next_button.size_hint = (.2, 1)
        logo = Image(source='../../Marketing/simbolo_semfundo.png')
        logo.size_hint= (.6, 1)
        qr = Image(source='qr.png')
        qr.size_hint = (.2, 1)
        self.layout_top.add_widget(qr)
        self.layout_top.add_widget(logo)
        self.layout_top.add_widget(self.next_button)
        self.layout_top.size_hint = (1, 0.2)

        # center layout
        self.layout_center = StackLayout(orientation='lr-bt')
        self.scrlv = ScrollView(size_hint=(0.9, 0.95))
        self.s = Slider(min=0, max=1, value=25, orientation='vertical', step=0.01, size_hint=(0.1, 0.95))
        self.scrlv.bind(scroll_y=partial(self.slider_change, self.s))

        # what this does is, whenever the slider is dragged, it scrolls the previously added scrollview by the same amount the slider is dragged
        self.s.bind(value=partial(self.scroll_change, self.scrlv))

        self.layout_grid = GridLayout(cols=3, size_hint_y=None)

        self.layout_grid.bind(minimum_height=self.layout_grid.setter('height'))
        self.scrlv.add_widget(self.layout_grid)
        self.layout_center.add_widget(self.scrlv)
        self.layout_center.add_widget(self.s)

        # bottom layout
        self.layout_bottom = BoxLayout(orientation='horizontal')
        label_total = Label(text='Total:', font_size=30, color=[0, 0, 0, 1])
        label_total.size_hint = (.5, 1)

        self.value = Label(text="0€", font_size=30, color=[0, 0, 0, 1])
        self.value.size_hint = (.5, 1)

        self.layout_bottom.add_widget(label_total)
        self.layout_bottom.add_widget(self.value)
        self.layout_bottom.size_hint = (1, 0.1)

        # global layout
        layout_global = BoxLayout(orientation='vertical')
        layout_global.add_widget(self.layout_top)
        layout_global.add_widget(self.layout_center)
        layout_global.add_widget(self.layout_bottom)

        return layout_global

    def scroll_change(self, scrlv, instance, value):
        scrlv.scroll_y = value

    def slider_change(self, s, instance, value):
        if value >= 0:
            s.value = value

    def t1(self):
        self.gotBill = False
        msg = self.con.receive()
        #print(msg)
        try:
            if msg['src'] == 'GBD':
                #print(colored("got right src",'green'))
                for prod in msg['payload']['products']:
                    #print(colored("prod= "+str(prod), 'green'))
                    i = 0
                    for c in prod:
                        #print(colored("c= " + str(c), 'green'))
                        i += 1
                        if i == 1:
                            c = base64.b64decode(c).decode('utf-8')
                        lbl = Label(text=c, size_hint_y=None, height=80, valign='middle', font_size=27, color=[0, 0, 0, 1])
                        # lbl.text_size = (lbl.size)
                        if i % 3 == 0:
                            lbl.size_hint = (0.2, 1)

                        self.layout_grid.add_widget(lbl)

                self.value.text = msg['payload']['total'] + '€'

                self.con.send("ACK")
                self.Bill = msg
                self.gotBill = True
        except Exception as e:
            print(e)
            print(colored("no valid message found!", 'red'))
            self.con.send("ERROR")

        self.layout_top.remove_widget(self.padding)
        self.layout_top.add_widget(self.next_button)

    def next_client(self):
        self.layout_grid.clear_widgets()
        self.value.text = ''
        self.layout_top.remove_widget(self.next_button)
        self.padding = Label(text="Processing Bill", color=[0, 0, 0, 1])
        self.padding.size_hint = (.2, 1)
        self.layout_top.add_widget(self.padding)
        threading.Thread(target=self.t1).start()

    def handleApp(self):
        while True:
            msg = self.app.receive()
            print(msg)
            print(colored('Got new APP request', 'green'))
            while not self.gotBill:
                pass
            try:
                self.Bill['payload'].pop('bill')
            except:
                print("already cleared")

            self.Bill['src'] = 'GUI'
            print(self.Bill)
            self.app.send(self.Bill)

    def build(self):
        Window.clearcolor = (1, 1, 1, 1)

        self.con = Connection('0.0.0.0', '6667')
        self.app = Connection('0.0.0.0', '7778')
        self.con.connect()
        self.app.connect()
        threading.Thread(target=self.handleApp).start()
        self.gotBill = False
        self.Bill = None
        self.layout_grid = None
        self.next_button = None
        self.layout_top = None
        self.layout_bottom = None
        self.layout_center = None
        self.padding = None
        self.value = None
        self.scrlv = None
        self.s = None
        self.title = 'HyperSmartSolutions'
        return self.BoxLayout()




if __name__ == '__main__':
    MyApp().run()


