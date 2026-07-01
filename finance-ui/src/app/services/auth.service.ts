import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// AuthResponse: Spring Boot backend'inden (login ve register sonucunda) dönecek yanıtın veri yapısı (DTO).
export interface AuthResponse {
  token: string;
  email: string;
  name: string;
}

// @Injectable: Bu sınıfın bir "servis" olduğunu ve tüm uygulamada istenen her yerden enjekte edilerek (kullanılarak) paylaşılabileceğini belirtir.
@Injectable({
  providedIn: 'root', // 'root' olması, tüm uygulama boyunca tek bir servis örneği (singleton) kullanılacağını belirtir.
})
export class AuthService {
  // Angular'ın yerleşik HTTP İstemcisini (HttpClient) enjekte ediyoruz
  private readonly http = inject(HttpClient);

  // proxy.conf.json üzerinden backend (http://localhost:8082)'e yönlenecek isteklerin ana yolu
  private readonly baseUrl = '/auth';

  // login: E-posta ve şifreyi Spring Boot'a post eder. Dönecek veri tipi AuthResponse'tur.
  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, { email, password });
  }

  // register: E-posta, şifre, ad-soyad, telefon ve opsiyonel rolü Spring Boot'a post eder.
  register(email: string, password: string, name: string, phone: string, role: string = 'USER'): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, { email, password, name, phone, role });
  }

  // saveSession: Başarılı giriş/kayıt sonrasında backend'in döndüğü JWT Token, Email ve Ad bilgisini tarayıcı hafızasına (localStorage) yazar.
  saveSession(authData: AuthResponse): void {
    localStorage.setItem('auth_token', authData.token);
    localStorage.setItem('auth_email', authData.email);
    localStorage.setItem('auth_name', authData.name || '');
  }

  // getToken: Tarayıcı hafızasındaki JWT Token'ı okur.
  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  // getEmail: Tarayıcı hafızasındaki giriş yapmış kullanıcının e-postasını okur.
  getEmail(): string | null {
    return localStorage.getItem('auth_email');
  }

  // getName: Tarayıcı hafızasındaki giriş yapmış kullanıcının adını okur.
  getName(): string | null {
    return localStorage.getItem('auth_name');
  }

  // clearSession: Kullanıcı çıkış yaptığında tarayıcı hafızasındaki bilgileri siler.
  clearSession(): void {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_email');
    localStorage.removeItem('auth_name');
  }

  // isLoggedIn: Kullanıcının oturum açıp açmadığını, token değerinin varlığına bakarak kontrol eder.
  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
