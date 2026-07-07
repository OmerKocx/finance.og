import { Routes } from '@angular/router';
import { AuthComponent } from './auth/auth';
import { DashboardComponent } from './dashboard/dashboard';
import { WalletComponent } from './wallet/wallet';

export const routes: Routes = [
  { path: 'login', component: AuthComponent },
  
  { path: 'dashboard', component: DashboardComponent },

  { path: 'wallet', component: WalletComponent },
  
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  
  { path: '**', redirectTo: 'login' }
];