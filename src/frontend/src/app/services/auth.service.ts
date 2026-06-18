import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, of, map, catchError } from 'rxjs';

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

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.checkAuth().subscribe();
  }

  get isAuthenticated(): boolean {
    return this.userSignal() !== null;
  }

  checkAuth(): Observable<boolean> {
    if (this.userSignal()) return of(true);
    return this.fetchProfile().pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  loginWithGitHub(): void {
    window.location.href = '/api/auth/github';
  }

  fetchProfile(): Observable<User> {
    return this.http.get<User>('/api/auth/profile').pipe(
      tap(user => this.userSignal.set(user))
    );
  }

  logout(): void {
    this.http.post('/api/auth/logout', {}).subscribe({
      next: () => {
        this.userSignal.set(null);
        localStorage.removeItem('auth_token');
        this.router.navigate(['/login']);
      },
      error: () => {
        this.userSignal.set(null);
        localStorage.removeItem('auth_token');
        this.router.navigate(['/login']);
      }
    });
  }
}
