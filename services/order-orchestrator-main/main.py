from fastapi import FastAPI
from app.routes.orders import router as orders_router
from app.routes.health import router as health_router
from app.config import Settings

settings = Settings()
app = FastAPI(title="Order Orchestrator", openapi_url=settings.openapi_url)
app.include_router(orders_router, prefix="/orders", tags=["orders"])
app.include_router(health_router, prefix="/orders/health", tags=["orders"])
