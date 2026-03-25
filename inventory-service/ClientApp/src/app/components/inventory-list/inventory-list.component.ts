import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InventoryService } from '../../services/inventory.service';
import { InventoryItem, PagedResult, InventorySearchParams } from '../../models/inventory.model';

@Component({
  selector: 'app-inventory-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory-list.component.html',
  styleUrls: ['./inventory-list.component.css'],
})
export class InventoryListComponent implements OnInit {
  items: InventoryItem[] = [];
  categories: string[] = [];
  totalCount = 0;
  totalPages = 0;
  loading = false;
  error: string | null = null;

  searchParams: InventorySearchParams = {
    page: 1,
    pageSize: 20,
    sortBy: 'name',
    sortDirection: 'asc',
  };

  constructor(
    private inventoryService: InventoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadItems();
    this.loadCategories();
  }

  loadItems(): void {
    this.loading = true;
    this.error = null;

    this.inventoryService.getItems(this.searchParams).subscribe({
      next: (result: PagedResult<InventoryItem>) => {
        this.items = result.items;
        this.totalCount = result.totalCount;
        this.totalPages = result.totalPages;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load inventory items';
        this.loading = false;
        console.error('Error loading items:', err);
      },
    });
  }

  loadCategories(): void {
    this.inventoryService.getCategories().subscribe({
      next: (categories) => (this.categories = categories),
      error: (err) => console.error('Error loading categories:', err),
    });
  }

  onSearch(): void {
    this.searchParams.page = 1;
    this.loadItems();
  }

  onCategoryChange(category: string): void {
    this.searchParams.category = category || undefined;
    this.searchParams.page = 1;
    this.loadItems();
  }

  onSort(field: string): void {
    if (this.searchParams.sortBy === field) {
      this.searchParams.sortDirection =
        this.searchParams.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.searchParams.sortBy = field;
      this.searchParams.sortDirection = 'asc';
    }
    this.loadItems();
  }

  onPageChange(page: number): void {
    this.searchParams.page = page;
    this.loadItems();
  }

  viewItem(id: string): void {
    this.router.navigate(['/inventory', id]);
  }

  createItem(): void {
    this.router.navigate(['/inventory/new']);
  }

  deleteItem(id: string, event: Event): void {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this item?')) {
      this.inventoryService.deleteItem(id).subscribe({
        next: () => this.loadItems(),
        error: (err) => {
          this.error = 'Failed to delete item';
          console.error('Error deleting item:', err);
        },
      });
    }
  }

  isLowStock(item: InventoryItem): boolean {
    return item.quantity <= item.reorderLevel;
  }

  getSortIcon(field: string): string {
    if (this.searchParams.sortBy !== field) return '↕';
    return this.searchParams.sortDirection === 'asc' ? '↑' : '↓';
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }
}
