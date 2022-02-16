#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WiFiMulti.h> 
#include <ESP8266mDNS.h>
#include <ESP8266WebServer.h>  
#include <Wire.h>
#include <Adafruit_ADS1X15.h>   // Control del ADC
#include <ArduinoJson.h>
#include <Ticker.h>

#define MUESTRAS 80
#define ADS_ADDRESS 0x48
#define MEDIR 1
#define NO_MEDIR 0
#define PERIODO_MUESTREO 2

Ticker timer_medir_adc;

Adafruit_ADS1115 ads;

ESP8266WiFiMulti wifiMulti;   

ESP8266WebServer server(80);    

void handleRoot();              
void handleNotFound();
int tension_adc_2_tension_red(int16_t counts);
int tension_adc_2_corriente_red(int16_t counts);



const int capacity = JSON_ARRAY_SIZE(MUESTRAS+1) + 2*JSON_OBJECT_SIZE(MUESTRAS); // Hago espacio en memoria para el json
StaticJsonDocument<capacity> doc;

JsonObject root = doc.to<JsonObject>();

JsonArray json = root.createNestedArray("data");       // Creo el objecto Json

//JsonArray json = doc.to<JsonArray>();

String output;                                    // AcÃ¡ guardo la deserializacion del json
 

int a = 0;
int i = 0;
int flagAdc = MEDIR;
int adcBufferTension[MUESTRAS];
int adcBufferCorriente[MUESTRAS];
int period = 2;
unsigned long time_now = 0;

void leer_adc(){
  int state = digitalRead(LED_BUILTIN);  
  digitalWrite(LED_BUILTIN, !state);     // Led blink
  /**
     COMO ESTAN CONECTADOS EN MI PLACA (LUCHO)
     CANAL 0 -----> SESNOR I
     CANAL 1 -----> SESNOR V
  **/
  if(flagAdc==MEDIR){
    time_now += period;
    adcBufferCorriente[i] = tension_adc_2_corriente_red( ads.readADC_SingleEnded(0) );
    adcBufferTension[i]   = tension_adc_2_tension_red  ( ads.readADC_SingleEnded(1) );
    i++;
  }
  if(i>=MUESTRAS) // Si lleno el buffer
    flagAdc = NO_MEDIR;
}

void setup(void){

  pinMode(LED_BUILTIN, OUTPUT);
  Serial.begin(115200);      
  delay(10);
  Serial.println('\n');

  Wire.begin();
  ads.begin();
  ads.setGain(GAIN_TWOTHIRDS);  // 2/3x gain +/- 6.144V  1 bit = 3mV      0.1875mV (default)
  
  wifiMulti.addAP("Pedro_2.4GHz", "contraseñaSuperSecreta");   
  delay(10);
  Serial.println("Connecting ...");
 
  while (wifiMulti.run() != WL_CONNECTED) {
    delay(250);
    Serial.print('.');
  }
  Serial.println('\n');
  Serial.print("Connected to ");
  Serial.println(WiFi.SSID());             
  Serial.print("IP address:\t");
  Serial.println(WiFi.localIP());        

  if (MDNS.begin("esp8266")) {              
    Serial.println("mDNS responder started");
  } else {
    Serial.println("Error setting up MDNS responder!");
  }

  server.on("/", handleRoot);               
  server.onNotFound(handleNotFound);        

  server.begin();                           
  Serial.println("HTTP server started");
  
  timer_medir_adc.attach_ms(PERIODO_MUESTREO, leer_adc);
}

void loop(void){
  if(flagAdc==NO_MEDIR)
    server.handleClient();    // Escuchar requests HTTP
} 

void handleRoot() {
  delay(20);
  for(i=0;i<MUESTRAS;i++){
    json[i]["tension"] = adcBufferTension[i];
    json[i]["corriente"] = adcBufferCorriente[i];
  }

  i=0;
  serializeJson(doc, output);
  
  server.send(200, "text/json", output);   // Enviar el string y un code 200
  delay(50);
  Serial.println(output);
  output.clear();
  flagAdc = MEDIR;
}

void handleNotFound(){
  server.send(404, "text/plain", "404: Not found");
}


int tension_adc_2_corriente_red(int16_t counts){
  float mean = 2.515f;
  float fsRange = 6.144f;
  float sqrt_2 = 1.41;
  float cte_sensor = 15.15; // VER DATASHEET P.7 YO TENGO SENSOR 20A 
  
  return (  (int)( ((counts * (fsRange / 32768))- mean)* cte_sensor*sqrt_2 )*1000/1000.0f );
}
int tension_adc_2_tension_red(int16_t counts){
  float mean = 2.528f;
  float fsRange = 6.144f;
  float cte_sensor = 439.01; // VER FOTO HOJA MIA
  float sqrt_2 = 1.41;
  return (  (int)( ((counts * (fsRange / 32768))-mean)*cte_sensor*sqrt_2 )*1000/1000.0f );
}
