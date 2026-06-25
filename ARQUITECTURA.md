# ARQUITECTURA - BiciPUCP

## Pregunta 1: Patrón arquitectónico global

**Patrón aplicado: Basado en servicios**

Nota: Si bien es cierto que en la ppt aparecen otros patrones en clase el profesor no explico esos, por lo que segun consulte en el laboratorio voy a colocar el que mas se adapta de los que explico

La solución no es monolítica: el backend está descompuesto en **tres procesos independientes** (`eureka-server`, `pucp-validador-service`, `orquestador-service`), cada uno con su propio ciclo de vida, puerto y despliegue.

Hay **service discovery** (Eureka) que desacopla productores y consumidores: el orquestador no conoce la dirección física del validador, lo encuentra por su nombre lógico.

Hay **separación de responsabilidades**: el validador encapsula **reglas de negocio puras** (formato del código, integridad del PIN); el orquestador encapsula la **lógica de coordinación** (compone llamadas, arma el JSON de respuesta, decide aprobado/rechazado).

Los servicios se comunican por **HTTP/REST** sobre contratos estables, lo que permite escalarlos, versionarlos o reemplazarlos independientemente.

Por lo que al cada uno ser independiente de los otros pero al compartir la BD entonces es basado en sercicios

## Pregunta 2: Statelessness del validador

El `pucp-validador-service` cumple la restricción **Stateless** de REST porque:

- Cada petición a `/validar/alumno/{codigo}` o `/validar/candado/{pin}` contiene **toda la información** necesaria para procesarla (el código o el PIN viajan en la URL).
- El servicio **no guarda sesión**, no usa cookies, no escribe en disco ni en memoria entre peticiones. No hay variable de instancia que recuerde quién llamó antes.
- La respuesta depende **únicamente** del input + reglas de negocio fijas; dos llamadas idénticas devuelven exactamente lo mismo (idempotencia).
- Esto permite escalarlo horizontalmente: cualquier réplica detrás de un load balancer puede atender cualquier petición sin coordinarse con las otras.
