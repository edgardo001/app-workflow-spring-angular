import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

const fakeStorage = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] ?? null,
    setItem: (key: string, value: string) => { store[key] = value; },
    removeItem: (key: string) => { delete store[key]; },
    clear: () => { store = {}; },
    get length() { return Object.keys(store).length; },
    key: (_: number) => null,
  } as Storage;
})();

Object.defineProperty(globalThis, 'localStorage', { value: fakeStorage, writable: true, configurable: true });

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        AuthService,
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } },
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

    const initReq = httpMock.expectOne('/api/auth/profile');
    initReq.flush({ id: '0', name: '', email: '', avatar: '', role: 'USER' });
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch profile', () => {
    const mockUser = { id: '1', name: 'Test', email: 'test@test.com', avatar: '', role: 'USER' };
    let result: any;
    service.fetchProfile().subscribe(user => { result = user; });
    const req = httpMock.expectOne('/api/auth/profile');
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
    expect(result.name).toBe('Test');
  });

  it('should logout and clear user', () => {
    service.logout();
    const req = httpMock.expectOne('/api/auth/logout');
    expect(req.request.method).toBe('POST');
    req.flush({});
    expect(service.isAuthenticated).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
