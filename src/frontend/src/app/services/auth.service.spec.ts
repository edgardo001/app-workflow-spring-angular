import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
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

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        AuthService,
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should handle OAuth callback', () => {
    const mockResponse = {
      token: 'test-token',
      user: { id: '1', name: 'Test', email: 'test@test.com', avatar: '', role: 'USER' }
    };
    let result: any;
    service.handleOAuthCallback('code123').subscribe(res => { result = res; });
    const req = httpMock.expectOne('/api/auth/github/callback');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
    expect(result.token).toBe('test-token');
    expect(service.isAuthenticated).toBe(true);
  });

  it('should fetch profile', () => {
    localStorage.setItem('auth_token', 'test-token');
    const mockUser = { id: '1', name: 'Test', email: 'test@test.com', avatar: '', role: 'USER' };
    let result: any;
    service.fetchProfile().subscribe(user => { result = user; });
    const req = httpMock.expectOne('/api/auth/profile');
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
    expect(result.name).toBe('Test');
  });

  it('should logout and clear token', () => {
    localStorage.setItem('auth_token', 'test-token');
    service.logout();
    expect(localStorage.getItem('auth_token')).toBeNull();
  });
});
