import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { InventoryService } from '../../services/inventory.service';
import { InventoryItem } from '../../models/inventory.model';

@Component({
  selector: 'app-low-stock-alert',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './low-stock-alert.component.html',
  styleUrls: ['./low-stock-alert.component.css'],
})
export class LowStockAlertComponent implements OnInit {
  lowStockItems: InventoryItem[] = [];
  loading = true;

  constructor(
    private inventoryService: InventoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadLowStockItems();
  }

  loadLowStockItems(): void {
    this.loading = true;
    this.inventoryService.getLowStockItems().subscribe({
      next: (items) => {
        this.lowStockItems = items;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        console.error('Error loading low stock items:', err);
      },
    });
  }

  viewItem(id: string): void {
    this.router.navigate(['/inventory', id]);
  }

  getStockPercentage(item: InventoryItem): number {
    if (item.reorderLevel === 0) return 100;
    return Math.min(100, Math.round((item.quantity / item.reorderLevel) * 100));
  }
}
