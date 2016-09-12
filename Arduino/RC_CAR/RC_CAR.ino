/* INCLUDE */
#include <RF24Network.h>
#include <RF24.h>
#include <SPI.h>
#include <IRremote.h>
#include <Servo.h>
#include <EEPROM.h>

/* VARIABLE */
IRsend irsend;
IRrecv irrecv(4);
decode_results results;
Servo servoFB, servoLR;

RF24 radio(7, 8);
RF24Network network(radio);

struct payload_controller {
  int positionX;
  int positionY;
  int positionA;
  int positionB;
};

int node_address, node_controller, node_channel, to_node;

String my_node;

String configOption[] = {
  "\rCar id (",
  "\rChannel (",
  "\rY is reverse (",
  "\rY forward speed step (",
  "\rY backward speed step (",
  "\rX is reverse (",
  "\rY use servo (",
  "\rX use servo (",
  "\rY servo pin (",
  "\rY analog pin A (",
  "\rY analog pin B (",
  "\rX servo pin (",
  "\rX analog pin A (",
  "\rX analog pin B ("
};

bool FBservo, LRservo;

int forwardPin, backwardPin, leftPin, rightPin;

int servoFBpin, servoLRpin;

int X_before, Y_before, A_before, B_before;

int YstepF, YstepR;

long RaceTypeTimer = 0; int raceType = 0;

long powerTimer = 0; bool powerState = 0;

long shootTimer = 0; long rechargeShootTimer = 0; bool shootState = 0, shoot = 0, shootON = 1;

long turboTimer = 0; long rechargeTurboTimer = 0; bool turboState = 0, turbo = 0, turboON = 1;

long hasBeenShootTimer = 0; bool hasBeenShoot = 0;

bool reverseY, reverseX, stopY = 0, slowdown = 0;

long previousMillis = 0;
long irPositionTimer = 0;

int lastGate = 0;

/* SETUP */
void setup(void) {
  Serial.begin(9600);
  readEEPROM();
  if (FBservo) servoFB.attach(servoFBpin,1000,2000);
	else {
		pinMode(forwardPin, OUTPUT); pinMode(backwardPin, OUTPUT); digitalWrite(forwardPin, LOW); digitalWrite(backwardPin, LOW);
	}
	if (LRservo) servoLR.attach(servoLRpin,1000,2000);
  else { 
		pinMode(leftPin, OUTPUT); pinMode(rightPin, OUTPUT); digitalWrite(leftPin, LOW); digitalWrite(rightPin, LOW);
	}
  pinMode(A0, OUTPUT);  pinMode(A1, OUTPUT);  pinMode(A2, OUTPUT);
  digitalWrite(A0, HIGH); digitalWrite(A1, LOW); digitalWrite(A2, LOW);
  SPI.begin();
  radio.begin();
  network.begin(node_channel, node_address);
  irrecv.enableIRIn();
  Serial.println("\r\rPress 'C' to load truck configuration menu.\r\r");
} // end setup()

  /* MAIN LOOP */
void loop(void) {
  if (Serial.read() == 'C') {
    writeEEPROM();
    readEEPROM();
    currentEEPROM();
    setup();
  }
  network.update();
  nRF_receive();
  if (millis() - previousMillis >= 1000) {
    handle(1, 8, 1, 1);
    previousMillis = millis();
  }
  if (raceType > 1) checkDamageState();
  if (raceType > 1) checkShootState();
  if (raceType > 0) checkTurboState();
  if (raceType > 0) checkIRreceiverState();
  if (raceType < 1) getRaceType();
  if (millis() - irPositionTimer >= 500) {
    irsend.sendSony(node_address + 20, 12);
    irrecv.enableIRIn();
    irPositionTimer = millis();
  }
} // end loop()

  /* FUNCTION */
void nRF_receive(void) {
  if (network.available()) {
    RF24NetworkHeader header;
    network.peek(header);
    if (header.from_node == 0) {
      char RecvPayload[31] = "";
      network.read(header, &RecvPayload, sizeof(RecvPayload));
      nRF_fromHost(RecvPayload[0], RecvPayload[1]);
    } else if (header.from_node == node_controller) {   
      payload_controller payload;
      network.read(header, &payload, sizeof(payload));
      handle(payload.positionX, payload.positionY, payload.positionA, payload.positionB);
    }
  }
} // end nRF_receive()

void nRF_send(String msg) {
  char SendPayload[31] = "";
  for (int i = 0; i<msg.length(); i++) SendPayload[i] = msg[i];
  RF24NetworkHeader header(0);
  network.write(header, &SendPayload, strlen(SendPayload));
} // end nRF_send()

