import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { InventoryService } from '../../services/inventory.service';
import { Category, CreateCategory } from '../../models/inventory.model';

@Component({
  selector: 'app-inventory-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2><i class="bi bi-tags me-2"></i>Categories</h2>
      <button class="btn btn-primary" (click)="showCreateForm = !showCreateForm">
        <i class="bi bi-plus-lg me-1"></i>Add Category
      </button>
    </div>

    <div class="card mb-4" *ngIf="showCreateForm">
      <div class="card-header">
        <h5 class="card-title mb-0">New Category</h5>
      </div>
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-4">
            <label class="form-label">Name *</label>
            <input type="text" class="form-control" [(ngModel)]="newCategory.name" placeholder="Category name">
          </div>
          <div class="col-md-6">
            <label class="form-label">Description</label>
            <input type="text" class="form-control" [(ngModel)]="newCategory.description" placeholder="Optional description">
          </div>
          <div class="col-md-2 d-flex align-items-end">
            <button class="btn btn-success w-100" (click)="createCategory()" [disabled]="!newCategory.name">
              <i class="bi bi-check-lg me-1"></i>Save
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="row">
      <div class="col-md-4 mb-4" *ngFor="let cat of categories">
        <div class="card h-100">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start">
              <h5 class="card-title">
                <i class="bi bi-tag me-1"></i>{{ cat.name }}
              </h5>
              <span [class]="cat.isActive ? 'badge bg-success' : 'badge bg-secondary'">
                {{ cat.isActive ? 'Active' : 'Inactive' }}
              </span>
            </div>
            <p class="card-text text-muted">{{ cat.description || 'No description' }}</p>
            <div class="d-flex justify-content-between align-items-center">
              <span class="badge bg-primary">{{ cat.itemCount }} items</span>
              <a [routerLink]="['/inventory']" [queryParams]="{categoryId: cat.id}" class="btn btn-sm btn-outline-primary">
                View Items
              </a>
            </div>
          </div>
          <div class="card-footer text-muted small">
            Created: {{ cat.createdAt | date:'mediumDate' }}
          </div>
        </div>
      </div>
      <div *ngIf="categories.length === 0" class="col-12 text-center py-5">
        <i class="bi bi-tags fs-1 text-muted"></i>
        <p class="text-muted mt-2">No categories found</p>
      </div>
    </div>
  `
})
export class InventorySearchComponent implements OnInit {
  categories: Category[] = [];
  showCreateForm: boolean = false;
  newCategory: CreateCategory = { name: '', description: '' };

  constructor(private inventoryService: InventoryService) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.inventoryService.getCategories().subscribe({
      next: (categories) => this.categories = categories,
      error: (err) => console.error('Failed to load categories', err)
    });
  }

  createCategory(): void {
    if (!this.newCategory.name) return;
    this.inventoryService.createCategory(this.newCategory).subscribe({
      next: () => {
        this.newCategory = { name: '', description: '' };
        this.showCreateForm = false;
        this.loadCategories();
      },
      error: (err) => console.error('Failed to create category', err)
    });
  }
}
