import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { AuthService } from '../services/auth.service';
import { WalletService } from '../services/wallet.service';

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
  imports: [DecimalPipe, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class DashboardComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly walletService = inject(WalletService);

  readonly userId = this.authService.getUserId();
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

  readonly balance = signal(0.00);
  readonly income = signal(0.00);
  readonly expenses = signal(0.00);
  readonly currency = signal('₺');

  readonly transactions = signal<Transaction[]>([]);

  ngOnInit(): void {
    if (!this.userId) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    this.walletService.getWalletByUserId(this.userId!).subscribe({
      next: (wallet) => {
        this.balance.set(wallet.balance);
        this.currency.set(this.getCurrencySymbol(wallet.currency));
        this.loadTransactions(wallet.id);
      },
      error: (err) => {
        console.error('Failed to load wallet on dashboard', err);
      }
    });
  }

  private loadTransactions(walletId: number): void {
    this.walletService.getTransactionHistory(walletId, 0, 10).subscribe({
      next: (page) => {
        const mapped: Transaction[] = (page.content || []).map((tx: any) => ({
          id: tx.id.toString(),
          title: tx.description,
          category: this.getCategoryLabel(tx.type),
          amount: tx.amount,
          type: (tx.type === 'DEPOSIT' || tx.type === 'TRANSFER_IN') ? 'income' : 'expense',
          date: this.formatDate(tx.createdDate)
        }));
        this.transactions.set(mapped);

        const inc = mapped.filter(t => t.type === 'income').reduce((sum, t) => sum + t.amount, 0);
        const exp = mapped.filter(t => t.type === 'expense').reduce((sum, t) => sum + t.amount, 0);
        this.income.set(inc);
        this.expenses.set(exp);
      },
      error: (err) => {
        console.error('Failed to load dashboard transactions', err);
      }
    });
  }

  private getCurrencySymbol(code: string): string {
    switch (code) {
      case 'TRY': return '₺';
      case 'USD': return '$';
      case 'EUR': return '€';
      default: return '₺';
    }
  }

  private getCategoryLabel(type: string): string {
    switch (type) {
      case 'DEPOSIT': return 'Para Yatırma';
      case 'WITHDRAW': return 'Para Çekme';
      case 'TRANSFER_IN': return 'Gelen Havale';
      case 'TRANSFER_OUT': return 'Giden Havale';
      default: return 'Cüzdan Hareketi';
    }
  }

  private formatDate(dateStr: string): string {
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString('tr-TR', {
        day: '2-digit',
        month: 'short'
      });
    } catch (e) {
      return dateStr;
    }
  }

  logout(): void {
    this.authService.clearSession();
    this.router.navigate(['/login']);
  }
}