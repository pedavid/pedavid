#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WiFiMulti.h> 
#include <ESP8266mDNS.h>
#include <ESP8266WebServer.h>  
#include <Wire.h>
#include <Adafruit_ADS1X15.h>   // Control del ADC
#include <ArduinoJson.h>
#include <Ticker.h>

/************ Defines **********************************/
#define CONNECTED 0
#define NO_CREDENTIALS 1
#define CREDENTIALS 2
#define MEDIR 1
#define NO_MEDIR 0
#define MUESTRAS 80
#define ADS_ADDRESS 0x48
#define PERIODO_MUESTREO 2


/************ Declaracion de funciones *****************/
ESP8266WebServer server(80);
Ticker timer_medir_adc;
Adafruit_ADS1115 ads;

int tension_adc_2_tension_red(int16_t counts);
int tension_adc_2_corriente_red(int16_t counts);


/************* Declaración de variables globales *********************************/
const int capacity = JSON_ARRAY_SIZE(MUESTRAS+1) + 2*JSON_OBJECT_SIZE(MUESTRAS); // Hago espacio en memoria para el json
StaticJsonDocument<capacity> doc;

JsonObject root = doc.to<JsonObject>();

JsonArray json = root.createNestedArray("data");       // Creo el objeto Json

String output;                                    // Aca guardo la deserializacion del json
String ssid = "ssid";
String pass = "pass";
int flagAdc = MEDIR;
uint32_t credentials = NO_CREDENTIALS;
int a = 0;
int i = 0;
int adcBufferTension[MUESTRAS];
int adcBufferCorriente[MUESTRAS];
int period = 2;
unsigned long time_now = 0;

/***************HTML********************************************/
const char credentials_html[] PROGMEM = R"rawliteral(
<!DOCTYPE HTML><html><head>
  <title>Medidor de potencia</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    html {font-family: Times New Roman; display: inline-block; text-align: center;}
    h2 {font-size: 3.0rem; color: #FF0000;}
  </style>
  </head><body>
  <FORM METHOD="POST"action="/postForm">
  <input type="text" name="ssid" value="Escriba el ssid">
  <input type="text" name="pass" value="Escriba el password">
  <input type="submit" value="Post credentials">
  </form><br>
</body></html>)rawliteral";
/**************************************************************/

// Set AP credentials
#define AP_SSID "Medidor de potencia"
#define AP_PASS "123456789"

/************************ FUNCIONES ***************************/
void leer_adc(){
  int state = digitalRead(LED_BUILTIN);  
  digitalWrite(LED_BUILTIN, !state);     // Led blink

  if(flagAdc==MEDIR){
    time_now += period;
    adcBufferCorriente[i] = tension_adc_2_corriente_red( ads.readADC_SingleEnded(0) );
    adcBufferTension[i]   = tension_adc_2_tension_red  ( ads.readADC_SingleEnded(1) );
    i++;
  }
  if(i>=MUESTRAS) // Si lleno el buffer
    flagAdc = NO_MEDIR;
}
 
void setup()
{
  Serial.begin(115200);
  Serial.println();

  Wire.begin();
  ads.begin();
  ads.setGain(GAIN_TWOTHIRDS);  // 2/3x gain +/- 6.144V  1 bit = 3mV      0.1875mV (default) 
 
  // Initialize AP mode (access point)
  WiFi.mode(WIFI_AP_STA);
  WiFi.softAP(AP_SSID, AP_PASS);

  // Servicios asociados al modo AP
  server.on("/inicio", inicioAP);
  server.on("/postForm", handlePostForm);
  server.begin(); 
 
}
 
void loop() {
  switch(credentials){  //Maquina de estados
    
    case NO_CREDENTIALS:  
      server.handleClient();
    break;

    case CREDENTIALS:
      WiFi.begin(ssid, pass);
      server.stop();
      Serial.print("Connecting to ");
      Serial.print(ssid);
      while (WiFi.status() != WL_CONNECTED)
      {
        delay(100);
        Serial.print(".");
      }
        server.on("/ip", credencialesSTA);
        server.on("/", handleRoot);               
        server.onNotFound(handleNotFound); 
        server.begin(); 
        delay(100);
        credentials = CONNECTED;
        timer_medir_adc.attach_ms(PERIODO_MUESTREO, leer_adc);
    break;
    
    case CONNECTED:
      if(flagAdc==NO_MEDIR){
        server.handleClient();
      }
    break;
 }
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
  //Serial.println(output);
  output.clear();
  flagAdc = MEDIR;
}

void handleNotFound(){
  server.send(404, "text/plain", "404: Not found");
}

void credencialesSTA(){
  String ip;
  ip = WiFi.localIP().toString();
  server.send(200, "text/html", "La direccion ip es: "+ ip);
}

void inicioAP(){
  server.send(200,"text/html", credentials_html);
}


void handlePostForm()
{
  String ssidRecibido = server.arg("ssid");
  String passRecibido = server.arg("pass");
  ssid = ssidRecibido;
  pass = passRecibido;
  credentials = CREDENTIALS;
  server.sendHeader("/ip", String("/"), true);
  server.send ( 302, "text/plain", "");
  
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
