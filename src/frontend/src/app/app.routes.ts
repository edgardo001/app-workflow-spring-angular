import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'landing',
    loadComponent: () => import('./components/landing/landing.component').then(m => m.LandingComponent)
  },
  {
    path: 'approve',
    loadComponent: () => import('./components/approve-flow/approve-flow.component').then(m => m.ApproveFlowComponent)
  },
  {
    path: '',
    loadComponent: () => import('./components/shell/shell.component').then(m => m.ShellComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'flows',
        loadComponent: () => import('./components/flows/flows.component').then(m => m.FlowsComponent)
      },
      {
        path: 'flows/new',
        loadComponent: () => import('./components/new-flow/new-flow.component').then(m => m.NewFlowComponent)
      },
      {
        path: 'flows/:id',
        loadComponent: () => import('./components/flow-detail/flow-detail.component').then(m => m.FlowDetailComponent)
      },
      {
        path: 'pending',
        loadComponent: () => import('./components/pending-flows/pending-flows.component').then(m => m.PendingFlowsComponent)
      },
      {
        path: 'users',
        loadComponent: () => import('./components/users/users.component').then(m => m.UsersComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
