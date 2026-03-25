import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventoryService } from '../../services/inventory.service';
import { StockAdjustment } from '../../models/inventory.model';

@Component({
  selector: 'app-stock-adjustment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stock-adjustment.component.html',
  styleUrls: ['./stock-adjustment.component.css'],
})
export class StockAdjustmentComponent {
  @Input() itemId!: string;
  @Input() currentQuantity!: number;
  @Output() adjusted = new EventEmitter<void>();

  adjustment: StockAdjustment = {
    quantity: 0,
    movementType: 'IN',
    reference: '',
    notes: '',
  };

  saving = false;
  error: string | null = null;

  constructor(private inventoryService: InventoryService) {}

  onSubmit(): void {
    this.saving = true;
    this.error = null;

    this.inventoryService.adjustStock(this.itemId, this.adjustment).subscribe({
      next: () => {
        this.saving = false;
        this.adjusted.emit();
      },
      error: (err) => {
        this.error = 'Failed to adjust stock';
        this.saving = false;
        console.error('Error adjusting stock:', err);
      },
    });
  }

  get previewQuantity(): number {
    switch (this.adjustment.movementType) {
      case 'IN':
        return this.currentQuantity + this.adjustment.quantity;
      case 'OUT':
        return this.currentQuantity - this.adjustment.quantity;
      case 'ADJUSTMENT':
        return this.adjustment.quantity;
      default:
        return this.currentQuantity;
    }
  }
}
