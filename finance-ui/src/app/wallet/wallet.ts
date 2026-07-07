import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { WalletService, WalletResponse } from '../services/wallet.service';
import { AuthService } from '../services/auth.service';

interface LocalTransaction {
  id: string;
  title: string;
  amount: number;
  type: 'deposit' | 'withdraw' | 'transfer_in' | 'transfer_out';
  date: string;
  status: 'SUCCESS' | 'FAILED';
}

@Component({
  selector: 'app-wallet',
  standalone: true,
  imports: [CommonModule, FormsModule, DecimalPipe, RouterModule],
  templateUrl: './wallet.html',
  styleUrl: './wallet.scss'
})
export class WalletComponent implements OnInit {
  private readonly walletService = inject(WalletService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  // User State
  readonly userId = this.authService.getUserId();
  readonly userName = signal(this.authService.getName() || 'Kullanıcı');

  // Component State
  readonly wallet = signal<WalletResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string>('');
  readonly success = signal<string>('');

  // Modals state
  readonly activeModal = signal<'deposit' | 'withdraw' | 'transfer' | null>(null);
  readonly isActionLoading = signal(false);

  // Form Fields
  readonly depositAmount = signal<number | null>(null);
  readonly withdrawAmount = signal<number | null>(null);
  readonly transferAmount = signal<number | null>(null);
  readonly transferDestId = signal<number | null>(null);

  readonly transactions = signal<LocalTransaction[]>([]);

  ngOnInit(): void {
    if (!this.userId) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadWallet();
  }

  loadWallet(): void {
    if (!this.userId) return;
    this.loading.set(true);
    this.error.set('');

    this.walletService.getWalletByUserId(this.userId).subscribe({
      next: (res) => {
        this.wallet.set(res);
        this.loadTransactionHistory(res.id);
        this.loading.set(false);
      },
      error: (err) => {
        // If wallet not found, we can let user create one
        if (err.status === 404) {
          this.wallet.set(null);
        } else {
          this.error.set('Cüzdan bilgileri yüklenirken bir hata oluştu.');
        }
        this.loading.set(false);
      }
    });
  }

  loadTransactionHistory(walletId: number): void {
    this.walletService.getTransactionHistory(walletId, 0, 50).subscribe({
      next: (page) => {
        const mapped = (page.content || []).map((tx: any) => ({
          id: 'TX-' + tx.id,
          title: tx.description,
          amount: tx.amount,
          type: tx.type.toLowerCase() as LocalTransaction['type'],
          date: this.formatDate(tx.createdDate),
          status: 'SUCCESS' as const
        }));
        this.transactions.set(mapped);
      },
      error: (err) => {
        console.error('Failed to load transaction history', err);
      }
    });
  }

  private formatDate(dateStr: string): string {
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString('tr-TR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (e) {
      return dateStr;
    }
  }

  initializeWallet(currency: 'TRY' | 'USD' | 'EUR'): void {
    if (!this.userId) return;
    this.loading.set(true);
    this.error.set('');

    this.walletService.createWallet(this.userId, 0, currency).subscribe({
      next: (res) => {
        this.wallet.set(res);
        this.loading.set(false);
        this.success.set('Cüzdanınız başarıyla oluşturuldu!');
        this.transactions.set([]); // empty history initially
        this.clearMessagesAfterDelay();
      },
      error: (err) => {
        this.error.set(this.getErrorMessage(err) || 'Cüzdan oluşturulurken bir hata oluştu.');
        this.loading.set(false);
      }
    });
  }

  openModal(type: 'deposit' | 'withdraw' | 'transfer'): void {
    this.activeModal.set(type);
    this.depositAmount.set(null);
    this.withdrawAmount.set(null);
    this.transferAmount.set(null);
    this.transferDestId.set(null);
    this.error.set('');
    this.success.set('');
  }

  closeModal(): void {
    this.activeModal.set(null);
  }

  onDepositSubmit(): void {
    const amount = this.depositAmount();
    const w = this.wallet();
    if (!w || !amount || amount <= 0) {
      this.error.set('Lütfen geçerli bir tutar girin.');
      return;
    }

    this.isActionLoading.set(true);
    this.error.set('');

    this.walletService.deposit(w.id, amount).subscribe({
      next: (res) => {
        this.wallet.set(res);
        this.isActionLoading.set(false);
        this.success.set(`${amount} ${w.currency} başarıyla yatırıldı.`);
        
        // Refresh transaction history
        this.loadTransactionHistory(w.id);

        setTimeout(() => this.closeModal(), 1500);
        this.clearMessagesAfterDelay();
      },
      error: (err) => {
        this.error.set(this.getErrorMessage(err) || 'Yatırma işlemi gerçekleştirilemedi.');
        this.isActionLoading.set(false);
      }
    });
  }

  onWithdrawSubmit(): void {
    const amount = this.withdrawAmount();
    const w = this.wallet();
    if (!w || !amount || amount <= 0) {
      this.error.set('Lütfen geçerli bir tutar girin.');
      return;
    }

    if (w.balance < amount) {
      this.error.set('Cüzdanınızda yeterli bakiye bulunmamaktadır.');
      return;
    }

    this.isActionLoading.set(true);
    this.error.set('');

    this.walletService.withdraw(w.id, amount).subscribe({
      next: (res) => {
        this.wallet.set(res);
        this.isActionLoading.set(false);
        this.success.set(`${amount} ${w.currency} başarıyla çekildi.`);

        // Refresh transaction history
        this.loadTransactionHistory(w.id);

        setTimeout(() => this.closeModal(), 1500);
        this.clearMessagesAfterDelay();
      },
      error: (err) => {
        this.error.set(this.getErrorMessage(err) || 'Çekme işlemi gerçekleştirilemedi.');
        this.isActionLoading.set(false);
      }
    });
  }

  onTransferSubmit(): void {
    const amount = this.transferAmount();
    const destId = this.transferDestId();
    const w = this.wallet();

    if (!w || !amount || amount <= 0 || !destId) {
      this.error.set('Lütfen tüm alanları doğru şekilde doldurun.');
      return;
    }

    if (w.id === destId) {
      this.error.set('Kendi cüzdanınıza transfer yapamazsınız.');
      return;
    }

    if (w.balance < amount) {
      this.error.set('Cüzdanınızda yeterli bakiye bulunmamaktadır.');
      return;
    }

    this.isActionLoading.set(true);
    this.error.set('');

    this.walletService.transfer(w.id, destId, amount).subscribe({
      next: () => {
        // Fetch updated wallet balance
        this.walletService.getWalletByUserId(this.userId!).subscribe(updated => {
          this.wallet.set(updated);
        });

        this.isActionLoading.set(false);
        this.success.set(`${amount} ${w.currency} başarıyla Cüzdan ID: ${destId} hesabına gönderildi.`);

        // Refresh transaction history
        this.loadTransactionHistory(w.id);

        setTimeout(() => this.closeModal(), 1500);
        this.clearMessagesAfterDelay();
      },
      error: (err) => {
        this.error.set(this.getErrorMessage(err) || 'Transfer gerçekleştirilemedi. Alıcı cüzdan ID geçerli olmayabilir.');
        this.isActionLoading.set(false);
      }
    });
  }

  private clearMessagesAfterDelay(): void {
    setTimeout(() => {
      this.success.set('');
      this.error.set('');
    }, 5000);
  }

  logout(): void {
    this.authService.clearSession();
    this.router.navigate(['/login']);
  }

  // getErrorMessage: Sunucu hatalarını son kullanıcıya yönelik şık ve anlaşılır mesajlara dönüştürür.
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
    
    // Sunucu kapalı veya internet yok (Status 0 veya Failed to fetch)
    if (err.status === 0 || lowerMsg.includes('failed to fetch') || lowerMsg.includes('unknown error')) {
      return 'Sunucuyla bağlantı kurulamadı. Lütfen internet bağlantınızı kontrol edin veya daha sonra tekrar deneyin.';
    }
    
    // Mükerrer / Zaten kayıtlı bilgileri
    if (lowerMsg.includes('duplicate key') || lowerMsg.includes('incorrectresultsizedataaccessexception') || lowerMsg.includes('already exists')) {
      return 'Bu işlem gerçekleştirilemedi, girdiğiniz benzersiz bilgiler sistemde zaten mevcut.';
    }
    
    // Yetki hataları
    if (lowerMsg.includes('bad credentials') || lowerMsg.includes('unauthorized') || lowerMsg.includes('401')) {
      return 'Bu işlemi gerçekleştirmek için yetkiniz bulunmamaktadır.';
    }
    
    // Veritabanı ve diğer genel 500 sunucu hataları
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
}
