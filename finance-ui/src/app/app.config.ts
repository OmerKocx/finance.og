import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

import { routes } from './app.routes';

// ApplicationConfig: Tüm uygulamanın global ayarlarını ve bağımlılıklarını barındıran nesne.
export const appConfig: ApplicationConfig = {
  providers: [
    // Tarayıcıdaki global Javascript hatalarını yakalayan Angular dinleyicisi
    provideBrowserGlobalErrorListeners(),
    
    // Uygulamanın sayfa rotalarını (yönlendirmelerini) aktif eder
    provideRouter(routes),
    
    // HTTP istekleri (GET, POST vs.) atabilmemiz için gerekli istemciyi aktif eder
    provideHttpClient()
  ]
};
