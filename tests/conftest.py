"""Pytest configuration and shared fixtures."""

import os
import sys

import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Add project root to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from db.models import Base


@pytest.fixture
def db_session():
    """Create a fresh in-memory database session for each test."""
    engine = create_engine("sqlite:///:memory:", connect_args={"check_same_thread": False})
    Base.metadata.create_all(bind=engine)
    Session = sessionmaker(bind=engine)
    session = Session()
    yield session
    session.close()


@pytest.fixture
def sample_swagger_v2():
    """Sample Swagger 2.0 spec (Petstore)."""
    return {
        "swagger": "2.0",
        "info": {"title": "Petstore", "version": "1.0"},
        "host": "petstore.swagger.io",
        "basePath": "/v2",
        "paths": {
            "/pets": {
                "get": {
                    "summary": "List all pets",
                    "operationId": "listPets",
                    "parameters": [
                        {"name": "limit", "in": "query", "type": "integer", "default": 10}
                    ],
                    "responses": {"200": {"description": "A list of pets"}},
                },
                "post": {
                    "summary": "Create a pet",
                    "operationId": "createPets",
                    "parameters": [
                        {
                            "name": "body",
                            "in": "body",
                            "schema": {
                                "type": "object",
                                "properties": {
                                    "name": {"type": "string"},
                                    "tag": {"type": "string"},
                                    "id": {"type": "integer"},
                                },
                            },
                        }
                    ],
                    "responses": {"201": {"description": "Created"}},
                },
            },
            "/pets/{petId}": {
                "get": {
                    "summary": "Get pet by ID",
                    "operationId": "showPetById",
                    "parameters": [
                        {"name": "petId", "in": "path", "type": "string", "required": True}
                    ],
                    "responses": {"200": {"description": "Pet info"}},
                }
            },
        },
    }


@pytest.fixture
def sample_swagger_v3():
    """Sample OpenAPI 3.x spec."""
    return {
        "openapi": "3.0.0",
        "info": {"title": "Petstore", "version": "1.0.0"},
        "paths": {
            "/pets": {
                "get": {
                    "summary": "List all pets",
                    "parameters": [
                        {"name": "limit", "in": "query", "schema": {"type": "integer"}}
                    ],
                    "responses": {"200": {"description": "OK"}},
                },
                "post": {
                    "summary": "Create a pet",
                    "requestBody": {
                        "content": {
                            "application/json": {
                                "schema": {
                                    "type": "object",
                                    "properties": {
                                        "name": {"type": "string", "example": "Fido"},
                                        "tag": {"type": "string"},
                                        "id": {"type": "integer", "example": 1},
                                    },
                                }
                            }
                        }
                    },
                    "responses": {"201": {"description": "Created"}},
                },
            },
            "/pets/{petId}": {
                "get": {
                    "summary": "Get pet by ID",
                    "parameters": [
                        {"name": "petId", "in": "path", "schema": {"type": "string"}, "required": True}
                    ],
                    "responses": {"200": {"description": "OK"}},
                },
                "delete": {
                    "summary": "Delete a pet",
                    "parameters": [
                        {"name": "petId", "in": "path", "schema": {"type": "string"}, "required": True}
                    ],
                    "responses": {"204": {"description": "Deleted"}},
                },
            },
        },
    }
