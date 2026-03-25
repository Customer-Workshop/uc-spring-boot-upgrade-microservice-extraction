export interface InventoryItem {
  id: number;
  sku: string;
  name: string;
  description?: string;
  categoryId: number;
  categoryName?: string;
  quantityOnHand: number;
  reorderLevel: number;
  unitPrice: number;
  location?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateInventoryItem {
  sku: string;
  name: string;
  description?: string;
  categoryId: number;
  quantityOnHand: number;
  reorderLevel: number;
  unitPrice: number;
  location?: string;
}

export interface UpdateInventoryItem {
  name?: string;
  description?: string;
  categoryId?: number;
  quantityOnHand?: number;
  reorderLevel?: number;
  unitPrice?: number;
  location?: string;
  isActive?: boolean;
}

export interface InventoryPagedResponse {
  items: InventoryItem[];
  totalCount: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface AdjustQuantity {
  adjustment: number;
  reason?: string;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
  isActive: boolean;
  itemCount: number;
  createdAt: string;
}

export interface CreateCategory {
  name: string;
  description?: string;
}
