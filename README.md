# My Finance - Mikroservis Projesi

Bu proje; Java 21, Spring Boot, Spring Cloud, Kafka, Angular ve Docker kullanılarak geliştirilmiş ölçeklenebilir ve modern bir finansal yönetim platformudur. Sistem kapsamında kullanıcıların kayıt olma ve giriş işlemleri (JWT bazlı), profil yönetimi, TRY/USD/EUR para birimlerinde cüzdan oluşturulması, cüzdanlar arası canlı döviz kurları ile para transferleri, para çekme/yatırma hareketleri ve tarihsel işlem geçmişi takibi gibi uçtan uca tüm finansal akışlar mikroservis standartlarında tasarlanmıştır.

---

## 🏗️ Genel Mimari Şema

Sistem; istemci (Angular UI) isteklerinin tek bir API Geçidinden (Gateway) geçmesi, servislerin birbirini Eureka Discovery ile keşfetmesi, haberleşmelerde OpenFeign ve Kafka kullanılması üzerine kurulmuştur:

```mermaid
graph TD
    %% İstemci ve Gateway
    UI["Finance UI (Angular)"] -->|"API İstekleri"| GW[API Gateway]

    %% Altyapı Servisleri
    CS[Config Server] -->|"Merkezi Yapılandırma"| GW
    CS -->|"Merkezi Yapılandırma"| AS[Auth Service]
    CS -->|"Merkezi Yapılandırma"| CUST[Customer Service]
    CS -->|"Merkezi Yapılandırma"| WALLET[Wallet Service]
    CS -->|"Merkezi Yapılandırma"| NOTIF[Notification Service]
    
    DS[Discovery Registry] <-->|"Dinamik Kayıt & Keşif"| GW
    DS <-->|"Dinamik Kayıt & Keşif"| AS
    DS <-->|"Dinamik Kayıt & Keşif"| CUST
    DS <-->|"Dinamik Kayıt & Keşif"| WALLET
    DS <-->|"Dinamik Kayıt & Keşif"| NOTIF

    %% Gateway Yönlendirmeleri
    GW -->|"/auth/**"| AS
    GW -->|"/customers/**"| CUST
    GW -->|"/wallets/**"| WALLET

    %% Servislerin Veritabanları ve Dış Dünya
    AS -->|"Kullanıcı Bilgileri"| DB_PG[(PostgreSQL)]
    CUST -->|"Müşteri Profilleri"| DB_MONGO[(MongoDB)]
    WALLET -->|"Cüzdanlar & Transferler"| DB_PG[(PostgreSQL)]
    WALLET -.->|"Scheduled Task"| EXT_API["Dış Döviz Kurları API"]

    %% Feign İletişimi
    AS -.->|"Feign Client: Müşteri Profilini Aç"| CUST

    %% Asenkron Olay Akışı (Kafka)
    AS -->|"Kayıt/Giriş Eventleri"| KAFKA{{Kafka Broker}}
    KAFKA -->|"Mesaj Tüketimi"| NOTIF
    NOTIF -->|"SMTP Protokolü"| MAIL[Gmail SMTP Server]
```

---

## 🛠️ Kullanılan Teknolojiler

*   **Java 21** & **Spring Boot 3.x/4.x** (Arka Plan Servisleri)
*   **Angular 17+** (Kullanıcı Arayüzü)
*   **Spring Cloud Gateway** (API Geçidi & JWT Doğrulama)
*   **Spring Cloud Eureka** (Hizmet Keşfi & Kayıt Defteri)
*   **Spring Cloud Config Server** (Merkezi Yapılandırma Yönetimi)
*   **Apache Kafka** (Servisler Arası Asenkron Mesajlaşma & Olay Akışı)
*   **PostgreSQL** (Kullanıcı ve Cüzdan Verileri)
*   **MongoDB** (Müşteri Profilleri)
*   **Docker** & **Docker Compose** (Altyapı Konteynerizasyonu)
*   **OpenFeign** & **Spring Cloud LoadBalancer** (Senkron Servisler Arası İletişim)
*   **Spring Boot Starter Mail & JavaMailSender** (E-posta Bildirim Gönderimi)
*   **Zipkin** (Dağıtık İstek İzleme & Trace Takibi)
