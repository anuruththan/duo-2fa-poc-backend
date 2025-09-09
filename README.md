# Data Aggregator API

A FastAPI application that provides multiple data source endpoints and aggregates data from them into a single unified endpoint.

## Features

- **Multiple Source Endpoints**: `/source1` and `/source2` that return mock data
- **Data Aggregation**: `/aggregate` endpoint that fetches and combines data from all source endpoints
- **Interactive Documentation**: Automatic OpenAPI/Swagger documentation at `/docs`
- **Direct Execution**: Run with `python main.py` instead of requiring uvicorn commands

## Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/anuruththan/duo-2fa-poc-backend.git
   cd duo-2fa-poc-backend
   ```

2. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

## Running the Application

Start the server with:
```bash
python main.py
```

The application will start on `http://127.0.0.1:8000`

You should see output similar to:
```
Starting Data Aggregator API...
Available endpoints:
  - GET /          : API information
  - GET /source1   : User data source
  - GET /source2   : Product data source
  - GET /aggregate : Combined data from all sources
  - GET /docs      : Interactive API documentation

Server starting on http://127.0.0.1:8000
```

## API Endpoints

### GET `/`
Returns basic API information and available endpoints.

### GET `/source1`
Returns mock user data.

**Example Response**:
```json
{
  "source": "source1",
  "data": {
    "users": ["alice", "bob", "charlie"],
    "total_users": 3,
    "status": "active"
  },
  "timestamp": "2024-01-01T10:00:00Z"
}
```

### GET `/source2`
Returns mock product data.

**Example Response**:
```json
{
  "source": "source2",
  "data": {
    "products": ["laptop", "phone", "tablet"],
    "total_products": 3,
    "inventory_status": "in_stock"
  },
  "timestamp": "2024-01-01T10:05:00Z"
}
```

### GET `/aggregate`
Fetches data from both source endpoints and returns combined results.

**Example Response**:
```json
{
  "aggregated_at": "2024-01-01T10:10:00Z",
  "total_sources": 2,
  "sources": {
    "source1": {
      "source": "source1",
      "data": {
        "users": ["alice", "bob", "charlie"],
        "total_users": 3,
        "status": "active"
      },
      "timestamp": "2024-01-01T10:00:00Z"
    },
    "source2": {
      "source": "source2",
      "data": {
        "products": ["laptop", "phone", "tablet"],
        "total_products": 3,
        "inventory_status": "in_stock"
      },
      "timestamp": "2024-01-01T10:05:00Z"
    }
  },
  "summary": {
    "total_users": 3,
    "total_products": 3,
    "all_statuses": ["active", "in_stock"]
  }
}
```

## Testing with curl

Once the server is running, you can test the endpoints using curl:

### Test the root endpoint:
```bash
curl http://127.0.0.1:8000/
```

### Test source1 endpoint:
```bash
curl http://127.0.0.1:8000/source1
```

### Test source2 endpoint:
```bash
curl http://127.0.0.1:8000/source2
```

### Test the aggregate endpoint:
```bash
curl http://127.0.0.1:8000/aggregate
```

### Pretty print JSON responses:
```bash
curl -s http://127.0.0.1:8000/aggregate | python -m json.tool
```

## Interactive Documentation

Visit `http://127.0.0.1:8000/docs` in your browser to access the interactive Swagger UI documentation where you can test all endpoints directly.

## Project Structure

```
duo-2fa-poc-backend/
├── main.py              # Main FastAPI application
├── requirements.txt     # Python dependencies
└── README.md           # This documentation
```

## Dependencies

- **FastAPI**: Modern, fast web framework for building APIs
- **uvicorn**: ASGI server for running the FastAPI application
- **httpx**: Async HTTP client for making requests between endpoints