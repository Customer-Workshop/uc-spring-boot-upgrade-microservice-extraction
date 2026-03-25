import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  InventoryItem,
  InventoryPagedResponse,
  CreateInventoryItem,
  UpdateInventoryItem,
  AdjustQuantity,
  Category,
  CreateCategory
} from '../models/inventory.model';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private readonly apiUrl = `${environment.apiUrl}/inventory`;
  private readonly categoriesUrl = `${environment.apiUrl}/categories`;

  constructor(private http: HttpClient) {}

  getAll(
    page: number = 1,
    pageSize: number = 20,
    search?: string,
    categoryId?: number,
    isActive?: boolean
  ): Observable<InventoryPagedResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('pageSize', pageSize.toString());

    if (search) params = params.set('search', search);
    if (categoryId) params = params.set('categoryId', categoryId.toString());
    if (isActive !== undefined) params = params.set('isActive', isActive.toString());

    return this.http.get<InventoryPagedResponse>(this.apiUrl, { params });
  }

  getById(id: number): Observable<InventoryItem> {
    return this.http.get<InventoryItem>(`${this.apiUrl}/${id}`);
  }

  getBySku(sku: string): Observable<InventoryItem> {
    return this.http.get<InventoryItem>(`${this.apiUrl}/sku/${sku}`);
  }

  create(item: CreateInventoryItem): Observable<InventoryItem> {
    return this.http.post<InventoryItem>(this.apiUrl, item);
  }

  update(id: number, item: UpdateInventoryItem): Observable<InventoryItem> {
    return this.http.put<InventoryItem>(`${this.apiUrl}/${id}`, item);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  adjustQuantity(id: number, adjustment: AdjustQuantity): Observable<InventoryItem> {
    return this.http.post<InventoryItem>(`${this.apiUrl}/${id}/adjust`, adjustment);
  }

  getLowStock(): Observable<InventoryItem[]> {
    return this.http.get<InventoryItem[]>(`${this.apiUrl}/low-stock`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.categoriesUrl);
  }

  createCategory(category: CreateCategory): Observable<Category> {
    return this.http.post<Category>(this.categoriesUrl, category);
  }
}
