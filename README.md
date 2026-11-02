# Frescura Total ERP

## Descripción

Sistema ERP de consola para **Frescura Total**, una bodega de alimentos. Controla el ciclo completo de la mercancía: desde que llega del proveedor hasta que se entrega al cliente.

Módulos:

- **Inventario y calidad:** recepción de mercancía por lotes, control de vencimientos (estados Disponible, Próximo a vencer, Vencido y En cuarentena), cuarentena, inspecciones de calidad, mermas y trazabilidad de cada lote.
- **Ventas y facturación:** registro de clientes, ventas con selección de lotes por **FEFO** (primero en vencer, primero en salir), descuento automático del 50 % a lotes próximos a vencer, IVA del 19 %, descuento de stock en una transacción y factura electrónica simulada en PNG con CUFE y código QR.
- **Logística:** panel de despachos, creación de rutas con vehículo y conductor libres, y seguimiento Planificada → En tránsito → Completada con el estado de cada venta.
- **Proveedores:** directorio, catálogo de productos por proveedor e historial de compras (lotes recibidos).

## Autores

- <<Miguel Angel Fonceca Rojas>> (GitHub: <<MiguelFonck >>)

## Requisitos previos

- Java **17 o superior** (JDK)
- Maven 3.8 o superior (viene incluido en IntelliJ IDEA)
- MySQL **8**
- Un cliente SQL, por ejemplo DBeaver
- IntelliJ IDEA (recomendado)

## Configuración de la base de datos

1. Abre `docs/database.sql` en tu cliente SQL.
2. Ejecuta **todo el script** (en DBeaver: `Alt + X`). El script elimina y vuelve a crear la base `erp_frescura_total`, crea las tablas y carga los datos de ejemplo (más de 50 filas por tabla principal).
3. Copia `config.properties.example` como `config.properties`, en la raíz del proyecto, y completa tus datos:

```
db.url=jdbc:mysql://localhost:3306/erp_frescura_total
db.user=root
db.password=tu_contraseña
```

Cambia el puerto de `db.url` si tu MySQL no usa el 3306. `config.properties` está en `.gitignore`: nunca se sube al repositorio.

## Instalación y ejecución

1. Clona el repositorio:
   ```
   git clone <<URL del repositorio>>
   ```
2. Ábrelo en IntelliJ IDEA y deja que Maven descargue las dependencias (MySQL Connector/J y ZXing).
3. Crea `config.properties` como se explicó arriba. Debe quedar en la raíz del proyecto, porque el programa lo busca en el directorio desde el que se ejecuta.
4. Ejecuta la clase `com.frescuratotal.erp.Main`.

Para solo compilar desde la terminal: `mvn compile`.

Las facturas se generan como imágenes en la carpeta `invoices/`, que se crea al emitir la primera factura.

## Menú principal

```
1. Inventory
2. Sales
3. Logistics
4. Suppliers
5. Lot traceability
0. Exit
```

## Estructura del proyecto

```
docs/
  database.sql          Script DDL + DML
  diagrama_er.png       Diagrama entidad-relación
  diagrama_clases.png   Diagrama de clases
src/main/java/com/frescuratotal/erp/
  Main.java             Menú principal
  common/
    db/                 DatabaseConnection (conexión JDBC)
    util/               ConsoleView (lectura y escritura por consola)
  inventory/            controller, model (records, enum, DAO), service, view
  sales/                controller, model, service (FEFO, descuentos, factura), view
  logistics/            controller, model, view
  suppliers/            controller, model, view
config.properties.example
```

Cada módulo sigue la estructura **MVC por componentes**: `model` (datos y acceso a la base), `view` (entrada y salida por consola) y `controller` (flujo del menú). Las reglas de negocio con lógica propia viven en `service`.

## Principios SOLID y patrones de diseño

**Patrones**

- **MVC por componentes:** cada módulo separa el modelo, la vista de consola y el controlador.
- **DAO (Data Access Object):** todo el SQL está en clases `*Dao` (`LotDao`, `SaleDao`, `DeliveryDao`, `SupplierDao`, ...). Los controladores nunca escriben SQL.
- **Capa de servicios:** reglas de negocio con lógica propia en clases `*Service` o con una sola tarea (`ExpiryCheckService`, `FefoLotSelector`, `DiscountService`, `InvoiceGenerator`).
- **Objetos de datos inmutables:** los modelos simples son `record` de Java; los estados de lote son un `enum` (`LotStatus`).

**SOLID**

- **S, responsabilidad única:** cada clase hace una sola cosa. Por ejemplo, `FefoLotSelector` solo elige lotes, `DiscountService` solo calcula descuentos e `InvoiceGenerator` solo dibuja y registra la factura.
- **Encapsulamiento:** los atributos son privados o inmutables y se accede a ellos por métodos.
- **O, abierto/cerrado:** agregar un estado de lote nuevo o una vista nueva no obliga a reescribir las clases existentes: se extiende `ConsoleView` o se agrega un valor al `enum`.
- **L, sustitución de Liskov:** `InventoryView`, `SalesView`, `LogisticsView` y `SupplierView` extienden `ConsoleView` y pueden usarse donde se espera una `ConsoleView`.
- **D, inversión de dependencias (parcial):** los controladores dependen de los DAO y de servicios, no del SQL. Como mejora futura, los DAO podrían exponerse mediante interfaces.

## Decisiones de diseño

- Un lote **rechazado en inspección** queda en estado Vencido con cantidad 0 y se registra una merma por toda su cantidad, para conservar el historial.
- Las ventas solo toman lotes **Disponibles** o **Próximos a vencer**. Los lotes en cuarentena o vencidos se omiten.
- Las transacciones (`commit` y `rollback`) protegen la venta (con el descuento de stock) y los cambios de estado de las rutas.
- La factura electrónica es una **simulación**: el CUFE es un hash SHA-384 y el QR apunta a una URL de consulta de ejemplo.
- Los datos de ejemplo fueron generados con IA. Según el enunciado, el proveedor con id 33 tiene el NIT `SYN-FT-900427-AG`.