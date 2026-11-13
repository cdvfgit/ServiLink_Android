-- =====================================================================
-- ServiLink S4 · Tabla calificaciones (mínima para la demo)
-- Ejecutar en Supabase SQL Editor para que la calificación funcione.
-- =====================================================================

CREATE TABLE IF NOT EXISTS public.calificaciones (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  solicitud_id  BIGINT NOT NULL,
  autor_id      UUID,
  calificado_id UUID,
  puntuacion    SMALLINT NOT NULL CHECK (puntuacion BETWEEN 1 AND 5),
  comentario    TEXT,
  creado_en     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Permisos públicos sin RLS (demo)
ALTER TABLE public.calificaciones ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Permitir todo en calificaciones"
  ON public.calificaciones FOR ALL
  USING (true)
  WITH CHECK (true);
