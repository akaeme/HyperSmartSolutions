/*
  controlo servo 1
  controlo servo 2
  controlo motor tapete 
*/
#include <Servo.h>
Servo myservo;

//Parametros dos servos
const int ServoPin1 = 9;
const int ServoPin2 = 10; 
const int Motor = 11;
const int maxAngle = 90;
const int minAngle = 5;
const int Cal1 = 55;
const int Cal2 = 65;

//Parametros do motor
const int VelArranque = 50;
const int VelCruzeiro = 28;

//int sensorValue = 0;        // value read from the pot
//int outputValue = 0;        // value output to the PWM (analog out)

void setup() 
{
	  // initialize serial communications at 9600 bps:
	  Serial.begin(9600);
	  pinMode(ServoPin1,OUTPUT);
	  pinMode(ServoPin2,OUTPUT);
	  pinMode(Motor,OUTPUT);
}

void loop() 
{

	  // Motor em funcionamento
	  analogWrite(Motor, VelArranque);   //Velocidade de arranque
	  delay(320);  
	  analogWrite(Motor, VelCruzeiro);   //Velocidade de cruzeiro

	  int i = minAngle;
	  int flag = 0;
	  
	  while(i < maxAngle + 1)
	  {
			moveServo(ServoPin1, i + Cal1);
			//delay(20);
			
			moveServo(ServoPin2,maxAngle-i+ Cal2);
			//delay(20);
			
			if(i == maxAngle )
				flag = 1;
			if(i == minAngle)
				flag = 0;

			if(flag == 0)
				i = i + 1;
			else
				i = i-1;
			  
			//Serial.println(i);
	  }
}

void moveServo(int SERVO_PIN, int PWM){
        myservo.attach(SERVO_PIN);
        //delay(5);
        myservo.write(PWM);
        delay(45);
        myservo.detach();
        //delay(5);
}
