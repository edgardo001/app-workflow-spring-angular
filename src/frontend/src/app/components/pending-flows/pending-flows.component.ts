import { Component, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FlowService } from '../../services/flow.service';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-pending-flows',
  imports: [FormsModule],
  template: `
  <div class="pending-container">
    <div class="page-header">
      <div>
        <h1>Tienes documentos pendientes</h1>
        <p>Revisión y firma requerida</p>
      </div>
    </div>

    @if (flow(); as f) {
      <div class="pending-card">
        <div class="pending-header">
          <div class="alert-banner">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
            <span>Tienes un documento pendiente de aprobación</span>
          </div>
          <h2>{{ f.title }}</h2>
          <div class="meta-row">
            <span>Enviado por: {{ f.ownerEmail }}</span>
            <span>Fecha límite: {{ f.deadline }}</span>
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
              <button (click)="previewDoc(doc)" class="btn btn-ghost btn-sm" aria-label="Previsualizar documento">Previsualizar</button>
            </div>
          }
        </div>

        <div class="pending-body">
          <p class="text-sm fw-medium mb-lg">Paso {{ f.step + 1 }} de {{ f.totalSteps }} — Tu firma es requerida</p>

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
              <button (click)="requestChanges()" class="btn btn-outline">Solicitar cambios</button>
              <button (click)="reject()" class="btn btn-outline btn-danger-ghost">Rechazar</button>
            </div>
          </div>
        </div>
      </div>
    } @else {
      <div class="empty-state" style="padding-top: 80px">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
        <p>No tienes documentos pendientes por revisar.</p>
      </div>
    }
  </div>
  `,
  styles: [':host { display: block; }']
})
export class PendingFlowsComponent {
  private flowService = inject(FlowService);
  private router = inject(Router);

  flows = toSignal(this.flowService.loadPendingFlows(), { initialValue: [] });
  termsAccepted = signal(false);

  flow = computed(() => this.flows()[0] || null);

  previewDoc(doc: any): void {
    const container = document.querySelector('.toast-container');
    if (container) {
      const toast = document.createElement('div');
      toast.className = 'toast toast-info';
      toast.textContent = `Abriendo previsualización de ${doc.name}`;
      container.appendChild(toast);
      setTimeout(() => toast.remove(), 3000);
    }
  }

  approve(): void {
    if (!this.termsAccepted()) return;
    const f = this.flow();
    if (!f) return;
    this.flowService.approveFlow(f.id).subscribe(() => {
      const container = document.querySelector('.toast-container');
      if (container) {
        const toast = document.createElement('div');
        toast.className = 'toast toast-success';
        toast.textContent = 'Documento aprobado correctamente.';
        container.appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
      }
      this.router.navigate(['/dashboard']);
    });
  }

  requestChanges(): void {
    const f = this.flow();
    if (!f) return;
    const reason = prompt('Describe los cambios solicitados:');
    if (reason === null) return;
    this.flowService.requestChanges(f.id, reason).subscribe(() => {
      const container = document.querySelector('.toast-container');
      if (container) {
        const toast = document.createElement('div');
        toast.className = 'toast toast-info';
        toast.textContent = 'Se ha notificado al remitente.';
        container.appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
      }
      this.router.navigate(['/dashboard']);
    });
  }

  reject(): void {
    const f = this.flow();
    if (!f) return;
    const reason = prompt('Motivo del rechazo:');
    if (reason === null) return;
    this.flowService.rejectFlow(f.id, reason).subscribe(() => {
      const container = document.querySelector('.toast-container');
      if (container) {
        const toast = document.createElement('div');
        toast.className = 'toast toast-error';
        toast.textContent = 'Flujo rechazado.';
        container.appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
      }
      this.router.navigate(['/dashboard']);
    });
  }
}
