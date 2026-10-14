-- =====================================================
-- Política de devoluciones (texto inicial editable desde el panel de administración)
-- =====================================================

INSERT INTO politicas (tipo, titulo, contenido, activo, orden, creado_en, actualizado_en) VALUES (
  'DEVOLUCIONES',
  'Política de devoluciones',
  '## Política de devoluciones\n\n### ¿Cuándo puedo pedir una devolución?\n\nDespués de marcar un intercambio como completado, cualquiera de las dos personas puede pedir la devolución dentro de los **7 días** siguientes, desde la sección *Intercambios > Historial*.\n\n### ¿Qué motivos se aceptan?\n\n- El artículo recibido no coincide con la descripción o las fotos.\n- El artículo tiene fallas que no fueron informadas.\n- Las dos partes acuerdan deshacer el intercambio.\n\n### ¿Cómo sigue?\n\n1. Explicás el motivo al pedir la devolución.\n2. La otra persona recibe un aviso y la acepta o la rechaza.\n3. Si se acepta, el intercambio queda como *Devuelto*, los artículos vuelven a estar disponibles y cada persona coordina por el chat la entrega de vuelta.\n4. Si se rechaza y no hay acuerdo, podés abrir una consulta en *Soporte* para que el equipo medie.\n\n### Importante\n\nEconomía Circular facilita el contacto entre las partes pero no se hace cargo del envío ni del estado de los artículos. Te recomendamos revisar el artículo al momento de la entrega.',
  1, 5, NOW(), NOW()
);
