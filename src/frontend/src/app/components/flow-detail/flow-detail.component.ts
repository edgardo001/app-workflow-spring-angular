import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FlowService, Flow } from '../../services/flow.service';

@Component({
  selector: 'app-flow-detail',
  template: `
  <div class="page-header">
    <div>
      <a (click)="goBack()" class="btn btn-ghost pl-0 mb-xs" style="cursor:pointer">
        <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" /></svg>
        Volver
      </a>
      @if (flow(); as f) {
        <h1>{{ f.title }}</h1>
        <p>ID: #{{ f.id.substring(0, 8) }} · Creado por {{ f.owner }}</p>
      }
    </div>
    @if (flow(); as f) {
      @if (f.status !== 'Completado' && f.status !== 'Rechazado') {
        <div class="flex-wrap">
          <button (click)="relaunchFlow()" class="btn btn-outline">Reenviar</button>
          <button (click)="rejectFlow()" class="btn btn-outline btn-danger-ghost">Rechazar</button>
        </div>
      }
    }
  </div>

  @if (flow(); as f) {
    <div class="kpi-grid" style="margin-bottom: var(--gap-lg)">
      <div class="kpi-card">
        <div class="kpi-label">Estado</div>
        <div style="margin-top: var(--gap-sm)">
          <span class="badge" [class.badge-en-curso]="f.status==='En curso'" [class.badge-completado]="f.status==='Completado'" [class.badge-pendiente]="f.status==='Pendiente'" [class.badge-rechazado]="f.status==='Rechazado'">{{ f.status }}</span>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">Progreso</div>
        <div class="progress-bar" style="margin-top: var(--gap-sm)">
          <div class="progress-track" style="width: 100px"><div class="progress-fill" [style.width.%]="(f.step / f.totalSteps) * 100"></div></div>
          <span class="progress-label">{{ f.step }}/{{ f.totalSteps }}</span>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">Fecha límite</div>
        <div style="margin-top: var(--gap-sm); font-weight: 500">{{ f.deadline }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">Creado</div>
        <div style="margin-top: var(--gap-sm); font-size: var(--fs-sm)">{{ f.createdAt }}</div>
      </div>
    </div>

    <div class="table-container" style="margin-bottom: var(--gap-lg)">
      <div style="padding: var(--gap-md) var(--gap-lg); border-bottom: 1px solid var(--border); font-weight: 500">Documentos</div>
      <div class="doc-list" style="background: transparent">
        @for (doc of f.documents; track doc.id) {
          <div class="doc-item">
            <div class="doc-info">
              <div class="doc-icon pdf">
                <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" /></svg>
              </div>
              <div>
                <div class="doc-name">{{ doc.name }}</div>
                <div class="doc-size">{{ doc.size }}</div>
              </div>
            </div>
            <button (click)="previewDoc(doc)" class="btn btn-ghost btn-sm">Previsualizar</button>
          </div>
        }
      </div>
    </div>

    <div class="table-container">
      <div style="padding: var(--gap-md) var(--gap-lg); border-bottom: 1px solid var(--border); font-weight: 500">Participantes</div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Email</th>
              <th>Paso</th>
              <th>Estado</th>
              <th>Aprobado</th>
            </tr>
          </thead>
          <tbody>
            @for (p of f.participants; track p.email) {
              <tr>
                <td>{{ p.name }}</td>
                <td><span class="td-cell-muted">{{ p.email }}</span></td>
                <td><span class="td-id">Paso {{ p.stepOrder }}</span></td>
                <td>
                  @if (p.status === 'approved') { <span class="badge badge-completado">Aprobado</span> }
                  @else if (p.status === 'pending') { <span class="badge badge-pendiente">Pendiente</span> }
                  @else { <span class="badge badge-rechazado">Rechazado</span> }
                </td>
                <td><span class="td-cell-muted">{{ p.approvedAt || '—' }}</span></td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  } @else {
    <div class="empty-state">
      <p>Cargando...</p>
    </div>
  }
  `,
  styles: [':host { display: block; }']
})
export class FlowDetailComponent {
  private route = inject(ActivatedRoute);
  private flowService = inject(FlowService);
  private router = inject(Router);

  flow = signal<Flow | null>(null);

  constructor() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.flowService.getFlow(id).subscribe(f => this.flow.set(f));
  }

  goBack(): void {
    this.router.navigate(['/flows']);
  }

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

  relaunchFlow(): void {
    const f = this.flow();
    if (!f) return;
    this.flowService.relaunchFlow(f.id).subscribe(updated => {
      this.flow.set(updated);
    });
  }

  rejectFlow(): void {
    const f = this.flow();
    if (!f) return;
    const reason = prompt('Motivo del rechazo:');
    if (reason === null) return;
    this.flowService.rejectFlow(f.id, reason).subscribe(updated => {
      this.flow.set(updated);
    });
  }
}
