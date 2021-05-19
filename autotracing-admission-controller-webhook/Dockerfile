FROM python:3.9-alpine

RUN apk update && apk add gcc libc-dev libffi-dev openssl openssl-dev musl-dev python3-dev cargo

COPY requirements.txt requirements.txt

RUN  pip install -r requirements.txt

COPY webhook.py webhook.py

COPY keys/* ./

ENTRYPOINT ["python", "webhook.py"]
