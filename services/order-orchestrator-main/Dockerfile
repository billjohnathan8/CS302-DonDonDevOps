FROM python:3.12-alpine
WORKDIR /opt
RUN apk add --no-cache build-base python3-dev
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY main.py .
COPY app/ app/
ENV DEBUG=0
ENV PYTHONUNBUFFERED=1
EXPOSE 8080
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8080"]
