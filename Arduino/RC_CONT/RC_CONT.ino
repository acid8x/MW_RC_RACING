#include <RF24Network.h>
#include <RF24.h>
#include <SPI.h>

RF24 radio(9, 10);
RF24Network network(radio);

struct payload_controller {
	int positionX;
	int positionY;
	int positionA;
	int positionB;
};

const int this_node = 012;
const int car_node = 02;

long previousMillis = 0;
int oldValue;

int joyPinY = A0, joyPinX = A1, but1pin = 5, but2pin = 6;

void setup(void) {
	pinMode(but1pin, INPUT_PULLUP);
	pinMode(but2pin, INPUT_PULLUP);
	pinMode(3, OUTPUT);
	digitalWrite(3, HIGH);
	SPI.begin();
	radio.begin();
	network.begin(/*channel*/ 115, /*node address*/ this_node);
}

void loop(void) {
	network.update();
	int Xval = analogRead(joyPinX);
  int X;
  if (Xval > 712) X = 2;
  else if (Xval < 312) X = 0;
  else X = 1;
	int Yval = analogRead(joyPinY);
  int Y = Yval / 64;
	int A = digitalRead(but1pin);
	int B = digitalRead(but2pin);
  int value = X+Y+A+B;
	long currentMillis = millis();
	if (value != oldValue || currentMillis - previousMillis >= 150) {
		payload_controller payload = { X, Y, A, B };
		RF24NetworkHeader header(/*to node*/ car_node);
		network.write(header, &payload, sizeof(payload));
		previousMillis = currentMillis;
    oldValue = value;
	}
}