void handle(int X, int Y, int A, int B) {
  // A
  if (A != A_before) {
    if (!A && shootON) {
      digitalWrite(A1, HIGH);
      for (int i = 0; i < 4; i++) {
        irsend.sendSony(node_address + 10, 12);
        delay(40);
      }
      shootTimer = millis();
      rechargeShootTimer = shootTimer;
      shoot = true;
      shootON = false;
      String msg = "";
      msg += node_address;
      msg += 'S';
      msg += 0;
      nRF_send(msg);
      irrecv.enableIRIn();
    }
    A_before = A;
  }
  // B
  if (B != B_before) {
    if (!B && turboON) {
      digitalWrite(A2, HIGH);
      turboTimer = millis();
      rechargeTurboTimer = turboTimer;
      turbo = true;
      turboON = false;
      String msg = "";
      msg += node_address;
      msg += 'T';
      msg += 0;
      nRF_send(msg);
    }
    B_before = B;
  }
  // TURN
  if (X != X_before) {
    int Xval = X;
    if (reverseX) X = 2 - X;
    if (X == 2) {
			if (LRservo) servoLR.writeMicroseconds(1200);
			else {
				digitalWrite(leftPin, LOW);
				digitalWrite(rightPin, HIGH);
			}
    } else if (X == 0) {
			if (LRservo) servoLR.writeMicroseconds(1800);
			else {
				digitalWrite(rightPin, LOW);
				digitalWrite(leftPin, HIGH);
			}
    } else {
			if (LRservo) servoLR.writeMicroseconds(1500);
			else {
				digitalWrite(leftPin, LOW);
				digitalWrite(rightPin, LOW);
			}
    }
    X_before = Xval;
  }
  // MOVE
  if (Y != Y_before) {
    int step = 0;
    if (reverseY) Y = 15 - Y;
    if (!turbo) step -= 4;
    if (slowdown) step -= 4;
    if (Y < 7 && !stopY) {
      step += YstepF;
      if (FBservo) {
				int Yval = 1500 - ((7-Y) * step);
				servoFB.writeMicroseconds(Yval);
			} else {
				int Yval = 3 + ((7-Y) * step);
				digitalWrite(backwardPin, LOW);
				analogWrite(forwardPin, Yval);
			}
    } else if (Y > 8 && !stopY) {
      step += YstepR;
      if (FBservo) {
        int Yval = 1500 + ((Y-8) * step);
        servoFB.writeMicroseconds(Yval);
      }	else {
        int Yval = 3 + ((Y-8) * step);
				digitalWrite(forwardPin, LOW);
				analogWrite(backwardPin, Yval);
			}
    } else {
			if (FBservo) servoFB.writeMicroseconds(1500);
			else {
				digitalWrite(forwardPin, LOW);
				digitalWrite(backwardPin, LOW);
			}
    }
    Y_before = Y;
  }  
  previousMillis = millis();
}

void nRF_fromHost(char command, char command2) {
  bool extra;
  int c2 = 0;
  if (command2 == 1 || command2 == 49) extra = true;
  else if (command2 == 0 || command2 == 48) extra = false;
  if (command2 > 47) c2 = command2 - 48;
  else c2 = command2;
  
  switch (command) {
  case 'C':
    raceType = c2;
    resetStats();
    break;
  case 'X':
    reverseX = extra;
    break;
  case 'Y':
    reverseY = extra;
    break;
  case 'T':
    turboON = extra;
    break;
  case 'S':
    slowdown = extra;
    break;
  case 'A':
    shoot = extra;
    break;
  case 'R':
    stopY = extra;
    break;
  case 'G':
    lastGate = c2;
    break;
  }
} // end nRF_fromHost()

void resetStats() {
  powerTimer = 0;
  powerState = 0;
  shootTimer = 0;
  shootState = 0;
  turboTimer = 0;
  turboState = 0;
  reverseY = false;
  reverseX = false;
  stopY = false;
  slowdown = false;
  hasBeenShoot = false;
  hasBeenShootTimer = 0;
  powerTimer = 0;
  shoot = false;
  if (raceType == 1) shootON = false;
  else shootON = true;
  shootTimer = 0;
  rechargeShootTimer = 0;
  turbo = false;
  turboON = true;
  turboTimer = 0;
  rechargeTurboTimer = 0;
  lastGate = 0;
  digitalWrite(A0, HIGH);
  digitalWrite(A1, LOW);
  digitalWrite(A2, LOW);
} // end resetStats()

void checkDamageState() {
  if (hasBeenShoot) {
    if (!stopY) stopY = true;
    if (hasBeenShootTimer == 0) {
      hasBeenShootTimer = millis();
      powerTimer = hasBeenShootTimer;
      powerState = 0;
      digitalWrite(A0, powerState);
    }
    else if ((millis() - hasBeenShootTimer) > 2000) {
      hasBeenShoot = false;
      stopY = false;
      hasBeenShootTimer = 0;
      powerTimer = 0;
      powerState = 1;
      digitalWrite(A0, powerState);
    }
    else if ((millis() - powerTimer) > 100) {
      powerState = !powerState;
      powerTimer = millis();
      digitalWrite(A0, powerState);
    }
  }
}

