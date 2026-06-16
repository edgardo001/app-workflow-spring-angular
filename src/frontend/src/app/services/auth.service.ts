import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

export interface User {
  id: string;
  name: string;
  email: string;
  avatar: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly userSignal = signal<User | null>(null);
  readonly user = this.userSignal.asReadonly();
  private readonly tokenSignal = signal<string | null>(localStorage.getItem('auth_token'));
  readonly token = this.tokenSignal.asReadonly();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    if (this.tokenSignal()) {
      this.fetchProfile().subscribe();
    }
  }

  get isAuthenticated(): boolean {
    return !!this.tokenSignal();
  }

  loginWithGitHub(): void {
    window.location.href = '/api/auth/github';
  }

  handleOAuthCallback(code: string): Observable<{ token: string; user: User }> {
    return this.http.post<{ token: string; user: User }>('/api/auth/github/callback', { code }).pipe(
      tap(res => {
        localStorage.setItem('auth_token', res.token);
        this.tokenSignal.set(res.token);
        this.userSignal.set(res.user);
      })
    );
  }

  fetchProfile(): Observable<User> {
    return this.http.get<User>('/api/auth/profile').pipe(
      tap(user => this.userSignal.set(user))
    );
  }

  logout(): void {
    localStorage.removeItem('auth_token');
    this.tokenSignal.set(null);
    this.userSignal.set(null);
    this.router.navigate(['/login']);
  }
}
