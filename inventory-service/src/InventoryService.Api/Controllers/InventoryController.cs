using InventoryService.Api.Models.Dtos;
using InventoryService.Api.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InventoryService.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
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
    /// Get all inventory items with pagination and filtering
    /// </summary>
    [HttpGet]
    [ProducesResponseType(typeof(InventoryItemPagedResponse), StatusCodes.Status200OK)]
    public async Task<ActionResult<InventoryItemPagedResponse>> GetAll(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20,
        [FromQuery] string? search = null,
        [FromQuery] int? categoryId = null,
        [FromQuery] bool? isActive = null)
    {
        var result = await _inventoryService.GetAllAsync(page, pageSize, search, categoryId, isActive);
        return Ok(result);
    }

    /// <summary>
    /// Get inventory item by ID
    /// </summary>
    [HttpGet("{id:int}")]
    [ProducesResponseType(typeof(InventoryItemDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<InventoryItemDto>> GetById(int id)
    {
        var item = await _inventoryService.GetByIdAsync(id);
        if (item == null)
        {
            return NotFound(new { message = $"Inventory item with ID {id} not found" });
        }
        return Ok(item);
    }

    /// <summary>
    /// Get inventory item by SKU
    /// </summary>
    [HttpGet("sku/{sku}")]
    [ProducesResponseType(typeof(InventoryItemDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<InventoryItemDto>> GetBySku(string sku)
    {
        var item = await _inventoryService.GetBySkuAsync(sku);
        if (item == null)
        {
            return NotFound(new { message = $"Inventory item with SKU '{sku}' not found" });
        }
        return Ok(item);
    }

    /// <summary>
    /// Create a new inventory item
    /// </summary>
    [HttpPost]
    [Authorize]
    [ProducesResponseType(typeof(InventoryItemDto), StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<ActionResult<InventoryItemDto>> Create([FromBody] CreateInventoryItemDto dto)
    {
        var userId = User.Identity?.Name;
        var item = await _inventoryService.CreateAsync(dto, userId);
        return CreatedAtAction(nameof(GetById), new { id = item.Id }, item);
    }

    /// <summary>
    /// Update an existing inventory item
    /// </summary>
    [HttpPut("{id:int}")]
    [Authorize]
    [ProducesResponseType(typeof(InventoryItemDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<InventoryItemDto>> Update(int id, [FromBody] UpdateInventoryItemDto dto)
    {
        var userId = User.Identity?.Name;
        var item = await _inventoryService.UpdateAsync(id, dto, userId);
        if (item == null)
        {
            return NotFound(new { message = $"Inventory item with ID {id} not found" });
        }
        return Ok(item);
    }

    /// <summary>
    /// Delete an inventory item
    /// </summary>
    [HttpDelete("{id:int}")]
    [Authorize]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Delete(int id)
    {
        var result = await _inventoryService.DeleteAsync(id);
        if (!result)
        {
            return NotFound(new { message = $"Inventory item with ID {id} not found" });
        }
        return NoContent();
    }

    /// <summary>
    /// Adjust inventory quantity (increase or decrease)
    /// </summary>
    [HttpPost("{id:int}/adjust")]
    [Authorize]
    [ProducesResponseType(typeof(InventoryItemDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<ActionResult<InventoryItemDto>> AdjustQuantity(int id, [FromBody] AdjustQuantityDto dto)
    {
        var userId = User.Identity?.Name;
        var item = await _inventoryService.AdjustQuantityAsync(id, dto, userId);
        if (item == null)
        {
            return NotFound(new { message = $"Inventory item with ID {id} not found" });
        }
        return Ok(item);
    }

    /// <summary>
    /// Get items that are at or below reorder level
    /// </summary>
    [HttpGet("low-stock")]
    [ProducesResponseType(typeof(IEnumerable<InventoryItemDto>), StatusCodes.Status200OK)]
    public async Task<ActionResult<IEnumerable<InventoryItemDto>>> GetLowStock()
    {
        var items = await _inventoryService.GetLowStockItemsAsync();
        return Ok(items);
    }
}
