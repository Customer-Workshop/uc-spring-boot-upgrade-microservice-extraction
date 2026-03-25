export interface InventoryItem {
  id: string;
  sku: string;
  name: string;
  description?: string;
  category: string;
  quantity: number;
  unitPrice: number;
  reorderLevel: number;
  warehouseLocation?: string;
  supplier?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateInventoryItem {
  sku: string;
  name: string;
  description?: string;
  category: string;
  quantity: number;
  unitPrice: number;
  reorderLevel: number;
  warehouseLocation?: string;
  supplier?: string;
}

export interface UpdateInventoryItem {
  name?: string;
  description?: string;
  category?: string;
  unitPrice?: number;
  reorderLevel?: number;
  warehouseLocation?: string;
  supplier?: string;
  isActive?: boolean;
}

export interface StockAdjustment {
  quantity: number;
  movementType: 'IN' | 'OUT' | 'ADJUSTMENT';
  reference?: string;
  notes?: string;
}

export interface StockMovement {
  id: string;
  inventoryItemId: string;
  movementType: string;
  quantity: number;
  previousQuantity: number;
  newQuantity: number;
  reference?: string;
  notes?: string;
  createdAt: string;
  createdBy?: string;
}

export interface PagedResult<T> {
  items: T[];
  totalCount: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface InventorySearchParams {
  searchTerm?: string;
  category?: string;
  isActive?: boolean;
  lowStock?: boolean;
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}
