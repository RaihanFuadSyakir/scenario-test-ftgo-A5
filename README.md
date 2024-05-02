# Pengujian Skenario oleh Kelompok A5
* **Penambahan order** melalui ```localhost:8082:/orders``` method **POST**
* **Perubahan order** melalui ```localhost:8082:/orders/{orderId}/revise``` method **POST**
* **Pembatalan order** melalui ```localhost:8082:/orders/{orderId}/cancel``` method **POST**
## Scenario Test
Penambahan Order (create order)
  1. Uji Create Order dengan consumer id dan restaurant id yang valid
  2. Uji Create Order dengan Consumer Id yang Invalid
  3. Uji Create Order dengan Restaurant Id yang invalid
     
Perubahan Order (revise order)
  1. Uji Revise Order dengan Order Id dan menuItem Id yang valid
  2. Uji Revise Order dengan Order Id yang valid dan menuItem Id yang invalid
  3. Uji Revise Order dengan menuItem Id yang valid dan Order Id yang invalid
  4. Uji Revise Order dengan Order Id dan menuItem Id yang invalid
     
Pembatalan Order (cancel order)
  1. Uji Cancel Order dengan Order Id yang valid
  2. Uji Cancel Order dengan Order Id yang invalid
