from fastapi import APIRouter

router = APIRouter()

@router.get("", summary="API healthcheck")
async def create_order():
    return { "status": "up" }
