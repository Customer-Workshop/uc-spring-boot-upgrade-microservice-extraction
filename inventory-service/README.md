# Inventory Microservice

Standalone inventory management microservice decomposed from the monolith application.

## Architecture

- **Backend**: .NET 8 Web API with Entity Framework Core
- **Frontend**: Angular 17 standalone components
- **Database**: SQL Server (production) / SQLite (development)
- **Container**: Multi-stage Docker build
- **Orchestration**: Helm chart with HPA, NetworkPolicy, ServiceMonitor
- **CI/CD**: GitHub Actions + ArgoCD

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/inventory` | List items (paginated, filterable) |
| GET | `/api/inventory/{id}` | Get item by ID |
| GET | `/api/inventory/sku/{sku}` | Get item by SKU |
| POST | `/api/inventory` | Create item (auth required) |
| PUT | `/api/inventory/{id}` | Update item (auth required) |
| DELETE | `/api/inventory/{id}` | Delete item (auth required) |
| POST | `/api/inventory/{id}/adjust` | Adjust stock quantity (auth required) |
| GET | `/api/inventory/low-stock` | Get items at/below reorder level |
| GET | `/api/categories` | List categories |
| POST | `/api/categories` | Create category (auth required) |
| PUT | `/api/categories/{id}` | Update category (auth required) |
| DELETE | `/api/categories/{id}` | Delete category (auth required) |
| GET | `/health` | Health check endpoint |

## Quick Start

### Backend
```bash
cd src/InventoryService.Api
dotnet run
```
API available at `http://localhost:5062`

### Run Tests
```bash
dotnet test InventoryService.sln
```

### Docker
```bash
docker build -t inventory-service .
docker run -p 8080:8080 inventory-service
```

### Helm Deploy
```bash
# Dev
helm install inventory-dev ./helm/inventory-service -f ./helm/inventory-service/values-dev.yaml -n inventory-dev

# Staging
helm install inventory-staging ./helm/inventory-service -f ./helm/inventory-service/values-staging.yaml -n inventory-staging
```

## Monolith Integration

The monolith includes `InventoryServiceClient` (HTTP client with circuit breaker) and `InventoryProxyApi` (proxy endpoints) to forward inventory requests to this microservice. Configure the monolith's `application.properties`:

```properties
inventory.service.url=http://inventory-service:8080
inventory.service.timeout=15
```

## Platform Engineering

- **Network Policy**: Restricts ingress to ingress-nginx, same namespace, and monitoring
- **ServiceMonitor**: Prometheus scraping via `/health` endpoint
- **HPA**: Auto-scales 2-10 replicas based on CPU/memory utilization
- **ArgoCD**: Automated sync with self-heal for dev and staging environments
