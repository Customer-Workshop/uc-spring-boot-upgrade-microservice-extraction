using InventoryService.Models;
using InventoryService.Models.DTOs;

namespace InventoryService.Services;

public interface IInventoryService
{
    Task<PagedResult<InventoryItemDto>> GetItemsAsync(InventorySearchParams searchParams);
    Task<InventoryItemDto?> GetItemByIdAsync(Guid id);
    Task<InventoryItemDto?> GetItemBySkuAsync(string sku);
    Task<InventoryItemDto> CreateItemAsync(CreateInventoryItemDto dto);
    Task<InventoryItemDto?> UpdateItemAsync(Guid id, UpdateInventoryItemDto dto);
    Task<bool> DeleteItemAsync(Guid id);
    Task<InventoryItemDto?> AdjustStockAsync(Guid id, StockAdjustmentDto dto);
    Task<IEnumerable<StockMovementDto>> GetStockMovementsAsync(Guid itemId, int limit = 50);
    Task<IEnumerable<InventoryItemDto>> GetLowStockItemsAsync();
    Task<IEnumerable<string>> GetCategoriesAsync();
}
