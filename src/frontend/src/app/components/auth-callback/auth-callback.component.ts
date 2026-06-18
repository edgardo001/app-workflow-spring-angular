import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-auth-callback',
  template: `<p>Autenticando...</p>`,
  styles: [`:host { display: flex; justify-content: center; align-items: center; height: 100vh; }`]
})
export class AuthCallbackComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      if (token) {
        localStorage.setItem('auth_token', token);
        this.router.navigate(['/dashboard']);
      } else {
        this.router.navigate(['/login']);
      }
    });
  }
}
