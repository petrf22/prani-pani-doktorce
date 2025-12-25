import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { UserService } from './services/user-service';

export const authGuard: CanActivateFn = (route, state) => {
  const userService = inject(UserService);
  const router = inject(Router);
  const authenticated = userService.isAuthenticated();

  if (!authenticated) {
      router.navigate(['/welcome'], { replaceUrl: true });
  }

  return authenticated;
};
