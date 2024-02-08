# Online Judge
#### A Type-safe Asynchronous Microservice Online Judge System 100% written in Kotlin

#### this system is in development 

## Modules

### go-judger-api
a **kotlin-multiplatform** **API** module define judge api and platform independence operation,shared by all services.

### go-judger

a **Ktor** app to communicate with the **go-judge** .

### go-judger-app

a **kotlin-multiplatform** **full-stack** app with the **KVision**, **JsMain** is the **Frontend**, **CommonMain** provide some common api and operations,**JvmMain** is the **Backend** written by **Spring Boot**(may be replaced by **Ktor**) 

