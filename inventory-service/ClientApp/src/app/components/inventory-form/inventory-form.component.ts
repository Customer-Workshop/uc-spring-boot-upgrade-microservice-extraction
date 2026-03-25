import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InventoryService } from '../../services/inventory.service';
import { CreateInventoryItem, UpdateInventoryItem } from '../../models/inventory.model';

@Component({
  selector: 'app-inventory-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory-form.component.html',
  styleUrls: ['./inventory-form.component.css'],
})
export class InventoryFormComponent implements OnInit {
  isEditMode = false;
  itemId: string | null = null;
  loading = false;
  saving = false;
  error: string | null = null;

  formData: CreateInventoryItem = {
    sku: '',
    name: '',
    description: '',
    category: '',
    quantity: 0,
    unitPrice: 0,
    reorderLevel: 10,
    warehouseLocation: '',
    supplier: '',
  };

  categories: string[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private inventoryService: InventoryService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.itemId = this.route.snapshot.paramMap.get('id');
    if (this.itemId) {
      this.isEditMode = true;
      this.loadItem(this.itemId);
    }
  }

  loadItem(id: string): void {
    this.loading = true;
    this.inventoryService.getItem(id).subscribe({
      next: (item) => {
        this.formData = {
          sku: item.sku,
          name: item.name,
          description: item.description || '',
          category: item.category,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
          reorderLevel: item.reorderLevel,
          warehouseLocation: item.warehouseLocation || '',
          supplier: item.supplier || '',
        };
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load item';
        this.loading = false;
        console.error('Error loading item:', err);
      },
    });
  }

  loadCategories(): void {
    this.inventoryService.getCategories().subscribe({
      next: (categories) => (this.categories = categories),
      error: (err) => console.error('Error loading categories:', err),
    });
  }

  onSubmit(): void {
    this.saving = true;
    this.error = null;

    if (this.isEditMode && this.itemId) {
      const updateDto: UpdateInventoryItem = {
        name: this.formData.name,
        description: this.formData.description,
        category: this.formData.category,
        unitPrice: this.formData.unitPrice,
        reorderLevel: this.formData.reorderLevel,
        warehouseLocation: this.formData.warehouseLocation,
        supplier: this.formData.supplier,
      };

      this.inventoryService.updateItem(this.itemId, updateDto).subscribe({
        next: () => {
          this.router.navigate(['/inventory', this.itemId]);
        },
        error: (err) => {
          this.error = 'Failed to update item';
          this.saving = false;
          console.error('Error updating item:', err);
        },
      });
    } else {
      this.inventoryService.createItem(this.formData).subscribe({
        next: (item) => {
          this.router.navigate(['/inventory', item.id]);
        },
        error: (err) => {
          this.error = 'Failed to create item';
          this.saving = false;
          console.error('Error creating item:', err);
        },
      });
    }
  }

  cancel(): void {
    if (this.isEditMode && this.itemId) {
      this.router.navigate(['/inventory', this.itemId]);
    } else {
      this.router.navigate(['/inventory']);
    }
  }
}
