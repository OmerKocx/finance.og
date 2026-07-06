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

  // Local/Mock Transactions (dynamically added to during this session)
  readonly transactions = signal<LocalTransaction[]>([
    { id: 'TX-9021', title: 'İlk Cüzdan Açılış Bonusu', amount: 100.00, type: 'deposit', date: 'Bugün', status: 'SUCCESS' },
    { id: 'TX-7643', title: 'Market Gideri (Mock)', amount: 120.50, type: 'withdraw', date: 'Dün', status: 'SUCCESS' }
  ]);

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

  initializeWallet(currency: 'TRY' | 'USD' | 'EUR'): void {
    if (!this.userId) return;
    this.loading.set(true);
    this.error.set('');

    this.walletService.createWallet(this.userId, 100, currency).subscribe({
      next: (res) => {
        this.wallet.set(res);
        this.loading.set(false);
        this.success.set('Cüzdanınız başarıyla oluşturuldu ve 100 birim hoş geldin hediyesi tanımlandı!');
        this.clearMessagesAfterDelay();
      },
      error: (err) => {
        this.error.set('Cüzdan oluşturulurken bir hata oluştu.');
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
        
        // Add to local history
        this.addLocalTransaction('Para Yatırma', amount, 'deposit');

        setTimeout(() => this.closeModal(), 1500);
        this.clearMessagesAfterDelay();
      },
      error: (err) => {
        this.error.set('Yatırma işlemi gerçekleştirilemedi.');
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

        // Add to local history
        this.addLocalTransaction('Para Çekme', amount, 'withdraw');

        setTimeout(() => this.closeModal(), 1500);
        this.clearMessagesAfterDelay();
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Çekme işlemi gerçekleştirilemedi.');
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

        // Add to local history
        this.addLocalTransaction(`Gönderilen Havale (ID: ${destId})`, amount, 'transfer_out');

        setTimeout(() => this.closeModal(), 1500);
        this.clearMessagesAfterDelay();
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Transfer gerçekleştirilemedi. Alıcı cüzdan ID geçerli olmayabilir.');
        this.isActionLoading.set(false);
      }
    });
  }

  private addLocalTransaction(title: string, amount: number, type: LocalTransaction['type']): void {
    const newTx: LocalTransaction = {
      id: 'TX-' + Math.floor(1000 + Math.random() * 9000),
      title,
      amount,
      type,
      date: 'Şimdi',
      status: 'SUCCESS'
    };
    this.transactions.update(list => [newTx, ...list]);
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
}
