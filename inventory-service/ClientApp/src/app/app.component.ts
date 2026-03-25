import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-container">
      <nav class="navbar">
        <div class="nav-brand">
          <h1>Inventory Service</h1>
        </div>
        <div class="nav-links">
          <a routerLink="/inventory" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">
            Inventory
          </a>
          <a routerLink="/inventory/low-stock" routerLinkActive="active">
            Low Stock Alerts
          </a>
        </div>
      </nav>
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      background-color: #f5f5f5;
    }
    .navbar {
      background-color: #343a40;
      color: white;
      padding: 0 20px;
      display: flex;
      align-items: center;
      height: 60px;
    }
    .nav-brand h1 {
      font-size: 18px;
      margin: 0;
    }
    .nav-links {
      margin-left: 30px;
      display: flex;
      gap: 20px;
    }
    .nav-links a {
      color: rgba(255,255,255,0.7);
      text-decoration: none;
      padding: 8px 12px;
      border-radius: 4px;
    }
    .nav-links a:hover,
    .nav-links a.active {
      color: white;
      background-color: rgba(255,255,255,0.1);
    }
    .main-content {
      padding: 20px;
    }
  `],
})
export class AppComponent {
  title = 'Inventory Service';
}
