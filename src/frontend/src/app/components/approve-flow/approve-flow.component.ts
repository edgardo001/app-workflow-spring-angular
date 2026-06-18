import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FlowService, Flow } from '../../services/flow.service';
import { DocumentService } from '../../services/document.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-approve-flow',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
  <div class="pending-container" style="max-width: 800px; margin: 40px auto; padding: 0 20px;">
    <div class="page-header mb-lg">
      <div>
        <a routerLink="/dashboard" class="btn btn-ghost pl-0 mb-xs">
          <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" /></svg>
          Ir al Panel
        </a>
        <h1>Revisión de Flujo de Aprobación</h1>
        <p>Valida los documentos y firma electrónicamente</p>
      </div>
    </div>

    @if (loading()) {
      <div class="empty-state">
        <p>Cargando información del flujo...</p>
      </div>
    } @else if (error()) {
      <div class="empty-state text-danger">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true" style="color: var(--danger)"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
        <p>{{ error() }}</p>
      </div>
    } @else if (flow(); as f) {
      <div class="pending-card">
        <div class="pending-header">
          <div class="alert-banner">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <span>Enlace de revisión JWS verificado</span>
          </div>
          <h2>{{ f.title }}</h2>
          <div class="meta-row">
            <span>Remitente: {{ f.ownerEmail }}</span>
            <span>Límite: {{ f.deadline }}</span>
          </div>
        </div>

        <div class="doc-list">
          @for (doc of f.documents; track doc.id) {
            <div class="doc-item">
              <div class="doc-info">
                <div class="doc-icon" [class.pdf]="doc.type==='pdf'" [class.doc]="doc.type==='doc'">
                  <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" /></svg>
                </div>
                <div>
                  <div class="doc-name">{{ doc.name }}</div>
                  <div class="doc-size">{{ doc.size }}</div>
                </div>
              </div>
              <a [href]="getDownloadUrl(doc.id)" class="btn btn-ghost btn-sm">Descargar</a>
            </div>
          }
        </div>

        <div class="pending-body">
          <p class="text-sm fw-medium mb-lg">Paso {{ f.step + 1 }} de {{ f.totalSteps }}</p>

          <div class="stepper">
            <div class="stepper-track">
              <div class="stepper-fill" [style.width.%]="f.totalSteps > 1 ? (f.step / (f.totalSteps - 1)) * 100 : 100"></div>
            </div>
            @for (p of f.participants; track p.email) {
              <div class="step-dot">
                @if (p.status === 'approved') {
                  <div class="dot done">
                    <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                  </div>
                  <span class="dot-label done-label">{{ p.name }}</span>
                } @else if (p.stepOrder === f.step) {
                  <div class="dot current">{{ p.stepOrder + 1 }}</div>
                  <span class="dot-label current-label">{{ p.name }}</span>
                } @else {
                  <div class="dot">{{ p.stepOrder + 1 }}</div>
                  <span class="dot-label">{{ p.name }}</span>
                }
              </div>
            }
          </div>

          @if (!authService.isAuthenticated) {
            <div class="approve-area" style="text-align: center; border-color: var(--warning);">
              <p class="mb-md text-sm text-muted">Debes iniciar sesión con GitHub para firmar o rechazar este flujo de aprobación.</p>
              <button (click)="login()" class="btn btn-primary">
                Iniciar sesión con GitHub
              </button>
            </div>
          } @else {
            @if (emailMismatch()) {
              <div class="approve-area" style="border-color: var(--danger); text-align: center; padding: 24px;">
                <div class="alert-banner mb-md" style="background-color: #fef2f2; border: 1px solid #f87171; color: #991b1b; border-radius: 8px; padding: 12px; text-align: left; display: flex; align-items: flex-start; gap: 8px;">
                  <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true" style="color: #991b1b; flex-shrink: 0; margin-top: 2px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
                  <span><strong>Acceso Denegado:</strong> Este paso de aprobación está asignado a <strong>{{ currentStepEmail() }}</strong>, pero has iniciado sesión como <strong>{{ authService.user()?.email }}</strong>. No tienes permisos para firmar con esta cuenta.</span>
                </div>
                <p class="mb-md text-sm text-muted">Para proceder con la firma, debes cerrar la sesión actual e iniciarla con la cuenta correcta.</p>
                <button (click)="logoutAndRedirect()" class="btn btn-outline btn-danger-ghost">
                  Cerrar sesión y cambiar de cuenta
                </button>
              </div>
            } @else {
              <div class="approve-area">
                <div class="checkbox-card" [class.checked]="termsAccepted()">
                  <input type="checkbox" [(ngModel)]="termsAccepted" id="terms">
                  <label for="terms">He revisado los documentos adjuntos y apruebo este flujo.</label>
                </div>
                <div class="flex-wrap">
                  <button (click)="approve()" [disabled]="!termsAccepted()" class="btn btn-success flex-1">
                    <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                    APROBAR
                  </button>
                  <button (click)="reject()" class="btn btn-outline btn-danger-ghost">Rechazar</button>
                </div>
              </div>
            }
          }
        </div>
      </div>
    }
  </div>
  `,
  styles: []
})
export class ApproveFlowComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private flowService = inject(FlowService);
  private documentService = inject(DocumentService);
  public authService = inject(AuthService);

  token = signal<string>('');
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  flow = signal<Flow | null>(null);
  termsAccepted = signal<boolean>(false);

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const tok = params['token'];
      if (!tok) {
        this.error.set('Enlace inválido: falta el token de aprobación.');
        this.loading.set(false);
        return;
      }
      this.token.set(tok);
      this.loadFlow(tok);
    });
  }

  loadFlow(tok: string): void {
    this.flowService.verifyToken(tok).subscribe({
      next: (f) => {
        this.flow.set(f);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('No se pudo verificar el token. Es posible que haya expirado o sea inválido.');
        this.loading.set(false);
      }
    });
  }

  getDownloadUrl(docId: string): string {
    return this.documentService.getDownloadUrl(docId);
  }

  currentStepEmail(): string {
    const f = this.flow();
    if (!f || f.step === undefined) return '';
    return f.participants[f.step]?.email || '';
  }

  emailMismatch(): boolean {
    const authUser = this.authService.user();
    if (!authUser) return false;
    const stepEmail = this.currentStepEmail();
    return stepEmail !== '' && authUser.email.toLowerCase() !== stepEmail.toLowerCase();
  }

  login(): void {
    // Save token in localStorage so we can redirect back after authentication
    localStorage.setItem('redirect_after_login', window.location.pathname + window.location.search);
    this.authService.loginWithGitHub();
  }

  logoutAndRedirect(): void {
    localStorage.setItem('redirect_after_login', window.location.pathname + window.location.search);
    this.authService.logout();
  }

  approve(): void {
    if (!this.termsAccepted()) return;
    const f = this.flow();
    if (!f) return;
    this.flowService.approveFlow(f.id, this.token()).subscribe({
      next: () => {
        this.showToast('Documento aprobado correctamente.', 'success');
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.showToast('Error al aprobar el documento.', 'error');
      }
    });
  }

  reject(): void {
    const f = this.flow();
    if (!f) return;
    const reason = prompt('Motivo del rechazo:');
    if (reason === null) return;
    this.flowService.rejectFlow(f.id, reason, this.token()).subscribe({
      next: () => {
        this.showToast('Flujo rechazado correctamente.', 'error');
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.showToast('Error al rechazar el documento.', 'error');
      }
    });
  }

  private showToast(msg: string, type: 'success' | 'error' | 'info'): void {
    const container = document.querySelector('.toast-container');
    if (container) {
      const toast = document.createElement('div');
      toast.className = `toast toast-${type}`;
      toast.textContent = msg;
      container.appendChild(toast);
      setTimeout(() => toast.remove(), 3000);
    }
  }
}
