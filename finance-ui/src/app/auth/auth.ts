import { Component, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './auth.html',
  styleUrl: './auth.scss',
})
export class AuthComponent {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  readonly mode = signal<'login' | 'register'>('login');

  readonly name = signal('');
  readonly phone = signal('');
  readonly email = signal('');
  readonly password = signal('');
  readonly confirmPassword = signal('');
  readonly rememberMe = signal(false);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly showPassword = signal(false);

  readonly isLogin = computed(() => this.mode() === 'login');

  readonly passwordStrength = computed(() => {
    const pass = this.password();
    if (!pass) return 0;

    let score = 0;
    if (pass.length >= 8) score++;
    if (/[A-Z]/.test(pass)) score++;
    if (/[0-9]/.test(pass)) score++;
    if (/[[^A-Za-z0-9]/.test(pass)) score++;
    return score;
  });

  readonly passwordStrengthText = computed(() => {
    const score = this.passwordStrength();
    if (score === 0) return 'Çok Zayıf';
    if (score === 1) return 'Zayıf';
    if (score === 2) return 'Orta';
    if (score === 3) return 'Güçlü';
    return 'Çok Güçlü';
  });

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

  toggleShowPassword(): void {
    this.showPassword.update((show) => !show);
  }

  onSubmit(): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (this.isLogin()) {
      this.handleLogin();
    } else {
      this.handleRegister();
    }
  }

  private handleLogin(): void {
    const emailVal = this.email().trim();
    const passVal = this.password();

    if (!emailVal || !passVal) {
      this.errorMessage.set('Lütfen tüm alanları doldurun.');
      return;
    }

    if (!this.isValidEmail(emailVal)) {
      this.errorMessage.set('Geçersiz e-posta formatı.');
      return;
    }

    this.loading.set(true);

    this.authService.login(emailVal, passVal).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.authService.saveSession(res);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading.set(false);
        console.error('Giriş hatası:', err);
        const msg = this.getErrorMessage(err) || 'Giriş yapılamadı. E-posta veya şifre hatalı.';
        this.errorMessage.set(msg);
      },
    });
  }

  private handleRegister(): void {
    const nameVal = this.name().trim();
    const phoneVal = this.phone().trim();
    const emailVal = this.email().trim();
    const passVal = this.password();
    const confirmPassVal = this.confirmPassword();

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

    this.loading.set(true);

    this.authService.register(emailVal, passVal, nameVal, phoneVal).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.authService.saveSession(res);
        this.successMessage.set('Kayıt başarıyla tamamlandı! Yönlendiriliyorsunuz...');

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

  private getErrorMessage(err: any): string {
    if (!err) return '';
    
    let rawMessage = '';
    const errorBody = err.error;
    if (errorBody) {
      if (typeof errorBody === 'string') {
        rawMessage = errorBody;
      } else if (errorBody.errors && errorBody.errors.message) {
        rawMessage = errorBody.errors.message;
      } else if (errorBody.message) {
        rawMessage = errorBody.message;
      }
    }
    
    if (!rawMessage && err.message) {
      rawMessage = err.message;
    }
    
    if (!rawMessage) {
      return 'Beklenmedik bir hata oluştu. Lütfen daha sonra tekrar deneyin.';
    }

    const lowerMsg = rawMessage.toLowerCase();
    
    if (err.status === 0 || lowerMsg.includes('failed to fetch') || lowerMsg.includes('unknown error')) {
      return 'Sunucuyla bağlantı kurulamadı. Lütfen internet bağlantınızı kontrol edin veya daha sonra tekrar deneyin.';
    }
    
    if (lowerMsg.includes('duplicate key') || lowerMsg.includes('incorrectresultsizedataaccessexception') || lowerMsg.includes('already exists')) {
      if (lowerMsg.includes('email') || lowerMsg.includes('customer')) {
        return 'Bu e-posta adresi ile kayıtlı bir hesap zaten bulunuyor.';
      }
      if (lowerMsg.includes('phone')) {
        return 'Bu telefon numarası ile kayıtlı bir hesap zaten bulunuyor.';
      }
      return 'Girdiğiniz bilgilerden bazıları zaten sistemde kayıtlı.';
    }
    
    if (lowerMsg.includes('bad credentials') || lowerMsg.includes('unauthorized') || lowerMsg.includes('401')) {
      return 'E-posta adresiniz veya şifreniz hatalı. Lütfen bilgilerinizi kontrol edin.';
    }
    
    if (
      lowerMsg.includes('relation') || 
      lowerMsg.includes('does not exist') || 
      lowerMsg.includes('sql') || 
      lowerMsg.includes('jdbc') || 
      lowerMsg.includes('query') || 
      lowerMsg.includes('hibernate') || 
      lowerMsg.includes('mongo') || 
      lowerMsg.includes('psqlexception') || 
      lowerMsg.includes('feignexception') || 
      lowerMsg.includes('internal server error') || 
      err.status === 500
    ) {
      return 'İşleminiz gerçekleştirilirken geçici bir sistem hatası oluştu. Lütfen daha sonra tekrar deneyin.';
    }

    return rawMessage;
  }

  private isValidEmail(email: string): boolean {
    const re = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return re.test(email);
  }
}