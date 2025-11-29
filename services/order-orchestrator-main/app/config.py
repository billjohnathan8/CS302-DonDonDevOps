from functools import lru_cache
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    inventory_service_url: str = "http://localhost:8081"
    payment_service_url: str = "http://localhost:8083"
    promotions_service_url: str = "http://localhost:8082"
    openapi_url: str = ""
    
    class Config:
        env_file = ".env"

@lru_cache
def get_settings() -> Settings:
    return Settings()
