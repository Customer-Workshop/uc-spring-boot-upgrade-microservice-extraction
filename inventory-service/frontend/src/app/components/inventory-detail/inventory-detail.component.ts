import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InventoryService } from '../../services/inventory.service';
import { InventoryItem } from '../../models/inventory.model';

@Component({
  selector: 'app-inventory-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div *ngIf="item">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>
          <i class="bi bi-box me-2"></i>{{ item.name }}
        </h2>
        <div>
          <a [routerLink]="['/inventory', item.id, 'edit']" class="btn btn-primary me-2">
            <i class="bi bi-pencil me-1"></i>Edit
          </a>
          <button class="btn btn-danger" (click)="deleteItem()">
            <i class="bi bi-trash me-1"></i>Delete
          </button>
        </div>
      </div>

      <div class="row">
        <div class="col-md-8">
          <div class="card mb-4">
            <div class="card-header">
              <h5 class="card-title mb-0">Item Details</h5>
            </div>
            <div class="card-body">
              <div class="row mb-3">
                <div class="col-sm-3 fw-bold">SKU</div>
                <div class="col-sm-9"><code>{{ item.sku }}</code></div>
              </div>
              <div class="row mb-3">
                <div class="col-sm-3 fw-bold">Name</div>
                <div class="col-sm-9">{{ item.name }}</div>
              </div>
              <div class="row mb-3">
                <div class="col-sm-3 fw-bold">Description</div>
                <div class="col-sm-9">{{ item.description || 'N/A' }}</div>
              </div>
              <div class="row mb-3">
                <div class="col-sm-3 fw-bold">Category</div>
                <div class="col-sm-9">{{ item.categoryName }}</div>
              </div>
              <div class="row mb-3">
                <div class="col-sm-3 fw-bold">Location</div>
                <div class="col-sm-9">{{ item.location || 'N/A' }}</div>
              </div>
              <div class="row mb-3">
                <div class="col-sm-3 fw-bold">Unit Price</div>
                <div class="col-sm-9">{{ item.unitPrice | currency }}</div>
              </div>
              <div class="row mb-3">
                <div class="col-sm-3 fw-bold">Status</div>
                <div class="col-sm-9">
                  <span [class]="item.isActive ? 'badge bg-success' : 'badge bg-secondary'">
                    {{ item.isActive ? 'Active' : 'Inactive' }}
                  </span>
                </div>
              </div>
              <div class="row mb-3">
                <div class="col-sm-3 fw-bold">Created</div>
                <div class="col-sm-9">{{ item.createdAt | date:'medium' }}</div>
              </div>
              <div class="row">
                <div class="col-sm-3 fw-bold">Updated</div>
                <div class="col-sm-9">{{ item.updatedAt | date:'medium' }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="col-md-4">
          <div class="card mb-4" [class.border-danger]="item.quantityOnHand <= item.reorderLevel">
            <div class="card-header">
              <h5 class="card-title mb-0">Stock Information</h5>
            </div>
            <div class="card-body text-center">
              <div class="display-4 mb-2" [class.text-danger]="item.quantityOnHand <= item.reorderLevel"
                [class.text-success]="item.quantityOnHand > item.reorderLevel">
                {{ item.quantityOnHand }}
              </div>
              <p class="text-muted">Quantity on Hand</p>
              <p class="mb-1">Reorder Level: <strong>{{ item.reorderLevel }}</strong></p>
              <div *ngIf="item.quantityOnHand <= item.reorderLevel" class="alert alert-danger mt-3 mb-0">
                <i class="bi bi-exclamation-triangle me-1"></i>
                Stock is at or below reorder level!
              </div>
            </div>
          </div>

          <div class="card">
            <div class="card-header">
              <h5 class="card-title mb-0">Adjust Quantity</h5>
            </div>
            <div class="card-body">
              <div class="mb-3">
                <label class="form-label">Adjustment</label>
                <input type="number" class="form-control" [(ngModel)]="adjustmentAmount"
                  placeholder="e.g., 10 or -5">
              </div>
              <div class="mb-3">
                <label class="form-label">Reason</label>
                <input type="text" class="form-control" [(ngModel)]="adjustmentReason"
                  placeholder="e.g., Restock, Sold, Damaged">
              </div>
              <button class="btn btn-warning w-100" (click)="adjustQuantity()"
                [disabled]="!adjustmentAmount">
                <i class="bi bi-arrow-left-right me-1"></i>Adjust Stock
              </button>
            </div>
          </div>
        </div>
      </div>

      <a routerLink="/inventory" class="btn btn-outline-secondary mt-3">
        <i class="bi bi-arrow-left me-1"></i>Back to List
      </a>
    </div>

    <div *ngIf="!item && !loading" class="text-center py-5">
      <i class="bi bi-exclamation-circle fs-1 text-muted"></i>
      <p class="text-muted mt-2">Item not found</p>
      <a routerLink="/inventory" class="btn btn-primary">Back to Inventory</a>
    </div>
  `
})
export class InventoryDetailComponent implements OnInit {
  item: InventoryItem | null = null;
  loading: boolean = true;
  adjustmentAmount: number = 0;
  adjustmentReason: string = '';

  constructor(
    private inventoryService: InventoryService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadItem(id);
  }

  loadItem(id: number): void {
    this.inventoryService.getById(id).subscribe({
      next: (item) => {
        this.item = item;
        this.loading = false;
      },
      error: () => {
        this.item = null;
        this.loading = false;
      }
    });
  }

  adjustQuantity(): void {
    if (!this.item || !this.adjustmentAmount) return;
    this.inventoryService.adjustQuantity(this.item.id, {
      adjustment: this.adjustmentAmount,
      reason: this.adjustmentReason
    }).subscribe({
      next: (updated) => {
        this.item = updated;
        this.adjustmentAmount = 0;
        this.adjustmentReason = '';
      },
      error: (err) => console.error('Failed to adjust quantity', err)
    });
  }

  deleteItem(): void {
    if (!this.item) return;
    if (confirm('Are you sure you want to delete this item?')) {
      this.inventoryService.delete(this.item.id).subscribe({
        next: () => this.router.navigate(['/inventory']),
        error: (err) => console.error('Failed to delete item', err)
      });
    }
  }
}
