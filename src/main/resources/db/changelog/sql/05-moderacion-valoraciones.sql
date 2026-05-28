ALTER TABLE valoraciones_oferente
  ADD COLUMN aprobado BOOLEAN NOT NULL DEFAULT false;

UPDATE valoraciones_oferente
SET aprobado = true;
