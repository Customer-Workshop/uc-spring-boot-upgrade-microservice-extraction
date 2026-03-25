import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/inventory', pathMatch: 'full' },
  {
    path: 'inventory',
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./components/inventory-list/inventory-list.component').then(m => m.InventoryListComponent)
      },
      {
        path: 'low-stock',
        loadComponent: () =>
          import('./components/inventory-list/inventory-list.component').then(m => m.InventoryListComponent),
        data: { lowStockOnly: true }
      },
      {
        path: 'new',
        loadComponent: () =>
          import('./components/inventory-form/inventory-form.component').then(m => m.InventoryFormComponent)
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./components/inventory-detail/inventory-detail.component').then(m => m.InventoryDetailComponent)
      },
      {
        path: ':id/edit',
        loadComponent: () =>
          import('./components/inventory-form/inventory-form.component').then(m => m.InventoryFormComponent)
      }
    ]
  },
  {
    path: 'categories',
    loadComponent: () =>
      import('./components/inventory-search/inventory-search.component').then(m => m.InventorySearchComponent)
  }
];
