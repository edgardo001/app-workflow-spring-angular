import { Component, signal, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, NavigationEnd, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map } from 'rxjs';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
  <div class="app-shell">
    <aside class="sidebar" [class.closed]="!sidebarOpen()" role="navigation" aria-label="Navegación principal">
      <div class="sidebar-header">
        <a routerLink="/dashboard" class="logo-icon">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </a>
        <span>app-workflow</span>
      </div>
      @if (user(); as u) {
        <div class="sidebar-user">
          <div class="avatar">{{ u.name.charAt(0) }}{{ u.name.split(' ').pop()?.charAt(0) }}</div>
          <div class="user-info">
            <div class="name">{{ u.name }}</div>
            <div class="role">{{ u.role }}</div>
          </div>
        </div>
      }
      <div class="sidebar-nav">
        <div class="nav-section">Navegación</div>
        <a routerLink="/dashboard" class="nav-item" routerLinkActive="active" (click)="sidebarOpen.set(false)">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" /></svg>
          Dashboard
        </a>
        <a routerLink="/flows" class="nav-item" routerLinkActive="active" (click)="sidebarOpen.set(false)">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" /></svg>
          Mis Flujos
        </a>
        <a routerLink="/pending" class="nav-item" routerLinkActive="active" (click)="sidebarOpen.set(false)">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
          Pendientes
        </a>
        <div class="nav-section">Administración</div>
        <a routerLink="/flows/new" class="nav-item" routerLinkActive="active" (click)="sidebarOpen.set(false)">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
          Nuevo Flujo
        </a>
        <a routerLink="/users" class="nav-item" routerLinkActive="active" (click)="sidebarOpen.set(false)">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" /></svg>
          Usuarios
        </a>
      </div>
      <div class="sidebar-foot">
        <button (click)="auth.logout()" class="nav-item">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" /></svg>
          Cerrar sesión
        </button>
      </div>
    </aside>

    <div class="main-area">
      <header class="topbar" role="banner" aria-label="Barra superior">
        <div class="flex-row">
          <button (click)="sidebarOpen.set(!sidebarOpen())" class="menu-btn" aria-label="Abrir o cerrar menú lateral">
            <svg width="22" height="22" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" /></svg>
          </button>
          <span class="breadcrumb">app-workflow / <strong>{{ breadcrumb() }}</strong></span>
        </div>
        <div class="topbar-actions">
          <button (click)="toggleTheme()" class="theme-toggle-app" [attr.aria-label]="'Cambiar a tema ' + (theme() === 'dark' ? 'claro' : 'oscuro')">
            <svg class="moon-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"/></svg>
            <svg class="sun-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
          </button>
          <button class="notif-btn" aria-label="Notificaciones">
            <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /></svg>
            <span class="dot" aria-hidden="true"></span>
          </button>
          @if (user(); as u) {
            <div class="top-avatar">{{ u.name.charAt(0) }}{{ u.name.split(' ').pop()?.charAt(0) }}</div>
          }
        </div>
      </header>

      <div class="page-content" role="main">
        <div class="page-inner">
          <router-outlet />
        </div>
      </div>
    </div>
  </div>

  <div class="toast-container" #toastContainer></div>
  `,
  styles: [':host { display: contents; }']
})
export class ShellComponent {
  auth = inject(AuthService);
  private router = inject(Router);

  user = this.auth.user;
  sidebarOpen = signal(false);
  theme = signal(document.documentElement.getAttribute('data-theme') || 'light');
  breadcrumb = toSignal(
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      map(() => {
        const url = this.router.url.split('/').pop() || '';
        const labels: Record<string, string> = { dashboard: 'Dashboard', 'new': 'Nuevo Flujo', flows: 'Mis Flujos', pending: 'Pendientes', users: 'Usuarios' };
        return labels[url] || 'Dashboard';
      })
    ),
    { initialValue: 'Dashboard' }
  );

  toggleTheme(): void {
    const next = this.theme() === 'dark' ? 'light' : 'dark';
    this.theme.set(next);
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('theme', next);
  }
}
