import { Component, signal, AfterViewInit } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing',
  imports: [RouterLink],
  template: `
  <header class="topnav" data-od-id="topnav" style="
    position: sticky; top: 0; z-index: 100;
    background: color-mix(in oklch, var(--bg) 92%, transparent);
    backdrop-filter: blur(12px);
    border-bottom: 1px solid var(--border);
  ">
    <div class="container" style="
      max-width: var(--container, 1120px);
      margin-inline: auto;
      padding-inline: var(--gutter, 32px);
      display: flex; align-items: center; justify-content: space-between; padding-block: 14px; min-height: var(--nav-h, 60px);
    ">
      <span class="logo row" style="font-family: var(--font-display); font-size: 19px; font-weight: 600; letter-spacing: -0.01em; flex-shrink: 0; display: flex; align-items: center; gap: 8px;">
        <svg width="24" height="24" viewBox="0 0 32 32" fill="none" aria-hidden="true"><rect x="0.5" y="0.5" width="31" height="31" rx="7.5" stroke="var(--accent)" stroke-width="2.5"/><path d="M9 17l5 5 9-9" stroke="var(--accent)" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
        app-workflow
      </span>
      <nav style="display: flex; gap: var(--gap-lg, 32px); align-items: center;">
        <a href="#features" style="font-size: 14px; color: var(--muted); white-space: nowrap;">Características</a>
        <a href="#testimonial" style="font-size: 14px; color: var(--muted); white-space: nowrap;">Testimonios</a>
        <button (click)="toggleTheme()" class="theme-toggle" style="
          display: flex; align-items: center; justify-content: center;
          width: 34px; height: 34px; border: 1px solid var(--border);
          border-radius: var(--radius, 8px); background: transparent; color: var(--muted);
          cursor: pointer; flex-shrink: 0;
        " aria-label="Cambiar tema claro/oscuro">
          <svg class="moon-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"/></svg>
          <svg class="sun-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
        </button>
        <a routerLink="/login" class="btn btn-primary btn-mobile-full" style="display: inline-flex; align-items: center; gap: 8px; padding: 11px 20px; border-radius: var(--radius, 8px); border: 1px solid transparent; font-size: 15px; font-weight: 500; background: var(--accent); color: var(--accent-on);">Ir a la app</a>
      </nav>
    </div>
  </header>

  <main id="content" role="main">
    <section class="section" data-od-id="hero" style="
      padding-block: clamp(80px, 12vw, 160px);
      position: relative;
      overflow: hidden;
    ">
      <div style="
        position: absolute; inset: 0;
        background: radial-gradient(ellipse at 15% 40%, var(--accent-soft) 0%, transparent 55%),
                    radial-gradient(ellipse at 85% 60%, var(--fg-soft) 0%, transparent 55%);
        pointer-events: none;
      "></div>
      <div class="container" style="
        max-width: var(--container, 1120px); margin-inline: auto; padding-inline: var(--gutter, 32px);
        text-align: center; max-width: 32ch; margin-inline: auto; position: relative;
      ">
        <p class="eyebrow fade-in-up" style="font-family: var(--font-mono); font-size: 12px; letter-spacing: 0.08em; text-transform: uppercase; color: var(--accent); margin: 0 0 var(--gap-md, 20px);">APP-WORKFLOW · PLATAFORMA DE APROBACIONES</p>
        <h1 class="fade-in-up" data-delay="1" style="font-family: var(--font-display); font-size: var(--fs-h1, clamp(44px,6vw,76px)); line-height: 1.04; letter-spacing: -0.02em; font-weight: 600; margin: 0 0 var(--gap-md, 20px);">Flujos de aprobación que tu equipo usará de verdad.</h1>
        <p class="lead fade-in-up" data-delay="2" style="font-size: var(--fs-lead, 19px); line-height: 1.55; color: var(--muted); max-width: 60ch; margin: 0 auto var(--gap-lg, 32px);">Gestiona, revisa y firma documentos con la claridad del correo moderno y la seguridad de una plataforma empresarial.</p>
        <div class="hero-cta fade-in-up" data-delay="3" style="display: inline-flex; gap: var(--gap-sm, 12px); flex-wrap: wrap;">
          <a routerLink="/login" class="btn btn-primary" style="display: inline-flex; align-items: center; gap: 8px; padding: 11px 20px; border-radius: var(--radius, 8px); border: 1px solid transparent; font-size: 15px; font-weight: 500; background: var(--accent); color: var(--accent-on);">Iniciar flujo ahora</a>
          <a href="#features" class="btn btn-secondary" style="display: inline-flex; align-items: center; gap: 8px; padding: 11px 20px; border-radius: var(--radius, 8px); border: 1px solid var(--border); font-size: 15px; font-weight: 500; background: transparent; color: var(--fg);">Ver demo</a>
        </div>
      </div>
    </section>

    <section class="section" data-od-id="features" style="padding-block: clamp(48px, 8vw, 96px);">
      <div class="container" style="max-width: var(--container, 1120px); margin-inline: auto; padding-inline: var(--gutter, 32px); display: flex; flex-direction: column; gap: var(--gap-xl, 56px);">
        <div style="max-width: 36ch;">
          <p class="eyebrow" style="font-family: var(--font-mono); font-size: 12px; letter-spacing: 0.08em; text-transform: uppercase; color: var(--accent); margin: 0 0 var(--gap-md, 20px);">CAPACIDADES CLAVE</p>
          <h2 style="font-family: var(--font-display); font-size: var(--fs-h2, clamp(32px,4vw,48px)); line-height: 1.1; letter-spacing: -0.015em; font-weight: 600; margin: 0;">Tres razones por las que los equipos eligen app-workflow.</h2>
        </div>
        <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--gap-lg, 32px);">
          <div class="feature card-flat fade-in-up" style="opacity: 0; transform: translateY(24px); transition: opacity 0.6s cubic-bezier(0.2,0,0,1), transform 0.6s cubic-bezier(0.2,0,0,1);">
            <div class="feature-mark" style="width: 36px; height: 36px; display: grid; place-items: center; border: 1px solid var(--border); border-radius: var(--radius, 8px); color: var(--accent); margin-bottom: var(--gap-md, 20px);">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" width="18" height="18" aria-hidden="true"><circle cx="12" cy="12" r="8"/><path d="M12 7v5l3 2"/><path d="M4 4l4 4M20 4l-4 4"/></svg>
            </div>
            <h3 style="font-size: var(--fs-h3, 22px); font-weight: 600; line-height: 1.3; margin: 0 0 6px;">Control de tiempos</h3>
            <p style="margin: 0; color: var(--muted); font-size: 15px;">Establece fechas límite y alertas automáticas para que ningún documento se quede esperando. Cada paso tiene un responsable y un plazo.</p>
          </div>
          <div class="feature card-flat fade-in-up" data-delay="1" style="opacity: 0; transform: translateY(24px); transition: opacity 0.6s cubic-bezier(0.2,0,0,1) 0.1s, transform 0.6s cubic-bezier(0.2,0,0,1) 0.1s;">
            <div class="feature-mark" style="width: 36px; height: 36px; display: grid; place-items: center; border: 1px solid var(--border); border-radius: var(--radius, 8px); color: var(--accent); margin-bottom: var(--gap-md, 20px);">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" width="18" height="18" aria-hidden="true"><path d="M12 2l8 4v6c0 4-3.5 7.5-8 9-4.5-1.5-8-5-8-9V6l8-4z"/><path d="M9 12l2 2 4-4"/></svg>
            </div>
            <h3 style="font-size: var(--fs-h3, 22px); font-weight: 600; line-height: 1.3; margin: 0 0 6px;">Pista de auditoría</h3>
            <p style="margin: 0; color: var(--muted); font-size: 15px;">Registro inmutable de cada acción: quién aprobó, cuándo y desde dónde. Ideal para cumplimiento normativo y auditorías internas.</p>
          </div>
          <div class="feature card-flat fade-in-up" data-delay="2" style="opacity: 0; transform: translateY(24px); transition: opacity 0.6s cubic-bezier(0.2,0,0,1) 0.2s, transform 0.6s cubic-bezier(0.2,0,0,1) 0.2s;">
            <div class="feature-mark" style="width: 36px; height: 36px; display: grid; place-items: center; border: 1px solid var(--border); border-radius: var(--radius, 8px); color: var(--accent); margin-bottom: var(--gap-md, 20px);">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" width="18" height="18" aria-hidden="true"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M9 3v18"/></svg>
            </div>
            <h3 style="font-size: var(--fs-h3, 22px); font-weight: 600; line-height: 1.3; margin: 0 0 6px;">Interfaz limpia</h3>
            <p style="margin: 0; color: var(--muted); font-size: 15px;">Diseñada para minimizar la carga cognitiva. Tus flujos, documentos y aprobaciones en un solo lugar, sin ruido visual.</p>
          </div>
        </div>
      </div>
    </section>

    <section class="section" data-od-id="stats" style="padding-block: clamp(48px, 8vw, 96px); border-top: 1px solid var(--border);">
      <div class="container" style="max-width: var(--container, 1120px); margin-inline: auto; padding-inline: var(--gutter, 32px);">
        <p class="eyebrow text-center" style="font-family: var(--font-mono); font-size: 12px; letter-spacing: 0.08em; text-transform: uppercase; color: var(--accent); margin: 0 0 var(--gap-md, 20px); text-align: center;">EN CIFRAS</p>
        <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--gap-md, 20px);">
          <div class="stat text-center fade-in-up" style="opacity: 0; transform: translateY(24px); transition: opacity 0.6s cubic-bezier(0.2,0,0,1), transform 0.6s cubic-bezier(0.2,0,0,1);">
            <div class="stat-num" style="font-family: var(--font-display); font-size: clamp(56px,8vw,96px); line-height: 0.95; letter-spacing: -0.04em; color: var(--accent); font-weight: 600;">12 <span style="font-size: 0.5em; opacity: 0.7; margin-left: 2px;">K</span></div>
            <div class="stat-label" style="color: var(--muted); font-size: 14px; margin-top: 8px; max-width: 24ch; margin-inline: auto;">Documentos gestionados cada mes</div>
          </div>
          <div class="stat text-center fade-in-up" data-delay="1" style="opacity: 0; transform: translateY(24px); transition: opacity 0.6s cubic-bezier(0.2,0,0,1) 0.1s, transform 0.6s cubic-bezier(0.2,0,0,1) 0.1s;">
            <div class="stat-num" style="font-family: var(--font-display); font-size: clamp(56px,8vw,96px); line-height: 0.95; letter-spacing: -0.04em; color: var(--accent); font-weight: 600;">4</div>
            <div class="stat-label" style="color: var(--muted); font-size: 14px; margin-top: 8px; max-width: 24ch; margin-inline: auto;">Años en el mercado sin interrupciones</div>
          </div>
          <div class="stat text-center fade-in-up" data-delay="2" style="opacity: 0; transform: translateY(24px); transition: opacity 0.6s cubic-bezier(0.2,0,0,1) 0.2s, transform 0.6s cubic-bezier(0.2,0,0,1) 0.2s;">
            <div class="stat-num" style="font-family: var(--font-display); font-size: clamp(56px,8vw,96px); line-height: 0.95; letter-spacing: -0.04em; color: var(--accent); font-weight: 600;">48</div>
            <div class="stat-label" style="color: var(--muted); font-size: 14px; margin-top: 8px; max-width: 24ch; margin-inline: auto;">Equipos activos en la plataforma</div>
          </div>
          <div class="stat text-center fade-in-up" data-delay="3" style="opacity: 0; transform: translateY(24px); transition: opacity 0.6s cubic-bezier(0.2,0,0,1) 0.3s, transform 0.6s cubic-bezier(0.2,0,0,1) 0.3s;">
            <div class="stat-num" style="font-family: var(--font-display); font-size: clamp(56px,8vw,96px); line-height: 0.95; letter-spacing: -0.04em; color: var(--accent); font-weight: 600;">99<span style="font-size: 0.5em; opacity: 0.7; margin-left: 2px;">%</span></div>
            <div class="stat-label" style="color: var(--muted); font-size: 14px; margin-top: 8px; max-width: 24ch; margin-inline: auto;">Documentos aprobados antes de su vencimiento</div>
          </div>
        </div>
      </div>
    </section>

    <section class="section" data-od-id="testimonial" style="padding-block: clamp(48px, 8vw, 96px); border-top: 1px solid var(--border);">
      <div class="container" style="max-width: var(--container, 1120px); margin-inline: auto; padding-inline: var(--gutter, 32px);">
        <p class="eyebrow text-center" style="font-family: var(--font-mono); font-size: 12px; letter-spacing: 0.08em; text-transform: uppercase; color: var(--accent); margin: 0 0 var(--gap-md, 20px); text-align: center;">TESTIMONIO</p>
        <div style="display: grid; grid-template-columns: 1fr 2fr; gap: var(--gap-xl, 56px); align-items: center;">
          <div class="testimonial-author" style="display: flex; align-items: center; gap: var(--gap-md, 20px);">
            <div style="width: 72px; height: 72px; border-radius: 50%; aspect-ratio: 1; flex-shrink: 0; background: linear-gradient(135deg, var(--accent-soft), var(--fg-soft)), var(--surface); border: 1px solid var(--border);" aria-label="Foto de Carlos Ruiz" role="img"></div>
            <div>
              <p style="font-weight: 600; margin: 0; font-size: var(--fs-body, 16px);">Carlos Ruiz</p>
              <p style="font-family: var(--font-mono); font-size: var(--fs-meta, 13px); color: var(--muted); margin: 0;">COO, Innovatech</p>
            </div>
          </div>
          <div class="fade-in-up" style="opacity: 0; transform: translateY(24px); transition: opacity 0.6s cubic-bezier(0.2,0,0,1), transform 0.6s cubic-bezier(0.2,0,0,1);">
            <div style="font-family: var(--font-display); font-size: 140px; line-height: 0.7; color: var(--fg); opacity: 0.08; margin-bottom: -28px;">"</div>
            <blockquote style="font-family: var(--font-display); font-size: clamp(24px, 2.6vw, 32px); line-height: 1.32; letter-spacing: -0.01em; max-width: 28ch; margin: 0;">app-workflow eliminó los cuellos de botella en nuestras aprobaciones. Pasamos de 3 días a 2 horas en contratos complejos con varios firmantes.</blockquote>
          </div>
        </div>
      </div>
    </section>

    <section class="section text-center" data-od-id="cta-strip" style="padding-block: clamp(48px, 8vw, 96px); border-top: 1px solid var(--border); text-align: center;">
      <div class="container" style="max-width: var(--container, 1120px); margin-inline: auto; padding-inline: var(--gutter, 32px);">
        <div style="display: flex; flex-direction: column; gap: var(--gap-md, 20px); align-items: center; margin-inline: auto; max-width: 36ch;">
          <h2 style="font-family: var(--font-display); font-size: var(--fs-h2, clamp(32px,4vw,48px)); line-height: 1.1; letter-spacing: -0.015em; font-weight: 600; margin: 0;">Empieza a aprobar en minutos, no en semanas.</h2>
          <p class="lead" style="font-size: var(--fs-lead, 19px); line-height: 1.55; color: var(--muted); max-width: 60ch; margin: 0;">Diseñado para equipos que valoran su tiempo.</p>
          <a routerLink="/login" class="btn btn-primary" style="display: inline-flex; align-items: center; gap: 8px; padding: 11px 20px; border-radius: var(--radius, 8px); border: 1px solid transparent; font-size: 15px; font-weight: 500; background: var(--accent); color: var(--accent-on);">Probar la app</a>
        </div>
      </div>
    </section>
  </main>

  <footer class="pagefoot" data-od-id="footer" style="padding-block: var(--gap-xl, 56px); color: var(--muted); font-size: 13px; border-top: 1px solid var(--border);">
    <div class="container" style="max-width: var(--container, 1120px); margin-inline: auto; padding-inline: var(--gutter, 32px);">
      <div style="display: flex; align-items: center; justify-content: space-between; gap: var(--gap-md, 20px); flex-wrap: wrap;">
        <span>© app-workflow · 2026</span>
        <nav style="display: flex; gap: var(--gap-lg, 32px);">
          <a href="#features">Características</a>
          <a href="mailto:hola@app-workflow.com">Contacto</a>
          <a href="/tos.html">Términos de uso</a>
          <a href="/privacidad.html">Política de privacidad</a>
        </nav>
        <span style="font-family: var(--font-mono); font-size: var(--fs-meta, 13px); color: var(--muted);">Aprobaciones sin fricción</span>
      </div>
    </div>
  </footer>
  `,
  styles: [`
    :host { display: block; }
    .fade-in-up.visible { opacity: 1 !important; transform: translateY(0) !important; }
    @media (prefers-reduced-motion: reduce) {
      .fade-in-up { opacity: 1 !important; transform: none !important; transition: none !important; }
    }
    .btn:active { transform: translateY(1px); }
    .btn-primary:hover { background: color-mix(in oklch, var(--accent) 88%, black); }
    .btn-secondary:hover { border-color: var(--fg); }
  `]
})
export class LandingComponent implements AfterViewInit {
  theme = signal(document.documentElement.getAttribute('data-theme') || 'light');

  ngAfterViewInit(): void {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible');
        }
      });
    }, { threshold: 0.15 });

    setTimeout(() => {
      document.querySelectorAll('.fade-in-up').forEach(el => observer.observe(el));
    });
  }

  toggleTheme(): void {
    const next = this.theme() === 'dark' ? 'light' : 'dark';
    this.theme.set(next);
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('theme', next);
  }
}
