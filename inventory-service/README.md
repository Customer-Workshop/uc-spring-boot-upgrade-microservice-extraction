# Inventory Microservice

A standalone microservice decomposed from the monolith application, responsible for inventory management operations.

## Architecture

- **Backend**: .NET 8 Web API with Entity Framework Core
- **Frontend**: Angular 17 standalone components
- **Database**: SQL Server (InMemory for development)
- **Metrics**: Prometheus via prometheus-net
- **Logging**: Serilog structured logging
- **Health Checks**: ASP.NET Core health checks at `/health`, `/health/ready`, `/health/live`

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/inventory` | List items (search, filter, paginate) |
| GET | `/api/v1/inventory/{id}` | Get item by ID |
| GET | `/api/v1/inventory/sku/{sku}` | Get item by SKU |
| POST | `/api/v1/inventory` | Create item |
| PUT | `/api/v1/inventory/{id}` | Update item |
| DELETE | `/api/v1/inventory/{id}` | Delete item |
| POST | `/api/v1/inventory/{id}/stock` | Adjust stock (IN/OUT/ADJUSTMENT) |
| GET | `/api/v1/inventory/{id}/movements` | Get stock movement history |
| GET | `/api/v1/inventory/low-stock` | Get low-stock alerts |
| GET | `/api/v1/inventory/categories` | Get all categories |
| GET | `/health` | Health check |
| GET | `/metrics` | Prometheus metrics |

## Development

### Prerequisites
- .NET 8 SDK
- Node.js 20+
- Angular CLI 17+

### Running the Backend
```bash
cd inventory-service
dotnet restore
dotnet run
```
The API will be available at `http://localhost:5000` with Swagger UI at `/swagger`.

### Running the Frontend
```bash
cd inventory-service/ClientApp
npm install
npm start
```
The Angular app will be available at `http://localhost:4200`.

### Docker
```bash
cd inventory-service
docker build -t inventory-service .
docker run -p 8080:8080 inventory-service
```

## Deployment

### Helm Chart
```bash
helm install inventory-service ./helm/inventory-service -n inventory-dev
```

### ArgoCD
Apply the ArgoCD application manifests:
```bash
kubectl apply -f argocd/dev/application.yaml    # Dev environment
kubectl apply -f argocd/staging/application.yaml # Staging environment
```

## Monolith Integration

The monolith has been refactored to use `InventoryServiceClient` (HTTP client) to communicate with this microservice instead of direct database access. Configure the inventory service URL in the monolith's `application.properties`:

```properties
inventory.service.url=http://inventory-service:80
```
