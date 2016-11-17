    #include <SoftwareSerial.h>
    #define IR 4 
    #define RE 12
    #define ST 5
    #define DE 3
    #define DEBUG true
     
    SoftwareSerial HC06(0,1); 
    int ENA=11;
    int IN1=9;
    int IN2=8;
    int ENB=10;
    int IN3=7;
    int IN4=6;
    int detection = HIGH;
    void derecha(int val){
      digitalWrite(ENA,HIGH);
      digitalWrite(ENB,HIGH);
      digitalWrite(IN1,HIGH);
      digitalWrite(IN3,HIGH);
      digitalWrite(ST,LOW);
      analogWrite(ENA,val);
      analogWrite(ENB,val);
    }
    void izquierda(int val){
    digitalWrite(ENA,HIGH);
    digitalWrite(ENB,HIGH);
    digitalWrite(IN2,HIGH);
    digitalWrite(IN4,HIGH);
    digitalWrite(ST,LOW);
    analogWrite(ENA,val);
    analogWrite(ENB,val);
    }
    void adelante(int val){
      digitalWrite(ENA,HIGH);
      digitalWrite(ENB,HIGH);
      digitalWrite(IN2,HIGH);
      digitalWrite(IN1,LOW);
      digitalWrite(IN3,HIGH);
      digitalWrite(IN4,LOW);
      digitalWrite(ST,LOW);
      analogWrite(ENA,val);
      analogWrite(ENB,val);
    }
     void parar(){
      analogWrite(ENA,0);
      analogWrite(ENB,0);
      digitalWrite(ENA,LOW);
      digitalWrite(ENB,LOW);
      digitalWrite(IN1,LOW);
      digitalWrite(IN2,LOW);
      digitalWrite(IN3,LOW);
      digitalWrite(IN4,LOW);
      digitalWrite(ST,HIGH);
      digitalWrite(RE,LOW);
    }
     boolean hayObstaculo(){
      detection = digitalRead(IR);
      if(detection == LOW){
        Serial.println("Hay un obstaculo!\n");
        return true;
      }
      else{ 
        Serial.println("No hay obstaculo!\n");
        return false;
      }
     }
     void atras(int val){
        
        digitalWrite(ENA,HIGH);
        digitalWrite(ENB,HIGH);
        digitalWrite(IN2,LOW);
        digitalWrite(IN1,HIGH);
        digitalWrite(IN3,LOW);
        digitalWrite(IN4,HIGH);
        digitalWrite(ST,LOW);
        digitalWrite(RE,HIGH);
        analogWrite(ENA,val);
        analogWrite(ENB,val);
        
    }
    void setup()
    {
      Serial.begin(9600);
      HC06.begin(9600); // your esp's baud rate might be different
      pinMode(ENA,OUTPUT);//output
      pinMode(ENB,OUTPUT);
      pinMode(DE,OUTPUT);
      pinMode(ST,OUTPUT);
      pinMode(RE,OUTPUT);
      pinMode(IN1,OUTPUT);
      pinMode(IN2,OUTPUT);
      pinMode(IN3,OUTPUT);
      pinMode(IN4,OUTPUT);
      pinMode(IR,INPUT);
      pinMode(12,OUTPUT);
      pinMode(13,OUTPUT);
      digitalWrite(ENA,LOW);
      digitalWrite(ENB,LOW);//stop driving
      digitalWrite(IN1,LOW); 
      digitalWrite(IN2,LOW);//setting motorA's directon
      digitalWrite(IN3,LOW);
      digitalWrite(IN4,LOW);//setting motorB's directon
      analogWrite(ENA,255);//start driving motorA
      analogWrite(ENB,255);
    }
   char msg;
   int vel=100;
   boolean luz=false;
    void loop()
    {
      if(HC06.available()) // check if the esp is sending a message 
      {
        digitalWrite(13,HIGH);  
        msg=(HC06.read());
        Serial.println("Mensaje recibido: "+msg);
         if(msg =='1'){
           Serial.println("Avanzando: "+vel);
           adelante(vel);           
         }
         if(msg == '2'){
            if(!hayObstaculo()){
            atras(vel);
            Serial.println("Retrocediendo: "+vel);
            }    
         }
         if(msg == '3'){
          Serial.println("Detenido.");
          parar();
         }
         if(msg == '4'){
          Serial.println("Doblando a la izquierda: "+vel);
          izquierda(vel);
         }
         if(msg == '5'){
          Serial.println("Doblando a la derecha: "+vel);
          derecha(vel);
         }
         if(msg == '6'){
           vel=100;
         }
         if(msg == '7'){
           vel=150;
         }
         if(msg == '8'){
           vel=200;
         }
         if(msg == '9'){
           vel=255;
         }
         if(msg == '0'){
            if (luz){
              digitalWrite(DE,LOW);
              luz = false;
            }else{
              digitalWrite(DE,HIGH);
              luz = true;
            }
         }
      }
   }
