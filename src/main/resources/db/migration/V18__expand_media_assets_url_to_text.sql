-- Allow storing long URLs/data-URIs for local inline media fallback
ALTER TABLE media_assets
ALTER COLUMN url TYPE TEXT;