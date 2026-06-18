import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface Flow {
  id: string;
  title: string;
  description: string;
  owner: string;
  ownerEmail: string;
  status: string;
  badgeClass: string;
  step: number;
  totalSteps: number;
  deadline: string;
  isMyTurn: boolean;
  documents: FlowDocument[];
  participants: FlowParticipant[];
  createdAt: string;
}

export interface FlowDocument {
  id: string;
  name: string;
  size: string;
  type: string;
  url: string;
}

export interface FlowParticipant {
  email: string;
  name: string;
  stepOrder: number;
  status: string;
  approvedAt?: string;
}

export interface CreateFlowRequest {
  title: string;
  description: string;
  deadline: string;
  participantEmails: string[];
  documentIds: string[];
}

@Injectable({ providedIn: 'root' })
export class FlowService {
  private readonly flowsSignal = signal<Flow[]>([]);
  readonly flows = this.flowsSignal.asReadonly();

  private readonly pendingFlowsSignal = signal<Flow[]>([]);
  readonly pendingFlows = this.pendingFlowsSignal.asReadonly();

  constructor(private http: HttpClient) {}

  loadFlows(): Observable<Flow[]> {
    return this.http.get<Flow[]>('/api/flows').pipe(
      tap(flows => this.flowsSignal.set(flows))
    );
  }

  loadPendingFlows(): Observable<Flow[]> {
    return this.http.get<Flow[]>('/api/flows/pending').pipe(
      tap(flows => this.pendingFlowsSignal.set(flows))
    );
  }

  getFlow(id: string): Observable<Flow> {
    return this.http.get<Flow>(`/api/flows/${id}`);
  }

  verifyToken(token: string): Observable<Flow> {
    return this.http.get<Flow>(`/api/flows/verify`, { params: { token } });
  }

  createFlow(data: CreateFlowRequest): Observable<Flow> {
    return this.http.post<Flow>('/api/flows', data).pipe(
      tap(flow => this.flowsSignal.update(flows => [flow, ...flows]))
    );
  }

  approveFlow(id: string, token?: string): Observable<Flow> {
    return this.http.post<Flow>(`/api/flows/${id}/approve`, { token });
  }

  rejectFlow(id: string, reason: string, token?: string): Observable<Flow> {
    return this.http.post<Flow>(`/api/flows/${id}/reject`, { reason, token });
  }

  requestChanges(id: string, reason: string): Observable<Flow> {
    return this.http.post<Flow>(`/api/flows/${id}/request-changes`, { reason });
  }

  relaunchFlow(id: string): Observable<Flow> {
    return this.http.post<Flow>(`/api/flows/${id}/relaunch`, {});
  }
}
