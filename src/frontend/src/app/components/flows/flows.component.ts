import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FlowService } from '../../services/flow.service';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-flows',
  imports: [RouterLink, FormsModule],
  template: `
  <div class="page-header">
    <div>
      <h1>Mis Flujos</h1>
      <p>Todos los flujos de aprobación en los que participas.</p>
    </div>
    <a routerLink="/flows/new" class="btn btn-primary" aria-label="Crear nuevo flujo">
      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
      Nuevo Flujo
    </a>
  </div>
  <div class="flows-grid">
    @for (flow of flows(); track flow.id) {
      <a [routerLink]="['/flows', flow.id]" class="flow-card">
        <div class="fc-top">
          <div>
            <div class="fc-title">{{ flow.title }}</div>
            <div class="fc-owner">
              <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
              <span>{{ flow.owner }}</span>
            </div>
          </div>
          <span class="badge" [class.badge-en-curso]="flow.status==='En curso'" [class.badge-completado]="flow.status==='Completado'" [class.badge-pendiente]="flow.status==='Pendiente'" [class.badge-rechazado]="flow.status==='Rechazado'">{{ flow.status }}</span>
        </div>
        <div class="fc-meta">
          <span>
            <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <span>{{ flow.step }}/{{ flow.totalSteps }} pasos</span>
          </span>
          <span>
            <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
            <span>{{ flow.deadline }}</span>
          </span>
        </div>
        <div class="fc-foot">
          <div class="progress-bar flex-1">
            <div class="progress-track"><div class="progress-fill" [style.width.%]="(flow.step / flow.totalSteps) * 100"></div></div>
            <span class="progress-label">{{ Math.round((flow.step / flow.totalSteps) * 100) }}%</span>
          </div>
          @if (flow.isMyTurn) {
            <button (click)="$event.stopPropagation(); $event.preventDefault(); navigateToPending()" class="action-btn view" aria-label="Ir a pendientes">
              <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
            </button>
          }
        </div>
      </a>
    }
  </div>
  `,
  styles: [':host { display: block; }']
})
export class FlowsComponent {
  private flowService = inject(FlowService);
  private router = inject(Router);
  flows = toSignal(this.flowService.loadFlows(), { initialValue: [] });
  protected readonly Math = Math;

  navigateToPending(): void {
    this.router.navigate(['/pending']);
  }
}
