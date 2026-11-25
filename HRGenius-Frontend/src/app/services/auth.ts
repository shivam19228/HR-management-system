import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject, map, catchError, throwError, of } from 'rxjs';
import { environment } from '../../environments/environment';

export type UserRole = 'ADMIN' | 'HR_MANAGER' | 'EMPLOYEE';

export function normalizeRole(role: string | undefined | null): UserRole {
  if (!role) return 'EMPLOYEE';
  const r = role.trim().toUpperCase();
  if (r === 'ADMIN' || r === 'HR ADMINISTRATOR' || r === 'HR_ADMIN' || r === 'ADMINISTRATOR') {
    return 'ADMIN';
  }
  if (r === 'HR MANAGER' || r === 'HR_MANAGER' || r === 'MANAGER' || r === 'RECRUITER' || r === 'HR LEAD') {
    return 'HR_MANAGER';
  }
  return 'EMPLOYEE';
}

export interface UserSession {
  id?: number;
  username: string;
  name: string;
  email: string;
  role: string;
  avatar: string;
  empId?: string;
  employeeId?: number;
  token?: string;
}
export type User = UserSession;

export interface BackendUserResponse {
  id: number;
  username: string;
  employee: string;
  email: string;
  role: string;
  status: string;
  empId?: string;
  employeeId?: number;
  token?: string;
}

export interface ProfileResponse {
  id: number;
  username: string;
  name: string;
  email: string;
  role: string;
  status: string;
  empId?: string;
  employeeId?: number;
  department?: string;
  departmentId?: number;
  job?: string;
  designation?: string;
  phone?: string;
  joiningDate?: string;
  salary?: number;
  gender?: string;
  employmentType?: string;
  city?: string;
  address?: string;
  avatar?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly currentUserKey = 'hrgenius_auth_user';
  private currentUser: UserSession | null = null;
  private readonly apiUrl = environment.apiUrl;

  private currentUserSubject = new BehaviorSubject<UserSession | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.loadUser();
  }

  private loadUser(): void {
    try {
      const stored = localStorage.getItem(this.currentUserKey);
      if (stored) {
        this.currentUser = JSON.parse(stored);
      } else {
        this.currentUser = null;
      }
    } catch {
      this.currentUser = null;
    }
    this.currentUserSubject.next(this.currentUser);
  }

  getCurrentUser(): UserSession | null {
    if (!this.currentUser) {
      const stored = localStorage.getItem(this.currentUserKey);
      if (stored) {
        try {
          this.currentUser = JSON.parse(stored);
          this.currentUserSubject.next(this.currentUser);
        } catch {
          this.currentUser = null;
        }
      }
    }
    return this.currentUser;
  }

  getRole(): UserRole {
    const user = this.getCurrentUser();
    return normalizeRole(user?.role);
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  isManager(): boolean {
    return this.getRole() === 'HR_MANAGER';
  }

  isEmployee(): boolean {
    return this.getRole() === 'EMPLOYEE';
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(this.currentUserKey);
  }

  login(username: string, password?: string): Observable<UserSession> {
    return this.http.post<BackendUserResponse>(`${this.apiUrl}/auth/login`, {
      username: username.trim(),
      password: password || ''
    }).pipe(
      map(res => {
        const name = res.employee || res.username || 'User';
        const initials = name.split(' ').map(n => n[0]).join('').toUpperCase() || 'US';
        const session: UserSession = {
          id: res.id,
          username: res.username,
          name: name,
          email: res.email,
          role: res.role,
          avatar: initials,
          empId: res.empId,
          employeeId: res.employeeId,
          token: res.token
        };
        this.currentUser = session;
        localStorage.setItem(this.currentUserKey, JSON.stringify(session));
        this.currentUserSubject.next(session);
        return session;
      }),
      catchError(err => throwError(() => err))
    );
  }

  register(userData: { name: string; username: string; email: string; password?: string; role: string }): Observable<UserSession> {
    return this.http.post<BackendUserResponse>(`${this.apiUrl}/auth/register`, {
      name: userData.name,
      username: userData.username,
      email: userData.email,
      password: userData.password || 'password123',
      role: userData.role
    }).pipe(
      map(res => {
        const name = res.employee || userData.name;
        const initials = name.split(' ').map(n => n[0]).join('').toUpperCase() || 'US';
        const session: UserSession = {
          id: res.id,
          username: res.username,
          name: name,
          email: res.email,
          role: res.role,
          avatar: initials,
          empId: res.empId,
          employeeId: res.employeeId,
          token: res.token
        };
        this.currentUser = session;
        localStorage.setItem(this.currentUserKey, JSON.stringify(session));
        this.currentUserSubject.next(session);
        return session;
      }),
      catchError(err => throwError(() => err))
    );
  }

  getProfile(): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.apiUrl}/profile`).pipe(
      catchError(() => {
        return this.http.get<ProfileResponse>(`${this.apiUrl}/auth/profile`);
      })
    );
  }

  logout(): void {
    // Notify backend to invalidate any active session
    this.http.post(`${this.apiUrl}/auth/logout`, {}).pipe(
      catchError(() => of(null))
    ).subscribe();

    // Clear all client auth state
    localStorage.removeItem(this.currentUserKey);
    try {
      sessionStorage.clear();
    } catch {
      // ignore
    }

    this.currentUser = null;
    this.currentUserSubject.next(null);

    // Redirect to login replacing current history entry
    this.router.navigate(['/login'], { replaceUrl: true });
  }
}
