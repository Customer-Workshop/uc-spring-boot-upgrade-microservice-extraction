import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { InventoryService } from '../../services/inventory.service';
import { Category } from '../../models/inventory.model';

@Component({
  selector: 'app-inventory-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="row justify-content-center">
      <div class="col-md-8">
        <div class="card">
          <div class="card-header">
            <h4 class="card-title mb-0">
              <i class="bi" [class.bi-plus-lg]="!isEdit" [class.bi-pencil]="isEdit"></i>
              {{ isEdit ? 'Edit Inventory Item' : 'New Inventory Item' }}
            </h4>
          </div>
          <div class="card-body">
            <form [formGroup]="form" (ngSubmit)="onSubmit()">
              <div class="row mb-3">
                <div class="col-md-6">
                  <label class="form-label">SKU *</label>
                  <input type="text" class="form-control" formControlName="sku"
                    [class.is-invalid]="form.get('sku')?.invalid && form.get('sku')?.touched"
                    placeholder="e.g., ELEC-001">
                  <div class="invalid-feedback">SKU is required</div>
                </div>
                <div class="col-md-6">
                  <label class="form-label">Name *</label>
                  <input type="text" class="form-control" formControlName="name"
                    [class.is-invalid]="form.get('name')?.invalid && form.get('name')?.touched"
                    placeholder="Item name">
                  <div class="invalid-feedback">Name is required</div>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label">Description</label>
                <textarea class="form-control" formControlName="description" rows="3"
                  placeholder="Optional description"></textarea>
              </div>

              <div class="row mb-3">
                <div class="col-md-6">
                  <label class="form-label">Category *</label>
                  <select class="form-select" formControlName="categoryId"
                    [class.is-invalid]="form.get('categoryId')?.invalid && form.get('categoryId')?.touched">
                    <option [ngValue]="null" disabled>Select a category</option>
                    <option *ngFor="let cat of categories" [ngValue]="cat.id">{{ cat.name }}</option>
                  </select>
                  <div class="invalid-feedback">Category is required</div>
                </div>
                <div class="col-md-6">
                  <label class="form-label">Location</label>
                  <input type="text" class="form-control" formControlName="location"
                    placeholder="e.g., Warehouse A - Shelf 1">
                </div>
              </div>

              <div class="row mb-3">
                <div class="col-md-4">
                  <label class="form-label">Quantity On Hand *</label>
                  <input type="number" class="form-control" formControlName="quantityOnHand"
                    [class.is-invalid]="form.get('quantityOnHand')?.invalid && form.get('quantityOnHand')?.touched"
                    min="0">
                  <div class="invalid-feedback">Valid quantity is required</div>
                </div>
                <div class="col-md-4">
                  <label class="form-label">Reorder Level *</label>
                  <input type="number" class="form-control" formControlName="reorderLevel"
                    [class.is-invalid]="form.get('reorderLevel')?.invalid && form.get('reorderLevel')?.touched"
                    min="0">
                  <div class="invalid-feedback">Valid reorder level is required</div>
                </div>
                <div class="col-md-4">
                  <label class="form-label">Unit Price *</label>
                  <div class="input-group">
                    <span class="input-group-text">$</span>
                    <input type="number" class="form-control" formControlName="unitPrice"
                      [class.is-invalid]="form.get('unitPrice')?.invalid && form.get('unitPrice')?.touched"
                      min="0" step="0.01">
                  </div>
                  <div class="invalid-feedback">Valid price is required</div>
                </div>
              </div>

              <div class="d-flex justify-content-between mt-4">
                <a routerLink="/inventory" class="btn btn-outline-secondary">
                  <i class="bi bi-arrow-left me-1"></i>Cancel
                </a>
                <button type="submit" class="btn btn-primary" [disabled]="form.invalid || submitting">
                  <span *ngIf="submitting" class="spinner-border spinner-border-sm me-1"></span>
                  <i *ngIf="!submitting" class="bi" [class.bi-plus-lg]="!isEdit" [class.bi-check-lg]="isEdit"></i>
                  {{ isEdit ? 'Update Item' : 'Create Item' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  `
})
export class InventoryFormComponent implements OnInit {
  form!: FormGroup;
  categories: Category[] = [];
  isEdit: boolean = false;
  itemId: number | null = null;
  submitting: boolean = false;

  constructor(
    private fb: FormBuilder,
    private inventoryService: InventoryService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadCategories();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.itemId = Number(id);
      this.loadItem(this.itemId);
    }
  }

  initForm(): void {
    this.form = this.fb.group({
      sku: ['', [Validators.required, Validators.maxLength(100)]],
      name: ['', [Validators.required, Validators.maxLength(255)]],
      description: [''],
      categoryId: [null, Validators.required],
      quantityOnHand: [0, [Validators.required, Validators.min(0)]],
      reorderLevel: [0, [Validators.required, Validators.min(0)]],
      unitPrice: [0, [Validators.required, Validators.min(0)]],
      location: ['']
    });
  }

  loadCategories(): void {
    this.inventoryService.getCategories().subscribe({
      next: (categories) => this.categories = categories,
      error: (err) => console.error('Failed to load categories', err)
    });
  }

  loadItem(id: number): void {
    this.inventoryService.getById(id).subscribe({
      next: (item) => {
        this.form.patchValue({
          sku: item.sku,
          name: item.name,
          description: item.description,
          categoryId: item.categoryId,
          quantityOnHand: item.quantityOnHand,
          reorderLevel: item.reorderLevel,
          unitPrice: item.unitPrice,
          location: item.location
        });
        if (this.isEdit) {
          this.form.get('sku')?.disable();
        }
      },
      error: () => this.router.navigate(['/inventory'])
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitting = true;
    const formValue = this.form.getRawValue();

    if (this.isEdit && this.itemId) {
      this.inventoryService.update(this.itemId, formValue).subscribe({
        next: () => this.router.navigate(['/inventory', this.itemId]),
        error: (err) => {
          console.error('Failed to update item', err);
          this.submitting = false;
        }
      });
    } else {
      this.inventoryService.create(formValue).subscribe({
        next: (item) => this.router.navigate(['/inventory', item.id]),
        error: (err) => {
          console.error('Failed to create item', err);
          this.submitting = false;
        }
      });
    }
  }
}
