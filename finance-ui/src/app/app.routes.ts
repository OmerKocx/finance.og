import { Routes } from '@angular/router';
import { AuthComponent } from './auth/auth';
import { DashboardComponent } from './dashboard/dashboard';

// Routes: Hangi URL yolunun (path) tarayıcıda hangi bileşeni (sayfayı) yükleyeceğini tanımlayan liste.
export const routes: Routes = [
  // http://localhost:4201/login girilirse Giriş/Kayıt sayfasını aç
  { path: 'login', component: AuthComponent },
  
  // http://localhost:4201/dashboard girilirse Finansal Paneli aç
  { path: 'dashboard', component: DashboardComponent },
  
  // Site ilk açıldığında (boş adreste) otomatik olarak /login sayfasına yönlendir
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  
  // Tanımsız herhangi bir adres yazılırsa (404 yerine) yine /login sayfasına yönlendir
  { path: '**', redirectTo: 'login' }
];
