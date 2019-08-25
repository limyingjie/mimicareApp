/*
    Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleServer.cpp
    Ported to Arduino ESP32 by Evandro Copercini
    updates by chegewara
*/

/*
Version 4, attempt to adapt reading messages sent from phone.
*/

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define CHARACTERISTIC_UUID_2 "beb5483e-36e1-4688-b7f5-ea07361b26a9" // testing another characteristics


void setup() {
  Serial.begin(115200);
  Serial.println("Starting BLE work!");

  uint8_t presentationFormat1[] = {  //cccd?
  0x01, // 
  0x00 //
  
};


  BLEDevice::init("ES32Testbed"); // this is the name of device
  BLEServer *pServer = BLEDevice::createServer();
  BLEService *pService = pServer->createService(SERVICE_UUID);
  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_WRITE                                        
                                       );

  pCharacteristic->setValue("Hello World says Neil"); 

  // addtion of characteristic in a service --------------------------------------------------------
  BLECharacteristic *pCharacteristic2 = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID_2,
                                         BLECharacteristic::PROPERTY_READ | // I have made it read only
                                         BLECharacteristic::PROPERTY_NOTIFY 
                                       );

 // Adding descriptor
 BLEDescriptor *pDescriptor1 = new BLEDescriptor((uint16_t)0x2902); // 0x2902 is for the notification service. This descriptor is needed for notification to work   
  pDescriptor1->setValue(presentationFormat1, sizeof presentationFormat1);                                 
  pCharacteristic2->addDescriptor(pDescriptor1);

  //pCharacteristic2->setValue("Hi Neil says Arduino"); //this is the characteristics (data) to be sent
  // end of addition of characteristic --------------------------------------------------------------
  
  pService->start();
  // BLEAdvertising *pAdvertising = pServer->getAdvertising();  // this still is working for backward compatibility
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
  Serial.println("Characteristic defined! Now you can read it in your phone!");





// All setup done, This will be the start of a while loop(the main loop)
  while(1){
  delay(5000);
  pCharacteristic2->setValue("updates"); //this is the characteristics (data) to be sent
  pCharacteristic2->notify(); // this sends the notification
  Serial.println("Notify changed to updates");
  delay(5000);

  
  pCharacteristic2->setValue("updates2"); 
    pCharacteristic2->notify();
      Serial.println("Notify changed to updates2");


      // RGB Code
      //Serial.println(pCharacteristic->getValue());//does not work, need to change to string
  if (pCharacteristic->getValue() == "r"){ //getvalue gets the value of the characteristic
    Serial.println("RED"); //the get value gets the byte value so 31 = 1
    } else if (pCharacteristic->getValue() == "g"){
      Serial.println("GREEN");
      } else if (pCharacteristic->getValue() == "b"){
        Serial.println("BLUE");
        }

// r = 72
// g = 67
// b = 62
  //pCharacteristic2->readValue
  }
}

void loop() {
  // put your main code here, to run repeatedly:
  // I have decided not to use this loop, code will be in the setup, and there will be a loop there.
  // Either that or there is a need to bring information from the setup down here
  delay(20000);


}
