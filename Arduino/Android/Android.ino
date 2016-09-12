/* INCLUDE */
#include <RF24Network.h>
#include <RF24.h>
#include <SPI.h>
#include <SoftwareSerial.h>

SoftwareSerial mySerial(2,3); // TX, RX of other device

/* VARIABLE */
RF24 radio(7,8);
RF24Network network(radio);

uint8_t node_address = 0;
uint8_t node_channel = 115;

String my_node = String(node_address);
int to_node;

int dataBufferIndex = 0;

char RecvPayload[31] = "";
char mySerialBuffer[31] = "";

boolean stringComplete = false;
boolean to_node_set = false;

/* SETUP */

void setup(void) {
  mySerial.begin(9600);
  SPI.begin();
  radio.begin();
  network.begin(node_channel, node_address);
  delay(2000);
} // end setup()

/* MAIN LOOP */
 
void loop(void) {
  network.update();
  nRF_receive();
  if (mySerial.available() > 0) mySerialEvent();
} // end loop()

/* FUNCTION */
 
void mySerialEvent() {
  while (mySerial.available() > 0 ) {
    if (to_node_set == false) {
      char c = mySerial.read();
      to_node = c - 48;
      to_node_set = true;
    } else {
      char incomingByte = mySerial.read();
      if (incomingByte=='\r') {
        mySerialBuffer[dataBufferIndex] = 0;
        String msg = mySerialBuffer;
        nRF_send(msg,to_node);
        for (int i=0;i<31;i++) mySerialBuffer[i] = 0;
        dataBufferIndex = 0;
        to_node_set = false;
      } else {
        mySerialBuffer[dataBufferIndex++] = incomingByte;
        mySerialBuffer[dataBufferIndex] = 0;
      }
    }
  } // end while
} // end mySerialEvent()

void nRF_receive(void) {
  if (network.available()) {
    RF24NetworkHeader header;
    network.read(header,&RecvPayload,sizeof(RecvPayload));
    mySerial.println(RecvPayload);
    for (int i=0;i<31;i++) RecvPayload[i] = 0;
  }
} // end nRF_receive()

void nRF_send(String msg, int to) {
  char SendPayload[31] = "";
  for (int i=0;i<msg.length();i++) SendPayload[i] = msg[i];
  RF24NetworkHeader header(to);
  network.write(header,&SendPayload,strlen(SendPayload));
} // end nRF_send()
