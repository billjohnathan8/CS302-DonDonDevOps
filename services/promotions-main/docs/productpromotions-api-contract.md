# Product Promotions API Contracts

Endpoints below expose CRUD operations for the `ProductPromotion` mappings stored in DynamoDB (`ProductPromotion` table). Each mapping links a `promotionId` to a `productId`.

## `GET /productpromotions/health`
**Request Body** — none.

**Response 200**
```json
{
  "status": "ok"
}
```

## `GET /productpromotions`
Returns every mapping stored in DynamoDB.

**Response 200**
```json
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
```

## `GET /productpromotions/{id}`
Fetches a single mapping by identifier.

**Response 200**
```json
{
  "id": "e1a8e6b5-5d1b-46ea-9195-7b0e7c4825c2",
  "promotionId": "7d26bf16-620b-4d13-80b1-d8e39435a640",
  "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
}
```

**Response 404** — empty body when the mapping does not exist.

## `GET /productpromotions/product/{productId}`
Lists every promotion mapping for the provided product.

**Response 200**
```json
[
  {
    "id": "e1a8e6b5-5d1b-46ea-9195-7b0e7c4825c2",
    "promotionId": "7d26bf16-620b-4d13-80b1-d8e39435a640",
    "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
  }
]
```

## `GET /productpromotions/promotion/{promotionId}`
Returns only the product identifiers currently attached to the promotion. The list is empty when no products are linked.

**Response 200**
```json
[
  {
    "productId": "90c93487-6e2e-4748-8615-7141732f373b"
  },
  {
    "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
  }
]
```

## `POST /productpromotions`
Creates a new mapping. Both identifiers are required; creation fails with `400` if either is missing or references a nonexistent promotion.

**Request Body**
```json
{
  "promotionId": "7d26bf16-620b-4d13-80b1-d8e39435a640",
  "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
}
```

**Response 201**
```json
{
  "id": "e1a8e6b5-5d1b-46ea-9195-7b0e7c4825c2",
  "promotionId": "7d26bf16-620b-4d13-80b1-d8e39435a640",
  "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
}
```
`Location` header points to `/productpromotions/{id}`.

## `PUT /productpromotions/{id}`
Replaces the product and/or promotion on an existing mapping.

**Request Body**
```json
{
  "promotionId": "5a3f6a4d-7c7c-4e24-8c61-b8a9490d5c6a",
  "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
}
```

**Response 200**
```json
{
  "id": "e1a8e6b5-5d1b-46ea-9195-7b0e7c4825c2",
  "promotionId": "5a3f6a4d-7c7c-4e24-8c61-b8a9490d5c6a",
  "productId": "2c9f4451-3a93-4c8f-919b-6d9583be30e0"
}
```

**Response 404** — mapping not found.

## `DELETE /productpromotions/{id}`
Removes the mapping with the provided identifier.

**Request Body** — none.

**Response 204** — empty body on success. `404` when the identifier does not exist.
