import { Component, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [FormsModule], // HTML şablonunda [(ngModel)] veya input bağlama kullanabilmek için gerekli
  templateUrl: './auth.html',
  styleUrl: './auth.scss',
})
export class AuthComponent {
  // Bağımlılıkları (Router ve AuthService) bileşene enjekte ediyoruz
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  // mode: Sayfanın o anki modunu tutar ('login' veya 'register'). Varsayılan: 'login'
  readonly mode = signal<'login' | 'register'>('login');

  // Form Alanları için Sinyaller (Signals)
  // Input değerleri değiştikçe bu sinyaller otomatik güncellenecek
  readonly name = signal('');
  readonly phone = signal('');
  readonly email = signal('');
  readonly password = signal('');
  readonly confirmPassword = signal('');
  readonly rememberMe = signal(false);

  // UI Durum Sinyalleri
  readonly loading = signal(false); // İstek devam ederken spinner göstermek için
  readonly errorMessage = signal(''); // Sunucudan dönen hata mesajını tutar
  readonly successMessage = signal(''); // Başarılı işlem bildirimlerini tutar
  readonly showPassword = signal(false); // Şifre görünürlüğünü açıp kapatmak için

  // isLogin: Sadece mode() sinyalinin 'login' olup olmadığını kontrol eden hesaplanmış (computed) bir sinyaldir.
  // mode() değiştikçe bu değer de otomatik olarak yeniden hesaplanır.
  readonly isLogin = computed(() => this.mode() === 'login');

  // passwordStrength: Girilen şifrenin gücünü hesaplayan computed sinyal (0 ile 4 arası puan verir)
  readonly passwordStrength = computed(() => {
    const pass = this.password();
    if (!pass) return 0;

    let score = 0;
    if (pass.length >= 8) score++; // En az 8 karakter
    if (/[A-Z]/.test(pass)) score++; // Büyük harf içeriyor mu
    if (/[0-9]/.test(pass)) score++; // Rakam içeriyor mu
    if (/[[^A-Za-z0-9]/.test(pass)) score++; // Özel karakter içeriyor mu
    return score;
  });

  // passwordStrengthText: passwordStrength() değerine göre ekranda gösterilecek metni belirler.
  readonly passwordStrengthText = computed(() => {
    const score = this.passwordStrength();
    if (score === 0) return 'Çok Zayıf';
    if (score === 1) return 'Zayıf';
    if (score === 2) return 'Orta';
    if (score === 3) return 'Güçlü';
    return 'Çok Güçlü';
  });

  // toggleMode: Giriş Yap / Kayıt Ol modları arasında geçiş yapar ve form alanlarını sıfırlar.
  toggleMode(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    this.name.set('');
    this.phone.set('');
    this.password.set('');
    this.confirmPassword.set('');
    this.showPassword.set(false);

    this.mode.set(this.mode() === 'login' ? 'register' : 'login');
  }

  // toggleShowPassword: Şifrenin görünürlüğünü tersine çevirir.
  toggleShowPassword(): void {
    this.showPassword.update((show) => !show);
  }

  // onSubmit: Form submit edildiğinde çalışacak ana tetikleyici.
  onSubmit(): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (this.isLogin()) {
      this.handleLogin();
    } else {
      this.handleRegister();
    }
  }

  // handleLogin: Spring Boot API'sine giriş isteği gönderir.
  private handleLogin(): void {
    const emailVal = this.email().trim();
    const passVal = this.password();

    // Temel ön doğrulamalar
    if (!emailVal || !passVal) {
      this.errorMessage.set('Lütfen tüm alanları doldurun.');
      return;
    }

    if (!this.isValidEmail(emailVal)) {
      this.errorMessage.set('Geçersiz e-posta formatı.');
      return;
    }

    this.loading.set(true); // Yükleniyor durumunu başlat (spinner göster)

    // AuthService içindeki metodu çağırıp sunucu yanıtını dinliyoruz (subscribe)
    this.authService.login(emailVal, passVal).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.authService.saveSession(res); // Token'ı localStorage'a kaydet
        this.router.navigate(['/dashboard']); // Paneli aç
      },
      error: (err) => {
        this.loading.set(false);
        console.error('Giriş hatası:', err);
        // Spring Boot'tan veya custom handler'dan dönen hata mesajını göster
        const msg = this.getErrorMessage(err) || 'Giriş yapılamadı. E-posta veya şifre hatalı.';
        this.errorMessage.set(msg);
      },
    });
  }

  // handleRegister: Spring Boot API'sine kayıt isteği gönderir.
  private handleRegister(): void {
    const nameVal = this.name().trim();
    const phoneVal = this.phone().trim();
    const emailVal = this.email().trim();
    const passVal = this.password();
    const confirmPassVal = this.confirmPassword();

    // Temel doğrulamalar
    if (!nameVal || !phoneVal || !emailVal || !passVal || !confirmPassVal) {
      this.errorMessage.set('Lütfen tüm alanları doldurun.');
      return;
    }

    if (!this.isValidEmail(emailVal)) {
      this.errorMessage.set('Geçersiz e-posta formatı.');
      return;
    }

    if (passVal.length < 6) {
      this.errorMessage.set('Şifre en az 6 karakter olmalıdır.');
      return;
    }

    if (passVal !== confirmPassVal) {
      this.errorMessage.set('Şifreler uyuşmuyor.');
      return;
    }

    this.loading.set(true); // Yükleniyor durumunu başlat

    // AuthService üzerinden kaydolma isteği gönderiyoruz
    this.authService.register(emailVal, passVal, nameVal, phoneVal).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.authService.saveSession(res); // Kayıt olunca otomatik giriş yapması için token'ı kaydet
        this.successMessage.set('Kayıt başarıyla tamamlandı! Yönlendiriliyorsunuz...');

        // 1.5 saniye sonra kullanıcıyı panele yönlendir
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 1500);
      },
      error: (err) => {
        this.loading.set(false);
        console.error('Kayıt hatası:', err);
        const msg = this.getErrorMessage(err) || 'Kayıt başarısız. E-posta adresi zaten kullanımda olabilir.';
        this.errorMessage.set(msg);
      },
    });
  }

  // getErrorMessage: Backend'den dönen nested veya düz hata mesajlarını akıllıca parse eder.
  private getErrorMessage(err: any): string {
    if (!err) return '';
    const errorBody = err.error;
    if (errorBody) {
      if (typeof errorBody === 'string') {
        return errorBody;
      }
      // GlobalExceptionHandler'dan dönen ErrorResponse (errors: { message: "..." })
      if (errorBody.errors && errorBody.errors.message) {
        return errorBody.errors.message;
      }
      // Standart Spring Boot veya diğer hata gövdeleri (message: "...")
      if (errorBody.message) {
        return errorBody.message;
      }
    }
    if (err.message) {
      return err.message;
    }
    return '';
  }

  // isValidEmail: Basit regex e-posta format doğrulaması.
  private isValidEmail(email: string): boolean {
    const re = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return re.test(email);
  }
}
