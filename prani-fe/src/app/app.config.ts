import { ApplicationConfig, provideAppInitializer, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { icons } from './icons-provider';
import { provideNzIcons } from 'ng-zorro-antd/icon';
import { cs_CZ, provideNzI18n } from 'ng-zorro-antd/i18n';
import { registerLocaleData } from '@angular/common';
import cs from '@angular/common/locales/cs';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { tokenInterceptor } from './func/token-func';
import { authInitializer } from './func/auth-initializer';

registerLocaleData(cs);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes), provideNzIcons(icons), provideNzI18n(cs_CZ), provideAnimationsAsync(), provideHttpClient(),
    provideHttpClient(withInterceptors([tokenInterceptor])),
    provideAppInitializer(authInitializer()),
  ]
};