void checkShootState() {
  if (shootTimer == 0) shootTimer = millis();
  if (shootON) {
    if ((millis() - shootTimer) > 100) {
      shootState = !shootState;
      shootTimer = millis();
      digitalWrite(A1, shootState);
    }
  } else if (shoot) {
    if ((millis() - shootTimer) > 1500) {
      shoot = false;
      shootState = 0;
      shootTimer = millis();
      digitalWrite(A1, shootState);
    }
  } else {
    if ((millis() - shootTimer) > 1500) {
      shootON = true;
      shootState = 1;
      shootTimer = millis();
      digitalWrite(A1, shootState);
      String msg = "";
      msg += node_address;
      msg += 'S';
      msg += 1;
      nRF_send(msg);
    }
  }
}

void checkTurboState() {
  if (turboTimer == 0) turboTimer = millis();
  if (turboON) {
    if ((millis() - turboTimer) > 100) {
      turboState = !turboState;
      turboTimer = millis();
      digitalWrite(A2, turboState);
    }
  }
  else if (turbo) {
    if ((millis() - turboTimer) > 3000) {
      turbo = false;
      turboState = 0;
      turboTimer = millis();
      digitalWrite(A2, turboState);
    }
  }
  else {
    if ((millis() - turboTimer) > 7000) {
      turboON = true;
      turboState = 1;
      turboTimer = millis();
      digitalWrite(A2, turboState);
      String msg = "";
      msg += node_address;
      msg += 'T';
      msg += 1;
      nRF_send(msg);
    }
  }
}

void checkIRreceiverState() {
  if (irrecv.decode(&results)) {
    int code = results.value;
    if (code > 0 && code < 11) {
      if (lastGate != code) {
        lastGate = code;
        String msg = "";
        msg += node_address;
        msg += 'G';
        msg += code;
        nRF_send(msg);
      }
    } else if (code > 10 && code < 16) {
      hasBeenShoot = true;
      String msg = "";
      msg += node_address;
      msg += 'D';
      msg += code - 10;
      nRF_send(msg);
    } else if (code > 20 && code < 26) {
      String msg = "";
      msg += node_address;
      msg += 'P';
      msg += code - 20;
      nRF_send(msg);
    }
    irrecv.resume();
  }
}

void getRaceType() {
  if (RaceTypeTimer == 0) RaceTypeTimer = millis();
  if ((millis() - RaceTypeTimer) > 2000) {
    RaceTypeTimer = millis();
    String msg = "";
    msg += node_address;
    msg += 'Z';
    msg += 0;
    nRF_send(msg);
  }
}

void readEEPROM() {
  // 0 carID, 1 channel, 2 reverseY, 3 stepY-F, 4 stepY-B, 5 reverseX, 6 FB Servo/Analog, 7 LR Servo/Analog, 8 FB-Spin, 9 FB-ApinA. 10 FB-ApinB, 11 LR-Spin, 12 LR-ApinA, 13 LR-ApinB
  delay(100);
  node_address = EEPROM.read(0);
  my_node = String(node_address);
  delay(100);
  node_controller = node_address + 8;
  node_channel = EEPROM.read(1);
  delay(100);
  reverseY = EEPROM.read(2);
  delay(100);
  YstepF = EEPROM.read(3);
  delay(100);
  YstepR = EEPROM.read(4);
  delay(100);
  reverseX = EEPROM.read(5);
  delay(100);
  FBservo = EEPROM.read(6);
  delay(100);
  LRservo = EEPROM.read(7);
  delay(100);
  servoFBpin = EEPROM.read(8);
  delay(100);
  forwardPin = EEPROM.read(9);
  delay(100);
  backwardPin = EEPROM.read(10);
  delay(100);
  servoLRpin = EEPROM.read(11);
  delay(100);
  leftPin = EEPROM.read(12);
  delay(100);
  rightPin = EEPROM.read(13);
  delay(100);
}

void writeEEPROM() {  
  for (int i = 0; i < 14; i++) {
    Serial.print(configOption[i]);
    Serial.print(EEPROM.read(i));
    Serial.print("): ");
    int Evalue = -1;
    while (true) {
      char num = Serial.read();
      if (num == 13) {
        if (Evalue != -1) {
          EEPROM.update(i, Evalue);
          if (i == 6) FBservo = Evalue;
          else if (i == 7) LRservo = Evalue;
        }
        break;
      } else if (num > 47 && num < 58) {
        if (Evalue == -1) Evalue = 0;
        Evalue = (Evalue*10) + (num-48);
      }
    }
    if (i == 7 && !FBservo) i++;
    else if (i == 8 && FBservo && !LRservo) i += 3;
    else if (i == 8 && FBservo && LRservo) i += 2;
    if (i == 10 && !LRservo) i++;
    else if (i == 11 && LRservo) i += 2;
  }
}

String currentEEPROM() {
  for (int i = 0; i < 14; i++) {
    Serial.print(configOption[i]);
    Serial.print(EEPROM.read(i));
    Serial.print(")");
    delay(100);
  }
}
