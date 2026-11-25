import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const stored = localStorage.getItem('hrgenius_auth_user');
  if (stored) {
    try {
      const user = JSON.parse(stored);
      if (user && user.username) {
        const headers: Record<string, string> = {
          'X-Auth-Username': user.username,
          'X-Auth-Role': user.role || 'Employee'
        };
        if (user.token) {
          headers['Authorization'] = `Bearer ${user.token}`;
        }
        const cloned = req.clone({ setHeaders: headers });
        return next(cloned);
      }
    } catch {
      // Continue with original request if storage parsing fails
    }
  }
  return next(req);
};
