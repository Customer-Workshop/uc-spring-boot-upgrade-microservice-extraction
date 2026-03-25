namespace InventoryService.Api.Configuration;

public class JwtSettings
{
    public string Secret { get; set; } = "default-secret-key-for-development-only-change-in-production";
    public string Issuer { get; set; } = "inventory-service";
    public string Audience { get; set; } = "inventory-service-clients";
    public int ExpirationInMinutes { get; set; } = 60;
}
