import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { InventoryService } from '../../services/inventory.service';
import { InventoryItem, StockMovement } from '../../models/inventory.model';
import { StockAdjustmentComponent } from '../stock-adjustment/stock-adjustment.component';

@Component({
  selector: 'app-inventory-detail',
  standalone: true,
  imports: [CommonModule, StockAdjustmentComponent],
  templateUrl: './inventory-detail.component.html',
  styleUrls: ['./inventory-detail.component.css'],
})
export class InventoryDetailComponent implements OnInit {
  item: InventoryItem | null = null;
  movements: StockMovement[] = [];
  loading = true;
  showStockAdjustment = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private inventoryService: InventoryService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadItem(id);
      this.loadMovements(id);
    }
  }

  loadItem(id: string): void {
    this.loading = true;
    this.inventoryService.getItem(id).subscribe({
      next: (item) => {
        this.item = item;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load inventory item';
        this.loading = false;
        console.error('Error loading item:', err);
      },
    });
  }

  loadMovements(id: string): void {
    this.inventoryService.getStockMovements(id).subscribe({
      next: (movements) => (this.movements = movements),
      error: (err) => console.error('Error loading movements:', err),
    });
  }

  editItem(): void {
    if (this.item) {
      this.router.navigate(['/inventory', this.item.id, 'edit']);
    }
  }

  goBack(): void {
    this.router.navigate(['/inventory']);
  }

  toggleStockAdjustment(): void {
    this.showStockAdjustment = !this.showStockAdjustment;
  }

  onStockAdjusted(): void {
    if (this.item) {
      this.loadItem(this.item.id);
      this.loadMovements(this.item.id);
    }
    this.showStockAdjustment = false;
  }

  isLowStock(): boolean {
    return this.item ? this.item.quantity <= this.item.reorderLevel : false;
  }
}
