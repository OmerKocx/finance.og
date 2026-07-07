import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AuthResponse {
  token: string;
  email: string;
  name: string;
  userId: number;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly baseUrl = '/auth';

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, { email, password });
  }

  register(email: string, password: string, name: string, phone: string, role: string = 'USER'): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, { 
      email, 
      password, 
      fullName: name, 
      phoneNumber: phone, 
      role 
    });
  }

  saveSession(authData: AuthResponse): void {
    localStorage.setItem('auth_token', authData.token);
    localStorage.setItem('auth_email', authData.email);
    localStorage.setItem('auth_name', authData.name || '');
    localStorage.setItem('auth_user_id', authData.userId ? authData.userId.toString() : '');
  }

  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  getEmail(): string | null {
    return localStorage.getItem('auth_email');
  }

  getName(): string | null {
    return localStorage.getItem('auth_name');
  }

  getUserId(): number | null {
    const id = localStorage.getItem('auth_user_id');
    return id ? parseInt(id, 10) : null;
  }

  clearSession(): void {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_email');
    localStorage.removeItem('auth_name');
    localStorage.removeItem('auth_user_id');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}