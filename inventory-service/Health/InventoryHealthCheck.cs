using Microsoft.Extensions.Diagnostics.HealthChecks;
using InventoryService.Data;

namespace InventoryService.Health;

public class InventoryHealthCheck : IHealthCheck
{
    private readonly InventoryDbContext _context;

    public InventoryHealthCheck(InventoryDbContext context)
    {
        _context = context;
    }

    public async Task<HealthCheckResult> CheckHealthAsync(
        HealthCheckContext context,
        CancellationToken cancellationToken = default)
    {
        try
        {
            var canConnect = await _context.Database.CanConnectAsync(cancellationToken);
            if (canConnect)
            {
                return HealthCheckResult.Healthy("Database connection is healthy");
            }
            return HealthCheckResult.Unhealthy("Cannot connect to database");
        }
        catch (Exception ex)
        {
            return HealthCheckResult.Unhealthy("Database health check failed", ex);
        }
    }
}
