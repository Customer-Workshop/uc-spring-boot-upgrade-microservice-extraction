import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary mb-4">
      <div class="container">
        <a class="navbar-brand" routerLink="/">
          <i class="bi bi-box-seam me-2"></i>Inventory Service
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav">
            <li class="nav-item">
              <a class="nav-link" routerLink="/inventory" routerLinkActive="active">
                <i class="bi bi-list-ul me-1"></i>Inventory
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" routerLink="/inventory/low-stock" routerLinkActive="active">
                <i class="bi bi-exclamation-triangle me-1"></i>Low Stock
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" routerLink="/categories" routerLinkActive="active">
                <i class="bi bi-tags me-1"></i>Categories
              </a>
            </li>
          </ul>
        </div>
      </div>
    </nav>
    <div class="container">
      <router-outlet></router-outlet>
    </div>
  `
})
export class AppComponent {
  title = 'Inventory Management';
}
