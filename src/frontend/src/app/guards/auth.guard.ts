import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

import { map, catchError, of } from 'rxjs';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.checkAuth().pipe(
    map(isAuthenticated => {
      if (isAuthenticated) return true;
      return router.parseUrl('/login');
    }),
    catchError(() => of(router.parseUrl('/login')))
  );
};
