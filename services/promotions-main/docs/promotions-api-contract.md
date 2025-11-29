# Promotions API Contracts

Contract reference for every REST endpoint exposed by `PromotionController`. All timestamps follow ISO-8601 UTC (e.g., `2025-11-08T12:00:00Z`). Monetary values use decimal numbers.

## `GET /promotions/health`
**Request Body** â€“ none.

**Response 200**
```json
{
  "status": "ok"
}
```

## `GET /promotions`
Returns all promotions in DynamoDB.

**Response 200**
```json
[
  {
    "name": "Black Friday 2025",
    "startTime": "2025-11-22T00:00:00Z",
    "endTime": "2025-11-30T23:59:59Z",
    "discountRate": 0.25
  },
  {
    "name": "Halloween 2025",
    "startTime": "2025-10-31T00:00:00Z",
    "endTime": "2025-10-31T23:59:59Z",
    "discountRate": 0.5
  }
]
```

## `POST /promotions`
Creates a promotion. All fields are required.

**Request Body**
```json
{
  "name": "Black Friday 2025",
  "startTime": "2025-11-22T00:00:00Z",
  "endTime": "2025-11-30T23:59:59Z",
  "discountRate": 0.25
}
```

**Response 201**
```json
{
  "id": "5a3f6a4d-7c7c-4e24-8c61-b8a9490d5c6a",
  "name": "Black Friday 2025",
  "startTime": "2025-11-22T00:00:00Z",
  "endTime": "2025-11-30T23:59:59Z",
  "discountRate": 0.25
}
```

## `PUT /promotions/{promotionId}`
Fully replaces an existing promotion by id. Same payload constraints as `POST /promotions`.

**Request Body**
```json
{
  "name": "Holiday Deals",
  "startTime": "2025-12-15T00:00:00Z",
  "endTime": "2026-01-05T00:00:00Z",
  "discountRate": 0.10
}
```

**Response 200**
```json
{
  "id": "7d26bf16-620b-4d13-80b1-d8e39435a640",
  "name": "Holiday Deals",
  "startTime": "2025-12-15T00:00:00Z",
  "endTime": "2026-01-05T00:00:00Z",
  "discountRate": 0.10
}
```

## `PATCH /promotions/{promotionId}`
Applies partial updates. Any subset of fields may be provided; omitted properties remain unchanged.

**Request Body**
```json
{
  "endTime": "2026-01-07T00:00:00Z",
  "discountRate": 0.12
}
```

**Response 200**
```json
{
  "id": "7d26bf16-620b-4d13-80b1-d8e39435a640",
  "name": "Holiday Deals",
  "startTime": "2025-12-15T00:00:00Z",
  "endTime": "2026-01-07T00:00:00Z",
  "discountRate": 0.12
}
```

## `DELETE /promotions/{promotionId}`
Removes a promotion.

**Request Body** â€“ none.

**Response 204** â€“ empty body on success. `404` when the identifier does not exist.

## `POST /promotions/apply`
Evaluates which promotions apply to the provided basket. `now` is optional; when omitted the service uses the current instant.

**Request Body**
```json
{
  "now": "2025-11-25T10:00:00Z",
  "items": [
    {
      "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0",
      "quantity": 2,
      "unitPrice": 199.99
    },
    {
      "productId": "90c93487-6e2e-4748-8615-7141732f373b",
      "quantity": 1,
      "unitPrice": 59.50
    }
  ]
}
```---

# Product Promotions API Contracts

Endpoints below expose CRUD operations for the ProductPromotion mappings stored in DynamoDB (ProductPromotion table). Each mapping links a promotionId to a productId.

## GET /productpromotions/health
**Request Body** — none.

**Response 200**
`json
{
  "status": "ok"
}
`

## GET /productpromotions
Returns every mapping stored in DynamoDB.

**Response 200**
`json
[
  {
    "id": "e1a8e6b5-5d1b-46ea-9195-7b0e7c4825c2",
    "promotionId": "7d26bf16-620b-4d13-80b1-d8e39435a640",
    "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
  },
  {
    "id": "ab8caca6-9384-4d7c-8f6a-7fb2dc8f7fd8",
    "promotionId": "5a3f6a4d-7c7c-4e24-8c61-b8a9490d5c6a",
    "productId": "90c93487-6e2e-4748-8615-7141732f373b"
  }
]
`

## GET /productpromotions/{id}
Fetches a single mapping by identifier.

**Response 200**
`json
{
  "id": "e1a8e6b5-5d1b-46ea-9195-7b0e7c4825c2",
  "promotionId": "7d26bf16-620b-4d13-80b1-d8e39435a640",
  "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
}
`

**Response 404** — empty body when the mapping does not exist.

## GET /productpromotions/product/{productId}
Returns the promotions attached to the provided product.

**Response 200**
`json
[
  {
    "id": "7d26bf16-620b-4d13-80b1-d8e39435a640",
    "name": "Holiday Deals",
    "startTime": "2025-12-15T00:00:00Z",
    "endTime": "2026-01-05T00:00:00Z",
    "discountRate": 0.10
  },
  {
    "id": "5a3f6a4d-7c7c-4e24-8c61-b8a9490d5c6a",
    "name": "Black Friday 2025",
    "startTime": "2025-11-22T00:00:00Z",
    "endTime": "2025-11-30T23:59:59Z",
    "discountRate": 0.25
  }
]
`

## GET /productpromotions/promotion/{promotionId}
Returns only the product identifiers currently attached to the promotion. The list is empty when no products are linked.

**Response 200**
`json
[
  {
    "productId": "90c93487-6e2e-4748-8615-7141732f373b"
  },
  {
    "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
  }
]
`

## POST /productpromotions
Creates a new mapping. Both identifiers are required; creation fails with 400 if either is missing or references a nonexistent promotion.

**Request Body**
`json
{
  "promotionId": "7d26bf16-620b-4d13-80b1-d8e39435a640",
  "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
}
`

**Response 201**
`json
{
  "id": "e1a8e6b5-5d1b-46ea-9195-7b0e7c4825c2",
  "promotionId": "7d26bf16-620b-4d13-80b1-d8e39435a640",
  "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
}
`
Location header points to /productpromotions/{id}.

## PUT /productpromotions/{id}
Replaces the product and/or promotion on an existing mapping.

**Request Body**
`json
{
  "promotionId": "5a3f6a4d-7c7c-4e24-8c61-b8a9490d5c6a",
  "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
}
`

**Response 200**
`json
{
  "id": "e1a8e6b5-5d1b-46ea-9195-7b0e7c4825c2",
  "promotionId": "5a3f6a4d-7c7c-4e24-8c61-b8a9490d5c6a",
  "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
}
`

**Response 404** — mapping not found.

## DELETE /productpromotions/{id}
Removes the mapping with the provided identifier.

**Request Body** — none.

**Response 204** — empty body on success. 404 when the identifier does not exist.
