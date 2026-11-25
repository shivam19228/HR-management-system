import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  // Prevent back-button or direct URL access after logout
  router.navigate(['/login'], {
    queryParams: { returnUrl: state.url },
    replaceUrl: true
  });
  return false;
};
