
import { inject } from '@angular/core';
import { UserService } from '../services/user-service';
import { catchError, Observable } from 'rxjs';

export function authInitializer(): () => void | Observable<unknown> | Promise<unknown> {
  return () => {
    const user = inject(UserService);
    return user.refreshToken().pipe(catchError((error) => {
      console.error('Auth Initializer :: Token refresh failed:', error);
      return [];
    }));
  };
}