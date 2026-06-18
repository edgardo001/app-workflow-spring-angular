import { Component, inject, signal } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FlowService } from '../../services/flow.service';
import { DocumentService } from '../../services/document.service';
import { forkJoin, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-new-flow',
  imports: [RouterLink, FormsModule],
  template: `
  <div class="page-header">
    <div>
      <a routerLink="/dashboard" class="btn btn-ghost pl-0 mb-xs">
        <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" /></svg>
        Volver
      </a>
      <h1>Crear Nuevo Flujo</h1>
      <p>Configura un nuevo proceso de aprobación.</p>
    </div>
  </div>

  <div class="form-layout">
    <div class="form-card">
      <div class="field">
        <label>Título del flujo *</label>
        <input type="text" [(ngModel)]="title" placeholder="Ej: Contrato de servicios Q3">
      </div>
      <div class="field">
        <label>Descripción (opcional)</label>
        <textarea [(ngModel)]="description" placeholder="Añade contexto para los revisores..."></textarea>
      </div>
      <div class="field">
        <label>Fecha límite *</label>
        <input type="date" [(ngModel)]="deadline">
      </div>
      <div class="field">
        <label>Documentos adjuntos</label>
        <div class="dropzone" (click)="fileInput.click()">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" /></svg>
          <p>Arrastra archivos aquí o haz clic</p>
          <p class="hint">PDF, DOCX — máx. 2 MB por archivo (máx. 5)</p>
          <input #fileInput type="file" multiple accept=".pdf,.docx" (change)="onFilesSelected($event)" style="display:none" aria-hidden="true">
        </div>
        @for (file of files(); track file.name) {
          <div class="file-chip">
            <span>
              <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true" class="inline-icon"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"/></svg>
              {{ file.name }} <span class="text-xs text-muted">{{ (file.size / 1024 / 1024).toFixed(1) }} MB</span>
            </span>
            <button (click)="removeFile(file)" class="rm-btn" aria-label="Eliminar archivo">
              <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
            </button>
          </div>
        }
      </div>
      <div class="field">
        <label>Destinatarios (orden de aprobación)</label>
        @for (d of destinatarios(); track $index) {
          <div class="dest-row">
            <span class="idx">{{ $index + 1 }}.</span>
            <input type="email" [(ngModel)]="destinatarios()[$index]" placeholder="correo@ejemplo.com" [attr.aria-label]="'Correo del destinatario ' + ($index + 1)">
            <button (click)="removeDest($index)" class="rm-btn" [attr.aria-label]="'Eliminar destinatario ' + ($index + 1)">
              <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
            </button>
          </div>
        }
        <button (click)="addDest()" class="btn btn-ghost btn-sm mt-xs pl-0">
          <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
          Agregar destinatario
        </button>
      </div>
      <div class="form-actions">
        <a routerLink="/dashboard" class="btn btn-outline">Cancelar</a>
        <button (click)="startFlow()" class="btn btn-primary">Iniciar flujo →</button>
      </div>
    </div>

    <div>
      <div class="preview-card sticky-preview">
        <h3>Vista previa del flujo</h3>
        <div class="preview-flow">
          <div class="flow-title">{{ title || 'Título del flujo' }}</div>
          <div class="flow-desc">{{ description || 'Sin descripción' }}</div>
          <div class="step-chain">
            @for (d of destinatarios(); track $index) {
              <div class="step-item">
                <div class="step-line"></div>
                <div class="step-node" [class.active]="d">
                  <span>{{ $index + 1 }}</span>
                </div>
                <div class="step-info">
                  <div class="s-name">{{ d || 'Pendiente' }}</div>
                  <div class="s-role">Paso de aprobación</div>
                </div>
              </div>
            }
          </div>
        </div>
        <div class="info-note">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
          <span>Los destinatarios recibirán un correo con un enlace seguro para revisar y aprobar los documentos en el orden establecido.</span>
        </div>
      </div>
    </div>
  </div>
  `,
  styles: [':host { display: block; }']
})
export class NewFlowComponent {
  private flowService = inject(FlowService);
  private documentService = inject(DocumentService);
  private router = inject(Router);

  title = signal('');
  description = signal('');
  deadline = signal(new Date(Date.now() + 2 * 86400000).toISOString().split('T')[0]);
  files = signal<File[]>([]);
  destinatarios = signal<string[]>(['', '']);

  addDest(): void {
    this.destinatarios.update(d => [...d, '']);
  }

  removeDest(index: number): void {
    this.destinatarios.update(d => d.filter((_, i) => i !== index));
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    const newFiles = Array.from(input.files).slice(0, 5 - this.files().length);
    const validFiles = newFiles.filter(f => f.size <= 2 * 1024 * 1024);
    this.files.update(f => [...f, ...validFiles]);
    input.value = '';
  }

  removeFile(file: File): void {
    this.files.update(f => f.filter(fi => fi !== file));
  }

  startFlow(): void {
    if (!this.title()) {
      this.showError('El título del flujo es obligatorio.');
      return;
    }
    const validEmails = this.destinatarios().filter(e => e.trim());
    if (validEmails.length === 0) {
      this.showError('Debes agregar al menos un destinatario.');
      return;
    }

    const uploads$ = this.files().length > 0 
      ? forkJoin(this.files().map(f => this.documentService.upload(f)))
      : of([]);

    uploads$.pipe(
      switchMap(uploadedDocs => {
        const documentIds = uploadedDocs.map(d => d.id);
        return this.flowService.createFlow({
          title: this.title(),
          description: this.description(),
          deadline: this.deadline() ? this.deadline() + 'T00:00:00Z' : '',
          participantEmails: validEmails,
          documentIds: documentIds
        });
      })
    ).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.showError('Error al crear el flujo: ' + (err.error?.message || err.message));
      }
    });
  }

  private showError(msg: string): void {
    const container = document.querySelector('.toast-container');
    if (container) {
      const toast = document.createElement('div');
      toast.className = 'toast toast-error';
      toast.textContent = msg;
      container.appendChild(toast);
      setTimeout(() => toast.remove(), 3000);
    }
  }
}
