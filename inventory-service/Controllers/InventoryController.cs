using Microsoft.AspNetCore.Mvc;
using InventoryService.Models.DTOs;
using InventoryService.Services;

namespace InventoryService.Controllers;

[ApiController]
[Route("api/v1/[controller]")]
[Produces("application/json")]
public class InventoryController : ControllerBase
{
    private readonly IInventoryService _inventoryService;
    private readonly ILogger<InventoryController> _logger;

    public InventoryController(IInventoryService inventoryService, ILogger<InventoryController> logger)
    {
        _inventoryService = inventoryService;
        _logger = logger;
    }

    /// <summary>
    /// Get all inventory items with search, filter, and pagination
    /// </summary>
    [HttpGet]
    [ProducesResponseType(typeof(PagedResult<InventoryItemDto>), StatusCodes.Status200OK)]
    public async Task<ActionResult<PagedResult<InventoryItemDto>>> GetItems(
        [FromQuery] string? searchTerm,
        [FromQuery] string? category,
        [FromQuery] bool? isActive,
        [FromQuery] bool? lowStock,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20,
        [FromQuery] string sortBy = "name",
        [FromQuery] string sortDirection = "asc")
    {
        var searchParams = new InventorySearchParams(
            searchTerm, category, isActive, lowStock,
            page, pageSize, sortBy, sortDirection);

        var result = await _inventoryService.GetItemsAsync(searchParams);
        return Ok(result);
    }

    /// <summary>
    /// Get a specific inventory item by ID
    /// </summary>
    [HttpGet("{id:guid}")]
    [ProducesResponseType(typeof(InventoryItemDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<InventoryItemDto>> GetItem(Guid id)
    {
        var item = await _inventoryService.GetItemByIdAsync(id);
        if (item == null)
            return NotFound(new { message = $"Inventory item with ID {id} not found" });

        return Ok(item);
    }

    /// <summary>
    /// Get a specific inventory item by SKU
    /// </summary>
    [HttpGet("sku/{sku}")]
    [ProducesResponseType(typeof(InventoryItemDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<InventoryItemDto>> GetItemBySku(string sku)
    {
        var item = await _inventoryService.GetItemBySkuAsync(sku);
        if (item == null)
            return NotFound(new { message = $"Inventory item with SKU {sku} not found" });

        return Ok(item);
    }

    /// <summary>
    /// Create a new inventory item
    /// </summary>
    [HttpPost]
    [ProducesResponseType(typeof(InventoryItemDto), StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<ActionResult<InventoryItemDto>> CreateItem([FromBody] CreateInventoryItemDto dto)
    {
        try
        {
            var item = await _inventoryService.CreateItemAsync(dto);
            return CreatedAtAction(nameof(GetItem), new { id = item.Id }, item);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error creating inventory item");
            return BadRequest(new { message = ex.Message });
        }
    }

    /// <summary>
    /// Update an existing inventory item
    /// </summary>
    [HttpPut("{id:guid}")]
    [ProducesResponseType(typeof(InventoryItemDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<InventoryItemDto>> UpdateItem(Guid id, [FromBody] UpdateInventoryItemDto dto)
    {
        var item = await _inventoryService.UpdateItemAsync(id, dto);
        if (item == null)
            return NotFound(new { message = $"Inventory item with ID {id} not found" });

        return Ok(item);
    }

    /// <summary>
    /// Delete an inventory item
    /// </summary>
    [HttpDelete("{id:guid}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> DeleteItem(Guid id)
    {
        var result = await _inventoryService.DeleteItemAsync(id);
        if (!result)
            return NotFound(new { message = $"Inventory item with ID {id} not found" });

        return NoContent();
    }

    /// <summary>
    /// Adjust stock for an inventory item (IN, OUT, or ADJUSTMENT)
    /// </summary>
    [HttpPost("{id:guid}/stock")]
    [ProducesResponseType(typeof(InventoryItemDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<InventoryItemDto>> AdjustStock(Guid id, [FromBody] StockAdjustmentDto dto)
    {
        var item = await _inventoryService.AdjustStockAsync(id, dto);
        if (item == null)
            return BadRequest(new { message = "Stock adjustment failed. Item not found or invalid movement." });

        return Ok(item);
    }

    /// <summary>
    /// Get stock movement history for an inventory item
    /// </summary>
    [HttpGet("{id:guid}/movements")]
    [ProducesResponseType(typeof(IEnumerable<StockMovementDto>), StatusCodes.Status200OK)]
    public async Task<ActionResult<IEnumerable<StockMovementDto>>> GetStockMovements(
        Guid id, [FromQuery] int limit = 50)
    {
        var movements = await _inventoryService.GetStockMovementsAsync(id, limit);
        return Ok(movements);
    }

    /// <summary>
    /// Get all items that are below their reorder level
    /// </summary>
    [HttpGet("low-stock")]
    [ProducesResponseType(typeof(IEnumerable<InventoryItemDto>), StatusCodes.Status200OK)]
    public async Task<ActionResult<IEnumerable<InventoryItemDto>>> GetLowStockItems()
    {
        var items = await _inventoryService.GetLowStockItemsAsync();
        return Ok(items);
    }

    /// <summary>
    /// Get all distinct categories
    /// </summary>
    [HttpGet("categories")]
    [ProducesResponseType(typeof(IEnumerable<string>), StatusCodes.Status200OK)]
    public async Task<ActionResult<IEnumerable<string>>> GetCategories()
    {
        var categories = await _inventoryService.GetCategoriesAsync();
        return Ok(categories);
    }
}
