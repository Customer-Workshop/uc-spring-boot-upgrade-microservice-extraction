import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  InventoryItem,
  CreateInventoryItem,
  UpdateInventoryItem,
  StockAdjustment,
  StockMovement,
  PagedResult,
  InventorySearchParams,
} from '../models/inventory.model';

@Injectable({
  providedIn: 'root',
})
export class InventoryService {
  private readonly apiUrl = `${environment.apiBaseUrl}/api/v1/inventory`;

  constructor(private http: HttpClient) {}

  getItems(params?: InventorySearchParams): Observable<PagedResult<InventoryItem>> {
    let httpParams = new HttpParams();

    if (params) {
      if (params.searchTerm) httpParams = httpParams.set('searchTerm', params.searchTerm);
      if (params.category) httpParams = httpParams.set('category', params.category);
      if (params.isActive !== undefined) httpParams = httpParams.set('isActive', params.isActive.toString());
      if (params.lowStock !== undefined) httpParams = httpParams.set('lowStock', params.lowStock.toString());
      if (params.page) httpParams = httpParams.set('page', params.page.toString());
      if (params.pageSize) httpParams = httpParams.set('pageSize', params.pageSize.toString());
      if (params.sortBy) httpParams = httpParams.set('sortBy', params.sortBy);
      if (params.sortDirection) httpParams = httpParams.set('sortDirection', params.sortDirection);
    }

    return this.http.get<PagedResult<InventoryItem>>(this.apiUrl, { params: httpParams });
  }

  getItem(id: string): Observable<InventoryItem> {
    return this.http.get<InventoryItem>(`${this.apiUrl}/${id}`);
  }

  getItemBySku(sku: string): Observable<InventoryItem> {
    return this.http.get<InventoryItem>(`${this.apiUrl}/sku/${sku}`);
  }

  createItem(item: CreateInventoryItem): Observable<InventoryItem> {
    return this.http.post<InventoryItem>(this.apiUrl, item);
  }

  updateItem(id: string, item: UpdateInventoryItem): Observable<InventoryItem> {
    return this.http.put<InventoryItem>(`${this.apiUrl}/${id}`, item);
  }

  deleteItem(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  adjustStock(id: string, adjustment: StockAdjustment): Observable<InventoryItem> {
    return this.http.post<InventoryItem>(`${this.apiUrl}/${id}/stock`, adjustment);
  }

  getStockMovements(id: string, limit: number = 50): Observable<StockMovement[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<StockMovement[]>(`${this.apiUrl}/${id}/movements`, { params });
  }

  getLowStockItems(): Observable<InventoryItem[]> {
    return this.http.get<InventoryItem[]>(`${this.apiUrl}/low-stock`);
  }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/categories`);
  }
}
