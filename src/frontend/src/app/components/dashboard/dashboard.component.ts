import { Component, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FlowService } from '../../services/flow.service';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, FormsModule],
  template: `
  <div class="page-header">
    <div>
      <h1>Dashboard</h1>
      <p>Resumen general de los flujos de aprobación.</p>
    </div>
    <a routerLink="/flows/new" class="btn btn-primary">
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
      Nuevo Flujo
    </a>
  </div>

  <div class="kpi-grid">
    <div class="kpi-card">
      <div class="kpi-icon kpi-icon-accent">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
      </div>
      <div class="kpi-num">{{ activeCount() }}</div>
      <div class="kpi-label">Flujos activos</div>
      <div class="kpi-trend up">
        <svg width="12" height="12" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true"><path fill-rule="evenodd" d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z" clip-rule="evenodd" /></svg>
        3 esta semana
      </div>
    </div>
    <div class="kpi-card">
      <div class="kpi-icon kpi-icon-success">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
      </div>
      <div class="kpi-num">{{ completedCount() }}</div>
      <div class="kpi-label">Completados</div>
      <div class="kpi-trend up">
        <svg width="12" height="12" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true"><path fill-rule="evenodd" d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z" clip-rule="evenodd" /></svg>
        8 esta semana
      </div>
    </div>
    <div class="kpi-card">
      <div class="kpi-icon kpi-icon-warn">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
      </div>
      <div class="kpi-num">{{ pendingCount() }}</div>
      <div class="kpi-label">Pendientes de firma</div>
      <div class="kpi-trend down">
        <svg width="12" height="12" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true"><path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" /></svg>
        2 desde ayer
      </div>
    </div>
    <div class="kpi-card">
      <div class="kpi-icon kpi-icon-danger">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
      </div>
      <div class="kpi-num">{{ expiredCount() }}</div>
      <div class="kpi-label">Expirados</div>
      <div class="kpi-trend muted">Sin cambios hoy</div>
    </div>
  </div>

  <div class="table-container">
    <div class="table-toolbar">
      <div class="search-wrap">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
        <input type="text" placeholder="Buscar por título, ID o destinatario..." [(ngModel)]="searchQuery" aria-label="Buscar flujos">
      </div>
    </div>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID Flujo</th>
            <th>Título</th>
            <th>Estado</th>
            <th>Progreso</th>
            <th>Fecha Lím.</th>
            <th class="text-right">Acciones</th>
          </tr>
        </thead>
        <tbody>
          @if (pagedFlows().length === 0) {
            <tr>
              <td colspan="6">
                <div class="empty-state">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
                  <p>No se encontraron flujos con ese criterio.</p>
                  <p class="hint">Prueba con otro término o limpia el filtro.</p>
                </div>
              </td>
            </tr>
          }
          @for (flow of pagedFlows(); track flow.id) {
            <tr [routerLink]="['/flows', flow.id]">
              <td><span class="td-id">#{{ flow.id.substring(0,8) }}</span></td>
              <td>
                <div class="td-title">{{ flow.title }}</div>
                <div class="td-owner">{{ flow.owner }}</div>
              </td>
              <td><span class="badge" [class.badge-en-curso]="flow.status==='En curso'" [class.badge-completado]="flow.status==='Completado'" [class.badge-pendiente]="flow.status==='Pendiente'" [class.badge-rechazado]="flow.status==='Rechazado'">{{ flow.status }}</span></td>
              <td>
                <div class="progress-bar">
                  <div class="progress-track"><div class="progress-fill" [style.width.%]="(flow.step / flow.totalSteps) * 100"></div></div>
                  <span class="progress-label">{{ flow.step }}/{{ flow.totalSteps }}</span>
                </div>
              </td>
              <td><span class="td-cell-muted">{{ flow.deadline }}</span></td>
              <td class="td-actions">
                <a [routerLink]="['/flows', flow.id]" class="action-btn view" aria-label="Ver detalle del flujo">
                  <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
                </a>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
    <div class="table-foot">
      <span class="page-info">Mostrando <strong>{{ showingFrom() }}</strong>–<strong>{{ showingTo() }}</strong> de <strong>{{ filteredFlows().length }}</strong> resultados</span>
      <div class="pagination" role="navigation" aria-label="Paginación de resultados">
        <button (click)="prevPage()" class="page-btn" [disabled]="currentPage() <= 1" aria-label="Página anterior">Anterior</button>
        <span aria-current="page">Página <strong>{{ currentPage() }}</strong> de <strong>{{ totalPages() }}</strong></span>
        <button (click)="nextPage()" class="page-btn" [disabled]="currentPage() >= totalPages()" aria-label="Página siguiente">Siguiente</button>
      </div>
    </div>
  </div>
  `,
  styles: [':host { display: block; }']
})
export class DashboardComponent {
  private flowService = inject(FlowService);
  flows = toSignal(this.flowService.loadFlows(), { initialValue: [] });

  searchQuery = signal('');
  currentPage = signal(1);
  pageSize = 10;

  filteredFlows = computed(() => {
    const q = this.searchQuery().toLowerCase();
    if (!q) return this.flows();
    return this.flows().filter(f =>
      f.title.toLowerCase().includes(q) ||
      f.owner.toLowerCase().includes(q) ||
      f.id.includes(q)
    );
  });

  totalPages = computed(() => Math.max(1, Math.ceil(this.filteredFlows().length / this.pageSize)));

  pagedFlows = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredFlows().slice(start, start + this.pageSize);
  });

  showingFrom = computed(() => this.filteredFlows().length === 0 ? 0 : (this.currentPage() - 1) * this.pageSize + 1);
  showingTo = computed(() => Math.min(this.currentPage() * this.pageSize, this.filteredFlows().length));

  activeCount = computed(() => this.flows().filter(f => f.status === 'En curso').length);
  completedCount = computed(() => this.flows().filter(f => f.status === 'Completado').length);
  pendingCount = computed(() => this.flows().filter(f => f.status === 'Pendiente').length);
  expiredCount = computed(() => this.flows().filter(f => f.status === 'Rechazado').length);

  prevPage(): void { if (this.currentPage() > 1) this.currentPage.update(p => p - 1); }
  nextPage(): void { if (this.currentPage() < this.totalPages()) this.currentPage.update(p => p + 1); }
}
