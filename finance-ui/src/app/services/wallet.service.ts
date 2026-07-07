import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface WalletResponse {
  id: number;
  userId: number;
  balance: number;
  currency: 'TRY' | 'USD' | 'EUR';
  status: 'ACTIVE' | 'PENDING' | 'BLOCKED';
  createdDate: string;
  updatedDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class WalletService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly baseUrl = '/wallets/api/v1';

  private getAuthHeaders(): { headers: HttpHeaders } {
    const token = this.authService.getToken();
    return {
      headers: new HttpHeaders({
        'Authorization': `Bearer ${token}`
      })
    };
  }

  getWalletByUserId(userId: number): Observable<WalletResponse> {
    return this.http.get<WalletResponse>(`${this.baseUrl}/user/${userId}`, this.getAuthHeaders());
  }

  createWallet(userId: number, initialBalance: number = 0, currency: string = 'TRY', status: string = 'ACTIVE'): Observable<WalletResponse> {
    const body = {
      userId,
      balance: initialBalance,
      currency,
      status
    };
    return this.http.post<WalletResponse>(`${this.baseUrl}/create`, body, this.getAuthHeaders());
  }

  deposit(walletId: number, amount: number): Observable<WalletResponse> {
    return this.http.post<WalletResponse>(`${this.baseUrl}/${walletId}/deposit?amount=${amount}`, {}, this.getAuthHeaders());
  }

  withdraw(walletId: number, amount: number): Observable<WalletResponse> {
    return this.http.post<WalletResponse>(`${this.baseUrl}/${walletId}/withdraw?amount=${amount}`, {}, this.getAuthHeaders());
  }

  transfer(sourceWalletId: number, destinationWalletId: number, amount: number): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}/transfer?sourceWalletId=${sourceWalletId}&destinationWalletId=${destinationWalletId}&amount=${amount}`,
      {},
      this.getAuthHeaders()
    );
  }

  getTransactionHistory(walletId: number, page: number = 0, size: number = 20): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${walletId}/history?page=${page}&size=${size}`, this.getAuthHeaders());
  }
}