import { Routes } from '@angular/router';
import { Welcome } from './welcome/welcome';
import { VerifyTokenComponent } from './verify-token/verify-token.component';
import { PraniFormComponent } from './prani-form/prani-form.component';
import { authGuard } from './auth-guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: '/welcome' },
  { path: 'welcome', component: Welcome },
  { path: 'verify-token/:emailToken', component: VerifyTokenComponent },
  { path: 'prani', component: PraniFormComponent, canActivate: [authGuard]},
];
