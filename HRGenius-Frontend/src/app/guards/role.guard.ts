import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    router.navigate(['/login'], {
      queryParams: { returnUrl: state.url },
      replaceUrl: true
    });
    return false;
  }

  const expectedRoles = route.data?.['roles'] as string[] | undefined;
  if (!expectedRoles || expectedRoles.length === 0) {
    return true;
  }

  const currentRole = authService.getRole();
  if (expectedRoles.includes(currentRole)) {
    return true;
  }

  // If role is unauthorized, redirect to their role-appropriate dashboard
  router.navigate(['/dashboard'], { replaceUrl: true });
  return false;
};
