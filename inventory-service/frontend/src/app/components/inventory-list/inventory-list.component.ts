import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InventoryService } from '../../services/inventory.service';
import { InventoryItem, InventoryPagedResponse, Category } from '../../models/inventory.model';

@Component({
  selector: 'app-inventory-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>
        <i class="bi bi-box-seam me-2"></i>
        {{ lowStockOnly ? 'Low Stock Items' : 'Inventory Items' }}
      </h2>
      <a routerLink="/inventory/new" class="btn btn-primary" *ngIf="!lowStockOnly">
        <i class="bi bi-plus-lg me-1"></i>Add Item
      </a>
    </div>

    <div class="card mb-4" *ngIf="!lowStockOnly">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-4">
            <input type="text" class="form-control search-input" placeholder="Search by name, SKU, or description..."
              [(ngModel)]="searchTerm" (keyup.enter)="loadItems()">
          </div>
          <div class="col-md-3">
            <select class="form-select" [(ngModel)]="selectedCategoryId" (change)="loadItems()">
              <option [ngValue]="null">All Categories</option>
              <option *ngFor="let cat of categories" [ngValue]="cat.id">{{ cat.name }}</option>
            </select>
          </div>
          <div class="col-md-2">
            <select class="form-select" [(ngModel)]="selectedStatus" (change)="loadItems()">
              <option [ngValue]="null">All Status</option>
              <option [ngValue]="true">Active</option>
              <option [ngValue]="false">Inactive</option>
            </select>
          </div>
          <div class="col-md-3">
            <button class="btn btn-outline-secondary me-2" (click)="loadItems()">
              <i class="bi bi-search me-1"></i>Search
            </button>
            <button class="btn btn-outline-danger" (click)="clearFilters()">
              <i class="bi bi-x-lg me-1"></i>Clear
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead class="table-light">
            <tr>
              <th>SKU</th>
              <th>Name</th>
              <th>Category</th>
              <th class="text-end">Qty On Hand</th>
              <th class="text-end">Reorder Level</th>
              <th class="text-end">Unit Price</th>
              <th>Location</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of items" [class.table-danger]="item.quantityOnHand <= item.reorderLevel">
              <td><code>{{ item.sku }}</code></td>
              <td>
                <a [routerLink]="['/inventory', item.id]">{{ item.name }}</a>
              </td>
              <td>{{ item.categoryName }}</td>
              <td class="text-end">
                <span [class]="item.quantityOnHand <= item.reorderLevel ? 'badge bg-danger' : 'badge bg-success'">
                  {{ item.quantityOnHand }}
                </span>
              </td>
              <td class="text-end">{{ item.reorderLevel }}</td>
              <td class="text-end">{{ item.unitPrice | currency }}</td>
              <td>{{ item.location }}</td>
              <td>
                <span [class]="item.isActive ? 'badge bg-success' : 'badge bg-secondary'">
                  {{ item.isActive ? 'Active' : 'Inactive' }}
                </span>
              </td>
              <td>
                <a [routerLink]="['/inventory', item.id, 'edit']" class="btn btn-sm btn-outline-primary me-1">
                  <i class="bi bi-pencil"></i>
                </a>
                <a [routerLink]="['/inventory', item.id]" class="btn btn-sm btn-outline-info">
                  <i class="bi bi-eye"></i>
                </a>
              </td>
            </tr>
            <tr *ngIf="items.length === 0">
              <td colspan="9" class="text-center py-4 text-muted">
                <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                No inventory items found
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <nav *ngIf="totalPages > 1" class="mt-4">
      <ul class="pagination justify-content-center">
        <li class="page-item" [class.disabled]="currentPage === 1">
          <a class="page-link" (click)="goToPage(currentPage - 1)">Previous</a>
        </li>
        <li *ngFor="let p of pageNumbers" class="page-item" [class.active]="p === currentPage">
          <a class="page-link" (click)="goToPage(p)">{{ p }}</a>
        </li>
        <li class="page-item" [class.disabled]="currentPage === totalPages">
          <a class="page-link" (click)="goToPage(currentPage + 1)">Next</a>
        </li>
      </ul>
    </nav>

    <div class="text-center text-muted mt-2" *ngIf="totalCount > 0">
      Showing {{ items.length }} of {{ totalCount }} items
    </div>
  `
})
export class InventoryListComponent implements OnInit {
  items: InventoryItem[] = [];
  categories: Category[] = [];
  searchTerm: string = '';
  selectedCategoryId: number | null = null;
  selectedStatus: boolean | null = null;
  currentPage: number = 1;
  pageSize: number = 20;
  totalCount: number = 0;
  totalPages: number = 0;
  lowStockOnly: boolean = false;

  constructor(
    private inventoryService: InventoryService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.lowStockOnly = this.route.snapshot.data['lowStockOnly'] === true;
    this.loadCategories();
    this.loadItems();
  }

  loadItems(): void {
    if (this.lowStockOnly) {
      this.inventoryService.getLowStock().subscribe({
        next: (items) => {
          this.items = items;
          this.totalCount = items.length;
        },
        error: (err) => console.error('Failed to load low stock items', err)
      });
    } else {
      this.inventoryService.getAll(
        this.currentPage,
        this.pageSize,
        this.searchTerm || undefined,
        this.selectedCategoryId ?? undefined,
        this.selectedStatus ?? undefined
      ).subscribe({
        next: (response: InventoryPagedResponse) => {
          this.items = response.items;
          this.totalCount = response.totalCount;
          this.totalPages = response.totalPages;
        },
        error: (err) => console.error('Failed to load inventory items', err)
      });
    }
  }

  loadCategories(): void {
    this.inventoryService.getCategories().subscribe({
      next: (categories) => this.categories = categories,
      error: (err) => console.error('Failed to load categories', err)
    });
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedCategoryId = null;
    this.selectedStatus = null;
    this.currentPage = 1;
    this.loadItems();
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadItems();
    }
  }

  get pageNumbers(): number[] {
    const pages: number[] = [];
    const start = Math.max(1, this.currentPage - 2);
    const end = Math.min(this.totalPages, this.currentPage + 2);
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }
}
