# My Finance - Mikroservis Projesi

Bu proje; **Java 21**, **Spring Boot**, **Spring Cloud** ve **Kafka** mimarileri kullanılarak geliştirilmiş, ölçeklenebilir ve modern bir finansal arka plan (backend) sistemidir. Projede servisler arası asenkron iletişim, merkezi yapılandırma yönetimi, servis keşfi (discovery) ve güvenli kimlik doğrulama mekanizmaları uygulanmıştır.

---

## 🏗️ Genel Mimari Şema

Sistem; istemci (UI) isteklerinin tek bir kapıdan alınması, servislerin birbirini dinamik olarak keşfetmesi ve bazı işlemlerin asenkron (Kafka aracılığıyla) yürütülmesi üzerine kurulmuştur:

```mermaid
graph TD
    %% İstemci ve Gateway
    UI[Finance UI Portal] -->|API İstekleri| GW[API Gateway]

    %% Altyapı Servisleri
    CS[Config Server] -->|Merkezi Konfigürasyon Dağıtımı| GW
    CS -->|Merkezi Konfigürasyon Dağıtımı| AS[Auth Service]
    CS -->|Merkezi Konfigürasyon Dağıtımı| CUST[Customer Service]
    CS -->|Merkezi Konfigürasyon Dağıtımı| NOTIF[Notification Service]
    
    DS[Discovery Registry] <-->|Dinamik Kayıt & Keşif| GW
    DS <-->|Dinamik Kayıt & Keşif| AS
    DS <-->|Dinamik Kayıt & Keşif| CUST
    DS <-->|Dinamik Kayıt & Keşif| NOTIF

    %% Gateway Yönlendirmeleri
    GW -->|/auth/**| AS
    GW -->|/customers/** (JWT Korumalı)| CUST

    %% Servislerin Veritabanları
    AS -->|Kullanıcı Bilgileri| DB_PG[(PostgreSQL)]
    CUST -->|Müşteri Profilleri| DB_MONGO[(MongoDB)]

    %% Feign İletişimi
    AS -.->|Feign Client: Müşteri Oluştur/Sorgula| CUST

    %% Asenkron Olay Akışı (Kafka)
    AS -->|Kayıt/Giriş Eventleri| KAFKA{{Kafka Broker}}
    KAFKA -->|Mesaj Tüketimi| NOTIF
    NOTIF -->|SMTP Protokolü| MAIL[E-posta Sunucusu]
```

---

## 🧩 Servisler ve Görevleri

Sistem, her biri tek bir sorumluluğa (Single Responsibility) sahip mikroservislerin bir araya gelmesiyle çalışır:

### 1. Yapılandırma Sunucusu (`config-server`)
* **Görevi:** Tüm mikroservislerin konfigürasyon dosyalarını (`application.yml` vb.) tek bir merkezden yönetir.
* **Çalışma Şekli:** Servisler ilk ayağa kalktıklarında bu sunucuya bağlanarak kendilerine ait veritabanı bağlantı bilgilerini, port ayarlarını ve özel tanımlarını çekerler.

### 2. Hizmet Kayıt Defteri (`discovery-service`)
* **Görevi:** Sistemdeki tüm mikroservislerin dinamik olarak kayıt olduğu ve birbirlerinin IP/Port bilgilerini öğrendiği Eureka sunucusudur.
* **Çalışma Şekli:** Bir servis diğerine istek atacağı zaman (örneğin Auth servisinin Müşteri servisine bağlanması) doğrudan IP yazmak yerine Eureka üzerinden isme göre dinamik yönlendirme yapar.

### 3. API Geçidi (`gateway`)
* **Görevi:** Dış dünyaya açılan tek kapıdır. İstemciden (Frontend) gelen tüm istekleri karşılar ve ilgili servislere yönlendirir.
* **Güvenlik:** Giriş (Login) ve Kayıt (Register) dışındaki korumalı servislere (Müşteri bilgileri gibi) erişim isteklerini yakalar, JWT (JSON Web Token) kontrolünü yapar ve sadece geçerli token'a sahip isteklerin geçişine izin verir.

### 4. Kimlik Doğrulama Servisi (`auth-service`)
* **Görevi:** Kullanıcı üyelik işlemlerini, sisteme giriş kontrollerini ve güvenlik tokenı (JWT) üretimini yönetir.
* **Veritabanı:** Kullanıcı adı, şifre hash'leri (BCrypt) ve yetkileri **PostgreSQL** üzerinde saklanır.
* **Kafka Entegrasyonu:** Bir kullanıcı başarıyla kayıt olduğunda veya giriş yaptığında bunu sisteme duyurmak için Kafka'ya birer olay (Event) fırlatır.

### 5. Müşteri Yönetim Servisi (`customer`)
* **Görevi:** Müşterilerin detaylı profil bilgilerini (ad, soyad, telefon vb.) yönetir.
* **Veritabanı:** İlişkisel olmayan, esnek yapılı **MongoDB** üzerinde verileri saklar.

### 6. Bildirim Servisi (`notification`)
* **Görevi:** Kullanıcılara gönderilecek sistem bildirimlerini yönetir.
* **Çalışma Şekli:** Kafka üzerindeki ilgili konuları (Topic) dinler. Yeni bir kullanıcı kayıt olduğunda ya da sisteme giriş yapıldığında bu olayları yakalayarak kullanıcıya otomatik olarak hoş geldin ya da güvenlik uyarı e-postaları gönderir.

---

## 🔄 Sistem Nasıl Çalışır? (Kayıt ve Bildirim Akışı)

1. **İstek ve Kayıt:** İstemci, API Gateway üzerinden geçerek `auth-service` üzerindeki `/register` endpoint'ine istek atar.
2. **Kullanıcı & Profil Oluşturma:** Auth servisi kullanıcıyı PostgreSQL veritabanına kaydeder ve ardından arka planda Feign Client aracılığıyla `customer` servisine bağlanarak bu kullanıcının detaylı müşteri profilini MongoDB üzerinde oluşturur.
3. **Kafka Olayı Fırlatma:** Auth servisi işlemi tamamladıktan sonra Kafka'ya bir `UserRegisterEvent` mesajı gönderir.
4. **Asenkron Mail Bildirimi:** `notification` servisi bu mesajı Kafka'dan asenkron olarak tüketir ve `EmailService` aracılığıyla kullanıcının adresine hoş geldin e-postası yollar.

---

## 🚀 Projeyi Çalıştırma

### Altyapıyı Başlatma
Veritabanları ve mesaj kuyruğu (PostgreSQL, MongoDB, Kafka vb.) Docker üzerinde hazır hale getirilmiştir. Projeyi ayağa kaldırmadan önce altyapıyı başlatın:
```bash
cd services
docker-compose up -d
```

### Servisleri Başlatma Sırası
Servislerin doğru şekilde birbirine bağlanabilmesi için şu sırayla çalıştırılması önerilir:
1. `config-server` (Yapılandırmaların okunabilmesi için en başta)
2. `discovery` (Diğer servislerin kayıt olabilmesi için)
3. `auth-service`, `customer`, `notification` (Çekirdek servisler)
4. `gateway` (Geçit kapısı)
