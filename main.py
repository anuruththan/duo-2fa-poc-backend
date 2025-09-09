#!/usr/bin/env python3
"""
FastAPI Data Aggregator Application

This application provides multiple source endpoints and an aggregation endpoint
that combines data from all sources.
"""

import asyncio
from typing import Dict, Any
import httpx
from fastapi import FastAPI, HTTPException
import uvicorn

# Create FastAPI application instance
app = FastAPI(
    title="Data Aggregator API",
    description="API with multiple source endpoints and data aggregation",
    version="1.0.0"
)

# Mock data for source endpoints
SOURCE1_DATA = {
    "source": "source1",
    "data": {
        "users": ["alice", "bob", "charlie"],
        "total_users": 3,
        "status": "active"
    },
    "timestamp": "2024-01-01T10:00:00Z"
}

SOURCE2_DATA = {
    "source": "source2", 
    "data": {
        "products": ["laptop", "phone", "tablet"],
        "total_products": 3,
        "inventory_status": "in_stock"
    },
    "timestamp": "2024-01-01T10:05:00Z"
}


@app.get("/")
async def root():
    """Root endpoint with API information."""
    return {
        "message": "Data Aggregator API",
        "version": "1.0.0",
        "available_endpoints": [
            "/source1",
            "/source2", 
            "/aggregate",
            "/docs"
        ]
    }


@app.get("/source1")
async def get_source1_data():
    """Get data from source 1 - User data."""
    return SOURCE1_DATA


@app.get("/source2") 
async def get_source2_data():
    """Get data from source 2 - Product data."""
    return SOURCE2_DATA


@app.get("/aggregate")
async def aggregate_data():
    """
    Aggregate data from all source endpoints.
    
    This endpoint fetches data from /source1 and /source2 endpoints
    and combines them into a single response.
    """
    try:
        # Use httpx to make requests to our own endpoints
        async with httpx.AsyncClient(base_url="http://127.0.0.1:8000") as client:
            # Fetch data from both source endpoints concurrently
            source1_response = await client.get("/source1")
            source2_response = await client.get("/source2")
            
            # Check if both requests were successful
            source1_response.raise_for_status()
            source2_response.raise_for_status()
            
            # Parse JSON responses
            source1_data = source1_response.json()
            source2_data = source2_response.json()
            
            # Combine the data
            aggregated_result = {
                "aggregated_at": "2024-01-01T10:10:00Z",
                "total_sources": 2,
                "sources": {
                    "source1": source1_data,
                    "source2": source2_data
                },
                "summary": {
                    "total_users": source1_data["data"]["total_users"],
                    "total_products": source2_data["data"]["total_products"],
                    "all_statuses": [
                        source1_data["data"]["status"],
                        source2_data["data"]["inventory_status"]
                    ]
                }
            }
            
            return aggregated_result
            
    except httpx.RequestError as e:
        raise HTTPException(
            status_code=503, 
            detail=f"Error connecting to source endpoints: {str(e)}"
        )
    except httpx.HTTPStatusError as e:
        raise HTTPException(
            status_code=e.response.status_code,
            detail=f"Source endpoint returned error: {e.response.text}"
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Internal server error: {str(e)}"
        )


if __name__ == "__main__":
    print("Starting Data Aggregator API...")
    print("Available endpoints:")
    print("  - GET /          : API information")
    print("  - GET /source1   : User data source")
    print("  - GET /source2   : Product data source") 
    print("  - GET /aggregate : Combined data from all sources")
    print("  - GET /docs      : Interactive API documentation")
    print("\nServer starting on http://127.0.0.1:8000")
    
    # Run the application with uvicorn
    uvicorn.run(
        "main:app",
        host="127.0.0.1",
        port=8000,
        reload=True,
        log_level="info"
    )