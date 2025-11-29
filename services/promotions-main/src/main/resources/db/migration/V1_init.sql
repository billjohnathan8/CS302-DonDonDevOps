create table if not exists promotion (
  id uuid primary key,
  name varchar(255) not null,
  start_time timestamptz not null,
  end_time timestamptz not null,
  discount_rate double precision not null check (discount_rate >= 0 and discount_rate <= 1)
);

create table if not exists product_promotion (
  id uuid primary key,
  promotion_id uuid not null references promotion(id) on delete cascade,
  product_id uuid not null,
  constraint uq_product_promotion unique (promotion_id, product_id)
);

create index if not exists idx_product_promotion_product_id on product_promotion(product_id);
