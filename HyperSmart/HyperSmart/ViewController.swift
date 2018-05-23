//
//  ViewController.swift
//  HyperSmart
//
//  Created by Nelson Reverendo on 08/11/2017.
//  Copyright © 2017 Nelson Reverendo. All rights reserved.
//

import UIKit
import SwiftyZeroMQ
import CoreNFC

class ViewController: UIViewController {

    var context :SwiftyZeroMQ.Context!
    var client : SwiftyZeroMQ.Socket!
    var ip:String = "127.0.0.1"
    var connected:Bool = false
    @IBOutlet weak var text: UITextView!
    @IBOutlet weak var total: UILabel!
    @IBOutlet weak var getBill: UIBarButtonItem!
    @IBOutlet weak var waiting: UIActivityIndicatorView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(ip_changed),
                                               name:Notification.Name("IP"),
                                               object: nil)//register for notification
        
        //waiting.isHidden = true
        waiting.stopAnimating()
        do{
           
            context = try SwiftyZeroMQ.Context()
            client    = try context.socket(.request)
            //try client.connect("tcp://"+ip+":7777")
            //try requestData()
            
        }catch{print(error)}
        // Do any additional setup after loading the view, typically from a nib.
    }
    
    @objc func ip_changed(notification:Notification) -> Void{
        //print("Must change IP")
        guard let userInfo = notification.userInfo,
            let ip  = userInfo["ip"] as? String
            else {
                print("No userInfo found in notification")
                return
        }
        print("ip = \(ip)")
        if(self.connected){
            do{
                //send disconnect
                self.connected = false
                try client.close()
                client = try context.socket(.request)
                //self.connected = true
            }catch{
                print(error)
            }
        }
        do{
            try client.connect("tcp://"+ip+":7777")
            DispatchQueue.main.async{
                self.text.text = "Processing Request..."
                self.total.text = ""
                self.getBill.isEnabled = false
                self.waiting.isHidden = false
                self.waiting.startAnimating()
                
            }
            DispatchQueue.global(qos: .background).async{
                self.requestData()
            }
            //self.connected = true
        }catch{
            print(error)
        }
        
    }
    
    @IBAction func getBillPressed(_ sender: Any) {
        DispatchQueue.global(qos: .background).async{
            if(self.connected){
                DispatchQueue.main.async{
                    self.text.text = "Processing Request..."
                    self.total.text = ""
                    self.getBill.isEnabled = false
                    self.waiting.isHidden = false
                    self.waiting.startAnimating()
                    
                }
                self.requestData()
            }else{
                let alert = UIAlertController(title: "Connection Error", message: "Please read the QR code to connect", preferredStyle: UIAlertControllerStyle.alert)
                alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.default, handler: nil))
                self.present(alert, animated: true, completion: nil)
            }
        }
        
    }
    
    func requestData() -> Void {
        do{
            let json: [String:Any] = ["type":"Bill"]
            
            let data = try? JSONSerialization.data(withJSONObject: json, options: .prettyPrinted)
            //let msg = String(data: data!, encoding: .utf8)
            try client.send(data: data!)
            //try client.send(string: "Bill")
            let rep = try client.recv()
            self.connected = true
            var js :[String:Any]!
            js = self.convertToDictionary(text: rep!)
            let src = js["src"] as! String
            //print(js)
            if(src == "GUI"){
                
                let payload = js["payload"] as! [String:Any]
                let total = payload["total"] as! String
                DispatchQueue.main.async{
                    self.total.text = String(total)
                    
                }
                let products = payload["products"] as! [[String]]
                DispatchQueue.main.async{
                    self.text.text = ""
                
                    for p in products{
                        var title = p[0]
                        let howMany = p[1]
                        let parcTotal = p[2]
                        
                        let data = NSData(base64Encoded: title)
                        // Convert back to a string
                        let t = NSString(data: data as! Data, encoding: String.Encoding.utf8.rawValue) as! String
                        
                        //print(t.padding(toLength: 20, withPad: "-", startingAt: 0))
                        //print(howMany)
                        //print(parcTotal)
                        //print(parcTotal.padding(toLength: 20, withPad: "-", startingAt: 0))
                        
                        var T : String = self.text.text
                        
                        
                        self.text.text = T + "\n\n" + String(format:"%@%@%@", t.padding(toLength: 30, withPad: " ", startingAt: 0),howMany.padding(toLength: 30, withPad: " ", startingAt: 0),parcTotal.leftPadding(toLength:10 , withPad: " "))
                    }
                    self.getBill.isEnabled = true
                    self.waiting.stopAnimating()
                }
                
                
            }
            else{
                print("invalid src")
            }
            //text.text = rep
            
        }catch{print(error)}
        
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func convertToDictionary(text: String) -> [String: Any]? {
        if let data = text.data(using: .utf8) {
            do {
                return try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
            } catch {
                print(error.localizedDescription)
            }
        }
        return nil
    }


}
extension String {
    func leftPadding(toLength: Int, withPad character: Character) -> String {
        let newLength = self.characters.count
        if newLength < toLength {
            return String(repeatElement(character, count: toLength - newLength)) + self
        } else {
            return self.substring(from: index(self.startIndex, offsetBy: newLength - toLength))
        }
    }
}

