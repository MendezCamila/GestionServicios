# Especificación de Requisitos de Software  
### Sistema de Facturación de Servicios

El propósito de este documento es describir los **requisitos funcionales** del sistema de **Facturación de Servicios**, cuyo objetivo es permitir a una empresa gestionar las cuentas de sus clientes, emitir facturación masiva o individual, registrar pagos (totales o parciales), anular facturas y llevar el control del estado de cada cuenta.

El sistema será de **uso interno**, es decir, los clientes no accederán directamente al sistema, sino que las operaciones serán realizadas exclusivamente por los **administradores y empleados autorizados** de la empresa.

---

## Alcance del sistema

El sistema permitirá:

- Registrar clientes con su información fiscal y condiciones de IVA.  
- Administrar los servicios contratados por cada cliente.  
- Emitir facturación masiva (por período) o individual.  
- Registrar pagos (completos o parciales) y emitir recibos.  
- Gestionar anulaciones de facturas y mantener el registro histórico.  
- Consultar estados de cuenta, saldos pendientes y saldos a favor.  
- Llevar registro de las operaciones de facturación masiva e individual.  

> Por el momento, no se contempla conexión con **AFIP** ni **ARCA**, aunque el sistema deberá estar preparado para una posible futura integración.

---

## Descripción general

El sistema funcionará como un **módulo interno de gestión (ERP)** utilizado por el personal administrativo.  
No habrá acceso público ni autenticación de clientes externos.

### Módulos principales:
1. Gestión de Clientes y Servicios  
2. Facturación Masiva  
3. Facturación Individual  
4. Gestión de Pagos  
5. Anulación de Facturas  
6. Reportes y Consultas  

### Tipos de usuario:
- **Administrador:** acceso completo (gestiona clientes, servicios, facturas, anulaciones y pagos).  
- **Empleado:** puede registrar pagos, emitir facturas individuales y consultar cuentas.

### Suposiciones y dependencias:
- Las condiciones fiscales se basan en la **legislación argentina sobre IVA**.  
- La empresa emisora está registrada como **Responsable Inscripto**.  
- Métodos de pago: efectivo, transferencia, tarjeta, etc.  
- No se aplicarán recargos por pago fuera de término.  
- La facturación masiva se iniciará manualmente por el administrador.

---

## Requisitos Específicos

### Requisitos Funcionales

#### RF1 — Gestión de Clientes
- RF1.1: Registrar, editar, listar y desactivar clientes.  
- RF1.2: Registrar la condición fiscal (Responsable Inscripto, Monotributista, etc.).  
- RF1.3: Cada cliente tendrá un estado (Activo, Inactivo, Suspendido).  
- RF1.4: Consultar los servicios contratados por cliente.  

#### RF2 — Gestión de Servicios
- RF2.1: Registrar los servicios ofrecidos por la empresa.  
- RF2.2: Cada servicio tendrá concepto, importe y tipo de IVA.  
- RF2.3: Permitir baja lógica sin afectar facturas previas.  

#### RF3 — Facturación Masiva
- RF3.1: El administrador podrá iniciar un proceso de facturación masiva.  
- RF3.2: Solo se generarán facturas para cuentas activas.  
- RF3.3: Cada proceso registrará:  
  - Fecha de emisión  
  - Fecha de vencimiento  
  - Cantidad de facturas generadas  
- RF3.4: Calcular el IVA según el tipo de servicio.  

#### RF4 — Facturación Individual
- RF4.1: Emitir factura individual a cliente específico.  
- RF4.2: Permitir concepto personalizado y descuentos.  
- RF4.3: Registrar usuario que emitió la factura.  

#### RF5 — Anulación de Facturas
- RF5.1: Permitir anular una factura con motivo.  
- RF5.2: Una factura anulada no podrá anularse nuevamente.  
- RF5.3: Mantener historial de facturas anuladas.  
- RF5.4: Consultar motivos de anulación.  

#### RF6 — Gestión de Pagos
- RF6.1: Registrar pagos parciales o totales.  
- RF6.2: Permitir múltiples métodos de pago.  
- RF6.3: Emitir recibo con:  
  - Cliente  
  - Fecha  
  - Importe total y detalle  
  - Métodos de pago  
- RF6.4: Actualizar saldo de cuenta (pendiente / a favor).  
- RF6.5: Listar facturas impagas y permitir pagos parciales.  

#### RF7 — Consultas y Reportes
- RF7.1: Listar facturas impagas, pagas, anuladas y vencidas.  
- RF7.2: Mostrar detalle de facturación por cliente.  
- RF7.3: Consultar detalle de recibo de pago.  
- RF7.4: Visualizar estado de cuenta completo del cliente.  

---

## Enunciado del problema

Actualmente, los procesos de facturación y registro de pagos se realizan **de forma manual**, lo que genera errores, demoras y dificultad para mantener actualizada la información contable.  

El sistema busca:
- Automatizar la generación de facturas.  
- Controlar pagos parciales o totales.  
- Brindar al administrador una visión clara del estado de cada cuenta.  

---

## Clientes potenciales

- **Administradores:** generan facturación masiva o individual, controlan cuentas y pagos.  
- **Empleados contables:** gestionan comprobantes, recibos y trazabilidad.  
- **Clientes externos:** sin acceso al sistema (uso interno únicamente).  

---

## Solución propuesta

Desarrollo de un **sistema web interno** que centralice la gestión de facturas, servicios y pagos.  

Permitirá:
- Emitir facturas masivas o individuales.  
- Registrar pagos totales o parciales con recibos.  
- Consultar estados de cuenta, facturas impagas y saldos a favor.  
- Mantener trazabilidad sobre emisión y anulación de facturas.  
- Registrar operaciones de facturación masiva y motivos de anulación.  

---

## Historias de Usuario

| Rol | Deseo | Para |
|-----|--------|------|
| Administrador | Generar facturación masiva | Mantener la cobranza actualizada |
| Administrador | Registrar datos de facturación masiva | Trazabilidad del proceso |
| Administrador | Emitir facturas individuales con descuentos | Casos especiales |
| Administrador | Registrar pagos totales o parciales | Control de cobranzas |
| Administrador | Visualizar estado de cuenta | Seguimiento de clientes |
| Administrador | Registrar motivos de anulación | Transparencia |
| Administrador | Facturar solo cuentas activas | Evitar errores |
| Administrador | Gestionar servicios e importes | Control de productos facturados |
| Administrador | Emitir recibos con detalle | Respaldo contable |

---

## Arquitectura de Software

**Tipo:** Aplicación Web — Arquitectura Cliente-Servidor  

- **Cliente:** Interfaz web (HTML, CSS, JavaScript, React o Thymeleaf).  
- **Servidor:** Aplicación Java con **Spring Boot**, estructurada en capas:
  - Controladores  
  - Servicios  
  - Repositorios  
- **Base de datos:** MySQL o PostgreSQL, gestionada mediante **Spring Data JPA (Hibernate)**.  

---

## Glosario

| Término | Descripción |
|----------|-------------|
| **IVA** | Impuesto al Valor Agregado |
| **Cuenta Activa** | Cliente habilitado para facturación |
| **Factura vencida** | Factura cuyo vencimiento ha pasado (sin recargo) |
| **Pago parcial** | Pago de una parte del total |
| **Saldo a favor** | Crédito del cliente aplicable a futuras facturas |
| **Recibo** | Comprobante emitido al registrar un pago |

---
