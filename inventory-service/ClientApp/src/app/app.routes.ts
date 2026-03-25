import { Routes } from '@angular/router';
import { InventoryListComponent } from './components/inventory-list/inventory-list.component';
import { InventoryDetailComponent } from './components/inventory-detail/inventory-detail.component';
import { InventoryFormComponent } from './components/inventory-form/inventory-form.component';
import { LowStockAlertComponent } from './components/low-stock-alert/low-stock-alert.component';

export const routes: Routes = [
  { path: '', redirectTo: '/inventory', pathMatch: 'full' },
  { path: 'inventory', component: InventoryListComponent },
  { path: 'inventory/new', component: InventoryFormComponent },
  { path: 'inventory/low-stock', component: LowStockAlertComponent },
  { path: 'inventory/:id', component: InventoryDetailComponent },
  { path: 'inventory/:id/edit', component: InventoryFormComponent },
  { path: '**', redirectTo: '/inventory' },
];
