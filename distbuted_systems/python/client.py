import asyncio
import json
import logging
import random
import time
import uuid

import websockets

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger(__name__)

CLIENT_ID = f"client_{str(uuid.uuid4())[:8]}"


async def send_messages(ws):
    counter = 0
    while True:
        await asyncio.sleep(random.uniform(5, 15))
        payload = {
            "id": str(uuid.uuid4()),
            "content": f"Data from {CLIENT_ID} #{counter}",
            "timestamp": time.time()
        }
        try:
            await ws.send(json.dumps(payload))
            logger.info(f"Sent: {payload['content']}")
            counter += 1
        except websockets.ConnectionClosed:
            logger.error("Connection closed")
            break


async def receive_messages(ws):
    global CLIENT_ID
    try:
        async for msg in ws:
            try:
                data = json.loads(msg)
                logger.info(f"Received: {data}")
                if data.get("type") == "info":
                    if "client_id" in data:
                        old_id = CLIENT_ID
                        CLIENT_ID = data["client_id"]
                        logger.info(f"Assigned ID: {CLIENT_ID} (was {old_id})")
                    else:
                        logger.info(f"Info: {data.get('message')}")
                elif data.get("type") == "message":
                    sender = data.get("sender_id", "Unknown")
                    logger.info(f"Message from {sender}: {data.get('data', {}).get('content')}")
                elif data.get("type") == "ack":
                    logger.info(f"Ack for message {data.get('message_id')}")
                elif data.get("type") == "error":
                    logger.warning(f"Server error: {data.get('message')}")
            except json.JSONDecodeError:
                logger.warning(f"Invalid JSON: {msg}")
    except websockets.ConnectionClosed:
        logger.info("Connection closed")


async def connect():
    uri = "ws://server:8765"
    while True:
        try:
            logger.info(f"Connecting to {uri}")
            async with websockets.connect(uri) as ws:
                logger.info("Connected")
                await asyncio.gather(
                    send_messages(ws),
                    receive_messages(ws)
                )
        except (ConnectionRefusedError, OSError) as e:
            logger.error(f"Connection failed: {e}")
        except Exception as e:
            logger.error(f"Error: {e}")
        logger.info("Retrying in 5 seconds...")
        await asyncio.sleep(5)


if __name__ == "__main__":
    try:
        asyncio.run(connect())
    except KeyboardInterrupt:
        logger.info("Client shutting down")
