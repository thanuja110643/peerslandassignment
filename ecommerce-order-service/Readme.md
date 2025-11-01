1.CreateOrder
[
{
"productId": 101,
"quantity": 2,
"price": 29.99
},
{
"productId": 202,
"quantity": 1,
"price": 49.99
}
]
=======================================

Get Order by ID — GET /api/orders/{id}
{
"id": 1,
"status": "PENDING",
"items": [
{
"productId": 101,
"quantity": 2,
"price": 29.99
},
{
"productId": 202,
"quantity": 1,
"price": 49.99
}
],
"total": 109.97,
"createdAt": "2025-10-31T12:34:56"
}
=======================================

[
{
"id": 1,
"status": "PENDING",
"items": [
{
"productId": 101,
"quantity": 2,
"price": 29.99
}
],
"total": 59.98,
"createdAt": "2025-10-31T12:34:56"
},
{
"id": 2,
"status": "PENDING",
"items": [
{
"productId": 202,
"quantity": 1,
"price": 49.99
}
],
"total": 49.99,
"createdAt": "2025-10-31T12:50:00"
}
]

=====================================

 Update Status — PUT /api/orders/{id}/status?status=SHIPPED
=====================================

Cancel Order — POST /api/orders/{id}/cancel     