using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace InventoryService.Models;

[Table("stock_movements")]
public class StockMovement
{
    [Key]
    [Column("id")]
    public Guid Id { get; set; } = Guid.NewGuid();

    [Required]
    [Column("inventory_item_id")]
    public Guid InventoryItemId { get; set; }

    [ForeignKey("InventoryItemId")]
    public InventoryItem? InventoryItem { get; set; }

    [Required]
    [Column("movement_type")]
    [MaxLength(50)]
    public string MovementType { get; set; } = string.Empty; // IN, OUT, ADJUSTMENT

    [Column("quantity")]
    public int Quantity { get; set; }

    [Column("previous_quantity")]
    public int PreviousQuantity { get; set; }

    [Column("new_quantity")]
    public int NewQuantity { get; set; }

    [MaxLength(500)]
    [Column("reference")]
    public string? Reference { get; set; }

    [MaxLength(1000)]
    [Column("notes")]
    public string? Notes { get; set; }

    [Column("created_at")]
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    [MaxLength(100)]
    [Column("created_by")]
    public string? CreatedBy { get; set; }
}
