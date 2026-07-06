import { Component, signal, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { AuthService } from '../services/auth.service';

// Transaction: İşlem listesi için kullanılan veri yapısı model tanımı.
interface Transaction {
  id: string;
  title: string;
  category: string;
  amount: number;
  type: 'income' | 'expense';
  date: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DecimalPipe, RouterLink], // HTML içinde sayı biçimlendirme (number pipe) kullanabilmek için DecimalPipe ekliyoruz
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class DashboardComponent {
  // Yönlendirme ve veri oturumu işlemleri için ilgili bağımlılıkları enjekte ediyoruz
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  // Oturum açmış kullanıcının adı ve baş harfleri
  readonly userName = signal(this.authService.getName() || 'Kullanıcı');
  readonly userInitials = computed(() => {
    const name = this.userName().trim();
    if (!name) return 'K';
    const parts = name.split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  });

  // Finansal özet istatistiklerini tutan sinyaller (Signals)
  readonly balance = signal(14250.75); // Toplam bakiye değeri
  readonly income = signal(5800.00); // Gelir değeri
  readonly expenses = signal(1549.25); // Gider değeri
  readonly currency = signal('₺'); // Para birimi simgesi

  // transactions: Arayüzde listelenecek mockup (yapay) son işlemler verisini tutan dizi sinyali.
  readonly transactions = signal<Transaction[]>([
    { id: '1', title: 'Aylık Maaş Yatırımı', category: 'Gelir', amount: 5000.00, type: 'income', date: 'Bugün' },
    { id: '2', title: 'Market Alışverişi', category: 'Mutfak', amount: 345.50, type: 'expense', date: 'Dün' },
    { id: '3', title: 'Elektrik Faturası', category: 'Faturalar', amount: 480.00, type: 'expense', date: '29 Haz' },
    { id: '4', title: 'Freelance Tasarım Projesi', category: 'Gelir', amount: 800.00, type: 'income', date: '28 Haz' },
    { id: '5', title: 'Kahve & Atıştırmalık', category: 'Sosyal', amount: 75.00, type: 'expense', date: '27 Haz' },
    { id: '6', title: 'Dijital Platform Üyeliği', category: 'Eğlence', amount: 149.99, type: 'expense', date: '25 Haz' }
  ]);

  // logout: Kullanıcının oturum bilgilerini siler ve giriş sayfasına yönlendirir.
  logout(): void {
    this.authService.clearSession(); // Tarayıcı hafızasındaki token'ı ve email'i temizler
    this.router.navigate(['/login']); // Giriş sayfasına geri döner
  }
}
