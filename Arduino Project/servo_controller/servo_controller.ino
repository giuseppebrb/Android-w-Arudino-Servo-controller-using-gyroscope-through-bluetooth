#include <Servo.h>
#include <SoftwareSerial.h>

Servo myServo;
SoftwareSerial mySerial(6, 5);

String value;
int angle;
int prevValue = 0;

char command;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(57600);
  Serial.println("Set up complete");
  mySerial.begin(9600);
  myServo.attach(9);
}

void loop() {
  // put your main code here, to run repeatedly:
  // if there's a new command reset the string
  if(mySerial.available()){
    value = "";
    }

    // Construct the command string fetching the bytes, sent by Android, one by one.
    while(mySerial.available()){
      command = ((byte)mySerial.read());
      
      if(command == ':') {
        break;
      } else { 
        value += command;
      }
      delay(1);
    }
    
    int casted = value.toInt();
    if(prevValue != casted){
      Serial.println(casted);
      angle = map(casted, -75, 85, 0, 179); // In case, re-map values as you wish
      myServo.write(angle);
      prevValue = casted;
    } else {
        prevValue = casted;
    }
    
    delay(1);
}
