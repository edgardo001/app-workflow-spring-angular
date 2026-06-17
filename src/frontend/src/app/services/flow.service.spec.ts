import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { FlowService, CreateFlowRequest } from './flow.service';

describe('FlowService', () => {
  let service: FlowService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        FlowService,
      ]
    });
    service = TestBed.inject(FlowService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should load flows', () => {
    const mockFlows = [{ id: '1', title: 'Flow 1' }];
    let result: any;
    service.loadFlows().subscribe(flows => { result = flows; });
    const req = httpMock.expectOne('/api/flows');
    expect(req.request.method).toBe('GET');
    req.flush(mockFlows);
    expect(result.length).toBe(1);
    expect(service.flows().length).toBe(1);
  });

  it('should load pending flows', () => {
    const mockFlows = [{ id: '2', title: 'Pending Flow' }];
    let result: any;
    service.loadPendingFlows().subscribe(flows => { result = flows; });
    const req = httpMock.expectOne('/api/flows/pending');
    expect(req.request.method).toBe('GET');
    req.flush(mockFlows);
    expect(result.length).toBe(1);
  });

  it('should get flow by id', () => {
    const mockFlow = { id: '1', title: 'Test Flow' };
    let result: any;
    service.getFlow('1').subscribe(flow => { result = flow; });
    const req = httpMock.expectOne('/api/flows/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockFlow);
    expect(result.title).toBe('Test Flow');
  });

  it('should create a flow', () => {
    const request: CreateFlowRequest = {
      title: 'New Flow',
      description: 'Desc',
      deadline: '2026-07-01',
      destinatarios: ['test@test.com'],
      documentIds: ['doc1'],
    };
    const mockFlow = { id: '3', title: 'New Flow' };
    let result: any;
    service.createFlow(request).subscribe(flow => { result = flow; });
    const req = httpMock.expectOne('/api/flows');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockFlow);
    expect(result.title).toBe('New Flow');
  });

  it('should approve a flow', () => {
    service.approveFlow('1').subscribe();
    const req = httpMock.expectOne('/api/flows/1/approve');
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should reject a flow with reason', () => {
    service.rejectFlow('1', 'Not needed').subscribe();
    const req = httpMock.expectOne('/api/flows/1/reject');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ reason: 'Not needed' });
    req.flush({});
  });
});
